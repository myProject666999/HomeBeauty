import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ApiService } from '../../services/api.service';

@Component({
  selector: 'app-order-confirm',
  templateUrl: './order-confirm.component.html',
  styleUrls: ['./order-confirm.component.css']
})
export class OrderConfirmComponent implements OnInit {
  orderForm!: FormGroup;
  serviceId!: number;
  service: any = null;
  submitting = false;
  timeSlots: string[] = [];
  selectedDate: string = '';

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private fb: FormBuilder,
    private apiService: ApiService
  ) {
    this.generateTimeSlots();
  }

  ngOnInit(): void {
    this.serviceId = Number(this.route.snapshot.queryParamMap.get('serviceId'));
    if (!this.serviceId) {
      this.router.navigate(['/services']);
      return;
    }
    const today = new Date();
    this.selectedDate = today.toISOString().split('T')[0];
    this.initForm();
    this.loadService();
  }

  generateTimeSlots(): void {
    for (let h = 9; h < 18; h++) {
      const start = h.toString().padStart(2, '0') + ':00';
      const end = (h + 1).toString().padStart(2, '0') + ':00';
      this.timeSlots.push(start + '-' + end);
    }
  }

  initForm(): void {
    this.orderForm = this.fb.group({
      contactName: ['', Validators.required],
      contactPhone: ['', [Validators.required, Validators.pattern('^1[3-9]\\d{9}$')]],
      address: ['', Validators.required],
      appointmentDate: [this.selectedDate, Validators.required],
      appointmentTime: ['', Validators.required],
      longitude: [116.467281],
      latitude: [39.908823],
      remark: ['']
    });
  }

  loadService(): void {
    this.apiService.get('/service/item/' + this.serviceId)
      .subscribe({
        next: (res: any) => {
          if (res.code === 200) {
            this.service = res.data;
          }
        },
        error: () => {
          this.service = null;
        }
      });
  }

  submitOrder(): void {
    if (this.orderForm.invalid) {
      Object.keys(this.orderForm.controls).forEach(key => {
        this.orderForm.get(key)?.markAsTouched();
      });
      return;
    }

    const userInfo = localStorage.getItem('userInfo');
    if (!userInfo) {
      alert('请先登录');
      this.router.navigate(['/login']);
      return;
    }
    const userId = JSON.parse(userInfo).id;

    this.submitting = true;
    const orderData = {
      userId: userId,
      serviceItemId: this.serviceId,
      ...this.orderForm.value
    };

    this.apiService.post('/order/create', orderData)
      .subscribe({
        next: (res: any) => {
          if (res.code === 200) {
            alert('预约成功！');
            this.router.navigate(['/orders']);
          } else {
            alert(res.message || '预约失败');
          }
          this.submitting = false;
        },
        error: (err: any) => {
          alert(err?.error?.message || '预约失败，请稍后重试');
          this.submitting = false;
        }
      });
  }
}
