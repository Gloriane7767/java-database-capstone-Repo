
// adminDashboard.js
import { openModal } from "./components/modals.js";
import { createDoctorCard } from "./components/doctorCard.js";
import { getDoctors, filterDoctors, saveDoctor } from "./services/doctorServices.js";

document.addEventListener("DOMContentLoaded", () => {
  const addDocBtn = document.getElementById("addDocBtn");
  if (addDocBtn) {
    addDocBtn.addEventListener("click", () => {
      openModal("addDoctor");
    });
  }

  loadDoctorCards();

  const searchBar = document.getElementById("searchBar");
  const filterTime = document.getElementById("filterTime");
  const filterSpecialty = document.getElementById("filterSpecialty");

  if (searchBar) searchBar.addEventListener("input", filterDoctorsOnChange);
  if (filterTime) filterTime.addEventListener("change", filterDoctorsOnChange);
  if (filterSpecialty) filterSpecialty.addEventListener("change", filterDoctorsOnChange);
});

function loadDoctorCards() {
  getDoctors()
    .then((doctors) => {
      const contentDiv = document.getElementById("content");
      contentDiv.innerHTML = "";
      doctors.forEach((doctor) => {
        const card = createDoctorCard(doctor);
        contentDiv.appendChild(card);
      });
    })
    .catch((error) => {
      console.error("Failed to load doctors:", error);
    });
}

function filterDoctorsOnChange() {
  const searchBarValue = document.getElementById("searchBar").value.trim();
  const filterTimeValue = document.getElementById("filterTime").value;
  const filterSpecialtyValue = document.getElementById("filterSpecialty").value;

  const name = searchBarValue.length > 0 ? searchBarValue : null;
  const time = filterTimeValue.length > 0 ? filterTimeValue : null;
  const specialty = filterSpecialtyValue.length > 0 ? filterSpecialtyValue : null;

  filterDoctors(name, time, specialty)
    .then((response) => {
      const doctors = response.doctors;
      const contentDiv = document.getElementById("content");
      contentDiv.innerHTML = "";

      if (doctors && doctors.length > 0) {
        renderDoctorCards(doctors);
      } else {
        contentDiv.innerHTML = "<p>No doctors found with the given filters.</p>";
      }
    })
    .catch((error) => {
      console.error("Failed to filter doctors:", error);
      alert("An error occurred while filtering doctors.");
    });
}

function renderDoctorCards(doctors) {
  const contentDiv = document.getElementById("content");
  contentDiv.innerHTML = "";
  doctors.forEach((doctor) => {
    const card = createDoctorCard(doctor);
    contentDiv.appendChild(card);
  });
}

window.adminAddDoctor = async function () {
  const name = document.getElementById("doctorName").value;
  const email = document.getElementById("doctorEmail").value;
  const phone = document.getElementById("doctorPhone").value;
  const password = document.getElementById("doctorPassword").value;
  const specialty = document.getElementById("specialization").value;

  const checkboxes = document.querySelectorAll('input[name="availability"]:checked');
  const availableTimes = Array.from(checkboxes).map((cb) => cb.value);

  const token = localStorage.getItem("token");
  if (!token) {
    alert("Session expired. Please log in again.");
    return;
  }

  const doctor = {
    name,
    email,
    phone,
    password,
    specialty,
    availableTimes,
  };

  try {
    const { success, message } = await saveDoctor(doctor, token);
    if (success) {
      alert("Doctor added successfully.");
      document.getElementById("modal").style.display = "none";
      window.location.reload();
    } else {
      alert("Failed to add doctor: " + message);
    }
  } catch (error) {
    console.error("Error adding doctor:", error);
    alert("An error occurred while adding the doctor.");
  }
};