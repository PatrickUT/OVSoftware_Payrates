// username is used to store the username entered by the end user
// password is used to store the password entered by the end user
var username;
var password;

var xhr = new XMLHttpRequest();

/**
 * Function is called the login button for ov account is pressed,
 * Username and password will be filled with the entered credentials and sanitized
 * If the length of the username or password is empty then it will display a message not to leave the space empty
 * Otherwise, it will send the entered credentials with a jquery function
 * If the entered credentials are in the database, then it will redirect to the main.html
 * If the entered credentials are not in the database, then it will display an error message
 */
function login() {
    username = sanitizeInput(document.getElementById("username").value);
    password = sanitizeInput(document.getElementById("password").value);
    if (username.length == 0 || password.length == 0) {
        $("#warning").text(
            "Please do not leave one of the space empty.");
    } else {
        $
            .get({
                url: "./OV/login/" + username + "/" + password,
                success: function (response) {
                    if (response == 0) {
                        $("#warning")
                            .text(
                                "Either username or password is wrong or does not exist, please try again.");
                    } else {
                        window.open("./main.html", "_self");
                    }
                }
            })
    }

}

/**
 * Function is called the login button for google account is pressed,
 * Var id_token will get a token from google and append it to the xhr and redirect it to the google login page
 * It will send back an object containing the email used to log in
 * If the email is registered, then the log in will be successful and user will be redirected to the main page
 * If the email is not registered, then the log in will fail and login.html will display an error message.
 */
function onSignIn(googleUser) {
    var id_token = googleUser.getAuthResponse().id_token;
    xhr.open('POST', './OV/googleLogin');
    xhr.setRequestHeader('Content-Type',
        'application/x-www-form-urlencoded');
    xhr.send(id_token);
    xhr.onload = function () {
        if (xhr.readyState == 4 && xhr.status == 200) {
            if (this.responseText == 0) {
                var auth2 = gapi.auth2.getAuthInstance();
                auth2
                    .signOut()
                    .then(
                        function () {
                            $("#warning")
                                .text(
                                    "Your google account is not permitted to access");
                        });
            } else {
                window.open("./main.html", "_self");
            }
        }
    }

}

/**
 * Sanitizing the input by removing all the character that is not permitted
 * @param input is used to handle the input to be sanitized
 * @return sanitized format of the input
 */
function sanitizeInput(input) {
    var out = '';
    for (i = 0; i < input.length; i++) {
        inp = input.charCodeAt(i);
        // remove special character else than '_'
        if ((inp >= 65 && inp <= 90) || (inp >= 97 && inp <= 122)
            || (inp == 95) || (inp >= 48 && inp <= 57)) {
            out += String.fromCharCode(inp);
        } else {
            inp = '%'
                + (Math.floor((Math.random() * 99) + 1)).toString();
            out += inp;
        }
    }
    return out;
}