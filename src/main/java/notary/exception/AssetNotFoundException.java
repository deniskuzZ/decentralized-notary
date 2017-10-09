package notary.exception;

import static java.lang.String.format;

/**
 * Created by kuzmende on 4/20/16.
 */
public class AssetNotFoundException extends RuntimeException {

    public AssetNotFoundException(String assetId) {
        super(format("Asset %s is not present in local storage", assetId));
    }
}
