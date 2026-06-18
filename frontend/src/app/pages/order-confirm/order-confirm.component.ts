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

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private fb: FormBuilder,
    private apiService: ApiService
  ) { }

  ngOnInit(): void {
    this.serviceId = Number(this.route.snapshot.queryParamMap.get('serviceId'));
    this.initForm();
    this.loadService();
  }

  initForm(): void {
    this.orderForm = this.fb.group({
      customerName: ['', Validators.required],
      customerPhone: ['', [Validators.required, Validators.pattern('^1[3-9]\\d{9}$')]],
      serviceAddress: ['', Validators.required],
      appointmentTime: ['', Validators.required],
      remark: ['']
    });
  }

  loadService(): void {
    this.apiService.get('/service/items/' + this.serviceId)
      .subscribe((res: any) => {
        if (res.code === 200) {
          this.service = res.data;
        }
      });
  }

  submitOrder(): void {
    if (this.orderForm.invalid) {
      return;
    }

    this.submitting = true;
    const orderData = {
      serviceItemId: this.serviceId,
      ...this.orderForm.value
    };

    this.apiService.post('/orders', orderData)
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
        error: () => {
          alert('预约失败，请稍后重试');
          this.submitting = false;
        }
      });
  }
}
