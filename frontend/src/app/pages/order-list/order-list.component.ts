import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { ApiService } from '../../services/api.service';

interface Order {
  id: number;
  orderNo: string;
  serviceName: string;
  price: number;
  orderStatus: number;
  appointmentDate: string;
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

  constructor(
    private router: Router,
    private apiService: ApiService
  ) { }

  ngOnInit(): void {
    this.loadOrders();
  }

  loadOrders(): void {
    const userInfo = localStorage.getItem('userInfo');
    if (!userInfo) {
      this.router.navigate(['/login']);
      return;
    }
    const userId = JSON.parse(userInfo).id;

    this.loading = true;
    this.apiService.get('/order/user/' + userId)
      .subscribe({
        next: (res: any) => {
          if (res.code === 200) {
            this.orders = res.data || [];
          }
          this.loading = false;
        },
        error: () => {
          this.loading = false;
        }
      });
  }

  getStatusText(status: number): string {
    const statusMap: Record<number, string> = {
      0: '待接单',
      1: '已接单',
      2: '服务中',
      3: '已完成',
      4: '已取消',
      5: '待支付',
      6: '待评价'
    };
    return statusMap[status] || '未知状态';
  }

  getStatusClass(status: number): string {
    const classMap: Record<number, string> = {
      0: 'status-pending',
      1: 'status-accepted',
      2: 'status-in_progress',
      3: 'status-completed',
      4: 'status-cancelled',
      5: 'status-pending_pay',
      6: 'status-pending_review'
    };
    return classMap[status] || 'status-unknown';
  }

  getFullAppointmentTime(order: Order): string {
    return order.appointmentDate + ' ' + order.appointmentTime;
  }

  cancelOrder(orderId: number): void {
    if (!confirm('确定要取消此订单吗？')) {
      return;
    }
    const userInfo = localStorage.getItem('userInfo');
    const userId = userInfo ? JSON.parse(userInfo).id : 0;

    this.apiService.post('/order/cancel?orderId=' + orderId + '&reason=用户取消&operatorType=1&operatorId=' + userId, {})
      .subscribe({
        next: (res: any) => {
          if (res.code === 200) {
            alert('取消成功');
            this.loadOrders();
          } else {
            alert(res.message || '取消失败');
          }
        },
        error: (err: any) => {
          alert(err?.error?.message || '取消失败');
        }
      });
  }
}
