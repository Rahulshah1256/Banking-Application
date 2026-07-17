# 🏦 JantaBank — Enterprise NetBanking Platform

A production-grade, full-stack **Internet & Mobile Banking** application built with a **Spring Boot 3** REST backend and a **React 19 + Material UI** single-page front end. JantaBank models a real retail bank: customer onboarding with KYC, multi-account management, inter-bank fund transfers (IMPS/NEFT/RTGS/UPI), debit/credit cards, loans, fixed & recurring deposits, cheque services, notifications, support desk, reporting, and a full admin console.

> The application simulates the operations of a fictional branch — **Excellence Bank, MG Road (IFSC `EXCB0002480`, Branch `0048`)**.

---

## 📑 Table of Contents

1. [What is this application?](#-what-is-this-application)
2. [Tech Stack](#-tech-stack)
3. [Architecture](#-architecture)
4. [Feature Catalogue](#-feature-catalogue)
5. [Project Structure](#-project-structure)
6. [Prerequisites](#-prerequisites)
7. [Setup & Installation](#-setup--installation)
8. [Running the Application](#-running-the-application)
9. [Default Credentials](#-default-credentials)
10. [Configuration Reference](#-configuration-reference)
11. [REST API Reference](#-rest-api-reference)
12. [Security Model](#-security-model)
13. [Database & Migrations](#-database--migrations)
14. [Domain Enumerations](#-domain-enumerations)
15. [Troubleshooting](#-troubleshooting)

---

## 🎯 What is this application?

JantaBank is an end-to-end digital banking system split into two deployable applications:

| Application | Directory | Description |
|-------------|-----------|-------------|
| **Backend API** | `Banking-Application-BackEnd` | Spring Boot 3 REST API secured with JWT, backed by MySQL and versioned with Flyway migrations. Exposes ~120 endpoints across 18 controllers. |
| **Frontend SPA** | `Banking-Application-FrontEnd` | React 19 + Vite single-page app using Material UI 6, with a distinctive fintech design system, protected routing, and a dedicated admin console. |

The system supports two roles — **Customers** (`ROLE_USER`) who manage their own banking, and **Administrators** (`ROLE_ADMIN`) who approve users, monitor transactions, and generate reports.

---

## 🧰 Tech Stack

### Backend
- **Java 20**, **Spring Boot 3.1.3**
- **Spring Security 6** + **JWT** (`jjwt 0.11.5`) — access + refresh tokens, token blacklist
- **Spring Data JPA / Hibernate** (validate mode — Flyway owns the schema)
- **MySQL 8** (`mysql-connector-j`)
- **Flyway** database migrations (16 versioned scripts)
- **ModelMapper** for DTO ↔ entity mapping
- **Bean Validation** (`spring-boot-starter-validation`)
- **OpenPDF** (statement/receipt PDFs) + **ZXing** (UPI QR codes)
- **springdoc-openapi** (Swagger UI)
- **Lombok**

### Frontend
- **React 19** + **Vite 5**
- **Material UI 6** (`@mui/material`, `@mui/icons-material`, `@mui/x-data-grid`, `@mui/x-date-pickers`)
- **React Router 6.28** (protected route guards)
- **Axios** (central client with JWT interceptor + response unwrapping)
- **notistack** (snackbar notifications), **dayjs**, **jwt-decode**
- Custom design system (indigo/violet + teal palette, Plus Jakarta Sans, glassmorphism, gradient surfaces)

---

## 🏗 Architecture

JantaBank follows a **decoupled client–server architecture**: a stateless REST API and an independently deployable SPA that communicate purely over JSON + JWT. There are no server-rendered pages and no shared session state — the token *is* the session.

### 1. High-level topology

```
┌──────────────────────────┐        HTTPS/JSON        ┌────────────────────────────┐
│   React 19 SPA (Vite)    │  ───────────────────────▶│   Spring Boot REST API      │
│   http://localhost:3000  │   Authorization: Bearer  │   http://localhost:8080     │
│                          │◀───────────────────────  │                             │
│  • Route guards (JWT)    │        JWT / JSON        │  • Controllers (18)         │
│  • Axios interceptor     │                          │  • Services / Impl          │
│  • MUI design system     │                          │  • Spring Security + JWT    │
└──────────────────────────┘                          │  • Schedulers (transfers,   │
                                                       │    deposits, token cleanup) │
                                                       └──────────────┬──────────────┘
                                                                      │ JPA / Hibernate
                                                                      ▼
                                                       ┌────────────────────────────┐
                                                       │      MySQL 8 (bank_...)     │
                                                       │  Schema managed by Flyway   │
                                                       └────────────────────────────┘
```

### 2. Backend layered architecture

The backend is a classic **N-tier Spring Boot application**. Each HTTP request flows top-to-bottom through clearly separated layers, and data crosses layer boundaries as DTOs (never raw entities leaving the service layer):

```
                          HTTP request (JSON)
                                  │
        ┌─────────────────────────▼─────────────────────────┐
        │  web/filter → TraceIdFilter (assigns MDC traceId)  │
        │  security   → JwtAuthenticationFilter (validates   │
        │               Bearer token, sets SecurityContext)  │
        └─────────────────────────┬─────────────────────────┘
                                  │
        ┌──────────────────── CONTROLLER LAYER ─────────────────────┐
        │  @RestController — request binding, @Valid on DTOs,       │
        │  role checks (@PreAuthorize / SecurityConfig), maps to    │
        │  a uniform ApiResponse / ErrorDetails envelope.           │
        └─────────────────────────┬─────────────────────────────────┘
                                  │  Request DTO
        ┌──────────────────── SERVICE LAYER (interface) ────────────┐
        │  Business contract — e.g. AccountService, LoanService.    │
        └─────────────────────────┬─────────────────────────────────┘
                                  │
        ┌──────────────────── IMPL LAYER (@Service) ────────────────┐
        │  Business rules, @Transactional boundaries, orchestration │
        │  of multiple repositories, interest/EMI/limit calc,       │
        │  ModelMapper entity↔DTO mapping, domain validation.       │
        └─────────────────────────┬─────────────────────────────────┘
                                  │  JPA entities
        ┌──────────────────── REPOSITORY LAYER ─────────────────────┐
        │  Spring Data JPA interfaces (JpaRepository) + queries.    │
        └─────────────────────────┬─────────────────────────────────┘
                                  │  SQL (Hibernate)
        ┌──────────────────── PERSISTENCE ──────────────────────────┐
        │  MySQL 8 — schema created & versioned by Flyway.          │
        │  Enums stored as compact codes via AttributeConverters.  │
        └───────────────────────────────────────────────────────────┘
```

**Package responsibilities** (`com.jantabank.*`):

| Package | Responsibility |
|---------|----------------|
| `controller/` | 18 `@RestController`s — the HTTP boundary; binding, validation, response envelopes |
| `service/` + `impl/` | Business contracts and their implementations (`@Service`, `@Transactional`) |
| `repository/` | Spring Data JPA repositories |
| `entity/` | JPA entities; `BaseEntity` provides shared audit columns (created/updated) |
| `dto/` | Per-module request/response DTOs (`account`, `card`, `loan`, `deposit`, …) |
| `domain/enums/` + `domain/converter/` | `CodedEnum` types persisted as codes via JPA converters |
| `security/` | `JwtTokenProvider`, `JwtAuthenticationFilter`, `JwtAuthenticationEntryPoint`, `CustomUserDetailsService`, `TokenHashUtil` |
| `web/filter/` | `TraceIdFilter` — injects a per-request `traceId` into the logging MDC |
| `config/` | `SpringSecurityConfig`, `AppConfig`, `BranchConfig`, `SwaggerConfig`, `Enums` |
| `scheduler/` | `ScheduledTransferTask`, `DepositMaturityTask`, `TokenCleanupTask` |
| `exception/` | `GlobalExceptionHandler`, `ErrorDetails`, `ResourceNotFoundException`, `TodoAPIException` |
| `common/` | `ApiResponse` — uniform success/data/message envelope |

### 3. Request lifecycle (end-to-end)

A typical authenticated call — e.g. **POST `/api/transactions/transfer`**:

1. **TraceIdFilter** generates a `traceId` and stores it in the SLF4J MDC so every log line for this request is correlated.
2. **JwtAuthenticationFilter** extracts the `Bearer` token, verifies its signature/expiry via `JwtTokenProvider`, checks it against the **token blacklist**, loads the user through `CustomUserDetailsService`, and populates the `SecurityContext`.
3. **SpringSecurityConfig** authorizes the route by role (`ROLE_USER` / `ROLE_ADMIN`). Unauthorized requests are rejected by `JwtAuthenticationEntryPoint` (401).
4. The **Controller** binds the JSON body to a DTO and runs Bean Validation (`@Valid`).
5. The **Service impl** opens a `@Transactional` boundary, applies business rules (per-mode transfer limits, balance checks), debits/credits accounts, writes a `Transaction`, and may enqueue a `Notification`.
6. **Repositories** persist changes through Hibernate to MySQL in a single atomic transaction.
7. The controller returns an `ApiResponse` envelope (or a raw payload for legacy endpoints such as login/register).
8. Any thrown exception is caught by **GlobalExceptionHandler**, which returns a consistent `ErrorDetails` body: `{ success, message, errorCode, timestamp, path, traceId }`.

### 4. Background schedulers

Three `@Scheduled` jobs run inside the backend process (intervals in `application.properties`):

| Task | Interval | Responsibility |
|------|----------|----------------|
| `ScheduledTransferTask` | 60 s | Executes due scheduled/recurring transfers |
| `DepositMaturityTask` | 60 s | Processes matured FDs/RDs, handles auto-renew |
| `TokenCleanupTask` | 1 h | Purges expired refresh tokens & blacklist entries |

### 5. Frontend architecture

The SPA is organised around a **provider shell → routing → feature pages** structure:

```
index.jsx  (composition root)
  └─ ThemeProvider (theme.js) ─ CssBaseline ─ LocalizationProvider (dayjs)
       └─ SnackbarProvider (notistack)
            └─ BrowserRouter
                 └─ AuthContext  (JWT decode, current user, login/logout)
                      └─ App.jsx  (route tree)
                           ├─ Public routes ── Login / Register  (AuthShell)
                           └─ Protected routes (RouteGuards)
                                └─ AppLayout  (sidebar + appbar + NotificationBell)
                                     ├─ Dashboard, Accounts, Transfer, Beneficiaries,
                                     │  Cards, Loans, Deposits, Cheques, Profile,
                                     │  Notifications, Support, Reports
                                     └─ Admin-only ── admin/AdminConsole
```

- **`api/client.js`** — a single Axios instance (`baseURL: http://localhost:8080`). A **request interceptor** injects `Authorization: Bearer <jb_token>`; a **response interceptor** `unwrap()`s `{success,data}` envelopes (and passes raw payloads through) and redirects to `/login` on `401`.
- **`services/`** — 14 thin modules (one per domain: `accounts`, `transfers`, `cards`, `loans`, …) that map UI actions to API endpoints, keeping pages free of URL/HTTP details.
- **`auth/AuthContext.jsx`** — decodes the JWT (`jwt-decode`) to expose the current user, role, name and email, and drives `RouteGuards` (redirect unauthenticated users, gate admin routes on `ROLE_ADMIN`).
- **`layout/AppLayout.jsx`** — the authenticated shell (glassmorphic sidebar, blurred app bar, live notification bell).
- **`theme.js` + `index.css`** — the centralised design system (palette, typography, component overrides, gradients) so visual identity is defined in one place.

### 6. Cross-cutting concerns

| Concern | Mechanism |
|---------|-----------|
| **AuthN / AuthZ** | Stateless JWT (access + refresh), Spring Security filter chain, role guards on both tiers |
| **Observability** | `traceId` MDC per request, structured log pattern, `traceId` surfaced in error responses |
| **Validation** | Bean Validation on DTOs (server) mirrored by client-side form validation |
| **Error handling** | Central `GlobalExceptionHandler` → uniform `ErrorDetails` envelope |
| **Schema evolution** | Flyway migrations (V1–V16); Hibernate `validate` guards drift |
| **API docs** | springdoc OpenAPI / Swagger UI |
| **File storage** | Local `uploads/` dir for KYC docs & profile photos (size-capped) |
| **Documents** | OpenPDF for statements/receipts, ZXing for UPI QR codes |

---

## ✨ Feature Catalogue

### 🔐 Authentication & Account Security
- User **registration** with strict KYC validation (Aadhaar, PAN, mobile) — new users enter `REQUESTED` status
- **Login** with username *or* email, JWT **access + refresh** tokens
- **Email verification** and **password reset** via tokenised links
- **OTP** request/verify flow (configurable length & expiry)
- **Change password**, **forgot password**
- **Login history**, **known devices** management, and **failed-login lockout** (5 attempts → 15-min lock)
- Token **blacklist** on logout, scheduled token cleanup

### 👤 Customer Profile & KYC
- View / update customer profile, upload profile photo
- **KYC document** upload (PAN, Aadhaar, Passport, Driving Licence, Address Proof), status tracking & admin verification
- **Nominee** management (add / list / update / delete)

### 💳 Accounts
- Open **Savings** / **Current** accounts, list & view details
- Account **summary**, **balance**, and **mini-statement**
- Statement export as **CSV** and **PDF**
- Interest accrual by account type (Savings 3.5%, Current 0%)

### 💸 Transfers & Transactions
- Fund transfers across modes: **WITHIN_BANK, IMPS, NEFT, RTGS, UPI**
- Per-mode limits (IMPS ≤ ₹5L, RTGS ≥ ₹2L, NEFT ≤ ₹10L, per-txn ≤ ₹20L)
- **Scheduled / recurring transfers** with a background scheduler
- Transaction history with filtering, single-transaction lookup, and **PDF receipts**

### 🧑‍🤝‍🧑 Beneficiaries
- Add, edit, delete, search beneficiaries
- **Cooling-off activation delay** (30 min) before a beneficiary is usable
- Approve beneficiaries, mark **favourites**

### 💳 Cards
- Issue **debit/credit** cards (VISA / Mastercard / RuPay)
- **Block / unblock / replace** cards, set/change **PIN**
- Toggle usage **controls** (ATM / POS / Online) and adjust **spend limits**
- Card action **history** audit trail

### 🏦 Loans
- **EMI calculator**, apply for **HOME / CAR / EDUCATION / PERSONAL** loans
- Amortisation **schedule**, loan **statement**
- Pay **EMI**, make **prepayments**
- Rates configured per loan type (Home 8.5% … Personal 14%)

### 💰 Deposits
- **Fixed (FD)** and **Recurring (RD)** deposits with a maturity calculator
- Pay RD **installments**, **premature closure** (with penalty), **auto-renew** toggle
- Background scheduler for maturity processing

### 🧾 Cheque Services
- Request **cheque books** (10/25/50/100 leaves), issue & deliver
- List leaves, **stop payment**, **positive pay** registration

### 🔔 Notifications & 🆘 Support
- In-app **notifications** (unread count, mark read / read-all, delete), low-balance alerts
- **Support tickets** by category/priority with threaded messages and status workflow
- Self-service **FAQs**, **branch** and **ATM locator**

### 📊 UPI
- Create **UPI handles (VPA)**, resolve VPA, **pay via UPI**, generate **QR codes**

### 📈 Reports & 🛠 Admin Console
- Customer **portfolio** and **transaction** reports (grouped by mode / type / status)
- **Admin overview** dashboard, user management with **status approval** (approve/reject/deactivate)
- Admin-wide **transaction monitoring** and **reporting**

---

## 📂 Project Structure

```
Banking-Application/
├── README.md                          ← you are here
├── Banking-Application-BackEnd/
│   ├── pom.xml
│   └── src/main/
│       ├── java/com/jantabank/
│       │   ├── controller/            ← 18 REST controllers
│       │   ├── service/ & impl/       ← business logic
│       │   ├── repository/            ← Spring Data JPA repos
│       │   ├── entity/                ← JPA entities (User, Account, Loan, …)
│       │   ├── dto/                   ← request/response DTOs (by module)
│       │   ├── domain/enums/          ← coded enums + converters
│       │   ├── security/              ← JWT provider, filters, user details
│       │   ├── scheduler/             ← background jobs
│       │   ├── exception/             ← GlobalExceptionHandler + custom errors
│       │   ├── config/ & common/ & utils/ & web/
│       └── resources/
│           ├── application.properties
│           └── db/migration/          ← Flyway V1__ … V16__ scripts
│
└── Banking-Application-FrontEnd/
    ├── package.json
    ├── vite.config.js                 ← dev server on port 3000
    ├── index.html
    └── src/
        ├── index.jsx                  ← app entry (providers + router)
        ├── App.jsx                    ← route tree
        ├── theme.js                   ← central design system
        ├── index.css                  ← global styles (aurora background)
        ├── api/client.js              ← axios client + interceptors
        ├── context/                   ← AuthContext
        ├── layout/AppLayout.jsx       ← sidebar + appbar shell
        ├── components/                ← PageHeader, StatCard, NotificationBell, guards
        ├── services/                  ← 14 API service modules
        └── pages/                     ← Login, Register, Dashboard, Accounts,
                                          Transfer, Beneficiaries, Cards, Loans,
                                          Deposits, Cheques, Profile, Notifications,
                                          Support, Reports, admin/AdminConsole
```

---

## ✅ Prerequisites

Install the following before setup:

| Tool | Version | Notes |
|------|---------|-------|
| **JDK** | 20+ (21 works) | Backend targets Java 20 |
| **Maven** | 3.8+ | Or use the bundled `mvnw` wrapper if present |
| **MySQL** | 8.x | A running server on `localhost:3306` |
| **Node.js** | 18+ (20+ recommended) | Ships with npm |

---

## ⚙️ Setup & Installation

### 1. Clone the repository
```bash
git clone https://github.com/Rahulshah1256/Banking-Application.git
cd Banking-Application
```

### 2. Create the MySQL database
```sql
CREATE DATABASE bank_management;
```
> Flyway automatically creates and versions all tables on first backend startup — you only need the empty schema.

### 3. Configure backend credentials
Edit `Banking-Application-BackEnd/src/main/resources/application.properties` and update the datasource to match your MySQL:
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/bank_management
spring.datasource.username=root
spring.datasource.password=YOUR_MYSQL_PASSWORD
```
> ⚠️ **Security note:** The committed `app.jwt-secret` and DB password are for local development only. Change them (and externalise via environment variables) before any real deployment.

### 4. Install frontend dependencies
```bash
cd Banking-Application-FrontEnd
npm install
```

---

## ▶️ Running the Application

Run the backend and frontend in **two separate terminals**.

### Terminal 1 — Backend (port 8080)
```bash
cd Banking-Application-BackEnd
# Using Maven wrapper (if present):
./mvnw spring-boot:run
# …or with a system Maven:
mvn spring-boot:run
```
On startup Flyway applies all migrations and the API becomes available at:
- **API base:** http://localhost:8080
- **Swagger UI:** http://localhost:8080/swagger-ui/index.html

### Terminal 2 — Frontend (port 3000)
```bash
cd Banking-Application-FrontEnd
npm run dev
```
Open the SPA at **http://localhost:3000**.

### Production build (frontend)
```bash
npm run build      # outputs static assets to dist/
npm run preview    # serve the production build locally
```

### Package the backend as a JAR
```bash
cd Banking-Application-BackEnd
mvn clean package
java -jar target/Banking-Application-For-JantaBank-0.0.1-SNAPSHOT.jar
```

---

## 🔑 Default Credentials

Seeded by the Flyway baseline migration:

| Role | Username | Password | Notes |
|------|----------|----------|-------|
| **Admin** | `admin` | `admin123` | `ROLE_ADMIN` — full admin console |
| **Customer** | `user1` | `user123` | `ROLE_USER` — owns two demo accounts |

Log in on the SPA with **username or email** + password. New self-registered users are created in `REQUESTED` status (they can still log in) and can be approved by an admin.

---

## 🔧 Configuration Reference

Key tunables in `application.properties`:

| Property | Default | Meaning |
|----------|---------|---------|
| `app.jwt-expiration-milliseconds` | `900000` (15 min) | Access token lifetime |
| `app.jwt-refresh-expiration-milliseconds` | `604800000` (7 d) | Refresh token lifetime |
| `app.max-failed-login-attempts` | `5` | Lockout threshold |
| `app.account-lock-duration-milliseconds` | `900000` (15 min) | Lockout duration |
| `app.otp-length` / `app.otp-expiration-milliseconds` | `6` / `300000` | OTP config |
| `app.transfer.imps-max-amount` | `500000` | IMPS ceiling |
| `app.transfer.rtgs-min-amount` | `200000` | RTGS floor |
| `app.transfer.neft-max-amount` | `1000000` | NEFT ceiling |
| `app.beneficiary.activation-delay-minutes` | `30` | Cooling-off before use |
| `app.loan.*-interest-rate` | 8.5%–14% | Per-type loan rates |
| `app.deposit.fd/rd-interest-rate` | 6.8% / 6.5% | Deposit rates |
| `app.storage.upload-dir` | `uploads` | KYC/photo storage dir |
| `bank.branch-ifsc` | `EXCB0002480` | Simulated branch IFSC |

---

## 📡 REST API Reference

All endpoints are under `http://localhost:8080`. Protected routes require an `Authorization: Bearer <accessToken>` header.

### Auth — `/api/auth`
| Method | Path | Purpose |
|--------|------|---------|
| POST | `/register` | Register a new customer |
| POST | `/login` | Authenticate → JWT |
| POST | `/refresh` | Exchange refresh token |
| POST | `/logout` | Blacklist current token |
| POST | `/change-password` | Change password |
| POST | `/forgot-password` · `/reset-password` | Password reset flow |
| POST | `/verify-email` · `/resend-verification` | Email verification |
| POST | `/otp/request` · `/otp/verify` | OTP flow |
| GET | `/login-history` · `/last-login` | Login audit |
| GET/DELETE | `/devices` · `/devices/{id}` | Known devices |

### Accounts — `/api/accounts`
`POST /` · `GET /` · `GET /{id}` · `DELETE /{id}` · `GET /{id}/details` · `GET /{id}/summary` · `GET /{id}/statement` · `GET /{id}/statement/csv` · `GET /{id}/statement/pdf`

### Transactions — `/api/transactions`
`POST /` · `POST /transfer` · `GET /` · `GET /{reference}` · `GET /{reference}/receipt` · `POST /scheduled` · `GET /scheduled` · `DELETE /scheduled/{id}`

### Beneficiaries — `/api/beneficiaries`
`GET /` · `POST /` · `POST /register` · `PUT /{id}` · `DELETE /{id}` · `POST /{id}/approve` · `PATCH /{id}/favourite` · `GET /search`

### Cards — `/api/cards`
`POST /` · `GET /` · `GET /{id}` · `POST /{id}/block` · `POST /{id}/unblock` · `POST /{id}/replace` · `POST /{id}/pin` · `PATCH /{id}/controls` · `PATCH /{id}/limits` · `GET /{id}/history`

### Loans — `/api/loans`
`POST /calculate` · `POST /` · `GET /` · `GET /{id}` · `GET /{id}/schedule` · `GET /{id}/statement` · `POST /{id}/emi` · `POST /{id}/prepay`

### Deposits — `/api/deposits`
`POST /calculate` · `POST /fd` · `POST /rd` · `GET /` · `GET /{id}` · `POST /{id}/installment` · `POST /{id}/close` · `PATCH /{id}/auto-renew`

### Cheques — `/api/cheques`
`POST /books` · `GET /books` · `GET /books/{id}` · `POST /books/{id}/issue` · `POST /books/{id}/deliver` · `GET /leaves` · `GET /leaves/{id}` · `POST /leaves/{id}/stop` · `POST /leaves/{id}/positive-pay`

### UPI — `/api/upi`
`POST /handles` · `GET /handles` · `GET /resolve/{vpa}` · `POST /pay` · `GET /qr`

### Profile / KYC / Nominees
- `/api/profile` — `GET` · `PUT` · `POST /photo` · `GET /photo`
- `/api/kyc` — `POST /documents` · `GET /documents` · `GET /status` · `GET /documents/{id}/file` · `POST /documents/{id}/verify`
- `/api/nominees` — `POST /` · `GET /` · `PUT /{id}` · `DELETE /{id}`

### Notifications & Support
- `/api/notifications` — `GET /` · `GET /unread-count` · `PATCH /{id}/read` · `PATCH /read-all` · `DELETE /{id}`
- `/api/support` — `POST /tickets` · `GET /tickets` · `GET /tickets/{id}` · `POST /tickets/{id}/messages` · `PATCH /tickets/{id}/status` · `GET /faqs` · `GET /branches` · `GET /atms`

### Dashboard & Reports
- `/api/dashboard` — `GET /`
- `/api/reports` — `GET /transactions` · `GET /portfolio`

### Admin — `/api/admin` *(requires `ROLE_ADMIN`)*
`GET /overview` · `GET /users` · `GET /users/{id}` · `PATCH /users/{id}/status` · `GET /transactions` · `GET /reports/transactions`

> A complete, interactive contract is available via **Swagger UI** at `/swagger-ui/index.html`.

---

## 🛡 Security Model

- **Stateless JWT** authentication. On login the API returns a `JwtAuthResponse` containing `accessToken` (+ refresh). The SPA stores the token in `localStorage` (`jb_token`) and attaches `Bearer` on every request via an Axios interceptor.
- **Roles:** `ROLE_ADMIN`, `ROLE_USER`. JWT claims include `sub` (username), `role`, `name`, `email`. Admin routes are guarded both server-side (Spring Security) and client-side (route guards).
- **Refresh tokens** are persisted and rotate; **logout** blacklists the current token; a scheduled job purges expired/blacklisted tokens.
- **Account lockout** after repeated failed logins; **OTP** and **email verification** add step-up assurance.
- On a `401` response the SPA interceptor redirects to `/login`.

---

## 🗄 Database & Migrations

- Schema is **owned by Flyway** — Hibernate runs in `validate` mode and will fail fast if entities and tables diverge.
- Migrations live in `src/main/resources/db/migration` and run automatically on startup:

| Version | Migration | Version | Migration |
|---------|-----------|---------|-----------|
| V1 | baseline schema | V9 | UPI handles |
| V2 | audit columns & indexes | V10 | enrich beneficiaries |
| V3 | auth refresh & blacklist | V11 | cards |
| V4 | password reset tokens | V12 | loans |
| V5 | email verification & OTP | V13 | deposits |
| V6 | login history, devices, lockout | V14 | profile / KYC / nominee |
| V7 | enrich transactions | V15 | cheques |
| V8 | scheduled transfers | V16 | notifications & support |

> `spring.flyway.baseline-on-migrate=true` allows introducing Flyway onto a pre-existing DB.

---

## 🔤 Domain Enumerations

Key enums used across the API (see `domain/enums`):

- **AccountType** `SAVINGS, CURRENT` · **AccountStatus**
- **TransferMode** `WITHIN_BANK, IMPS, NEFT, RTGS, UPI` · **TransactionType/Status**
- **CardNetwork** `VISA, MASTERCARD, RUPAY` · **CardType/Status/Action**
- **LoanType** `HOME, CAR, EDUCATION, PERSONAL` · **LoanStatus** · **RepaymentType**
- **DepositType** `FIXED, RECURRING` · **DepositStatus**
- **ChequeBookStatus** · **ChequeStatus**
- **Gender** `MALE, FEMALE, OTHER` · **KycDocumentType** `PAN, AADHAAR, PASSPORT, DRIVING_LICENSE, ADDRESS_PROOF` · **KycStatus**
- **TicketCategory** `ACCOUNT, CARD, LOAN, DEPOSIT, TRANSACTION, KYC, TECHNICAL, GENERAL` · **TicketPriority** `LOW, MEDIUM, HIGH, URGENT` · **TicketStatus**
- **NotificationType/Channel** · **ScheduleFrequency/Status** · **BeneficiaryStatus**
- **UserStatus** `REQUESTED(0), ACTIVE(1), INACTIVE(2), REJECTED(4)`

Enums implement `CodedEnum` and are persisted via JPA converters as compact codes.

---

## 🧩 Registration Field Rules

The register form enforces the same validation as the backend `RegisterDto`:

| Field | Rule |
|-------|------|
| `name` | required, max 100 chars |
| `username` | 4–50 chars |
| `email` | valid email |
| `password` | min 8 chars |
| `aadhaarno` | exactly **12 digits** |
| `panno` | format `ABCDE1234F` (5 letters + 4 digits + 1 letter, uppercase) |
| `mobile` | **10 digits** starting with 6, 7, 8 or 9 |
| `address` | required |

---

## 🩺 Troubleshooting

| Symptom | Cause & Fix |
|---------|-------------|
| **400 on `/api/auth/register`** | Invalid PAN / mobile / Aadhaar format. Follow the [registration rules](#-registration-field-rules). |
| **Backend fails on startup — Flyway/JPA validate error** | Ensure the `bank_management` database exists and no manual schema drift. Let Flyway create tables. |
| **`Access denied` from MySQL** | Update `spring.datasource.username/password` to match your MySQL. |
| **Frontend calls fail with CORS/connection refused** | Confirm the backend is running on `:8080` before starting the SPA. |
| **`npm run dev` starts in the wrong directory** | Always run it from inside `Banking-Application-FrontEnd`. |
| **401 loops / logged out immediately** | Access token expired — the SPA redirects to `/login`; log in again to refresh. |
| **Port already in use** | Stop the process holding `:8080` (backend) or `:3000` (frontend), or change the port. |

---

## 📜 License

This project is provided for educational and demonstration purposes. Update this section with your chosen license before distribution.

---

<p align="center"><b>JantaBank</b> — Excellence Bank, MG Road · IFSC EXCB0002480</p>
