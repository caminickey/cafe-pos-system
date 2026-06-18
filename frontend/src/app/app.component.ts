import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ApiService, MenuItem, Order, SplitPreview } from './api.service';

interface CartLine {
  item: MenuItem;
  quantity: number;
  modifiers: string;
}

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './app.component.html',
  styleUrl: './app.component.css'
})
export class AppComponent implements OnInit {
  menu: MenuItem[] = [];
  openOrders: Order[] = [];
  cart: CartLine[] = [];
  currentOrder: Order | null = null;
  tableName = 'Table 1';
  people = 2;
  split: SplitPreview | null = null;
  payerName = 'Guest';
  paymentMethod = 'CARD';
  paymentAmount = 0;
  aiText = '';
  busy = false;
  message = '';

  constructor(private api: ApiService) {}

  ngOnInit(): void {
    this.load();
  }

  load(): void {
    this.api.menu().subscribe(menu => (this.menu = menu));
    this.api.openOrders().subscribe(orders => (this.openOrders = orders));
  }

  categories(): string[] {
    return [...new Set(this.menu.map(item => item.category))];
  }

  byCategory(category: string): MenuItem[] {
    return this.menu.filter(item => item.category === category);
  }

  addToCart(item: MenuItem): void {
    const existing = this.cart.find(line => line.item.id === item.id && !line.modifiers);
    if (existing) {
      existing.quantity += 1;
    } else {
      this.cart.push({ item, quantity: 1, modifiers: '' });
    }
    this.message = `${item.name} added`;
  }

  removeCartLine(index: number): void {
    this.cart.splice(index, 1);
  }

  cartTotal(): number {
    return this.cart.reduce((sum, line) => sum + line.item.price * line.quantity, 0);
  }

  createOrder(): void {
    if (!this.cart.length) {
      this.message = 'Add at least one item first.';
      return;
    }
    this.busy = true;
    const items = this.cart.map(line => ({
      menuItemId: line.item.id,
      quantity: line.quantity,
      modifiers: line.modifiers
    }));
    this.api.createOrder(this.tableName, items).subscribe({
      next: order => {
        this.currentOrder = order;
        this.cart = [];
        this.paymentAmount = order.balanceDue;
        this.load();
        this.message = `Order #${order.id} opened.`;
        this.busy = false;
      },
      error: err => this.fail(err)
    });
  }

  selectOrder(order: Order): void {
    this.currentOrder = order;
    this.paymentAmount = order.balanceDue;
    this.split = null;
    this.aiText = '';
  }

  addItemToOrder(item: MenuItem): void {
    if (!this.currentOrder) {
      this.addToCart(item);
      return;
    }
    this.busy = true;
    this.api.addItem(this.currentOrder.id, item.id).subscribe({
      next: order => this.refreshOrder(order, `${item.name} added to order.`),
      error: err => this.fail(err)
    });
  }

  updateLine(lineId: number, quantity: number, modifiers: string | null): void {
    if (!this.currentOrder) return;
    this.busy = true;
    this.api.updateLine(this.currentOrder.id, lineId, quantity, modifiers ?? '').subscribe({
      next: order => this.refreshOrder(order, 'Order line updated.'),
      error: err => this.fail(err)
    });
  }

  voidLine(lineId: number): void {
    if (!this.currentOrder) return;
    this.busy = true;
    this.api.voidLine(this.currentOrder.id, lineId).subscribe({
      next: order => this.refreshOrder(order, 'Item voided from the bill.'),
      error: err => this.fail(err)
    });
  }

  saveNote(note: string): void {
    if (!this.currentOrder) return;
    this.busy = true;
    this.api.updateNote(this.currentOrder.id, note).subscribe({
      next: order => this.refreshOrder(order, 'Kitchen note saved.'),
      error: err => this.fail(err)
    });
  }

  previewSplit(): void {
    if (!this.currentOrder) return;
    this.api.splitPreview(this.currentOrder.id, this.people).subscribe(split => {
      this.split = split;
      this.paymentAmount = split.eachPays;
    });
  }

  pay(): void {
    if (!this.currentOrder) return;
    this.busy = true;
    this.api.addPayment(this.currentOrder.id, this.payerName, this.paymentMethod, this.paymentAmount).subscribe({
      next: order => this.refreshOrder(order, order.status === 'PAID' ? 'Order fully paid.' : 'Payment recorded.'),
      error: err => this.fail(err)
    });
  }

  suggestAddOn(): void {
    const names = this.currentOrder
      ? this.currentOrder.lines.filter(line => line.status === 'ACTIVE').map(line => `${line.quantity} ${line.itemName}`)
      : this.cart.map(line => `${line.quantity} ${line.item.name}`);
    if (!names.length) {
      this.message = 'Add items before asking AI.';
      return;
    }
    this.aiText = 'Asking Gemini...';
    this.api.aiCartSuggestion(names).subscribe({
      next: result => (this.aiText = result.text),
      error: err => (this.aiText = this.errorMessage(err))
    });
  }

  kitchenSummary(): void {
    if (!this.currentOrder) return;
    this.aiText = 'Asking Gemini...';
    this.api.aiKitchenSummary(this.currentOrder.id).subscribe({
      next: result => (this.aiText = result.text),
      error: err => (this.aiText = this.errorMessage(err))
    });
  }

  private refreshOrder(order: Order, message: string): void {
    this.currentOrder = order;
    this.paymentAmount = order.balanceDue;
    this.split = null;
    this.load();
    this.message = message;
    this.busy = false;
  }

  private fail(err: unknown): void {
    this.message = this.errorMessage(err);
    this.busy = false;
  }

  private errorMessage(err: any): string {
    return err?.error?.message ?? 'Something went wrong. Check the backend console.';
  }
}
