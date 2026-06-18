import { Component } from '@angular/core';
import { FormBuilder, FormGroup, Validators, AbstractControl, ValidationErrors } from '@angular/forms';
import { Router } from '@angular/router';
import { ApiService } from '../../services/api.service';

@Component({
  selector: 'app-register',
  templateUrl: './register.component.html',
  styleUrls: ['./register.component.css']
})
export class RegisterComponent {
  registerForm: FormGroup;
  submitting = false;

  constructor(
    private fb: FormBuilder,
    private router: Router,
    private apiService: ApiService
  ) {
    this.registerForm = this.fb.group({
      phone: ['', [Validators.required, Validators.pattern('^1[3-9]\\d{9}$')]],
      nickname: ['', [Validators.required, Validators.minLength(2)]],
      password: ['', [Validators.required, Validators.minLength(6)]],
      confirmPassword: ['', [Validators.required]]
    }, { validators: this.passwordMatchValidator });
  }

  passwordMatchValidator(control: AbstractControl): ValidationErrors | null {
    const password = control.get('password');
    const confirmPassword = control.get('confirmPassword');
    if (password && confirmPassword && password.value !== confirmPassword.value) {
      return { passwordMismatch: true };
    }
    return null;
  }

  onSubmit(): void {
    if (this.registerForm.invalid) {
      return;
    }

    this.submitting = true;
    const { confirmPassword, ...registerData } = this.registerForm.value;

    this.apiService.post('/user/register', registerData)
      .subscribe({
        next: (res: any) => {
          if (res.code === 200) {
            alert('注册成功！请登录');
            this.router.navigate(['/login']);
          } else {
            alert(res.message || '注册失败');
          }
          this.submitting = false;
        },
        error: () => {
          alert('注册失败，请稍后重试');
          this.submitting = false;
        }
      });
  }

  goToLogin(): void {
    this.router.navigate(['/login']);
  }
}
