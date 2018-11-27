
$('.table > tbody > tr').click(function () {
    $("#empInfo").modal();
});

/*
    * The script of main page and it's various functions
   */



// global variables
var list = [];
var info = [];
var dropdownitems = [];
var A;
var I;
var H;
var status = -1;
var elem1;
var elem2;
var elem3;
var ascNum = true;
var ascName = false;
var rowNum = -1;
var xhr = new XMLHttpRequest();
var rqueue = ["./OV/test", "./OV/main/databases", "./OV/main/employees"];
var rcounter = 0;

// when ever the page loads
xhr.onload = function () {
    if (xhr.readyState == 4 && xhr.status == 200) {
        if (this.responseText == "0") {
            window.location.replace(".");
        }
        try {
            var temp = JSON.parse(this.responseText);
            if (temp.length > 0) {
                if (temp[0].hasOwnProperty("cost")) {
                    info = temp;
                    updateinfo();
                } else if (temp[0].hasOwnProperty("status")) {
                    list = temp;
                    updatelist();
                } else {
                    dropdownitems = temp;
                    addDropdown();
                }
            }
        } catch (e) {
            if (this.responseText == "import success") {
                showSuccessToast("Import successful.");
            } else {
                showErrorToast(this.responseText);
            }
        }

        requestContent();
    }
};

// the file submitter when importing
function submitfile() {
    var formData = new FormData();
    formData.append("files", document.getElementById("fileLoader").files[0]);
    document.getElementById("fileLoader").value = "";
    xhr.open("POST", "./OV/main/import");
    xhr.send(formData);
}

// functionality for office dropdown(selects the database)
function addDropdown() {
    dropdown = document.getElementById("officeDropdown")
    dropdown.innerHTML = "";
    for (var i = 0; i < dropdownitems.length; i++) {
        dropdown.innerHTML += "<a class=\"dropdown-item\" role=\"presentation\" onclick=\"selectitem('" + dropdownitems[i] + "')\">"
            + dropdownitems[i] + "</a>";
    }

}

// select a row of the employee iist on the web page
function selectitem(item) {
    var body = document.getElementById('EmployeeList');
    body.innerHTML = "";
    rqueue.push("./OV/main/employees");
    xhr.open("POST", "./OV/main/databases/" + item);
    xhr.send();
}

// update the employee list whenever the page reloads
function updatelist() {
    var body = document.getElementById('EmployeeList');
    body.innerHTML = "";

    for (var i = 0; i < list.length; i++) {

        var ver = document.createElement("tr");

        var id = document.createElement("td");
        var ptxt = document.createTextNode(list[i].id);
        id.id = "employeeid";
        id.appendChild(ptxt);
        ver.appendChild(id);

        var name = document.createElement("td");
        var ntxt = document.createTextNode(list[i].name);
        name.id = "employeename";
        name.appendChild(ntxt);
        ver.appendChild(name);

        var active = document.createElement("td");
        var atxt = document.createTextNode(list[i].status);
        active.id = "employeestatus";
        active.appendChild(atxt);
        ver.appendChild(active);
        ver.setAttribute('onclick', "getId(" + list[i].id + ", \""
            + list[i].id + " " + list[i].name + "\")");
        body.appendChild(ver);
    }

}

// open the file dialog when file loader is clicked
function openFileDialog() {
    $("#fileLoader").click();
}

// send a get request for just search query
function searchupdate() {
    var body = document.getElementById('EmployeeList');
    body.innerHTML = "";
    crdnr = document.getElementById("searchid").value;
    fullname = document.getElementById("searchname").value;
    if (crdnr != "" && fullname != "") {
        xhr.open("GET", "./OV/main/search/" + crdnr + "/" + fullname
            + "/status/" + status + "/sort/-1");
        xhr.send();
    } else if (crdnr == "" && fullname != "") {
        xhr.open("GET", "./OV/main/search/" + -1 + "/" + fullname
            + "/status/" + status + "/sort/-1");
        xhr.send();
    } else if (crdnr != "" && fullname == "") {
        xhr.open("GET", "./OV/main/search/" + crdnr + "/" + -1
            + "/status/" + status + "/sort/-1");
        xhr.send();
    } else if (crdnr == "" && fullname == "") {
        xhr.open("GET", "./OV/main/employees");
        xhr.send();
    }
}

function resetFilters() {
	 xhr.open("GET", "./OV/main/employees");
     xhr.send();
}

// update payrate table when page reloads
function updateinfo() {
    var body = document.getElementById('infotable');
    body.innerHTML = "";

    for (var i = 0; i < info.length; i++) {

        var ver = document.createElement("tr");

        var id = document.createElement("td");
        var ptxt = document.createTextNode(info[i].cost);
        id.appendChild(ptxt);
        ver.appendChild(id);

        var name = document.createElement("td");
        var ntxt = document.createTextNode(info[i].startDate);
        name.appendChild(ntxt);
        ver.appendChild(name);
        var active = document.createElement("td");
        var atxt = document.createTextNode(info[i].endDate);
        active.appendChild(atxt);
        ver.appendChild(active);

        // create an edit icon
        var edit = document.createElement("td");
        var editIcon = document.createElement("span");
        editIcon.style = "cursor:pointer;";
        editIcon.className = "fas fa-pencil-alt";
        editIcon.setAttribute("onclick", "edit(" + i + ")");
        edit.appendChild(editIcon);
        ver.appendChild(edit);

        // create trash icon
        var trash = document.createElement("td");
        var trashIcon = document.createElement("span");
        trashIcon.style = "cursor:pointer;"
        trashIcon.className = "fas fa-trash-alt";
        trashIcon.setAttribute("onclick", "deletePayrate(" + i + ")");
        trash.appendChild(trashIcon);
        ver.appendChild(trash);

        body.appendChild(ver);
        $("#payratestatus").html("");
    }
}

// functionality when edit button is pressed
function edit(i) {
    var row = document.getElementById('infotable').children

    for (l = 0; l < row.length; l++) {
        for (k = 0; k < 3; k++) {
            var element = row[l].children[k];
            if (element.children.length > 0) {
                element.innerHTML = element.children[0].value
            }
        }
    }

    for (k = 0; k < 3; k++) {

        var element = row[i].children[k];
        var temp = element.innerHTML;
        element.innerHTML = "<input id = 'editedPayrate' type = 'text' size = '10' value = '" + temp + "'>";
    }
}

// functionality when add new payrate button is pressed
function newPayrtField() {
    var body = document.getElementById('infotable');

    info.push({'cost': "0", 'startDate': "Start Date", 'endDate': "End Date"});

    var ver = document.createElement("tr");

    var id = document.createElement("td");
    var ptxt = document.createElement("input");
    ptxt.setAttribute("placeholder", "Price");
    ptxt.setAttribute('size', ptxt.getAttribute('placeholder').length + 2);
    id.appendChild(ptxt);
    ver.appendChild(id);

    var name = document.createElement("td");
    var ntxt = document.createElement("input");
    ntxt.setAttribute("placeholder", "YYYY-MM-DD");
    ntxt.setAttribute('size', ntxt.getAttribute('placeholder').length + 2);
    name.appendChild(ntxt);
    ver.appendChild(name);
    var active = document.createElement("td");
    var atxt = document.createElement("input");
    atxt.setAttribute("placeholder", "YYYY-MM-DD");
    atxt.setAttribute('size', atxt.getAttribute('placeholder').length + 2);
    active.appendChild(atxt);
    ver.appendChild(active);

    // create edit icon
    var edit = document.createElement("td");
    var editIcon = document.createElement("span");
    editIcon.className = "fas fa-pencil-alt";
    editIcon.setAttribute("onclick", "edit(" + body.children.length + ")")
    edit.appendChild(editIcon);
    ver.appendChild(edit);

    // create trash icon
    var trash = document.createElement("td");
    var trashIcon = document.createElement("span");
    trashIcon.className = "fas fa-trash-alt";
    trashIcon.setAttribute("onclick", "deletePayrate(" + body.children.length + ")");
    trash.appendChild(trashIcon);
    ver.appendChild(trash);
    body.appendChild(ver);
}

// function when save button pressed
function saveChanges() {
    var infoname = document.getElementById("infotitle").innerHTML;
    var crdnr = infoname.split(" ")[0];
    var out = crdnr + "\n";

    var row = document.getElementById('infotable').children;
    for (var i = 0; i < row.length; i++) {
        var inf = []
        for (k = 0; k < 3; k++) {
            var element = row.item(i).children.item(k);
            if (element.children.length > 0) {
                inf.push(element.children[0].value);
            } else {
                inf.push(element.innerHTML);
            }
        }
        out += inf[0] + "," + inf[1] + ","
            + inf[2] + "\n";

    }
    var modal = $('#empInfo');
    $.post({
        url: "./OV/main/editPayrates/",
        beforeSend: function (xhr) {
            xhr.setRequestHeader('Accept', 'text/plain');
            xhr.setRequestHeader("Content-type", "text/plain");
        },
        data: out,
        success: function (response) {
            setTimeout(function () {
                getId(crdnr, infoname)
            }, 200);
            showSuccessToast("Payrates have been successfully saved.");
        },
        error: function (data) {
            showErrorToast("There has been a problem while saving the payrates.");
        }
    })
}

// sort the tables when the employee name or number is pressed
function sort(n) {
    var sortnum = document.getElementById("numSort");
    var sortname = document.getElementById("nameSort");
    var crdnr = document.getElementById("searchid").value;
    var fullname = document.getElementById("searchname").value;
    if (crdnr == "") {
        crdnr = -1;
    }
    if (fullname == "") {
        fullname = -1;
    }
    if (n == 0 && ascNum == false) {
        xhr.open("GET", "./OV/main/search/" + crdnr + "/" + fullname
            + "/status/" + status + "/sort/00");
        ascNum = true;
        sortnum.className = "fas fa-sort-up fa-fw";
        sortname.className = "fas fa-sort fa-fw";
    } else if (n == 0 && ascNum == true) {
        xhr.open("GET", "./OV/main/search/" + crdnr + "/" + fullname
            + "/status/" + status + "/sort/01");
        ascNum = false;
        sortnum.className = "fas fa-sort-down fa-fw";
        sortname.className = "fas fa-sort fa-fw";
    } else if (n == 1 && ascName == false) {
        xhr.open("GET", "./OV/main/search/" + crdnr + "/" + fullname
            + "/status/" + status + "/sort/10");
        ascName = true;
        sortname.className = "fas fa-sort-up fa-fw";
        sortnum.className = "fas fa-sort fa-fw";
    } else if (n == 1 && ascName == true) {
        xhr.open("GET", "./OV/main/search/" + crdnr + "/" + fullname
            + "/status/" + status + "/sort/11");
        ascName = false;
        sortname.className = "fas fa-sort-down fa-fw";
        sortnum.className = "fas fa-sort fa-fw";
    }
    xhr.send();
}

// logout off the main html page
function logout() {
    var auth2 = gapi.auth2.getAuthInstance();
    auth2.signOut().then(function () {
        console.log('User signed out.');
    });
    xhr.open("GET", "./OV/logout");
    xhr.send();
    window.open("./login.html", "_self");
}

function onLoad() {
    gapi.load('auth2', function () {
        gapi.auth2.init();
    });
}

/*
 * the functions when status filter is applied
 * we had to make three of them becaue we cannot take the value selected from an "a" tag
 */

function statusFilterA() {
    status = document.getElementById("statusA").getAttribute("value");
    crdnr = document.getElementById("searchid").value;
    fullname = document.getElementById("searchname").value;
    if (crdnr != "" && fullname != "") {
        xhr.open("GET", "./OV/main/search/" + crdnr + "/" + fullname
            + "/status/A/sort/-1");
    } else if (crdnr == "" && fullname != "") {
        xhr.open("GET", "./OV/main/search/" + -1 + "/" + fullname
            + "/status/A/sort/-1");
    } else if (crdnr != "" && fullname == "") {
        xhr.open("GET", "./OV/main/search/" + crdnr + "/" + -1
            + "/status/A/sort/-1");
    } else {
        xhr.open("GET", "./OV/main/search/" + -1 + "/" + -1
            + "/status/A/sort/-1");
    }
    xhr.send();
}

function statusFilterI() {
    status = document.getElementById("statusI").getAttribute("value");
    var crdnr = document.getElementById("searchid").value;
    var fullname = document.getElementById("searchname").value;
    if (crdnr != "" && fullname != "") {
        xhr.open("GET", "./OV/main/search/" + crdnr + "/" + fullname
            + "/status/I/sort/-1");
    } else if (crdnr == "" && fullname != "") {
        xhr.open("GET", "./OV/main/search/" + -1 + "/" + fullname
            + "/status/I/sort/-1");
    } else if (crdnr != "" && fullname == "") {
        xhr.open("GET", "./OV/main/search/" + crdnr + "/" + -1
            + "/status/I/sort/-1");
    } else {
        xhr.open("GET", "./OV/main/search/" + -1 + "/" + -1
            + "/status/I/sort/-1");
    }
    xhr.send();
}

function statusFilterH() {
    status = document.getElementById("statusH").getAttribute("value");
    var crdnr = document.getElementById("searchid").value;
    var fullname = document.getElementById("searchname").value;
    if (crdnr != "" && fullname != "") {
        xhr.open("GET", "./OV/main/search/" + crdnr + "/" + fullname
            + "/status/H/sort/-1");
    } else if (crdnr == "" && fullname != "") {
        xhr.open("GET", "./OV/main/search/" + -1 + "/" + fullname
            + "/status/H/sort/-1");
    } else if (crdnr != "" && fullname == "") {
        xhr.open("GET", "./OV/main/search/" + crdnr + "/" + -1
            + "/status/H/sort/-1");
    } else {
        xhr.open("GET", "./OV/main/search/" + -1 + "/" + -1
            + "/status/H/sort/-1");
    }
    xhr.send();
}

// get the id of the employee when a row is pressed
function getId(x, name) {
    info = [];
    xhr.open("GET", "./OV/main/employees/" + x);
    xhr.send(this.responseText);
    var infoname = document.getElementById("infotitle");
    infoname.innerHTML = name;
    var body = document.getElementById('infotable');
    body.innerHTML = "";
    $("#empInfo").modal();
}

// office button and status dropdown functionality 
$(function () {

    $("#officeDropdown a").click(function () {

        $("#officeButtton").text($(this).text());
        $("#officeButtton").val($(this).text());

    });

    $("#statusDropdown a").click(function () {

        $("#statusButton").text($(this).text());
        $("#statusButton").val($(this).text());

    });

});

// this is called whenever the server recieves new content
function requestContent() {
    if (rcounter < rqueue.length) {
        xhr.open("GET", rqueue[rcounter]);
        xhr.send();
        rcounter++;
    }
}

// delete the selected payrate
function deletePayrate(i) {
    var delelem = document.getElementById('infotable').children[i];
    document.getElementById('infotable').removeChild(delelem);

    var row = document.getElementById('infotable').children;
    for (l = 0; l < row.length; l++) {
        row[l].children[3].children[0].setAttribute('onclick', "edit(" + l + ")");
        row[l].children[4].children[0].setAttribute('onclick', "deletePayrate(" + l + ")");
    }
}

// the error notification message on the page
function showErrorToast(message) {
    $.notify({
        message: message
    }, {
        type: 'danger',
        placement: {
            from: "bottom",
            align: "center"
        },
        animate: {
            enter: 'animated fadeInUp',
            exit: 'animated fadeOutDown'
        }
    });
}

// the success notification message on the page
function showSuccessToast(message) {
    $.notify({
        message: message
    }, {
        type: 'success',
        allow_dismiss: true,
        placement: {
            from: "bottom",
            align: "center"
        },
        animate: {
            enter: 'animated fadeInUp',
            exit: 'animated fadeOutDown'
        },
        z_index: 1000000000
    });
}

// resets all the filters selected
$('#reset').on('click', function(e) {
	document.getElementById("searchid").value = "";
    document.getElementById("searchname").value = "";
    document.getElementById("numSort").className = "fas fa-sort fa-fw";
    document.getElementById("nameSort").className = "fas fa-sort fa-fw";
	$('.dropdown-toggle').first().html("Status");
	xhr.open("GET", "./OV/main/employees");
	xhr.send();
}).trigger('click');

// menu toggle function
$("#menu-toggle").click(function (e) {
    e.preventDefault();
    $("#wrapper").toggleClass("toggled");
});

// notify the status of import
function showImportStatus(status) {
	console.log(status);
    $("#importstatus").html(status);
    $("#importpopup").modal();
}

// notify the status of the payrate change
function showPayrateStatus(status) {
    $("#payratestatus").html(status);
}