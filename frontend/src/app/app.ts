import { Component, inject, signal } from '@angular/core';

import { QuotesService } from './services/quotes';
import { QuoteResponse } from './models/quote-response';

@Component({
  selector: 'app-root',
  templateUrl: './app.html',
  styleUrl: './app.css',
})
export class App {
  private readonly quotesService = inject(QuotesService);

  protected readonly quote = signal<QuoteResponse | null>(null);
  protected readonly loading = signal(false);
  protected readonly error = signal(false);

  constructor() {
    this.loadQuote();
  }

  protected loadQuote(): void {
    this.loading.set(true);
    this.error.set(false);

    this.quotesService.getRandomQuote().subscribe({
      next: (response) => {
        this.quote.set(response);
        this.loading.set(false);
      },
      error: () => {
        this.error.set(true);
        this.loading.set(false);
      },
    });
  }
}
