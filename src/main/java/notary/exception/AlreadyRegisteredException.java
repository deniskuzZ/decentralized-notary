package notary.exception;

import static java.lang.String.format;

/**
 * Created by kuzmende on 4/20/16.
 */
public class AlreadyRegisteredException extends RuntimeException {

    public AlreadyRegisteredException(String ownerId) {
        super(format("Asset is already registered under %s", ownerId));
    }
}
