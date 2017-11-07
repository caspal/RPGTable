import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';

import { NavbarComponent } from './navbar.component';
import { WorldComponent } from './../world/world.component';
import { RealmComponent } from './../realm/realm.component';
import { ScenarioComponent } from './../scenario/scenario.component';

const routes: Routes = [
  { path: 'world', component: WorldComponent },
  { path: 'realm', component: RealmComponent },
  { path: 'scenario', component: ScenarioComponent },
  {
    path: '',
    redirectTo: '/world',
    pathMatch: 'full'
  },
  { path: '**', component: WorldComponent }
];

@NgModule({
  declarations: [
    NavbarComponent,
    WorldComponent,
    RealmComponent,
    ScenarioComponent
  ],
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule, NavbarComponent]
})
export class NavbarModule { }
