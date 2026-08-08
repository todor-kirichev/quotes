package com.todor.quotes.service;

import com.todor.quotes.dto.CreateQuoteRequest;
import com.todor.quotes.dto.QuoteResponse;
import com.todor.quotes.entity.Quote;
import com.todor.quotes.repository.QuoteRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class QuoteService {

    private final QuoteRepository repository;

    public QuoteService(QuoteRepository repository) {
        this.repository = repository;
    }

    public Optional<QuoteResponse> getRandom() {
        return repository.findRandom().map(this::toResponse);
    }

    public List<QuoteResponse> getAll() {
        return repository.findAll().stream().map(this::toResponse).toList();
    }

    @Transactional
    public QuoteResponse create(CreateQuoteRequest request) {
        Quote saved = repository.save(new Quote(request.text(), request.author()));
        return toResponse(saved);
    }

    private QuoteResponse toResponse(Quote quote) {
        return new QuoteResponse(quote.getId(), quote.getText(), quote.getAuthor());
    }
}
