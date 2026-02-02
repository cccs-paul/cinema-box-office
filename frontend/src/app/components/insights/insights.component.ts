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
import { ProcurementItem, PROCUREMENT_STATUS_INFO, ProcurementItemStatus } from '../../models/procurement.model';

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
  @ViewChild('spendingStatusChart') spendingStatusChartRef!: ElementRef<HTMLCanvasElement>;

  currentUser: User | null = null;

  // Selected RC and FY
  selectedRC: ResponsibilityCentreDTO | null = null;
  selectedFY: FiscalYear | null = null;

  // Data
  fundingItems: FundingItem[] = [];
  spendingItems: SpendingItem[] = [];
  procurementItems: ProcurementItem[] = [];
  
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
      if (this.fundingItems.length > 0 || this.spendingItems.length > 0 || this.procurementItems.length > 0) {
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
      procurement: this.procurementService.getProcurementItems(this.selectedRC.id, this.selectedFY.id)
    }).subscribe({
      next: (data) => {
        this.fundingItems = data.funding;
        this.spendingItems = data.spending;
        this.procurementItems = data.procurement;
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
    this.createSpendingStatusChart();
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

    const statusCounts = this.countByStatus(this.procurementItems);
    
    const chart = new Chart(this.procurementStatusChartRef.nativeElement, {
      type: 'pie',
      data: {
        labels: statusCounts.labels,
        datasets: [{
          data: statusCounts.values,
          backgroundColor: [
            '#94a3b8', // DRAFT - gray
            '#fbbf24', // PENDING_QUOTES - yellow
            '#3b82f6', // QUOTES_RECEIVED - blue
            '#f97316', // UNDER_REVIEW - orange
            '#10b981', // APPROVED - green
            '#8b5cf6', // PO_ISSUED - purple
            '#22c55e', // COMPLETED - bright green
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
            text: this.translate.instant('insights.procurementByStatus'),
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
            '#94a3b8', // DRAFT - gray
            '#fbbf24', // PENDING - yellow
            '#10b981', // APPROVED - green
            '#8b5cf6', // COMMITTED - purple
            '#22c55e', // PAID - bright green
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
   * Aggregate items by category.
   */
  private aggregateByCategory(items: any[], type: 'funding' | 'spending'): { labels: string[]; values: number[] } {
    const categoryMap = new Map<string, number>();
    const uncategorizedLabel = this.getUncategorizedLabel();
    
    for (const item of items) {
      const categoryName = item.categoryName || uncategorizedLabel;
      let total = 0;
      
      if (item.moneyAllocations) {
        for (const allocation of item.moneyAllocations) {
          total += (allocation.capAmount || 0) + (allocation.omAmount || 0);
        }
      }
      
      categoryMap.set(categoryName, (categoryMap.get(categoryName) || 0) + total);
    }
    
    const sorted = Array.from(categoryMap.entries()).sort((a, b) => b[1] - a[1]);
    return {
      labels: sorted.map(([label]) => label),
      values: sorted.map(([, value]) => value)
    };
  }

  /**
   * Calculate funding CAP and OM totals.
   */
  private calculateFundingCapOm(): { fundingCap: number; fundingOm: number } {
    let fundingCap = 0;
    let fundingOm = 0;
    
    for (const item of this.fundingItems) {
      if (item.moneyAllocations) {
        for (const allocation of item.moneyAllocations) {
          fundingCap += allocation.capAmount || 0;
          fundingOm += allocation.omAmount || 0;
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
      if (item.moneyAllocations) {
        for (const allocation of item.moneyAllocations) {
          spendingCap += allocation.capAmount || 0;
          spendingOm += allocation.omAmount || 0;
        }
      }
    }
    
    return { spendingCap, spendingOm };
  }

  /**
   * Count procurement items by status.
   */
  private countByStatus(items: ProcurementItem[]): { labels: string[]; values: number[] } {
    const statusMap = new Map<string, number>();
    
    for (const item of items) {
      // Use PROCUREMENT_STATUS_INFO for user-friendly labels
      const statusInfo = PROCUREMENT_STATUS_INFO[item.status as ProcurementItemStatus];
      const label = statusInfo?.label || item.status;
      statusMap.set(label, (statusMap.get(label) || 0) + 1);
    }
    
    return {
      labels: Array.from(statusMap.keys()),
      values: Array.from(statusMap.values())
    };
  }

  /**
   * Count spending items by status.
   */
  private countSpendingByStatus(): { labels: string[]; values: number[] } {
    const statusMap = new Map<string, number>();
    
    const statusLabels: Record<string, string> = {
      'DRAFT': 'Draft',
      'PENDING': 'Pending',
      'APPROVED': 'Approved',
      'COMMITTED': 'Committed',
      'PAID': 'Paid',
      'CANCELLED': 'Cancelled'
    };
    
    for (const item of this.spendingItems) {
      const label = statusLabels[item.status] || item.status;
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
   * Format currency for display.
   */
  formatCurrency(value: number | null | undefined, currency = 'CAD'): string {
    if (value === null || value === undefined) return '$0.00';
    return new Intl.NumberFormat('en-CA', {
      style: 'currency',
      currency: currency
    }).format(value);
  }
}
