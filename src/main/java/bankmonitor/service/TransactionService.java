package bankmonitor.service;

import bankmonitor.dto.TransactionCreateRequest;
import bankmonitor.exceptionhandler.ValidationException;
import bankmonitor.model.TransactionEntity;
import bankmonitor.repository.TransactionRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

import javax.persistence.EntityNotFoundException;
import java.util.List;
import java.util.Objects;

import static bankmonitor.service.TransactionMapper.AMOUNT_KEY;
import static bankmonitor.service.TransactionMapper.REFERENCE_KEY;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository transactionRepository;

    private final ObjectMapper objectMapper;

    public List<TransactionEntity> findAll() {
        return transactionRepository.findAll();
    }

    public TransactionEntity getById(Long id) {
        return transactionRepository.findById(id).orElseThrow(
                () -> new EntityNotFoundException("TransactionEntity with id: " + id + " not found")
        );
    }

    public TransactionEntity create(TransactionCreateRequest transactionCreateRequest) {
        TransactionEntity data = new TransactionEntity();
        String json;

        try {
            json = objectMapper.writeValueAsString(transactionCreateRequest);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        validateDataSize(json);

        data.setData(json);
        return transactionRepository.save(data);
    }

    public TransactionEntity update(Long id, Integer amount, String reference) {
        TransactionEntity transactionEntity = getById(id);

        var trdata = new JSONObject(transactionEntity.getData());

        if (Objects.nonNull(amount)) {
            trdata.put(AMOUNT_KEY, amount);
        }

        if (Objects.nonNull(reference)) {
            trdata.put(REFERENCE_KEY, reference);
        }

        String updatedJson = trdata.toString();

        validateDataSize(updatedJson);

        transactionEntity.setData(updatedJson);

        return transactionRepository.save(transactionEntity);
    }

    private static void validateDataSize(String json) {
        if (json.length() > 1000) {
            throw new ValidationException("Invalid json data content size maximum: " + 1000 + ", current: " + json.length());
        }
    }
}
