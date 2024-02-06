function addNewCustomer(id, lat, lng, map, marker) {
    $('#newCustomerModal').modal('show')
    const customerModalContent = $("#newCustomerModalContent");
    customerModalContent.children().remove();
    let customerForm = "";
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
        "          <input type='number' class='form-control' id='inputDemand' aria-describedby='inputDemand' value='1' required>" +
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
        `          <input class='form-control' id='inputMinStartTime' aria-describedby='inputMinStartTime' required>` +
        "          <div class='invalid-feedback'>" +
        "            Field is required" +
        "          </div>" +
        "        </div>" +
        "        <div class='col-3'>" +
        "          <label for='inputMaxStartTime'>Max Start Time</label>" +
        `          <input class='form-control' id='inputMaxStartTime' aria-describedby='inputMaxStartTime' required>` +
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
        "          <input type='number' class='form-control' id='inputDuration' aria-describedby='inputDuration' value='120' required>" +
        "          <div class='invalid-feedback'>" +
        "            Field is required" +
        "          </div>" +
        "        </div>" +
        "      </div>" +
        "  </div>";
    customerModalContent.append(customerForm);
    let startDate = JSJoda.LocalDateTime.now().plusDays(1);
    let endDate = JSJoda.LocalDateTime.now().plusHours(30);
    flatpickr("#inputMinStartTime", {enableTime: true, dateFormat: "Y-m-d H:i", defaultDate: startDate.format(JSJoda.DateTimeFormatter.ofPattern('yyyy-M-d HH:mm'))});
    flatpickr("#inputMaxStartTime", {enableTime: true, dateFormat: "Y-m-d H:i", defaultDate: endDate.format(JSJoda.DateTimeFormatter.ofPattern('yyyy-M-d HH:mm'))});
    const customerModalFooter = $("#newCustomerModalFooter");
    customerModalFooter.children().remove();
    customerModalFooter.append("<button id='recommendationButton' type='button' class='btn btn-success'><i class='fas fa-arrow-right'></i> Get Recommendations</button>");
    $("#recommendationButton").click(getRecommendationsModal);
}

function requestRecommendations(customerId, solution, endpointPath) {
    $.post(endpointPath, JSON.stringify({solution, customerId}), function (recommendations) {
        const customerModalContent = $("#newCustomerModalContent");
        customerModalContent.children().remove();
        let customerOptions = "";
        const customer = solution.customers.find(c => c.id === customerId);
        recommendations.forEach((recommendation, index) => {

            const analysisTable = $(`<table class="table"/>`).css({textAlign: 'center'});
            const analysisTHead = $(`<thead/>`).append($(`<tr/>`)
                .append($(`<th></th>`))
                .append($(`<th>Constraint</th>`).css({textAlign: 'left'}))
                .append($(`<th>Type</th>`))
                .append($(`<th># Matches</th>`))
                .append($(`<th>Weight</th>`))
                .append($(`<th>Score</th>`))
                .append($(`<th></th>`)));
            analysisTable.append(analysisTHead);
            const analysisTBody = $(`<tbody/>`)
            $.each(recommendation.scoreDiff.constraints, function (index2, constraintAnalysis) {
                visualizeConstraintAnalysis(analysisTBody, index2, constraintAnalysis, true, index)
            });
            analysisTable.append(analysisTBody);
            customerOptions += "<div class='form-check'>" +
                `  <input class='form-check-input' type='radio' name='recommendationOptions' id='option${index}' value='option${index}' ${index === 0 ? 'checked=true' : ''}>` +
                `  <label class='form-check-label' for='option${index}'>` +
                `    Add <b>${customer.name}</b> to the vehicle <b>${recommendation.proposition.vehicleId}</b> at the position <b>${recommendation.proposition.index + 1} ${index === 0 ? ' (Best Recommendation)' : ''}</b>` +
                "  </label>" +
                `  <a id="analyzeRecommendationButton${index}" class="float-justify" href="#" role="button">` +
                "    <i class='fas fa-info-circle'></i>" +
                "  </a>" +
                `  <div class='collapse' id='collapse${index}'>` +
                "    <div class='card card-body'>" +
                `      <table class="table" style='text-align: center'>${analysisTable.html()}</table>` +
                "    </div>" +
                "  </div>" +
                "</div>";
        });
        customerModalContent.append(customerOptions);
        // We add button events only after modal content is loaded
        recommendations.forEach((recommendation, index) => {
            $(`#analyzeRecommendationButton${index}`).click(_ => {
                $(`#collapse${index}`).collapse('toggle');
            });
            $.each(recommendation.scoreDiff.constraints, function (index2, _) {
                $(`#row${index2}Button${index}`).click(e => {
                    $(`#row${index2}Collapse${index}`).collapse('toggle');
                    let target = $(e.target);
                    if (target.hasClass('fa-arrow-down')) {
                        target.removeClass('fa-arrow-down').addClass('fa-arrow-up');
                    } else {
                        target.removeClass('fa-arrow-up').addClass('fa-arrow-down');
                    }
                });
            });
        });
        const customerModalFooter = $("#newCustomerModalFooter");
        customerModalFooter.children().remove();
        customerModalFooter.append("<button id='applyRecommendationButton' type='button' class='btn btn-success'><i class='fas fa-check'></i> Accept and Solve</button>");
        $("#applyRecommendationButton").click(_ => applyRecommendationModal(recommendations));
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



