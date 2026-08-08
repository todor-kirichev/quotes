package com.todor.quotes.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateQuoteRequest(
        @NotBlank @Size(max = 500) String text,
        @Size(max = 100) String author
) {
}
