/*
 * myRC - Insights Component
 * Copyright (c) 2026 myRC Team
 * Licensed under MIT License
 */
import { Component, OnInit, OnDestroy, AfterViewInit, ViewChild, ElementRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { TranslateModule, TranslateService } from '@ngx-translate/core';
import { AuthService } from '../../services/auth.service';
import { LanguageService } from '../../services/language.service';
import { User } from '../../models/user.model';
import { Subject, forkJoin } from 'rxjs';
import { takeUntil } from 'rxjs/operators';
import { ResponsibilityCentreService } from '../../services/responsibility-centre.service';
import { FiscalYearService } from '../../services/fiscal-year.service';
import { FundingItemService } from '../../services/funding-item.service';
import { SpendingItemService } from '../../services/spending-item.service';
import { ProcurementService } from '../../services/procurement.service';
import { ResponsibilityCentreDTO } from '../../models/responsibility-centre.model';
import { FiscalYear } from '../../models/fiscal-year.model';
import { FundingItem } from '../../models/funding-item.model';
import { SpendingItem } from '../../models/spending-item.model';
import { ProcurementItem, TRACKING_STATUS_INFO, TrackingStatus, ProcurementType, PROCUREMENT_TYPE_INFO } from '../../models/procurement.model';
import { CategoryService } from '../../services/category.service';
import { Category } from '../../models/category.model';
import { TrainingItemService } from '../../services/training-item.service';
import { TravelItemService } from '../../services/travel-item.service';
import { TrainingItem, TrainingItemStatus, TrainingType, TRAINING_STATUS_INFO, TRAINING_TYPE_INFO } from '../../models/training-item.model';
import { TravelItem, TravelItemStatus, TravelType, TRAVEL_STATUS_INFO, TRAVEL_TYPE_INFO } from '../../models/travel-item.model';

// Chart.js imports
import { Chart, ChartConfiguration, ChartType, registerables } from 'chart.js';
Chart.register(...registerables);

/**
 * Insights component providing visual analytics for funding, spending, and procurement.
 *
 * @author myRC Team
 * @version 1.0.0
 * @since 2026-01-29
 */
@Component({
  selector: 'app-insights',
  standalone: true,
  imports: [CommonModule, FormsModule, TranslateModule],
  templateUrl: './insights.component.html',
  styleUrls: ['./insights.component.scss'],
})
export class InsightsComponent implements OnInit, OnDestroy, AfterViewInit {
  @ViewChild('fundingByCategoryChart') fundingByCategoryChartRef!: ElementRef<HTMLCanvasElement>;
  @ViewChild('spendingByCategoryChart') spendingByCategoryChartRef!: ElementRef<HTMLCanvasElement>;
  @ViewChild('fundingVsSpendingChart') fundingVsSpendingChartRef!: ElementRef<HTMLCanvasElement>;
  @ViewChild('capVsOmChart') capVsOmChartRef!: ElementRef<HTMLCanvasElement>;
  @ViewChild('procurementStatusChart') procurementStatusChartRef!: ElementRef<HTMLCanvasElement>;
  @ViewChild('procurementTypeChart') procurementTypeChartRef!: ElementRef<HTMLCanvasElement>;
  @ViewChild('spendingStatusChart') spendingStatusChartRef!: ElementRef<HTMLCanvasElement>;
  @ViewChild('invoiceCoverageChart') invoiceCoverageChartRef!: ElementRef<HTMLCanvasElement>;
  @ViewChild('currencyDistributionChart') currencyDistributionChartRef!: ElementRef<HTMLCanvasElement>;
  @ViewChild('trainingStatusChart') trainingStatusChartRef!: ElementRef<HTMLCanvasElement>;
  @ViewChild('trainingTypeChart') trainingTypeChartRef!: ElementRef<HTMLCanvasElement>;
  @ViewChild('travelStatusChart') travelStatusChartRef!: ElementRef<HTMLCanvasElement>;
  @ViewChild('travelTypeChart') travelTypeChartRef!: ElementRef<HTMLCanvasElement>;

  currentUser: User | null = null;

  // Selected RC and FY
  selectedRC: ResponsibilityCentreDTO | null = null;
  selectedFY: FiscalYear | null = null;

  // Data
  fundingItems: FundingItem[] = [];
  spendingItems: SpendingItem[] = [];
  procurementItems: ProcurementItem[] = [];
  trainingItems: TrainingItem[] = [];
  travelItems: TravelItem[] = [];
  categories: Category[] = [];
  
  isLoading = false;
  errorMessage: string | null = null;

  // Charts
  private charts: Chart[] = [];

  // Color palette for charts
  private readonly colors = [
    '#3b82f6', '#10b981', '#f59e0b', '#ef4444', '#8b5cf6',
    '#ec4899', '#06b6d4', '#84cc16', '#f97316', '#6366f1',
    '#14b8a6', '#f43f5e', '#a855f7', '#22c55e', '#eab308'
  ];

  private destroy$ = new Subject<void>();

  constructor(
    private authService: AuthService,
    private router: Router,
    private rcService: ResponsibilityCentreService,
    private fyService: FiscalYearService,
    private fundingItemService: FundingItemService,
    private spendingItemService: SpendingItemService,
    private procurementService: ProcurementService,
    private categoryService: CategoryService,
    private trainingItemService: TrainingItemService,
    private travelItemService: TravelItemService,
    private translate: TranslateService,
    private languageService: LanguageService
  ) {}

  ngOnInit(): void {
    this.authService.currentUser$.pipe(takeUntil(this.destroy$)).subscribe((user: User | null) => {
      this.currentUser = user;
      if (!user) {
        this.router.navigate(['/login']);
      } else {
        this.loadSelectedContext();
      }
    });

    // Subscribe to language changes and recreate charts
    this.languageService.currentLanguage$.pipe(takeUntil(this.destroy$)).subscribe(() => {
      // Only recreate charts if data is already loaded
      if (this.fundingItems.length > 0 || this.spendingItems.length > 0 || this.procurementItems.length > 0 || this.trainingItems.length > 0 || this.travelItems.length > 0) {
        this.destroyCharts();
        setTimeout(() => this.createCharts(), 50);
      }
    });
  }

  ngAfterViewInit(): void {
    // Charts will be created after data is loaded
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
    this.destroyCharts();
  }

  /**
   * Load the selected RC and FY context.
   */
  private loadSelectedContext(): void {
    const rcId = this.rcService.getSelectedRC();
    const fyId = this.rcService.getSelectedFY();

    if (!rcId || !fyId) {
      this.router.navigate(['/app/select']);
      return;
    }

    this.rcService.getResponsibilityCentre(rcId).subscribe({
      next: (rc) => {
        this.selectedRC = rc;
        this.fyService.getFiscalYear(rcId, fyId).subscribe({
          next: (fy) => {
            this.selectedFY = fy;
            this.loadAllData();
          },
          error: () => {
            this.router.navigate(['/app/select']);
          }
        });
      },
      error: () => {
        this.router.navigate(['/app/select']);
      }
    });
  }

  /**
   * Load all data for charts.
   */
  private loadAllData(): void {
    if (!this.selectedRC || !this.selectedFY) return;

    this.isLoading = true;

    forkJoin({
      funding: this.fundingItemService.getFundingItemsByFY(this.selectedFY.id),
      spending: this.spendingItemService.getSpendingItemsByFY(this.selectedRC.id, this.selectedFY.id),
      procurement: this.procurementService.getProcurementItems(this.selectedRC.id, this.selectedFY.id),
      categories: this.categoryService.getCategoriesByFY(this.selectedRC.id, this.selectedFY.id),
      training: this.trainingItemService.getTrainingItemsByFY(this.selectedRC.id, this.selectedFY.id),
      travel: this.travelItemService.getTravelItemsByFY(this.selectedRC.id, this.selectedFY.id)
    }).subscribe({
      next: (data) => {
        this.fundingItems = data.funding;
        this.spendingItems = data.spending;
        this.procurementItems = data.procurement;
        this.categories = data.categories;
        this.trainingItems = this.selectedRC?.trainingIncludeInSummary !== false ? data.training : [];
        this.travelItems = this.selectedRC?.travelIncludeInSummary !== false ? data.travel : [];
        this.isLoading = false;
        
        // Give DOM time to render before creating charts
        setTimeout(() => this.createCharts(), 100);
      },
      error: (error) => {
        this.errorMessage = 'Failed to load data: ' + error.message;
        this.isLoading = false;
      }
    });
  }

  /**
   * Create all charts.
   */
  private createCharts(): void {
    this.destroyCharts();
    
    this.createFundingByCategoryChart();
    this.createSpendingByCategoryChart();
    this.createFundingVsSpendingChart();
    this.createCapVsOmChart();
    this.createProcurementStatusChart();
    this.createProcurementTypeChart();
    this.createSpendingStatusChart();
    this.createInvoiceCoverageChart();
    this.createCurrencyDistributionChart();
    if (this.selectedRC?.trainingIncludeInSummary !== false) {
      this.createTrainingStatusChart();
      this.createTrainingTypeChart();
    }
    if (this.selectedRC?.travelIncludeInSummary !== false) {
      this.createTravelStatusChart();
      this.createTravelTypeChart();
    }
  }

  /**
   * Destroy all charts.
   */
  private destroyCharts(): void {
    this.charts.forEach(chart => chart.destroy());
    this.charts = [];
  }

  /**
   * Create funding by category pie chart.
   */
  private createFundingByCategoryChart(): void {
    if (!this.fundingByCategoryChartRef?.nativeElement) return;

    const categoryData = this.aggregateByCategory(this.fundingItems, 'funding');
    
    const chart = new Chart(this.fundingByCategoryChartRef.nativeElement, {
      type: 'doughnut',
      data: {
        labels: categoryData.labels,
        datasets: [{
          data: categoryData.values,
          backgroundColor: this.colors.slice(0, categoryData.labels.length),
          borderWidth: 2,
          borderColor: '#ffffff'
        }]
      },
      options: {
        responsive: true,
        maintainAspectRatio: false,
        plugins: {
          legend: {
            position: 'bottom',
            labels: { padding: 15, usePointStyle: true }
          },
          title: {
            display: true,
            text: this.translate.instant('insights.fundingByCategory'),
            font: { size: 16, weight: 'bold' }
          }
        }
      }
    });
    this.charts.push(chart);
  }

  /**
   * Create spending by category pie chart.
   */
  private createSpendingByCategoryChart(): void {
    if (!this.spendingByCategoryChartRef?.nativeElement) return;

    const categoryData = this.aggregateByCategory(this.spendingItems, 'spending');
    
    const chart = new Chart(this.spendingByCategoryChartRef.nativeElement, {
      type: 'doughnut',
      data: {
        labels: categoryData.labels,
        datasets: [{
          data: categoryData.values,
          backgroundColor: this.colors.slice(0, categoryData.labels.length),
          borderWidth: 2,
          borderColor: '#ffffff'
        }]
      },
      options: {
        responsive: true,
        maintainAspectRatio: false,
        plugins: {
          legend: {
            position: 'bottom',
            labels: { padding: 15, usePointStyle: true }
          },
          title: {
            display: true,
            text: this.translate.instant('insights.spendingByCategory'),
            font: { size: 16, weight: 'bold' }
          }
        }
      }
    });
    this.charts.push(chart);
  }

  /**
   * Create funding vs spending comparison bar chart.
   */
  private createFundingVsSpendingChart(): void {
    if (!this.fundingVsSpendingChartRef?.nativeElement) return;

    // Get all unique categories
    const fundingCategories = this.aggregateByCategory(this.fundingItems, 'funding');
    const spendingCategories = this.aggregateByCategory(this.spendingItems, 'spending');
    
    const allCategories = [...new Set([...fundingCategories.labels, ...spendingCategories.labels])];
    
    const fundingValues = allCategories.map(cat => {
      const idx = fundingCategories.labels.indexOf(cat);
      return idx >= 0 ? fundingCategories.values[idx] : 0;
    });
    
    const spendingValues = allCategories.map(cat => {
      const idx = spendingCategories.labels.indexOf(cat);
      return idx >= 0 ? spendingCategories.values[idx] : 0;
    });

    const chart = new Chart(this.fundingVsSpendingChartRef.nativeElement, {
      type: 'bar',
      data: {
        labels: allCategories,
        datasets: [
          {
            label: this.translate.instant('insights.funding'),
            data: fundingValues,
            backgroundColor: 'rgba(59, 130, 246, 0.7)',
            borderColor: '#3b82f6',
            borderWidth: 1
          },
          {
            label: this.translate.instant('insights.spending'),
            data: spendingValues,
            backgroundColor: 'rgba(239, 68, 68, 0.7)',
            borderColor: '#ef4444',
            borderWidth: 1
          }
        ]
      },
      options: {
        responsive: true,
        maintainAspectRatio: false,
        plugins: {
          legend: {
            position: 'top'
          },
          title: {
            display: true,
            text: this.translate.instant('insights.fundingVsSpending'),
            font: { size: 16, weight: 'bold' }
          }
        },
        scales: {
          y: {
            beginAtZero: true,
            ticks: {
              callback: (value: string | number) => '$' + this.formatNumber(value as number)
            }
          }
        }
      }
    });
    this.charts.push(chart);
  }

  /**
   * Create CAP vs OM comparison chart.
   */
  private createCapVsOmChart(): void {
    if (!this.capVsOmChartRef?.nativeElement) return;

    const { fundingCap, fundingOm } = this.calculateFundingCapOm();
    const { spendingCap, spendingOm } = this.calculateSpendingCapOm();

    const chart = new Chart(this.capVsOmChartRef.nativeElement, {
      type: 'bar',
      data: {
        labels: [this.translate.instant('insights.funding'), this.translate.instant('insights.spending')],
        datasets: [
          {
            label: this.translate.instant('insights.capital'),
            data: [fundingCap, spendingCap],
            backgroundColor: 'rgba(16, 185, 129, 0.7)',
            borderColor: '#10b981',
            borderWidth: 1
          },
          {
            label: this.translate.instant('insights.om'),
            data: [fundingOm, spendingOm],
            backgroundColor: 'rgba(245, 158, 11, 0.7)',
            borderColor: '#f59e0b',
            borderWidth: 1
          }
        ]
      },
      options: {
        responsive: true,
        maintainAspectRatio: false,
        plugins: {
          legend: {
            position: 'top'
          },
          title: {
            display: true,
            text: this.translate.instant('insights.capVsOm'),
            font: { size: 16, weight: 'bold' }
          }
        },
        scales: {
          y: {
            beginAtZero: true,
            ticks: {
              callback: (value: string | number) => '$' + this.formatNumber(value as number)
            }
          }
        }
      }
    });
    this.charts.push(chart);
  }

  /**
   * Create procurement status pie chart.
   */
  private createProcurementStatusChart(): void {
    if (!this.procurementStatusChartRef?.nativeElement) return;

    const statusCounts = this.countByTrackingStatus(this.procurementItems);
    
    const chart = new Chart(this.procurementStatusChartRef.nativeElement, {
      type: 'pie',
      data: {
        labels: statusCounts.labels,
        datasets: [{
          data: statusCounts.values,
          backgroundColor: [
            '#64748b', // PLANNING - gray (matches status-gray badge)
            '#16a34a', // ON_TRACK - green (matches status-green badge)
            '#b45309', // AT_RISK - yellow (matches status-yellow badge)
            '#2563eb', // COMPLETED - blue (matches status-blue badge)
            '#dc2626'  // CANCELLED - red (matches status-red badge)
          ],
          borderWidth: 2,
          borderColor: '#ffffff'
        }]
      },
      options: {
        responsive: true,
        maintainAspectRatio: false,
        plugins: {
          legend: {
            position: 'bottom',
            labels: { padding: 15, usePointStyle: true }
          },
          title: {
            display: true,
            text: this.translate.instant('insights.procurementByStatus'),
            font: { size: 16, weight: 'bold' }
          }
        }
      }
    });
    this.charts.push(chart);
  }

  /**
   * Create procurement type pie chart.
   */
  private createProcurementTypeChart(): void {
    if (!this.procurementTypeChartRef?.nativeElement) return;

    const typeCounts = this.countByProcurementType(this.procurementItems);
    
    const chart = new Chart(this.procurementTypeChartRef.nativeElement, {
      type: 'pie',
      data: {
        labels: typeCounts.labels,
        datasets: [{
          data: typeCounts.values,
          backgroundColor: [
            '#2563eb', // RC_INITIATED - blue (matches status-blue badge)
            '#7c3aed'  // CENTRALLY_MANAGED - purple (matches status-purple badge)
          ],
          borderWidth: 2,
          borderColor: '#ffffff'
        }]
      },
      options: {
        responsive: true,
        maintainAspectRatio: false,
        plugins: {
          legend: {
            position: 'bottom',
            labels: { padding: 15, usePointStyle: true }
          },
          title: {
            display: true,
            text: this.translate.instant('insights.procurementByType'),
            font: { size: 16, weight: 'bold' }
          }
        }
      }
    });
    this.charts.push(chart);
  }

  /**
   * Create spending status pie chart.
   */
  private createSpendingStatusChart(): void {
    if (!this.spendingStatusChartRef?.nativeElement) return;

    const statusCounts = this.countSpendingByStatus();
    
    const chart = new Chart(this.spendingStatusChartRef.nativeElement, {
      type: 'pie',
      data: {
        labels: statusCounts.labels,
        datasets: [{
          data: statusCounts.values,
          backgroundColor: [
            '#64748b', // PLANNING - gray
            '#8b5cf6', // COMMITTED - purple
            '#22c55e', // COMPLETED - green
            '#ef4444'  // CANCELLED - red
          ],
          borderWidth: 2,
          borderColor: '#ffffff'
        }]
      },
      options: {
        responsive: true,
        maintainAspectRatio: false,
        plugins: {
          legend: {
            position: 'bottom',
            labels: { padding: 15, usePointStyle: true }
          },
          title: {
            display: true,
            text: this.translate.instant('insights.spendingByStatus'),
            font: { size: 16, weight: 'bold' }
          }
        }
      }
    });
    this.charts.push(chart);
  }

  /**
   * Get the translated "Uncategorized" label.
   */
  getUncategorizedLabel(): string {
    return this.translate.instant('common.uncategorized');
  }

  /**
   * Calculate the total number of invoices across all spending items.
   */
  getTotalInvoiceCount(): number {
    return this.spendingItems.reduce((sum, item) => sum + (item.invoiceCount || 0), 0);
  }

  /**
   * Calculate the total invoiced amount in CAD across all spending items.
   */
  getTotalInvoicedAmount(): number {
    return this.spendingItems.reduce((sum, item) => sum + (item.invoiceTotalCad || 0), 0);
  }

  /**
   * Calculate the total allocated amount in CAD across all spending items.
   */
  getTotalAllocatedAmount(): number {
    return this.spendingItems.reduce((sum, item) => sum + (item.moneyAllocationTotalCad || 0), 0);
  }

  /**
   * Calculate the percentage of allocated spending covered by invoices.
   */
  getInvoiceCoveragePercent(): number {
    const allocated = this.getTotalAllocatedAmount();
    if (allocated <= 0) return 0;
    return Math.min(100, (this.getTotalInvoicedAmount() / allocated) * 100);
  }

  /**
   * Count spending items that have at least one invoice.
   */
  getItemsWithInvoiceCount(): number {
    return this.spendingItems.filter(item => (item.invoiceCount || 0) > 0).length;
  }

  /**
   * Create invoice coverage horizontal bar chart comparing allocated vs invoiced
   * amounts per category.
   */
  private createInvoiceCoverageChart(): void {
    if (!this.invoiceCoverageChartRef?.nativeElement) return;

    const categoryMap = new Map<string, { allocated: number; invoiced: number }>();
    const uncategorizedLabel = this.getUncategorizedLabel();

    for (const item of this.spendingItems) {
      const categoryName = this.getCategoryDisplayNameById(
        item.categoryId || null,
        item.categoryName || uncategorizedLabel
      );
      const existing = categoryMap.get(categoryName) || { allocated: 0, invoiced: 0 };
      existing.allocated += item.moneyAllocationTotalCad || 0;
      existing.invoiced += item.invoiceTotalCad || 0;
      categoryMap.set(categoryName, existing);
    }

    // Sort by allocated amount descending
    const sorted = Array.from(categoryMap.entries())
      .sort((a, b) => b[1].allocated - a[1].allocated);

    const labels = sorted.map(([name]) => name);
    const allocatedValues = sorted.map(([, data]) => data.allocated);
    const invoicedValues = sorted.map(([, data]) => data.invoiced);

    const chart = new Chart(this.invoiceCoverageChartRef.nativeElement, {
      type: 'bar',
      data: {
        labels,
        datasets: [
          {
            label: this.translate.instant('insights.allocatedAmount'),
            data: allocatedValues,
            backgroundColor: 'rgba(59, 130, 246, 0.7)',
            borderColor: '#3b82f6',
            borderWidth: 1
          },
          {
            label: this.translate.instant('insights.invoicedAmount'),
            data: invoicedValues,
            backgroundColor: 'rgba(16, 185, 129, 0.7)',
            borderColor: '#10b981',
            borderWidth: 1
          }
        ]
      },
      options: {
        indexAxis: 'y',
        responsive: true,
        maintainAspectRatio: false,
        plugins: {
          legend: {
            position: 'top'
          },
          title: {
            display: true,
            text: this.translate.instant('insights.invoiceCoverage'),
            font: { size: 16, weight: 'bold' }
          }
        },
        scales: {
          x: {
            beginAtZero: true,
            ticks: {
              callback: (value: string | number) => '$' + this.formatNumber(value as number)
            }
          }
        }
      }
    });
    this.charts.push(chart);
  }

  /**
   * Aggregate items by category, using translated category names.
   */
  private aggregateByCategory(items: any[], type: 'funding' | 'spending'): { labels: string[]; values: number[] } {
    const categoryMap = new Map<number | null, { name: string; total: number }>();
    const uncategorizedLabel = this.getUncategorizedLabel();
    
    for (const item of items) {
      const categoryId = item.categoryId || null;
      const categoryName = this.getCategoryDisplayNameById(categoryId, item.categoryName || uncategorizedLabel);
      let total = 0;
      const rate = this.getCadConversionRate(item);
      
      if (item.moneyAllocations) {
        for (const allocation of item.moneyAllocations) {
          total += ((allocation.capAmount || 0) + (allocation.omAmount || 0)) * rate;
        }
      }
      
      const existing = categoryMap.get(categoryId);
      if (existing) {
        existing.total += total;
      } else {
        categoryMap.set(categoryId, { name: categoryName, total });
      }
    }
    
    const sorted = Array.from(categoryMap.values()).sort((a, b) => b.total - a.total);
    return {
      labels: sorted.map(entry => entry.name),
      values: sorted.map(entry => entry.total)
    };
  }

  /**
   * Get the translated display name for a category.
   * Looks up the category by ID in the loaded categories array and uses
   * the translationKey for i18n. Falls back to the raw name for custom categories.
   *
   * @param categoryId the category ID to look up
   * @param fallbackName the fallback name to use if the category is not found
   * @returns the translated category name
   */
  getCategoryDisplayNameById(categoryId: number | null | undefined, fallbackName: string): string {
    if (!categoryId) return fallbackName;
    const category = this.categories.find(c => c.id === categoryId);
    if (category) {
      if (category.translationKey) {
        const translated = this.translate.instant(category.translationKey);
        return translated !== category.translationKey ? translated : category.name;
      }
      return category.name;
    }
    return fallbackName;
  }

  /**
   * Calculate funding CAP and OM totals.
   */
  private calculateFundingCapOm(): { fundingCap: number; fundingOm: number } {
    let fundingCap = 0;
    let fundingOm = 0;
    
    for (const item of this.fundingItems) {
      const rate = this.getCadConversionRate(item);
      if (item.moneyAllocations) {
        for (const allocation of item.moneyAllocations) {
          fundingCap += (allocation.capAmount || 0) * rate;
          fundingOm += (allocation.omAmount || 0) * rate;
        }
      }
    }
    
    return { fundingCap, fundingOm };
  }

  /**
   * Calculate spending CAP and OM totals.
   */
  private calculateSpendingCapOm(): { spendingCap: number; spendingOm: number } {
    let spendingCap = 0;
    let spendingOm = 0;
    
    for (const item of this.spendingItems) {
      const rate = this.getCadConversionRate(item);
      if (item.moneyAllocations) {
        for (const allocation of item.moneyAllocations) {
          spendingCap += (allocation.capAmount || 0) * rate;
          spendingOm += (allocation.omAmount || 0) * rate;
        }
      }
    }
    
    return { spendingCap, spendingOm };
  }

  /**
   * Count procurement items by tracking status.
   */
  private countByTrackingStatus(items: ProcurementItem[]): { labels: string[]; values: number[] } {
    // Use fixed order for consistent chart colors
    const trackingStatuses: TrackingStatus[] = ['PLANNING', 'ON_TRACK', 'AT_RISK', 'COMPLETED', 'CANCELLED'];
    const statusCounts = new Map<TrackingStatus, number>();
    
    // Initialize all statuses with 0
    for (const status of trackingStatuses) {
      statusCounts.set(status, 0);
    }
    
    for (const item of items) {
      const trackingStatus = (item.trackingStatus as TrackingStatus) || 'PLANNING';
      statusCounts.set(trackingStatus, (statusCounts.get(trackingStatus) || 0) + 1);
    }
    
    // Build arrays in fixed order, only including statuses with counts > 0
    const labels: string[] = [];
    const values: number[] = [];
    
    for (const status of trackingStatuses) {
      const count = statusCounts.get(status) || 0;
      if (count > 0) {
        const key = 'procurement.status' + status.split('_').map(p => p.charAt(0) + p.slice(1).toLowerCase()).join('');
        const translated = this.translate.instant(key);
        labels.push(translated !== key ? translated : TRACKING_STATUS_INFO[status].label);
        values.push(count);
      }
    }
    
    return { labels, values };
  }

  /**
   * Count procurement items by procurement type.
   */
  private countByProcurementType(items: ProcurementItem[]): { labels: string[]; values: number[] } {
    const procurementTypes: ProcurementType[] = ['RC_INITIATED', 'CENTRALLY_MANAGED'];
    const typeCounts = new Map<ProcurementType, number>();
    
    // Initialize all types with 0
    for (const ptype of procurementTypes) {
      typeCounts.set(ptype, 0);
    }
    
    for (const item of items) {
      const procurementType = (item.procurementType as ProcurementType) || 'RC_INITIATED';
      typeCounts.set(procurementType, (typeCounts.get(procurementType) || 0) + 1);
    }
    
    // Build arrays in fixed order, only including types with counts > 0
    const labels: string[] = [];
    const values: number[] = [];
    
    for (const ptype of procurementTypes) {
      const count = typeCounts.get(ptype) || 0;
      if (count > 0) {
        const keyMap: Record<ProcurementType, string> = {
          RC_INITIATED: 'procurement.procurementTypeRcInitiated',
          CENTRALLY_MANAGED: 'procurement.procurementTypeCentrallyManaged'
        };
        const key = keyMap[ptype];
        const translated = this.translate.instant(key);
        labels.push(translated !== key ? translated : PROCUREMENT_TYPE_INFO[ptype].label);
        values.push(count);
      }
    }
    
    return { labels, values };
  }

  /**
   * Count spending items by status.
   */
  private countSpendingByStatus(): { labels: string[]; values: number[] } {
    const statusMap = new Map<string, number>();
    
    const statusI18nKeys: Record<string, string> = {
      'PLANNING': 'spending.statusPlanning',
      'COMMITTED': 'spending.statusCommitted',
      'COMPLETED': 'spending.statusCompleted',
      'CANCELLED': 'spending.statusCancelled'
    };
    
    const statusFallbacks: Record<string, string> = {
      'PLANNING': 'Planning',
      'COMMITTED': 'Committed',
      'COMPLETED': 'Completed',
      'CANCELLED': 'Cancelled'
    };
    
    for (const item of this.spendingItems) {
      const key = statusI18nKeys[item.status];
      let label: string;
      if (key) {
        const translated = this.translate.instant(key);
        label = translated !== key ? translated : (statusFallbacks[item.status] || item.status);
      } else {
        label = statusFallbacks[item.status] || item.status;
      }
      statusMap.set(label, (statusMap.get(label) || 0) + 1);
    }
    
    return {
      labels: Array.from(statusMap.keys()),
      values: Array.from(statusMap.values())
    };
  }

  /**
   * Format number for display.
   */
  private formatNumber(value: number): string {
    if (value >= 1000000) {
      return (value / 1000000).toFixed(1) + 'M';
    } else if (value >= 1000) {
      return (value / 1000).toFixed(1) + 'K';
    }
    return value.toFixed(0);
  }

  /**
   * Get the CAD conversion rate for an item.
   * Returns 1 for CAD items, the exchangeRate for non-CAD items.
   */
  private getCadConversionRate(item: any): number {
    if (!item.currency || item.currency === 'CAD') return 1;
    return item.exchangeRate || 1;
  }

  /**
   * Aggregate spending amounts by currency for the currency distribution chart.
   */
  private aggregateByCurrency(): { labels: string[]; values: number[] } {
    const currencyMap = new Map<string, number>();

    for (const item of this.spendingItems) {
      const currency = item.currency || 'CAD';
      let total = 0;
      if (item.moneyAllocations) {
        for (const allocation of item.moneyAllocations) {
          total += (allocation.capAmount || 0) + (allocation.omAmount || 0);
        }
      }
      currencyMap.set(currency, (currencyMap.get(currency) || 0) + total);
    }

    // Also include funding items
    for (const item of this.fundingItems) {
      const currency = item.currency || 'CAD';
      let total = 0;
      if (item.moneyAllocations) {
        for (const allocation of item.moneyAllocations) {
          total += (allocation.capAmount || 0) + (allocation.omAmount || 0);
        }
      }
      currencyMap.set(currency, (currencyMap.get(currency) || 0) + total);
    }

    // Sort by amount descending
    const sorted = Array.from(currencyMap.entries())
      .filter(([, amount]) => amount > 0)
      .sort((a, b) => b[1] - a[1]);

    return {
      labels: sorted.map(([currency]) => currency),
      values: sorted.map(([, amount]) => amount)
    };
  }

  /**
   * Create currency distribution doughnut chart.
   */
  private createCurrencyDistributionChart(): void {
    if (!this.currencyDistributionChartRef?.nativeElement) return;

    const currencyData = this.aggregateByCurrency();
    if (currencyData.labels.length === 0) return;

    // Currency-specific colors
    const currencyColors: Record<string, string> = {
      'CAD': '#ef4444', // red
      'USD': '#3b82f6', // blue
      'GBP': '#8b5cf6', // purple
      'EUR': '#10b981', // green
      'AUD': '#f59e0b', // amber
      'NZD': '#06b6d4'  // cyan
    };

    const backgroundColors = currencyData.labels.map(
      (label, i) => currencyColors[label] || this.colors[i % this.colors.length]
    );

    const chart = new Chart(this.currencyDistributionChartRef.nativeElement, {
      type: 'doughnut',
      data: {
        labels: currencyData.labels,
        datasets: [{
          data: currencyData.values,
          backgroundColor: backgroundColors,
          borderWidth: 2,
          borderColor: '#ffffff'
        }]
      },
      options: {
        responsive: true,
        maintainAspectRatio: false,
        plugins: {
          legend: {
            position: 'bottom',
            labels: { padding: 15, usePointStyle: true }
          },
          title: {
            display: true,
            text: this.translate.instant('insights.currencyDistribution'),
            font: { size: 16, weight: 'bold' }
          },
          tooltip: {
            callbacks: {
              label: (context) => {
                const label = context.label || '';
                const value = context.parsed || 0;
                const total = (context.dataset.data as number[]).reduce((a: number, b: number) => a + b, 0);
                const percentage = total > 0 ? ((value / total) * 100).toFixed(1) : '0';
                return `${label}: $${this.formatNumber(value)} (${percentage}%)`;
              }
            }
          }
        }
      }
    });
    this.charts.push(chart);
  }

  /**
   * Format currency for display.
   */
  formatCurrency(value: number | null | undefined, currency = 'CAD'): string {
    if (value === null || value === undefined) return '$0.00';
    return new Intl.NumberFormat('en-CA', {
      style: 'currency',
      currency: currency
    }).format(value);
  }

  /**
   * Create training status pie chart.
   */
  private createTrainingStatusChart(): void {
    if (!this.trainingStatusChartRef?.nativeElement) return;

    const statusCounts = new Map<TrainingItemStatus, number>();
    for (const item of this.trainingItems) {
      statusCounts.set(item.status, (statusCounts.get(item.status) || 0) + 1);
    }

    const labels: string[] = [];
    const values: number[] = [];
    const colors: string[] = [];

    const statusColorMap: Record<TrainingItemStatus, string> = {
      'PLANNED': '#64748b',
      'APPROVED': '#2563eb',
      'IN_PROGRESS': '#f59e0b',
      'COMPLETED': '#16a34a',
      'CANCELLED': '#dc2626'
    };

    for (const [status, count] of statusCounts) {
      labels.push(this.translate.instant('training.status_' + status));
      values.push(count);
      colors.push(statusColorMap[status] || '#94a3b8');
    }

    const chart = new Chart(this.trainingStatusChartRef.nativeElement, {
      type: 'pie',
      data: {
        labels,
        datasets: [{ data: values, backgroundColor: colors, borderWidth: 2, borderColor: '#ffffff' }]
      },
      options: {
        responsive: true,
        maintainAspectRatio: false,
        plugins: {
          legend: { position: 'bottom', labels: { padding: 15, usePointStyle: true } },
          title: { display: true, text: this.translate.instant('insights.trainingByStatus'), font: { size: 16, weight: 'bold' } }
        }
      }
    });
    this.charts.push(chart);
  }

  /**
   * Create training type pie chart.
   */
  private createTrainingTypeChart(): void {
    if (!this.trainingTypeChartRef?.nativeElement) return;

    const typeCounts = new Map<TrainingType, number>();
    for (const item of this.trainingItems) {
      if (item.trainingType) {
        typeCounts.set(item.trainingType, (typeCounts.get(item.trainingType) || 0) + 1);
      }
    }

    const labels: string[] = [];
    const values: number[] = [];

    for (const [type, count] of typeCounts) {
      labels.push(this.translate.instant('training.type_' + type));
      values.push(count);
    }

    const chart = new Chart(this.trainingTypeChartRef.nativeElement, {
      type: 'pie',
      data: {
        labels,
        datasets: [{ data: values, backgroundColor: this.colors.slice(0, labels.length), borderWidth: 2, borderColor: '#ffffff' }]
      },
      options: {
        responsive: true,
        maintainAspectRatio: false,
        plugins: {
          legend: { position: 'bottom', labels: { padding: 15, usePointStyle: true } },
          title: { display: true, text: this.translate.instant('insights.trainingByType'), font: { size: 16, weight: 'bold' } }
        }
      }
    });
    this.charts.push(chart);
  }

  /**
   * Create travel status pie chart.
   */
  private createTravelStatusChart(): void {
    if (!this.travelStatusChartRef?.nativeElement) return;

    const statusCounts = new Map<TravelItemStatus, number>();
    for (const item of this.travelItems) {
      statusCounts.set(item.status, (statusCounts.get(item.status) || 0) + 1);
    }

    const labels: string[] = [];
    const values: number[] = [];
    const colors: string[] = [];

    const statusColorMap: Record<TravelItemStatus, string> = {
      'PLANNED': '#64748b',
      'APPROVED': '#2563eb',
      'IN_PROGRESS': '#f59e0b',
      'COMPLETED': '#16a34a',
      'CANCELLED': '#dc2626'
    };

    for (const [status, count] of statusCounts) {
      labels.push(this.translate.instant('travel.status_' + status));
      values.push(count);
      colors.push(statusColorMap[status] || '#94a3b8');
    }

    const chart = new Chart(this.travelStatusChartRef.nativeElement, {
      type: 'pie',
      data: {
        labels,
        datasets: [{ data: values, backgroundColor: colors, borderWidth: 2, borderColor: '#ffffff' }]
      },
      options: {
        responsive: true,
        maintainAspectRatio: false,
        plugins: {
          legend: { position: 'bottom', labels: { padding: 15, usePointStyle: true } },
          title: { display: true, text: this.translate.instant('insights.travelByStatus'), font: { size: 16, weight: 'bold' } }
        }
      }
    });
    this.charts.push(chart);
  }

  /**
   * Create travel type pie chart.
   */
  private createTravelTypeChart(): void {
    if (!this.travelTypeChartRef?.nativeElement) return;

    const typeCounts = new Map<TravelType, number>();
    for (const item of this.travelItems) {
      if (item.travelType) {
        typeCounts.set(item.travelType, (typeCounts.get(item.travelType) || 0) + 1);
      }
    }

    const labels: string[] = [];
    const values: number[] = [];

    for (const [type, count] of typeCounts) {
      labels.push(this.translate.instant('travel.type_' + type));
      values.push(count);
    }

    const chart = new Chart(this.travelTypeChartRef.nativeElement, {
      type: 'pie',
      data: {
        labels,
        datasets: [{ data: values, backgroundColor: this.colors.slice(0, labels.length), borderWidth: 2, borderColor: '#ffffff' }]
      },
      options: {
        responsive: true,
        maintainAspectRatio: false,
        plugins: {
          legend: { position: 'bottom', labels: { padding: 15, usePointStyle: true } },
          title: { display: true, text: this.translate.instant('insights.travelByType'), font: { size: 16, weight: 'bold' } }
        }
      }
    });
    this.charts.push(chart);
  }

  /**
   * Get total estimated cost for training items (CAD).
   */
  getTotalTrainingEstimated(): number {
    return this.trainingItems.reduce((sum, item) => sum + (item.estimatedCostCad || 0), 0);
  }

  /**
   * Get total estimated cost for travel items (CAD).
   */
  getTotalTravelEstimated(): number {
    return this.travelItems.reduce((sum, item) => sum + (item.estimatedCostCad || 0), 0);
  }
}
