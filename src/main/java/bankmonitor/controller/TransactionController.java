package bankmonitor.controller;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import bankmonitor.model.TransactionEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import org.json.JSONObject;

import bankmonitor.repository.TransactionRepository;

@Controller
@RequestMapping("/")
public class TransactionController {

    @Autowired
    private TransactionRepository transactionRepository;

    @GetMapping("/transactions")
    @ResponseBody
    public List<TransactionEntity> getAllTransactions() {
        return transactionRepository.findAll();
    }

    @GetMapping("/transactions/{id}")
    @ResponseBody
    public ResponseEntity<TransactionEntity> getAllTransactions(@PathVariable Long id) {
        Optional<TransactionEntity> transaction = transactionRepository.findById(id);
        return transaction.map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }


    @PostMapping("/transactions")
    @ResponseBody
    public TransactionEntity createTransaction(@RequestBody String jsonData) {
        TransactionEntity data = new TransactionEntity();
        data.setTimestamp(LocalDateTime.now());
        data.setData(jsonData);

        return transactionRepository.save(data);
    }

    @PutMapping("/transactions/{id}")
    @ResponseBody
    public ResponseEntity<TransactionEntity> updateTransaction(@PathVariable Long id, @RequestBody String update) {

        JSONObject updateJson = new JSONObject(update);

        Optional<TransactionEntity> data = transactionRepository.findById(id);

        if (!data.isPresent()) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        TransactionEntity transactionEntity = data.get();
        JSONObject trdata = new JSONObject(transactionEntity.getData());

        if (updateJson.has("amount")) {
            trdata.put("amount", updateJson.getInt("amount"));
        }

        if (updateJson.has(TransactionEntity.REFERENCE_KEY)) {
            trdata.put(TransactionEntity.REFERENCE_KEY, updateJson.getString(TransactionEntity.REFERENCE_KEY));
        }

        transactionEntity.setData(trdata.toString());

        TransactionEntity updatedTransactionEntity = transactionRepository.save(transactionEntity);
        return ResponseEntity.ok(updatedTransactionEntity);
    }
}
