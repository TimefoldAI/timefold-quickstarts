const colors = [
  'aqua',
  'aquamarine',
  'blue',
  'blueviolet',
  'chocolate',
  'cornflowerblue',
  'crimson',
  'forestgreen',
  'gold',
  'lawngreen',
  'limegreen',
  'maroon',
  'mediumvioletred',
  'orange',
  'slateblue',
  'tomato',
];

let autoRefreshCount = 0;
let autoRefreshIntervalId = null;

let initialized = false;
const depotByIdMap = new Map();
const customerByIdMap = new Map();

const solveButton = $('#solveButton');
const stopSolvingButton = $('#stopSolvingButton');
const vehiclesTable = $('#vehicles');
const depotsTable = $('#depots');

const colorById = (i) => colors[i % colors.length];
const colorByVehicle = (vehicle) => vehicle === null ? null : colorById(vehicle.id);
const colorByDepot = (depot) => depot === null ? null : colorById(depot.id);

const defaultIcon = new L.Icon.Default();
const greyIcon = new L.Icon({
  iconUrl: 'https://raw.githubusercontent.com/pointhi/leaflet-color-markers/master/img/marker-icon-grey.png',
  shadowUrl: 'https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.6.0/images/marker-shadow.png',
  iconSize: [25, 41],
  iconAnchor: [12, 41],
  popupAnchor: [1, -34],
  shadowSize: [41, 41],
});

let testData = null;
let scheduleId = null;
let loadedSchedule = null;
const map = L.map('map', { doubleClickZoom: false }).setView([51.505, -0.09], 13);
const customerGroup = L.layerGroup().addTo(map);
const depotGroup = L.layerGroup().addTo(map);
const routeGroup = L.layerGroup().addTo(map);

$(document).ready(function () {
    replaceQuickstartTimefoldAutoHeaderFooter();

    L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
      maxZoom: 19,
      attribution: '&copy; <a href="https://www.openstreetmap.org/">OpenStreetMap</a> contributors',
    }).addTo(map);

    solveButton.click(solve);
    stopSolvingButton.click(stopSolving);
    refreshSolvingButtons(false);

    $('[data-bs-toggle="tooltip"]').tooltip();

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

    //initMenu();
    createDataSets();
});

const formatDistance = (distanceInMeters) => `${Math.floor(distanceInMeters / 1000)}km ${distanceInMeters % 1000}m`;

function solve() {
  $.post("/route-plans", JSON.stringify(loadedSchedule), function (data) {
    scheduleId = data;
    refreshSolvingButtons(true);

    //map.whenReady(getStatus);

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
      autoRefreshIntervalId = setInterval(refreshRoutePlan, 2000);
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

function refreshRoutePlan() {
  let path = "/route-plans/" + scheduleId;
  if (scheduleId === null) {
    if (testData === null) {
      alert("Please select a test data set.");
      return;
    }

    path = "/demo/datasets/" + testData;
  }

  $.getJSON(path, function (schedule) {
    loadedSchedule = schedule;
    refreshSolvingButtons(schedule.solverStatus != null && schedule.solverStatus !== "NOT_SOLVING");
	renderRoutes(schedule);
  })
  .fail(function (xhr, ajaxOptions, thrownError) {
      showError("Getting timetable has failed.", xhr);
      refreshSolvingButtons(false);
  });
}

function stopSolving() {
  $.delete("/route-plans/" + scheduleId, function () {
    refreshSolvingButtons(false);
    refreshRoutePlan();
  }).fail(function (xhr, ajaxOptions, thrownError) {
    showError("Stop solving failed.", xhr);
  });
}

const formatErrorResponseBody = (body) => {
  // JSON must not contain \t (Quarkus bug)
  const json = JSON.parse(body.replace(/\t/g, '  '));
  return `${json.details}\n${json.stack}`;
};

const handleErrorResponse = (title, response) => {
  return response.text()
    .then((body) => {
      const message = `${title} (${response.status}: ${response.statusText}).`;
      const stackTrace = body ? formatErrorResponseBody(body) : '';
      showError(message, stackTrace);
    });
};

const handleClientError = (title, error) => {
  console.error(error);
  showError(`${title}.`,
    // Stack looks differently in Chrome and Firefox.
    error.stack.startsWith(error.name)
      ? error.stack
      : `${error.name}: ${error.message}\n    ${error.stack.replace(/\n/g, '\n    ')}`);
};

const depotPopupContent = (depot, color) => `<h5>Depot ${depot.id}</h5>
<ul class="list-unstyled">
<li><span style="background-color: ${color}; display: inline-block; width: 12px; height: 12px; text-align: center">
</span> ${color}</li>
</ul>`;

const customerPopupContent = (customer) => `<h5>Customer ${customer.id}</h5>
Demand: ${customer.demand}`;

const getDepotMarker = ({ id, location }) => {
  let marker = depotByIdMap.get(id);
  if (marker) {
    return marker;
  }
  marker = L.marker(location);
  marker.addTo(depotGroup).bindPopup();
  depotByIdMap.set(id, marker);
  return marker;
};

const getCustomerMarker = ({ id, location }) => {
  let marker = customerByIdMap.get(id);
  if (marker) {
    return marker;
  }
  marker = L.circleMarker(location);
  marker.addTo(customerGroup).bindPopup();
  customerByIdMap.set(id, marker);
  return marker;
};

function initMenu() {
     $("#navUI").click(function () {
        $("#demo").removeClass('d-none');
        $("#rest").addClass('d-none');
        $("#openapi").addClass('d-none');

        $("#navUIItem").addClass('active');
        $("#navRestItem").removeClass('active');
        $("#navOpenApiItem").removeClass('active');
      });
      $("#navRest").click(function () {
        $("#demo").addClass('d-none');
        $("#rest").removeClass('d-none');
        $("#openapi").addClass('d-none');

        $("#navUIItem").removeClass('active');
        $("#navRestItem").addClass('active');
        $("#navOpenApiItem").removeClass('active');
      });
      $("#navOpenApi").click(function () {
        $("#demo").addClass('d-none');
        $("#rest").addClass('d-none');
        $("#openapi").removeClass('d-none');

        $("#navUIItem").removeClass('active');
        $("#navRestItem").removeClass('active');
        $("#navOpenApiItem").addClass('active');
      });
}

function createDataSets() {
    $.get("/demo/datasets", function (data) {
        if (data && data.length > 0) {
          data.forEach(item => {
            $("#testDataButton").append($('<a id="' + item + 'TestData" class="dropdown-item" href="#">' + item + '</a>'));

            $("#" + item + "TestData").click(function () {
              switchDataDropDownItemActive(item);
              scheduleId = null;
              testData = item;

              refreshRoutePlan();
            });
          });

          // load first data set
          testData = data[0];
          switchDataDropDownItemActive(testData);

          refreshRoutePlan();

          $("#solveButton").click(function () {
            solve();
          });
          $("#stopSolvingButton").click(function () {
            stopSolving();
          });

        } else {
          $("#demo").removeClass('d-none');
          $("#demo").empty();
          $("#demo").html("<h1><p align=\"center\">No test data available</p></h1>")
        }
      }).fail(function (xhr, ajaxOptions, thrownError) {
        // disable this page as there is no data
        $("#demo").removeClass('d-none');
        $("#demo").empty();
        $("#demo").html("<h1><p align=\"center\">No test data available</p></h1>")
      });
}

function switchDataDropDownItemActive(newItem) {
    activeCssClass = "active";
    $("#testDataButton > a." + activeCssClass).removeClass(activeCssClass);
    $("#" + newItem + "TestData").addClass(activeCssClass);
}

function renderRoutes(solution) {
  if (!initialized) {
    initialized = true;
    const bounds = [solution.southWestCorner, solution.northEastCorner];
    map.fitBounds(bounds);
  }
  // Vehicles
  $('[data-bs-toggle="tooltip-load"]').tooltip('dispose');
  vehiclesTable.children().remove();
  solution.vehicles.forEach((vehicle) => {
    const { id, capacity, totalDemand, totalDistanceMeters } = vehicle;
    const percentage = totalDemand / capacity * 100;
    const color = colorByVehicle(vehicle);
    const colorIfUsed = color;
    vehiclesTable.append(`
      <tr>
        <td>
          <i class="fas fa-crosshairs" id="crosshairs-${id}"
            style="background-color: ${colorIfUsed}; display: inline-block; width: 1rem; height: 1rem; text-align: center">
          </i>
        </td>
        <td>Vehicle ${id}</td>
        <td>
          <div class="progress" data-bs-toggle="tooltip-load" data-bs-placement="left" data-html="true"
            title="Cargo: ${totalDemand}<br/>Capacity: ${capacity}">
            <div class="progress-bar" role="progressbar" style="width: ${percentage}%">${totalDemand}/${capacity}</div>
          </div>
        </td>
        <td>${formatDistance(totalDistanceMeters)}</td>
      </tr>`);
  });
  $('[data-bs-toggle="tooltip-load"]').tooltip();
  // Depots
  depotsTable.children().remove();
  solution.depots.forEach((depot) => {
    const { id } = depot;
    const color = colorByDepot(depot);
    const icon = defaultIcon;
    const marker = getDepotMarker(depot);
    marker.setIcon(icon);
    marker.setPopupContent(depotPopupContent(depot, color));
    depotsTable.append(`<tr>
      <td><i class="fas fa-crosshairs" id="crosshairs-${id}"
      style="background-color: ${color}; display: inline-block; width: 1rem; height: 1rem; text-align: center">
      </i></td><td>Depot ${id}</td>
      </tr>`);
  });
  // Customers
  solution.customers.forEach((customer) => {
    getCustomerMarker(customer).setPopupContent(customerPopupContent(customer));
  });
  // Route
  routeGroup.clearLayers();
  solution.vehicles.forEach((vehicle) => {
    L.polyline(vehicle.route, { color: colorByVehicle(vehicle) }).addTo(routeGroup);
  });

  // Summary
  $('#score').text(solution.score);
  $('#scoreInfo').text(solution.scoreExplanation);
  $('#distance').text(formatDistance(solution.distanceMeters));
}

function textToClipboard(id) {
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