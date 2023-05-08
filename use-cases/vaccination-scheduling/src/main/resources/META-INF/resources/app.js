var autoRefreshIntervalId = null;
var vaccineCenterLeafletGroup = null;
var personLeafletGroup = null;

function refreshSolution() {
  $.getJSON("/vaccinationSchedule?page=0", function (schedule) {
    refreshSolvingButtons(schedule.solverStatus != null && schedule.solverStatus !== "NOT_SOLVING");
    $("#score").text("Score: " + (schedule.score == null ? "?" : schedule.score));

    const vaccineTypesDiv = $("#vaccineTypes");
    vaccineTypesDiv.children().remove();
    const vaccineTypeMap = new Map();
    schedule.vaccineTypeList.forEach((vaccineType) => {
      const color = pickColor(vaccineType.name);
      vaccineTypesDiv.append($(`<div class="col"/>`).append($(`<div class="card" style="background-color: ${color}"/>`)
          .append($(`<div class="card-body p-2"/>`)
            .append($(`<h5 class="card-title mb-0"/>`).text(vaccineType.name)))));
      vaccineTypeMap.set(vaccineType.name, vaccineType);
    });

    const scheduleTable = $("#scheduleTable");
    scheduleTable.children().remove();
    vaccineCenterLeafletGroup.clearLayers();
    const unassignedPeronsDiv = $("#unassignedPersons");
    unassignedPeronsDiv.children().remove();

    if (schedule.appointmentList.size > 10000) {
      scheduleTable.append($(`<p/>`)
        .text("There are " + schedule.appointmentList.size + " appointments. Too much data to show a schedule."));
      return;
    }


    const vaccinationCenterMap = new Map(
      schedule.vaccinationCenterList.map(vaccinationCenter => [vaccinationCenter.id, vaccinationCenter]));

    const dateTimeSet = new Set();
    const dateTimeList = [];
    const vaccinationCenterIdToBoothIdSetMap = new Map(
      schedule.vaccinationCenterList.map(vaccinationCenter => [vaccinationCenter.id, new Set()]));
    schedule.appointmentList.forEach((appointment) => {
      const dateTime = moment(appointment.dateTime, "YYYY,M,D,H,m");
      const dateTimeString = dateTime.format("YYYY MMM D HH:mm")
      if (!dateTimeSet.has(dateTimeString)) {
        dateTimeSet.add(dateTimeString);
        dateTimeList.push(dateTime);
      }
      vaccinationCenterIdToBoothIdSetMap.get(appointment.vaccinationCenter).add(appointment.boothId);
    });
    dateTimeList.sort((a, b) => a.unix() - b.unix());

    const thead = $("<thead>").appendTo(scheduleTable);
    const headerRow = $("<tr>").appendTo(thead);
    headerRow.append($("<th>Time</th>"));
    schedule.vaccinationCenterList.forEach((vaccinationCenter) => {
      const boothIdSet = vaccinationCenterIdToBoothIdSetMap.get(vaccinationCenter.id);
      boothIdSet.forEach((boothId) => {
        headerRow
          .append($("<th/>")
            .append($("<span/>").text(vaccinationCenter.name + (boothIdSet.size <= 1 ? "" : " booth " + boothId))));
      });
    });

    const appointmentMap = new Map(schedule.appointmentList
      .map(appointment => [moment(appointment.dateTime, "YYYY,M,D,H,m") + "/" + appointment.vaccinationCenter + "/" + appointment.boothId, appointment]));
    if (schedule.appointmentList.length !== appointmentMap.size) {
      var conflicts = schedule.appointmentList.length - appointmentMap.size;
      scheduleTable.append($(`<p class="badge badge-danger">There are ${conflicts} double bookings.</span>`));
    }
    const appointmentToPersonMap = new Map();
    schedule.personList.forEach((person) => {
      if (person.appointment != null) {
        appointmentToPersonMap.set(moment(person.appointment.dateTime, "YYYY,M,D,H,m") + "/" + person.appointment.vaccinationCenter + "/" + person.appointment.boothId, person);
      }
    });

    const tbody = $(`<tbody>`).appendTo(scheduleTable);
    var previousDateTime = null;
    dateTimeList.forEach((dateTime) => {
      const row = $(`<tr>`).appendTo(tbody);
      var showDate = (previousDateTime == null || !dateTime.isSame(previousDateTime, "day"));
      row
        .append($(`<th class="align-middle"/>`)
          .append($(`<span style="float: right"/>`).text(showDate ? dateTime.format("ddd MMM D HH:mm") : dateTime.format("HH:mm"))));
      previousDateTime = dateTime;
      schedule.vaccinationCenterList.forEach((vaccinationCenter) => {
        const boothIdSet = vaccinationCenterIdToBoothIdSetMap.get(vaccinationCenter.id);
        boothIdSet.forEach((boothId) => {
          var appointment = appointmentMap.get(dateTime + "/" + vaccinationCenter.id + "/" + boothId);
          if (appointment == null) {
            row.append($(`<td class="p-1"/>`));
          } else {
            const color = pickColor(appointment.vaccineType);
            var cardBody = $(`<div class="card-body pt-1 pb-1 px-2"/>`);
            const person = appointmentToPersonMap.get(dateTime + "/" + vaccinationCenter.id + "/" + boothId);
            if (person == null) {
              cardBody.append($(`<h5 class="card-title mb-0"/>`).text("Unassigned"));
            } else {
              var appointmentDateTime = moment(appointment.dateTime, "YYYY,M,D,H,m");
              var birthdate = moment(person.birthdate, "YYYY,M,D");
              var age = appointmentDateTime.diff(birthdate, 'years')
              cardBody.append($(`<h5 class="card-title mb-1"/>`)
                .text(person.name + " (" + age + ")"));
              const vaccineType = vaccineTypeMap.get(appointment.vaccineType);
              if (vaccineType.maximumAge != null && age > vaccineType.maximumAge) {
                cardBody.append($(`<p class="badge badge-danger mb-0"/>`).text(vaccineType.name + " maximum age is " + vaccineType.maximumAge));
              }
              if (person.requiredVaccineType != null
                && appointment.vaccineType !== person.requiredVaccineType) {
                cardBody.append($(`<p class="badge badge-danger ms-2 mb-0"/>`).text("Required vaccine is " + person.requiredVaccineType));
              }
              if (person.preferredVaccineType != null
                && appointment.vaccineType !== person.preferredVaccineType) {
                cardBody.append($(`<p class="badge badge-warning ms-2 mb-0"/>`).text("Preferred vaccine is " + person.preferredVaccineType));
              }
              if (person.requiredVaccinationCenter != null
                && appointment.vaccinationCenter !== person.requiredVaccinationCenter) {
                const requiredVaccinationCenter = vaccinationCenterMap.get(person.requiredVaccinationCenter);
                cardBody.append($(`<p class="badge badge-danger ms-2 mb-0"/>`).text("Required vaccination center is " + requiredVaccinationCenter.name));
              }
              if (person.preferredVaccinationCenter != null
                && appointment.vaccinationCenter !== person.preferredVaccinationCenter) {
                const preferredVaccinationCenter = vaccinationCenterMap.get(person.preferredVaccinationCenter);
                cardBody.append($(`<p class="badge badge-warning ms-2 mb-0"/>`).text("Preferred vaccination center is " + preferredVaccinationCenter.name));
              }
              if (person.readyDate != null) {
                var readyDate = moment(person.readyDate, "YYYY,M,D");
                var readyDateDiff = appointmentDateTime.diff(readyDate, 'days');
                if (readyDateDiff < 0) {
                  cardBody.append($(`<p class="badge badge-danger ms-2 mb-0"/>`).text("Dose is " + (-readyDateDiff) + " days too early"));
                }
              }
              if (person.dueDate != null) {
                var dueDate = moment(person.dueDate, "YYYY,M,D");
                var dueDateDiff = appointmentDateTime.diff(dueDate, 'days');
                if (dueDateDiff > 0) {
                  cardBody.append($(`<p class="badge badge-danger ms-2 mb-0"/>`).text("Dose is " + (dueDateDiff) + " days too late"));
                }
              }
              var dosePrefix = person.doseNumber.toString() + ((person.doseNumber === 1) ? "st" : "nd");
              var doseSuffix = "";
              if (person.idealDate != null) {
                var idealDate = moment(person.idealDate, "YYYY,M,D");
                var idealDateDiff = appointmentDateTime.diff(idealDate, 'days');
                doseSuffix = " (" + (idealDateDiff === 0 ? "ideal day"
                    : (idealDateDiff < 0 ? (-idealDateDiff) + " days too early"
                    : idealDateDiff + " days too late")) + ")";
              }
              cardBody.append($(`<p class="card-text ms-2 mb-0"/>`).text(dosePrefix + " dose" + doseSuffix));
            }
            row.append($(`<td class="p-1"/>`)
              .append($(`<div class="card" style="background-color: ${color}"/>`)
                .append(cardBody)));
          }
        });
      });
    });


    schedule.vaccinationCenterList.forEach((vaccinationCenter) => {
      L.marker(vaccinationCenter.location).addTo(vaccineCenterLeafletGroup);
    });
    var assignedPersonCount = 0;
    var unassignedPersonCount = 0;
    personLeafletGroup.clearLayers();
    schedule.personList.forEach((person) => {
      const appointment = person.appointment;
      const personColor = (appointment == null ? "gray" : pickColor(appointment.vaccineType));
      L.circleMarker(person.homeLocation, {radius: 4, color: personColor, weight: 2}).addTo(personLeafletGroup);
      if (person.requiredVaccineType != null) {
        const requiredVaccineTypeColor = pickColor(person.requiredVaccineType);
        L.circleMarker(person.homeLocation, {radius: 3, color: requiredVaccineTypeColor, weight: 0, fillOpacity: 1.0}).addTo(personLeafletGroup);
      }
      if (appointment != null) {
        assignedPersonCount++;
        const vaccinationCenter = vaccinationCenterMap.get(appointment.vaccinationCenter);
        L.polyline([person.homeLocation, vaccinationCenter.location], {color: personColor, weight: 1}).addTo(personLeafletGroup);
      } else {
        unassignedPersonCount++;
        var firstDateTime = dateTimeList[0];
        var birthdate = moment(person.birthdate, "YYYY,M,D");
        var age = firstDateTime.diff(birthdate, 'years');

        var dosePrefix = person.doseNumber.toString() + ((person.doseNumber === 1) ? "st" : "nd");
        var doseSuffix = "";
        if (person.requiredVaccineType != null) {
          const vaccineType = vaccineTypeMap.get(person.requiredVaccineType);
          doseSuffix += " " + vaccineType.name;
        }
        if (person.idealDate != null) {
          doseSuffix += " (ideally " + moment(person.idealDate, "YYYY,M,D").format("ddd MMM D") + ")";
        }
        unassignedPeronsDiv.append($(`<div class="col"/>`).append($(`<div class="card"/>`)
            .append($(`<div class="card-body pt-1 pb-1 px-2"/>`)
              .append($(`<h5 class="card-title mb-1"/>`).text(person.name + " (" + age + ")"))
              .append($(`<p class="card-text ms-2"/>`).text(dosePrefix + " dose" + doseSuffix)))));
      }
    });
    $("#assignedPersonCount").text(assignedPersonCount);
    $("#unassignedPersonCount").text(unassignedPersonCount);
  });
}

function solve() {
  $.post("/vaccinationSchedule/solve", function () {
    refreshSolvingButtons(true);
  }).fail(function (xhr, ajaxOptions, thrownError) {
    showError("Start solving failed.", xhr);
  });
}

function refreshSolvingButtons(solving) {
  if (solving) {
    $("#solveButton").hide();
    $("#stopSolvingButton").show();
    if (autoRefreshIntervalId == null) {
      autoRefreshIntervalId = setInterval(refreshSolution, 2000);
    }
  } else {
    $("#solveButton").show();
    $("#stopSolvingButton").hide();
    if (autoRefreshIntervalId != null) {
      clearInterval(autoRefreshIntervalId);
      autoRefreshIntervalId = null;
    }
  }
}

function stopSolving() {
  $.post("/vaccinationSchedule/stopSolving", function () {
    refreshSolvingButtons(false);
    refreshSolution();
  }).fail(function (xhr, ajaxOptions, thrownError) {
    showError("Stop solving failed.", xhr);
  });
}

$(document).ready(function () {
  replaceTimefoldAutoHeaderFooter();
  $.ajaxSetup({
    headers: {
      "Content-Type": "application/json",
      "Accept": "application/json"
    }
  });
  // Extend jQuery to support $.put() and $.delete()
  jQuery.each(["put", "delete"], function (i, method) {
    jQuery[method] = function (url, data, callback, type) {
      if (jQuery.isFunction(data)) {
        type = type || callback;
        callback = data;
        data = undefined;
      }
      return jQuery.ajax({
        url: url,
        type: method,
        dataType: type,
        data: data,
        success: callback
      });
    };
  });

  $("#refreshButton").click(function () {
    refreshSolution();
  });
  $("#solveButton").click(function () {
    solve();
  });
  $("#stopSolvingButton").click(function () {
    stopSolving();
  });

  const leafletMap = L.map("leafletMap", {doubleClickZoom: false})
    .setView([33.75, -84.40], 10);

  L.tileLayer("https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png", {
    maxZoom: 19,
    attribution: '&copy; <a href="https://www.openstreetmap.org/">OpenStreetMap</a> contributors',
  }).addTo(leafletMap);
  $(`button[data-bs-toggle="tab"]`).on("shown.bs.tab", function (e) {
    leafletMap.invalidateSize();
  })

  vaccineCenterLeafletGroup = L.layerGroup();
  vaccineCenterLeafletGroup.addTo(leafletMap);
  personLeafletGroup = L.layerGroup();
  personLeafletGroup.addTo(leafletMap);

  refreshSolution();
});
