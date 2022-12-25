import { Component, OnInit } from '@angular/core';
import { UsageLimitPieChartService } from "../../../../service/usage-limit-pie-chart.service";
import {
  brown,
  darkGreen,
  deepPurple,
  lightBlue,
  lightGreen,
  lightPurple,
  pink
} from "../../../../utils/chart-colors.enum";

@Component({
  selector: 'app-pie-usage-chart',
  templateUrl: './pie-usage-chart.component.html',
  styleUrls: ['./pie-usage-chart.component.scss']
})
export class PieUsageChartComponent implements OnInit {

  view: number[] = [450, 350];

  // options
  gradient: boolean = false;
  showLegend: boolean = false;
  showLabels: boolean = true;
  isDoughnut: boolean = false;
  maxLength: number = 35;

  colorScheme = {
    domain: [
      deepPurple,
      lightPurple,
      pink,
      lightBlue,
      lightGreen,
      darkGreen,
      brown
    ],
  };

  constructor(public pieChartService: UsageLimitPieChartService) {
  }

  ngOnInit(): void {
    this.pieChartService.refresh();
  }
}
