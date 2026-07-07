package com.project.back_end.services;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.project.back_end.models.Admin;
import com.project.back_end.models.Doctor;
import com.project.back_end.models.Patient;
import com.project.back_end.repo.AdminRepository;
import com.project.back_end.repo.DoctorRepository;
import com.project.back_end.repo.PatientRepository;

@org.springframework.stereotype.Service
public class Service {

    private final TokenService tokenService;
    private final AdminRepository adminRepository;
    private final DoctorRepository doctorRepository;
    private final PatientRepository patientRepository;
    private final DoctorService doctorService;
    private final PatientService patientService;

    public Service(TokenService tokenService,
                   AdminRepository adminRepository,
                   DoctorRepository doctorRepository,
                   PatientRepository patientRepository,
                   DoctorService doctorService,
                   PatientService patientService) {
        this.tokenService = tokenService;
        this.adminRepository = adminRepository;
        this.doctorRepository = doctorRepository;
        this.patientRepository = patientRepository;
        this.doctorService = doctorService;
        this.patientService = patientService;
    }

    public ResponseEntity<Map<String, String>> validateToken(String token, String role) {
        Map<String, String> response = new HashMap<>();
        boolean valid = tokenService.validateToken(token, role);
        if (!valid) {
            response.put("error", "Invalid or expired token");
            return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
        }
        response.put("message", "Token is valid");
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    public ResponseEntity<Map<String, String>> validateAdmin(String username, String password) {
        Map<String, String> response = new HashMap<>();
        try {
            Admin admin = adminRepository.findByUsername(username);
            if (admin == null) {
                response.put("error", "Admin not found");
                return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
            }
            if (!admin.getPassword().equals(password)) {
                response.put("error", "Invalid credentials");
                return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
            }
            String token = tokenService.generateToken(admin.getUsername());
            response.put("token", token);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            response.put("error", "Internal server error");
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public List<Doctor> filterDoctor(String name, String specialty, String amOrPm) {
        boolean hasName = name != null && !name.trim().isEmpty();
        boolean hasSpecialty = specialty != null && !specialty.trim().isEmpty();
        boolean hasTime = amOrPm != null && !amOrPm.trim().isEmpty();

        if (hasName && hasSpecialty && hasTime) {
            return doctorService.filterDoctorsByNameSpecilityandTime(name, specialty, amOrPm);
        } else if (hasName && hasSpecialty) {
            return doctorService.filterDoctorByNameAndSpecility(name, specialty);
        } else if (hasName && hasTime) {
            return doctorService.filterDoctorByNameAndTime(name, amOrPm);
        } else if (hasSpecialty && hasTime) {
            return doctorService.filterDoctorByTimeAndSpecility(specialty, amOrPm);
        } else if (hasName) {
            return doctorService.findDoctorByName(name);
        } else if (hasSpecialty) {
            return doctorService.filterDoctorBySpecility(specialty);
        } else if (hasTime) {
            return doctorService.filterDoctorsByTime(amOrPm);
        } else {
            return doctorService.getDoctors();
        }
    }

    public int validateAppointment(Long doctorId, LocalDateTime appointmentTime) {
        Optional<Doctor> doctorOpt = doctorRepository.findById(doctorId);
        if (doctorOpt.isEmpty()) {
            return -1;
        }

        LocalDate date = appointmentTime.toLocalDate();
        List<String> availableSlots = doctorService.getDoctorAvailability(doctorId, date);

        String requestedStart = appointmentTime.toLocalTime().format(DateTimeFormatter.ofPattern("HH:mm"));
        for (String slot : availableSlots) {
            String slotStart = slot.split("-")[0].trim();
            if (slotStart.equals(requestedStart)) {
                return 1;
            }
        }
        return 0;
    }

    public boolean validatePatient(Patient patient) {
        Patient existing = patientRepository.findByEmailOrPhone(patient.getEmail(), patient.getPhone());
        return existing == null;
    }

    public ResponseEntity<Map<String, String>> validatePatientLogin(String email, String password) {
        Map<String, String> response = new HashMap<>();
        try {
            Patient patient = patientRepository.findByEmail(email);
            if (patient == null) {
                response.put("error", "Patient not found");
                return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
            }
            if (!patient.getPassword().equals(password)) {
                response.put("error", "Invalid credentials");
                return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
            }
            String token = tokenService.generateToken(patient.getEmail());
            response.put("token", token);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            response.put("error", "Internal server error");
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public ResponseEntity<Map<String, Object>> filterPatient(String token, String condition, String doctorName) {
        Map<String, Object> response = new HashMap<>();
        String email = tokenService.extractEmail(token);
        if (email == null) {
            response.put("error", "Invalid or expired token");
            return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
        }

        Patient patient = patientRepository.findByEmail(email);
        if (patient == null) {
            response.put("error", "Patient not found");
            return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
        }

        boolean hasCondition = condition != null && !condition.trim().isEmpty();
        boolean hasDoctor = doctorName != null && !doctorName.trim().isEmpty();

        if (hasCondition && hasDoctor) {
            return patientService.filterByDoctorAndCondition(doctorName, condition, patient.getId());
        } else if (hasCondition) {
            return patientService.filterByCondition(condition, patient.getId());
        } else if (hasDoctor) {
            return patientService.filterByDoctor(doctorName, patient.getId());
        } else {
            return patientService.getPatientAppointment(patient.getId());
        }
    }
}