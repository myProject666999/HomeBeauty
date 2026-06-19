import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { ApiService } from '../../services/api.service';

interface UserInfo {
  id: number;
  phone: string;
  nickname: string;
  avatar: string;
  gender: number;
  address: string;
  longitude: number;
  latitude: number;
  status: number;
}

@Component({
  selector: 'app-user-center',
  templateUrl: './user-center.component.html',
  styleUrls: ['./user-center.component.css']
})
export class UserCenterComponent implements OnInit {
  user: UserInfo | null = null;
  loading = false;

  constructor(
    private router: Router,
    private apiService: ApiService
  ) { }

  ngOnInit(): void {
    this.loadUserInfo();
  }

  loadUserInfo(): void {
    const cachedUser = localStorage.getItem('userInfo');
    if (cachedUser) {
      try {
        this.user = JSON.parse(cachedUser);
      } catch (e) {
        localStorage.removeItem('userInfo');
      }
    }

    const token = this.apiService.getToken();
    if (!token) {
      this.router.navigate(['/login']);
      return;
    }

    this.loading = true;
    this.apiService.get('/user/info')
      .subscribe({
        next: (res: any) => {
          if (res.code === 200) {
            this.user = res.data;
            localStorage.setItem('userInfo', JSON.stringify(res.data));
          }
          this.loading = false;
        },
        error: () => {
          this.loading = false;
        }
      });
  }

  logout(): void {
    if (confirm('确定要退出登录吗？')) {
      this.apiService.setToken('');
      localStorage.removeItem('userInfo');
      this.router.navigate(['/login']);
    }
  }
}
