import { Component } from '@angular/core';
import { Router } from '@angular/router';
import { OAuthService } from 'angular-oauth2-oidc';

@Component({
  selector: 'app-callback',
  standalone: true,
  imports: [],
  templateUrl: './callback.html',
  styleUrl: './callback.scss',
})
export class Callback {
  constructor(
    private readonly oauthService: OAuthService,
    private readonly router: Router
  ) {}

  async ngOnInit() {
    // Parses code/state from URL and completes login (exchanges code for tokens).
    await this.oauthService.tryLoginCodeFlow();
    await this.router.navigateByUrl('/');
  }
}
