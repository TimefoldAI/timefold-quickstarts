const TROLLEY_PATHS = new Map();
let TROLLEY_TRAVEL_DISTANCE = new Map();
let autoRefreshIntervalId = null;


function refreshSolution() {
    $.getJSON("/orderPicking", (orderPickingPlanning) => {
        TROLLEY_TRAVEL_DISTANCE = new Map(Object.entries(orderPickingPlanning.distanceToTravelByTrolley));
        updateWelcomeMessage(orderPickingPlanning.solverWasNeverStarted);
        printSolutionScore(orderPickingPlanning.solution);
        printSolutionTable(orderPickingPlanning.solution);
        printTrolleysMap(orderPickingPlanning.solution);
        refreshSolvingButtons(orderPickingPlanning.solverStatus != null && orderPickingPlanning.solverStatus !== "NOT_SOLVING");
    })
            .fail(function (jqxhr, textStatus, error) {
                const err = "Internal error: " + textStatus + ", " + error;
                showError("An error was produced during solution refresh.", err);
            });
}

// refresh solution to resize the canvas
window.addEventListener('resize', e => refreshSolution());
refreshSolution();

function printSolutionScore(orderPickingSolution) {
    const score = orderPickingSolution.score;
    if (score == null) {
        $("#score").text("Score: ?");
    } else {
        $("#score").text(`Score: ${score.hardScore}hard/${score.softScore}soft`);
    }
}

function updateWelcomeMessage(solverWasNeverStarted) {
    const welcomeMessageContainer = $('#welcomeMessageContainer');
    if (solverWasNeverStarted) {
        welcomeMessageContainer.show();
    } else {
        welcomeMessageContainer.empty();
    }
}

function printSolutionTable(orderPickingSolution) {
    const solutionTable = $('#solutionTable');
    solutionTable.children().remove();
    const tableBody = $('<tbody>').appendTo(solutionTable);
    const unassignedOrderItemsAndOrdersSpreading = findUnassignedOrderItemsAndOrdersSpreading(orderPickingSolution);
    const unassignedItemsByOrder = unassignedOrderItemsAndOrdersSpreading[0];
    const trolleysByOrder = unassignedOrderItemsAndOrdersSpreading[1];
    const unassignedTrolleys = [];
    for (const trolley of orderPickingSolution.trolleys) {
        if (trolley.nextElement != null) {
            const travelDistance = TROLLEY_TRAVEL_DISTANCE.get(trolley.id);
            printTrolley(tableBody, trolley, travelDistance, unassignedItemsByOrder, trolleysByOrder);
        } else {
            unassignedTrolleys.push(trolley);
        }
    }
    printUnassignedEntities(unassignedTrolleys, unassignedItemsByOrder);
}

function printUnassignedEntities(unassignedTrolleys, unAssignedItemsByOrder) {
    const unassignedEntitiesContainer = $('#unassignedEntitiesContainer');
    unassignedEntitiesContainer.empty();

    const unassignedEntitiesNav = $('<nav>').appendTo(unassignedEntitiesContainer);
    const unassignedEntitiesTabs = $('<div class="nav nav-tabs" id="unassignedEntitiesTabList" role="tablist">').appendTo(unassignedEntitiesNav);
    const unassignedEntitiesTabListContent = $('<div class="tab-content" id="unassignedEntitiesTabListContent">').appendTo(unassignedEntitiesContainer);

    printUnassignedTrolleys(unassignedTrolleys, unassignedEntitiesTabs, unassignedEntitiesTabListContent);
    printUnassignedOrders(unAssignedItemsByOrder, unassignedEntitiesTabs, unassignedEntitiesTabListContent);
}

function printTabNavLink(navTabs, active, tabId, tabPaneId, name) {
    const activeValue = active ? 'active' : '';
    return $(`<a class="nav-link ${activeValue}" id="${tabId}" data-bs-toggle="tab" href="#${tabPaneId}" role="tab" aria-controls="${tabId}" aria-selected="true">${name}</a>`).appendTo(navTabs);
}

function printTabPane(navTabsContainer, active, show, tabPaneId, tabId) {
    const activeValue = active ? 'active' : '';
    const showValue = show ? 'show' : '';
    return $(`<div class="tab-pane fade ${showValue} ${activeValue}" id="${tabPaneId}" role="tabpanel" aria-labelledby="${tabId}"></div>`).appendTo(navTabsContainer);
}

function printUnassignedTrolleys(trolleys, unassignedEntitiesTabs, unassignedEntitiesTabListContent) {
    printTabNavLink(unassignedEntitiesTabs, true, 'unassignedTrolleys', 'unassignedTrolleysTab', 'Trolleys');
    const tabPane = printTabPane(unassignedEntitiesTabListContent, true, true, 'unassignedTrolleysTab', 'unassignedTrolleys');
    const unassignedTrolleysTable = $(`<table class="table table-striped" id="unassignedTrolleysTable">`).appendTo(tabPane);
    printUnassignedTrolleysTableHeader(unassignedTrolleysTable);
    const unassignedTrolleysTableBody = $('<tbody>').appendTo(unassignedTrolleysTable);
    for (const trolley of trolleys) {
        const location = trolley.location;
        printUnassignedTrolleyRow(unassignedTrolleysTableBody, trolley, location);
    }
}

function printUnassignedTrolleysTableHeader(unassignedTrolleysTable) {
    const header = $('<thead class="table-dark">').appendTo(unassignedTrolleysTable);
    const headerTr = $('<tr>').appendTo(header);
    $('<th scope="col">#Trolley</th>').appendTo(headerTr);
    $('<th scope="col">Start location</th>').appendTo(headerTr);
    $('<th scope="col">Buckets</th>').appendTo(headerTr);
    $('<th scope="col">Bucket capacity</th>').appendTo(headerTr);
}

function printUnassignedTrolleyRow(unassignedTrolleysTableBody, trolley, location) {
    const trolleyRow = $('<tr>').appendTo(unassignedTrolleysTableBody);
    trolleyRow.append($(`<th scope="row">${trolley.id}</th>`));
    trolleyRow.append($(`<td>${location.shelvingId}, ${location.side}, ${location.row}</td>`));
    trolleyRow.append($(`<td>${trolley.bucketCount}</td>`));
    trolleyRow.append($(`<td>${trolley.bucketCapacity}</td>`));
}

function printUnassignedOrders(unAssignedItemsByOrder, unassignedEntitiesTabs, unassignedEntitiesTabListContent) {
    const orderIds = Array.from(unAssignedItemsByOrder.keys());
    orderIds.sort((a, b) => a - b);
    for (const orderId of orderIds) {
        const unassignedItems = unAssignedItemsByOrder.get(orderId);
        if (unassignedItems.length > 0) {
            unassignedItems.sort((item1, item2) => item1.id - item2.id);
            printUnassignedOrder(orderId, unassignedItems, unassignedEntitiesTabs, unassignedEntitiesTabListContent);
        }
    }
}

function printUnassignedOrder(orderId, unassignedItems, unassignedEntitiesTabs, unassignedEntitiesTabListContent) {
    const name = 'Order_' + orderId;
    const tabId = 'unassignedOrder_' + orderId;
    const tabPaneId = "unassignedOrderTab_" + orderId;

    printTabNavLink(unassignedEntitiesTabs, false, tabId, tabPaneId, name);
    const tabPane = printTabPane(unassignedEntitiesTabListContent, false, false, tabPaneId, tabId);
    const unassignedOrderTable = $('<table class="table table-striped">').appendTo(tabPane);
    printUnassignedOrderTableHeader(unassignedOrderTable);
    const unassignedOrderTableBody = $('<tbody>').appendTo(unassignedOrderTable);
    for (const orderItem of unassignedItems) {
        printUnassignedOrderRow(unassignedOrderTableBody, orderItem);
    }
}

function printUnassignedOrderTableHeader(unassignedOrderTable) {
    const header = $('<thead class="table-dark">').appendTo(unassignedOrderTable);
    const headerTr = $('<tr>').appendTo(header);
    $('<th scope="col">#Order item</th>').appendTo(headerTr);
    $('<th scope="col">Warehouse location</th>').appendTo(headerTr);
    $('<th scope="col">Name</th>').appendTo(headerTr);
    $('<th scope="col">Volume</th>').appendTo(headerTr);
}

function printUnassignedOrderRow(unassignedOrderTableBody, orderItem) {
    const itemRow = $('<tr>').appendTo(unassignedOrderTableBody);
    const product = orderItem.product;
    const location = product.location;
    itemRow.append($(`<th scope="row">${orderItem.id}</th>`));
    itemRow.append($(`<td>${location.shelvingId}, ${location.side}, ${location.row}</td>`));
    itemRow.append($(`<td>${product.name}</td>`));
    itemRow.append($(`<td>${product.volume}</td>`));
}

/**
 * Calculates the unassigned items and the occupied trolleys by each order.
 * @param orderPickingSolution an OrderPickingSolution solution returned by the service.
 * @returns an array in the form [Map<orderId, [OrderItem]>, Map<orderId, Set<int>] where the first element contains
 * a map that indexes the unassigned items for each order, and the second element contains a map that indexes the
 * trolleys where the current order has items.
 */
function findUnassignedOrderItemsAndOrdersSpreading(orderPickingSolution) {
    const unassignedItemsByOrder = new Map();
    const trolleysByOrder = new Map();
    for (const trolleyStep of orderPickingSolution.trolleySteps) {
        const orderItem = trolleyStep.orderItem;
        if (trolleyStep.trolleyId === null) {
            let unassignedItems = unassignedItemsByOrder.get(orderItem.orderId);
            if (unassignedItems === undefined) {
                unassignedItems = [];
                unassignedItemsByOrder.set(orderItem.orderId, unassignedItems);
            }
            unassignedItems.push(orderItem);
        } else {
            let trolleys = trolleysByOrder.get(orderItem.orderId);
            if (trolleys === undefined) {
                trolleys = new Set();
                trolleysByOrder.set(orderItem.orderId, trolleys);
            }
            trolleys.add(trolleyStep.trolleyId);
        }
    }
    return [unassignedItemsByOrder, trolleysByOrder];
}

/**
 * @param trolley a trolley instance to get the steps from.
 * @returns [TrolleyStep] an array with the trolley steps for the given trolley.
 */
function extractTrolleySteps(trolley) {
    const trolleySteps = [];
    let next = trolley.nextElement;
    while (next != null) {
        trolleySteps.push(next);
        next = next.nextElement;
    }
    return trolleySteps;
}

function printTrolley(tableBody, trolley, travelDistance, unAssignedItemsByOrder, trolleysByOrder) {
    const trolleyId = 'Trolley_' + trolley.id;
    const trolleyIcon = 'fa-cart-plus';
    const trolleySteps = extractTrolleySteps(trolley);
    const trolleyRow = $('<tr class="agent-row">').appendTo(tableBody);
    const trolleyTd = $('<td style="width:15%;">').appendTo(trolleyRow);
    const trolleyCard = $('<div class="card" style="background-color:#f7ecd5">').appendTo(trolleyTd);
    const trolleyCardBody = $('<div class="card-body p-1">').appendTo(trolleyCard);
    const trolleyCardRow = $(`<div class="row flex-nowrap">
                <div class="col-1">
                    <i class="fas ${trolleyIcon}"></i>
                </div>
                <div class="col-11">
                    <span style="font-size:1em" title="${trolleySteps.length} order items assigned to this Trolley, with a travel distance of ${travelDistance} meters."><a id="${trolleyId}">${trolleyId}&nbsp;&nbsp;(${trolleySteps.length} items, ${travelDistance} m)</a></span>
                </div>
            </div>`).appendTo(trolleyCardBody);

    printTrolleyDetail(trolleyCardBody, trolley, trolleySteps, unAssignedItemsByOrder, trolleysByOrder);

    const stepsTd = $('<td style="flex-flow:row; display: flex;">').appendTo(trolleyRow);
    printTrolleySteps(stepsTd, trolleySteps);
}

function printTrolleyDetail(detailContainer, trolley, trolleySteps, unAssignedItemsByOrder, trolleysByOrder) {
    const orderVolumes = new Map();
    for (const trolleyStep of trolleySteps) {
        const orderItem = trolleyStep.orderItem;
        let orderVolume = orderVolumes.get(orderItem.orderId);
        if (orderVolume === undefined) {
            orderVolume = orderItem.product.volume;
        } else {
            orderVolume = orderVolume + orderItem.product.volume;
        }
        orderVolumes.set(orderItem.orderId, orderVolume);
    }

    const sortedEntries = Array.from(orderVolumes.entries());
    sortedEntries.sort((e1, e2) => e2[1] - e1[1]);

    const bucketWidth = 50;
    const trolleyBucketsContainer = $('<div class="row">').appendTo(detailContainer);
    const bucketsDiv = $('<div style="padding-left: 15px; padding-top: 15px;">').appendTo(trolleyBucketsContainer);
    const bucketsTable = $('<table>').appendTo(bucketsDiv);
    let bucketsRow;
    let bucketTd;
    let bucketTdNumber = 0;
    let availableBuckets = trolley.bucketCount;
    let orderCount = 0;
    const ordersDetail = [];

    for (const entry of sortedEntries) {
        const orderNumber = entry[0];
        const orderTotalVolume = entry[1];
        const orderRequiredBuckets = Math.ceil(orderTotalVolume / trolley.bucketCapacity);
        const bucketColor = orderColor(orderNumber);
        ordersDetail.push([orderNumber, bucketColor, orderTotalVolume, orderRequiredBuckets]);
        for (let orderBucket = 1; orderBucket <= orderRequiredBuckets; orderBucket++) {
            if (bucketTdNumber % 2 === 0) {
                bucketsRow = $('<tr>').appendTo(bucketsTable);
            }
            bucketTdNumber++;
            let bucketDivWidth = bucketWidth;
            let bucketOccupancyPercent = 100;
            if (orderBucket === orderRequiredBuckets) {
                const lastBucketVolume = orderTotalVolume - ((orderBucket - 1) * trolley.bucketCapacity);
                bucketDivWidth = (bucketDivWidth / trolley.bucketCapacity) * lastBucketVolume;
                bucketOccupancyPercent = Math.ceil((100 * bucketDivWidth) / bucketWidth);
            }
            bucketTd = $(`<td style="border: 1px solid; border-color: black; padding: 1px; width:${bucketWidth};" title="${bucketOccupancyPercent}% of the bucket reserved for order #${orderNumber}">`).appendTo(bucketsRow);
            $(`<div style="background-color: ${bucketColor}; width:${bucketDivWidth}px; height:${bucketWidth}px;"></div>`).appendTo(bucketTd);
            availableBuckets--;
        }
    }

    if (availableBuckets > 0) {
        for (let i = 0; i < availableBuckets; i++) {
            if (bucketTdNumber % 2 === 0) {
                bucketsRow = $('<tr>').appendTo(bucketsTable);
            }
            bucketTdNumber++;
            bucketTd = $(`<td style="border: 1px solid; border-color: black; padding: 1px; width:${bucketWidth};" title="Free bucket">`).appendTo(bucketsRow);
            $(`<div style="width:${bucketWidth}px; height:${bucketWidth}px;"></div>`).appendTo(bucketTd);
        }
    } else if (availableBuckets < 0) {
        $(`<div><strong>Over constrained problem!! with the configured number of trolleys and buckets it's not possible to complete the orders, please check the configuration parameters.</strong></div>`).appendTo(bucketsDiv);
    }

    const trolleyOrdersDetailContainer = $('<div class="row" style="padding-left: 15px; padding-top: 15px; padding-right: 15px;">').appendTo(detailContainer);
    printTrolleyOrdersDetail(trolleyOrdersDetailContainer, trolley, ordersDetail);

    const trolleyOrdersSplitDetailContainer = $('<div class="row" style="padding-left: 15px; padding-top: 15px; padding-right: 15px;">').appendTo(detailContainer);
    printTrolleyOrdersSplitDetail(trolleyOrdersSplitDetailContainer, trolley, ordersDetail, unAssignedItemsByOrder, trolleysByOrder);
}

function printTrolleyOrdersDetail(ordersDetailContainer, trolley, ordersDetail) {
    const orderDetailsTable = $('<table class="table table-striped">').appendTo(ordersDetailContainer);
    printOrdersDetailTableHeader(orderDetailsTable);
    const ordersDetailTableBody = $('<tbody>').appendTo(orderDetailsTable);
    for (let orderDetail of ordersDetail) {
        const orderNumber = orderDetail[0];
        const bucketColor = orderDetail[1];
        const orderTotalVolume = orderDetail[2];
        const orderRequiredBuckets = orderDetail[3];
        printOrdersDetailRow(ordersDetailTableBody, orderNumber, bucketColor, orderTotalVolume, orderRequiredBuckets);
    }
    $(`<div>Bucket capacity ${trolley.bucketCapacity}</div>`).appendTo(ordersDetailContainer);
}

function printOrdersDetailTableHeader(ordersDetailTable) {
    const header = $('<thead class="table-dark">').appendTo(ordersDetailTable);
    const headerTr = $('<tr>').appendTo(header);
    $('<th scope="col">#Order</th>').appendTo(headerTr);
    $('<th scope="col">Volume</th>').appendTo(headerTr);
    $('<th scope="col">Buckets</th>').appendTo(headerTr);
}

function printOrdersDetailRow(ordersDetailTableBody, orderNumber, bucketColor, orderTotalVolume, orderRequiredBuckets) {
    const orderDetailRow = $('<tr>').appendTo(ordersDetailTableBody);
    orderDetailRow.append($(`<th scope="row"><div style="background-color: ${bucketColor}">${orderNumber}</div></th>`));
    orderDetailRow.append($(`<td>${orderTotalVolume}</td>`));
    orderDetailRow.append($(`<td>${orderRequiredBuckets}</td>`));
}

function printTrolleyOrdersSplitDetail(ordersDetailContainer, trolley, ordersDetail, unAssignedItemsByOrder, trolleysByOrder) {
    for (const orderDetail of ordersDetail) {
        const orderNumber = orderDetail[0];
        let anotherTrolleys = trolleysByOrder.get(orderNumber);
        if (anotherTrolleys !== undefined && anotherTrolleys.size > 1) {
            const anotherTrolleysDiv = $(`<div><span>*Order #${orderNumber} also in</span></div>`).appendTo(ordersDetailContainer);
            let first = true;
            for (const anotherTrolley of anotherTrolleys) {
                if (trolley.id !== anotherTrolley) {
                    const separator = first ? '' : ',';
                    $(`<a href="#Trolley_${anotherTrolley}">${separator}&nbsp;T${anotherTrolley}</a>`).appendTo(anotherTrolleysDiv);
                    first = false;
                }
            }
        }
    }
    for (const orderDetail of ordersDetail) {
        const orderNumber = ordersDetail[0];
        const unAssignedItems = unAssignedItemsByOrder.get(orderNumber);
        if (unAssignedItems !== undefined && unAssignedItems.length > 0) {
            $(`<div><span>*Order #${orderNumber} has <a href="#UnAssignedOrderItems_${orderNumber}">${unAssignedItems.length} un-assigned items</a></span></div>`).appendTo(ordersDetailContainer);
        }
    }
}

function printTrolleySteps(stepsContainer, trolleySteps) {
    const stepsTable = $('<table class="table table-striped">').appendTo(stepsContainer);
    printTrolleyStepsTableHeader(stepsTable);
    const stepsTableBody = $('<tbody>').appendTo(stepsTable);
    let stepNumber = 1;
    for (const trolleyStep of trolleySteps) {
        printTrolleyStep(stepsTableBody, stepNumber++, trolleyStep)
    }
}

function printTrolleyStepsTableHeader(stepsTable) {
    const header = $('<thead class="table-dark">').appendTo(stepsTable);
    const headerTr = $('<tr>').appendTo(header);
    $('<th scope="col">#Stop</th>').appendTo(headerTr);
    $('<th scope="col">Warehouse location</th>').appendTo(headerTr);
    $('<th scope="col">#Order</th>').appendTo(headerTr);
    $('<th scope="col">#Order item</th>').appendTo(headerTr);
    $('<th scope="col">Name</th>').appendTo(headerTr);
    $('<th scope="col">Volume</th>').appendTo(headerTr);
}

function printTrolleyStep(stepsTableBody, stepNumber, trolleyStep) {
    const orderItem = trolleyStep.orderItem;
    const orderItemId = orderItem.id
    const product = orderItem.product;
    const location = product.location;
    const orderId = orderItem.orderId;

    const stepRow = $('<tr>').appendTo(stepsTableBody);
    stepRow.append($(`<th scope="row">${stepNumber}</th>`));
    stepRow.append($(`<td>${location.shelvingId}, ${location.side}, ${location.row}</td>`));
    stepRow.append($(`<td>${orderId}</td>`));
    stepRow.append($(`<td>${orderItemId}</td>`));
    stepRow.append($(`<td>${product.name}</td>`));
    stepRow.append($(`<td>${product.volume}</td>`));
}

function printTrolleysMap(orderPickingSolution) {
    clearWarehouseCanvas();
    drawWarehouse();
    const mapActionsContainer = $('#mapActionsContainer');
    mapActionsContainer.children().remove();
    const trolleyCheckBoxes = [];
    let trolleyIndex = 0;
    for (const trolley of orderPickingSolution.trolleys) {
        if (trolley.nextElement != null) {
            printTrolleyPath(trolley, trolleyIndex, orderPickingSolution.trolleys.length, false);
            trolleyCheckBoxes.push(trolley.id);
        }
        trolleyIndex++;
    }
    for (const trolley of orderPickingSolution.trolleys) {
        if (trolley.nextElement != null) {
            printTrolleyPath(trolley, trolleyIndex, orderPickingSolution.trolleys.length, true);
            trolleyCheckBoxes.push(trolley.id);
        }
        trolleyIndex++;
    }
    if (trolleyCheckBoxes.length > 0) {
        const mapActionsContainer = $('#mapActionsContainer');
        mapActionsContainer.append($(`<div style="display: inline-block; padding-left: 10px;">
        <button id="unSelectButton" type="button" class="btn btn-secondary btn-sm" onclick="unCheckTrolleyCheckBoxes([${trolleyCheckBoxes}])">Uncheck all</button>
        </div>`));
    }
}

function printTrolleyPath(trolley, trolleyIndex, trolleyCount, writeText) {
    const trolleySteps = extractTrolleySteps(trolley);
    const trolleyPath = [];
    const trolleyLocation = trolley.location;

    trolleyPath.push(new WarehouseLocation(trolleyLocation.shelvingId, trolleyLocation.side, trolleyLocation.row));
    for (const trolleyStep of trolleySteps) {
        const location = trolleyStep.location;
        trolleyPath.push(new WarehouseLocation(location.shelvingId, location.side, location.row));
    }
    trolleyPath.push(new WarehouseLocation(trolleyLocation.shelvingId, trolleyLocation.side, trolleyLocation.row));
    TROLLEY_PATHS.set(trolley.id, trolleyPath);

    const color = trolleyColor(trolley.id);
    let trolleyCheckboxEnabled = false;
    if (trolleyPath.length > 2) {
        if (writeText) {
            drawTrolleyText(color, trolleyPath, trolleyIndex, trolleyCount);
        } else {
            drawTrolleyPath(color, trolleyPath, trolleyIndex, trolleyCount);
            trolleyCheckboxEnabled = true;
            const travelDistance = TROLLEY_TRAVEL_DISTANCE.get(trolley.id);
            printTrolleyCheckbox(trolley, trolleySteps.length, travelDistance, color, trolleyCheckboxEnabled);
        }
    }
}

function printTrolleyCheckbox(trolley, stepsLength, travelDistance, color, enabled) {
    const mapActionsContainer = $('#mapActionsContainer');
    const disabledValue = enabled ? '' : 'disabled';
    const checkedValue = enabled ? 'true' : 'false';
    mapActionsContainer.append($(`<div style="display: inline-block; padding-left: 15px;">
        <div class="trolley-checkbox-rectangle" style="background-color: ${color}; display: inline-block;"></div>
        <div style="display: inline-block;">
            <label title="${stepsLength} order items assigned to this Trolley, with a travel distance of ${travelDistance} meters.">
            <input type="checkbox" id="trolleyPath_${trolley.id}" onChange="printSelectedTrolleys()" checked="${checkedValue}" ${disabledValue}/>
                Trolley_${trolley.id} (${stepsLength} items, ${travelDistance} m)
            </label>
        </div>
    </div>`));
}

function unCheckTrolleyCheckBoxes(trolleyCheckBoxes) {
    for (const trolleyCheckBoxId of trolleyCheckBoxes) {
        const trolleyCheckBox = $(`#trolleyPath_${trolleyCheckBoxId}`);
        trolleyCheckBox.prop('checked', false);
    }
    clearWarehouseCanvas();
    drawWarehouse()
}

function orderColor(orderId) {
    return pickColor('order_color_' + orderId);
}

function trolleyColor(trolleyId) {
    return pickColor('trolley_color_' + trolleyId);
}

function printSelectedTrolleys() {
    clearWarehouseCanvas();
    drawWarehouse();
    let it = TROLLEY_PATHS.entries();
    let trolleyIndex = 0;
    for (const trolleyEntry of it) {
        const trolleyCheck = document.getElementById('trolleyPath_' + trolleyEntry[0]);
        if (trolleyCheck.checked) {
            const color = trolleyColor(trolleyEntry[0]);
            drawTrolleyPath(color, trolleyEntry[1], trolleyIndex, TROLLEY_PATHS.size);
        }
        trolleyIndex++;
    }
    it = TROLLEY_PATHS.entries();
    trolleyIndex = 0;
    for (const trolleyEntry of it) {
        const trolleyCheck = document.getElementById('trolleyPath_' + trolleyEntry[0]);
        if (trolleyCheck.checked) {
            const color = trolleyColor(trolleyEntry[0]);
            drawTrolleyText(color, trolleyEntry[1], trolleyIndex + TROLLEY_PATHS.size, TROLLEY_PATHS.size);
        }
        trolleyIndex++;
    }
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

function solve() {
    $.post("/orderPicking/solve", function () {
        refreshSolvingButtons(true);
    }).fail(function (xhr, ajaxOptions, thrownError) {
        showError("Start solving failed.", xhr);
    });
}

function stopSolving() {
    $.post("/orderPicking/stopSolving", function () {
        refreshSolvingButtons(false);
        refreshSolution();
    }).fail(function (xhr, ajaxOptions, thrownError) {
        showError("Stop solving failed.", xhr);
    });
}

$(document).ready(function () {
    replaceTimefoldAutoHeaderFooter();

    //Initialize button listeners
    $('#refreshButton').click(function () {
        refreshSolution()
    });

    $("#solveButton").click(function () {
        solve();
    });

    $("#stopSolvingButton").click(function () {
        stopSolving();
    });

    //Initial solution loading
    refreshSolution();
});

function doClickOnUnassignedEntities() {
    $('#unassignedEntitiesTab').trigger("click")
}
