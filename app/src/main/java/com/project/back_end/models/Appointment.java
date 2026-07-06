package com.project.back_end.models;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Transient;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;

@Entity
public class Appointment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Loaded lazily since an appointment is usually fetched without needing
    // the full doctor object immediately.
    @NotNull(message = "Doctor is required")
    @ManyToOne(fetch = FetchType.LAZY)
    private Doctor doctor;

    @NotNull(message = "Patient is required")
    @ManyToOne(fetch = FetchType.LAZY)
    private Patient patient;

    @NotNull(message = "Appointment time is required")
    @Future(message = "Appointment time must be in the future")
    private LocalDateTime appointmentTime;

    @NotNull(message = "Status is required")
    @Enumerated(EnumType.STRING)
    private Status status;

    public enum Status {
        SCHEDULED,
        COMPLETED,
        CANCELLED
    }

    public Appointment() {
    }

    public Appointment(Doctor doctor, Patient patient, LocalDateTime appointmentTime, Status status) {
        this.doctor = doctor;
        this.patient = patient;
        this.appointmentTime = appointmentTime;
        this.status = status;
    }

    // Standard appointment slot length. Extracted as a constant so it's easy
    // to change in one place if slot duration policy ever changes.
    private static final long DEFAULT_DURATION_MINUTES = 60;

    /**
     * Returns the calculated end time of the appointment, assuming a fixed
     * 60-minute slot. Not persisted — derived on demand from appointmentTime.
     */
    @Transient
    @JsonIgnore
    public LocalDateTime getEndTime() {
        if (appointmentTime == null) {
            return null;
        }
        return appointmentTime.plusMinutes(DEFAULT_DURATION_MINUTES);
    }

    /**
     * Convenience helper returning just the date portion of the appointment,
     * useful for grouping/filtering appointments by day.
     */
    @Transient
    @JsonIgnore
    public java.time.LocalDate getAppointmentDate() {
        return appointmentTime == null ? null : appointmentTime.toLocalDate();
    }

    /**
     * Convenience helper returning just the time-of-day portion of the
     * appointment.
     */
    @Transient
    @JsonIgnore
    public java.time.LocalTime getAppointmentTimeOnly() {
        return appointmentTime == null ? null : appointmentTime.toLocalTime();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Doctor getDoctor() {
        return doctor;
    }

    public void setDoctor(Doctor doctor) {
        this.doctor = doctor;
    }

    public Patient getPatient() {
        return patient;
    }

    public void setPatient(Patient patient) {
        this.patient = patient;
    }

    public LocalDateTime getAppointmentTime() {
        return appointmentTime;
    }

    public void setAppointmentTime(LocalDateTime appointmentTime) {
        this.appointmentTime = appointmentTime;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }
}
