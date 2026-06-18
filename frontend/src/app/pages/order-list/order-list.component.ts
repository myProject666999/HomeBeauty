import { Component, OnInit } from '@angular/core';
import { ApiService } from '../../services/api.service';

interface Order {
  id: number;
  orderNo: string;
  serviceName: string;
  price: number;
  status: string;
  appointmentTime: string;
  createTime: string;
}

@Component({
  selector: 'app-order-list',
  templateUrl: './order-list.component.html',
  styleUrls: ['./order-list.component.css']
})
export class OrderListComponent implements OnInit {
  orders: Order[] = [];
  loading = false;

  constructor(private apiService: ApiService) { }

  ngOnInit(): void {
    this.loadOrders();
  }

  loadOrders(): void {
    this.loading = true;
    this.apiService.get('/orders')
      .subscribe({
        next: (res: any) => {
          if (res.code === 200) {
            this.orders = res.data;
          }
          this.loading = false;
        },
        error: () => {
          this.loading = false;
        }
      });
  }

  getStatusText(status: string): string {
    const statusMap: Record<string, string> = {
      'PENDING': '待接单',
      'ACCEPTED': '已接单',
      'IN_PROGRESS': '服务中',
      'COMPLETED': '已完成',
      'CANCELLED': '已取消'
    };
    return statusMap[status] || status;
  }

  getStatusClass(status: string): string {
    return 'status-' + status.toLowerCase();
  }

  cancelOrder(orderId: number): void {
    if (confirm('确定要取消此订单吗？')) {
      this.apiService.put('/orders/' + orderId + '/cancel', {})
        .subscribe((res: any) => {
          if (res.code === 200) {
            alert('取消成功');
            this.loadOrders();
          }
        });
    }
  }
}
