## The architecture has five layers:
1. Client tier — the Thymeleaf-based dashboards (Admin, Doctor) and the REST-facing modules (Appointments, PatientDashboard, PatientRecord).
2. Controller tier, inside the Spring Boot app — Thymeleaf controllers serve the dashboard pages, REST controllers handle JSON API calls.
3. Service layer — a single shared layer holding the business logic, called by both controller types.
4. Repository tier — MySQL repositories (JPA-based) and a MongoDB repository (driver-based), used by the service layer depending on the data type.
5. Database tier — the MySQL database (relational data: Patient, Doctor, Appointment, Admin) and the MongoDB database (document data: Prescription).
