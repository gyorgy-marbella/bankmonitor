package bankmonitor.service;

import bankmonitor.dto.Transaction;
import bankmonitor.model.TransactionEntity;
import org.json.JSONObject;
import org.springframework.stereotype.Component;

import java.util.function.Function;

@Component
public class TransactionMapper implements Function<TransactionEntity, Transaction> {

    String EMPTY = "";
    public static final String AMOUNT_KEY = "amount";

    public static final String REFERENCE_KEY = "reference";

    @Override
    public Transaction apply(TransactionEntity entity) {
        JSONObject jsonData = new JSONObject(entity.getData());
        Integer amount;

        if (jsonData.has(AMOUNT_KEY)) {
            amount = jsonData.getInt(AMOUNT_KEY);
        } else {
            amount = -1;
        }

        String reference;
        if (jsonData.has(REFERENCE_KEY)) {
            reference = jsonData.getString(REFERENCE_KEY);
        } else {
            reference = EMPTY;
        }


        return new Transaction(entity.getId(), entity.getData(), amount, reference);
    }
}
