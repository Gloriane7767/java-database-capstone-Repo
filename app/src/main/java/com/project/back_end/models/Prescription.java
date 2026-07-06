package com.project.back_end.models;

import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Document(collection = "prescriptions")
public class Prescription {

    @Id
    private String id;

    // Stored as a plain name rather than a reference, since prescriptions are
    // read back as standalone documents and don't need a live join to the
    // relational Patient table for typical use cases (e.g. printing a
    // prescription slip).
    @NotNull(message = "Patient name is required")
    @Size(min = 2, max = 100, message = "Patient name must be between 2 and 100 characters")
    private String patientName;

    @NotNull(message = "At least one medication is required")
    @Size(min = 1, message = "Medication list must not be empty")
    private List<String> medication;

    @Size(max = 500, message = "Dosage details must be at most 500 characters")
    private String dosage;

    // Links back to the relational Appointment this prescription was issued
    // during, allowing the service layer to cross-reference MySQL data.
    @NotNull(message = "Appointment ID is required")
    private Long appointmentId;

    public Prescription() {
    }

    public Prescription(String patientName, List<String> medication, String dosage, Long appointmentId) {
        this.patientName = patientName;
        this.medication = medication;
        this.dosage = dosage;
        this.appointmentId = appointmentId;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPatientName() {
        return patientName;
    }

    public void setPatientName(String patientName) {
        this.patientName = patientName;
    }

    public List<String> getMedication() {
        return medication;
    }

    public void setMedication(List<String> medication) {
        this.medication = medication;
    }

    public String getDosage() {
        return dosage;
    }

    public void setDosage(String dosage) {
        this.dosage = dosage;
    }

    public Long getAppointmentId() {
        return appointmentId;
    }

    public void setAppointmentId(Long appointmentId) {
        this.appointmentId = appointmentId;
    }
}

