package notary.repositry;

import notary.domain.DigitalAsset;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;

/**
 * Created by kuzmende on 3/13/16.
 */
public interface DigitalAssetRepository extends MongoRepository<DigitalAsset, String> {

    @Query(value = "{'transactionData.transactionId' : ?0 }")
    DigitalAsset findByTransactionId(String transactionId);

}
