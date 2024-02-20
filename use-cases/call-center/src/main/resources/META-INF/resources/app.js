const skillToColorMap = new Map([
  ['EN', '#edd400'],
  ['ES', '#ef2929'],
  ['DE', '#e9b96e'],
  ['Car insurance', '#ad7fa8'],
  ['Property insurance', '#729fcf'],
  ['Life insurance', '#73d216']
]);

const pinnedCallColor = '#ebfadc';
const waitingCallColor = 'White';

var autoRefreshIntervalId = null;
var solving = false;

function refresh() {
  $.getJSON("/call-center", (callCenterData) => {
    solving = callCenterData.solving;
    refreshSolvingButtons();
    $("#score").text("Score: " + (callCenterData.score == null ? "?" : callCenterData.score));
    printCallTable(callCenterData);
  });
}

function printCallTable(callCenterData) {
  const callTable = $('#callTable');
  callTable.children().remove();
  printHeader(callTable, 10);
  const tableBody = $('<tbody/>').appendTo(callTable);
  callCenterData.agents.forEach((agent) => {
    printAgent(tableBody, agent);
  });
}

function printHeader(callTable, calls) {
  const thead = $(`<thead/>`).appendTo(callTable);
  const headerRow = $(`<tr/>`).appendTo(thead);
  headerRow.append($(`<th style="width:10%;"><h2>Agent</h2></th>`));
  headerRow.append($(`<th colspan="' + calls + '" style="text-align:center"><h2>Incoming calls</h2></th>`))
}

function printAgent(tableBody, agent) {
  const tableRow = $('<tr class="agent-row"/>').appendTo(tableBody);
  const td = $('<td/>').appendTo(tableRow);
  const agentCard = $('<div class="card" style="background-color:#f7ecd5">').appendTo(td);
  const agentCardBody = $('<div class="card-body p-2"/>')
    .append($(`<h5 class="card-title mb-2"><i class="fas fa-user-alt me-1"></i> ${agent.name}</h5>`));

  printSkills(agentCardBody, agent.skills);
  agentCardBody.appendTo(agentCard);

  const callsTd = $(`<td/>`).appendTo(tableRow);
  const callsHstack = $(`<div class="hstack gap-1"/>`).appendTo(callsTd);

  agent.calls.forEach((call) => {
    printCall(callsHstack, call);
  });
}

function printCall(callsHstack, call) {
  const callColor = (call.pinned) ? pinnedCallColor : waitingCallColor;

  const callCard = $(`<div class="card" style="width: 14rem; background-color: ${callColor}"/>`);
  const callButtons = $(`<div class="float-end"/>`);
  callButtons.append($(`<div><button class="btn btn-sm btn-outline-danger"><i class="fas fa-phone-slash"></i></button></div>`)
    .click(() => removeCall(call)));
  const callCardBody = $(`<div class="card-body p-2"/>`)
    .append(callButtons)
    .append($(`<h5 class="card-title mb-2"/>)`)
      .append((call.pinned) ? $(`<i class="fas fa-phone-volume me-1"></i>`) : $(`<i class="fas fa-phone me-1"></i>`))
      .append(call.phoneNumber))
    .append();

  printTimes(callCardBody, callButtons, call);
  printSkills(callCardBody, call.requiredSkills);
  callCard.append(callCardBody);
  callCard.appendTo(callsHstack);
}

function printTimes(callCard, callButtons, call) {
  const LocalTime = JSJoda.LocalTime;
  const Duration = JSJoda.Duration;

  const startedTime = LocalTime.parse(call.startTime);
  if (call.pinned) {
    const pickedUpTime = LocalTime.parse(call.pickUpTime);
    const waitingTillPickedUpTime = formatDuration(Duration.between(startedTime, pickedUpTime));
    const inProgressTime = formatDuration(Duration.between(LocalTime.parse(call.pickUpTime), LocalTime.now()));
    $(`<p class="card-text mb-1" style="font-size:0.8em">Waiting: ${waitingTillPickedUpTime}</p>`).appendTo(callCard);
    $(`<p class="card-text mb-1" style="font-size:0.8em">In progress: ${inProgressTime}</p>`).appendTo(callCard);

    callButtons.append($(`<div class="mt-1"><button class="btn btn-sm btn-outline-primary py-0 px-1">+ 1m</button></div>`).click(() => prolongCall(call)));
  } else {
    const waiting = formatDuration(Duration.between(startedTime, LocalTime.now()));
    const estimatedWaiting = formatDuration(Duration.ofSeconds(Math.floor(call.estimatedWaiting)));
    $(`<p class="card-text mb-1" style="font-size:0.8em">Waiting: ${waiting}</p>`).appendTo(callCard);
    $(`<p class="card-text mb-1" style="font-size:0.8em">Estimated waiting: ${estimatedWaiting}</p>`)
      .appendTo(callCard);
  }
}

function formatDuration(duration) {
  const hours = Math.floor(duration.seconds() / 3600);
  const minutes = Math.floor((duration.seconds() % 3600) / 60);
  const seconds = duration.seconds() % 60;
  var formattedDuration = '';
  if (hours > 0) {
    formattedDuration += hours + 'h ';
  }
  formattedDuration += minutes + 'm ' + seconds + 's';
  return formattedDuration;
}

function printSkills(container, skills) {
  const skillRow = $(`<div/>`);
  container.append(skillRow);
  skills.forEach((skill) => {
    let color = skillToColorMap.get(skill);
    skillRow.append($(`<span class="badge me-1 mt-1" style="background-color:${color}">${skill}</span>`));
  });
}

function solve() {
  $.post('/call-center/solve', null, () => {
      solving = true;
      refreshSolvingButtons();
    }).fail((xhr, ajaxOptions, thrownError) => {
      showError("Start solving failed.", xhr);
    },
    "text");
}

function stopSolving() {
  $.post('/call-center/stop', null, () => {
      solving = false;
      refreshSolvingButtons();
      refresh();
    }).fail((xhr, ajaxOptions, thrownError) => {
      showError("Stop solving failed.", xhr);
    },
    "text");
}

function refreshSolvingButtons() {
  if (solving) {
    $("#solveButton").hide();
    $("#stopSolvingButton").show();
    if (autoRefreshIntervalId == null) {
      autoRefreshIntervalId = setInterval(refresh, 1000);
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

function removeCall(call) {
  $.delete("/call/" + call.id, null, () => {
      refresh();
    }).fail((xhr, ajaxOptions, thrownError) => {
      showError('Cancelling a call (' + call.phoneNumber + ') failed.', xhr);
    },
    "text");
}

function prolongCall(call) {
  $.put("/call/" + call.id, null, () => {
      refresh();
    }).fail((xhr, ajaxOptions, thrownError) => {
      showError('Prolonging a call (' + call.phoneNumber + ') failed.', xhr);
    },
    "text");
}

function restartSimulation(frequency, duration) {
  $.put('/simulation', JSON.stringify({'frequency': frequency, 'duration': duration}), () => {
  }).fail((xhr, ajaxOptions, thrownError) => {
      showError('Updating simulation parameters (frequency:' + frequency + ', duration:' + duration + ') failed.', xhr);
    },
    "text");
}

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

$(document).ready(function () {
  replaceTimefoldAutoHeaderFooter();

  setupAjax();

  $('#solveButton').click(solve);

  $('#stopSolvingButton').click(function () {
    stopSolving();
  });

  const callFrequencyRange = $('#callFrequencyRange');
  const callFrequencyValue = $('#callFrequencyValue');
  const callLengthRange = $('#callLengthRange');
  const callLengthValue = $('#callLengthValue');

  callFrequencyRange.on('change', function () {
    callFrequencyValue.html(callFrequencyRange.val());
    restartSimulation(callFrequencyRange.val(), callLengthRange.val());
  });
  callFrequencyRange.on('input', function () {
    callFrequencyValue.html(callFrequencyRange.val());
  });
  callFrequencyValue.html(callFrequencyRange.val());

  callLengthRange.on('change', function () {
    callLengthValue.html(callLengthRange.val());
    restartSimulation(callFrequencyRange.val(), callLengthRange.val());
  });
  callLengthRange.on('input', function () {
    callLengthValue.html(callLengthRange.val());
  });
  callLengthValue.html(callLengthRange.val());

  // Make sure the values are propagated to the server.
  restartSimulation(callFrequencyRange.val(), callLengthRange.val());

  refresh();
});
