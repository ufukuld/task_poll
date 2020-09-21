const listContainer = document.querySelector('#service-list');
let servicesRequest = new Request('/service');
fetch(servicesRequest)
.then(function(response) { return response.json(); })
.then(function(serviceList) {
  serviceList.forEach(service => {
    var li = document.createElement("li");
    li.appendChild(document.createTextNode(service.name + ': ' + service.status));
    listContainer.appendChild(li);
  });
});

const saveButton = document.querySelector('#post-service');
saveButton.onclick = evt => {
    let strUrl = document.querySelector('#txtUrl').value;
    let strName = document.querySelector('#txtName').value;
    fetch('/service', {
    method: 'post',
    headers: {
    'Accept': 'application/json, text/plain, */*',
    'Content-Type': 'application/json'
    },
  body: JSON.stringify({url:strUrl, name:strName})
}).then(res=> location.reload());
}

const deleteButton = document.querySelector('#delete-service');
deleteButton.onclick = evt => {
    alert("dd");
    let strName = document.querySelector('#txtName').value;

    fetch('/service', {
    method: 'delete',
    headers: {
    'Accept': 'application/json, text/plain, */*',
    'Content-Type': 'application/json'
    },
  body: JSON.stringify({name:strName})
}).then(res=> location.reload());
}