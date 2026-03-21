import { Observable } from 'rxjs';
import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { ClientStats } from 'app/Model/ClientStats';

@Injectable({
  providedIn: 'root'
})
export class ClientStatsService {
  private baseUrl = 'http://localhost:8089/ClientStats';

  constructor(private http: HttpClient) {}

  getClientStats(): Observable<ClientStats[]> {
    return this.http.get<ClientStats[]>(`${this.baseUrl}/all`);
  }

  deleteClientStatEntry(client: string, nomProduit: string): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/delete`, {
      params: { client, nomProduit }
    });
  }
}
