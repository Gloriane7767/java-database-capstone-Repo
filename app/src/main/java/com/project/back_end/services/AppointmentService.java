package com.project.back_end.services;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.project.back_end.DTO.AppointmentDTO;
import com.project.back_end.models.Appointment;
import com.project.back_end.repo.AppointmentRepository;
import com.project.back_end.repo.DoctorRepository;
import com.project.back_end.repo.PatientRepository;

@Service
public class AppointmentService {

    private static final Logger logger = LoggerFactory.getLogger(AppointmentService.class);

    private final AppointmentRepository appointmentRepository;
    private final com.project.back_end.services.Service service;
    private final TokenService tokenService;
    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;

    public AppointmentService(AppointmentRepository appointmentRepository,
                               com.project.back_end.services.Service service,
                               TokenService tokenService,
                               PatientRepository patientRepository,
                               DoctorRepository doctorRepository) {
        this.appointmentRepository = appointmentRepository;
        this.service = service;
        this.tokenService = tokenService;
        this.patientRepository = patientRepository;
        this.doctorRepository = doctorRepository;
    }

    @Transactional
    public int bookAppointment(Appointment appointment) {
        try {
            appointmentRepository.save(appointment);
            return 1;
        } catch (Exception e) {
            logger.error("Error booking appointment: {}", e.getMessage());
            return 0;
        }
    }

    @Transactional
    public ResponseEntity<Map<String, String>> updateAppointment(Appointment appointment, Long patientId) {
        Map<String, String> response = new HashMap<>();
        try {
            Optional<Appointment> existingOpt = appointmentRepository.findById(appointment.getId());
            if (existingOpt.isEmpty()) {
                response.put("error", "Appointment not found");
                return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
            }

            Appointment existing = existingOpt.get();
            if (!existing.getPatient().getId().equals(patientId)) {
                response.put("error", "This appointment does not belong to the requesting patient");
                return new ResponseEntity<>(response, HttpStatus.FORBIDDEN);
            }

            int availability = service.validateAppointment(
                    appointment.getDoctor().getId(), appointment.getAppointmentTime());
            if (availability == -1) {
                response.put("error", "Doctor not found");
                return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
            }
            if (availability == 0) {
                response.put("error", "Requested time slot is not available");
                return new ResponseEntity<>(response, HttpStatus.CONFLICT);
            }

            existing.setDoctor(appointment.getDoctor());
            existing.setAppointmentTime(appointment.getAppointmentTime());
            appointmentRepository.save(existing);

            response.put("message", "Appointment updated successfully");
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Error updating appointment: {}", e.getMessage());
            response.put("error", "Internal server error");
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Transactional
    public ResponseEntity<Map<String, String>> cancelAppointment(Long appointmentId, Long patientId) {
        Map<String, String> response = new HashMap<>();
        try {
            Optional<Appointment> existingOpt = appointmentRepository.findById(appointmentId);
            if (existingOpt.isEmpty()) {
                response.put("error", "Appointment not found");
                return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
            }

            Appointment existing = existingOpt.get();
            if (!existing.getPatient().getId().equals(patientId)) {
                response.put("error", "This appointment does not belong to the requesting patient");
                return new ResponseEntity<>(response, HttpStatus.FORBIDDEN);
            }

            appointmentRepository.delete(existing);
            response.put("message", "Appointment cancelled successfully");
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Error cancelling appointment: {}", e.getMessage());
            response.put("error", "Internal server error");
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Transactional
    public Map<String, Object> getAppointments(Long doctorId, LocalDate date, String patientName) {
        Map<String, Object> response = new HashMap<>();
        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.atTime(23, 59, 59);

        List<Appointment> appointments;
        if (patientName == null || patientName.trim().isEmpty()) {
            appointments = appointmentRepository.findByDoctorIdAndAppointmentTimeBetween(doctorId, startOfDay, endOfDay);
        } else {
            appointments = appointmentRepository
                    .findByDoctorIdAndPatient_NameContainingIgnoreCaseAndAppointmentTimeBetween(
                            doctorId, patientName, startOfDay, endOfDay);
        }

        response.put("appointments", toDtoList(appointments));
        return response;
    }

    @Transactional
    public void changeStatus(Long appointmentId, int status) {
        appointmentRepository.updateStatus(status, appointmentId);
    }

    private List<AppointmentDTO> toDtoList(List<Appointment> appointments) {
        List<AppointmentDTO> dtos = new ArrayList<>();
        for (Appointment a : appointments) {
            dtos.add(new AppointmentDTO(
                    a.getId(),
                    a.getDoctor() != null ? a.getDoctor().getId() : null,
                    a.getDoctor() != null ? a.getDoctor().getName() : null,
                    a.getPatient() != null ? a.getPatient().getId() : null,
                    a.getPatient() != null ? a.getPatient().getName() : null,
                    a.getPatient() != null ? a.getPatient().getEmail() : null,
                    a.getPatient() != null ? a.getPatient().getPhone() : null,
                    a.getPatient() != null ? a.getPatient().getAddress() : null,
                    a.getAppointmentTime(),
                    a.getStatus()
            ));
        }
        return dtos;
    }
}