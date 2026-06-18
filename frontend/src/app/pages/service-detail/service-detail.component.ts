import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { ApiService } from '../../services/api.service';

@Component({
  selector: 'app-service-detail',
  templateUrl: './service-detail.component.html',
  styleUrls: ['./service-detail.component.css']
})
export class ServiceDetailComponent implements OnInit {
  serviceId!: number;
  service: any = null;
  artisans: any[] = [];
  loading = false;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private apiService: ApiService
  ) { }

  ngOnInit(): void {
    this.serviceId = Number(this.route.snapshot.paramMap.get('id'));
    this.loadServiceDetail();
  }

  loadServiceDetail(): void {
    this.loading = true;
    this.apiService.get('/service/items/' + this.serviceId)
      .subscribe({
        next: (res: any) => {
          if (res.code === 200) {
            this.service = res.data;
          }
          this.loading = false;
        },
        error: () => {
          this.loading = false;
        }
      });
  }

  bookNow(): void {
    this.router.navigate(['/order-confirm'], { queryParams: { serviceId: this.serviceId } });
  }
}
