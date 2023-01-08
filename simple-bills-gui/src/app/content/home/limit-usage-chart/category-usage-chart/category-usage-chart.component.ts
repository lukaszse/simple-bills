import { Component, Input } from '@angular/core';
import { Observable } from "rxjs";
import { CategoryUsageLimitBarChartModel } from "../../../../../dto/category-usage-limit-bar-chart.model";
import { darkRed, lightGray } from "../../../../../utils/chart-colors.enum";

@Component({
  selector: 'app-category-usage-chart',
  templateUrl: './category-usage-chart.component.html',
  styleUrls: ['./category-usage-chart.component.scss']
})
export class CategoryUsageChartComponent {

  @Input() categoryUsageChart$: Observable<CategoryUsageLimitBarChartModel[][]>;
  @Input() loading$: Observable<boolean>;
  @Input() chartDescription: String;

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

}
