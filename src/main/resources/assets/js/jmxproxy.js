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
        if (_.has(items, name) && $('#'+name).is(':visible')) {
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
                    tickFormatter: _.isUndefined(type) ? Math.floor : type,
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
        endpointHost.fetchData('/', function(data) {
            tree = [];
            data.sort();

            var dataSource = function(nodeData, callback) {
                callback({
                    data: _.has(nodeData, 'tree') ? nodeData.tree : tree,
                });
            };

            var addHeader = function(tree, name) {
                find = _.find(tree, function(o, i) {
                    return o.text == name;
                });

                if (find) {
                    return find;
                } else {
                    item = {
                        text: name,
                        type: 'folder',
                        tree: [],
                    };
                    tree.push(item);
                    return item;
                }
            };

            for (bean in data) {
                head = data[bean].split(':')[0].replace(/"/g, '');

                node = addHeader(tree, head);

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
                            type: 'item',
                            attr: {
                                'title': data[bean],
                                'data-toggle': 'tooltip',
                                'data-placement': 'right',
                            },
                        };

                        node.tree.push(item);
                    } else {
                        node = addHeader(node.tree, name);
                    }
                }
            }

            $('#mbeans-tree')
            .tree({
                dataSource: dataSource,
                folderSelect: false,
            })
            .on('opened.fu.tree', function(e, node) {
                $('.tree-item').tooltip({container: 'body'});
            })
            .on('selected.fu.tree', function(e, node) {
                buildBeanData(node.target.attr.title);
            });
        });
    };

    var buildBeanData = function(bean) {
        endpointHost.fetchData('/'+bean+'?full=true', function(data) {
            var columns = [{
                label: 'Name',
                property: 'key',
                sortable: true,
            }, {
                label: 'Value',
                property: 'val',
                sortable: true,

            }];

            var typeMap = {
                'array': function(k, v) {
                    return [1, k];
                },
                'object': function(k, v) {
                    return [2, k];
                },
                'boolean': function(k, v) {
                    return [3 + (v ^ 1), k];
                },
                'null': function(k, v) {
                    return [5, k];
                },
                'number': function(k, v) {
                    return [6, v];
                },
                'string': function(k, v) {
                    return [7, v];
                },
            };

            var objects = _.map(data, function(val, key) {
                return {
                    key: key,
                    val: val,
                    srt: typeMap[$.type(val)](key, val),
                };
            });

            var mainDataSource = function(options, callback) {
                list = _.extend([], objects);

                if (options.search) {
                    list = _.filter(list, function(item) {
                        s = options.search.toLowerCase();

                        a = item.key.toLowerCase();
                        b = item.srt.join(',').toLowerCase();
                        c = (item.val + '').toLowerCase();
                        d = $.type(item.val).toLowerCase();

                        return (
                            a.indexOf(s) >= 0 ||
                            b.indexOf(s) >= 0 ||
                            c.indexOf(s) >= 0 ||
                            d.indexOf(s) >= 0
                        );
                    });
                }

                if (options.sortProperty) {
                    list.sort(function(a, b) {
                        if (options.sortProperty == 'key') {
                            return a.key.localeCompare(b.key);
                        } else if (a.srt[0] < b.srt[0]) {
                            return -1;
                        } else if (a.srt[0] > b.srt[0]) {
                            return 1;
                        } else if (a.srt[0] == typeMap.number()[0]) {
                            return a.srt[1] - b.srt[1];
                        } else {
                            return a.srt[1].localeCompare(b.srt[1]);
                        }
                    });

                    if (options.sortDirection === 'desc') {
                        list.reverse();
                    }
                }

                rval = {
                    columns: columns,
                    count:   list.length,
                    pages:   Math.ceil(list.length / options.pageSize),
                    page:    options.pageIndex,
                };

                rval.start = options.pageIndex * options.pageSize + 1;
                rval.end   = _.min([rval.start + options.pageSize - 1, rval.count]);
                rval.items = list.slice(rval.start - 1, rval.end);
                callback(rval);
            };

            var mainColBuilder = function(helpers, callback) {
                if (helpers.columnAttr == 'val') {
                    key = helpers.rowData.key;
                    val = helpers.rowData.val;

                    if (_.isArray(val) || _.isObject(val)) {
                        val = $('<div/>')
                        .append(
                            $('<button/>')
                            .attr('title', 'Expand')
                            .data('attrib-name', key)
                            .data('attrib-data', val)
                            .data('toggle', 'tooltip')
                            .data('placement', 'bottom')
                            .tooltip({
                                container: 'body',
                            })
                            .click(function() {
                                $('#attrib-name').text($(this).data('attrib-name'));
                                if ($('#attrib-data').data('fu.repeater')) {
                                    $('#attrib-data')
                                    .repeater('clear')
                                    .removeData('fu.repeater');
                                }
                                $('#attrib-data').repeater({
                                    dataSource: attrDataSource,
                                    dataTarget: $(this).data('attrib-data'),
                                    list_columnRendered: attrColBuilder,
                                });
                                $('#attrib-modal').modal();
                            })
                            .addClass('btn btn-xs btn-info')
                            .text($.type(val))
                        )
                        .append(
                            $('<span/>')
                            .addClass('badge progress-bar-danger pull-right')
                            .text(_.size(val))
                        );
                    } else if (_.isNull(val)) {
                        val = $('<div/>')
                        .addClass('text-danger')
                        .text('null');
                    } else if (_.isNaN(val)) {
                        val = $('<div/>')
                        .addClass('text-danger')
                        .text('NaN');
                    } else if (_.isBoolean(val)) {
                        val = $('<div/>')
                        .addClass('text-warning')
                        .text(val);
                    } else if (_.isNumber(val)) {
                        val = $('<div/>')
                        .addClass('text-success')
                        .text(val);
                    } else if (_.isString(val) && val.length > 50) {
                        val = $('<div/>')
                        .append(
                            $('<a/>')
                            .attr('href', '#')
                            .data('content', val)
                            .addClass('text-primary')
                            .text(val.substring(0,50))
                            .append(
                                $('<span/>')
                                .addClass('text-primary')
                                .html('&nbsp;&raquo;&nbsp;')
                            )
                            .click(function() {
                                tmp = $(this).html();
                                $(this)
                                    .html($(this).data('content'))
                                    .data('content', tmp)
                                    .next()
                                        .toggleClass('hidden');
                            })
                        )
                        .append(
                            $('<span/>')
                            .addClass('badge progress-bar-danger pull-right')
                            .text(val.length)
                        );
                    } else {
                        val = $('<div/>')
                        .addClass('text-primary')
                        .text(val);
                    }

                    helpers.item.html(val);
                }

                callback();
            };

            var attrColBuilder = function(helpers, callback) {
                val = helpers.rowData[helpers.columnAttr];

                if (_.isNull(val)) {
                    val = $('<div/>')
                    .addClass('text-danger')
                    .text('null');
                } else if (_.isNaN(val)) {
                    val = $('<div/>')
                    .addClass('text-danger')
                    .text('NaN');
                } else if (_.isBoolean(val)) {
                    val = $('<div/>')
                    .addClass('text-warning')
                    .text(val);
                } else if (_.isNumber(val)) {
                    val = $('<div/>')
                    .addClass('text-success')
                    .text(val);
                } else if (_.isString(val) && val.length > 50) {
                    val = $('<div/>')
                    .append(
                        $('<a/>')
                        .attr('href', '#')
                        .data('content', val)
                        .addClass('text-primary')
                        .text(val.substring(0,50))
                        .append(
                            $('<span/>')
                            .addClass('text-primary')
                            .html('&nbsp;&raquo;&nbsp;')
                        )
                        .click(function() {
                            tmp = $(this).html();
                            $(this)
                                .html($(this).data('content'))
                                .data('content', tmp)
                                .next()
                                    .toggleClass('hidden');
                        })
                    )
                    .append(
                        $('<span/>')
                        .addClass('badge progress-bar-danger pull-right')
                        .text(val.length)
                    );
                } else {
                    val = $('<div/>')
                    .addClass('text-primary')
                    .text(val);
                }

                helpers.item.html(val);

                callback();
            };

            var attrDataSource = function(options, callback) {
                list = [];
                cols = [];

                data = this.dataTarget;
                if (_.isArray(data)) {
                    if (data.length && _.isObject(data[0])) {
                        cols = _.map(data[0], function(v, k) {
                            return {
                                label: k.charAt(0).toUpperCase() + k.slice(1),
                                property: k,
                                sortable: true,
                            };
                        });
                        list = _.extend([], data);
                    } else if (data.length) {
                        cols = [{
                            label: 'Value',
                            property: 'val',
                            sortable: true,
                        }];
                        list = _.map(data, function(v) {
                            return {
                                val: v,
                            };
                        })
                    }
                } else if (_.isObject(data)) {
                    var cols = [{
                        label: 'Name',
                        property: 'key',
                        sortable: true,
                    }, {
                        label: 'Value',
                        property: 'val',
                        sortable: true,
                    }];
                    list = _.map(data, function(v, k) {
                        return {
                            key: k,
                            val: v,
                        };
                    });
                }

                if (options.search) {
                    list = _.filter(list, function(item) {
                        s = options.search.toLowerCase();
                        for (key in item) {
                            a = key.toLowerCase();
                            b = (item[key] + '').toLowerCase();
                            c = $.type(item[key]).toLowerCase();

                            if (a.indexOf(s) >= 0 || b.indexOf(s) >= 0 || c.indexOf(s) >= 0) {
                                return true;
                            }
                        }

                        return false;
                    });
                }

                if (options.sortProperty) {
                    list = _.sortBy(list, options.sortProperty);
                    if (options.sortDirection === 'desc') {
                        list.reverse();
                    }
                }

                rval = {
                    columns: cols,
                    count:   list.length,
                    pages:   Math.ceil(list.length / options.pageSize),
                    page:    options.pageIndex,
                };

                rval.start = options.pageIndex * options.pageSize + 1;
                rval.end   = _.min([rval.start + options.pageSize - 1, rval.count]);
                rval.items = list.slice(rval.start - 1, rval.end);
                callback(rval);
            };

            if ($('#mbeans-data').data('fu.repeater')) {
                $('#mbeans-data')
                .repeater('clear')
                .removeData('fu.repeater');
            }
            $('#mbeans-data').repeater({
                dataSource: mainDataSource,
                list_columnRendered: mainColBuilder,
            });
            $('#mbean-title').text(bean);
            $('#mbean-reset').click(function() {
                buildBeanData(bean);
            });
        });
    };

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
                        if (_.has(items['memory-gr'], item.Name)) {
                            items['memory-gr'][item.Name][0].data.push([ts, item.Usage.used]);
                            items['memory-gr'][item.Name][0].info = {
                                used:      item.Usage.used,
                                committed: item.Usage.committed,
                                max:       item.Usage.max,
                            };
                        } else {
                            items['memory-gr'][item.Name] = [{
                                label: item.Name+' Memory Usage',
                                data: [ [ts, item.Usage.used] ],
                                info: {
                                    used:      item.Usage.used,
                                    committed: item.Usage.committed,
                                    max:       item.Usage.max,
                                },
                            }];
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
                used:      data.HeapMemoryUsage.used,
                committed: data.HeapMemoryUsage.committed,
                max:       data.HeapMemoryUsage.max,
            };
            items['memory-gr']['nm'][0].info = {
                used:      data.NonHeapMemoryUsage.used,
                committed: data.NonHeapMemoryUsage.committed,
                max:       data.NonHeapMemoryUsage.max,
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
        refreshMemory: refreshMemory,
        refreshGraphs: refreshGraphs,
        buildBeanTree: buildBeanTree,
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
            username: username,
            password: password,
        }
        checkHost();
    };
    var fetchData = function(item, callback) {
        if (auth != null) {
            $.post('/jmxproxy/'+host+item, auth, callback, 'json')
            .fail(function(jqXHR) {
                if (jqXHR.status == 401) {
                    $('#endpoint-auth').modal();
                } else if (jqXHR.status == 404) {
                    displayError('Selected endpoint is unavailable.');
                }
            });
        } else {
            $.getJSON('/jmxproxy/'+host+item, callback)
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
            $('#welcome-banner').toggleClass('hidden');
            $('#endpoint-select').toggleClass('hidden');
            $('#endpoint-navbar').toggleClass('hidden');
            $('a[data-toggle="tab"]:first').tab('show');
        });

        return endpointDataClass();
    };

    data = checkHost();

    return {
        resetAuth: resetAuth,
        fetchData: fetchData,
        refreshGraphs: data.refreshGraphs,
        refreshMemory: data.refreshMemory,
        buildBeanTree: data.buildBeanTree,
    };
};

var jmxproxyConf;
var endpointHost;

$(document).ready(function() {
    $('#endpoint-input')
    .keypress(function(e) {
        if (e.keyCode == 13 && this.validity.valid) {
            endpointHost = endpointHostClass($(this).val());
        }
    })
    .keyup(function(e) {
        $(this).parent()
        .toggleClass('has-error', !this.validity.valid)
        .toggleClass('has-success', this.validity.valid);
    });

    $('#endpoint-creds').submit(function() {
        endpointHost.resetAuth($('#endpoint-user').val(), $('#endpoint-pass').val());

        $('#endpoint-auth').modal({
            show: false
        });
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

    $('form').submit(function(e) {
        return false;
    });

    $('[data-toggle="tooltip"]').tooltip();

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
                            endpointHost = endpointHostClass($(this).text());
                        })
                        .text(val)
                    )
                );
            });

            $('#welcome-banner h3').text($('#welcome-banner h3').text().replace('enter', 'select'));
            $('#endpoint-choice').toggleClass('hidden');
        } else {
            $('#welcome-banner h3').text($('#welcome-banner h3').text().replace('select', 'enter'));
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
