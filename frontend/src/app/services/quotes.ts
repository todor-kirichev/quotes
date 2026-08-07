import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { QuoteResponse } from '../models/quote-response';

@Injectable({ providedIn: 'root' })
export class QuotesService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = 'http://localhost:8080/api/quotes';

  getRandomQuote(): Observable<QuoteResponse> {
    return this.http.get<QuoteResponse>(`${this.baseUrl}/random`);
  }
}
