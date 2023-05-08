var autoRefreshIntervalId = null;
const dateTimeFormatter = JSJoda.DateTimeFormatter.ofPattern('HH:mm')

$(document).ready(function () {
  replaceTimefoldAutoHeaderFooter();
  $.ajaxSetup({
    headers: {
      'Content-Type': 'application/json',
      'Accept': 'application/json'
    }
  });
  // TODO remove edit and delete functionality of test data in UI
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
    refreshTimeTable();
  });
  $("#solveButton").click(function () {
    solve();
  });
  $("#stopSolvingButton").click(function () {
    stopSolving();
  });
  $("#addLessonSubmitButton").click(function () {
    addLesson();
  });
  $("#addTimeslotSubmitButton").click(function () {
    addTimeslot();
  });
  $("#addRoomSubmitButton").click(function () {
    addRoom();
  });

  refreshTimeTable();
});

function refreshTimeTable() {
  $.getJSON("/timeTable", function (timeTable) {
    refreshSolvingButtons(timeTable.solverStatus != null && timeTable.solverStatus !== "NOT_SOLVING");
    $("#score").text("Score: " + (timeTable.score == null ? "?" : timeTable.score));

    const timeTableByRoom = $("#timeTableByRoom");
    timeTableByRoom.children().remove();
    const timeTableByTeacher = $("#timeTableByTeacher");
    timeTableByTeacher.children().remove();
    const timeTableByStudentGroup = $("#timeTableByStudentGroup");
    timeTableByStudentGroup.children().remove();
    const unassignedLessons = $("#unassignedLessons");
    unassignedLessons.children().remove();

    const theadByRoom = $("<thead>").appendTo(timeTableByRoom);
    const headerRowByRoom = $("<tr>").appendTo(theadByRoom);
    headerRowByRoom.append($("<th>Timeslot</th>"));
    $.each(timeTable.roomList, (index, room) => {
      headerRowByRoom
        .append($("<th/>")
          .append($("<span/>").text(room.name))
          .append($(`<button type="button" class="ms-2 mb-1 btn btn-light btn-sm p-1"/>`)
            .append($(`<small class="fas fa-trash"/>`)
            ).click(() => deleteRoom(room))));
    });
    const theadByTeacher = $("<thead>").appendTo(timeTableByTeacher);
    const headerRowByTeacher = $("<tr>").appendTo(theadByTeacher);
    headerRowByTeacher.append($("<th>Timeslot</th>"));
    const teacherList = [...new Set(timeTable.lessonList.map(lesson => lesson.teacher))];
    $.each(teacherList, (index, teacher) => {
      headerRowByTeacher
        .append($("<th/>")
          .append($("<span/>").text(teacher)));
    });
    const theadByStudentGroup = $("<thead>").appendTo(timeTableByStudentGroup);
    const headerRowByStudentGroup = $("<tr>").appendTo(theadByStudentGroup);
    headerRowByStudentGroup.append($("<th>Timeslot</th>"));
    const studentGroupList = [...new Set(timeTable.lessonList.map(lesson => lesson.studentGroup))];
    $.each(studentGroupList, (index, studentGroup) => {
      headerRowByStudentGroup
        .append($("<th/>")
          .append($("<span/>").text(studentGroup)));
    });

    const tbodyByRoom = $("<tbody>").appendTo(timeTableByRoom);
    const tbodyByTeacher = $("<tbody>").appendTo(timeTableByTeacher);
    const tbodyByStudentGroup = $("<tbody>").appendTo(timeTableByStudentGroup);

    const LocalTime = JSJoda.LocalTime;

    $.each(timeTable.timeslotList, (index, timeslot) => {
      const rowByRoom = $("<tr>").appendTo(tbodyByRoom);
      rowByRoom
        .append($(`<th class="align-middle"/>`)
          .append($("<span/>").text(`
                    ${timeslot.dayOfWeek.charAt(0) + timeslot.dayOfWeek.slice(1).toLowerCase()}
                    ${LocalTime.parse(timeslot.startTime).format(dateTimeFormatter)}
                    -
                    ${LocalTime.parse(timeslot.endTime).format(dateTimeFormatter)}
                `)
            .append($(`<button type="button" class="ms-2 mb-1 btn btn-light btn-sm p-1"/>`)
              .append($(`<small class="fas fa-trash"/>`)
              ).click(() => deleteTimeslot(timeslot)))));
      $.each(timeTable.roomList, (index, room) => {
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

    $.each(timeTable.lessonList, (index, lesson) => {
      const color = pickColor(lesson.subject);
      const lessonElementWithoutDelete = $(`<div class="card" style="background-color: ${color}"/>`)
        .append($(`<div class="card-body p-2"/>`)
          .append($(`<h5 class="card-title mb-1"/>`).text(lesson.subject))
          .append($(`<p class="card-text ms-2 mb-1"/>`)
            .append($(`<em/>`).text(`by ${lesson.teacher}`)))
          .append($(`<small class="ms-2 mt-1 card-text text-muted align-bottom float-end"/>`).text(lesson.id))
          .append($(`<p class="card-text ms-2"/>`).text(lesson.studentGroup)));
      const lessonElement = lessonElementWithoutDelete.clone();
      lessonElement.find(".card-body").prepend(
        $(`<button type="button" class="ms-2 btn btn-light btn-sm p-1 float-end"/>`)
          .append($(`<small class="fas fa-trash"/>`)
          ).click(() => deleteLesson(lesson))
      );
      if (lesson.timeslot == null || lesson.room == null) {
        unassignedLessons.append($(`<div class="col"/>`).append(lessonElement));
      } else {
        $(`#timeslot${lesson.timeslot.id}room${lesson.room.id}`).append(lessonElement);
        $(`#timeslot${lesson.timeslot.id}teacher${convertToId(lesson.teacher)}`).append(lessonElementWithoutDelete.clone());
        $(`#timeslot${lesson.timeslot.id}studentGroup${convertToId(lesson.studentGroup)}`).append(lessonElementWithoutDelete.clone());
      }
    });
  });
}

function convertToId(str) {
  // Base64 encoding without padding to avoid XSS
  return btoa(str).replace(/=/g, "");
}

function solve() {
  $.post("/timeTable/solve", function () {
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
      autoRefreshIntervalId = setInterval(refreshTimeTable, 2000);
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
  $.post("/timeTable/stopSolving", function () {
    refreshSolvingButtons(false);
    refreshTimeTable();
  }).fail(function (xhr, ajaxOptions, thrownError) {
    showError("Stop solving failed.", xhr);
  });
}

function addLesson() {
  var subject = $("#lesson_subject").val().trim();
  $.post("/lessons", JSON.stringify({
    "subject": subject,
    "teacher": $("#lesson_teacher").val().trim(),
    "studentGroup": $("#lesson_studentGroup").val().trim()
  }), function () {
    refreshTimeTable();
  }).fail(function (xhr, ajaxOptions, thrownError) {
    showError("Adding lesson (" + subject + ") failed.", xhr);
  });
  $('#lessonDialog').modal('toggle');
}

function deleteLesson(lesson) {
  $.delete("/lessons/" + lesson.id, function () {
    refreshTimeTable();
  }).fail(function (xhr, ajaxOptions, thrownError) {
    showError("Deleting lesson (" + lesson.name + ") failed.", xhr);
  });
}

function addTimeslot() {
  $.post("/timeslots", JSON.stringify({
    "dayOfWeek": $("#timeslot_dayOfWeek").val().trim().toUpperCase(),
    "startTime": $("#timeslot_startTime").val().trim(),
    "endTime": $("#timeslot_endTime").val().trim()
  }), function () {
    refreshTimeTable();
  }).fail(function (xhr, ajaxOptions, thrownError) {
    showError("Adding timeslot failed.", xhr);
  });
  $('#timeslotDialog').modal('toggle');
}

function deleteTimeslot(timeslot) {
  $.delete("/timeslots/" + timeslot.id, function () {
    refreshTimeTable();
  }).fail(function (xhr, ajaxOptions, thrownError) {
    showError("Deleting timeslot (" + timeslot.name + ") failed.", xhr);
  });
}

function addRoom() {
  var name = $("#room_name").val().trim();
  $.post("/rooms", JSON.stringify({
    "name": name
  }), function () {
    refreshTimeTable();
  }).fail(function (xhr, ajaxOptions, thrownError) {
    showError("Adding room (" + name + ") failed.", xhr);
  });
  $("#roomDialog").modal('toggle');
}

function deleteRoom(room) {
  $.delete("/rooms/" + room.id, function () {
    refreshTimeTable();
  }).fail(function (xhr, ajaxOptions, thrownError) {
    showError("Deleting room (" + room.name + ") failed.", xhr);
  });
}
