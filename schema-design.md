# Schema Design — Smart Clinic System

This document defines the data schema for the Smart Clinic System, covering both the relational (MySQL) data and the document-based (MongoDB) data used by the application.

## Why this split?

The system has two kinds of data with very different shapes:

- **Structured, relational data** — patients, doctors, appointments, and admin accounts. These have a fixed, predictable set of fields, clear relationships to one another (a doctor has many appointments, a patient has many appointments), and benefit from strong constraints (uniqueness, foreign keys, referential integrity). This maps naturally to **MySQL**.
- **Flexible, document-style data** — prescriptions, which can have a variable number of medications, dosage instructions, and notes per visit. Forcing this into rigid relational tables (e.g. a separate `medications` table with a strict schema) adds complexity without much benefit, since the internal shape of a prescription can vary and doesn't need to be queried relationally in the same way appointments do. This maps naturally to **MongoDB**.

---

## MySQL database design

### 1. `patients`

| Column | Type | Constraints |
|---|---|---|
| id | INT | PRIMARY KEY, AUTO_INCREMENT |
| first_name | VARCHAR(50) | NOT NULL |
| last_name | VARCHAR(50) | NOT NULL |
| email | VARCHAR(100) | NOT NULL, UNIQUE |
| phone | VARCHAR(15) | NOT NULL |
| password_hash | VARCHAR(255) | NOT NULL |
| date_of_birth | DATE | NOT NULL |
| gender | ENUM('male','female','other') | NOT NULL |
| address | VARCHAR(255) | NULL |
| created_at | TIMESTAMP | NOT NULL, DEFAULT CURRENT_TIMESTAMP |

**Notes:**
- `email` is unique because it's used as the login identifier, and duplicate accounts would break authentication.
- `phone` is stored as `VARCHAR` rather than a numeric type, since phone numbers can include leading zeros, `+` country codes, or formatting characters that a numeric type would strip or reject.
- `password_hash` stores a hashed password (e.g. bcrypt) — the raw password is never persisted.

### 2. `doctors`

| Column | Type | Constraints |
|---|---|---|
| id | INT | PRIMARY KEY, AUTO_INCREMENT |
| first_name | VARCHAR(50) | NOT NULL |
| last_name | VARCHAR(50) | NOT NULL |
| email | VARCHAR(100) | NOT NULL, UNIQUE |
| phone | VARCHAR(15) | NOT NULL |
| password_hash | VARCHAR(255) | NOT NULL |
| specialty | VARCHAR(100) | NOT NULL |
| license_number | VARCHAR(50) | NOT NULL, UNIQUE |
| available_from | TIME | NOT NULL |
| available_to | TIME | NOT NULL |
| created_at | TIMESTAMP | NOT NULL, DEFAULT CURRENT_TIMESTAMP |

**Notes:**
- `license_number` is unique since two doctors cannot legally share a medical license number; this also protects against duplicate/fraudulent doctor accounts.
- `available_from` / `available_to` model a doctor's general daily working window; specific booked slots are derived from this range minus existing appointments, rather than storing every open slot as a row.

### 3. `admins`

| Column | Type | Constraints |
|---|---|---|
| id | INT | PRIMARY KEY, AUTO_INCREMENT |
| username | VARCHAR(50) | NOT NULL, UNIQUE |
| password_hash | VARCHAR(255) | NOT NULL |
| email | VARCHAR(100) | NOT NULL, UNIQUE |
| role | ENUM('super_admin','staff_admin') | NOT NULL, DEFAULT 'staff_admin' |
| created_at | TIMESTAMP | NOT NULL, DEFAULT CURRENT_TIMESTAMP |

**Notes:**
- `role` distinguishes between a super admin (full system access, e.g. managing other admins) and a staff admin (day-to-day operations), so permission checks in the service layer can branch on this field.

### 4. `appointments`

| Column | Type | Constraints |
|---|---|---|
| id | INT | PRIMARY KEY, AUTO_INCREMENT |
| patient_id | INT | NOT NULL, FOREIGN KEY → patients(id) |
| doctor_id | INT | NOT NULL, FOREIGN KEY → doctors(id) |
| appointment_time | DATETIME | NOT NULL |
| status | ENUM('scheduled','completed','cancelled') | NOT NULL, DEFAULT 'scheduled' |
| reason | VARCHAR(255) | NULL |
| created_at | TIMESTAMP | NOT NULL, DEFAULT CURRENT_TIMESTAMP |

**Constraints:**
- `UNIQUE (doctor_id, appointment_time)` — prevents double-booking the same doctor at the same time.
- `FOREIGN KEY (patient_id) REFERENCES patients(id) ON DELETE CASCADE` — if a patient record is removed, their appointment history is removed with it.
- `FOREIGN KEY (doctor_id) REFERENCES doctors(id) ON DELETE RESTRICT` — a doctor cannot be deleted while they still have


