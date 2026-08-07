import { Component, inject, signal } from '@angular/core';
import { QuotesService } from './services/quotes';

@Component({
  selector: 'app-root',
  templateUrl: './app.html',
  styleUrl: './app.css',
})
export class App {
  private readonly quotesService = inject(QuotesService);

  protected readonly quote = signal('');
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
        this.quote.set(response.quote);
        this.loading.set(false);
      },
      error: () => {
        this.error.set(true);
        this.loading.set(false);
      },
    });
  }
}
