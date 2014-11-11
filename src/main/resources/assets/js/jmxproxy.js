var endpointDataClass = function() {
    var items = {
        'overview-mem-gr': [{
            'label': 'Heap Used',
            'data':  [],
        }],
        'overview-thr-gr': [{
            'label': 'Live Threads',
            'data':  [],
        }],
        'overview-cls-gr': [{
            'label': 'Loaded Classes',
            'data':  [],
        }],
        'overview-cpu-gr': [{
            'label': 'Process CPU',
            'data':  [],
        }],
        'threads-gr': [
            {
                'label': 'Live Running Threads',
                'data':  [],
            },
            {
                'label': 'Peak Running Threads',
                'data':  [],
            },
        ],
        'classes-gr': [
            {
                'label': 'Current Classes Loaded',
                'data':  [],
            },
            {
                'label': 'Total Classes Loaded',
                'data':  [],
            },
        ],
        'memory-gr': {
            'selected': 'hm',
            'hm': [{
                'label': 'Heap Memory Usage',
                'data':  [],
                'info':  {},
            }],
            'nm': [{
                'label': 'Non Heap Memory Usage',
                'data':  [],
                'info':  {},
            }],
        },
    };

    var graph = {
        'full': {
            'legend': {
                'show': true,
                'backgroundOpacity': 0.1,
            },
            'pan': {
                'interactive': true,
            },
            'zoom': {
                'interactive': true,
            },
        },
        'bare': {
            'legend': {
                'show': false,
            },
        },
    };

    var refreshMemory = function(item) {
        items['memory-gr'].selected = item;
        $('#memory-text').text(items['memory-gr'][item][0].label);
        $('#memory-hh').text(formatSize(items['memory-gr'][item][0].info.used));
        $('#memory-hc').text(formatSize(items['memory-gr'][item][0].info.committed));
        $('#memory-hm').text(formatSize(items['memory-gr'][item][0].info.max));

        refreshGraphs('memory-gr', true, formatSize);
    }

    var refreshGraphs = function(name, bare, type) {
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
                    labelWidth: 64,
                    tickFormatter: $.type(type) !== 'undefined' ? type : Math.floor,
                },
            };

            if (name != 'memory-gr') {
                $.plot($('#'+name), items[name], $.extend(opts, graph[bare ? 'bare' : 'full']));
            } else {
                $.plot($('#'+name), items[name][items[name].selected], $.extend(opts, graph.full));
            }
        }
    };

    var buildBeanTree = function() {
    }
    /*
    var buildBeanTree = function() {
        var addHeader = function(tree, list, name) {
            find = $.grep(tree, function(o, i) {
                return o.text == name;
            });

            if (find.length > 0) {
                return find[0];
            } else {
                item = {
                    text: name,
                    icon: 'glyphicon glyphicon-folder-close',
                    nodes: [],
                };
                tree.push(item);
                list.push(item);
                return item;
            }
        }

        endpointHost.fetchData('/', function(data) {
            data.sort();

            tree = [];
            list = [];
            for (bean in data) {
                head = data[bean].split(':')[0].replace(/"/g, '');

                node = addHeader(tree, list, head);

                body = data[bean].split(':').pop().split(',');
                for (part in body) {
                    name = body[part].split('=').pop().replace(/"/g, '');
                    if (part == body.length - 1) {
                        size = name.length;
                        bits = name.split('.');
                        for (i = 0; i < bits.length - 1 && size > 25; i++) {
                            size -= (bits[i].length - 1);
                            bits[i] = bits[i][0];
                        }
                        name = bits.join('.');

                        item = {
                            text: name,
                            icon: 'glyphicon glyphicon-file',
                            href: data[bean],
                        };

                        node.nodes.push(item);
                        list.push(item);
                    } else {
                        node = addHeader(node.nodes, list, name);
                    }
                }
            }

            $('#mbeans-tree').on('nodeSelected', function(e, item) {
                console.log(item);
            });
            $('#mbeans-tree').treeview({
                data: tree,
                levels: 1,
                nodeIcon: 'glyphicon',
                expandIcon: 'glyphicon glyphicon-chevron-right pull-right',
                collapseIcon: 'glyphicon glyphicon-chevron-down pull-right',
            });
            $.each($('#mbeans-tree li.list-group-item'), function(key, val) {
                console.log(key, val, list[key]);
            });
        });
    }
    var buildBeanTree = function() {
        endpointHost.fetchData('/', function(data) {
            data.sort();
            for (bean in data) {
                body = data[bean].split(':').pop().split(',');
                for (part in body) {
                    name = body[part].split('=').pop().replace(/"/g, '');
                    list = html.find('ul:first');
                    if (list.length == 0) {
                        list = $('<ul/>').addClass('nav nav-pills nav-stacked');
                        html.append(list);
                    }

                    if (part == body.length - 1) {
                        item = $('<li/>')
                            .append($('<a/>')
                                .attr('href', '#')
                                .data('bean', data[bean])
                                .click(function() {
                                    populateAttr($(this).data('bean'));
                                })
                                .append($('<i/>').addClass('glyphicon glyphicon-file'))
                                .append(name)
                            );
                        list.append(item);
                    } else {
                        item = list.find('li:contains('+name+'):first').parent();
                        if (item.length == 0) {
                            item = listHeader(name);
                            list.append(item);
                        }
                        html = item;
                    }
                }
            }
        });
    };
    */

    var populateAttr = function(bean) {
        endpointHost.fetchData('/'+bean+'?full=true', function(data) {
            $('#mbeans-data').html($('<table/>')
                .addClass('table table-condensed table-striped table-bordered')
                .append($('<thead/>'))
                .append($('<tbody/>'))
            );
            $('#mbeans-data > table').dataTable({
                'bDestroy':        true,
                'bFilter':         false,
                'bInfo':           false,
                'bLengthChange':   false,
                'bPaginate':       false,
                'sScrollX':        '100%',
                'sPaginationType': 'bootstrap',
                'fnRowCallback':   function(nRow, aData) {
                    if ($.type(nRow.jmxProxyConfigured) === 'undefined') {
                        nRow.jmxProxyConfigured = true;
                    } else {
                        return;
                    }

                    len = null;
                    dat = {
                        'bDestroy':        true,
                        'bFilter':         false,
                        'bInfo':           false,
                        'bLengthChange':   false,
                        'sScrollX':        '100%',
                        'sPaginationType': 'bootstrap',
                    };

                    if ($.type(aData.val) === 'array' && aData.val.length > 0) {
                        if ($.type(aData.val[0]) === 'object') {
                            dat['aaData'] = aData.val;
                            dat['aoColumns'] = $.map(aData.val[0], function(v, k) {
                                return {'mData': k, 'sTitle': k};
                            });
                        } else {
                            dat['aaData'] = $.map(aData.val, function(v, k) {
                                return {'val': v};
                            });
                            dat['aoColumns'] = [
                                {'mData': 'val', 'sTitle': aData.key}
                            ];
                        }
                        len = dat['aaData'].length;
                    } else if ($.type(aData.val) === 'object') {
                        dat['aaData'] = $.map(aData.val, function(v, k) {
                            return {'key': k, 'val': v};
                        });
                        dat['aoColumns'] = [
                            {'mData': 'key', 'sTitle': 'Name'},
                            {'mData': 'val', 'sTitle': 'Value'},
                        ];
                        len = dat['aaData'].length;
                    } else if ($.type(aData.val) === 'string' && aData.val.length > 50) {
                        len = aData.val.length;
                    }

                    if (len !== null) {
                        $('td:eq(0)', nRow)
                            .empty()
                            .click(function () {
                                $('td:eq(1) > :eq(0)', $(this).parent()).toggle();
                                $('td:eq(1) > :eq(1)', $(this).parent()).toggle();

                                $('div.dataTables_scrollBody > table', $('#mbeans-data')).dataTable().fnAdjustColumnSizing();
                                if ($('div.dataTables_scrollBody > table', $(this).parent()).length > 0) {
                                    $('div.dataTables_scrollBody > table', $(this).parent()).dataTable().fnAdjustColumnSizing();
                                }
                            })
                            .append($('<a/>')
                                .attr('href', '#')
                                .attr('title', 'Expand')
                                .text(aData.key)
                            )
                            .append($('<span/>')
                                .addClass('badge badge-important pull-right')
                                .text(len)
                            );

                        if ($.type(dat['aaData']) !== 'undefined') {
                            $('td:eq(1)', nRow)
                                .empty()
                                .append($('<table/>')
                                    .addClass('table table-condensed table-striped table-bordered')
                                    .append($('<thead/>'))
                                    .append($('<tbody/>'))
                                )
                                .append($('<div/>')
                                    .append($('<span/>')
                                        .addClass('label label-info')
                                        .text($.type(aData.val))
                                    )
                                );

                            $('td:eq(1) > table', nRow).dataTable(dat);
                            $('td:eq(1) > :first-child', nRow).hide();
                            if ($('div.dataTables_paginate > ul > li', nRow).length <= 3) {
                                $('div.dataTables_paginate', nRow).hide();
                            }
                        } else {
                            $('td:eq(1)', nRow)
                                .empty()
                                .append($('<span/>')
                                    .css('white-space', 'nowrap')
                                    .text(aData.val)
                                )
                                .append($('<div/>')
                                    .append($('<span/>')
                                        .addClass('label label-info')
                                        .text($.type(aData.val))
                                    )
                                    .append($('<span/>')
                                        .text(' '+aData.val.substring(0, 50)+'...')
                                    )
                                );

                            $('td:eq(1) > :first-child', nRow).hide();
                        }
                    } else if (aData.val === null) {
                        $('td:eq(1)', nRow)
                            .html($('<span/>').addClass('text-error').html($('<strong/>').text(aData.val)))
                    } else if ($.type(aData.val) === 'boolean') {
                        $('td:eq(1)', nRow)
                            .html($('<span/>').addClass('text-warning').text(aData.val))
                    } else if ($.type(aData.val) !== 'string') {
                        $('td:eq(1)', nRow)
                            .html($('<span/>').addClass('text-success').text(aData.val))
                    }
                },
                'fnHeaderCallback': function(nHead) {
                    $('th:eq(0):first', nHead).text('Name')
                    $('th:eq(1):first', nHead)
                        .empty()
                        .append(
                            $('<i/>')
                            .attr('title', 'Refresh')
                            .data('toggle', 'tooltip')
                            .data('placement', 'right')
                            .tooltip()
                            .click(function() {
                                populateAttr(bean);
                            })
                            .append(
                                $('<span/>')
                                .addClass('glyphicon glyphicon-refresh')
                            )
                        )
                        .text(' Value');
                },
                'aaData': $.map(data, function(v, k) {
                    return {'key': k, 'val': v};
                }),
                'aoColumns': [
                    {'mData': 'key'},
                    {'mData': 'val', 'sWidth': '100%'},
                ],
            });

            $('div.dataTables_scrollBody > table', $('#mbeans-data')).dataTable().fnAdjustColumnSizing();
            if ($('#mbeans-data > div.dataTables_wrapper > div.dataTables_paginate > ul > li').length <= 3) {
                $('#mbeans-data > div.dataTables_wrapper > div.dataTables_paginate').hide();
            }
        });
    }

    var gatherObjects = function() {
        ts = new Date().getTime();

        endpointHost.fetchData('/', function(data) {
            $('#summary-gc').empty();
            $('#memory-gc').empty();
            $('#memory-bar-hm').empty();
            $('#memory-bar-nm').empty();
            for (item in data) {
                if (data[item].lastIndexOf('java.lang:type=GarbageCollector', 0) === 0) {
                    endpointHost.fetchData('/'+data[item]+'?full=true', function(item) {
                        $('#summary-gc').append('Name = "'+item.Name+'"; Collections = '+item.CollectionCount+'; Time spent = '+formatTime(item.CollectionTime, 2)+'<br>');
                        $('#memory-gc').append(formatTime(item.CollectionTime, 2)+' on '+item.Name+' ('+item.CollectionCount+' collections)<br>');
                    });
                } else if (data[item].lastIndexOf('java.lang:type=MemoryPool') === 0) {
                    endpointHost.fetchData('/'+data[item]+'?full=true', function(item) {
                        if (item.Usage.max <= 0) {
                            return;
                        }
                        if ($.type(items['memory-gr'][item.Name]) === 'undefined') {
                            items['memory-gr'][item.Name] = [{
                                'label': item.Name+' Memory Usage',
                                'data': [ [ts, item.Usage.used] ],
                                'info': {
                                    'used':      item.Usage.used,
                                    'committed': item.Usage.committed,
                                    'max':       item.Usage.max,
                                },
                            }];
                        } else {
                            items['memory-gr'][item.Name][0].data.push([ts, item.Usage.used]);
                            items['memory-gr'][item.Name][0].info = {
                                'used':      item.Usage.used,
                                'committed': item.Usage.committed,
                                'max':       item.Usage.max,
                            };
                        }
                        if (item.Type == 'HEAP') {
                            $('#memory-bar-hm')
                            .append(
                                $('<div/>')
                                .addClass('progress')
                                .attr('title', 'Memory Pool "'+item.Name+'"')
                                .data('toggle', 'tooltip')
                                .data('placement', 'left')
                                .tooltip()
                                .click(function() {
                                    refreshMemory(item.Name);
                                })
                                .append(
                                    $('<div/>')
                                    .addClass('progress-bar progress-bar-success')
                                    .width(formatPercent(100 * item.Usage.used / item.Usage.max))
                                    .text(formatPercent(100 * item.Usage.used / item.Usage.max))
                                )
                            );
                        } else if (item.Type == 'NON_HEAP') {
                            $('#memory-bar-nm')
                            .append(
                                $('<div/>')
                                .addClass('progress')
                                .attr('title', 'Memory Pool "'+item.Name+'"')
                                .data('toggle', 'tooltip')
                                .data('placement', 'left')
                                .tooltip()
                                .click(function() {
                                    refreshMemory(item.Name);
                                })
                                .append(
                                    $('<div/>')
                                    .addClass('progress-bar progress-bar-info')
                                    .width(formatPercent(100 * item.Usage.used / item.Usage.max))
                                    .text(formatPercent(100 * item.Usage.used / item.Usage.max))
                                )
                            );
                        }
                        refreshGraphs('memory-gr', true, formatSize);
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
            refreshGraphs('classes-gr');

            items['overview-cls-gr'][0].data.push([ts, data.LoadedClassCount]);
            refreshGraphs('overview-cls-gr', true);
        });
        endpointHost.fetchData('/java.lang:type=Compilation?full=true', function(data) {
            $('#summary-jc').text(data.Name);
            $('#summary-jt').text(formatTime(data.TotalCompilationTime, 2));
        });
        endpointHost.fetchData('/java.lang:type=Memory?full=true', function(data) {
            $('#overview-mem-hh').text(formatSize(data.HeapMemoryUsage.used));
            $('#overview-mem-hc').text(formatSize(data.HeapMemoryUsage.committed));
            $('#overview-mem-hm').text(formatSize(data.HeapMemoryUsage.max));

            $('#memory-hh').text(formatSize(data.HeapMemoryUsage.used));
            $('#memory-hc').text(formatSize(data.HeapMemoryUsage.committed));
            $('#memory-hm').text(formatSize(data.HeapMemoryUsage.max));

            $('#summary-hh').text(formatSize(data.HeapMemoryUsage.used));
            $('#summary-hc').text(formatSize(data.HeapMemoryUsage.committed));
            $('#summary-hm').text(formatSize(data.HeapMemoryUsage.max));
            $('#summary-hf').text(data.ObjectPendingFinalizationCount+' object(s)');

            items['overview-mem-gr'][0].data.push([ts, data.HeapMemoryUsage.used]);
            refreshGraphs('overview-mem-gr', true, formatSize);

            items['memory-gr']['hm'][0].data.push([ts, data.HeapMemoryUsage.used]);
            items['memory-gr']['nm'][0].data.push([ts, data.NonHeapMemoryUsage.used]);
            items['memory-gr']['hm'][0].info = {
                'used':      data.HeapMemoryUsage.used,
                'committed': data.HeapMemoryUsage.committed,
                'max':       data.HeapMemoryUsage.max,
            };
            items['memory-gr']['nm'][0].info = {
                'used':      data.NonHeapMemoryUsage.used,
                'committed': data.NonHeapMemoryUsage.committed,
                'max':       data.NonHeapMemoryUsage.max,
            };
            refreshGraphs('memory-gr', true, formatSize);
        });
        endpointHost.fetchData('/java.lang:type=OperatingSystem?full=true', function(data) {
            $('#summary-pt').text(formatTime(data.ProcessCpuTime / 1000000, 3));
            $('#summary-mr').text(formatSize(data.TotalPhysicalMemorySize));
            $('#summary-ml').text(formatSize(data.FreePhysicalMemorySize));
            $('#summary-ms').text(formatSize(data.TotalSwapSpaceSize));
            $('#summary-mp').text(formatSize(data.FreeSwapSpaceSize));
            $('#summary-sn').text(data.Name+'/'+data.Version);
            $('#summary-sa').text(data.Arch);
            $('#summary-sp').text(data.AvailableProcessors);
            $('#summary-sm').text(formatSize(data.CommittedVirtualMemorySize));

            $('#overview-cpu-up').text(formatPercent(data.ProcessCpuLoad));
            $('#overview-cpu-us').text(formatPercent(data.SystemCpuLoad));

            items['overview-cpu-gr'][0].data.push([ts, data.ProcessCpuLoad]);
            refreshGraphs('overview-cpu-gr', true, formatPercent);
        });
        endpointHost.fetchData('/java.lang:type=Runtime?full=true', function(data) {
            $('#summary-ut').text(formatTime(data.Uptime));
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
            refreshGraphs('threads-gr');

            items['overview-thr-gr'][0].data.push([ts, data.ThreadCount]);
            refreshGraphs('overview-thr-gr', true);
        });

        setTimeout(gatherObjects, jmxproxyConf.cache_duration * 60 * 1000);
    };

    setTimeout(gatherObjects, 0);

    return {
        'refreshMemory': refreshMemory,
        'refreshGraphs': refreshGraphs,
        'buildBeanTree': buildBeanTree,
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
            'username': username,
            'password': password,
        }
        checkHost();
    };
    var fetchData = function(item, callback) {
        if (auth != null) {
            $.post("/jmxproxy/"+host+item, auth, callback, "json")
            .fail(function(jqXHR) {
                if (jqXHR.status == 401) {
                    $('#endpoint-auth').modal();
                } else if (jqXHR.status == 404) {
                    displayError('Selected endpoint is unavailable.');
                }
            });
        } else {
            $.getJSON("/jmxproxy/"+host+item, callback)
            .fail(function(jqXHR) {
                if (jqXHR.status == 401) {
                    $('#endpoint-auth').modal();
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
            $('#navbar-label').text(fetchName());
            $('#endpoint-select').toggleClass('hidden');
            $('#endpoint-navbar').toggleClass('hidden');
            $('a[data-toggle="tab"]:first').tab('show');
        });

        return endpointDataClass();
    };

    data = checkHost();

    return {
        'resetAuth': resetAuth,
        'fetchData': fetchData,
        'refreshGraphs': data.refreshGraphs,
        'refreshMemory': data.refreshMemory,
        'buildBeanTree': data.buildBeanTree,
    };
};

var jmxproxyConf;
var endpointHost;

$(document).ready(function() {
    $('#endpoint-input').keypress(function(e) {
        if (e.keyCode == 13 && this.validity.valid) {
            endpointHost = endpointHostClass($(this).val());

            $(this).blur();
        }
    });

    $('#endpoint-creds').submit(function() {
        endpointHost.resetAuth($('#endpoint-user').val(), $('#endpoint-pass').val());

        $('#endpoint-auth').modal({'show': false})
        return false;
    });

    $('#memory-btn-hm').click(function() {
        endpointHost.refreshMemory('hm');
    });
    $('#memory-btn-nm').click(function() {
        endpointHost.refreshMemory('nm');
    });
    $('#memory-gr').mouseout(function() {
        endpointHost.refreshGraphs($(this).attr('id'), true, formatSize);
    });
    $('#threads-gr').mouseout(function() {
        endpointHost.refreshGraphs($(this).attr('id'));
    });
    $('#classes-gr').mouseout(function() {
        endpointHost.refreshGraphs($(this).attr('id'));
    });
    $('a[data-toggle="tab"]').on('shown.bs.tab', function(e) {
        if (e.target.text == 'Overview') {
            endpointHost.refreshGraphs('overview-mem-gr', true, formatSize);
            endpointHost.refreshGraphs('overview-thr-gr', true);
            endpointHost.refreshGraphs('overview-cls-gr', true);
            endpointHost.refreshGraphs('overview-cpu-gr', true, formatPercent);
        } else if (e.target.text == 'Memory') {
            endpointHost.refreshMemory('hm');
        } else if (e.target.text == 'Classes' || e.target.text == 'Threads') {
            endpointHost.refreshGraphs(e.target.text.toLowerCase()+'-gr');
        } else if (e.target.text == 'MBeans') {
            endpointHost.buildBeanTree();
        }
    });

    $.extend($.fn.dataTableExt.oStdClasses, {
        'sWrapper': 'dataTables_wrapper form-inline',
        'sSortAsc': 'header headerSortDown',
        'sSortDesc': 'header headerSortUp',
        'sSortable': 'header',
    });

    $.getJSON('/jmxproxy/config', function(data) {
        jmxproxyConf = data;

        if (data.allowed_endpoints.length > 0) {
            $.each(data.allowed_endpoints, function(key, val) {
                $('#endpoint-list')
                .append(
                    $('<li/>')
                    .append(
                        $('<a/>')
                        .attr('href', '#')
                        .click(function() {
                            documentInfo.reset();
                            endpointHost = endpointHostClass($(this).text());
                        })
                        .text(val)
                    )
                );
            });

            $('#endpoint-choice').toggleClass('hidden');
        } else {
            $('#endpoint-insert').toggleClass('hidden');
            $('#endpoint-insert input').focus();
        }
    })
    .fail(function() {
        displayError('Malformed configuration data recieved from the server.')
    });
});

function formatTime(s, n) {
    v = s % 86400000;
    d = (s - v) / 86400000;
    s = v;

    v = s % 3600000;
    h = (s - v) / 3600000;
    s = v;

    v = s % 60000;
    m = (s - v) / 60000;
    s = (v / 1000).toFixed($.type(n) !== 'undefined' ? n : 0);

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

function formatSize(s) {
    if (s > (Math.pow(1024, 5))) {
        return (s / Math.pow(1024, 5)).toFixed(2) + 'PB';
    }
    if (s > (Math.pow(1024, 4))) {
        return (s / Math.pow(1024, 4)).toFixed(2) + 'TB';
    }
    if (s > (Math.pow(1024, 3))) {
        return (s / Math.pow(1024, 3)).toFixed(2) + 'GB';
    }
    if (s > (Math.pow(1024, 2))) {
        return (s / Math.pow(1024, 2)).toFixed(2) + 'MB';
    }
    if (s > (Math.pow(1024, 1))) {
        return (s / Math.pow(1024, 1)).toFixed(2) + 'KB';
    }

    return s + 'B';
}

function formatPercent(s) {
    return s.toFixed(2) + '%';
}

function displayError(text) {
    if (text != null) {
        $('#endpoint-error').text(text);
        $('#endpoint-alert').toggleClass('hidden');
    }
}
