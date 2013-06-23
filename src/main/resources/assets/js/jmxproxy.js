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
        'overview-mem-gr': [
            {
                label: 'Heap Used',
                data: [],
            },
        ],
        'overview-thr-gr': [
            {
                label: 'Live Threads',
                data: [],
            },
        ],
        'overview-cls-gr': [
            {
                label: 'Loaded Classes',
                data: [],
            },
        ],
        'overview-cpu-gr': [
            {
                label: 'Process CPU',
                data: [],
            },
        ],
        'threads-gr': [
            {
                label: 'Live Running Threads',
                data: [],
            },
            {
                label: 'Peak Running Threads',
                data: [],
            },
        ],
        'classes-gr': [
            {
                label: 'Current Classes Loaded',
                data: [],
            },
            {
                label: 'Total Classes Loaded',
                data: [],
            },
        ],
        'memory-gr': {
            selected: 'hm',
            hm: [
                {
                    label: 'Heap Memory Usage',
                    data: [],
                    info: {},
                },
            ],
            nm: [
                {
                    label: 'Non Heap Memory Usage',
                    data: [],
                    info: {},
                },
            ],
        },
    };

    var graph = {
        full: {
            legend: {
                show: true,
                backgroundOpacity: 0.1,
            },
            pan: {
                interactive: true,
            },
            zoom: {
                interactive: true,
            },
        },
        bare: {
            legend: {
                show: false,
            },
        },
    };

    var redrawMemory = function(item) {
        items['memory-gr'].selected = item;
        $('#memory-text').text(items['memory-gr'][item][0].label);
        $('#memory-hh').text(prettifySize(items['memory-gr'][item][0].info.used));
        $('#memory-hc').text(prettifySize(items['memory-gr'][item][0].info.committed));
        $('#memory-hm').text(prettifySize(items['memory-gr'][item][0].info.max));

        redrawGraphs('memory-gr', true, prettifySize);
    }

    var redrawGraphs = function(name, bare, type) {
        if (items.hasOwnProperty(name) && $('#'+name).is(':visible')) {
            opts = {
                grid: {
                    hoverable: true,
                    clickable: false,
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
                xaxis: {
                    mode: 'time',
                    timezone: 'browser',
                    timeformat: '%H:%M',
                },
                yaxis: {
                    labelWidth: 28,
                    tickFormatter: typeof type !== 'undefined' ? type : Math.floor,
                },
            };

            if (name != 'memory-gr') {
                $.plot($('#'+name), items[name], $.extend(opts, graph[bare ? 'bare' : 'full']));
            } else {
                $.plot($('#'+name), items[name][items[name].selected], $.extend(opts, graph.full));
            }
        }
    };

    var populateData = function() {
        ts = new Date().getTime();

        endpointHost.fetchData('/', function(data) {
            $('#summary-gc').text('');
            $('#memory-gc').text('');
            $('#memory-bar-hm').html('');
            $('#memory-bar-nm').html('');
            for (item in data) {
                if (data[item].lastIndexOf('java.lang:type=GarbageCollector', 0) === 0) {
                    endpointHost.fetchData('/'+data[item]+'?full=true', function(item) {
                        $('#summary-gc').append('Name = "'+item.Name+'"; Collections = '+item.CollectionCount+'; Time spent = '+prettifyTime(item.CollectionTime, 2)+'<br>');
                        $('#memory-gc').append(prettifyTime(item.CollectionTime, 2)+' on '+item.Name+' ('+item.CollectionCount+' collections)<br>');
                    });
                } else if (data[item].lastIndexOf('java.lang:type=MemoryPool') === 0) {
                    endpointHost.fetchData('/'+data[item]+'?full=true', function(item) {
                        if (typeof items['memory-gr'][item.Name] === 'undefined') {
                            items['memory-gr'][item.Name] = [
                                {
                                    label: item.Name+' Memory Usage',
                                    data: [ [ts, item.Usage.used] ],
                                    info: {
                                        used: item.Usage.used,
                                        committed: item.Usage.committed,
                                        max: item.Usage.max,
                                    },
                                },
                            ];
                        } else {
                            items['memory-gr'][item.Name][0].data.push([ts, item.Usage.used]);
                            items['memory-gr'][item.Name][0].info = {
                                used: item.Usage.used,
                                committed: item.Usage.committed,
                                max: item.Usage.max,
                            };
                        }
                        if (item.Type == 'HEAP') {
                            $('#memory-bar-hm').append(
                                $('<div/>')
                                .on('click', function() {
                                    redrawMemory(item.Name);
                                })
                                .attr('class', 'progress progress-success')
                                .attr('title', 'Memory Pool "'+item.Name+'"')
                                .attr('data-toggle', 'tooltip')
                                .attr('style', 'width:100%')
                                .append(
                                    $('<div/>')
                                    .attr('class', 'bar')
                                    .attr('style', 'width:'+prettifyPercent(100 * item.Usage.used / item.Usage.max))
                                    .text(prettifyPercent(100 * item.Usage.used / item.Usage.max))
                                )
                            );
                        } else if (item.Type == 'NON_HEAP') {
                            $('#memory-bar-nm').append(
                                $('<div/>')
                                .on('click', function() {
                                    redrawMemory(item.Name);
                                })
                                .attr('class', 'progress progress-info')
                                .attr('title', 'Memory Pool "'+item.Name+'"')
                                .attr('data-toggle', 'tooltip')
                                .attr('style', 'width:100%')
                                .append(
                                    $('<div/>')
                                    .attr('class', 'bar')
                                    .attr('style', 'width:'+prettifyPercent(100 * item.Usage.used / item.Usage.max))
                                    .text(prettifyPercent(100 * item.Usage.used / item.Usage.max))
                                )
                            );
                        }
                        redrawGraphs('memory-gr', true, prettifySize);
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

            items['classes-gr'][0].data.push([ts, data.LoadedClassCount]);
            items['classes-gr'][1].data.push([ts, data.TotalLoadedClassCount]);
            redrawGraphs('classes-gr');

            items['overview-cls-gr'][0].data.push([ts, data.LoadedClassCount]);
            redrawGraphs('overview-cls-gr', true);
        });
        endpointHost.fetchData('/java.lang:type=Compilation?full=true', function(data) {
            $('#summary-jc').text(data.Name);
            $('#summary-jt').text(prettifyTime(data.TotalCompilationTime, 2));
        });
        endpointHost.fetchData('/java.lang:type=Memory?full=true', function(data) {
            $('#overview-mem-hh').text(prettifySize(data.HeapMemoryUsage.used));
            $('#overview-mem-hc').text(prettifySize(data.HeapMemoryUsage.committed));
            $('#overview-mem-hm').text(prettifySize(data.HeapMemoryUsage.max));

            $('#memory-hh').text(prettifySize(data.HeapMemoryUsage.used));
            $('#memory-hc').text(prettifySize(data.HeapMemoryUsage.committed));
            $('#memory-hm').text(prettifySize(data.HeapMemoryUsage.max));

            $('#summary-hh').text(prettifySize(data.HeapMemoryUsage.used));
            $('#summary-hc').text(prettifySize(data.HeapMemoryUsage.committed));
            $('#summary-hm').text(prettifySize(data.HeapMemoryUsage.max));
            $('#summary-hf').text(data.ObjectPendingFinalizationCount+' object(s)');

            items['overview-mem-gr'][0].data.push([ts, data.HeapMemoryUsage.used]);
            redrawGraphs('overview-mem-gr', true, prettifySize);

            items['memory-gr']['hm'][0].data.push([ts, data.HeapMemoryUsage.used]);
            items['memory-gr']['nm'][0].data.push([ts, data.NonHeapMemoryUsage.used]);
            items['memory-gr']['hm'][0].info = {
                used: data.HeapMemoryUsage.used,
                committed: data.HeapMemoryUsage.committed,
                max: data.HeapMemoryUsage.max,
            };
            items['memory-gr']['nm'][0].info = {
                used: data.NonHeapMemoryUsage.used,
                committed: data.NonHeapMemoryUsage.committed,
                max: data.NonHeapMemoryUsage.max,
            };
            redrawGraphs('memory-gr', true, prettifySize);
        });
        endpointHost.fetchData('/java.lang:type=OperatingSystem?full=true', function(data) {
            $('#summary-pt').text(prettifyTime(data.ProcessCpuTime / 1000000, 3));
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

            items['overview-cpu-gr'][0].data.push([ts, data.ProcessCpuLoad]);
            redrawGraphs('overview-cpu-gr', true, prettifyPercent);
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

            items['threads-gr'][0].data.push([ts, data.ThreadCount]);
            items['threads-gr'][1].data.push([ts, data.PeakThreadCount]);
            redrawGraphs('threads-gr');

            items['overview-thr-gr'][0].data.push([ts, data.ThreadCount]);
            redrawGraphs('overview-thr-gr', true);
        });

        setTimeout(populateData, jmxproxyConf.cache_duration * 60 * 1000);
    };

    setTimeout(populateData, 0);

    $('a[data-toggle="tab"]').on('shown', function(e) {
        if (e.target.text == 'Overview') {
            redrawGraphs('overview-mem-gr', true, prettifySize);
            redrawGraphs('overview-thr-gr', true);
            redrawGraphs('overview-cls-gr', true);
            redrawGraphs('overview-cpu-gr', true, prettifyPercent);
        } else if (e.target.text == 'Memory') {
            redrawGraphs('memory-gr', true, prettifySize);
        } else if ($.inArray(e.target.text, ['Classes', 'Threads']) !== -1) {
            redrawGraphs(e.target.text.toLowerCase()+'-gr');
        }
    });

    return {
        populateData: populateData,
        redrawMemory: redrawMemory,
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

    $('#memory-btn-hm').on('click', function() {
        endpointData.redrawMemory('hm');
    });
    $('#memory-btn-nm').on('click', function() {
        endpointData.redrawMemory('nm');
    });

    $.getJSON('/jmxproxy/config', function(data) {
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

function prettifyTime(s, n) {
    v = s % 86400000;
    d = (s - v) / 86400000;
    s = v;

    v = s % 3600000;
    h = (s - v) / 3600000;
    s = v;

    v = s % 60000;
    m = (s - v) / 60000;
    s = (v / 1000).toFixed(typeof n !== 'undefined' ? n : 0);

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

    return s + ' B';
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
