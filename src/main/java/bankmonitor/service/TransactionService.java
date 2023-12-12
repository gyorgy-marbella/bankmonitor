package bankmonitor.service;

import bankmonitor.model.TransactionEntity;
import bankmonitor.repository.TransactionRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

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


    public TransactionEntity save(TransactionEntity data) {
        return transactionRepository.save(data);
    }
}
