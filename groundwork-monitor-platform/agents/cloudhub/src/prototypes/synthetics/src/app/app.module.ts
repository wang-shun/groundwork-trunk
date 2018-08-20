import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { FormsModule } from '@angular/forms';
import { AccordionModule, ModalModule } from 'ng2-bootstrap';
import { DataTableModule, ButtonModule, CheckboxModule, SharedModule } from 'primeng/primeng';

import { AppComponent }  from './app.component';
import { ListComponent }  from './list.component';
import { TableComponent }  from './table.component';

@NgModule({
  imports:      [ FormsModule, AccordionModule.forRoot(), ModalModule.forRoot(), DataTableModule, ButtonModule, CheckboxModule, SharedModule, BrowserModule ],
  declarations: [ AppComponent, ListComponent, TableComponent ],
  bootstrap:    [ AppComponent, ListComponent, TableComponent ]
})

export class AppModule { }
