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

package com.myrc.init;

import com.myrc.dto.CreateUserRequest;
import com.myrc.model.Category;
import com.myrc.model.Currency;
import com.myrc.model.FiscalYear;
import com.myrc.model.FundingItem;
import com.myrc.model.FundingSource;
import com.myrc.model.Money;
import com.myrc.model.MoneyAllocation;
import com.myrc.model.ProcurementEvent;
import com.myrc.model.ProcurementItem;
import com.myrc.model.ProcurementQuote;
import com.myrc.model.ProcurementQuoteFile;
import com.myrc.model.RCAccess;
import com.myrc.model.ResponsibilityCentre;
import com.myrc.model.SpendingItem;
import com.myrc.model.SpendingMoneyAllocation;
import com.myrc.model.User;
import com.myrc.repository.CategoryRepository;
import com.myrc.repository.FiscalYearRepository;
import com.myrc.repository.FundingItemRepository;
import com.myrc.repository.MoneyRepository;
import com.myrc.repository.ProcurementEventRepository;
import com.myrc.repository.ProcurementItemRepository;
import com.myrc.repository.ProcurementQuoteFileRepository;
import com.myrc.repository.ProcurementQuoteRepository;
import com.myrc.repository.RCAccessRepository;
import com.myrc.repository.ResponsibilityCentreRepository;
import com.myrc.repository.SpendingItemRepository;
import com.myrc.repository.UserRepository;
import com.myrc.service.MoneyService;
import com.myrc.service.CategoryService;
import com.myrc.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
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

    @Autowired
    private ProcurementQuoteRepository procurementQuoteRepository;

    @Autowired
    private ProcurementQuoteFileRepository procurementQuoteFileRepository;

    @Autowired
    private ProcurementEventRepository procurementEventRepository;

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
                // Also ensure demo discrete spending items exist
                initializeDemoSpendingItems(demoFY);
                // Also ensure demo procurement items exist
                initializeDemoProcurementItems(demoFY);
                // Create spending items linked to procurement items (after procurement is created)
                initializeProcurementLinkedSpendingItems(demoFY);
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

            // Create demo discrete spending items (not linked to procurement)
            initializeDemoSpendingItems(savedFY);

            // Create demo procurement items
            initializeDemoProcurementItems(savedFY);
            
            // Create spending items linked to procurement items (after procurement is created)
            initializeProcurementLinkedSpendingItems(savedFY);
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
        // {name, description, source, capAmount, omAmount, categoryName, comments}
        String[][] demoItems = {
            {"IT Infrastructure", "Annual IT infrastructure maintenance and upgrades", "BUSINESS_PLAN", "150000.00", "100000.00", "Compute", "Base allocation from the FY 2025-2026 business plan. Includes server upgrades and network equipment."},
            {"Staff Training", "Employee professional development and training programs", "BUSINESS_PLAN", "25000.00", "50000.00", "Professional Services", "Annual training budget from business plan. Includes certifications, conferences, and professional development courses."},
            {"Office Supplies", "General office supplies and consumables", "BUSINESS_PLAN", "0.00", "15000.00", "Small Procurement", "Monthly recurring expense for office consumables. Funded through regular O&M budget."},
            {"Software Licenses", "Annual software license renewals and new acquisitions", "BUSINESS_PLAN", "80000.00", "40000.00", "Software Licenses", "Baseline software licensing from business plan. Includes Microsoft 365, Adobe Creative Cloud, and specialized engineering software."},
            {"Consulting Services", "External consulting and advisory services", "ON_RAMP", "0.00", "200000.00", "Contractors", "Cloud migration consulting - additional funding received via on-ramp process in January 2026. Ref: OR-2026-012"},
            {"Equipment Purchase", "New equipment and hardware purchases", "BUSINESS_PLAN", "175000.00", "0.00", "Compute", "Capital equipment budget for fiscal year. Includes lab equipment and data center hardware. BP ref: CAP-2025-045"},
            {"Travel & Accommodation", "Business travel and accommodation expenses", "APPROVED_DEFICIT", "0.00", "50000.00", "Small Procurement", "Emergency travel budget approved Q4 to cover essential conference attendance. Deficit approval: DA-2025-089"},
            {"Building Maintenance", "Facility maintenance and repairs", "BUSINESS_PLAN", "35000.00", "50000.00", "Small Procurement", "Facility upgrades including HVAC and security systems. Annual maintenance allocation."},
            {"GPU Infrastructure", "High-performance GPU computing resources", "ON_RAMP", "120000.00", "25000.00", "GPUs", "Additional GPU allocation received through on-ramp to support ML/AI workloads. On-ramp ref: OR-2026-008"},
            {"Cloud Services Budget", "AWS, Azure, and GCP cloud services", "BUSINESS_PLAN", "0.00", "180000.00", "Cloud Services", "Annual cloud services budget. Includes compute, storage, and networking costs across multiple providers."},
            {"Research Equipment Fund", "Specialized research and laboratory equipment", "APPROVED_DEFICIT", "95000.00", "15000.00", "Research Equipment", "Deficit funding approved for urgent research equipment needs. Approval ref: DA-2025-102"},
            {"Security Tools & Services", "Cybersecurity infrastructure and services", "BUSINESS_PLAN", "45000.00", "65000.00", "Security", "Baseline security budget. Includes SIEM, endpoint protection, and penetration testing."},
            {"Storage Expansion", "Data storage infrastructure expansion", "ON_RAMP", "85000.00", "12000.00", "Storage", "Additional storage capacity via on-ramp to support data growth. On-ramp ref: OR-2026-015"}
        };

        for (String[] item : demoItems) {
            String name = item[0];
            String description = item[1];
            FundingSource source = FundingSource.fromString(item[2]);
            BigDecimal capAmount = new BigDecimal(item[3]);
            BigDecimal omAmount = new BigDecimal(item[4]);
            String categoryName = item[5];
            String comments = item.length > 6 ? item[6] : null;

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
                fundingItem.setComments(comments);
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
     * Includes both discrete (standalone) spending items and items that will be 
     * linked to procurement items later.
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
        Money gfMoney = fyMonies.stream()
            .filter(m -> "GF".equals(m.getCode()))
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

        // DISCRETE Spending Items (not linked to procurement)
        // These are small purchases, recurring expenses, or items that don't require formal procurement
        // {name, description, amount, status, categoryName, vendor, reference, moneyCode, capAmount, omAmount}
        String[][] discreteItems = {
            // Monthly cloud service payments - recurring expenses
            {"AWS Monthly Services - January", "AWS EC2, S3, and RDS services for January 2026", "8500.00", "PAID", 
             "Cloud Services", "Amazon Web Services", "INV-AWS-JAN26", "OA", "0.00", "8500.00"},
            {"AWS Monthly Services - February", "AWS EC2, S3, and RDS services for February 2026", "9200.00", "PAID", 
             "Cloud Services", "Amazon Web Services", "INV-AWS-FEB26", "OA", "0.00", "9200.00"},
            {"Azure Monthly Services", "Azure compute and storage - January 2026", "6800.00", "PAID", 
             "Cloud Services", "Microsoft Azure", "INV-AZ-JAN26", "OA", "0.00", "6800.00"},
            {"GCP ML Credits", "Google Cloud ML Platform credits - Q1", "12500.00", "COMMITTED", 
             "Cloud Services", "Google Cloud", "INV-GCP-Q1-26", "WCF", "0.00", "12500.00"},
            
            // Office supplies and small purchases
            {"Office Supplies Q1", "General office supplies and consumables for Q1", "3500.00", "PAID", 
             "Small Procurement", "Staples", "INV-STP-JAN26", "AB", "0.00", "3500.00"},
            {"Coffee & Refreshments", "Kitchen supplies and refreshments for Q1", "850.00", "PAID", 
             "Small Procurement", "Costco Business", "INV-COST-Q126", "AB", "0.00", "850.00"},
            {"Printer Supplies", "Toner cartridges and paper for printers", "1200.00", "APPROVED", 
             "Small Procurement", "Staples", "PO-PRNT-001", "AB", "0.00", "1200.00"},
            
            // Software subscriptions (usually don't need procurement)
            {"Slack Enterprise License", "Annual Slack Enterprise subscription - 150 users", "21600.00", "COMMITTED", 
             "Software Licenses", "Slack Technologies", "INV-SLACK-2026", "AB", "0.00", "21600.00"},
            {"GitHub Enterprise", "GitHub Enterprise Cloud - annual subscription", "18000.00", "COMMITTED", 
             "Software Licenses", "GitHub Inc", "INV-GH-2026", "AB", "0.00", "18000.00"},
            {"JetBrains All Products Pack", "IDE licenses for development team - 25 users", "7500.00", "APPROVED", 
             "Software Licenses", "JetBrains", "INV-JB-2026", "AB", "0.00", "7500.00"},
            {"Zoom Business Licenses", "Video conferencing - 50 licenses", "4800.00", "PAID", 
             "Software Licenses", "Zoom Communications", "INV-ZOOM-2026", "OA", "0.00", "4800.00"},
            
            // Professional services and contractors (often discrete purchases)
            {"Tax Advisory Services", "Annual tax consulting and advisory", "8500.00", "APPROVED", 
             "Professional Services", "KPMG Canada", "SO-TAX-2026", "OA", "0.00", "8500.00"},
            {"Legal Review Services", "Contract review and legal consultation", "5200.00", "PAID", 
             "Professional Services", "Norton Rose Fulbright", "INV-NRF-JAN26", "OA", "0.00", "5200.00"},
            {"Data Center Cabling", "Network cabling services for rack expansion", "3800.00", "COMMITTED", 
             "Contractors", "DataCom Services", "WO-CABLE-001", "AB", "3800.00", "0.00"},
            
            // Training and development
            {"AWS Certification Training", "AWS Solutions Architect training - 5 staff", "4500.00", "APPROVED", 
             "Professional Services", "AWS Training", "TR-AWS-2026", "OA", "0.00", "4500.00"},
            {"Kubernetes Workshop", "On-site K8s training workshop - 2 days", "8000.00", "COMMITTED", 
             "Professional Services", "Cloud Native Computing", "TR-K8S-2026", "OA", "0.00", "8000.00"},
            
            // Security services
            {"Annual Security Audit", "Compliance audit and security assessment", "35000.00", "APPROVED", 
             "Security", "CrowdStrike", "SO-SEC-2026", "OA", "0.00", "35000.00"},
            {"Penetration Testing", "Quarterly pen testing service - Q1", "12000.00", "COMMITTED", 
             "Security", "Rapid7", "SO-PEN-Q126", "OA", "0.00", "12000.00"},
            
            // Research and lab supplies
            {"Research Consumables", "Lab consumables and supplies for Q1", "2800.00", "PAID", 
             "Research Equipment", "Fisher Scientific", "INV-FISH-Q126", "GF", "0.00", "2800.00"},
            {"Calibration Services", "Annual equipment calibration", "4200.00", "APPROVED", 
             "Research Equipment", "Keysight Services", "SO-CAL-2026", "GF", "0.00", "4200.00"},
            
            // Data services
            {"Snowflake Credits", "Snowflake data warehouse compute credits", "15000.00", "COMMITTED", 
             "Data Services", "Snowflake Inc", "INV-SF-Q126", "OA", "0.00", "15000.00"},
            {"Datadog Monitoring", "Infrastructure monitoring - annual", "9600.00", "APPROVED", 
             "Data Services", "Datadog Inc", "INV-DD-2026", "OA", "0.00", "9600.00"}
        };

        // Create discrete spending items
        for (String[] item : discreteItems) {
            createSpendingItem(item, demoFY, categoryMap, fyMonies, abMoney, null);
        }

        logger.info("Demo discrete spending items initialized for FY: " + demoFY.getName());
    }
    
    /**
     * Create spending items linked to procurement items.
     * Called after procurement items are created.
     *
     * @param demoFY the Demo fiscal year
     */
    private void initializeProcurementLinkedSpendingItems(FiscalYear demoFY) {
        // Get all money types for this fiscal year
        List<Money> fyMonies = moneyRepository.findByFiscalYearId(demoFY.getId());
        if (fyMonies.isEmpty()) {
            logger.warning("No money types found for Demo FY, skipping procurement-linked spending items");
            return;
        }

        Money abMoney = fyMonies.stream()
            .filter(m -> "AB".equals(m.getCode()))
            .findFirst()
            .orElse(fyMonies.get(0));

        // Get categories
        List<Category> categories = categoryRepository.findByFiscalYearIdOrderByDisplayOrderAscNameAsc(demoFY.getId());
        java.util.Map<String, Category> categoryMap = categories.stream()
            .collect(java.util.stream.Collectors.toMap(Category::getName, c -> c, (a, b) -> a));

        // Get procurement items and create linked spending items
        List<ProcurementItem> procurementItems = procurementItemRepository.findByFiscalYearIdAndActiveTrueOrderByNameAsc(demoFY.getId());
        
        // PROCUREMENT-LINKED Spending Items
        // These represent actual spending from completed/in-progress procurement activities
        // Note: Final spending amounts may differ from quotes due to negotiations, partial deliveries, etc.
        for (ProcurementItem procItem : procurementItems) {
            // Only create spending items for procurement items that have progressed sufficiently
            if (procItem.getStatus() == ProcurementItem.Status.NOT_STARTED ||
                procItem.getStatus() == ProcurementItem.Status.QUOTE ||
                procItem.getStatus() == ProcurementItem.Status.CANCELLED) {
                continue;
            }
            
            // Check if spending item for this procurement already exists
            String spendingName = procItem.getName() + " (Procurement)";
            if (spendingItemRepository.existsByNameAndFiscalYear(spendingName, demoFY)) {
                logger.info("Procurement-linked spending item for '" + procItem.getName() + "' already exists, skipping");
                continue;
            }
            
            // Get the selected quote amount (or estimate from quotes)
            BigDecimal spendingAmount = getSpendingAmountForProcurement(procItem);
            if (spendingAmount == null || spendingAmount.compareTo(BigDecimal.ZERO) <= 0) {
                continue; // Skip if no valid amount
            }
            
            // Determine status based on procurement status
            SpendingItem.Status spendingStatus;
            switch (procItem.getStatus()) {
                case COMPLETED:
                    spendingStatus = SpendingItem.Status.PAID;
                    break;
                case PO_ISSUED:
                    spendingStatus = SpendingItem.Status.COMMITTED;
                    break;
                case APPROVED:
                    spendingStatus = SpendingItem.Status.APPROVED;
                    break;
                default:
                    spendingStatus = SpendingItem.Status.PENDING;
            }
            
            // Determine category based on procurement name/description
            String categoryName = determineCategoryForProcurement(procItem, categoryMap);
            Category category = categoryMap.get(categoryName);
            if (category == null) {
                category = categoryMap.values().stream().findFirst().orElse(null);
            }
            
            if (category == null) {
                logger.warning("No category found for procurement item: " + procItem.getName());
                continue;
            }
            
            // Determine if CAP or OM based on the item
            boolean isCapital = isCapitalProcurement(procItem);
            BigDecimal capAmount = isCapital ? spendingAmount : BigDecimal.ZERO;
            BigDecimal omAmount = isCapital ? BigDecimal.ZERO : spendingAmount;
            
            try {
                String description = "Spending from procurement: " + procItem.getDescription();
                SpendingItem spendingItem = new SpendingItem(spendingName, description, spendingAmount, 
                    spendingStatus, category, demoFY);
                
                // Link to procurement item
                spendingItem.setProcurementItem(procItem);
                
                // Set vendor from preferred vendor or selected quote
                String vendor = procItem.getPreferredVendor();
                if (vendor == null || vendor.isEmpty()) {
                    // Try to get vendor from selected quote
                    ProcurementQuote selectedQuote = procurementQuoteRepository
                        .findByProcurementItemIdAndActiveTrueOrderByVendorNameAsc(procItem.getId())
                        .stream()
                        .filter(ProcurementQuote::getSelected)
                        .findFirst()
                        .orElse(null);
                    if (selectedQuote != null) {
                        vendor = selectedQuote.getVendorName();
                    }
                }
                spendingItem.setVendor(vendor);
                spendingItem.setReferenceNumber(procItem.getPurchaseOrder() != null ? 
                    procItem.getPurchaseOrder() : procItem.getPurchaseRequisition());
                
                spendingItem = spendingItemRepository.save(spendingItem);
                
                // Create money allocation
                SpendingMoneyAllocation allocation = new SpendingMoneyAllocation(spendingItem, abMoney, capAmount, omAmount);
                spendingItem.addMoneyAllocation(allocation);
                spendingItemRepository.save(spendingItem);
                
                logger.info("Created procurement-linked spending item: " + spendingName + 
                           " [Linked to: " + procItem.getPurchaseRequisition() + "]" +
                           " (CAP: $" + capAmount + ", OM: $" + omAmount + ")");
            } catch (Exception e) {
                logger.warning(() -> "Failed to create procurement-linked spending item for '" + 
                    procItem.getName() + "': " + e.getMessage());
            }
        }
        
        logger.info("Procurement-linked spending items initialized for FY: " + demoFY.getName());
    }
    
    /**
     * Helper method to create a spending item.
     */
    private void createSpendingItem(String[] item, FiscalYear demoFY, 
            java.util.Map<String, Category> categoryMap, List<Money> fyMonies, 
            Money defaultMoney, ProcurementItem procurementItem) {
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
            return;
        }

        // Find the category
        Category category = categoryMap.get(categoryName);
        if (category == null) {
            logger.warning("Category '" + categoryName + "' not found for spending item '" + name + "', skipping");
            return;
        }

        // Find the money type
        Money money = fyMonies.stream()
            .filter(m -> moneyCode.equals(m.getCode()))
            .findFirst()
            .orElse(defaultMoney);

        try {
            SpendingItem spendingItem = new SpendingItem(name, description, amount, status, category, demoFY);
            spendingItem.setVendor(vendor);
            spendingItem.setReferenceNumber(reference);
            if (procurementItem != null) {
                spendingItem.setProcurementItem(procurementItem);
            }
            spendingItem = spendingItemRepository.save(spendingItem);

            // Create money allocation with CAP and OM values
            SpendingMoneyAllocation allocation = new SpendingMoneyAllocation(spendingItem, money, capAmount, omAmount);
            spendingItem.addMoneyAllocation(allocation);
            spendingItemRepository.save(spendingItem);

            String typeInfo = procurementItem != null ? " [Procurement-linked]" : " [Discrete]";
            logger.info("Created demo spending item: " + name + typeInfo + " (" + categoryName + ", " + moneyCode + 
                       " CAP: $" + capAmount + ", OM: $" + omAmount + ")");
        } catch (Exception e) {
            logger.warning(() -> "Failed to create demo spending item '" + name + "': " + e.getMessage());
        }
    }
    
    /**
     * Determine the spending amount for a procurement item.
     * Uses selected quote, or estimates based on available quotes.
     * Note: Final spending may differ slightly from quote amounts.
     */
    private BigDecimal getSpendingAmountForProcurement(ProcurementItem procItem) {
        List<ProcurementQuote> quotes = procurementQuoteRepository
            .findByProcurementItemIdAndActiveTrueOrderByAmountAsc(procItem.getId());
        
        if (quotes.isEmpty()) {
            return null;
        }
        
        // Find selected quote
        ProcurementQuote selectedQuote = quotes.stream()
            .filter(ProcurementQuote::getSelected)
            .findFirst()
            .orElse(null);
        
        if (selectedQuote != null) {
            // Apply a small variance to show that spending doesn't always match quote exactly
            // (e.g., due to shipping, installation, taxes, or negotiated discounts)
            BigDecimal quoteAmount = selectedQuote.getAmount();
            double variance = 0.95 + (Math.random() * 0.10); // 95% to 105% of quote
            return quoteAmount.multiply(new BigDecimal(String.format("%.4f", variance)))
                .setScale(2, java.math.RoundingMode.HALF_UP);
        }
        
        // No selected quote - use average of available quotes with variance
        if (!quotes.isEmpty()) {
            BigDecimal avgAmount = quotes.stream()
                .map(ProcurementQuote::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(new BigDecimal(quotes.size()), 2, java.math.RoundingMode.HALF_UP);
            return avgAmount;
        }
        
        return null;
    }
    
    /**
     * Determine the category for a procurement item based on its name/description.
     */
    private String determineCategoryForProcurement(ProcurementItem procItem, 
            java.util.Map<String, Category> categoryMap) {
        String nameLower = procItem.getName().toLowerCase();
        String descLower = procItem.getDescription() != null ? procItem.getDescription().toLowerCase() : "";
        String combined = nameLower + " " + descLower;
        
        if (combined.contains("gpu") || combined.contains("nvidia") || combined.contains("graphics")) {
            return "GPUs";
        }
        if (combined.contains("server") || combined.contains("compute") || combined.contains("switch") ||
            combined.contains("network") || combined.contains("cisco")) {
            return "Compute";
        }
        if (combined.contains("storage") || combined.contains("netapp") || combined.contains("backup")) {
            return "Storage";
        }
        if (combined.contains("license") || combined.contains("software") || combined.contains("autodesk")) {
            return "Software Licenses";
        }
        if (combined.contains("cloud") || combined.contains("aws") || combined.contains("azure") ||
            combined.contains("gcp") || combined.contains("ibm cloud")) {
            return "Cloud Services";
        }
        if (combined.contains("security") || combined.contains("audit") || combined.contains("penetration")) {
            return "Security";
        }
        if (combined.contains("research") || combined.contains("lab") || combined.contains("equipment") ||
            combined.contains("instrument") || combined.contains("oscilloscope")) {
            return "Research Equipment";
        }
        if (combined.contains("consulting") || combined.contains("training") || combined.contains("professional")) {
            return "Professional Services";
        }
        if (combined.contains("contractor") || combined.contains("staffing")) {
            return "Contractors";
        }
        if (combined.contains("furniture") || combined.contains("desk") || combined.contains("chair") ||
            combined.contains("printer") || combined.contains("laptop") || combined.contains("thinkpad")) {
            return "Small Procurement";
        }
        
        // Default to first available category
        return categoryMap.keySet().stream().findFirst().orElse("Compute");
    }
    
    /**
     * Determine if a procurement item is capital (CAP) or operational (OM).
     */
    private boolean isCapitalProcurement(ProcurementItem procItem) {
        String nameLower = procItem.getName().toLowerCase();
        String descLower = procItem.getDescription() != null ? procItem.getDescription().toLowerCase() : "";
        String combined = nameLower + " " + descLower;
        
        // Capital items: hardware, equipment, infrastructure
        if (combined.contains("server") || combined.contains("gpu") || combined.contains("storage") ||
            combined.contains("switch") || combined.contains("equipment") || combined.contains("hardware") ||
            combined.contains("laptop") || combined.contains("computer") || combined.contains("infrastructure")) {
            return true;
        }
        
        // Operational items: licenses, services, cloud, subscriptions
        if (combined.contains("license") || combined.contains("service") || combined.contains("cloud") ||
            combined.contains("subscription") || combined.contains("consulting") || combined.contains("training")) {
            return false;
        }
        
        // Default to capital for equipment, operational for everything else
        return combined.contains("purchase") || combined.contains("equipment");
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
             ProcurementItem.Status.GOODS_RECEIVED, Currency.CAD, null},
            {"PR-2025-002", "PO-2025-002", "NVIDIA A100 GPUs", 
             "4x NVIDIA A100 80GB GPUs for machine learning workloads",
             ProcurementItem.Status.ACKNOWLEDGED_BY_PROCUREMENT, Currency.USD, new BigDecimal("1.360000")},
            {"PR-2025-003", null, "Cisco Network Switches", 
             "Cisco Catalyst 9300 switches for network infrastructure upgrade",
             ProcurementItem.Status.PACKAGE_SENT_TO_PROCUREMENT, Currency.CAD, null},
            {"PR-2025-004", null, "NetApp Storage Array", 
             "NetApp AFF A400 storage system with 50TB capacity for data center",
             ProcurementItem.Status.SAM_ACKNOWLEDGEMENT_RECEIVED, Currency.CAD, null},
            {"PR-2025-005", null, "HP LaserJet Printers", 
             "5x HP LaserJet Enterprise printers for office deployment",
             ProcurementItem.Status.QUOTE, Currency.CAD, null},
            {"PR-2025-006", null, "IBM Cloud Credits", 
             "Annual IBM Cloud compute and storage credits",
             ProcurementItem.Status.SAM_ACKNOWLEDGEMENT_RECEIVED, Currency.USD, new BigDecimal("1.360000")},
            {"PR-2025-007", "PO-2025-003", "Lenovo ThinkPads", 
             "25x Lenovo ThinkPad X1 Carbon laptops for staff refresh",
             ProcurementItem.Status.FULL_INVOICE_SIGNED, Currency.CAD, null},
            {"PR-2025-008", null, "Autodesk Licenses", 
             "50x Autodesk AutoCAD licenses - 3-year subscription",
             ProcurementItem.Status.NOT_STARTED, Currency.USD, new BigDecimal("1.360000")},
            {"PR-2025-009", null, "Laboratory Equipment", 
             "Oscilloscopes and signal generators for research laboratory",
             ProcurementItem.Status.QUOTE, Currency.EUR, new BigDecimal("1.470000")},
            {"PR-2025-010", "PO-2025-004", "AWS Reserved Instances", 
             "3-year reserved capacity for EC2 and RDS instances",
             ProcurementItem.Status.ACKNOWLEDGED_BY_PROCUREMENT, Currency.USD, new BigDecimal("1.360000")},
            {"PR-2025-011", null, "Office Furniture", 
             "Standing desks and ergonomic chairs for new office space",
             ProcurementItem.Status.PACKAGE_SENT_TO_PROCUREMENT, Currency.CAD, null},
            {"PR-2025-012", null, "Security Assessment Services", 
             "Annual penetration testing and security audit services",
             ProcurementItem.Status.SAM_ACKNOWLEDGEMENT_RECEIVED, Currency.CAD, null},
            {"PR-2025-013", null, "UK Training Program", 
             "Staff training program with UK-based vendor",
             ProcurementItem.Status.NOT_STARTED, Currency.GBP, new BigDecimal("1.680000")},
            {"PR-2025-014", null, "Video Conferencing System", 
             "Cisco Webex Board 85 for main conference room",
             ProcurementItem.Status.SAM_ACKNOWLEDGEMENT_RECEIVED, Currency.CAD, null},
            {"PR-2025-015", null, "European Instrumentation", 
             "Specialized instrumentation from European manufacturer",
             ProcurementItem.Status.QUOTE, Currency.EUR, new BigDecimal("1.470000")}
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
                ProcurementItem savedItem = procurementItemRepository.save(procurementItem);

                // Add demo quotes for certain procurement items
                addDemoQuotes(savedItem);

                // Add demo events for procurement items
                addDemoProcurementEvents(savedItem);

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
     * Add demo quotes to a procurement item based on its status.
     *
     * @param procurementItem the procurement item to add quotes to
     */
    private void addDemoQuotes(ProcurementItem procurementItem) {
        // Only add quotes to items that would have quotes (SAM_ACKNOWLEDGEMENT_RECEIVED and beyond)
        ProcurementItem.Status status = procurementItem.getStatus();
        if (status == ProcurementItem.Status.NOT_STARTED || status == ProcurementItem.Status.QUOTE ||
            status == ProcurementItem.Status.SAM_ACKNOWLEDGEMENT_REQUESTED) {
            return;
        }

        // Demo quotes data based on the procurement item
        String pr = procurementItem.getPurchaseRequisition();
        Currency currency = procurementItem.getCurrency();

        // Generate different quotes based on the PR
        Object[][] quotes;
        switch (pr) {
            case "PR-2025-001": // Dell PowerEdge Servers - COMPLETED
                quotes = new Object[][] {
                    {"Dell Technologies", "Dell Sales Team", "Q-DELL-2025-001", new BigDecimal("43500.00"), 
                     LocalDate.of(2025, 9, 15), LocalDate.of(2025, 12, 15), "3-year warranty included. ProSupport Plus available.", 
                     ProcurementQuote.Status.SELECTED, true},
                    {"CDW Canada", "Enterprise Team", "CDW-Q-789456", new BigDecimal("45200.00"), 
                     LocalDate.of(2025, 9, 18), LocalDate.of(2025, 12, 18), "Standard 1-year warranty. Additional support available.", 
                     ProcurementQuote.Status.REJECTED, false},
                    {"Insight Canada", "Server Solutions", "INS-2025-4521", new BigDecimal("44100.00"), 
                     LocalDate.of(2025, 9, 20), LocalDate.of(2025, 12, 20), "2-year warranty, on-site support included.", 
                     ProcurementQuote.Status.REJECTED, false}
                };
                break;
            case "PR-2025-002": // NVIDIA GPUs - PO_ISSUED
                quotes = new Object[][] {
                    {"NVIDIA Corp", "Partner Sales", "NV-Q-2025-8521", new BigDecimal("51200.00"), 
                     LocalDate.of(2025, 10, 1), LocalDate.of(2026, 1, 1), "Direct from manufacturer. NVIDIA Enterprise Support.", 
                     ProcurementQuote.Status.SELECTED, true},
                    {"Lambda Labs", "GPU Sales", "LAMBDA-2025-102", new BigDecimal("54000.00"), 
                     LocalDate.of(2025, 10, 5), LocalDate.of(2026, 1, 5), "Includes installation support and configuration.", 
                     ProcurementQuote.Status.REJECTED, false}
                };
                break;
            case "PR-2025-003": // Cisco Network Switches - APPROVED
                quotes = new Object[][] {
                    {"Cisco Systems", "Enterprise Networks", "CISCO-Q-2025-112", new BigDecimal("31500.00"), 
                     LocalDate.of(2025, 11, 10), LocalDate.of(2026, 2, 10), "Direct from Cisco with SmartNet support.", 
                     ProcurementQuote.Status.SELECTED, true},
                    {"SHI International", "Networking Team", "SHI-NET-4521", new BigDecimal("32800.00"), 
                     LocalDate.of(2025, 11, 12), LocalDate.of(2026, 2, 12), "Authorized Cisco partner. Competitive pricing.", 
                     ProcurementQuote.Status.REJECTED, false},
                    {"CDW Canada", "Network Solutions", "CDW-NET-7891", new BigDecimal("33200.00"), 
                     LocalDate.of(2025, 11, 15), LocalDate.of(2026, 2, 15), "Bundle discount available for multiple units.", 
                     ProcurementQuote.Status.REJECTED, false}
                };
                break;
            case "PR-2025-004": // NetApp Storage - QUOTES_RECEIVED
                quotes = new Object[][] {
                    {"NetApp Inc", "Storage Solutions", "NTA-Q-2025-741", new BigDecimal("76500.00"), 
                     LocalDate.of(2026, 1, 10), LocalDate.of(2026, 4, 10), "5-year support included. ONTAP software license.", 
                     ProcurementQuote.Status.UNDER_REVIEW, false},
                    {"Pure Storage", "Enterprise Team", "PURE-2025-3321", new BigDecimal("79000.00"), 
                     LocalDate.of(2026, 1, 12), LocalDate.of(2026, 4, 12), "All-flash solution. Evergreen subscription.", 
                     ProcurementQuote.Status.PENDING, false},
                    {"Dell EMC", "Storage Division", "DEMC-Q-8852", new BigDecimal("74200.00"), 
                     LocalDate.of(2026, 1, 15), LocalDate.of(2026, 4, 15), "Competitive pricing. PowerStore technology.", 
                     ProcurementQuote.Status.PENDING, false}
                };
                break;
            case "PR-2025-006": // IBM Cloud Credits - UNDER_REVIEW
                quotes = new Object[][] {
                    {"IBM Corporation", "Cloud Sales", "IBM-CLD-2025-891", new BigDecimal("48000.00"), 
                     LocalDate.of(2025, 12, 5), LocalDate.of(2026, 3, 5), "Enterprise tier pricing. Priority support.", 
                     ProcurementQuote.Status.UNDER_REVIEW, false},
                    {"IBM Business Partner", "Cloud Services", "IBMBP-CLD-445", new BigDecimal("52000.00"), 
                     LocalDate.of(2025, 12, 8), LocalDate.of(2026, 3, 8), "Partner pricing with additional services.", 
                     ProcurementQuote.Status.PENDING, false}
                };
                break;
            case "PR-2025-007": // Lenovo ThinkPads - COMPLETED
                quotes = new Object[][] {
                    {"Lenovo Direct", "Enterprise Sales", "LEN-Q-2025-331", new BigDecimal("62500.00"), 
                     LocalDate.of(2025, 8, 1), LocalDate.of(2025, 11, 1), "Direct enterprise pricing. 3-year warranty.", 
                     ProcurementQuote.Status.SELECTED, true},
                    {"CDW Canada", "End User Computing", "CDW-EUC-5521", new BigDecimal("64800.00"), 
                     LocalDate.of(2025, 8, 5), LocalDate.of(2025, 11, 5), "Volume discount applied.", 
                     ProcurementQuote.Status.REJECTED, false}
                };
                break;
            case "PR-2025-010": // AWS Reserved Instances - PO_ISSUED
                quotes = new Object[][] {
                    {"Amazon Web Services", "Enterprise Support", "AWS-RI-2025-112", new BigDecimal("185000.00"), 
                     LocalDate.of(2025, 10, 15), LocalDate.of(2026, 1, 15), "3-year reserved instance pricing. Significant savings.", 
                     ProcurementQuote.Status.SELECTED, true}
                };
                break;
            case "PR-2025-011": // Office Furniture - APPROVED
                quotes = new Object[][] {
                    {"Steelcase Canada", "Furniture Solutions", "SC-Q-2025-221", new BigDecimal("28500.00"), 
                     LocalDate.of(2025, 12, 1), LocalDate.of(2026, 3, 1), "Ergonomic certified. 10-year warranty on frames.", 
                     ProcurementQuote.Status.SELECTED, true},
                    {"Herman Miller", "Workplace Design", "HM-WORK-4412", new BigDecimal("32000.00"), 
                     LocalDate.of(2025, 12, 3), LocalDate.of(2026, 3, 3), "Premium Aeron chairs included.", 
                     ProcurementQuote.Status.REJECTED, false},
                    {"Staples Business", "Furniture Team", "STB-FURN-789", new BigDecimal("24500.00"), 
                     LocalDate.of(2025, 12, 5), LocalDate.of(2026, 3, 5), "Budget option. Standard warranty.", 
                     ProcurementQuote.Status.REJECTED, false}
                };
                break;
            case "PR-2025-012": // Security Assessment - QUOTES_RECEIVED
                quotes = new Object[][] {
                    {"CrowdStrike", "Professional Services", "CS-Q-2025-441", new BigDecimal("32000.00"), 
                     LocalDate.of(2026, 1, 5), LocalDate.of(2026, 3, 5), "Comprehensive pen testing. Falcon platform included.", 
                     ProcurementQuote.Status.UNDER_REVIEW, false},
                    {"Deloitte Cyber", "Security Practice", "DL-SEC-2025-112", new BigDecimal("38500.00"), 
                     LocalDate.of(2026, 1, 8), LocalDate.of(2026, 3, 8), "Full assessment with remediation support.", 
                     ProcurementQuote.Status.PENDING, false}
                };
                break;
            case "PR-2025-014": // Video Conferencing - UNDER_REVIEW
                quotes = new Object[][] {
                    {"Cisco Webex", "Collaboration Sales", "WEBEX-Q-2025-112", new BigDecimal("18500.00"), 
                     LocalDate.of(2025, 12, 10), LocalDate.of(2026, 3, 10), "Webex Board 85 with Room OS. Installation included.", 
                     ProcurementQuote.Status.UNDER_REVIEW, false},
                    {"Zoom Rooms", "Enterprise Sales", "ZOOM-RM-2025-88", new BigDecimal("16200.00"), 
                     LocalDate.of(2025, 12, 12), LocalDate.of(2026, 3, 12), "Zoom Rooms hardware. 3-year subscription.", 
                     ProcurementQuote.Status.PENDING, false},
                    {"Microsoft Teams", "Surface Hub", "MS-TMS-2025-441", new BigDecimal("21000.00"), 
                     LocalDate.of(2025, 12, 15), LocalDate.of(2026, 3, 15), "Surface Hub 85 with Teams Rooms license.", 
                     ProcurementQuote.Status.PENDING, false}
                };
                break;
            default:
                return; // No quotes for other items
        }

        // Create the quotes
        for (Object[] quoteData : quotes) {
            try {
                ProcurementQuote quote = new ProcurementQuote(
                    (String) quoteData[0], // vendorName
                    (BigDecimal) quoteData[3], // amount
                    currency,
                    procurementItem
                );
                quote.setVendorContact((String) quoteData[1]);
                quote.setQuoteReference((String) quoteData[2]);
                quote.setReceivedDate((LocalDate) quoteData[4]);
                quote.setExpiryDate((LocalDate) quoteData[5]);
                quote.setNotes((String) quoteData[6]);
                quote.setStatus((ProcurementQuote.Status) quoteData[7]);
                quote.setSelected((Boolean) quoteData[8]);
                
                ProcurementQuote savedQuote = procurementQuoteRepository.save(quote);
                logger.info("Created demo quote from " + quote.getVendorName() + " for " + pr);
                
                // Add demo files for this quote
                addDemoQuoteFiles(savedQuote);
            } catch (Exception e) {
                logger.warning(() -> "Failed to create demo quote for " + pr + ": " + e.getMessage());
            }
        }
    }

    /**
     * Add demo files to a procurement quote.
     * Creates fake PDF quote documents for demonstration purposes.
     *
     * @param quote the quote to add files to
     */
    private void addDemoQuoteFiles(ProcurementQuote quote) {
        String vendorName = quote.getVendorName().replaceAll("\\s+", "_");
        String quoteRef = quote.getQuoteReference() != null ? quote.getQuoteReference() : "Quote";
        
        // Create a simple fake PDF content (PDF header + minimal content)
        // This is a minimal valid PDF that most readers will accept
        String pdfContent = "%PDF-1.4\n" +
            "1 0 obj\n" +
            "<< /Type /Catalog /Pages 2 0 R >>\n" +
            "endobj\n" +
            "2 0 obj\n" +
            "<< /Type /Pages /Kids [3 0 R] /Count 1 >>\n" +
            "endobj\n" +
            "3 0 obj\n" +
            "<< /Type /Page /Parent 2 0 R /MediaBox [0 0 612 792] /Contents 4 0 R >>\n" +
            "endobj\n" +
            "4 0 obj\n" +
            "<< /Length 100 >>\n" +
            "stream\n" +
            "BT\n" +
            "/F1 24 Tf\n" +
            "100 700 Td\n" +
            "(Quote from " + vendorName + ") Tj\n" +
            "ET\n" +
            "endstream\n" +
            "endobj\n" +
            "xref\n" +
            "0 5\n" +
            "0000000000 65535 f \n" +
            "0000000009 00000 n \n" +
            "0000000058 00000 n \n" +
            "0000000115 00000 n \n" +
            "0000000210 00000 n \n" +
            "trailer\n" +
            "<< /Size 5 /Root 1 0 R >>\n" +
            "startxref\n" +
            "362\n" +
            "%%EOF";
        
        byte[] pdfBytes = pdfContent.getBytes(java.nio.charset.StandardCharsets.UTF_8);

        try {
            // Create quote document file
            ProcurementQuoteFile quoteFile = new ProcurementQuoteFile();
            quoteFile.setQuote(quote);
            quoteFile.setFileName(quoteRef + "_Quote.pdf");
            quoteFile.setContentType("application/pdf");
            quoteFile.setContent(pdfBytes);
            quoteFile.setFileSize((long) pdfBytes.length);
            quoteFile.setDescription("Official quote document from " + quote.getVendorName());
            procurementQuoteFileRepository.save(quoteFile);
            
            // Add a terms and conditions document for some quotes
            if (quote.getStatus() == ProcurementQuote.Status.SELECTED || 
                quote.getStatus() == ProcurementQuote.Status.UNDER_REVIEW) {
                ProcurementQuoteFile termsFile = new ProcurementQuoteFile();
                termsFile.setQuote(quote);
                termsFile.setFileName(vendorName + "_Terms_Conditions.pdf");
                termsFile.setContentType("application/pdf");
                termsFile.setContent(pdfBytes);
                termsFile.setFileSize((long) pdfBytes.length);
                termsFile.setDescription("Terms and conditions document");
                procurementQuoteFileRepository.save(termsFile);
            }
            
            logger.info("Created demo files for quote from " + quote.getVendorName());
        } catch (Exception e) {
            logger.warning(() -> "Failed to create demo files for quote: " + e.getMessage());
        }
    }

    /**
     * Add demo procurement events to a procurement item based on its status.
     * Creates a realistic history of events that would lead to the current status.
     *
     * @param procurementItem the procurement item to add events to
     */
    private void addDemoProcurementEvents(ProcurementItem procurementItem) {
        ProcurementItem.Status status = procurementItem.getStatus();
        String pr = procurementItem.getPurchaseRequisition();
        LocalDate baseDate = LocalDate.of(2025, 8, 1);

        try {
            // All items start with a CREATED event
            ProcurementEvent createdEvent = new ProcurementEvent(
                procurementItem,
                ProcurementEvent.EventType.CREATED,
                baseDate,
                "Procurement item created for " + procurementItem.getName()
            );
            createdEvent.setCreatedBy("admin");
            procurementEventRepository.save(createdEvent);

            // Add status-appropriate events
            switch (status) {
                case COMPLETED:
                    // Full lifecycle events
                    addStatusChangeEvent(procurementItem, "DRAFT", "PENDING_QUOTES", baseDate.plusDays(1), "Submitted for quotes");
                    addStatusChangeEvent(procurementItem, "PENDING_QUOTES", "QUOTES_RECEIVED", baseDate.plusDays(14), "3 vendor quotes received");
                    addStatusChangeEvent(procurementItem, "QUOTES_RECEIVED", "UNDER_REVIEW", baseDate.plusDays(15), "Evaluation committee reviewing quotes");
                    addStatusChangeEvent(procurementItem, "UNDER_REVIEW", "APPROVED", baseDate.plusDays(21), "Approved by finance director");
                    addStatusChangeEvent(procurementItem, "APPROVED", "PO_ISSUED", baseDate.plusDays(25), "PO issued to vendor");
                    addStatusChangeEvent(procurementItem, "PO_ISSUED", "COMPLETED", baseDate.plusDays(60), "Items received and verified");
                    addNoteEvent(procurementItem, baseDate.plusDays(45), "Vendor confirmed delivery date");
                    addNoteEvent(procurementItem, baseDate.plusDays(62), "All items passed quality inspection");
                    break;

                case PO_ISSUED:
                    addStatusChangeEvent(procurementItem, "DRAFT", "PENDING_QUOTES", baseDate.plusDays(5), "Sent RFQ to vendors");
                    addStatusChangeEvent(procurementItem, "PENDING_QUOTES", "QUOTES_RECEIVED", baseDate.plusDays(20), "Quotes received from vendors");
                    addStatusChangeEvent(procurementItem, "QUOTES_RECEIVED", "UNDER_REVIEW", baseDate.plusDays(22), "Technical review in progress");
                    addStatusChangeEvent(procurementItem, "UNDER_REVIEW", "APPROVED", baseDate.plusDays(30), "Approved after technical review");
                    addStatusChangeEvent(procurementItem, "APPROVED", "PO_ISSUED", baseDate.plusDays(35), "Purchase order submitted to vendor");
                    addNoteEvent(procurementItem, baseDate.plusDays(40), "Expected delivery in 4 weeks");
                    break;

                case APPROVED:
                    addStatusChangeEvent(procurementItem, "DRAFT", "PENDING_QUOTES", baseDate.plusDays(7), "Requesting quotes from vendors");
                    addStatusChangeEvent(procurementItem, "PENDING_QUOTES", "QUOTES_RECEIVED", baseDate.plusDays(28), "Received quotes");
                    addStatusChangeEvent(procurementItem, "QUOTES_RECEIVED", "UNDER_REVIEW", baseDate.plusDays(30), "Reviewing vendor proposals");
                    addStatusChangeEvent(procurementItem, "UNDER_REVIEW", "APPROVED", baseDate.plusDays(45), "Budget approved, ready for PO");
                    break;

                case UNDER_REVIEW:
                    addStatusChangeEvent(procurementItem, "DRAFT", "PENDING_QUOTES", baseDate.plusDays(10), "Quote requests sent");
                    addStatusChangeEvent(procurementItem, "PENDING_QUOTES", "QUOTES_RECEIVED", baseDate.plusDays(30), "Multiple quotes received");
                    addStatusChangeEvent(procurementItem, "QUOTES_RECEIVED", "UNDER_REVIEW", baseDate.plusDays(35), "Evaluation started");
                    addNoteEvent(procurementItem, baseDate.plusDays(40), "Awaiting clarification from vendor");
                    break;

                case QUOTES_RECEIVED:
                    addStatusChangeEvent(procurementItem, "DRAFT", "PENDING_QUOTES", baseDate.plusDays(5), "RFQ sent to 3 vendors");
                    addStatusChangeEvent(procurementItem, "PENDING_QUOTES", "QUOTES_RECEIVED", baseDate.plusDays(25), "Quotes received, pending review");
                    addNoteEvent(procurementItem, baseDate.plusDays(26), "Need to schedule evaluation meeting");
                    break;

                case PENDING_QUOTES:
                    addStatusChangeEvent(procurementItem, "DRAFT", "PENDING_QUOTES", baseDate.plusDays(3), "Request for quotes submitted");
                    addNoteEvent(procurementItem, baseDate.plusDays(10), "Following up with vendors");
                    break;

                case DRAFT:
                    addNoteEvent(procurementItem, baseDate.plusDays(2), "Requirements being finalized");
                    break;

                case CANCELLED:
                    addStatusChangeEvent(procurementItem, "DRAFT", "PENDING_QUOTES", baseDate.plusDays(5), "Started procurement process");
                    addStatusChangeEvent(procurementItem, "PENDING_QUOTES", "CANCELLED", baseDate.plusDays(15), "Project cancelled due to budget constraints");
                    break;
            }

            logger.info("Created demo procurement events for " + pr);
        } catch (Exception e) {
            logger.warning(() -> "Failed to create demo events for " + pr + ": " + e.getMessage());
        }
    }

    /**
     * Helper to add a status change event.
     */
    private void addStatusChangeEvent(ProcurementItem item, String oldStatus, String newStatus,
            LocalDate eventDate, String comment) {
        ProcurementEvent event = ProcurementEvent.createStatusChangeEvent(
            item, oldStatus, newStatus, comment, "admin"
        );
        event.setEventDate(eventDate);
        procurementEventRepository.save(event);
    }

    /**
     * Helper to add a note event.
     */
    private void addNoteEvent(ProcurementItem item, LocalDate eventDate, String comment) {
        ProcurementEvent event = new ProcurementEvent(
            item,
            ProcurementEvent.EventType.NOTE_ADDED,
            eventDate,
            comment
        );
        event.setCreatedBy("admin");
        procurementEventRepository.save(event);
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
