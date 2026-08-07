package com.todor.quotes.controller;

import com.todor.quotes.dto.QuoteResponse;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Random;

@CrossOrigin(origins = "http://localhost:4200")
@RestController
@RequestMapping("/api/quotes")
public class QuotesController {

    private static final List<String> QUOTES = List.of(
            "Простотата е предпоставка за надеждност.",
            "Преждевременната оптимизация е коренът на всяко зло.",
            "Кодът се чете далеч по-често, отколкото се пише.",
            "Най-добрият код е кодът, който не си написал."
    );

    private final Random random = new Random();

    @GetMapping("/random")
    public QuoteResponse getRandomQuote() {
        return new QuoteResponse(QUOTES.get(random.nextInt(QUOTES.size())));
    }
}
