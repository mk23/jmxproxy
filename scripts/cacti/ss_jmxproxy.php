<?php

$no_http_headers = true;

/* display only fatal errors */
error_reporting(E_ERROR);

if (!isset($called_by_script_server)) {
    error_reporting(E_ALL);
    $self = array_shift(explode('.', array_shift($_SERVER["argv"])));
    echo call_user_func_array($self, $_SERVER["argv"]), "\n";
}

function ss_jmxproxy($host, $jmxproxy = 'http://localhost:8080/jmxproxy', $jmxcreds = '', $extra_stats = array()) {
    $jvm_stats = array(
        'thread_count' => array('java.lang:type=Threading', 'ThreadCount'),
        'thread_peak' => array('java.lang:type=Threading', 'PeakThreadCount'),
        'memory_heap_used' => array('java.lang:type=Memory', 'HeapMemoryUsage', 'used'),
        'memory_heap_max' => array('java.lang:type=Memory', 'HeapMemoryUsage', 'max'),
        'gc_count' => array('java.lang:type=GarbageCollector,name=.*', 'CollectionCount'),
        'classes_loaded' => array('java.lang:type=ClassLoading', 'LoadedClassCount'),
        'classes_total' => array('java.lang:type=ClassLoading', 'TotalLoadedClassCount'),
        'classes_unloaded' => array('java.lang:type=ClassLoading', 'UnloadedClassCount'),
    );

    $stats = array_merge($jvm_stats, $extra_stats);
    if (!empty($jmxcreds)) {
        $cntxt = stream_context_create(array(
            'http' => array(
                'method'  => 'POST',
                'header'  => 'Content-type: application/x-www-form-urlencoded',
                'content' => http_build_query(array(
                    'username' => urldecode(array_shift(explode(':', $jmxcreds))),
                    'password' => urldecode(array_pop(explode(':', $jmxcreds))),
                )),
            ),
        ));
    } else {
        $cntxt = stream_context_create();
    }

    stream_context_set_option($cntxt, 'http', 'timeout', 10.0);
    $beans = json_decode(file_get_contents("{$jmxproxy}/{$host}", FILE_USE_INCLUDE_PATH, $cntxt), true);
    $cache = array();

    $data = array();
    foreach ($stats as $key => $val) {
        if (is_null($val) || sizeof($val) < 2) {
            continue;
        }
        foreach (preg_grep("/^{$val[0]}\$/i", $beans) as $mbean) {
            if (!isset($cache[$mbean])) {
                $cache[$mbean] = json_decode(file_get_contents(sprintf("{$jmxproxy}/{$host}/%s?full=true", rawurlencode($mbean))), true);
            }
            $attribute = $cache[$mbean];

            foreach (array_slice($val, 1) as $part) {
                $attribute = $attribute[array_shift(preg_grep("/^{$part}\$/i", array_keys($attribute)))];
            }
            if (array_key_exists($key, $data)) {
                $data[$key] += $attribute;
            } else {
                $data[$key] = $attribute;
            }
        }
    }

    return implode(' ', array_map(function($k,$v) { return $k . ':' . (is_numeric($v) ? (int)$v : 'U'); }, array_keys($data), array_values($data)));
}
