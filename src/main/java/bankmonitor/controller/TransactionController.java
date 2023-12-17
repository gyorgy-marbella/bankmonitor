package bankmonitor.controller;

import bankmonitor.dto.Transaction;
import bankmonitor.dto.TransactionCreateRequest;
import bankmonitor.dto.TransactionPatchRequest;
import bankmonitor.service.TransactionMapper;
import bankmonitor.service.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RequestMapping("/")
@RestController
@Validated
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;
    private final TransactionMapper transactionMapper;

    @GetMapping("/transactions")
    public ResponseEntity<List<Transaction>> getAllTransactions() {
        var transactionList = transactionService.findAll().stream().map(
                transactionMapper
        ).collect(Collectors.toList());
        return new ResponseEntity<>(transactionList, HttpStatus.OK);
    }

    @GetMapping("/transactions/{id}")
    public ResponseEntity<Transaction> getAllTransactions(@PathVariable Long id) {
        var transaction = transactionMapper.apply(transactionService.getById(id));
        return new ResponseEntity<>(transaction, HttpStatus.OK);
    }

    @PostMapping("/transactions")
    public ResponseEntity<Transaction> createTransaction(@RequestBody TransactionCreateRequest transactionCreateRequest) {
        var transactionEntity = transactionService.create(transactionCreateRequest);
        return new ResponseEntity<>(transactionMapper.apply(transactionEntity), HttpStatus.CREATED);
    }

    @PutMapping("/transactions/{id}")
    public ResponseEntity<Transaction> updateTransaction(@PathVariable Long id, @RequestBody TransactionPatchRequest transactionPatchRequest) {
        var transactionEntity = transactionService.update(id, transactionPatchRequest.amount(), transactionPatchRequest.reference());
        return ResponseEntity.ok(transactionMapper.apply(transactionEntity));
    }

}
