
let sorts = {};
let filters = [];
let pagination = {
    'page': 0,
    'pageSize': 10
};

function sortAsUrlParam(sortings) {
    let sortPairs = [];

    sortings.forEach(sorting => sortPairs.push(['sort', sorting]));

    return sortPairs;
}

function fetchData(sortings, filter, pagination) {
    if (sortings.length === 0)
        sortings = ['id'];

    fetch('gateway.php', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify({
            'service': 1,
            'url': 'api/v1/vehicles',
            'url-params': [
                ...sortAsUrlParam(sortings),
                ['filter', filter],
                ['page', pagination.page],
                ['page_size', pagination.pageSize]
            ]
        })
    }).then(r => r.json()).then(responseJSON => {
        const responseCode = responseJSON['response_code'];
        const responseBody = JSON.parse(responseJSON['response_body']);

        if (responseCode == '400') {
            alert(`Bad request.\nDetails:\n    ${responseBody['error-message']}`);
        }
        else {
            document.querySelector("#table-content").replaceChildren([]);

            responseBody.forEach(vehicle => addRowToTable(vehicle));
        }
    });
}

function putData(id, vehicleJSON) {
    fetch('gateway.php', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify({
            'service': 1,
            'method': 'PUT',
            'url': `api/v1/vehicles/${id}`,
            'body': JSON.stringify(vehicleJSON)
        })
    }).then(r => r.json()).then(responseJSON => {
        const responseCode = responseJSON['response_code'];

        if (responseCode == '404') {
            alert(`Entity with id ${id} not found`);
        }
        else if (responseCode == '400') {
            const responseBody = JSON.parse(responseJSON['response_body']);
            alert(`Bad request.\nDetails:\n    ${responseBody['error-message']}`);
        }
        else if (responseCode > 400) {
            alert(`Client error: HTTP ${responseCode}`);
        }
    });
}

function deleteData(id) {
    fetch('gateway.php', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify({
            'service': 1,
            'method': 'DELETE',
            'url': `api/v1/vehicles/${id}`
        })
    }).then(r => r.json()).then(responseJSON => {
        const responseCode = responseJSON['response_code'];

        if (responseCode == '404') {
            alert(`Entity with id ${id} not found`);
        }
        else if (responseCode == '400') {
            const responseBody = JSON.parse(responseJSON['response_body']);
            alert(`Bad request.\nDetails:\n    ${responseBody['error-message']}`);
        }
        else {
            alert(`Client error: HTTP ${responseCode}`);
        }
    });
}

function addRowToTable(vehicleJSON) {
    const tbody = document.querySelector("#table-content");
    const template = document.querySelector("#template-row");
    const clone = template.content.cloneNode(true);
    let td = clone.querySelectorAll("td");

    td[0].textContent = vehicleJSON.id;
    td[1].textContent = vehicleJSON.name;
    td[2].textContent = vehicleJSON.coordinates.x;
    td[3].textContent = vehicleJSON.coordinates.y;
    td[4].textContent = vehicleJSON.creationDate;
    td[5].textContent = vehicleJSON.enginePower;
    td[6].textContent = vehicleJSON.numberOfWheels;
    td[7].textContent = vehicleJSON.distanceTravelled;
    td[8].textContent = vehicleJSON.fuelType;

    tbody.appendChild(clone);
}

window.onpageshow = () => {
    dataRequest();
}

function onReload() {
    const pageNumberField = document.getElementById('page-number-field');
    const pageNumber = Number(pageNumberField.value);

    if (!Number.isNaN(pageNumber) && pageNumber > 0) {
        pageNumberField.setCustomValidity('');
    }
    else {
        pageNumberField.setCustomValidity('Page number should positive number!');
        pageNumberField.reportValidity();

        return;
    }

    pagination.page = pageNumber - 1;
    pagination.pageSize = document.getElementById('page-size-field').value;

    dataRequest();
}

function sortDictionaryToSortArray(sortDict) {
    let newSortArray = [];

    for (let k in sortDict)
        newSortArray.push(`${sortDict[k] === 'desc' ? '-' : ''}${k}`);

    return newSortArray;
}

function doSort(event) {
    const sortField = event.target.getAttribute('entity-field');

    if (sorts[sortField] === 'asc')
        sorts[sortField] = 'desc';
    else if (sorts[sortField] === 'desc')
        delete sorts[sortField];
    else if (sorts[sortField] == undefined)
        sorts[sortField] = 'asc';

    event.target.setAttribute('sort-order', sorts[sortField] || 'none');

    dataRequest();
}

function doFilter() {
    const filterQuery = document.getElementById('filter-field').value;

    fetchData(sortDictionaryToSortArray(sorts), filterQuery, pagination);
}

function doDelete() {
    const deleteIdField = document.getElementById('delete-id-field');

    if (deleteIdField.value.length > 0)
        deleteData(deleteIdField.value);
    else
        alert('Id for deleting must be a positive number!');
}

function doReplace() {
    const replaceIdField = document.getElementById('replace-id-field').value;

    const nameField = document.getElementById('replace-field-name').value;
    const coordinatesXField = document.getElementById('replace-field-coordinates-x').value;
    const coordinatesYField = document.getElementById('replace-field-coordinates-y').value;
    const enginePowerField = document.getElementById('replace-field-engine-power').value;
    const numberOfWheelsField = document.getElementById('replace-field-number-of-wheels').value;
    const distanceTravelledField = document.getElementById('replace-field-distance-travelled').value;
    const fuelTypeField = document.getElementById('replace-field-fuel-type').value;

    putData(replaceIdField, {
        'name': nameField,
        'coordinates': {
            'x': coordinatesXField,
            'y': coordinatesYField
        },
        'enginePower': enginePowerField,
        'numberOfWheels': numberOfWheelsField,
        'distanceTravelled': distanceTravelledField,
        'fuelType': fuelTypeField
    });
}

function patchRequest(id, patchJSON) {
    fetch('gateway.php', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify({
            'service': 1,
            'method': 'PATCH',
            'url': `api/v1/vehicles/${id}`,
            'body': JSON.stringify(patchJSON)
        })
    }).then(r => r.json()).then(responseJSON => {
        const responseCode = responseJSON['response_code'];

        if (responseCode == '404') {
            alert(`Entity with id ${id} not found`);
        }
        else if (responseCode == '400') {
            const responseBody = JSON.parse(responseJSON['response_body']);
            alert(`Bad request.\nDetails:\n    ${responseBody['error-message']}`);
        }
        else if (responseCode > 400) {
            alert(`Client error: HTTP ${responseCode}`);
        }
    });
}

function doPatch() {
    const patchRecordTable = document.getElementById('patch-record-table');
    const records = patchRecordTable.querySelectorAll('tr');

    const patchJSON = {
        'fields': []
    };

    Array.from(records).forEach(record => {
        const cells = record.querySelectorAll('td');
        patchJSON.fields.push({
            'field-name': cells[0].querySelector('input').value,
            'value': cells[1].querySelector('input').value
        });
    });

    const patchIdField = document.getElementById('patch-id-field');
    const patchId = patchIdField.value;

    if (patchId.length > 0) {
        patchRequest(patchId, patchJSON);
    }
    else {
        alert('Id for patching must be positive number');
    }
}

function calculateAverageNumberOfWheels() {
    fetch('gateway.php', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify({
            'service': 1,
            'url': 'api/v1/vehicles/average-number-of-wheels'
        })
    }).then(r => r.json()).then(responseJSON => {
        const responseCode = responseJSON['response_code'];

        if (responseCode == '200') {
            const avgCounter = responseJSON['response_body'];
            document.getElementById('avg-number-span').textContent = avgCounter;
        }
        else if (responseCode == '404') {
            alert(`Entity with id ${id} not found`);
        }
        else {
            alert(`Client error: HTTP ${responseCode}`);
        }
    });
}

function disableRelatedButton(e) {
    const relatedButtonId = e.target.getAttribute('related-button-id');
    const relatedButton = document.getElementById(relatedButtonId);

    relatedButton.disabled = e.target.value.length === 0;
}

function dataRequest() {
    fetchData(sortDictionaryToSortArray(sorts), '', pagination);
}

function onDeleteByEnginePowerButton() {
    const deleteByEnginePowerField = document.getElementById("delete-by-engine-power-field");
    const deleteEnginePower = deleteByEnginePowerField.value;

    if (deleteEnginePower.length > 0) {
        fetch('gateway.php', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({
                'service': 1,
                'method': 'DELETE',
                'url': `api/v1/vehicles/delete-by-engine-power/${deleteEnginePower}`,
            })
        }).then(r => r.json()).then(responseJSON => {
            const responseCode = responseJSON['response_code'];

            if (responseCode == '400') {
                const responseBody = JSON.parse(responseJSON['response_body']);

                alert(`Bad request.\nDetails:\n    ${responseBody['error-message']}`);
            }
            else if (responseCode == '404') {
                alert(`No entity with enginePower ${deleteEnginePower}`);
            }
            else if (responseCode > 400) {
                alert(`Client error: HTTP ${responseCode}`);
            }
        });
    }
    else
        alert("Engine power for deleting must be positive number");
}

function onFuelTypeLessButton() {
    const fuelTypeLessField = document.getElementById("fuel-type-less-field");
    const fuelType = fuelTypeLessField.value;

    if (fuelType.length > 0) {
        fetch('gateway.php', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({
                'service': 1,
                'url': `api/v1/vehicles/fuel-type-less-than/${fuelType}`,
            })
        }).then(r => r.json()).then(responseJSON => {
            const responseCode = responseJSON['response_code'];

            if (responseCode == '200') {
                const responseBody = JSON.parse(responseJSON['response_body']);

                document.querySelector("#table-content").replaceChildren([]);
                responseBody.forEach(vehicle => addRowToTable(vehicle));
            }
            else if (responseCode == '400') {
                const responseBody = JSON.parse(responseJSON['response_body']);

                alert(`Bad request.\nDetails:\n    ${responseBody['error-message']}`);
            }
            else if (responseCode == '404') {
                alert(`No entity with fuelType less than ${fuelType}`);
            }
            else if (responseCode > 400) {
                alert(`Client error: HTTP ${responseCode}`);
            }
        });
    }
    else
        alert("Fuel type cannot be blank");
}

function addPatchRecord() {
    const patchRecordTable = document.getElementById('patch-record-table');
    const template = document.querySelector("#template-patch");

    const clone = template.content.cloneNode(true);
    const tr = clone.querySelector('tr');
    const td = tr.querySelectorAll('td');

    patchRecordTable.appendChild(tr);
}

function addWheels() {
    const addWheelsIdField = document.getElementById('add-wheels-id-field');
    const addWheelsId = addWheelsIdField.value;

    const addWheelsField = document.getElementById('add-wheels-field');
    const addWheelsNumber = addWheelsField.value;

    if (addWheelsId.length > 0 && addWheelsNumber.length > 0) {
        fetch('gateway.php', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({
                'service': 2,
                'method': 'PUT',
                'url': `second-service/api/v1/shop/add-wheels/${addWheelsId}/number-of-wheels`,
                'url-params': [
                    ['wheels', addWheelsNumber]
                ]
            })
        }).then(r => r.json()).then(responseJSON => {
            const responseCode = responseJSON['response_code'];

            if (responseCode == '400') {
                // const responseBody = JSON.parse(responseJSON['response_body']);

                // alert(`Bad request.\nDetails:\n    ${responseBody['error-message']}`);
                alert('Bad request');
            }
            else if (responseCode == '404') {
                alert(`No entity with id ${addWheelsId}`);
            }
            else if (responseCode > 400) {
                alert(`Client error: HTTP ${responseCode}`);
            }
        });
    }
    else {
        alert('Wheels count should be a number');
    }
}

function fixDistance() {
    const fixDistanceIdField = document.getElementById('fix-distance-field');
    const fixDistanceId = fixDistanceIdField.value;

    if (fixDistanceId.length > 0) {
        fetch('gateway.php', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({
                'service': 2,
                'method': 'PUT',
                'url': `second-service/api/v1/shop/fix-distance/${fixDistanceId}`,
            })
        }).then(r => r.json()).then(responseJSON => {
            const responseCode = responseJSON['response_code'];

            if (responseCode == '400') {
                const responseBody = JSON.parse(responseJSON['response_body']);

                alert(`Bad request.\nDetails:\n    ${responseBody['error-message']}`);
            }
            else if (responseCode == '404') {
                alert(`No entity with fuelType less than ${fuelType}`);
            }
            else if (responseCode > 400) {
                alert(`Client error: HTTP ${responseCode}`);
            }
        });
    }
    else {
        alert('Id for fixing distance must be positive number');
    }
}
