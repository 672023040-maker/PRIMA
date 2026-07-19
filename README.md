# PRIMA - Point of Sale System

Program Integrasi Manajemen Aplikasi (PRIMA) adalah sistem Point of Sale (POS) untuk bisnis makanan dan minuman.

## Tech Stack

| Komponen | Teknologi |
|----------|-----------|
| **Android** | Kotlin + Jetpack Compose + Retrofit + MVVM |
| **Server** | Node.js + Express.js |
| **Database** | PostgreSQL |
| **Auth** | JWT (JSON Web Token) |

## Fitur

- Login dengan role-based access (Kasir, Admin, Owner)
- Katalog produk dengan kategori
- Transaksi dengan keranjang belanja
- Multiple metode pembayaran (Tunai, Kartu, E-Wallet)
- Laporan transaksi dan total pendapatan

---

## Setup Server

### Prerequisites

- [Node.js](https://nodejs.org/) (v18+)
- [PostgreSQL](https://www.postgresql.org/) (v12+)

### 1. Install Dependencies

```bash
cd server
npm install
```

### 2. Buat Database

Buka **pgAdmin** atau **psql** lalu jalankan:

```sql
CREATE DATABASE prima_db;
```

### 3. Jalankan Migrations

```bash
psql -U postgres -d prima_db -f migrations/001_create_tables.sql
psql -U postgres -d prima_db -f migrations/002_seed_data.sql
```

> Ganti `postgres` dengan username PostgreSQL Anda jika berbeda.

### 4. Konfigurasi Environment

```bash
cd server
copy .env.example .env    # Windows
# atau
cp .env.example .env      # Linux/Mac
```

Edit file `.env` sesuaikan:

```
DB_HOST=localhost
DB_PORT=5432
DB_NAME=prima_db
DB_USER=postgres          # sesuaikan
DB_PASSWORD=your_password # sesuaikan
JWT_SECRET=your_secret_here
PORT=3000
```

### 5. Jalankan Server

```bash
# Development mode (auto-restart)
npm run dev

# Atau production mode
npm start
```

Server berjalan di `http://localhost:3000`

---

## Setup Android

### Prerequisites

- [Android Studio](https://developer.android.com/studio) (latest)
- Android Emulator (API 24+ / Android 7.0+)

### 1. Buka Project

Buka folder `D:\PRIMA` di Android Studio.

### 2. Sync Gradle

Tunggu Gradle sync selesai.

### 3. Jalankan Server

Pastikan server sudah jalan (lihat Setup Server di atas).

### 4. Jalankan App

Run di Android Emulator. App akan menghubungkan ke `http://10.0.2.2:3000` (localhost dari emulator).

---

## Test Accounts

| Role | Email | Password |
|------|-------|----------|
| Kasir | kasir@prima.com | kasir123 |
| Admin | admin@prima.com | admin123 |
| Owner | owner@prima.com | owner123 |

---

## API Endpoints

| Method | Endpoint | Auth | Deskripsi |
|--------|----------|------|-----------|
| POST | `/api/login` | No | Login |
| POST | `/api/logout` | Yes | Logout |
| GET | `/api/products` | Yes | Daftar produk |
| GET | `/api/categories` | Yes | Daftar kategori |
| GET | `/api/payment-methods` | Yes | Daftar metode pembayaran |
| POST | `/api/transactions` | Yes | Buat transaksi baru |
| POST | `/api/transactions/:id/details` | Yes | Tambah item ke transaksi |
| POST | `/api/transactions/:id/complete` | Yes | Selesaikan transaksi |
| GET | `/api/transactions` | Yes | Daftar transaksi |

---

## Struktur Project

```
PRIMA/
├── app/                          # Android App
│   └── src/main/java/com/example/prima/
│       ├── api/                  # Retrofit + Models
│       ├── data/                 # Repository + Session
│       ├── viewmodel/            # ViewModels
│       └── ui/                   # Compose Screens
│
└── server/                       # Node.js Server
    ├── config/                   # Database config
    ├── middleware/                # Auth middleware
    ├── models/                   # Database models
    ├── routes/                   # API routes
    └── migrations/               # SQL migrations
```

## License

MIT
