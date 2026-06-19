import { Component } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { ApiService } from '../../services/api.service';

@Component({
  selector: 'app-login',
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.css']
})
export class LoginComponent {
  loginForm: FormGroup;
  submitting = false;

  constructor(
    private fb: FormBuilder,
    private router: Router,
    private apiService: ApiService
  ) {
    this.loginForm = this.fb.group({
      phone: ['', [Validators.required, Validators.pattern('^1[3-9]\\d{9}$')]],
      password: ['', [Validators.required, Validators.minLength(6)]]
    });
  }

  onSubmit(): void {
    if (this.loginForm.invalid) {
      return;
    }

    this.submitting = true;
    const loginData = {
      ...this.loginForm.value,
      role: 'user'
    };
    this.apiService.post('/user/login', loginData)
      .subscribe({
        next: (res: any) => {
          if (res.code === 200) {
            this.apiService.setToken(res.data.token);
            if (res.data.user) {
              delete res.data.user.password;
              localStorage.setItem('userInfo', JSON.stringify(res.data.user));
            }
            alert('登录成功！');
            this.router.navigate(['/']);
          } else {
            alert(res.message || '登录失败');
          }
          this.submitting = false;
        },
        error: (err: any) => {
          alert(err?.error?.message || '登录失败，请稍后重试');
          this.submitting = false;
        }
      });
  }

  goToRegister(): void {
    this.router.navigate(['/register']);
  }
}
