var autoRefreshIntervalId = null;
const dateTimeFormatter = JSJoda.DateTimeFormatter.ofPattern('HH:mm')

let demoDataId = null;
let scheduleId = null;
let loadedSchedule = null;

$(document).ready(function () {
    replaceQuickstartTimefoldAutoHeaderFooter();

    $("#solveButton").click(function () {
        solve();
    });
    $("#stopSolvingButton").click(function () {
        stopSolving();
    });

    setupAjax();
    fetchDemoData();
});

function setupAjax() {
    $.ajaxSetup({
        headers: {
            'Content-Type': 'application/json',
            'Accept': 'application/json,text/plain', // plain text is required by solve() returning UUID of the solver job
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
}

function fetchDemoData() {
    $.get("/demo-data", function (data) {
        data.forEach(item => {
            $("#testDataButton").append($('<a id="' + item + 'TestData" class="dropdown-item" href="#">' + item + '</a>'));

            $("#" + item + "TestData").click(function () {
                switchDataDropDownItemActive(item);
                scheduleId = null;
                demoDataId = item;

                refreshSchedule();
            });
        });

        // load first data set
        demoDataId = data[0];
        switchDataDropDownItemActive(demoDataId);
        refreshSchedule();
    }).fail(function (xhr, ajaxOptions, thrownError) {
        // disable this page as there is no data
        $("#demo").empty();
        $("#demo").html("<h1><p align=\"center\">No test data available</p></h1>")
    });
}

function switchDataDropDownItemActive(newItem) {
    activeCssClass = "active";
    $("#testDataButton > a." + activeCssClass).removeClass(activeCssClass);
    $("#" + newItem + "TestData").addClass(activeCssClass);
}

function refreshSchedule() {
  let path = "/timetables/" + scheduleId;
  if (scheduleId === null) {
    if (demoDataId === null) {
      alert("Please select a test data set.");
      return;
    }

    path = "/demo-data/" + demoDataId;
  }

  $.getJSON(path, function (schedule) {
    loadedSchedule = schedule;
	renderSchedule(schedule);
  })
  .fail(function (xhr, ajaxOptions, thrownError) {
      showError("Getting the timetable has failed.", xhr);
      refreshSolvingButtons(false);
  });
}

function renderSchedule(timetable) {
    refreshSolvingButtons(timetable.solverStatus != null && timetable.solverStatus !== "NOT_SOLVING");
    $("#score").text("Score: " + (timetable.score == null ? "?" : timetable.score));

    const timetableByRoom = $("#timetableByRoom");
    timetableByRoom.children().remove();
    const timetableByTeacher = $("#timetableByTeacher");
    timetableByTeacher.children().remove();
    const timetableByStudentGroup = $("#timetableByStudentGroup");
    timetableByStudentGroup.children().remove();
    const unassignedLessons = $("#unassignedLessons");
    unassignedLessons.children().remove();

    const theadByRoom = $("<thead>").appendTo(timetableByRoom);
    const headerRowByRoom = $("<tr>").appendTo(theadByRoom);
    headerRowByRoom.append($("<th>Timeslot</th>"));

    $.each(timetable.rooms, (index, room) => {
      headerRowByRoom
        .append($("<th/>")
          .append($("<span/>").text(room.name))
          .append($(`<button type="button" class="ms-2 mb-1 btn btn-light btn-sm p-1"/>`)));
    });
    const theadByTeacher = $("<thead>").appendTo(timetableByTeacher);
    const headerRowByTeacher = $("<tr>").appendTo(theadByTeacher);
    headerRowByTeacher.append($("<th>Timeslot</th>"));
    const teacherList = [...new Set(timetable.lessons.map(lesson => lesson.teacher))];
    $.each(teacherList, (index, teacher) => {
      headerRowByTeacher
        .append($("<th/>")
          .append($("<span/>").text(teacher)));
    });
    const theadByStudentGroup = $("<thead>").appendTo(timetableByStudentGroup);
    const headerRowByStudentGroup = $("<tr>").appendTo(theadByStudentGroup);
    headerRowByStudentGroup.append($("<th>Timeslot</th>"));
    const studentGroupList = [...new Set(timetable.lessons.map(lesson => lesson.studentGroup))];
    $.each(studentGroupList, (index, studentGroup) => {
      headerRowByStudentGroup
        .append($("<th/>")
          .append($("<span/>").text(studentGroup)));
    });

    const tbodyByRoom = $("<tbody>").appendTo(timetableByRoom);
    const tbodyByTeacher = $("<tbody>").appendTo(timetableByTeacher);
    const tbodyByStudentGroup = $("<tbody>").appendTo(timetableByStudentGroup);

    const LocalTime = JSJoda.LocalTime;

    $.each(timetable.timeslots, (index, timeslot) => {
      const rowByRoom = $("<tr>").appendTo(tbodyByRoom);
      rowByRoom
        .append($(`<th class="align-middle"/>`)
          .append($("<span/>").text(`
                    ${timeslot.dayOfWeek.charAt(0) + timeslot.dayOfWeek.slice(1).toLowerCase()}
                    ${LocalTime.parse(timeslot.startTime).format(dateTimeFormatter)}
                    -
                    ${LocalTime.parse(timeslot.endTime).format(dateTimeFormatter)}
                `)));
      $.each(timetable.rooms, (index, room) => {
        rowByRoom.append($("<td/>").prop("id", `timeslot${timeslot.id}room${room.id}`));
      });

      const rowByTeacher = $("<tr>").appendTo(tbodyByTeacher);
      rowByTeacher
        .append($(`<th class="align-middle"/>`)
          .append($("<span/>").text(`
                    ${timeslot.dayOfWeek.charAt(0) + timeslot.dayOfWeek.slice(1).toLowerCase()}
                    ${LocalTime.parse(timeslot.startTime).format(dateTimeFormatter)}
                    -
                    ${LocalTime.parse(timeslot.endTime).format(dateTimeFormatter)}
                `)));
      $.each(teacherList, (index, teacher) => {
        rowByTeacher.append($("<td/>").prop("id", `timeslot${timeslot.id}teacher${convertToId(teacher)}`));
      });

      const rowByStudentGroup = $("<tr>").appendTo(tbodyByStudentGroup);
      rowByStudentGroup
        .append($(`<th class="align-middle"/>`)
          .append($("<span/>").text(`
                    ${timeslot.dayOfWeek.charAt(0) + timeslot.dayOfWeek.slice(1).toLowerCase()}
                    ${LocalTime.parse(timeslot.startTime).format(dateTimeFormatter)}
                    -
                    ${LocalTime.parse(timeslot.endTime).format(dateTimeFormatter)}
                `)));
      $.each(studentGroupList, (index, studentGroup) => {
        rowByStudentGroup.append($("<td/>").prop("id", `timeslot${timeslot.id}studentGroup${convertToId(studentGroup)}`));
      });
    });

    $.each(timetable.lessons, (index, lesson) => {
      const color = pickColor(lesson.subject);
      const lessonElement = $(`<div class="card" style="background-color: ${color}"/>`)
        .append($(`<div class="card-body p-2"/>`)
          .append($(`<h5 class="card-title mb-1"/>`).text(lesson.subject))
          .append($(`<p class="card-text ms-2 mb-1"/>`)
            .append($(`<em/>`).text(`by ${lesson.teacher}`)))
          .append($(`<small class="ms-2 mt-1 card-text text-muted align-bottom float-end"/>`).text(lesson.id))
          .append($(`<p class="card-text ms-2"/>`).text(lesson.studentGroup)));
      if (lesson.timeslot == null || lesson.room == null) {
        unassignedLessons.append($(`<div class="col"/>`).append(lessonElement));
      } else {
        // In the JSON, the lesson.timeslot and lesson.room are only IDs of these objects.
        $(`#timeslot${lesson.timeslot}room${lesson.room}`).append(lessonElement.clone());
        $(`#timeslot${lesson.timeslot}teacher${convertToId(lesson.teacher)}`).append(lessonElement.clone());
        $(`#timeslot${lesson.timeslot}studentGroup${convertToId(lesson.studentGroup)}`).append(lessonElement.clone());
      }
    });
}

function solve() {
  $.post("/timetables", JSON.stringify(loadedSchedule), function (data) {
    scheduleId = data;
    refreshSolvingButtons(true);
  }).fail(function (xhr, ajaxOptions, thrownError) {
    showError("Start solving failed.", xhr);
    refreshSolvingButtons(false);
  },
  "text");
}

function refreshSolvingButtons(solving) {
  if (solving) {
    $("#solveButton").hide();
    $("#stopSolvingButton").show();
    if (autoRefreshIntervalId == null) {
      autoRefreshIntervalId = setInterval(refreshSchedule, 2000);
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
  $.delete("/timetables/" + scheduleId, function () {
    refreshSolvingButtons(false);
    refreshSchedule();
  }).fail(function (xhr, ajaxOptions, thrownError) {
    showError("Stop solving failed.", xhr);
  });
}

function convertToId(str) {
  // Base64 encoding without padding to avoid XSS
  return btoa(str).replace(/=/g, "");
}

function copyTextToClipboard(id) {
  var text = $("#" + id).text().trim();

  var dummy = document.createElement("textarea");
  document.body.appendChild(dummy);
  dummy.value = text;
  dummy.select();
  document.execCommand("copy");
  document.body.removeChild(dummy);
}

// TODO: move to the webjar
function replaceQuickstartTimefoldAutoHeaderFooter() {
  const timefoldHeader = $("header#timefold-auto-header");
  if (timefoldHeader != null) {
    timefoldHeader.addClass("bg-black")
    timefoldHeader.append(
      $(`<div class="container-fluid">
        <nav class="navbar sticky-top navbar-expand-lg navbar-dark shadow mb-3">
          <a class="navbar-brand" href="https://timefold.ai">
            <img src="/webjars/timefold/img/timefold-logo-horizontal-negative.svg" alt="Timefold logo" width="200">
          </a>
          <button class="navbar-toggler" type="button" data-toggle="collapse" data-target="#navbarNav" aria-controls="navbarNav" aria-expanded="false" aria-label="Toggle navigation">
            <span class="navbar-toggler-icon"></span>
          </button>
          <div class="collapse navbar-collapse" id="navbarNav">
            <ul class="nav nav-pills">
              <li class="nav-item active" id="navUIItem">
                <button class="nav-link active" id="navUI" data-bs-toggle="pill" data-bs-target="#demo" type="button">Demo UI</button>
              </li>
              <li class="nav-item" id="navRestItem">
                <button class="nav-link" id="navRest" data-bs-toggle="pill" data-bs-target="#rest" type="button">Guide</button>
              </li>
              <li class="nav-item" id="navOpenApiItem">
                <button class="nav-link" id="navOpenApi" data-bs-toggle="pill" data-bs-target="#openapi" type="button">REST API</button>
              </li>
            </ul>
          </div>
          <div class="ms-auto">
              <div class="dropdown">
                  <button class="btn btn-secondary dropdown-toggle" type="button" id="dropdownMenuButton" data-bs-toggle="dropdown" aria-haspopup="true" aria-expanded="false">
                      Data
                  </button>
                  <div id="testDataButton" class="dropdown-menu" aria-labelledby="dropdownMenuButton"></div>
              </div>
          </div>
        </nav>
      </div>`));
  }

  const timefoldFooter = $("footer#timefold-auto-footer");
      if (timefoldFooter != null) {
        timefoldFooter.append(
          $(`<footer class="bg-black text-white-50">
               <div class="container">
                 <div class="hstack gap-3 p-4">
                   <div class="ms-auto"><a class="text-white" href="https://timefold.ai">Timefold</a></div>
                   <div class="vr"></div>
                   <div><a class="text-white" href="https://timefold.ai/docs">Documentation</a></div>
                   <div class="vr"></div>
                   <div><a class="text-white" href="https://github.com/TimefoldAI/timefold-quickstarts">Code</a></div>
                   <div class="vr"></div>
                   <div class="me-auto"><a class="text-white" href="https://timefold.ai/product/support/">Support</a></div>
                 </div>
               </div>
             </footer>`));
      }
}