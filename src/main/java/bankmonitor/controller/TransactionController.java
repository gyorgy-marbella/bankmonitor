package bankmonitor.controller;

import bankmonitor.dto.Transaction;
import bankmonitor.dto.TransactionCreateRequest;
import bankmonitor.dto.TransactionPatchRequest;
import bankmonitor.model.TransactionEntity;
import bankmonitor.service.TransactionService;
import org.json.JSONObject;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static bankmonitor.model.TransactionEntity.REFERENCE_KEY;

@RequestMapping("/")
@RestController
@Validated
public class TransactionController {

    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionRepository) {
        this.transactionService = transactionRepository;
    }

    @GetMapping("/transactions")
    public List<Transaction> getAllTransactions() {
        return transactionService.findAll().stream().map(
                TransactionController::mapTransaction
        ).collect(Collectors.toList());
    }

    @GetMapping("/transactions/{id}")
    public ResponseEntity<Transaction> getAllTransactions(@PathVariable Long id) {
        Optional<Transaction> transaction = transactionService.findById(id)
                .map(TransactionController::mapTransaction);
        return transaction.map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }


    @PostMapping("/transactions")
    public Transaction createTransaction(@RequestBody TransactionCreateRequest transactionCreateRequest) {
        return mapTransaction(transactionService.create(transactionCreateRequest));
    }

    @PutMapping("/transactions/{id}")
    public ResponseEntity<Transaction> updateTransaction(@PathVariable Long id, @RequestBody TransactionPatchRequest transactionPatchRequest) {
        return ResponseEntity.ok(mapTransaction(transactionService.update(id, transactionPatchRequest.amount(), transactionPatchRequest.reference())));
    }

    private static Transaction mapTransaction(TransactionEntity entity) {
        JSONObject jsonData = new JSONObject(entity.getData());
        Integer amount;

        if (jsonData.has("amount")) {
            amount = jsonData.getInt("amount");
        } else {
            amount = -1;
        }

        String reference;
        if (jsonData.has(REFERENCE_KEY)) {
            reference = jsonData.getString(REFERENCE_KEY);
        } else {
            reference = "";
        }


        return new Transaction(entity.getId(), entity.getData(), amount, reference);
    }
}
