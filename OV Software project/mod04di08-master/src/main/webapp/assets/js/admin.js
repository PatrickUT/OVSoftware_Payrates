var ovacc = [];
var googleacc = [];
var dropdownitems = [];
var status = -1;
var xhr = new XMLHttpRequest();
var rqueue = ["./OV/test", "./OV/admin/googleusers",
    "./OV/admin/ovusers"];
var rcounter = 0;
$("#username").hide();
$("#password").hide();
$("#email").show();
xhr.onload = function () {
    if (xhr.readyState == 4 && xhr.status == 200) {
        // session ends
        if (this.responseText == "0") {
            window.location.replace(".");
        }
        try {
            var temp = JSON.parse(this.responseText);
            if (temp.length > 0) {

                if (temp[0].hasOwnProperty("email")) {
                    googleacc = temp;
                    updateGoogleAccount();
                } else if (temp[0].hasOwnProperty("username")) {
                    ovacc = temp;
                    updateOVAccount();
                }
            }
        } catch (e) {
            console.log("caught exception : " + e);
        }
        requestContent();
    }
};

function updatelist() {
    var body = document.getElementById('EmployeeList');
    body.innerHTML = "";

    for (var i = 0; i < googleacc.length; i++) {

        var ver = document.createElement("tr");

        // create email display
        var em = document.createElement("td");
        var etxt = document.createTextNode(googleacc[i].email);
        em.appendChild(etxt);
        ver.appendChild(em);
        body.appendChild(ver);
    }
}

function updateOVAccount() {
    var body = document.getElementById('OVAccountList');
    body.innerHTML = "";

    for (var i = 0; i < ovacc.length; i++) {

        var ver = document.createElement("tr");

        // create user display
        var user = document.createElement("td");
        var utxt = document.createTextNode(ovacc[i].username);
        user.appendChild(utxt);
        ver.appendChild(user);
        user.className = "username";
        // create pass display
        var pass = document.createElement("td");
        var ptxt = document.createTextNode(ovacc[i].password);
        pass.appendChild(ptxt);
        ver.appendChild(pass);

        // create trash icon
        var trash = document.createElement("td");
        var trashIcon = document.createElement("span");
        trash.style = "cursor:pointer;"
        trash.className = "fas fa-trash-alt";
        trash.id = "trash";
        trash.setAttribute("onclick", "deleteOVUser(\""
            + ovacc[i].username + "\")");
        trash.appendChild(trashIcon);
        ver.appendChild(trash);

        body.appendChild(ver);

        body.appendChild(ver);
    }

    $('#OVAccountList')
        .on(
            'click',
            "#trash",
            function () {
                var row = $(this).closest('tr').remove();
                $
                    .ajax({
                        url: "./OV/admin/ovuser",
                        contentType: 'application/json; charset=utf-8',
                        type: "DELETE",
                        data: JSON.stringify({
                            "username": $(this).closest(
                                'tr').children(
                                "td.username").text()
                        }),
                        dataType: 'json',
                        success: function () {
                            row.remove();
                            showSuccessToast("User has been successfully deleted.");
                        },
                        error: function () {
                            showErrorToast("An error occurred while deleting the user.");
                        }
                    });
            });
}

// update google account table
function updateGoogleAccount() {
    var body = document.getElementById('GoogleAccountList');
    body.innerHTML = "";

    for (var i = 0; i < googleacc.length; i++) {

        var ver = document.createElement("tr");

        // create email display
        var em = document.createElement("td");
        var etxt = document.createTextNode(googleacc[i].email);
        em.className = "email";
        em.appendChild(etxt);
        ver.appendChild(em);

        // create trash icon
        var trash = document.createElement("td");
        trash.style = "cursor:pointer;"
        trash.className = "fas fa-trash-alt";
        trash.id = "trash";
        ver.appendChild(trash);
        body.appendChild(ver);
    }

    $('#GoogleAccountList')
        .on(
            'click',
            "#trash",
            function () {
                var row = $(this).closest('tr').remove();
                $
                    .ajax({
                        url: "./OV/admin/googleuser",
                        contentType: 'application/json; charset=utf-8',
                        type: "DELETE",
                        data: JSON.stringify({
                            "email": $(this).closest('tr')
                                .children("td.email")
                                .text()
                        }),
                        dataType: 'json',
                        success: function () {
                            row.remove();
                            showSuccessToast("User has been successfully deleted.");
                        },
                        error: function () {
                            showErrorToast("An error occurred while deleting the user.");
                        }
                    });
            });
}

function addAccount() {
    if ($("#OVSelector").is(':checked')) {
        var username = $("#username").val();
        var password = $("#password").val();
        $
            .post({
                url: "./OV/admin/ovuser",
                contentType: 'application/json; charset=utf-8',
                data: JSON.stringify({
                    "username": username,
                    "password": password
                }),
                dataType: 'json',
                success: function () {
                    showSuccessToast("User has been successfully created.");
                    $('#OVAccountList tr:last')
                        .after(
                            '<tr><td class="username">'
                            + username
                            + '</td><td>•••••••••</td><td class="fas fa-trash-alt" id="trash"></td></tr>');
                    $("#username").val("");
                    $("#password").val("");
                },
                error: function () {
                    showErrorToast("An error occurred while creating the user.")
                }
            });
    }
    if ($("#googleSelector").is(':checked')) {
        var email = $("#email").val();
        $
            .post({
                url: "./OV/admin/googleuser",
                contentType: 'application/json; charset=utf-8',
                data: JSON.stringify({
                    "email": email
                }),
                dataType: 'json',
                success: function () {
                    showSuccessToast("User has been successfully created.");
                    $('#GoogleAccountList tr:last')
                        .after(
                            '<tr><td class="email">'
                            + email
                            + '</td><td class="fas fa-trash-alt" id="trash"></td></tr>');
                    $("#email").val();
                },
                error: function () {
                    showErrorToast("An error occurred while creating the user.")
                }
            });
    }
}

$("#menu-toggle").click(function (e) {
    e.preventDefault();
    $("#wrapper").toggleClass("toggled");
});

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
        }
    });
}

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

function deleteOVUser(user) {
    $.ajax({
        url: "./OV/admin/ovuser",
        type: "DELETE",
        contentType: 'application/json; charset=utf-8',
        data: JSON.stringify({
            "username": user
        }),
        dataType: 'json'
    });
}

function requestContent() {
    if (rcounter < rqueue.length) {
        xhr.open("GET", rqueue[rcounter]);
        xhr.send();
        rcounter++;
    }
}

$("#googleSelector").click(function () {
    $("#username").hide();
    $("#password").hide();
    $("#email").show();
});

$("#OVSelector").click(function () {
    $("#username").show();
    $("#password").show();
    $("#email").hide();
});
