package notary.rest;

import notary.service.DigitalAssetService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static java.lang.String.format;

@CrossOrigin(allowCredentials = "true", allowedHeaders = "*")
@RestController
public class DigitalAssetController {

    @Value("${nxt.url}")
    private String nxtUrl;

    @Autowired
    private DigitalAssetService digitalAssetService;

    @RequestMapping(method = RequestMethod.POST, value = "/issueAsset")
    public String registerAsset(
            @RequestParam("accountId") String accountId,
            @RequestParam("file") MultipartFile file) throws IOException, InterruptedException {

        return digitalAssetService.registerAsset(accountId, file);
    }

    @RequestMapping(method = RequestMethod.POST, value = "/confirmPayment")
    public String confirmPayment(@RequestBody String transactionId) throws InterruptedException {
        return digitalAssetService.confirmPayment(transactionId);
    }

    @RequestMapping(method = RequestMethod.GET, value = "/nxtWallet")
    public void openWallet(HttpServletResponse response) throws IOException {
        response.sendRedirect(format("http://%s/index.html", nxtUrl));
    }

}
