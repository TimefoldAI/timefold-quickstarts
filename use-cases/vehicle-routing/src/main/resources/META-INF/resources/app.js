let autoRefreshIntervalId = null;
let initialized = false;
let demoDataId = null;
let scheduleId = null;
let loadedRoutePlan = null;
let newCustomer = null;
let customerMaker = null;
const solveButton = $('#solveButton');
const stopSolvingButton = $('#stopSolvingButton');
const vehiclesTable = $('#vehicles');
const depotsTable = $('#depots');
const analyzeButton = $('#analyzeButton');

/*************************************** Map constants and variable definitions  **************************************/

const depotMarkerByIdMap = new Map();
const customerMarkerByIdMap = new Map();

const defaultIcon = new L.Icon.Default();

const map = L.map('map', {doubleClickZoom: false}).setView([51.505, -0.09], 13);
const customerGroup = L.layerGroup().addTo(map);
const depotGroup = L.layerGroup().addTo(map);
const routeGroup = L.layerGroup().addTo(map);

/************************************ Time line constants and variable definitions ************************************/

const byVehiclePanel = document.getElementById("byVehiclePanel");
const byVehicleTimelineOptions = {
    timeAxis: {scale: "hour"},
    orientation: {axis: "top"},
    xss: {disabled: true}, // Items are XSS safe through JQuery
    stack: false,
    stackSubgroups: false,
    zoomMin: 1000 * 60 * 60, // A single hour in milliseconds
    zoomMax: 1000 * 60 * 60 * 24 // A single day in milliseconds
};
const byVehicleGroupData = new vis.DataSet();
const byVehicleItemData = new vis.DataSet();
const byVehicleTimeline = new vis.Timeline(byVehiclePanel, byVehicleItemData, byVehicleGroupData, byVehicleTimelineOptions);

const byCustomerPanel = document.getElementById("byCustomerPanel");
const byCustomerTimelineOptions = {
    timeAxis: {scale: "hour"},
    orientation: {axis: "top"},
    verticalScroll: true,
    xss: {disabled: true}, // Items are XSS safe through JQuery
    stack: false,
    stackSubgroups: false,
    zoomMin: 1000 * 60 * 60, // A single hour in milliseconds
    zoomMax: 1000 * 60 * 60 * 24 // A single day in milliseconds
};
const byCustomerGroupData = new vis.DataSet();
const byCustomerItemData = new vis.DataSet();
const byCustomerTimeline = new vis.Timeline(byCustomerPanel, byCustomerItemData, byCustomerGroupData, byCustomerTimelineOptions);

/************************************ Initialize ************************************/

$(document).ready(function () {
    replaceQuickstartTimefoldAutoHeaderFooter();

    L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
        maxZoom: 19,
        attribution: '&copy; <a href="https://www.openstreetmap.org/">OpenStreetMap</a> contributors',
    }).addTo(map);

    solveButton.click(solve);
    stopSolvingButton.click(stopSolving);
    analyzeButton.click(analyze);
    refreshSolvingButtons(false);

    // HACK to allow vis-timeline to work within Bootstrap tabs
    $("#byVehicleTab").on('shown.bs.tab', function (event) {
        byVehicleTimeline.redraw();
    })
    $("#byCustomerTab").on('shown.bs.tab', function (event) {
        byCustomerTimeline.redraw();
    })
    // Add new customer
    map.on('click', function (e) {
        customerMaker = L.circleMarker(e.latlng);
        customerMaker.setStyle({color: 'green'});
        customerMaker.addTo(map);
        openRecommendationModal(e.latlng.lat, e.latlng.lng);
    });
    // Remove customer mark
    $("#newCustomerModal").on("hidden.bs.modal", function () {
        map.removeLayer(customerMaker);
    });
    setupAjax();
    fetchDemoData();
});

function colorByVehicle(vehicle) {
    return vehicle === null ? null : pickColor('vehicle' + vehicle.id);
}

function colorByDepot(depot) {
    return depot === null ? null : pickColor('depot' + depot.id);
}

function formatDrivingTime(drivingTimeInSeconds) {
    return `${Math.floor(drivingTimeInSeconds / 3600)}h ${Math.round((drivingTimeInSeconds % 3600) / 60)}m`;
}

function depotPopupContent(depot, color) {
    return `<h5>Depot ${depot.id}</h5>
<ul class="list-unstyled">
<li><span style="background-color: ${color}; display: inline-block; width: 12px; height: 12px; text-align: center">
</span> ${color}</li>
</ul>`;
}

function customerPopupContent(customer) {
    const arrival = customer.arrivalTime ? `<h6>Arrival at ${showTimeOnly(customer.arrivalTime)}.</h6>` : '';
    return `<h5>${customer.name}</h5>
    <h6>Demand: ${customer.demand}</h6>
    <h6>Available from ${showTimeOnly(customer.minStartTime)} to ${showTimeOnly(customer.maxEndTime)}.</h6>
    ${arrival}`;
}

function showTimeOnly(localDateTimeString) {
    return JSJoda.LocalDateTime.parse(localDateTimeString).toLocalTime();
}

function getDepotMarker(depot) {
    let marker = depotMarkerByIdMap.get(depot.id);
    if (marker) {
        return marker;
    }
    marker = L.marker(depot.location);
    marker.addTo(depotGroup).bindPopup();
    depotMarkerByIdMap.set(depot.id, marker);
    return marker;
}

function getCustomerMarker(customer) {
    let marker = customerMarkerByIdMap.get(customer.id);
    if (marker) {
        return marker;
    }
    marker = L.circleMarker(customer.location);
    marker.addTo(customerGroup).bindPopup();
    customerMarkerByIdMap.set(customer.id, marker);
    return marker;
}

function renderRoutes(solution) {
    if (!initialized) {
        const bounds = [solution.southWestCorner, solution.northEastCorner];
        map.fitBounds(bounds);
    }
    // Vehicles
    vehiclesTable.children().remove();
    solution.vehicles.forEach(function (vehicle) {
        const {id, capacity, totalDemand, totalDrivingTimeSeconds} = vehicle;
        const percentage = totalDemand / capacity * 100;
        const color = colorByVehicle(vehicle);
        vehiclesTable.append(`
      <tr>
        <td>
          <i class="fas fa-crosshairs" id="crosshairs-${id}"
            style="background-color: ${color}; display: inline-block; width: 1rem; height: 1rem; text-align: center">
          </i>
        </td>
        <td>Vehicle ${id}</td>
        <td>
          <div class="progress" data-bs-toggle="tooltip-load" data-bs-placement="left" data-html="true"
            title="Cargo: ${totalDemand} / Capacity: ${capacity}">
            <div class="progress-bar" role="progressbar" style="width: ${percentage}%">${totalDemand}/${capacity}</div>
          </div>
        </td>
        <td>${formatDrivingTime(totalDrivingTimeSeconds)}</td>
      </tr>`);
    });
    // Depots
    depotsTable.children().remove();
    solution.depots.forEach(function (depot) {
        const {id} = depot;
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
    solution.customers.forEach(function (customer) {
        getCustomerMarker(customer).setPopupContent(customerPopupContent(customer));
    });
    // Route
    routeGroup.clearLayers();
    const customerByIdMap = new Map(solution.customers.map(customer => [customer.id, customer]));
    const depotByIdMap = new Map(solution.depots.map(depot => [depot.id, depot]));
    for (let vehicle of solution.vehicles) {
        const depotLocation = depotByIdMap.get(vehicle.depot).location;
        const locations = vehicle.customers.map(customerId => customerByIdMap.get(customerId).location);
        L.polyline([depotLocation, ...locations, depotLocation], {color: colorByVehicle(vehicle)}).addTo(routeGroup);
    }

    // Summary
    $('#score').text(solution.score);
    $('#drivingTime').text(formatDrivingTime(solution.totalDrivingTimeSeconds));
}

function renderTimelines(routePlan) {
    byVehicleGroupData.clear();
    byCustomerGroupData.clear();
    byVehicleItemData.clear();
    byCustomerItemData.clear();

    $.each(routePlan.vehicles, function (index, vehicle) {
        const {totalDemand, capacity} = vehicle
        const percentage = totalDemand / capacity * 100;
        const vehicleWithLoad = `<h5 class="card-title mb-1">vehicle-${vehicle.id}</h5>
                                 <div class="progress" data-bs-toggle="tooltip-load" data-bs-placement="left" 
                                      data-html="true" title="Cargo: ${totalDemand} / Capacity: ${capacity}">
                                   <div class="progress-bar" role="progressbar" style="width: ${percentage}%">
                                      ${totalDemand}/${capacity}
                                   </div>
                                 </div>`
        byVehicleGroupData.add({id: vehicle.id, content: vehicleWithLoad});
    });

    $.each(routePlan.customers, function (index, customer) {
        const minStartTime = JSJoda.LocalDateTime.parse(customer.minStartTime);
        const maxEndTime = JSJoda.LocalDateTime.parse(customer.maxEndTime);
        const serviceDuration = JSJoda.Duration.ofSeconds(customer.serviceDuration);

        const customerGroupElement = $(`<div/>`)
            .append($(`<h5 class="card-title mb-1"/>`).text(`${customer.name}`));
        byCustomerGroupData.add({
            id: customer.id,
            content: customerGroupElement.html()
        });

        // Time window per customer.
        byCustomerItemData.add({
            id: customer.id + "_readyToDue",
            group: customer.id,
            start: customer.minStartTime,
            end: customer.maxEndTime,
            type: "background",
            style: "background-color: #8AE23433"
        });

        if (customer.vehicle == null) {
            const byJobJobElement = $(`<div/>`)
                .append($(`<h5 class="card-title mb-1"/>`).text(`Unassigned`));

            // Unassigned are shown at the beginning of the customer's time window; the length is the service duration.
            byCustomerItemData.add({
                id: customer.id + '_unassigned',
                group: customer.id,
                content: byJobJobElement.html(),
                start: minStartTime.toString(),
                end: minStartTime.plus(serviceDuration).toString(),
                style: "background-color: #EF292999"
            });
        } else {
            const arrivalTime = JSJoda.LocalDateTime.parse(customer.arrivalTime);
            const beforeReady = arrivalTime.isBefore(minStartTime);
            const arrivalPlusService = arrivalTime.plus(serviceDuration);
            const afterDue = arrivalPlusService.isAfter(maxEndTime);

            const byVehicleElement = $(`<div/>`)
                .append('<div/>')
                .append($(`<h5 class="card-title mb-1"/>`).text(customer.name));

            const byCustomerElement = $(`<div/>`)
                // customer.vehicle is the vehicle.id due to Jackson serialization
                .append($(`<h5 class="card-title mb-1"/>`).text('vehicle-' + customer.vehicle));

            const byVehicleTravelElement = $(`<div/>`)
                .append($(`<h5 class="card-title mb-1"/>`).text('Travel'));

            const previousDeparture = arrivalTime.minusSeconds(customer.drivingTimeSecondsFromPreviousStandstill);
            byVehicleItemData.add({
                id: customer.id + '_travel',
                group: customer.vehicle, // customer.vehicle is the vehicle.id due to Jackson serialization
                subgroup: customer.vehicle,
                content: byVehicleTravelElement.html(),
                start: previousDeparture.toString(),
                end: customer.arrivalTime,
                style: "background-color: #f7dd8f90"
            });
            if (beforeReady) {
                const byVehicleWaitElement = $(`<div/>`)
                    .append($(`<h5 class="card-title mb-1"/>`).text('Wait'));

                byVehicleItemData.add({
                    id: customer.id + '_wait',
                    group: customer.vehicle, // customer.vehicle is the vehicle.id due to Jackson serialization
                    subgroup: customer.vehicle,
                    content: byVehicleWaitElement.html(),
                    start: customer.arrivalTime,
                    end: customer.minStartTime
                });
            }
            let serviceElementBackground = afterDue ? '#EF292999' : '#83C15955'

            byVehicleItemData.add({
                id: customer.id + '_service',
                group: customer.vehicle, // customer.vehicle is the vehicle.id due to Jackson serialization
                subgroup: customer.vehicle,
                content: byVehicleElement.html(),
                start: customer.startServiceTime,
                end: customer.departureTime,
                style: "background-color: " + serviceElementBackground
            });
            byCustomerItemData.add({
                id: customer.id,
                group: customer.id,
                content: byCustomerElement.html(),
                start: customer.startServiceTime,
                end: customer.departureTime,
                style: "background-color: " + serviceElementBackground
            });

        }

    });

    $.each(routePlan.vehicles, function (index, vehicle) {
        if (vehicle.customers.length > 0) {
            let lastCustomer = routePlan.customers.filter((customer) => customer.id == vehicle.customers[vehicle.customers.length - 1]).pop();
            if (lastCustomer) {
                byVehicleItemData.add({
                    id: vehicle.id + '_travelBackToDepot',
                    group: vehicle.id, // customer.vehicle is the vehicle.id due to Jackson serialization
                    subgroup: vehicle.id,
                    content: $(`<div/>`).append($(`<h5 class="card-title mb-1"/>`).text('Travel')).html(),
                    start: lastCustomer.departureTime,
                    end: vehicle.arrivalTime,
                    style: "background-color: #f7dd8f90"
                });
            }
        }
    });

    if (!initialized) {
        byVehicleTimeline.setWindow(routePlan.startDateTime, routePlan.endDateTime);
        byCustomerTimeline.setWindow(routePlan.startDateTime, routePlan.endDateTime);
    }
}

function analyze() {
    // see score-analysis.js
    analyzeScore(loadedRoutePlan, "/route-plans/analyze")
}

function openRecommendationModal(lat, lng) {
    // see recommended-fit.js
    const customerId = Math.max(...loadedRoutePlan.customers.map(c => parseInt(c.id))) + 1;
    newCustomer = {id: customerId, location: [lat, lng]};
    addNewCustomer(customerId, lat, lng, map, customerMaker);
}

function getRecommendationsModal() {
    let formValid = true;
    formValid = validateFormField(newCustomer, 'name' , '#inputName') && formValid;
    formValid = validateFormField(newCustomer, 'demand' , '#inputDemand') && formValid;
    formValid = validateFormField(newCustomer, 'minStartTime' , '#inputMinStartTime') && formValid;
    formValid = validateFormField(newCustomer, 'maxEndTime' , '#inputMaxStartTime') && formValid;
    formValid = validateFormField(newCustomer, 'serviceDuration' , '#inputDuration') && formValid;
    if (formValid) {
        const updatedCustomer = {...newCustomer, serviceDuration: `PT${newCustomer['serviceDuration']}M`};
        let updatedCustomerList = [...loadedRoutePlan['customers']];
        updatedCustomerList.push(updatedCustomer);
        let updatedSolution = {...loadedRoutePlan, customers: updatedCustomerList};
        // see recommended-fit.js
        requestRecommendations(updatedCustomer.id, updatedSolution, "/route-plans/recommendation")
    }
}

function validateFormField(target, fieldName, inputName) {
    target[fieldName] = $(inputName).val();
    if ($(inputName).val() == "") {
        $(inputName).addClass("is-invalid");
    } else {
        $(inputName).removeClass("is-invalid");
    }
    return $(inputName).val() != "";
}

function applyRecommendationModal(recommendations) {
    let checkedRecommendation = null;
    recommendations.forEach((recommendation, index) => {
        if ($('#option'+ index).is(":checked")) {
            checkedRecommendation = recommendations[index];
        }
    });
    if (checkedRecommendation != null) {
        $("#recommendationOptions").removeClass("is-invalid");
        const updatedCustomer = {...newCustomer, serviceDuration: `PT${newCustomer['serviceDuration']}M`};
        let updatedCustomerList = [...loadedRoutePlan['customers']];
        updatedCustomerList.push(updatedCustomer);
        let updatedSolution = {...loadedRoutePlan, customers: updatedCustomerList};
        // see recommended-fit.js
        applyRecommendation(updatedSolution, newCustomer.id, checkedRecommendation.proposition.vehicleId, checkedRecommendation.proposition.index,
            "/route-plans/recommendation/apply");
    }
}

function updateSolutionWithNewCustomer(newSolution) {
    loadedRoutePlan = newSolution;
    renderRoutes(newSolution);
    renderTimelines(newSolution);
    $('#newCustomerModal').modal('hide');
    solve();
}

// TODO: move the general functionality to the webjar.

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

function solve() {
    $.post("/route-plans", JSON.stringify(loadedRoutePlan), function (data) {
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
        $("#customerButton").hide();
        $("#stopSolvingButton").show();
        if (autoRefreshIntervalId == null) {
            autoRefreshIntervalId = setInterval(refreshRoutePlan, 2000);
        }
    } else {
        $("#solveButton").show();
        $("#customerButton").show();
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
        if (demoDataId === null) {
            alert("Please select a test data set.");
            return;
        }

        path = "/demo-data/" + demoDataId;
    }

    $.getJSON(path, function (routePlan) {
        loadedRoutePlan = routePlan;
        refreshSolvingButtons(routePlan.solverStatus != null && routePlan.solverStatus !== "NOT_SOLVING");
        renderRoutes(routePlan);
        renderTimelines(routePlan);
        initialized = true;
    }).fail(function (xhr, ajaxOptions, thrownError) {
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

function fetchDemoData() {
    $.get("/demo-data", function (data) {
        data.forEach(function (item) {
            $("#testDataButton").append($('<a id="' + item + 'TestData" class="dropdown-item" href="#">' + item + '</a>'));

            $("#" + item + "TestData").click(function () {
                switchDataDropDownItemActive(item);
                scheduleId = null;
                demoDataId = item;
                initialized = false;
                depotGroup.clearLayers();
                depotMarkerByIdMap.clear();
                customerGroup.clearLayers();
                customerMarkerByIdMap.clear();
                refreshRoutePlan();
            });
        });

        demoDataId = data[0];
        switchDataDropDownItemActive(demoDataId);

        refreshRoutePlan();
    }).fail(function (xhr, ajaxOptions, thrownError) {
        // disable this page as there is no data
        $("#demo").empty();
        $("#demo").html("<h1><p style=\"justify-content: center\">No test data available</p></h1>")
    });
}

function switchDataDropDownItemActive(newItem) {
    activeCssClass = "active";
    $("#testDataButton > a." + activeCssClass).removeClass(activeCssClass);
    $("#" + newItem + "TestData").addClass(activeCssClass);
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
              <div class="btn-group dropstart">
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