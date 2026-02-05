<!--
myRC - Comprehensive User Guide
Author: myRC Team
Date: 2026-02-04
Version: 1.0.0
License: MIT
-->

# myRC User Guide

## Table of Contents

1. [Introduction](#1-introduction)
2. [Getting Started](#2-getting-started)
3. [Authentication & Login](#3-authentication--login)
4. [User Management](#4-user-management)
5. [Responsibility Centres (RCs)](#5-responsibility-centres-rcs)
6. [Fiscal Years](#6-fiscal-years)
7. [Funding Items](#7-funding-items)
8. [Spending Items](#8-spending-items)
9. [Procurement Items](#9-procurement-items)
10. [Insights Dashboard](#10-insights-dashboard)
11. [Configuration](#11-configuration)
12. [Permissions & Access Control](#12-permissions--access-control)
13. [Multi-Currency Support](#13-multi-currency-support)
14. [Search & Filtering](#14-search--filtering)
15. [Import & Export](#15-import--export)
16. [Language & Theme Settings](#16-language--theme-settings)
17. [Demo RC](#17-demo-rc)
18. [Keyboard Shortcuts](#18-keyboard-shortcuts)
19. [Troubleshooting](#19-troubleshooting)
20. [Glossary](#20-glossary)

---

## 1. Introduction

### 1.1 What is myRC?

myRC is a comprehensive **Responsibility Centre Management System** designed to help organizations track and manage their financial allocations, expenditures, and procurement activities across different fiscal years. The application provides a centralized platform for:

- **Funding Management**: Track budget allocations from various sources
- **Spending Management**: Monitor expenditures with detailed event tracking
- **Procurement Management**: Manage the full procurement lifecycle from quotes to completion
- **Financial Insights**: Visualize budget utilization and spending patterns
- **Multi-User Access**: Collaborate with team members with role-based permissions

### 1.2 Key Features

| Feature | Description |
|---------|-------------|
| **Multi-Provider Authentication** | Support for local accounts, LDAP, and OAuth2/OIDC |
| **Responsibility Centre Management** | Create and manage multiple RCs with shared access |
| **Fiscal Year Tracking** | Organize data by fiscal years within each RC |
| **Funding & Spending Tracking** | Detailed financial allocation and expenditure management |
| **Procurement Lifecycle** | Full procurement management with quotes, events, and files |
| **Visual Analytics** | Interactive charts and dashboards |
| **Multi-Currency Support** | Track items in multiple currencies with CAD conversion |
| **Bilingual Support** | Full English and French language support |
| **Theme Options** | Light and dark mode themes |

### 1.3 System Requirements

- **Browser**: Modern web browser (Chrome, Firefox, Edge, Safari)
- **Screen Resolution**: 1280x720 or higher recommended
- **JavaScript**: Must be enabled
- **Cookies**: Must be enabled for session management

---

## 2. Getting Started

### 2.1 Accessing the Application

1. Open your web browser
2. Navigate to the myRC application URL provided by your administrator
3. You will see the login page with system status indicators

### 2.2 First-Time Setup

If you're a new user:

1. Click **Register** to create a new account (if self-registration is enabled)
2. Fill in your details:
   - Username (unique identifier)
   - Full Name
   - Email Address
   - Password (minimum 8 characters, must include uppercase, lowercase, number, and special character)
3. Click **Register** to create your account
4. Log in with your new credentials

### 2.3 Understanding the Interface

After logging in, you'll see:

| Element | Description |
|---------|-------------|
| **Header** | Contains user info, language toggle, and theme toggle |
| **Sidebar** | Navigation menu showing RC/FY selection and main sections |
| **Main Content** | The primary workspace area |
| **RC/FY Selection** | Quick context selector for your active RC and Fiscal Year |

![Main Interface](screenshots/main-interface.png)
*The main application interface showing the header, sidebar, and content area*

### 2.4 Navigation Overview

The main navigation sections are:

- **Dashboard**: Summary view of funding items with category grouping
- **Spending**: Expenditure tracking and management
- **Procurement**: Procurement item management with quotes and events
- **Insights**: Visual analytics and charts
- **Summary**: Overview of financial data
- **Configuration**: Settings for categories, money types, and more
- **Permissions**: Access control management (for RC owners)

---

## 3. Authentication & Login

### 3.1 Login Methods

myRC supports multiple authentication methods:

#### Local/App Account
- Standard username and password authentication
- Accounts created through the registration process
- Password management handled within myRC

#### LDAP Authentication
- Enterprise directory integration
- Use your corporate credentials
- Groups and roles synchronized from LDAP

#### OAuth2/OIDC
- Single Sign-On (SSO) support
- Integration with external identity providers
- Click the SSO button on the login page if available

### 3.2 Logging In

1. Navigate to the login page
2. Enter your **Username** or **Email**
3. Enter your **Password**
4. Click **Login**

![Login Page](screenshots/login-page.png)
*The login page with authentication options*

### 3.3 Password Requirements

When creating or changing passwords:
- Minimum 8 characters
- At least one uppercase letter
- At least one lowercase letter
- At least one number
- At least one special character

### 3.4 Account Lockout

After multiple failed login attempts, your account may be temporarily locked. Contact your administrator to unlock your account.

### 3.5 Logging Out

Click the **Logout** button in the header or sidebar to end your session securely.

---

## 4. User Management

### 4.1 Viewing Your Profile

1. Click your username in the header
2. Select **Profile** from the dropdown
3. View your account details

### 4.2 Updating Your Profile

1. Navigate to your profile
2. Click **Edit**
3. Update your information:
   - Full Name
   - Email Address
   - Profile Description
4. Click **Save**

### 4.3 Changing Your Password

1. Go to your profile
2. Click **Change Password**
3. Enter your current password
4. Enter your new password
5. Confirm your new password
6. Click **Change Password**

### 4.4 User Roles

| Role | Permissions |
|------|-------------|
| **USER** | Standard user access, can manage RCs they own or have access to |
| **ADMIN** | Full system access, can manage all users and settings |

---

## 5. Responsibility Centres (RCs)

### 5.1 What is a Responsibility Centre?

A Responsibility Centre (RC) is the primary organizational unit in myRC. It represents a budget center, department, or project that requires financial tracking. Each RC contains:
- One or more Fiscal Years
- Funding Items
- Spending Items
- Procurement Items

### 5.2 Creating a New RC

1. From the RC selection screen, click **Create New RC**
2. Enter the RC details:
   - **Name**: A unique name for the RC (cannot contain: `< > : " / \ | ? *`)
   - **Description**: Optional description of the RC's purpose
3. Click **Create**

You automatically become the **Owner** of any RC you create.

### 5.3 Selecting an RC

1. From the sidebar, click the RC name area
2. The RC selection screen appears
3. Click on the RC you want to work with
4. Select a Fiscal Year within that RC

![RC Selection](screenshots/rc-list.png)
*The Responsibility Centre selection screen*

### 5.4 Editing an RC

Only the RC **Owner** can edit RC details:

1. Navigate to **Configuration** while in the RC
2. Go to the **General** tab
3. Update the RC name or description
4. Click **Save**

### 5.5 Deleting an RC

Only the RC **Owner** can delete an RC:

1. Navigate to **Configuration**
2. Go to the **General** tab
3. Click **Delete RC**
4. Confirm the deletion

> ⚠️ **Warning**: Deleting an RC removes ALL associated data including fiscal years, funding, spending, and procurement items.

---

## 6. Fiscal Years

### 6.1 Understanding Fiscal Years

Fiscal Years organize your financial data into time periods within an RC. Each Fiscal Year contains:
- Configured money types (funding sources)
- Categories for organizing items
- Funding items
- Spending items
- Procurement items

### 6.2 Creating a Fiscal Year

1. Select your RC
2. On the RC detail screen, click **Create New Fiscal Year**
3. Enter the details:
   - **Name**: e.g., "FY 2025-2026" (cannot contain: `< > : " / \ | ? *`)
   - **Description**: Optional description
4. Click **Create**

Default categories and the default money type (AB) are automatically created.

### 6.3 Selecting a Fiscal Year

1. From the sidebar or RC selection screen
2. Click on the desired Fiscal Year
3. The FY name appears in the sidebar header

### 6.4 Managing Fiscal Year Settings

In **Configuration**, you can adjust:
- Display settings (search box, category filter, grouping)
- On-target variance thresholds
- Custom categories and money types

---

## 7. Funding Items

### 7.1 What are Funding Items?

Funding Items represent budget allocations for your fiscal year. They track where your money comes from and how much is allocated to different purposes.

### 7.2 Funding Sources

| Source | Description |
|--------|-------------|
| **Business Plan** | Standard allocation from annual business plan |
| **On-Ramp** | Additional mid-year funding received |
| **Approved Deficit** | Approved deficit spending |

### 7.3 Creating a Funding Item

1. Navigate to **Dashboard**
2. Click **Add Funding Item** or the **+** button
3. Fill in the details:
   - **Name**: Descriptive name for the funding
   - **Description**: Details about the funding
   - **Funding Source**: Select Business Plan, On-Ramp, or Approved Deficit
   - **Category**: Optional category assignment
   - **Comments**: Additional notes
4. Add **Money Allocations**:
   - Select the money type (e.g., AB, OA)
   - Enter CAP (Capital) amount
   - Enter OM (Operations & Maintenance) amount
5. Click **Save**

### 7.4 Viewing Funding Items

The Dashboard displays funding items with:
- **List View**: All items in a sortable list
- **Grouped View**: Items grouped by category
- **Summary Cards**: Totals by money type

![Dashboard Overview](screenshots/dashboard-overview.png)
*The Dashboard showing funding items grouped by category*

### 7.5 Editing a Funding Item

1. Click on the funding item
2. Click **Edit**
3. Modify the details
4. Click **Save**

### 7.6 Deleting a Funding Item

1. Click on the funding item
2. Click **Delete**
3. Confirm the deletion

### 7.7 Understanding Money Allocations

Each funding item can have multiple money allocations:
- **CAP (Capital)**: Capital expenditure funding (hardware, equipment)
- **OM (Operations & Maintenance)**: Operating expense funding (services, licenses)
- **Money Type**: The funding source type (AB, OA, WCF, etc.)

---

## 8. Spending Items

### 8.1 What are Spending Items?

Spending Items track actual expenditures within a fiscal year. They can be:
- **Discrete**: Standalone expenses not linked to procurement
- **Procurement-Linked**: Expenses tied to a procurement item

### 8.2 Spending Status Workflow

```
DRAFT → PENDING → APPROVED → COMMITTED → PAID
                                    ↓
                              CANCELLED
```

| Status | Description |
|--------|-------------|
| **DRAFT** | Item created, not yet submitted |
| **PENDING** | Submitted for approval |
| **APPROVED** | Approved for spending |
| **COMMITTED** | Funds committed (PO issued) |
| **PAID** | Payment completed |
| **CANCELLED** | Item cancelled |

### 8.3 Creating a Spending Item

1. Navigate to **Spending**
2. Click **Add Spending Item** or the **+** button
3. Fill in the details:
   - **Name**: Descriptive name
   - **Description**: Details about the expense
   - **Amount**: Total amount (or leave empty for ECO estimate)
   - **ECO Amount**: Estimated amount before final quotes
   - **Status**: Current status
   - **Category**: Spending category
   - **Vendor**: Vendor name
   - **Reference Number**: PO, invoice, or reference number
   - **Currency**: If not CAD, select currency and enter exchange rate
4. Add **Money Allocations**:
   - Select money type
   - Enter CAP and OM amounts
5. Click **Save**

### 8.4 Spending Events

Spending items support detailed event tracking:

| Event Type | Description |
|------------|-------------|
| **PENDING** | Waiting to be processed |
| **ECO_REQUESTED** | ECO approval requested |
| **ECO_RECEIVED** | ECO approval received |
| **EXTERNAL_APPROVAL_REQUESTED** | External approval requested |
| **EXTERNAL_APPROVAL_RECEIVED** | External approval received |
| **SECTION_32_PROVIDED** | Commitment authority provided |
| **RECEIVED_GOODS_SERVICES** | Goods/services received |
| **SECTION_34_PROVIDED** | Performance authority provided |
| **CREDIT_CARD_CLEARED** | Credit card payment cleared |
| **ON_HOLD** | Spending paused |
| **CANCELLED** | Spending cancelled |

To add an event:
1. Expand the spending item
2. Click **Add Event**
3. Select the event type
4. Enter the date and comments
5. Click **Save**

### 8.5 Filtering Spending Items

Use the filter options to narrow down spending items:
- **Search**: Text search in names and descriptions
- **Category Filter**: Show only items in a specific category
- **Status Filter**: Filter by spending status

![Spending List](screenshots/spending-list.png)
*The Spending page with items list and filters*

![Spending with Linked Procurement](screenshots/spending-linked-procurement.png)
*A spending item showing linked procurement with price mismatch warning (⚠️)*

---

## 9. Procurement Items

### 9.1 What are Procurement Items?

Procurement Items track the full lifecycle of purchasing goods and services, from initial request through to completion.

### 9.2 Procurement Status Workflow

```
DRAFT → PENDING_QUOTES → QUOTES_RECEIVED → UNDER_REVIEW → APPROVED → PO_ISSUED → COMPLETED
                                                                        ↓
                                                                   CANCELLED
```

| Status | Description |
|--------|-------------|
| **DRAFT** | Item created, requirements being defined |
| **PENDING_QUOTES** | Quote requests sent to vendors |
| **QUOTES_RECEIVED** | Quotes received from vendors |
| **UNDER_REVIEW** | Evaluating quotes |
| **APPROVED** | Quote approved, preparing PO |
| **PO_ISSUED** | Purchase order issued |
| **COMPLETED** | Goods/services received and paid |
| **CANCELLED** | Procurement cancelled |

### 9.3 Tracking Status

In addition to procurement status, items have a tracking status:

| Tracking Status | Color | Description |
|-----------------|-------|-------------|
| **ON_TRACK** | 🟢 Green | Proceeding normally |
| **AT_RISK** | 🟡 Yellow | Potential issues identified |
| **COMPLETED** | 🟦 Blue | Procurement completed |
| **CANCELLED** | 🔴 Red | Procurement cancelled |

![Procurement List](screenshots/procurement-list.png)
*Procurement items with tracking status badges showing ON_TRACK, AT_RISK, and other statuses*

### 9.4 Creating a Procurement Item

1. Navigate to **Procurement**
2. Click **Add Procurement Item** or the **+** button
3. Fill in the basic details:
   - **Purchase Requisition (PR)**: PR number (optional)
   - **Name**: Item name (required)
   - **Description**: Detailed description
   - **Category**: Category assignment
4. Enter pricing information:
   - **Quoted Price**: Estimated price
   - **Currency**: Currency for quoted price
   - **Exchange Rate**: If not CAD
5. Click **Save**

### 9.5 Managing Quotes

Each procurement item can have multiple vendor quotes.

#### Adding a Quote

1. Expand the procurement item
2. Click **Add Quote**
3. Enter quote details:
   - **Vendor Name**: Quote vendor
   - **Vendor Contact**: Contact information
   - **Quote Reference**: Quote or reference number
   - **Amount**: Quote amount (CAP and OM)
   - **Currency**: Quote currency
   - **Received Date**: When quote was received
   - **Expiry Date**: Quote expiration date
   - **Notes**: Additional notes
4. Click **Save**

#### Quote Status

| Status | Description |
|--------|-------------|
| **PENDING** | Quote awaiting review |
| **UNDER_REVIEW** | Quote being evaluated |
| **SELECTED** | Quote chosen for procurement |
| **REJECTED** | Quote not selected |

#### Selecting a Quote

1. Expand the quote you want to select
2. Click **Select Quote**
3. The quote status changes to SELECTED
4. Other quotes are automatically marked as REJECTED

#### Uploading Quote Files

1. Expand the quote
2. Click **Upload File**
3. Select the file (PDF, image, document)
4. Add an optional description
5. Click **Upload**

Files can be viewed inline (PDFs and images) or downloaded.

![Procurement Quotes](screenshots/procurement-quotes.png)
*The quotes section showing vendor quotes with status badges*

### 9.6 Procurement Events

Procurement events provide a detailed audit trail:

| Event Type | Description |
|------------|-------------|
| **NOT_STARTED** | Initial state |
| **QUOTE** | Quote-related event |
| **SAM_ACKNOWLEDGEMENT_REQUESTED** | SAM acknowledgement requested |
| **SAM_ACKNOWLEDGEMENT_RECEIVED** | SAM acknowledgement received |
| **PACKAGE_SENT_TO_PROCUREMENT** | Package submitted to procurement |
| **ACKNOWLEDGED_BY_PROCUREMENT** | Procurement acknowledgement received |
| **PAUSED** | Procurement paused |
| **CANCELLED** | Procurement cancelled |
| **CONTRACT_AWARDED** | Contract awarded to vendor |
| **GOODS_RECEIVED** | Goods/services received |
| **FULL_INVOICE_RECEIVED** | Full invoice received |
| **PARTIAL_INVOICE_RECEIVED** | Partial invoice received |
| **MONTHLY_INVOICE_RECEIVED** | Monthly invoice received |
| **FULL_INVOICE_SIGNED** | Full invoice signed |
| **PARTIAL_INVOICE_SIGNED** | Partial invoice signed |
| **MONTHLY_INVOICE_SIGNED** | Monthly invoice signed |
| **CONTRACT_AMENDED** | Contract amendment |
| **STATUS_CHANGE** | Automatic status change event |
| **NOTE_ADDED** | General note added |

#### Adding an Event

1. Expand the procurement item
2. Click **Add Event**
3. Select the event type
4. Enter the event date
5. Add comments
6. Click **Save**

#### Uploading Files to Events

1. Expand the event
2. Click **Upload File**
3. Select files (multiple files supported)
4. Add descriptions
5. Click **Upload**

### 9.7 Linking Procurement to Spending

When a procurement item progresses to a billable state, you can create linked spending:

1. Expand the procurement item
2. Click **Link to Spending** (➡️💰)
3. A new spending item is created linked to the procurement
4. The spending item inherits procurement details
5. The button changes to **Delete linked Spending** (🔗) to manage the link

Linked spending items show the connection in both views. The procurement list shows a 🔗 icon for items with linked spending.

![Procurement Link Spending](screenshots/procurement-link-spending.png)
*The Link to Spending button and linked spending display in a procurement item*

### 9.8 Contract Management

For completed procurements, track contract details:
- **Contract Number**: The official contract number
- **Contract Start Date**: When the contract begins
- **Contract End Date**: When the contract expires
- **Final Price**: Actual negotiated price
- **Vendor**: Selected vendor name

### 9.9 Filtering Procurement Items

Use filters to find specific items:
- **Search**: Text search across all fields
- **Category Filter**: Filter by category
- **Status Filter**: Filter by procurement status
- **Tracking Status Filter**: Filter by ON_TRACK, AT_RISK, or BLOCKED

---

## 10. Insights Dashboard

### 10.1 Overview

The Insights dashboard provides visual analytics for your financial data with interactive charts and summaries.

![Insights Overview](screenshots/insights-overview.png)
*The Insights dashboard with funding, spending, and procurement charts*

### 10.2 Available Charts

#### Funding Charts
- **Funding by Category**: Doughnut chart showing funding distribution across categories
- **CAP vs OM Breakdown**: Comparison of capital vs operational funding

#### Spending Charts
- **Spending by Category**: Distribution of spending across categories
- **Spending Status Distribution**: Pie chart of spending by status

#### Comparison Charts
- **Funding vs Spending**: Side-by-side comparison of allocations and expenditures
- **Budget Utilization**: Percentage of budget used with color indicators

#### Procurement Charts
- **Procurement by Status**: Distribution of items by tracking status (colors match the status badges: green for ON_TRACK, yellow for AT_RISK, blue for COMPLETED, red for CANCELLED)
- **Tracking Status Overview**: Visual summary of procurement health

![Procurement Status Chart](screenshots/insights-procurement-status.png)
*Procurement by Status pie chart with colors matching the tracking status badges*

### 10.3 Interacting with Charts

- **Hover**: View detailed values for chart segments
- **Click**: Some charts support filtering by clicked segment
- **Legend**: Click legend items to show/hide data series
- **Responsive**: Charts resize automatically for different screen sizes

### 10.4 Language Support

Charts automatically update labels and legends when you change the language setting.

---

## 11. Configuration

### 11.1 Accessing Configuration

1. Navigate to **Configuration** from the sidebar
2. Configuration is organized into tabs

### 11.2 General Tab

- **RC Name**: Edit the Responsibility Centre name
- **RC Description**: Update the description
- **FY Name**: Edit the Fiscal Year name
- **FY Description**: Update the description
- **Delete Options**: Delete RC or FY (owner only)

### 11.3 Money Types Tab

Money types represent different funding sources or budget pools.

#### Default Money Type

- **AB (A-Base)**: The default money type, automatically created
- Cannot be deleted
- Name can be customized

#### Custom Money Types

Click **Add Money Type** to create custom types:

| Field | Description |
|-------|-------------|
| **Code** | Short identifier (e.g., OA, WCF, GF) |
| **Name** | Descriptive name |
| **Description** | Additional details |

Common examples:
- **OA**: Operating Allotment
- **WCF**: Working Capital Fund
- **GF**: Grant Funding

### 11.4 Categories Tab

Categories organize funding, spending, and procurement items.

#### Default Categories

The following categories are created automatically:
- **Compute**: Computing resources (CAP & OM)
- **GPUs**: Graphics processing units (CAP & OM)
- **Storage**: Storage systems (CAP & OM)
- **Software Licenses**: Software licensing (OM only)
- **Hardware Support/Licensing**: Hardware maintenance (OM only)
- **Small Procurement**: Minor purchases (OM only)
- **Contractors**: Contract staff (OM only)

#### Creating a Category

1. Click **Add Category**
2. Enter the details:
   - **Name**: Category name
   - **Description**: Description
   - **Funding Type**: CAP_ONLY, OM_ONLY, or BOTH
   - **Display Order**: Sort order
3. Click **Save**

#### Funding Type Restrictions

| Funding Type | CAP Allowed | OM Allowed |
|--------------|-------------|------------|
| **CAP_ONLY** | ✅ Yes | ❌ No |
| **OM_ONLY** | ❌ No | ✅ Yes |
| **BOTH** | ✅ Yes | ✅ Yes |

### 11.5 Summary Tab

View a summary of:
- Total funding by money type
- Total spending by money type
- Budget utilization percentages

### 11.6 Import/Export Tab

- **Export**: Download fiscal year data as a file
- **Import**: Upload data to restore or migrate

---

## 12. Permissions & Access Control

### 12.1 Access Levels

| Level | Description |
|-------|-------------|
| **OWNER** | Full control, can manage permissions |
| **READ_WRITE** | Can view and modify data |
| **READ_ONLY** | Can only view data |

### 12.2 Managing Permissions (Owners Only)

1. Navigate to **Permissions** from the sidebar
2. View current access list

#### Granting User Access

1. Click **Add User Access**
2. Search for and select the user
3. Choose the access level
4. Click **Grant**

#### Granting Group Access

1. Click **Add Group Access**
2. Enter the group DN or identifier
3. Optionally enter a display name
4. Choose the access level
5. Click **Grant**

#### Updating Access

1. Find the user/group in the access list
2. Click **Edit**
3. Select the new access level
4. Click **Save**

#### Revoking Access

1. Find the user/group in the access list
2. Click **Remove**
3. Confirm the removal

### 12.3 Principal Types

| Type | Description |
|------|-------------|
| **USER** | Individual user account |
| **GROUP** | Security group |
| **DISTRIBUTION_LIST** | Distribution list |

---

## 13. Multi-Currency Support

### 13.1 Supported Currencies

myRC supports multiple currencies with the following defaults:
- **CAD** (🇨🇦): Canadian Dollar (default)
- **USD** (🇺🇸): US Dollar
- **EUR** (🇪🇺): Euro
- **GBP** (🇬🇧): British Pound

### 13.2 Currency Selection

When creating or editing items:
1. Select the currency from the dropdown
2. If not CAD, enter the exchange rate
3. The CAD equivalent is calculated automatically

### 13.3 Exchange Rates

- Exchange rates represent the conversion factor to CAD
- Example: If 1 USD = 1.36 CAD, enter `1.36` as the exchange rate
- CAD equivalent = Amount × Exchange Rate

### 13.4 Where Currency Applies

| Item Type | Currency Fields |
|-----------|-----------------|
| **Funding Items** | Currency, Exchange Rate |
| **Spending Items** | Currency, Exchange Rate |
| **Procurement Items** | Quoted Price Currency, Final Price Currency |
| **Quotes** | Quote Amount Currency |

---

## 14. Search & Filtering

### 14.1 Search Functionality

All main views (Dashboard, Spending, Procurement) include search:

1. Click the search box or use keyboard shortcut
2. Type your search term
3. Results filter in real-time
4. Search matches against:
   - Names
   - Descriptions
   - Reference numbers
   - Vendor names

### 14.2 Fuzzy Search

The search uses intelligent fuzzy matching:
- Finds partial matches
- Handles typos
- Ranks results by relevance
- Supports multi-word searches

### 14.3 Category Filtering

1. Click the category filter dropdown
2. Select a category to filter
3. Only items in that category are displayed
4. Click "All Categories" to clear the filter

### 14.4 Status Filtering

#### Spending Status Filter
- DRAFT, PENDING, APPROVED, COMMITTED, PAID, CANCELLED

#### Procurement Status Filter
- All procurement statuses

#### Tracking Status Filter
- ON_TRACK (🟢)
- AT_RISK (🟡)
- BLOCKED (🔴)

### 14.5 Combining Filters

Filters can be combined:
- Search text + Category filter
- Category filter + Status filter
- All three together

Click **Clear Filters** to reset all filters.

---

## 15. Import & Export

### 15.1 Exporting Data

1. Navigate to **Configuration**
2. Go to the **Import/Export** tab
3. Click **Export**
4. Choose the download location
5. Save the export file

Export includes:
- Fiscal year settings
- Categories and money types
- Funding items
- Spending items
- Procurement items with quotes and events

### 15.2 Importing Data

1. Navigate to **Configuration**
2. Go to the **Import/Export** tab
3. Click **Import**
4. Select the export file
5. Review the import preview
6. Confirm the import

> ⚠️ **Warning**: Importing may overwrite existing data. Always export a backup first.

---

## 16. Language & Theme Settings

### 16.1 Language Selection

myRC supports:
- **English** (en)
- **Français** (fr)

To change language:
1. Click the language toggle in the header
2. Or: Click your profile → Language
3. Select the desired language
4. The interface updates immediately

Language preference is saved and persists across sessions.

### 16.2 Theme Selection

Available themes:
- **Light Mode**: Default light appearance
- **Dark Mode**: Dark appearance for reduced eye strain

To change theme:
1. Click the theme toggle (☀️/🌙) in the header
2. Or: Click your profile → Theme
3. Select Light or Dark

Theme preference is saved to your user account.

![Light Theme](screenshots/theme-light.png)
*Application in light theme*

![Dark Theme](screenshots/theme-dark.png)
*Application in dark theme*

---

## 17. Demo RC

### 17.1 What is the Demo RC?

The Demo RC is a pre-configured Responsibility Centre available to all users. It includes:
- A complete fiscal year (FY 2025-2026)
- Sample funding items with various funding sources
- Sample spending items in different statuses
- Sample procurement items with quotes, events, and files
- Custom money types (OA, WCF, GF)
- Custom categories

### 17.2 Purpose

The Demo RC allows users to:
- Explore the application features
- Understand data structures and workflows
- Practice without affecting real data

### 17.3 Access

- All users have **READ_ONLY** access to the Demo RC
- Only administrators can modify Demo RC data
- The Demo RC is owned by the admin account

### 17.4 Sample Data

The Demo RC includes realistic sample data:

| Data Type | Examples |
|-----------|----------|
| **Funding Items** | IT Infrastructure, Cloud Services, Staff Training |
| **Spending Items** | AWS Monthly Services, Office Supplies, Software Licenses |
| **Procurement Items** | Dell Servers, NVIDIA GPUs, Cisco Switches |
| **Quotes** | Multiple vendors per procurement with files |
| **Events** | Complete procurement lifecycle events |

---

## 18. Keyboard Shortcuts

| Shortcut | Action |
|----------|--------|
| `/` | Focus search box |
| `Escape` | Clear search / Close dialog |
| `Enter` | Submit form / Confirm action |
| `Ctrl + S` | Save current item (in edit mode) |

---

## 19. Troubleshooting

### 19.1 Login Issues

**Problem**: Cannot log in

**Solutions**:
1. Verify your username and password are correct
2. Check if your account is locked (contact administrator)
3. Clear browser cookies and cache
4. Try a different browser
5. Check the API and Database status indicators on the login page

**Problem**: Session expired

**Solutions**:
1. Log in again
2. Check network connectivity
3. Ensure cookies are enabled

### 19.2 Data Not Loading

**Problem**: Items not appearing

**Solutions**:
1. Check your RC/FY selection
2. Verify you have access to the RC
3. Clear filters and search
4. Refresh the page
5. Check network connectivity

### 19.3 Permission Issues

**Problem**: Cannot edit items

**Solutions**:
1. Verify your access level (need READ_WRITE or OWNER)
2. Contact the RC owner to request access
3. Check if you're viewing the Demo RC (read-only for all users)

### 19.4 File Upload Issues

**Problem**: Cannot upload files

**Solutions**:
1. Check file size (maximum 10MB)
2. Verify file type is allowed
3. Check browser permissions
4. Try a different file format

### 19.5 Chart Display Issues

**Problem**: Charts not rendering

**Solutions**:
1. Refresh the page
2. Clear browser cache
3. Try a different browser
4. Ensure JavaScript is enabled

---

## 20. Glossary

| Term | Definition |
|------|------------|
| **AB (A-Base)** | Default funding allocation type |
| **CAP (Capital)** | Capital expenditure budget for equipment and infrastructure |
| **ECO** | Expenditure Control Officer |
| **FY (Fiscal Year)** | A financial year period within an RC |
| **OM (Operations & Maintenance)** | Operational budget for services and consumables |
| **PO (Purchase Order)** | Official document authorizing a purchase |
| **PR (Purchase Requisition)** | Initial request to purchase goods/services |
| **RC (Responsibility Centre)** | An organizational unit for budget management |
| **SAM** | Supply Arrangement Manager |
| **Section 32** | Financial commitment authority |
| **Section 34** | Financial performance authority |

---

## Version History

| Version | Date | Changes |
|---------|------|---------|
| 1.0.0 | 2026-02-04 | Initial release |

---

## Support

For technical support or to report issues:

1. Contact your system administrator
2. Submit an issue through your organization's ticketing system
3. Refer to the [TROUBLESHOOTING.md](TROUBLESHOOTING.md) documentation

---

*myRC User Guide - © 2026 myRC Team - MIT License*
