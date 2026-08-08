package com.todor.quotes.controller;

import com.todor.quotes.dto.CreateQuoteRequest;
import com.todor.quotes.dto.QuoteResponse;
import com.todor.quotes.service.QuoteService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/quotes")
public class QuotesController {

    private final QuoteService service;

    public QuotesController(QuoteService service) {
        this.service = service;
    }

    @GetMapping("/random")
    public ResponseEntity<QuoteResponse> getRandomQuote() {
        return service.getRandom()
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.noContent().build());
    }

    @GetMapping
    public List<QuoteResponse> getAllQuotes() {
        return service.getAll();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public QuoteResponse createQuote(@Valid @RequestBody CreateQuoteRequest request) {
        return service.create(request);
    }
}
