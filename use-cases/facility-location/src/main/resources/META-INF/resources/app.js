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
const facilityByIdMap = new Map();

const solveButton = $('#solveButton');
const stopSolvingButton = $('#stopSolvingButton');
const facilitiesTable = $('#facilities');

const colorById = (i) => colors[i % colors.length];
const colorByFacility = (facility) => facility === null ? null : colorById(facility.id);

const defaultIcon = new L.Icon.Default();
const greyIcon = new L.Icon({
  iconUrl: 'https://raw.githubusercontent.com/pointhi/leaflet-color-markers/master/img/marker-icon-grey.png',
  shadowUrl: 'https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.6.0/images/marker-shadow.png',
  iconSize: [25, 41],
  iconAnchor: [12, 41],
  popupAnchor: [1, -34],
  shadowSize: [41, 41],
});

const createCostFormat = (notation) => new Intl.NumberFormat('en-US', {
  style: 'currency',
  currency: 'USD',
  maximumFractionDigits: 1,
  minimumFractionDigits: 1,
  notation,
});
const shortCostFormat = createCostFormat('compact');
const longCostFormat = createCostFormat('standard');

const getStatus = () => {
  $.get('/flp/status', null, (data) => {
        return showProblem(data);
    }).fail((xhr, ajaxOptions, thrownError) => {
      showError('Get status failed.', xhr);
    },
    "text");
};

const solve = () => {
  $.post('/flp/solve', null, () => {
      updateSolvingStatus(true);
      autoRefreshCount = 300;
      if (autoRefreshIntervalId == null) {
        autoRefreshIntervalId = setInterval(autoRefresh, 500);
      }
    }).fail((xhr, ajaxOptions, thrownError) => {
      showError('Start solving failed.', xhr);
    },
    "text");
};

const stopSolving = () => {
  $.post('/flp/stopSolving', null, () => {
      updateSolvingStatus(false);
      getStatus();
    }).fail((xhr, ajaxOptions, thrownError) => {
      showError('Stop solving failed.', xhr);
    },
    "text");
};

const updateSolvingStatus = (solving) => {
  if (solving) {
    solveButton.hide();
    stopSolvingButton.show();
  } else {
    autoRefreshCount = 0;
    solveButton.show();
    stopSolvingButton.hide();
  }
};

const autoRefresh = () => {
  getStatus();
  autoRefreshCount--;
  if (autoRefreshCount <= 0) {
    clearInterval(autoRefreshIntervalId);
    autoRefreshIntervalId = null;
  }
};

const facilityPopupContent = (facility, cost, color) => `<h5>Facility ${facility.id}</h5>
<ul class="list-unstyled">
<li>Usage: ${facility.usedCapacity}/${facility.capacity}</li>
<li>Setup cost: ${cost}</li>
<li><span style="background-color: ${color}; display: inline-block; width: 12px; height: 12px; text-align: center">
</span> ${color}</li>
</ul>`;

const getFacilityMarker = ({id, location}) => {
  let marker = facilityByIdMap.get(id);
  if (marker) {
    return marker;
  }
  marker = L.marker(location);
  marker.addTo(facilityGroup).bindPopup();
  facilityByIdMap.set(id, marker);
  return marker;
};

const showProblem = ({solution, scoreExplanation, isSolving}) => {
  if (!initialized) {
    initialized = true;
    map.fitBounds(solution.bounds);
  }
  // Facilities
  facilitiesTable.children().remove();
  solution.facilities.forEach((facility) => {
    const {id, setupCost, capacity, usedCapacity, used} = facility;
    const percentage = usedCapacity / capacity * 100;
    const color = colorByFacility(facility);
    const colorIfUsed = facility.used ? color : 'white';
    const icon = facility.used ? defaultIcon : greyIcon;
    const marker = getFacilityMarker(facility);
    marker.setIcon(icon);
    marker.setPopupContent(facilityPopupContent(facility, longCostFormat.format(facility.setupCost), color));
    facilitiesTable.append(`<tr class="${used ? 'table-active' : 'text-muted'}">
<td><i class="fas fa-crosshairs" id="crosshairs-${id}"
style="background-color: ${colorIfUsed}; display: inline-block; width: 1rem; height: 1rem; text-align: center">
</i></td><td>Facility ${id}</td>
<td><div class="progress">
<div class="progress-bar" role="progressbar" style="width: ${percentage}%">${usedCapacity}/${capacity}</div>
</div></td>
<td class="text-end">${shortCostFormat.format(setupCost)}</td>
</tr>`);
    $(`#crosshairs-${id}`)
      .mouseenter(() => marker.openPopup())
      .mouseleave(() => marker.closePopup());
  });
  // Consumers
  consumerGroup.clearLayers();
  solution.consumers.forEach((consumer) => {
    const color = colorByFacility(consumer.facility);
    L.circleMarker(consumer.location, consumer.assigned ? {color} : {}).addTo(consumerGroup);
    if (consumer.assigned) {
      L.polyline([consumer.location, consumer.facility.location], {color}).addTo(consumerGroup);
    }
  });
  // Summary
  $('#score').text(solution.score);
  $('#cost').text(longCostFormat.format(solution.totalCost));
  $('#cost-percentage').text(Math.round(solution.totalCost * 1000 / solution.potentialCost) / 10);
  $('#distance').text(solution.totalDistance);
  $('#scoreInfo').text(scoreExplanation);
  updateSolvingStatus(isSolving);
};

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

setupAjax();

const map = L.map('map', {doubleClickZoom: false}).setView([51.505, -0.09], 13);
map.whenReady(getStatus);

L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
  maxZoom: 19,
  attribution: '&copy; <a href="https://www.openstreetmap.org/">OpenStreetMap</a> contributors',
}).addTo(map);

const consumerGroup = L.layerGroup();
const facilityGroup = L.layerGroup();
consumerGroup.addTo(map);
facilityGroup.addTo(map);

solveButton.click(solve);
stopSolvingButton.click(stopSolving);

updateSolvingStatus();
