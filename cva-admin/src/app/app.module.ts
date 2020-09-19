/**
 * @license
 * Copyright Akveo. All Rights Reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */
import {BrowserModule} from '@angular/platform-browser';
import {BrowserAnimationsModule} from '@angular/platform-browser/animations';
import {NgModule} from '@angular/core';
import {HTTP_INTERCEPTORS, HttpClientModule} from '@angular/common/http';
import {CoreModule} from './@core/core.module';
import {ThemeModule} from './@theme/theme.module';
import {AppComponent} from './app.component';
import {AppRoutingModule} from './app-routing.module';
import {
  NbChatModule,
  NbDatepickerModule,
  NbDialogModule,
  NbMenuModule,
  NbSidebarModule,
  NbToastrModule,
  NbWindowModule,
} from '@nebular/theme';
import {AuthGuard} from './auth-guard.service';
import {DialogflowService} from './dialogflow.service';
import {NbAuthJWTInterceptor} from '@nebular/auth';

@NgModule({
  declarations: [AppComponent],
  imports: [
    BrowserModule,
    BrowserAnimationsModule,
    HttpClientModule,
    AppRoutingModule,
    NbSidebarModule.forRoot(),
    NbMenuModule.forRoot(),
    NbDatepickerModule.forRoot(),
    NbDialogModule.forRoot(),
    NbWindowModule.forRoot(),
    NbToastrModule.forRoot(),
    NbChatModule.forRoot({
      messageGoogleMapKey: 'AIzaSyA_wNuCzia92MAmdLRzmqitRGvCF7wCZPY',
    }),
    CoreModule.forRoot(),
    ThemeModule.forRoot(),
  ],
  providers: [
    DialogflowService,
    AuthGuard,
    // Interceptor Service for JWT Injection & URL Filters
    {provide: HTTP_INTERCEPTORS, useClass: NbAuthJWTInterceptor, multi: true},
    // {
    //   provide: NB_AUTH_TOKEN_INTERCEPTOR_FILTER,
    //   useValue: function (req: HttpRequest<any>) {
    //     if ((req.url === '/api/auth/refresh-token') || (req.url.includes('/api/auth/login'))) {
    //       return true;
    //     }
    //     return false;
    //   },
    // },
  ],
  bootstrap: [AppComponent],
})
export class AppModule {
}
