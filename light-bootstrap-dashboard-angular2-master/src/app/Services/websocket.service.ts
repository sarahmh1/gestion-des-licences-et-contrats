import { Injectable } from '@angular/core';
import { Client, Message, StompSubscription } from '@stomp/stompjs';
import * as SockJS from 'sockjs-client';
import { BehaviorSubject, Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class WebsocketService {
  private stompClient: Client | null = null;
  private connected$ = new BehaviorSubject<boolean>(false);
  private messageSubject = new BehaviorSubject<any>(null);
  private typingSubject = new BehaviorSubject<any>(null);

  constructor() { }

  /**
   * Connecte au serveur WebSocket
   */
  connect(userId: string): void {
    const socket = new SockJS('http://localhost:8089/ws');

    this.stompClient = new Client({
      webSocketFactory: () => socket as any,
      reconnectDelay: 5000,
      heartbeatIncoming: 4000,
      heartbeatOutgoing: 4000,
      debug: (str) => {
        console.log('STOMP: ' + str);
      }
    });

    this.stompClient.onConnect = (frame) => {
      console.log('Connected: ' + frame);
      this.connected$.next(true);

      // S'abonner aux messages personnels via topic
      this.stompClient?.subscribe(`/topic/messages/${userId}`, (message: Message) => {
        const messageBody = JSON.parse(message.body);
        console.log('Message reçu via WebSocket:', messageBody);
        this.messageSubject.next(messageBody);
      });

      // S'abonner aux indicateurs de saisie
      this.stompClient?.subscribe(`/topic/typing/${userId}`, (message: Message) => {
        const typingUserId = JSON.parse(message.body);
        this.typingSubject.next(typingUserId);
      });
    };


    this.stompClient.onStompError = (frame) => {
      console.error('Broker reported error: ' + frame.headers['message']);
      console.error('Additional details: ' + frame.body);
      this.connected$.next(false);
    };

    this.stompClient.activate();
  }

  /**
   * Déconnecte du serveur WebSocket
   */
  disconnect(): void {
    if (this.stompClient !== null) {
      this.stompClient.deactivate();
      this.connected$.next(false);
    }
  }

  /**
   * Envoie un message
   */
  sendMessage(message: any): void {
    console.log('Tentative d\'envoi de message:', message);
    console.log('WebSocket connecté:', this.connected$.value);

    if (this.stompClient && this.connected$.value) {
      this.stompClient.publish({
        destination: '/app/chat.send',
        body: JSON.stringify(message)
      });
      console.log('Message envoyé via WebSocket');
    } else {
      console.warn('WebSocket non connecté, message non envoyé');
    }
  }

  /**
   * Envoie un indicateur de saisie
   */
  sendTypingIndicator(senderId: number, receiverId: number): void {
    if (this.stompClient && this.connected$.value) {
      this.stompClient.publish({
        destination: '/app/chat.typing',
        body: JSON.stringify({ senderId, receiverId })
      });
    }
  }

  /**
   * Observable pour les messages reçus
   */
  getMessages(): Observable<any> {
    return this.messageSubject.asObservable();
  }

  /**
   * Observable pour les indicateurs de saisie
   */
  getTypingIndicators(): Observable<any> {
    return this.typingSubject.asObservable();
  }

  /**
   * Observable pour l'état de connexion
   */
  isConnected(): Observable<boolean> {
    return this.connected$.asObservable();
  }
}
