package notary.service;

import com.jayway.jsonpath.JsonPath;
import notary.domain.DigitalAsset;
import notary.domain.DigitalAsset.DigitalAssetBuilder;
import notary.domain.TransactionData;
import notary.domain.TransactionData.TransactionDataBuilder;
import notary.exception.AlreadyRegisteredException;
import notary.exception.AssetNotFoundException;
import notary.repositry.DigitalAssetRepository;
import org.apache.commons.codec.digest.DigestUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigInteger;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

import static java.lang.String.format;

/**
 * Created by kuzmende on 4/20/16.
 */
@Service
public class DigitalAssetService {

    private static final int TRANSACTION = 4;
    private static final String UNKNOWN_ASSET = "Unknown asset";

    private final RestTemplate restTemplate = new RestTemplate();

    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyMMddHHmm");

    @Value("${nxt.uri}")
    private String nxtUri;

    @Value("${nxt.account}")
    private String nxtAccount;

    @Value("${nxt.secret.phrase}")
    private String nxtSecret;

    @Autowired
    private DigitalAssetRepository repository;


    public String registerAsset(String accountId, MultipartFile file) throws IOException, InterruptedException {
        String sha256Hex = DigestUtils.sha256Hex(file.getInputStream());

        Optional<String> assetId = getUnassignedAssetIdBySha256(sha256Hex);

        if (assetId.isPresent()) {
            return assetId.get();
        }

        TransactionData transactionData = uploadToBlockChain(accountId, sha256Hex);

        DigitalAsset digitalAsset = DigitalAssetBuilder.newInstance(file)
                .withSha256Hex(sha256Hex)
                .withTransactionData(transactionData)
                .build();

        repository.save(digitalAsset);

        return transactionData.getTransactionId();
    }

    public String confirmPayment(String transactionId) throws InterruptedException {
        DigitalAsset digitalAsset = repository.findByTransactionId(transactionId);
        getUnassignedAssetById(transactionId);

        if (digitalAsset == null) {
            throw new AssetNotFoundException(transactionId);
        }
        TransactionData data = digitalAsset.getTransactionData();

        waitForPaymentComplete(data);
        broadcastTransaction(data);

        return transactionId;
    }

    private Optional<String> getUnassignedAssetIdBySha256(String sha256Hex) {
        String searchUrl = nxtUri + "?requestType=searchAssets&query={sha256Hex}";

        String jsonData = restTemplate.getForObject(searchUrl, String.class, sha256Hex);
        JSONArray assets = new JSONObject(jsonData).getJSONArray("assets");

        if (assets.length() != 0) {
            String asset = assets.getJSONObject(0).getString("asset");

            return getUnassignedAssetById(asset);
        }

        return Optional.empty();
    }

    private Optional<String> getUnassignedAssetById(String assetId) {
        String searchUrl = nxtUri + "?requestType=getAssetTransfers&asset={id}";

        String jsonData = restTemplate.getForObject(searchUrl, String.class, assetId);
        JSONArray transfers = new JSONObject(jsonData).getJSONArray("transfers");

        if (transfers.length() != 0) {
            String assetOwner = transfers.getJSONObject(0).getString("recipientRS");

            if (!assetOwner.equals(nxtAccount)) {
                throw new AlreadyRegisteredException(assetOwner);
            }
        }
        return Optional.of(assetId);
    }

    private void waitForPaymentComplete(TransactionData data) throws InterruptedException {
        boolean paymentCompleted = false;

        String transactionsUrl = nxtUri + "?requestType=getAccountPhasedTransactions&account={account}";
        BigInteger senderId = new BigInteger(data.getSenderId());

        while (!paymentCompleted) {
            sleep();
            String jsonData = restTemplate.getForObject(transactionsUrl, String.class, senderId);

            try {
                paymentCompleted = !JsonPath.<List>read(jsonData,
                        "$.transactions[?(@.type==0)].attachment.phasingLinkedFullHashes[?(@=='" + data.getFullHash() + "')]"
                ).isEmpty();
            } catch (RuntimeException ignored) {
            }
        }
    }

    private void broadcastTransaction(TransactionData data) {
        MultiValueMap<String, Object> form = new LinkedMultiValueMap<>();

        form.add("requestType", "broadcastTransaction");
        form.add("transactionBytes", data.getTransactionBytes());

        restTemplate.postForObject(nxtUri, form, String.class);
    }

    private TransactionData uploadToBlockChain(String accountId, String sha256Hex) throws InterruptedException {
        String jsonData = issueAsset(sha256Hex);

        String transactionId = new JSONObject(jsonData).getString("transaction");
        jsonData = sendMoney();

        String fullHash = new JSONObject(jsonData).getString("fullHash");
        String unsignedTransactionBytes = new JSONObject(jsonData).getString("unsignedTransactionBytes");
        String transactionBytes = new JSONObject(jsonData).getString("transactionBytes");

        Integer blockHeight = new JSONObject(jsonData)
                .getJSONObject("transactionJSON").getInt("ecBlockHeight");

        jsonData = transferAsset(accountId, transactionId, fullHash, unsignedTransactionBytes, blockHeight);
        String sender = new JSONObject(jsonData)
                .getJSONObject("transactionJSON").getString("sender");

        return TransactionDataBuilder.newInstance()
                .withSender(accountId)
                .withSenderId(sender)
                .withFullHash(fullHash)
                .withTransactionBytes(transactionBytes)
                .withTransactionId(transactionId)
                .build();
    }

    private String issueAsset(String sha256Hex) {
        MultiValueMap<String, Object> form = new LinkedMultiValueMap<>();
        form.add("requestType", "issueAsset");
        form.add("secretPhrase", nxtSecret);

        form.add("name", LocalDateTime.now().format(formatter));
        form.add("description", sha256Hex);

        form.add("quantityQNT", 1);
        form.add("deadline", 1440);
        form.add("feeNQT", 0);

        return restTemplate.postForObject(nxtUri, form, String.class);
    }

    private String sendMoney() {
        JSONObject message = new JSONObject()
                .put("quack", 1)
                .put("trigger", 1);

        MultiValueMap<String, Object> form = new LinkedMultiValueMap<>();
        form.add("requestType", "sendMoney");

        form.add("recipient", "NXT-YTBB-LT9J-SRRR-7KLBQ");
        form.add("secretPhrase", nxtSecret);

        form.add("feeNQT", 0);
        form.add("broadcast", false);
        form.add("deadline", 1440);
        form.add("amountNQT", 250000000);

        form.add("message", message.toString());
        form.add("messageIsText", true);
        form.add("messageIsPrunable", false);
        form.add("calculateFee", true);

        return restTemplate.postForObject(nxtUri, form, String.class);
    }

    private String transferAsset(String accountId, String transactionId, String fullHash, String transactionBytes, Integer blockHeight) throws InterruptedException {
        JSONObject message = new JSONObject()
                .put("quack", 1)
                .put("recipient", accountId)
                .put("triggerBytes", transactionBytes)
                .put("assets", new JSONArray(format("[{'id':'%s','QNT':'1','type':'A'}]", transactionId)))
                .put("expected_assets", new JSONArray(format("[{'id':'1','QNT':'%d','type':'NXT'}]", 50 * 10000000)));

        MultiValueMap<String, Object> form = new LinkedMultiValueMap<>();
        form.add("requestType", "transferAsset");

        form.add("recipient", accountId);
        form.add("secretPhrase", nxtSecret);

        form.add("feeNQT", 0);
        form.add("broadcast", true);
        form.add("deadline", 720);
        form.add("asset", transactionId);
        form.add("quantityQNT", 1);

        form.add("message", message.toString());
        form.add("messageIsText", true);
        form.add("messageIsPrunable", true);
        form.add("messageToEncryptIsText", true);
        form.add("encryptedMessageIsPrunable", true);

        form.add("phased", true);
        form.add("phasingFinishHeight", blockHeight + 1450);
        form.add("phasingVotingModel", TRANSACTION);
        form.add("phasingQuorum", 1);
        form.add("phasingLinkedFullHash", fullHash);
        form.add("phasingMinBalanceModel", 0);
        form.add("calculateFee", false);

        String result = UNKNOWN_ASSET;

        while (UNKNOWN_ASSET.equals(result)) {
            sleep();
            result = restTemplate.postForObject(nxtUri, form, String.class);

            result = new JSONObject(result).optString("errorDescription", result);
        }
        return result;
    }

    private void sleep() throws InterruptedException {
        Thread.sleep(1000);
    }
}
