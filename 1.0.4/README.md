# Electro Accountant

Local-first accounting app for inventory, sales, purchases, payables, expenses, and journals.

## Windows desktop application

The desktop package runs one local process. It serves the React frontend and API at
`http://127.0.0.1:8080`, opens the default browser after launch, and stores its SQLite
data under `%LOCALAPPDATA%\ElectroAccountant\1.0.4\data`. Closing the browser does **not** stop
the app; use **Stop app** in the header when you are finished.

On a Windows machine with JDK 21 (including `jpackage`), Maven, and Node.js installed:

```powershell
.\scripts\build-windows-app.ps1
```

This produces a portable application at:

```text
windows-app\Electro Accountant\Electro Accountant.exe
```

To produce a Windows installer `.exe`, install WiX Toolset and run:

```powershell
.\scripts\build-windows-app.ps1 -PackageType exe
```

To produce an MSI installer instead:

```powershell
.\scripts\build-windows-app.ps1 -PackageType msi
```

The included GitHub Actions workflow builds the Windows `.exe` on GitHub's Windows
runner. After pushing this workflow, open **Actions → Build Windows installer → Run
workflow**, then download `Electro-Accountant-1.0.4-Windows-Installer` from the
completed run.

`jpackage` produces native packages only for the operating system on which it runs. A
Linux Docker container cannot create the Windows `.exe`/MSI. You can either run the
script directly on Windows (simplest) or switch Docker Desktop to Windows containers
only for the packaging build; the application itself does not need to run in a Windows
container.

## Run with Docker (recommended)

Install Docker Desktop, then from this folder run:

```powershell
docker compose up --build
```

Open `http://localhost:8080`. The browser dashboard, API, and SQLite database all run locally. The database is persisted in the `accountant-data` Docker volume, so `docker compose down` does not delete it.

## Basic workflow

1. Create items with their unit, opening quantity, default purchase rate, sales rate/MRP, and reorder level.
2. Add ledgers. A ledger can be a customer/consumer, supplier, both customer and supplier,
   capital, cash, bank, expense, income, asset, liability, or another account.
3. Post a purchase or sale with one or more item lines. Choose cash or credit for each entry.
4. Record customer receipts and supplier payments against their credit balances. Use **All transactions** to filter the full register by date and party.
5. Add freight, shipping, rent, utilities, or other expense vouchers. They appear in Profit & Loss, not Trading or inventory valuation.

`Reorder level` is a warning threshold only: when available stock is at or below it, the item is marked **Low stock**. It does not create a purchase automatically.

## Run backend only (development)

- JDK 21
- Maven 3.9+ (or add the Maven Wrapper later)

## Run

```powershell
$env:ELECTRO_ACCOUNTANT_DATA_DIR = "$PWD\data"
mvn spring-boot:run
```

The API starts at `http://127.0.0.1:8080`. The SQLite database will be created in the data directory.

## First API calls

Create an item:

```powershell
Invoke-RestMethod -Method Post http://127.0.0.1:8080/api/items -ContentType 'application/json' -Body '{"sku":"WIRE-001","name":"Copper Wire","unit":"MTR","openingQuantity":"100","reorderLevel":"10"}'
```

Create a purchase entry:

```powershell
Invoke-RestMethod -Method Post http://127.0.0.1:8080/api/purchases -ContentType 'application/json' -Body '{"entryDate":"2026-11-20","supplierName":"ABC Electricals","supplierInvoiceNumber":"INV-100","lines":[{"itemId":"REPLACE_ITEM_ID","quantity":"20","unitAmount":"50.00"}]}'
```

Create a sales entry:

```powershell
Invoke-RestMethod -Method Post http://127.0.0.1:8080/api/sales -ContentType 'application/json' -Body '{"entryDate":"2026-11-20","customerName":"Customer A","invoiceNumber":"SALE-001","lines":[{"itemId":"REPLACE_ITEM_ID","quantity":"3","unitAmount":"80.00"}]}'
```

## Current scope

- Credit sales and purchases, customer receipts, supplier payments, and balances
- Expense vouchers (freight/shipping included) and a Profit & Loss report
- Automatic, source-linked double-entry journals for new transactions
- Weighted-average COGS journal posting for new sales

Tax, invoice PDFs, credit notes, bank reconciliation, and cloud backup remain future work.
