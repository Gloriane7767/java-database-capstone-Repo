-- stored_procedures.sql
-- Stored procedures for the Smart Clinic System (cms database)

USE cms;

DELIMITER //

-- ============================================================
-- 1. GetDailyAppointmentReportByDoctor
-- Returns every appointment on a given date, along with the
-- doctor and patient names, ordered by doctor and time.
-- ============================================================
DROP PROCEDURE IF EXISTS GetDailyAppointmentReportByDoctor //

CREATE PROCEDURE GetDailyAppointmentReportByDoctor(IN report_date DATE)
BEGIN
    SELECT
        d.id AS doctor_id,
        d.name AS doctor_name,
        a.id AS appointment_id,
        p.id AS patient_id,
        p.name AS patient_name,
        a.appointment_time,
        a.status
    FROM appointment a
    JOIN doctor d ON a.doctor_id = d.id
    JOIN patient p ON a.patient_id = p.id
    WHERE DATE(a.appointment_time) = report_date
    ORDER BY d.name, a.appointment_time;
END //

-- ============================================================
-- 2. GetDoctorWithMostPatientsByMonth
-- Returns the doctor(s) with the most DISTINCT patients seen
-- in a given month/year.
-- ============================================================
DROP PROCEDURE IF EXISTS GetDoctorWithMostPatientsByMonth //

CREATE PROCEDURE GetDoctorWithMostPatientsByMonth(IN report_month INT, IN report_year INT)
BEGIN
    SELECT
        d.id AS doctor_id,
        d.name AS doctor_name,
        COUNT(DISTINCT a.patient_id) AS distinct_patient_count
    FROM appointment a
    JOIN doctor d ON a.doctor_id = d.id
    WHERE MONTH(a.appointment_time) = report_month
      AND YEAR(a.appointment_time) = report_year
    GROUP BY d.id, d.name
    ORDER BY distinct_patient_count DESC
    LIMIT 1;
END //

-- ============================================================
-- 3. GetDoctorWithMostPatientsByYear
-- Same as above, but for an entire year.
-- ============================================================
DROP PROCEDURE IF EXISTS GetDoctorWithMostPatientsByYear //

CREATE PROCEDURE GetDoctorWithMostPatientsByYear(IN report_year INT)
BEGIN
    SELECT
        d.id AS doctor_id,
        d.name AS doctor_name,
        COUNT(DISTINCT a.patient_id) AS distinct_patient_count
    FROM appointment a
    JOIN doctor d ON a.doctor_id = d.id
    WHERE YEAR(a.appointment_time) = report_year
    GROUP BY d.id, d.name
    ORDER BY distinct_patient_count DESC
    LIMIT 1;
END //

DELIMITER ;