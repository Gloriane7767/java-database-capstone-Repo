// doctorDashboard.js
import { getAllAppointments } from "./services/appointmentRecordService.js";
import { createPatientRow } from "./components/patientRows.js";

const tableBody = document.getElementById("patientTableBody");

let selectedDate = new Date().toISOString().split("T")[0];
const token = localStorage.getItem("token");
let patientName = null;

document.getElementById("searchBar").addEventListener("input", (e) => {
  const value = e.target.value.trim();
  patientName = value.length > 0 ? value : "null";
  loadAppointments();
});

document.getElementById("todayButton").addEventListener("click", () => {
  selectedDate = new Date().toISOString().split("T")[0];
  document.getElementById("datePicker").value = selectedDate;
  loadAppointments();
});

document.getElementById("datePicker").addEventListener("change", (e) => {
  selectedDate = e.target.value;
  loadAppointments();
});

function loadAppointments() {
  getAllAppointments(selectedDate, patientName || "null", token)
    .then((data) => {
      const appointments = data.appointments || [];
      tableBody.innerHTML = "";

      if (appointments.length === 0) {
        tableBody.innerHTML = `<tr><td colspan="5">No Appointments found for today.</td></tr>`;
        return;
      }

      appointments.forEach((appointment) => {
        const patient = {
          id: appointment.patientId,
          name: appointment.patientName,
          phone: appointment.patientPhone,
          email: appointment.patientEmail,
        };
        const row = createPatientRow(patient, appointment.id, appointment.doctorId);
        tableBody.appendChild(row);
      });
    })
    .catch((error) => {
      console.error("Error loading appointments:", error);
      tableBody.innerHTML = `<tr><td colspan="5">Error loading appointments. Try again later.</td></tr>`;
    });
}

document.addEventListener("DOMContentLoaded", () => {
  renderContent();
  loadAppointments();
});