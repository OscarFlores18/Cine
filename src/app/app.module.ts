import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { HttpClientModule } from '@angular/common/http'; // Para cuando conectes la API Backend

import { AppComponent } from './app.component';
import { ProductosComponent } from './productos/productos.component'; // Ajustado a la ruta dentro de src/app/productos

@NgModule({
  declarations: [
    AppComponent,
    ProductosComponent // Debe estar aquí
  ],
  imports: [
    BrowserModule,
    HttpClientModule
  ],
  providers: [],
  bootstrap: [AppComponent]
})
export class AppModule { }