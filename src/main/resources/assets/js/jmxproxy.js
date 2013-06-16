var documentInfoClass = function(defaultTitle, defaultLabel) {
    return {
        reset: function() {
            $('window').title = defaultTitle;
            $('#endpoint-title').text(defaultLabel);
            $('#endpoint-alert').hide();
            $('#endpoint-tabui').hide();
        },
    }
};

var endpointHostClass = function(host) {
    var creds = null;

    var fetchName = function() {
        return creds == null ? host : creds.username+'@'+host;
    };
    var resetAuth = function(username, password) {
        creds = {
            "username": username,
            "password": password
        }
        checkHost();
    };
    var fetchData = function(item, callback) {
        if (creds != null) {
            $.post("/jmxproxy/"+host+item, creds, callback, "json")
            .fail(function(jqXHR) {
                if (jqXHR.status == 401) {
                    $('#endpoint-auth').modal('show');
                } else if (jqXHR.status == 404) {
                    displayError('Selected endpoint is unavailable.');
                }
            });
        } else {
            $.getJSON("/jmxproxy/"+host+item, callback)
            .fail(function(jqXHR) {
                if (jqXHR.status == 401) {
                    $('#endpoint-auth').modal('show');
                } else if (jqXHR.status == 404) {
                    displayError('Selected endpoint is unavailable.');
                }
            });
        }
    };
    var checkHost = function() {
        fetchData('/java.lang:type=Runtime/Uptime', function(data) {
            $(document).attr('title', 'JMXProxy - ' + endpointHost.fetchName());
            $('#endpoint-label').text(endpointHost.fetchName());
            $('#endpoint-tabui').show();

            $('a[data-toggle="tab"]').on('show', function (e) {
                endpointHost['view'+e.target.text]();
            });
        });
    };
    var viewSummary = function() {
        $('#summary_cn').text(fetchName());

        fetchData('/', function(data) {
            $('#summary_gc').text('');
            for (item in data) {
                if (data[item].lastIndexOf('java.lang:type=GarbageCollector', 0) === 0) {
                    fetchData('/'+data[item]+'?full=true', function(item) {
                        $('#summary_gc').append('Name = "'+item.Name+'"; Collections = '+item.CollectionCount+'; Time spent = '+prettifyTime(item.CollectionTime)+'<br>');
                    });
                }
            }
        });
        fetchData('/java.lang:type=ClassLoading?full=true', function(data) {
            $('#summary_cc').text(data.LoadedClassCount);
            $('#summary_ct').text(data.TotalLoadedClassCount);
            $('#summary_cu').text(data.UnloadedClassCount);
        });
        fetchData('/java.lang:type=Compilation?full=true', function(data) {
            $('#summary_jc').text(data.Name);
            $('#summary_jt').text(prettifyTime(data.TotalCompilationTime));
        });
        fetchData('/java.lang:type=Memory?full=true', function(data) {
            $('#summary_hh').text(prettifySize(data.HeapMemoryUsage.used));
            $('#summary_hc').text(prettifySize(data.HeapMemoryUsage.committed));
            $('#summary_hm').text(prettifySize(data.HeapMemoryUsage.max));
            $('#summary_hf').text(prettifySize(data.ObjectPendingFinalizationCount));
        });
        fetchData('/java.lang:type=OperatingSystem?full=true', function(data) {
            $('#summary_pt').text(prettifyTime(Math.floor(data.ProcessCpuTime / 1000000)));
            $('#summary_mr').text(prettifySize(data.TotalPhysicalMemorySize));
            $('#summary_ml').text(prettifySize(data.FreePhysicalMemorySize));
            $('#summary_ms').text(prettifySize(data.TotalSwapSpaceSize));
            $('#summary_mp').text(prettifySize(data.FreeSwapSpaceSize));
            $('#summary_sn').text(data.Name+'/'+data.Version);
            $('#summary_sa').text(data.Arch);
            $('#summary_sp').text(data.AvailableProcessors);
            $('#summary_sm').text(prettifySize(data.CommittedVirtualMemorySize));
        });
        fetchData('/java.lang:type=Runtime?full=true', function(data) {
            $('#summary_ut').text(prettifyTime(data.Uptime));
            $('#summary_vm').text(data.VmName);
            $('#summary_vv').text(data.VmVendor);
            $('#summary_vn').text(data.Name);
        });
        fetchData('/java.lang:type=Threading?full=true', function(data) {
            $('#summary_tc').text(data.ThreadCount);
            $('#summary_tp').text(data.PeakThreadCount);
            $('#summary_td').text(data.DaemonThreadCount);
            $('#summary_tt').text(data.TotalStartedThreadCount);
        });
    };

    checkHost();

    return {
        fetchName: fetchName,
        resetAuth: resetAuth,
        fetchData: fetchData,
        checkHost: checkHost,
        viewSummary: viewSummary
    };
};

var jmxproxyConf;
var documentInfo;
var endpointHost;

$(document).ready(function() {
    documentInfo = documentInfoClass($(document).attr('title'), $('#endpoint-label').text());

    $('#endpoint-input').keypress(function(e) {
        documentInfo.reset();

        if (e.keyCode == 13 && this.validity.valid) {
            documentInfo.reset();
            endpointHost = endpointHostClass(this.value);

            this.value = '';
            this.blur();
        }
    });

    $('#endpoint-creds').submit(function(e) {
        documentInfo.reset();
        endpointHost.resetAuth($('#endpoint-user').val(), $('#endpoint-pass').val());

        $('#endpoint-auth').modal('hide')
    });

    $.getJSON( "/jmxproxy/config", function(data) {
        jmxproxyConf = data;
        endpointList = [];

        if (data.allowed_endpoints.length > 0) {
            $.each(data.allowed_endpoints, function(key, val) {
                endpointList.push('<li><a href="#" onclick="documentInfo.reset(); endpointHost = endpointHostClass(this.innerHTML)">'+val+'</a></li>');
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

function prettifyTime(s) {
    v = s % 86400000;
    d = (s - v) / 86400000;
    s = v;

    v = s % 3600000;
    h = (s - v) / 3600000;
    s = v;

    v = s % 60000;
    m = (s - v) / 60000;
    s = v / 1000;

    parts = [];
    if (d) {
        parts.push(d + ' day' + (d == 1 ? '' : 's'));
    }
    if (h) {
        parts.push(h + ' hour' + (h == 1 ? '' : 's'));
    }
    if (m) {
        parts.push(m + ' minute' + (m == 1 ? '' : 's'));
    }
    if (s) {
        parts.push(s + ' second' + (s == 1 ? '' : 's'));
    }

    return parts.join(' ');
}

function prettifySize(s) {
    if (s > (Math.pow(1024, 4))) {
        return (s / Math.pow(1024, 4)).toFixed(2) + ' TB';
    }
    if (s > (Math.pow(1024, 3))) {
        return (s / Math.pow(1024, 3)).toFixed(2) + ' GB';
    }
    if (s > (Math.pow(1024, 2))) {
        return (s / Math.pow(1024, 2)).toFixed(2) + ' MB';
    }
    if (s > (Math.pow(1024, 1))) {
        return (s / Math.pow(1024, 1)).toFixed(2) + ' KB';
    }

    return s + ' bytes';
}

function displayError(text) {
    if (text != null) {
        $('#endpoint-error').text(text);
        $('#endpoint-alert').show();
    }
}
