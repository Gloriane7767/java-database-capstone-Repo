package com.project.back_end.models;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Entity;
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

    @NotNull(message = "Doctor is required")
    @ManyToOne(fetch = FetchType.LAZY)
    private Doctor doctor;

    @NotNull(message = "Patient is required")
    @ManyToOne(fetch = FetchType.LAZY)
    private Patient patient;

    @NotNull(message = "Appointment time is required")
    @Future(message = "Appointment time must be in the future")
    private LocalDateTime appointmentTime;

    // 0 = Scheduled, 1 = Completed, 2 = Cancelled
    private int status;

    public Appointment() {
    }

    public Appointment(Doctor doctor, Patient patient, LocalDateTime appointmentTime, int status) {
        this.doctor = doctor;
        this.patient = patient;
        this.appointmentTime = appointmentTime;
        this.status = status;
    }

    private static final long DEFAULT_DURATION_MINUTES = 60;

    @Transient
    @JsonIgnore
    public LocalDateTime getEndTime() {
        if (appointmentTime == null) {
            return null;
        }
        return appointmentTime.plusMinutes(DEFAULT_DURATION_MINUTES);
    }

    @Transient
    @JsonIgnore
    public java.time.LocalDate getAppointmentDate() {
        return appointmentTime == null ? null : appointmentTime.toLocalDate();
    }

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

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }
}