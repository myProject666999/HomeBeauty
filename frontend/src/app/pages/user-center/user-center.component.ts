import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { ApiService } from '../../services/api.service';

interface UserInfo {
  id: number;
  username: string;
  phone: string;
  nickname: string;
  avatar: string;
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
      this.router.navigate(['/login']);
    }
  }
}
