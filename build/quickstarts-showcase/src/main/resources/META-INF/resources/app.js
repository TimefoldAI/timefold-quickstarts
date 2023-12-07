var loadingPorts = [];
var startedPorts = [];
var autoPingIntervalId = null;

function refreshQuickstartsPanel() {
  $.getJSON("/quickstart", function (quickstarts) {
    $.each(quickstarts, (index, quickstart) => {
      const quickstartPorts = $("#" + quickstart.id + "-ports");
      quickstartPorts.children().remove();
      $.each(quickstart.ports, (index, port) => {
        var started = startedPorts.includes(port);
        if (!started && !loadingPorts.includes(port)) {
          loadingPorts.push(port);
        }
        quickstartPorts
          .append($(`<div class="col mb-4"/>`).append($(`<div class="card m-0"/>`)
            .append($(`<div class="card-header"/>`).text("Port " + port))
            .append($(`<button type="button" class="btn ${started ? "btn-success" : "btn-secondary"} m-2" id="showPort${port}"/>`)
              .append($(`<span class="fas fa-play"/>`))
              .text(started ? "Show" : "Loading...")
              .click(() => window.open("//localhost:" + port, '_blank')))
            .append($(`<button type="button" class="btn btn-danger mb-2 ms-2 me-2"/>`)
              .append($(`<span class="fas fa-stop"/>`))
              .text("Stop")
              .click(() => stopQuickstart(quickstart.id, port)))));
      });
    });
    if (autoPingIntervalId == null && loadingPorts.length > 0) {
      autoPingIntervalId = setInterval(pingLoadingPorts, 1000);
    }
  });
}

function pingLoadingPorts() {
  var newLoadingPorts = [];
  console.log("Pinging ports...");
  for (const port of loadingPorts) {
    $.ajax({
      url: "//localhost:" + port,
      type: "HEAD",
      timeout: 1000,
      statusCode: {
        200: function (response) {
          console.log("  Port " + port + " has started.");
          let button = $(`#showPort${port}`);
          button.addClass("btn-success");
          button.removeClass("btn-secondary");
          button.text("Show");
          startedPorts.push(port);
        },
        400: function (response) {
          console.log("  Port " + port + " is still loading.");
          loadingPorts.push(port);
          if (autoPingIntervalId == null) {
            autoPingIntervalId = setInterval(pingLoadingPorts, 1000);
          }
        },
        0: function (response) {
          console.log("  Port " + port + " is still loading.");
          loadingPorts.push(port);
          if (autoPingIntervalId == null) {
            autoPingIntervalId = setInterval(pingLoadingPorts, 1000);
          }
        }
      }
    });
  }
  loadingPorts = [];
  if (autoPingIntervalId != null) {
    clearInterval(autoPingIntervalId);
    autoPingIntervalId = null;
  }
}

function launchQuickstart(quickstartId) {
  $.post("/quickstart/" + quickstartId + "/launch", function () {
    refreshQuickstartsPanel();
  }).fail(function (xhr, ajaxOptions, thrownError) {
    showError("Launching quickstart (" + quickstartId + ") failed.", xhr);
  });
}

function stopQuickstart(quickstartId, port) {
  $.delete("/quickstart/" + quickstartId + "/stop/" + port, function () {
    refreshQuickstartsPanel();
  }).fail(function (xhr, ajaxOptions, thrownError) {
    showError("Stopping quickstart (" + quickstartId + ") on port (" + port + ") failed.", xhr);
  });
}

function exit() {
  $.post("/exit", function () {
    $("#content").hide();
    $("#exit-alert").removeClass("d-none");
    $("#exit").attr("disabled", "");
  }).fail(function (xhr, ajaxOptions, thrownError) {
    showError("Exit failed.", xhr);
  });
}

$(document).ready(function () {
  replaceTimefoldAutoHeaderFooter();
  $.ajaxSetup({
    headers: {
      'Content-Type': 'application/json',
      'Accept': 'application/json'
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
  $("#school-timetabling-launch").click(function () {
    launchQuickstart("school-timetabling");
  });
  $("#facility-location-launch").click(function () {
    launchQuickstart("facility-location");
  });
  $("#maintenance-scheduling-launch").click(function () {
    launchQuickstart("maintenance-scheduling");
  });
  $("#call-center-launch").click(function () {
      launchQuickstart("call-center");
  });
  $("#vaccination-scheduling-launch").click(function () {
    launchQuickstart("vaccination-scheduling");
  });
  $("#vehicle-routing-capacity-launch").click(function () {
    launchQuickstart("vehicle-routing-capacity");
  });
  $("#vehicle-routing-time-windows-launch").click(function () {
    launchQuickstart("vehicle-routing-time-windows");
  });
  $("#order-picking-launch").click(function () {
    launchQuickstart("order-picking");
  });
  $("#employee-scheduling-launch").click(function () {
    launchQuickstart("employee-scheduling");
  });
  $("#food-packaging-launch").click(function () {
    launchQuickstart("food-packaging");
  });
  $("#exit").click(function () {
    exit();
  });

  refreshQuickstartsPanel();
});
