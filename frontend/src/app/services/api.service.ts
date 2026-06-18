import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';

const API_BASE = 'http://localhost:8080/api';

@Injectable({
  providedIn: 'root'
})
export class ApiService {
  private token: string = '';

  constructor(private http: HttpClient) { }

  setToken(token: string): void {
    this.token = token;
    localStorage.setItem('token', token);
  }

  getToken(): string {
    if (!this.token) {
      this.token = localStorage.getItem('token') || '';
    }
    return this.token;
  }

  private getHeaders(): HttpHeaders {
    let headers = new HttpHeaders({
      'Content-Type': 'application/json'
    });
    const token = this.getToken();
    if (token) {
      headers = headers.set('Authorization', 'Bearer ' + token);
    }
    return headers;
  }

  get<T>(url: string): Observable<any> {
    return this.http.get(API_BASE + url, { headers: this.getHeaders() });
  }

  post<T>(url: string, data: any): Observable<any> {
    return this.http.post(API_BASE + url, data, { headers: this.getHeaders() });
  }

  put<T>(url: string, data: any): Observable<any> {
    return this.http.put(API_BASE + url, data, { headers: this.getHeaders() });
  }

  delete<T>(url: string): Observable<any> {
    return this.http.delete(API_BASE + url, { headers: this.getHeaders() });
  }
}
