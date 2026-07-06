1. User accesses AdminDashboard or DoctorDashboard pages, or interacts with the Appointments, PatientDashboard, or PatientRecord modules via the JSON API.
2. The action is routed to the appropriate Thymeleaf Controller (for dashboard pages) or REST Controller (for API calls) within the SpringBootApp.
 3. The controller calls the Service Layer, which handles the core business logic.
4. The Service Layer uses the MySQL Repositories or the MongoDB Repository depending on the type of data needed.
5. The MySQL Repositories access the MySQL Database to retrieve or store relational data.
6.  The MongoDB Repository accesses the MongoDB Database, returning data as a MongoDB Model (e.g., Prescription).
7. The MySQL Database data is mapped through JPA Entities (Patient, Doctor, Appointment, Admin) into MySQL Models used by the application.

   
