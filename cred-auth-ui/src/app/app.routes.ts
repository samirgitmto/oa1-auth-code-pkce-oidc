import { Routes } from '@angular/router';

import { Login } from './auth/login/login';
import { Callback } from './auth/callback/callback';
import { Home } from './shell/home/home';

export const routes: Routes = [
  { path: '', component: Home },
  { path: 'auth/login', component: Login },
  { path: 'auth/callback', component: Callback },
];
