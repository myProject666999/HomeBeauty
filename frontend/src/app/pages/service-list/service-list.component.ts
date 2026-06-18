import { Component, OnInit } from '@angular/core';
import { ApiService } from '../../services/api.service';

interface ServiceItem {
  id: number;
  name: string;
  description: string;
  basePrice: number;
  defaultDuration: number;
  coverImg: string;
  categoryId: number;
  sort: number;
  status: number;
  createTime: string;
  updateTime: string;
}

@Component({
  selector: 'app-service-list',
  templateUrl: './service-list.component.html',
  styleUrls: ['./service-list.component.css']
})
export class ServiceListComponent implements OnInit {
  services: ServiceItem[] = [];
  loading = false;

  constructor(private apiService: ApiService) { }

  ngOnInit(): void {
    this.loadServices();
  }

  loadServices(): void {
    this.loading = true;
    this.apiService.get('/service/items')
      .subscribe({
        next: (res: any) => {
          if (res.code === 200) {
            this.services = res.data;
          }
          this.loading = false;
        },
        error: () => {
          this.loading = false;
        }
      });
  }
}
