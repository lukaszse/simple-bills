import { Component, OnInit } from '@angular/core';
import { UsageLimitBarChartService } from "../../../../service/usage-limit-bar-chart.service";
import { darkRed, lightGray } from "../../../../utils/chart-colors.enum";

@Component({
  selector: 'app-limit-usage-chart',
  templateUrl: './limit-usage-charts.component.html',
  styleUrls: ['./limit-usage-charts.component.scss']
})
export class LimitUsageChartsComponent implements OnInit {

  // options
  view: any[] = [450, 35];
  showXAxis: boolean = false;
  showYAxis: boolean = true;
  gradient: boolean = false;
  showLegend: boolean = false;
  showXAxisLabel: boolean = false;
  xAxisLabel: string = '';
  showYAxisLabel: boolean = false;
  yAxisLabel: string = '';
  trimYAxis: boolean = true;
  maxLength: number = 35;

  colorScheme = {
    domain: [darkRed, lightGray],
  };

  constructor(public barChartService: UsageLimitBarChartService) {
  }

  ngOnInit(): void {
    this.barChartService.refresh();
  }
}
