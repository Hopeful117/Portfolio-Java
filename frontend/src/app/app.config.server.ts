import { mergeApplicationConfig, ApplicationConfig } from '@angular/core';
import { provideServerRendering, withRoutes } from '@angular/ssr';
import { appConfig } from './app.config';
import { serverRoutes } from './app.routes.server';
import { API_ORIGIN } from './core/public-api.service';

const serverConfig: ApplicationConfig = {
  providers: [
    provideServerRendering(withRoutes(serverRoutes)),
    {
      provide: API_ORIGIN,
      useFactory: () => process.env['API_ORIGIN'] ?? 'http://localhost:8080',
    },
  ]
};

export const config = mergeApplicationConfig(appConfig, serverConfig);
