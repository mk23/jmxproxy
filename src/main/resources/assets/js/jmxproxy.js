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

var endpointDataClass = function() {
    var items = {
        threads: [
            {
                label: 'Live Running Threads',
                data: []
            },
            {
                label: 'Peak Running Threads',
                data: []
            }
        ],
        classes: [
            {
                label: 'Current Classes Loaded',
                data: [],
            },
            {
                label: 'Total Classes Loaded',
                data: [],
            }
        ],
    };

    var graph = {
        grid: {
            hoverable: true,
            clickable: false,
        },
        pan: {
            interactive: true,
        },
        selection: {
            mode: 'x',
            shape: 'bevel',
        },
        series: {
            lines: {
                show: true,
            },
            points: {
                show: true,
            },
        },
        tooltip: true,
        tooltipOpts: {
            content: '%s: %y',
        },
        zoom: {
            interactive: true,
        },
        xaxis: {
            mode:       'time',
            timezone:   'browser',
            timeformat: '%H:%M:%S',
        },
        yaxis: {
            tickDecimals: 0,
        },
    };

    var redrawGraphs = function(name) {
        if (items.hasOwnProperty(name)) {
            $.plot($('#'+name+'-gr'), items[name], graph);
        }
    }

    var populateData = function() {
        ts = new Date().getTime();

        endpointHost.fetchData('/', function(data) {
            $('#summary-gc').text('');
            for (item in data) {
                if (data[item].lastIndexOf('java.lang:type=GarbageCollector', 0) === 0) {
                    endpointHost.fetchData('/'+data[item]+'?full=true', function(item) {
                        $('#summary-gc').append('Name = "'+item.Name+'"; Collections = '+item.CollectionCount+'; Time spent = '+prettifyTime(item.CollectionTime)+'<br>');
                    });
                }
            }
        });
        endpointHost.fetchData('/java.lang:type=ClassLoading?full=true', function(data) {
            $('#summary-cl').text(data.LoadedClassCount);
            $('#summary-cu').text(data.UnloadedClassCount);
            $('#summary-ct').text(data.TotalLoadedClassCount);
            $('#overview-cls-cl').text(data.LoadedClassCount);
            $('#overview-cls-cu').text(data.UnloadedClassCount);
            $('#overview-cls-ct').text(data.TotalLoadedClassCount);

            items.classes[0].data.push([ts, data.LoadedClassCount]);
            items.classes[1].data.push([ts, data.TotalLoadedClassCount]);
        });
        endpointHost.fetchData('/java.lang:type=Compilation?full=true', function(data) {
            $('#summary-jc').text(data.Name);
            $('#summary-jt').text(prettifyTime(data.TotalCompilationTime));
        });
        endpointHost.fetchData('/java.lang:type=Memory?full=true', function(data) {
            $('#summary-hh').text(prettifySize(data.HeapMemoryUsage.used));
            $('#summary-hc').text(prettifySize(data.HeapMemoryUsage.committed));
            $('#summary-hm').text(prettifySize(data.HeapMemoryUsage.max));
            $('#summary-hf').text(data.ObjectPendingFinalizationCount+' object(s)');
            $('#overview-mem-hh').text(prettifySize(data.HeapMemoryUsage.used));
            $('#overview-mem-hc').text(prettifySize(data.HeapMemoryUsage.committed));
            $('#overview-mem-hm').text(prettifySize(data.HeapMemoryUsage.max));
        });
        endpointHost.fetchData('/java.lang:type=OperatingSystem?full=true', function(data) {
            $('#summary-pt').text(prettifyTime(Math.floor(data.ProcessCpuTime / 1000000)));
            $('#summary-mr').text(prettifySize(data.TotalPhysicalMemorySize));
            $('#summary-ml').text(prettifySize(data.FreePhysicalMemorySize));
            $('#summary-ms').text(prettifySize(data.TotalSwapSpaceSize));
            $('#summary-mp').text(prettifySize(data.FreeSwapSpaceSize));
            $('#summary-sn').text(data.Name+'/'+data.Version);
            $('#summary-sa').text(data.Arch);
            $('#summary-sp').text(data.AvailableProcessors);
            $('#summary-sm').text(prettifySize(data.CommittedVirtualMemorySize));
            $('#overview-cpu-up').text(prettifyPercent(data.ProcessCpuLoad));
            $('#overview-cpu-us').text(prettifyPercent(data.SystemCpuLoad));
        });
        endpointHost.fetchData('/java.lang:type=Runtime?full=true', function(data) {
            $('#summary-ut').text(prettifyTime(data.Uptime));
            $('#summary-vm').text(data.VmName);
            $('#summary-vv').text(data.VmVendor);
            $('#summary-vn').text(data.Name);
        });
        endpointHost.fetchData('/java.lang:type=Threading?full=true', function(data) {
            $('#summary-tc').text(data.ThreadCount);
            $('#summary-tp').text(data.PeakThreadCount);
            $('#summary-td').text(data.DaemonThreadCount);
            $('#summary-tt').text(data.TotalStartedThreadCount);
            $('#overview-thr-tc').text(data.ThreadCount);
            $('#overview-thr-tp').text(data.PeakThreadCount);
            $('#overview-thr-tt').text(data.TotalStartedThreadCount);

            items.threads[0].data.push([ts, data.ThreadCount]);
            items.threads[1].data.push([ts, data.PeakThreadCount]);
        });

        setTimeout(populateData, jmxproxyConf.cache_duration * 60 * 1000);
    };

    setTimeout(populateData, 0);

    $('a[data-toggle="tab"]').on('shown', function(e) {
        redrawGraphs(e.target.hash.substring(1));
    });

    return {
        populateData: populateData,
        redrawGraphs: redrawGraphs,
    };
};

var endpointHostClass = function(host) {
    var auth = null;
    var data = null;

    var fetchName = function() {
        return auth == null ? host : auth.username+'@'+host;
    };
    var resetAuth = function(username, password) {
        auth = {
            "username": username,
            "password": password
        }
        checkHost();
    };
    var fetchData = function(item, callback) {
        if (auth != null) {
            $.post("/jmxproxy/"+host+item, auth, callback, "json")
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
        fetchData('/java.lang:type=Runtime/Uptime', function(test) {
            $(document).attr('title', 'JMXProxy - ' + fetchName());
            $('#summary-cn').text(fetchName());
            $('#endpoint-label').text(fetchName());
            $('#endpoint-tabui').show();

            endpointData = endpointDataClass();
        });
    };

    checkHost();

    return {
        resetAuth: resetAuth,
        fetchData: fetchData,
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

function prettifyPercent(s) {
    return s.toFixed(2) + '%';
}

function displayError(text) {
    if (text != null) {
        $('#endpoint-error').text(text);
        $('#endpoint-alert').show();
    }
}
