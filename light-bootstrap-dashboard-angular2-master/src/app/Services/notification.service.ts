import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { AppNotification } from '../Model/Notification';

@Injectable({
    providedIn: 'root'
})
export class NotificationAppService {

    private baseUrl = 'http://localhost:8089/notifications';

    constructor(private http: HttpClient) { }

    private getHeaders(): HttpHeaders {
        const token = localStorage.getItem('token');
        return new HttpHeaders({
            'Content-Type': 'application/json',
            'Authorization': `Bearer ${token}`
        });
    }

    getNotificationsForUser(userId: number): Observable<AppNotification[]> {
        return this.http.get<AppNotification[]>(`${this.baseUrl}/user/${userId}`, { headers: this.getHeaders() });
    }

    getUnreadCount(userId: number): Observable<{ count: number }> {
        return this.http.get<{ count: number }>(`${this.baseUrl}/unread-count/${userId}`, { headers: this.getHeaders() });
    }

    markAsRead(notificationId: number): Observable<void> {
        return this.http.put<void>(`${this.baseUrl}/${notificationId}/mark-read`, {}, { headers: this.getHeaders() });
    }

    markAllAsRead(userId: number): Observable<void> {
        return this.http.put<void>(`${this.baseUrl}/mark-all-read/${userId}`, {}, { headers: this.getHeaders() });
    }
}
