var loadingPorts = [];
var startedPorts = [];
var autoPingIntervalId = null;

const quickstarts = [
  {
    id: "vehicle-routing",
    title: "Vehicle routing",
    description: 'Find the most efficient routes for vehicles to reach customers, taking into account vehicle capacity and time windows when customers are available. Sometimes also called "CVRPTW".',
    icon: "fa-truck",
    screenshot: "screenshot/quarkus-vehicle-routing-screenshot.png"
  },
  {
    id: "maintenance-scheduling",
    title: "Maintenance scheduling",
    description: "Schedule maintenance jobs to crews over time to reduce both premature and overdue maintenance.",
    icon: "fa-tools",
    screenshot: "screenshot/quarkus-maintenance-scheduling-screenshot.png"
  },
  {
    id: "employee-scheduling",
    title: "Employee scheduling",
    description: "Schedule shifts to employees, accounting for employee availability and shift skill requirements.",
    icon: "fa-calendar",
    screenshot: "screenshot/quarkus-employee-scheduling-screenshot.png"
  },
  {
    id: "food-packaging",
    title: "Food packaging",
    description: "Generate the optimal schedule for your food packaging manufacturing lines.",
    icon: "fa-box",
    screenshot: "screenshot/quarkus-food-packaging-screenshot.png"
  },
  {
    id: "order-picking",
    title: "Order picking",
    description: "Generate an optimal picking plan for completing a set of orders.",
    icon: "fa-cart-plus",
    screenshot: "screenshot/quarkus-order-picking-screenshot.png"
  },
  {
    id: "school-timetabling",
    title: "School timetabling",
    description: "Assign lessons to timeslots and rooms to produce a better schedule for teachers and students.",
    icon: "fa-graduation-cap",
    screenshot: "screenshot/quarkus-school-timetabling-screenshot.png"
  },
  {
    id: "facility-location",
    title: "Facility location problem",
    description: "Pick the best geographical locations for new stores, distribution centers, covid test centers or telco masts.",
    icon: "fa-broadcast-tower",
    screenshot: "screenshot/quarkus-facility-location-screenshot.png"
  }
];

function initQuickstartCards() {
  const container = $('#quickstartCardsContainer');
  $.each(quickstarts, (index, quickstart) => {
    container.append(`
      <div class="col mb-4">
        <div class="card">
          <div class="card-header">
            <h5><i class="fas ${quickstart.icon}"></i> ${quickstart.title}</h5>
          </div>
          <img src="${quickstart.screenshot}" class="card-img-top mt-3" alt="Screenshot"/>
          <div class="card-body">
            <p class="card-text">${quickstart.description}</p>
            <button id="${quickstart.id}-launch" type="button" class="btn btn-primary w-100">
              <span class="fas fa-play"></span> Launch
            </button>
            <div id="${quickstart.id}-ports" class="row row-cols-1 row-cols-md-3 mt-3"></div>
          </div>
        </div>
      </div>
    `);
    $(`#${quickstart.id}-launch`).click(function () {
      launchQuickstart(quickstart.id);
    });
  });

  container.append(`
    <div class="col mb-4">
      <div class="card">
        <div class="card-header">Timefold examples</div>
        <img src="screenshot/timefold-solver-examples-screenshot.png" class="card-img-top mt-3" alt="Screenshot"/>
        <div class="card-body">
          <p class="card-text">Many examples in desktop technology.</p>
          <a href="https://timefold.ai" target="_blank">
            <button type="button" class="btn btn-secondary w-100">Download</button>
          </a>
        </div>
      </div>
    </div>
  `);
}

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

  $("#exit").click(function () {
    exit();
  });

  replaceTimefoldAutoHeaderFooter();
  initQuickstartCards();
  refreshQuickstartsPanel();
});
