var documentInfo;
var jmxproxyConf;
var endpointHost;
var endpointAuth;

$(document).ready(function() {
    documentInfo = function() {
        var defaultTitle = $(document).attr('title');
        var defaultLabel = $('#endpoint-label').text();

        return {
            reset: function() {
                endpointAuth = null;

                $('window').title = defaultTitle;
                $('#endpoint-title').text(defaultLabel);
                $('#endpoint-alert').hide();
                $('#endpoint-tabui').hide();
            }
        }
    }();


    $('#endpoint-input').keypress(function(e) {
        documentInfo.reset();

        if (e.keyCode == 13 && this.validity.valid) {
            ep = this.value;
            this.value = '';
            this.blur();

            testEndpoint(ep);
        }
    });
    $('#endpoint-creds').submit(function(e) {
        documentInfo.reset();

        endpointAuth = {
            "username": $('#endpoint-user').val(),
            "password": $('#endpoint-pass').val()
        }
        $('#endpoint-auth').modal('hide')

        testEndpoint(endpointHost);
    });

    $.getJSON( "/jmxproxy/config", function(data) {
        jmxproxyConf = data;
        endpointList = [];

        if (data.allowed_endpoints.length > 0) {
            $.each(data.allowed_endpoints, function(key, val) {
                endpointList.push('<li><a href="#" onclick="documentInfo.reset(); testEndpoint(this.innerHTML)">'+val+'</a></li>');
            });

            $('#endpoint-list').html(endpointList.join(''))
            $('#endpoint-group').show();
        } else {
            $('#endpoint-entry').show();
        }
    })
    .fail(function() {
        displayError('Malformed configuration data recieved from the server.')
    });
});

function testEndpoint(item) {
    endpointHost = item;
    if (endpointAuth != null) {
        $.post("/jmxproxy/"+endpointHost+"/java.lang:type=Runtime/Uptime", endpointAuth, function(data) {
            return loadEndpoint();
        }, "json")
        .fail(function(jqXHR) {
            if (jqXHR.status == 401) {
                $('#endpoint-auth').modal('show');
            } else if (jqXHR.status == 404) {
                displayError('Selected endpoint is unavailable.');
            }
        });
    } else {
        $.getJSON("/jmxproxy/"+endpointHost+"/java.lang:type=Runtime/Uptime", function(data) {
            return loadEndpoint();
        })
        .fail(function(jqXHR) {
            if (jqXHR.status == 401) {
                $('#endpoint-auth').modal('show');
            } else if (jqXHR.status == 404) {
                displayError('Selected endpoint is unavailable.');
            }
        });
    }
}

function loadEndpoint() {
    head = endpointAuth == null ? endpointHost : endpointAuth.username+'@'+endpointHost;
    $(document).attr('title', 'JMXProxy - ' + head);
    $('#endpoint-label').text(head);
    $('#endpoint-tabui').show();
}

function displayError(text) {
    if (text != null) {
        $('#endpoint-error').text(text);
        $('#endpoint-alert').show();
    }
}
