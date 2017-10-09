package notary.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * Created by kuzmende on 3/13/16.
 */
@Document(collection = "digital_asset")
public class DigitalAsset {

    @Id
    private String id;

    private String fileName;
    private byte[] fileContent;

    private String sha256Hex;
    private TransactionData transactionData;

    public DigitalAsset() {
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public byte[] getFileContent() {
        return fileContent;
    }

    public void setFileContent(byte[] fileContent) {
        this.fileContent = fileContent;
    }

    public String getSha256Hex() {
        return sha256Hex;
    }

    public void setSha256Hex(String sha256Hex) {
        this.sha256Hex = sha256Hex;
    }

    public TransactionData getTransactionData() {
        return transactionData;
    }

    public void setTransactionData(TransactionData transactionData) {
        this.transactionData = transactionData;
    }

    @Override
    public String toString() {
        return String.format(
                "DigitalProperty[id=%s, owner='%s', fileName='%s']",
                id, transactionData.getSender(), fileName);
    }


    public static final class DigitalAssetBuilder {

        private String fileName;
        private byte[] fileContent;

        private String sha256Hex;
        private TransactionData transactionData;

        private DigitalAssetBuilder(MultipartFile file) throws IOException {
            this.fileName = file.getOriginalFilename();
            this.fileContent = file.getBytes();
        }

        public static DigitalAssetBuilder newInstance(MultipartFile file) throws IOException {
            return new DigitalAssetBuilder(file);
        }

        public DigitalAssetBuilder withSha256Hex(String sha256Hex) {
            this.sha256Hex = sha256Hex;
            return this;
        }

        public DigitalAssetBuilder withTransactionData(TransactionData transactionData) {
            this.transactionData = transactionData;
            return this;
        }

        public DigitalAsset build() {
            DigitalAsset digitalAsset = new DigitalAsset();
            digitalAsset.setFileName(fileName);

            digitalAsset.setFileContent(fileContent);
            digitalAsset.setSha256Hex(sha256Hex);
            digitalAsset.setTransactionData(transactionData);

            return digitalAsset;
        }
    }

}
