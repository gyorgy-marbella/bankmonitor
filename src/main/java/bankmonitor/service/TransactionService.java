package bankmonitor.service;

import bankmonitor.dto.TransactionCreateRequest;
import bankmonitor.exceptionhandler.ValidationException;
import bankmonitor.model.TransactionEntity;
import bankmonitor.repository.TransactionRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.hibernate.exception.ConstraintViolationException;
import org.json.JSONObject;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.MethodArgumentNotValidException;

import javax.persistence.EntityNotFoundException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static bankmonitor.model.TransactionEntity.REFERENCE_KEY;

@Service
public class TransactionService {

    private final TransactionRepository transactionRepository;

    private final ObjectMapper objectMapper;

    public TransactionService(TransactionRepository transactionRepository, ObjectMapper objectMapper) {
        this.transactionRepository = transactionRepository;
        this.objectMapper = objectMapper;
    }

    public List<TransactionEntity> findAll() {
        return transactionRepository.findAll();
    }

    public Optional<TransactionEntity> findById(Long id) {
        return transactionRepository.findById(id);
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

        if (json.length() > 1000) {
            throw new ValidationException();
        }

        data.setData(json);
        return transactionRepository.save(data);
    }

    public TransactionEntity update(Long id, Integer amount, String reference) {
        TransactionEntity transactionEntity = getById(id);

        JSONObject trdata = new JSONObject(transactionEntity.getData());

        if (Objects.nonNull(amount)) {
            trdata.put("amount", amount);
        }

        if (Objects.nonNull(reference)) {
            trdata.put(REFERENCE_KEY, reference);
        }

        transactionEntity.setData(trdata.toString());

        return transactionRepository.save(transactionEntity);
    }

}
