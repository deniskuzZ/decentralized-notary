package notary.domain;

import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Created by kuzmende on 6/16/16.
 */
@Document(collection = "transaction_data")
public class TransactionData {

    private String transactionId;

    private String sender;
    private String senderId;

    private String fullHash;
    private String transactionBytes;

    public TransactionData() {
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public String getFullHash() {
        return fullHash;
    }

    public void setFullHash(String fullHash) {
        this.fullHash = fullHash;
    }

    public String getTransactionBytes() {
        return transactionBytes;
    }

    public void setTransactionBytes(String transactionBytes) {
        this.transactionBytes = transactionBytes;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }


    public static final class TransactionDataBuilder {

        private String sender;
        private String senderId;

        private String fullHash;
        private String transactionBytes;

        private String transactionId;

        private TransactionDataBuilder() {

        }

        public static TransactionDataBuilder newInstance() {
            return new TransactionDataBuilder();
        }

        public TransactionDataBuilder withSender(String sender) {
            this.sender = sender;
            return this;
        }

        public TransactionDataBuilder withSenderId(String senderId) {
            this.senderId = senderId;
            return this;
        }

        public TransactionDataBuilder withFullHash(String fullHash) {
            this.fullHash = fullHash;
            return this;
        }

        public TransactionDataBuilder withTransactionBytes(String transactionBytes) {
            this.transactionBytes = transactionBytes;
            return this;
        }

        public TransactionDataBuilder withTransactionId(String transactionId) {
            this.transactionId = transactionId;
            return this;
        }

        public TransactionData build() {
            TransactionData transactionData = new TransactionData();
            transactionData.setSender(sender);
            transactionData.setSenderId(senderId);
            transactionData.setFullHash(fullHash);
            transactionData.setTransactionBytes(transactionBytes);
            transactionData.setTransactionId(transactionId);

            return transactionData;
        }

    }

}
