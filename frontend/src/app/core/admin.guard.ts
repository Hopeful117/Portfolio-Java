import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { catchError, map, of } from 'rxjs';
import { AdminApiService } from './admin-api.service';

export const adminGuard: CanActivateFn = () => {
  const api = inject(AdminApiService);
  const router = inject(Router);
  return api.session().pipe(
    map((session) => session.authenticated ? true : router.createUrlTree(['/admin/login'])),
    catchError(() => of(router.createUrlTree(['/admin/login']))),
  );
};
