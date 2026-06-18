import { Component, OnInit } from '@angular/core';
import { HttpClient } from '@angular/common/http';

interface ServiceCategory {
  id: number;
  name: string;
  icon: string;
}

@Component({
  selector: 'app-home',
  templateUrl: './home.component.html',
  styleUrls: ['./home.component.css']
})
export class HomeComponent implements OnInit {
  categories: ServiceCategory[] = [];

  constructor(private http: HttpClient) { }

  ngOnInit(): void {
    this.loadCategories();
  }

  loadCategories(): void {
    this.http.get<any>('http://localhost:8080/api/service/categories')
      .subscribe(res => {
        if (res.code === 200) {
          this.categories = res.data;
        }
      });
  }
}
