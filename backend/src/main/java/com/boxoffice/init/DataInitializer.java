/*
 * myRC User Management System
 * Data Initialization Component
 * 
 * Author: myRC Team
 * Date: 2026-01-17
 * Version: 1.0.0
 * 
 * License: Apache License 2.0
 */

package com.boxoffice.init;

import com.boxoffice.dto.CreateUserRequest;
import com.boxoffice.model.Category;
import com.boxoffice.model.Currency;
import com.boxoffice.model.FiscalYear;
import com.boxoffice.model.FundingItem;
import com.boxoffice.model.FundingSource;
import com.boxoffice.model.Money;
import com.boxoffice.model.MoneyAllocation;
import com.boxoffice.model.ProcurementItem;
import com.boxoffice.model.RCAccess;
import com.boxoffice.model.ResponsibilityCentre;
import com.boxoffice.model.SpendingItem;
import com.boxoffice.model.SpendingMoneyAllocation;
import com.boxoffice.model.User;
import com.boxoffice.repository.CategoryRepository;
import com.boxoffice.repository.FiscalYearRepository;
import com.boxoffice.repository.FundingItemRepository;
import com.boxoffice.repository.MoneyRepository;
import com.boxoffice.repository.ProcurementItemRepository;
import com.boxoffice.repository.RCAccessRepository;
import com.boxoffice.repository.ResponsibilityCentreRepository;
import com.boxoffice.repository.SpendingItemRepository;
import com.boxoffice.repository.UserRepository;
import com.boxoffice.service.MoneyService;
import com.boxoffice.service.CategoryService;
import com.boxoffice.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

/**
 * Initializes default application data on startup.
 * Creates a default admin user if no users exist in the database.
 * Skips initialization in test profile.
 */
@Component
@org.springframework.context.annotation.Profile("!test")
public class DataInitializer implements ApplicationRunner {

    private static final Logger logger = Logger.getLogger(DataInitializer.class.getName());

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private ResponsibilityCentreRepository rcRepository;

    @Autowired
    private RCAccessRepository rcAccessRepository;

    @Autowired
    private FiscalYearRepository fiscalYearRepository;

    @Autowired
    private MoneyService moneyService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private MoneyRepository moneyRepository;

    @Autowired
    private FundingItemRepository fundingItemRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private SpendingItemRepository spendingItemRepository;

    @Autowired
    private ProcurementItemRepository procurementItemRepository;

    @Override
    public void run(org.springframework.boot.ApplicationArguments args) throws Exception {
        initializeDefaultUsers();
        initializeDemoRC();
    }

    private void initializeDefaultUsers() {
        // Check if admin user already exists
        if (userRepository.findByUsername("admin").isEmpty()) {
            logger.info("Creating default admin user...");
            
            CreateUserRequest adminRequest = new CreateUserRequest();
            adminRequest.setUsername("admin");
            adminRequest.setEmail("admin@myrc.local");
            adminRequest.setFullName("Administrator");
            adminRequest.setPassword("Admin@123");
            adminRequest.setAuthProvider("LOCAL");
            
            Set<String> adminRoles = new HashSet<>();
            adminRoles.add("ADMIN");
            adminRoles.add("USER");
            adminRequest.setRoles(adminRoles);
            adminRequest.setProfileDescription("Default system administrator account");
            
            try {
                userService.createUser(adminRequest);
                logger.info("Default admin user created successfully with username: admin, password: Admin@123");
                logger.info("IMPORTANT: Please change the default admin password immediately for security!");
            } catch (Exception e) {
                logger.warning(() -> "Failed to create default admin user: " + e.getMessage());
            }
        } else {
            logger.info("Admin user already exists, skipping initialization");
        }
        
        // Check if default-user exists (for unauthenticated development access)
        if (userRepository.findByUsername("default-user").isEmpty()) {
            logger.info("Creating default-user for unauthenticated access...");
            
            CreateUserRequest defaultRequest = new CreateUserRequest();
            defaultRequest.setUsername("default-user");
            defaultRequest.setEmail("default@myrc.local");
            defaultRequest.setFullName("Default User");
            defaultRequest.setPassword("DefaultPass@123");
            defaultRequest.setAuthProvider("LOCAL");
            
            Set<String> userRoles = new HashSet<>();
            userRoles.add("USER");
            defaultRequest.setRoles(userRoles);
            defaultRequest.setProfileDescription("Default user for unauthenticated access");
            
            try {
                userService.createUser(defaultRequest);
                logger.info("Default user created successfully for unauthenticated development access");
            } catch (Exception e) {
                logger.warning(() -> "Failed to create default user: " + e.getMessage());
            }
        } else {
            logger.info("Default user already exists, skipping initialization");
        }
    }

    /**
     * Initialize the Demo RC with read-only access for all users.
     * The Demo RC is owned by admin and all other users get read-only access.
     * Also creates a Demo FY "FY 2025-2026" for the Demo RC.
     */
    private void initializeDemoRC() {
        User adminUser = userRepository.findByUsername("admin").orElse(null);
        if (adminUser == null) {
            logger.warning("Admin user not found, cannot create Demo RC");
            return;
        }

        ResponsibilityCentre demoRC;

        // Check if Demo RC already exists
        if (rcRepository.findByNameAndOwner("Demo", adminUser).isPresent()) {
            logger.info("Demo RC already exists, ensuring all users have read-only access");
            demoRC = rcRepository.findByNameAndOwner("Demo", adminUser).get();
            grantDemoAccessToAllUsers();
        } else {
            logger.info("Creating Demo RC...");
            try {
                demoRC = new ResponsibilityCentre(
                    "Demo",
                    "Demo responsibility centre for exploring the application. All users have read-only access.",
                    adminUser
                );
                demoRC = rcRepository.save(demoRC);
                logger.info("Demo RC created successfully");

                // Grant read-only access to all non-admin users
                grantDemoAccessToAllUsers();
            } catch (Exception e) {
                logger.warning(() -> "Failed to create Demo RC: " + e.getMessage());
                return;
            }
        }

        // Create Demo FY if it doesn't exist
        initializeDemoFY(demoRC);
    }

    /**
     * Initialize the Demo FY "FY 2025-2026" for the Demo RC.
     *
     * @param demoRC the Demo responsibility centre
     */
    private void initializeDemoFY(ResponsibilityCentre demoRC) {
        String demoFYName = "FY 2025-2026";

        FiscalYear demoFY;
        if (fiscalYearRepository.existsByNameAndResponsibilityCentre(demoFYName, demoRC)) {
            logger.info("Demo FY already exists, ensuring default money and spending categories exist");
            // Ensure default money exists for existing demo FY
            demoFY = fiscalYearRepository.findByNameAndResponsibilityCentre(demoFYName, demoRC).orElse(null);
            if (demoFY != null) {
                moneyService.ensureDefaultMoneyExists(demoFY.getId());
                categoryService.initializeDefaultCategories(demoFY.getId());
                // Ensure custom money types exist
                initializeDemoMoneyTypes(demoFY);
                // Ensure custom categories exist
                initializeDemoCategories(demoFY);
                // Also ensure demo funding items exist
                initializeDemoFundingItems(demoFY);
                // Also ensure demo spending items exist
                initializeDemoSpendingItems(demoFY);
                // Also ensure demo procurement items exist
                initializeDemoProcurementItems(demoFY);
            }
            return;
        }

        logger.info("Creating Demo FY...");
        try {
            demoFY = new FiscalYear(
                demoFYName,
                "Demo fiscal year for exploring the application.",
                demoRC
            );
            FiscalYear savedFY = fiscalYearRepository.save(demoFY);
            logger.info("Demo FY created successfully: " + demoFYName);

            // Create default AB money for the demo FY
            moneyService.ensureDefaultMoneyExists(savedFY.getId());
            logger.info("Default AB money created for Demo FY");

            // Create custom money types for demo
            initializeDemoMoneyTypes(savedFY);
            logger.info("Custom money types created for Demo FY");

            // Create default categories for the demo FY
            categoryService.initializeDefaultCategories(savedFY.getId());
            logger.info("Default categories created for Demo FY");

            // Create custom categories for demo
            initializeDemoCategories(savedFY);
            logger.info("Custom categories created for Demo FY");

            // Create demo funding items
            initializeDemoFundingItems(savedFY);

            // Create demo spending items
            initializeDemoSpendingItems(savedFY);

            // Create demo procurement items
            initializeDemoProcurementItems(savedFY);
        } catch (Exception e) {
            logger.warning(() -> "Failed to create Demo FY: " + e.getMessage());
        }
    }

    /**
     * Initialize demo funding items for the Demo FY.
     * Creates sample funding items with realistic money allocations.
     *
     * @param demoFY the Demo fiscal year
     */
    private void initializeDemoFundingItems(FiscalYear demoFY) {
        // Get the AB money for this fiscal year
        List<Money> fyMonies = moneyRepository.findByFiscalYearId(demoFY.getId());
        if (fyMonies.isEmpty()) {
            logger.warning("No money types found for Demo FY, skipping funding item creation");
            return;
        }

        Money abMoney = fyMonies.stream()
            .filter(m -> "AB".equals(m.getCode()))
            .findFirst()
            .orElse(fyMonies.get(0));

        // Get categories for this fiscal year
        List<Category> categories = categoryRepository.findByFiscalYearIdOrderByDisplayOrderAscNameAsc(demoFY.getId());
        java.util.Map<String, Category> categoryMap = categories.stream()
            .collect(java.util.stream.Collectors.toMap(Category::getName, c -> c, (a, b) -> a));

        // Demo Funding Items with sample data
        // {name, description, source, capAmount, omAmount, categoryName}
        String[][] demoItems = {
            {"IT Infrastructure", "Annual IT infrastructure maintenance and upgrades", "APPROVED", "150000.00", "100000.00", "Compute"},
            {"Staff Training", "Employee professional development and training programs", "BUSINESS_PLAN", "25000.00", "50000.00", "Professional Services"},
            {"Office Supplies", "General office supplies and consumables", "BUSINESS_PLAN", "0.00", "15000.00", "Small Procurement"},
            {"Software Licenses", "Annual software license renewals and new acquisitions", "APPROVED", "80000.00", "40000.00", "Software Licenses"},
            {"Consulting Services", "External consulting and advisory services", "ON_RAMP", "0.00", "200000.00", "Contractors"},
            {"Equipment Purchase", "New equipment and hardware purchases", "BUSINESS_PLAN", "175000.00", "0.00", "Compute"},
            {"Travel & Accommodation", "Business travel and accommodation expenses", "APPROVED_DEFICIT", "0.00", "50000.00", "Small Procurement"},
            {"Building Maintenance", "Facility maintenance and repairs", "BUSINESS_PLAN", "35000.00", "50000.00", "Small Procurement"}
        };

        for (String[] item : demoItems) {
            String name = item[0];
            String description = item[1];
            FundingSource source = FundingSource.fromString(item[2]);
            BigDecimal capAmount = new BigDecimal(item[3]);
            BigDecimal omAmount = new BigDecimal(item[4]);
            String categoryName = item[5];

            // Check if funding item already exists
            if (fundingItemRepository.existsByNameAndFiscalYear(name, demoFY)) {
                logger.info("Demo funding item '" + name + "' already exists, skipping");
                continue;
            }

            // Find the category (optional for funding items)
            Category category = categoryMap.get(categoryName);
            if (category == null) {
                logger.info("Category '" + categoryName + "' not found for funding item '" + name + "', creating without category");
            }

            try {
                FundingItem fundingItem = new FundingItem(name, description, source, demoFY);
                fundingItem.setCategory(category);
                fundingItem = fundingItemRepository.save(fundingItem);

                // Create money allocation with CAP and OM values
                MoneyAllocation allocation = new MoneyAllocation(fundingItem, abMoney, capAmount, omAmount);
                fundingItem.addMoneyAllocation(allocation);
                fundingItemRepository.save(fundingItem);

                String categoryInfo = category != null ? " [" + categoryName + "]" : "";
                logger.info("Created demo funding item: " + name + categoryInfo + " (CAP: $" + capAmount + ", OM: $" + omAmount + ")");
            } catch (Exception e) {
                logger.warning(() -> "Failed to create demo funding item '" + name + "': " + e.getMessage());
            }
        }

        logger.info("Demo funding items initialized for FY: " + demoFY.getName());
    }

    /**
     * Initialize custom money types for the Demo FY.
     * Creates additional money types beyond the default AB.
     *
     * @param demoFY the Demo fiscal year
     */
    private void initializeDemoMoneyTypes(FiscalYear demoFY) {
        // Custom money types for demo
        String[][] demoMoneyTypes = {
            // {code, name, description, displayOrder}
            {"OA", "Operating Allotment", "Regular operating budget allocation", "1"},
            {"WCF", "Working Capital Fund", "Revolving fund for operations", "2"},
            {"GF", "Grant Funding", "External grant funding sources", "3"}
        };

        for (String[] moneyType : demoMoneyTypes) {
            String code = moneyType[0];
            String name = moneyType[1];
            String description = moneyType[2];
            int displayOrder = Integer.parseInt(moneyType[3]);

            // Check if money type already exists
            if (moneyRepository.existsByCodeAndFiscalYear(code, demoFY)) {
                logger.info("Demo money type '" + code + "' already exists, skipping");
                continue;
            }

            try {
                Money money = new Money(code, name, description, demoFY);
                money.setDisplayOrder(displayOrder);
                money.setIsDefault(false);
                moneyRepository.save(money);
                logger.info("Created demo money type: " + code + " - " + name);
            } catch (Exception e) {
                logger.warning(() -> "Failed to create demo money type '" + code + "': " + e.getMessage());
            }
        }
    }

    /**
     * Initialize custom categories for the Demo FY.
     * Creates additional categories beyond the defaults.
     *
     * @param demoFY the Demo fiscal year
     */
    private void initializeDemoCategories(FiscalYear demoFY) {
        // Custom categories for demo
        String[][] demoCategories = {
            // {name, description, displayOrder}
            {"Cloud Services", "Cloud computing and hosting services (AWS, Azure, GCP)", "10"},
            {"Security", "Cybersecurity tools, audits, and compliance", "11"},
            {"Research Equipment", "Specialized equipment for research projects", "12"},
            {"Data Services", "Data storage, backup, and analytics services", "13"},
            {"Professional Services", "Consulting, legal, and accounting services", "14"}
        };

        for (String[] category : demoCategories) {
            String name = category[0];
            String description = category[1];
            int displayOrder = Integer.parseInt(category[2]);

            // Check if category already exists
            if (categoryRepository.existsByNameAndFiscalYear(name, demoFY)) {
                logger.info("Demo category '" + name + "' already exists, skipping");
                continue;
            }

            try {
                Category cat = new Category(name, description, demoFY);
                cat.setDisplayOrder(displayOrder);
                cat.setIsDefault(false);
                categoryRepository.save(cat);
                logger.info("Created demo category: " + name);
            } catch (Exception e) {
                logger.warning(() -> "Failed to create demo category '" + name + "': " + e.getMessage());
            }
        }
    }

    /**
     * Initialize demo spending items for the Demo FY.
     * Creates sample spending items with realistic money allocations.
     *
     * @param demoFY the Demo fiscal year
     */
    private void initializeDemoSpendingItems(FiscalYear demoFY) {
        // Get all money types for this fiscal year
        List<Money> fyMonies = moneyRepository.findByFiscalYearId(demoFY.getId());
        if (fyMonies.isEmpty()) {
            logger.warning("No money types found for Demo FY, skipping spending item creation");
            return;
        }

        // Get specific money types
        Money abMoney = fyMonies.stream()
            .filter(m -> "AB".equals(m.getCode()))
            .findFirst()
            .orElse(fyMonies.get(0));
        Money oaMoney = fyMonies.stream()
            .filter(m -> "OA".equals(m.getCode()))
            .findFirst()
            .orElse(null);
        Money wcfMoney = fyMonies.stream()
            .filter(m -> "WCF".equals(m.getCode()))
            .findFirst()
            .orElse(null);

        // Get categories
        List<Category> categories = categoryRepository.findByFiscalYearIdOrderByDisplayOrderAscNameAsc(demoFY.getId());
        if (categories.isEmpty()) {
            logger.warning("No categories found for Demo FY, skipping spending item creation");
            return;
        }

        // Map category names to Category objects
        java.util.Map<String, Category> categoryMap = categories.stream()
            .collect(java.util.stream.Collectors.toMap(Category::getName, c -> c, (a, b) -> a));

        // Demo Spending Items with sample data
        // {name, description, amount, status, categoryName, vendor, reference, moneyCode, capAmount, omAmount}
        String[][] demoItems = {
            {"Dell PowerEdge Servers", "3x Dell PowerEdge R750 rack servers for data center", "45000.00", "APPROVED", 
             "Compute", "Dell Technologies", "PO-2025-001", "AB", "45000.00", "0.00"},
            {"NVIDIA A100 GPUs", "4x NVIDIA A100 80GB GPUs for ML workloads", "52000.00", "COMMITTED", 
             "GPUs", "NVIDIA Corp", "PO-2025-002", "AB", "52000.00", "0.00"},
            {"AWS Monthly Services", "AWS EC2, S3, and RDS services - January 2026", "8500.00", "PAID", 
             "Cloud Services", "Amazon Web Services", "INV-AWS-JAN26", "OA", "0.00", "8500.00"},
            {"Azure DevOps Licenses", "50 Azure DevOps user licenses - annual", "12000.00", "APPROVED", 
             "Software Licenses", "Microsoft Corporation", "PO-2025-003", "AB", "0.00", "12000.00"},
            {"NetApp Storage Array", "NetApp AFF A400 storage system with 50TB capacity", "78000.00", "PENDING", 
             "Storage", "NetApp Inc", "PO-2025-004", "AB", "78000.00", "0.00"},
            {"Security Assessment", "Annual penetration testing and security audit", "35000.00", "APPROVED", 
             "Security", "CrowdStrike", "SO-2025-001", "OA", "0.00", "35000.00"},
            {"Lab Equipment", "Oscilloscopes and signal generators for research lab", "28000.00", "DRAFT", 
             "Research Equipment", "Keysight Technologies", "RQ-2025-001", "WCF", "28000.00", "0.00"},
            {"Data Analytics Platform", "Snowflake data warehouse annual subscription", "42000.00", "APPROVED", 
             "Data Services", "Snowflake Inc", "PO-2025-005", "OA", "0.00", "42000.00"},
            {"Office Supplies Q1", "General office supplies and consumables for Q1", "3500.00", "PAID", 
             "Small Procurement", "Staples", "INV-STP-JAN26", "AB", "0.00", "3500.00"},
            {"IT Consulting", "Cloud migration consulting services", "65000.00", "COMMITTED", 
             "Professional Services", "Accenture", "SO-2025-002", "OA", "0.00", "65000.00"},
            {"Network Switches", "Cisco Catalyst 9300 switches for network upgrade", "32000.00", "APPROVED", 
             "Compute", "Cisco Systems", "PO-2025-006", "AB", "32000.00", "0.00"},
            {"GPU Cloud Credits", "Google Cloud GPU compute credits for training", "15000.00", "COMMITTED", 
             "Cloud Services", "Google Cloud", "INV-GCP-JAN26", "WCF", "0.00", "15000.00"},
            {"Adobe Creative Suite", "25 Adobe Creative Cloud licenses - annual", "18000.00", "APPROVED", 
             "Software Licenses", "Adobe Inc", "PO-2025-007", "AB", "0.00", "18000.00"},
            {"External Contractors", "3 contractors for data center migration project", "95000.00", "COMMITTED", 
             "Contractors", "TechForce Solutions", "SO-2025-003", "AB", "0.00", "95000.00"},
            {"Backup Storage", "Veeam backup infrastructure and licenses", "24000.00", "PENDING", 
             "Storage", "Veeam Software", "PO-2025-008", "OA", "12000.00", "12000.00"}
        };

        for (String[] item : demoItems) {
            String name = item[0];
            String description = item[1];
            BigDecimal amount = new BigDecimal(item[2]);
            SpendingItem.Status status = SpendingItem.Status.valueOf(item[3]);
            String categoryName = item[4];
            String vendor = item[5];
            String reference = item[6];
            String moneyCode = item[7];
            BigDecimal capAmount = new BigDecimal(item[8]);
            BigDecimal omAmount = new BigDecimal(item[9]);

            // Check if spending item already exists
            if (spendingItemRepository.existsByNameAndFiscalYear(name, demoFY)) {
                logger.info("Demo spending item '" + name + "' already exists, skipping");
                continue;
            }

            // Find the category
            Category category = categoryMap.get(categoryName);
            if (category == null) {
                logger.warning("Category '" + categoryName + "' not found for spending item '" + name + "', skipping");
                continue;
            }

            // Find the money type
            Money money = fyMonies.stream()
                .filter(m -> moneyCode.equals(m.getCode()))
                .findFirst()
                .orElse(abMoney);

            try {
                SpendingItem spendingItem = new SpendingItem(name, description, amount, status, category, demoFY);
                spendingItem.setVendor(vendor);
                spendingItem.setReferenceNumber(reference);
                spendingItem = spendingItemRepository.save(spendingItem);

                // Create money allocation with CAP and OM values
                SpendingMoneyAllocation allocation = new SpendingMoneyAllocation(spendingItem, money, capAmount, omAmount);
                spendingItem.addMoneyAllocation(allocation);
                spendingItemRepository.save(spendingItem);

                logger.info("Created demo spending item: " + name + " (" + categoryName + ", " + moneyCode + 
                           " CAP: $" + capAmount + ", OM: $" + omAmount + ")");
            } catch (Exception e) {
                logger.warning(() -> "Failed to create demo spending item '" + name + "': " + e.getMessage());
            }
        }

        logger.info("Demo spending items initialized for FY: " + demoFY.getName());
    }

    /**
     * Initialize demo procurement items for the Demo FY.
     * Creates sample procurement items with various statuses, currencies, and realistic data.
     *
     * @param demoFY the Demo fiscal year
     */
    private void initializeDemoProcurementItems(FiscalYear demoFY) {
        // Demo Procurement Items with sample data
        // {pr, po, name, description, status, currency, exchangeRate}
        Object[][] demoItems = {
            {"PR-2025-001", "PO-2025-001", "Dell PowerEdge Servers", 
             "3x Dell PowerEdge R750 rack servers for data center expansion",
             ProcurementItem.Status.COMPLETED, Currency.CAD, null},
            {"PR-2025-002", "PO-2025-002", "NVIDIA A100 GPUs", 
             "4x NVIDIA A100 80GB GPUs for machine learning workloads",
             ProcurementItem.Status.PO_ISSUED, Currency.USD, new BigDecimal("1.360000")},
            {"PR-2025-003", null, "Cisco Network Switches", 
             "Cisco Catalyst 9300 switches for network infrastructure upgrade",
             ProcurementItem.Status.APPROVED, Currency.CAD, null},
            {"PR-2025-004", null, "NetApp Storage Array", 
             "NetApp AFF A400 storage system with 50TB capacity for data center",
             ProcurementItem.Status.QUOTES_RECEIVED, Currency.CAD, null},
            {"PR-2025-005", null, "HP LaserJet Printers", 
             "5x HP LaserJet Enterprise printers for office deployment",
             ProcurementItem.Status.PENDING_QUOTES, Currency.CAD, null},
            {"PR-2025-006", null, "IBM Cloud Credits", 
             "Annual IBM Cloud compute and storage credits",
             ProcurementItem.Status.UNDER_REVIEW, Currency.USD, new BigDecimal("1.360000")},
            {"PR-2025-007", "PO-2025-003", "Lenovo ThinkPads", 
             "25x Lenovo ThinkPad X1 Carbon laptops for staff refresh",
             ProcurementItem.Status.COMPLETED, Currency.CAD, null},
            {"PR-2025-008", null, "Autodesk Licenses", 
             "50x Autodesk AutoCAD licenses - 3-year subscription",
             ProcurementItem.Status.DRAFT, Currency.USD, new BigDecimal("1.360000")},
            {"PR-2025-009", null, "Laboratory Equipment", 
             "Oscilloscopes and signal generators for research laboratory",
             ProcurementItem.Status.PENDING_QUOTES, Currency.EUR, new BigDecimal("1.470000")},
            {"PR-2025-010", "PO-2025-004", "AWS Reserved Instances", 
             "3-year reserved capacity for EC2 and RDS instances",
             ProcurementItem.Status.PO_ISSUED, Currency.USD, new BigDecimal("1.360000")},
            {"PR-2025-011", null, "Office Furniture", 
             "Standing desks and ergonomic chairs for new office space",
             ProcurementItem.Status.APPROVED, Currency.CAD, null},
            {"PR-2025-012", null, "Security Assessment Services", 
             "Annual penetration testing and security audit services",
             ProcurementItem.Status.QUOTES_RECEIVED, Currency.CAD, null},
            {"PR-2025-013", null, "UK Training Program", 
             "Staff training program with UK-based vendor",
             ProcurementItem.Status.DRAFT, Currency.GBP, new BigDecimal("1.680000")},
            {"PR-2025-014", null, "Video Conferencing System", 
             "Cisco Webex Board 85 for main conference room",
             ProcurementItem.Status.UNDER_REVIEW, Currency.CAD, null},
            {"PR-2025-015", null, "European Instrumentation", 
             "Specialized instrumentation from European manufacturer",
             ProcurementItem.Status.PENDING_QUOTES, Currency.EUR, new BigDecimal("1.470000")}
        };

        for (Object[] item : demoItems) {
            String pr = (String) item[0];
            String po = (String) item[1];
            String name = (String) item[2];
            String description = (String) item[3];
            ProcurementItem.Status status = (ProcurementItem.Status) item[4];
            Currency currency = (Currency) item[5];
            BigDecimal exchangeRate = (BigDecimal) item[6];

            // Check if procurement item already exists
            if (procurementItemRepository.existsByPurchaseRequisitionAndFiscalYearAndActiveTrue(pr, demoFY)) {
                logger.info("Demo procurement item '" + pr + "' already exists, skipping");
                continue;
            }

            try {
                ProcurementItem procurementItem = new ProcurementItem(pr, name, description, status, demoFY);
                procurementItem.setPurchaseOrder(po);
                procurementItem.setCurrency(currency);
                procurementItem.setExchangeRate(exchangeRate);
                procurementItemRepository.save(procurementItem);

                String currencyInfo = currency != Currency.CAD ? 
                    " (" + currency + " @ " + exchangeRate + ")" : "";
                logger.info("Created demo procurement item: " + pr + " - " + name + 
                           " [" + status + "]" + currencyInfo);
            } catch (Exception e) {
                logger.warning(() -> "Failed to create demo procurement item '" + pr + "': " + e.getMessage());
            }
        }

        logger.info("Demo procurement items initialized for FY: " + demoFY.getName());
    }

    /**
     * Grant read-only access to the Demo RC for all users who don't already have access.
     */
    private void grantDemoAccessToAllUsers() {
        User adminUser = userRepository.findByUsername("admin").orElse(null);
        if (adminUser == null) {
            return;
        }

        ResponsibilityCentre demoRC = rcRepository.findByNameAndOwner("Demo", adminUser).orElse(null);
        if (demoRC == null) {
            return;
        }

        List<User> allUsers = userRepository.findAll();
        for (User user : allUsers) {
            // Skip if this is the owner (admin)
            if (user.getId().equals(adminUser.getId())) {
                continue;
            }

            // Check if user already has access
            if (rcAccessRepository.findByResponsibilityCentreAndUser(demoRC, user).isPresent()) {
                continue;
            }

            // Grant read-only access
            try {
                RCAccess access = new RCAccess(demoRC, user, RCAccess.AccessLevel.READ_ONLY);
                rcAccessRepository.save(access);
                logger.info(() -> "Granted read-only access to Demo RC for user: " + user.getUsername());
            } catch (Exception e) {
                logger.warning(() -> "Failed to grant Demo RC access to user " + user.getUsername() + ": " + e.getMessage());
            }
        }
    }
}
