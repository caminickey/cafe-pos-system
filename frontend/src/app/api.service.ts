import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../environments/environment';

export interface MenuItem {
  id: number;
  name: string;
  category: string;
  description: string;
  price: number;
}

export interface OrderLine {
  id: number;
  menuItemId: number;
  itemName: string;
  unitPrice: number;
  quantity: number;
  modifiers: string | null;
  status: 'ACTIVE' | 'VOIDED';
  lineTotal: number;
}

export interface Payment {
  id: number;
  payerName: string;
  method: 'CASH' | 'CARD' | 'EWALLET';
  amount: number;
  paidAt: string;
}

export interface Order {
  id: number;
  tableName: string;
  status: 'OPEN' | 'PAID' | 'CANCELLED';
  note: string | null;
  subtotal: number;
  serviceTax: number;
  total: number;
  paidAmount: number;
  balanceDue: number;
  createdAt: string;
  updatedAt: string;
  lines: OrderLine[];
  payments: Payment[];
}

export interface SplitPreview {
  people: number;
  eachPays: number;
  remainingBalance: number;
}

@Injectable({ providedIn: 'root' })
export class ApiService {
  private api = environment.apiUrl;

  constructor(private http: HttpClient) {}

  menu(): Observable<MenuItem[]> {
    return this.http.get<MenuItem[]>(`${this.api}/menu-items`);
  }

  openOrders(): Observable<Order[]> {
    return this.http.get<Order[]>(`${this.api}/orders/open`);
  }

  createOrder(tableName: string, items: { menuItemId: number; quantity: number; modifiers?: string }[]): Observable<Order> {
    return this.http.post<Order>(`${this.api}/orders`, { tableName, items });
  }

  addItem(orderId: number, menuItemId: number, quantity = 1, modifiers = ''): Observable<Order> {
    return this.http.post<Order>(`${this.api}/orders/${orderId}/items`, { menuItemId, quantity, modifiers });
  }

  updateLine(orderId: number, lineId: number, quantity: number, modifiers: string): Observable<Order> {
    return this.http.patch<Order>(`${this.api}/orders/${orderId}/items/${lineId}`, { quantity, modifiers });
  }

  voidLine(orderId: number, lineId: number): Observable<Order> {
    return this.http.delete<Order>(`${this.api}/orders/${orderId}/items/${lineId}`);
  }

  updateNote(orderId: number, note: string): Observable<Order> {
    return this.http.patch<Order>(`${this.api}/orders/${orderId}/note`, { note });
  }

  splitPreview(orderId: number, people: number): Observable<SplitPreview> {
    return this.http.post<SplitPreview>(`${this.api}/orders/${orderId}/split-preview`, { people });
  }

  addPayment(orderId: number, payerName: string, method: string, amount: number): Observable<Order> {
    return this.http.post<Order>(`${this.api}/orders/${orderId}/payments`, { payerName, method, amount });
  }

  aiCartSuggestion(cartItems: string[]): Observable<{ text: string }> {
    return this.http.post<{ text: string }>(`${this.api}/ai/cart-suggestion`, { cartItems });
  }

  aiKitchenSummary(orderId: number): Observable<{ text: string }> {
    return this.http.post<{ text: string }>(`${this.api}/ai/orders/${orderId}/kitchen-summary`, {});
  }
}
