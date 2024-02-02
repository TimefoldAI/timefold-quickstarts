function addNewCustomer(id, lat, lng, map, marker) {
    $('#newCustomerModal').modal('show')
    const customerModalContent = $("#newCustomerModalContent");
    customerModalContent.children().remove();
    let customerForm = "";
    customerForm += "<div class='form-group'>" +
        "      <div class='row'>" +
        "        <div class='col-2'>" +
        "          <label for='inputId'>Id</label>" +
        `          <input type='text' disabled class='form-control' id='inputId' aria-describedby='inputId' value='${id}'>` +
        "        </div>" +
        "      </div>" +
        "  </div>";
    customerForm += "<div class='form-group'>" +
        "      <div class='row'>" +
        "        <div class='col-5'>" +
        "          <label for='inputName'>Name</label>" +
        `          <input type='text' class='form-control' id='inputName' aria-describedby='inputName' value='customer${id}' required>` +
        "          <div class='invalid-feedback'>" +
        "            Field is required" +
        "          </div>" +
        "        </div>" +
        "      </div>" +
        "  </div>";
    customerForm += "<div class='form-group'>" +
        "      <div class='row'>" +
        "        <div class='col-4'>" +
        "          <label for='inputLatitude'>Latitude</label>" +
        `          <input type='text' disabled class='form-control' id='inputLatitude' aria-describedby='inputLatitude' value='${lat}'>` +
        "        </div>" +
        "        <div class='col-4'>" +
        "          <label for='inputLongitude'>Longitude</label>" +
        `          <input type='text' disabled class='form-control' id='inputLongitude' aria-describedby='inputLongitude' value='${lng}'>` +
        "        </div>" +
        "      </div>" +
        "    </div>";
    customerForm += "<div class='form-group'>" +
        "      <div class='row'>" +
        "        <div class='col-2'>" +
        "          <label for='inputDemand'>Demand</label>" +
        "          <input type='number' class='form-control' id='inputDemand' aria-describedby='inputDemand' value='10' required>" +
        "          <div class='invalid-feedback'>" +
        "            Field is required" +
        "          </div>" +
        "        </div>" +
        "      </div>" +
        "  </div>";
    customerForm += "<div class='form-group'>" +
        "      <div class='row'>" +
        "        <div class='col-3'>" +
        "          <label for='inputMinStartTime'>Min Start Time</label>" +
        `          <input type='datetime-local' class='form-control' id='inputMinStartTime' aria-describedby='inputMinStartTime' value='2024-02-02T10:56' required>` +
        "          <div class='invalid-feedback'>" +
        "            Field is required" +
        "          </div>" +
        "        </div>" +
        "        <div class='col-3'>" +
        "          <label for='inputMaxStartTime'>Max Start Time</label>" +
        `          <input type='datetime-local' class='form-control' id='inputMaxStartTime' aria-describedby='inputMaxStartTime' value='2024-02-02T11:56' required>` +
        "          <div class='invalid-feedback'>" +
        "            Field is required" +
        "          </div>" +
        "        </div>" +
        "      </div>" +
        "   </div>" +
        "</div>";
    customerForm += "<div class='form-group'>" +
        "      <div class='row'>" +
        "        <div class='col-3'>" +
        "          <label for='inputDuration'>Duration in Minutes</label>" +
        "          <input type='number' class='form-control' id='inputDuration' aria-describedby='inputDuration' value='10' required>" +
        "          <div class='invalid-feedback'>" +
        "            Field is required" +
        "          </div>" +
        "        </div>" +
        "      </div>" +
        "  </div>";
    customerModalContent.append(customerForm);
    const customerModalFooter = $("#newCustomerModalFooter");
    customerModalFooter.children().remove();
    customerModalFooter.append("<button id='recommendationButton' type='button' class='btn btn-success'>Next</button>");
    $("#recommendationButton").click(newCustomerModal);
}

function requestRecommendations(customerId, solution, endpointPath) {
    $.post(endpointPath, JSON.stringify({solution, customerId}), function (recommendations) {
        const customerModalContent = $("#newCustomerModalContent");
        customerModalContent.children().remove();
        let customerOptions = "";
        const customer = solution.customers.find(c => c.id === customerId);
        recommendations.forEach((recommendation, index) => {
            customerOptions += "<div class='form-check'>" +
                `  <input class='form-check-input' type='radio' name='recommendationOptions' id='option${index}' value='option${index}'>` +
                `  <label class='form-check-label' for='option${index}'>` +
                `    Add <b>${customer.name}</b> to the vehicle <b>${recommendation.proposition.vehicleId}</b> at the position <b>${recommendation.proposition.index + 1}</b> (${recommendation.scoreDiff.score})` +
                "  </label>" +
                "</div>";
        });
        $("#newCustomerModalLabel").text("Top-5 Recommendations");
        customerModalContent.append(customerOptions);
        const customerModalFooter = $("#newCustomerModalFooter");
        customerModalFooter.children().remove();
        customerModalFooter.append("<button id='applyRecommendationButton' type='button' class='btn btn-success'>Apply Recommendation</button>");
        $("#applyRecommendationButton").click(_ => applyCustomerModal(recommendations));
    }).fail(function (xhr, ajaxOptions, thrownError) {
            showError("Recommendations request analysis failed.", xhr);
            $('#newCustomerModal').modal('hide');
        },
        "text");
}

function applyRecommendation(solution, customerId, vehicleId, index, endpointPath) {
    $.post(endpointPath, JSON.stringify({solution, customerId, vehicleId, index}), function (updatedSolution) {
        updateSolutionWithNewCustomer(updatedSolution);
    }).fail(function (xhr, ajaxOptions, thrownError) {
            showError("Apply recommendation request failed.", xhr);
            $('#newCustomerModal').modal('hide');
        },
        "text");
}



