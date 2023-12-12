package bankmonitor.controller;

import bankmonitor.dto.Transaction;
import bankmonitor.model.TransactionEntity;
import bankmonitor.service.TransactionService;
import com.fasterxml.jackson.annotation.JsonIgnore;
import org.json.JSONObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static bankmonitor.model.TransactionEntity.REFERENCE_KEY;

@Controller
@RequestMapping("/")
public class TransactionController {

    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionRepository) {
        this.transactionService = transactionRepository;
    }

    @GetMapping("/transactions")
    @ResponseBody
    public List<Transaction> getAllTransactions() {
        return transactionService.findAll().stream().map(
                entity -> {
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
        ).collect(Collectors.toList());
    }

    @GetMapping("/transactions/{id}")
    @ResponseBody
    public ResponseEntity<TransactionEntity> getAllTransactions(@PathVariable Long id) {
        Optional<TransactionEntity> transaction = transactionService.findById(id);
        return transaction.map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }


    @PostMapping("/transactions")
    @ResponseBody
    public TransactionEntity createTransaction(@RequestBody String jsonData) {
        TransactionEntity data = new TransactionEntity();
        data.setTimestamp(LocalDateTime.now());
        data.setData(jsonData);

        return transactionService.save(data);
    }

    @PutMapping("/transactions/{id}")
    @ResponseBody
    public ResponseEntity<TransactionEntity> updateTransaction(@PathVariable Long id, @RequestBody String update) {

        JSONObject updateJson = new JSONObject(update);

        Optional<TransactionEntity> data = transactionService.findById(id);

        if (!data.isPresent()) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        TransactionEntity transactionEntity = data.get();
        JSONObject trdata = new JSONObject(transactionEntity.getData());

        if (updateJson.has("amount")) {
            trdata.put("amount", updateJson.getInt("amount"));
        }

        if (updateJson.has(REFERENCE_KEY)) {
            trdata.put(REFERENCE_KEY, updateJson.getString(REFERENCE_KEY));
        }

        transactionEntity.setData(trdata.toString());

        TransactionEntity updatedTransactionEntity = transactionService.save(transactionEntity);
        return ResponseEntity.ok(updatedTransactionEntity);
    }
}
