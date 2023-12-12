package bankmonitor.service;

import bankmonitor.model.TransactionEntity;
import bankmonitor.repository.TransactionRepository;
import org.json.JSONObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import javax.persistence.EntityNotFoundException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static bankmonitor.model.TransactionEntity.REFERENCE_KEY;

@Service
public class TransactionService {

    private final TransactionRepository transactionRepository;

    public TransactionService(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    public List<TransactionEntity> findAll() {
        return transactionRepository.findAll();
    }

    public Optional<TransactionEntity> findById(Long id) {
        return transactionRepository.findById(id);
    }


    public TransactionEntity createOrUpdate(TransactionEntity data) {
        return transactionRepository.save(data);
    }

    public TransactionEntity update(Long id, Integer amount, String reference) {
        Optional<TransactionEntity> data = findById(id);

        //add exception handler
        if (!data.isPresent()) {
            throw new EntityNotFoundException();
        }

        TransactionEntity transactionEntity = data.get();
        JSONObject trdata = new JSONObject(transactionEntity.getData());

        if (Objects.nonNull(amount)) {
            trdata.put("amount", amount);
        }

        if (Objects.nonNull(reference)) {
            trdata.put(REFERENCE_KEY, reference);
        }

        transactionEntity.setData(trdata.toString());

        return createOrUpdate(transactionEntity);
    }

}
