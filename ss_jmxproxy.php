<?php

$no_http_headers = true;

/* display only fatal errors */
error_reporting(E_ERROR);
include_once(dirname(__FILE__) . "/../include/config.php");
include_once(dirname(__FILE__) . "/../lib/snmp.php");

if (!isset($called_by_script_server)) {
    error_reporting(E_ALL);
    $self = array_shift(explode('.', array_shift($_SERVER["argv"])));
    echo call_user_func_array($self, $_SERVER["argv"]), "\n";
}

function ss_jmxproxy($host, $jmxproxy = 'localhost:8080', $extra_stats = array()) {
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
    $beans = array();
    foreach (array_unique(array_map(function($name) { return array_shift(explode(':', $name[0])); }, $stats)) as $domain) {
        preg_match_all('/<a href=.+?>(.+?)<\/a>/', file_get_contents("http://{$jmxproxy}/jmxproxy/{$host}/{$domain}"), $found);
        $beans = array_merge($beans, $found[1]);
    }

    $data = array();
    foreach ($stats as $key => $val) {
        if (is_null($val) || sizeof($val) < 2) {
            continue;
        }
        foreach (array_filter($beans, function($name) use ($val) { return preg_match("/^{$val[0]}\$/", $name); }) as $mbean) {
            $request = sprintf(
                "http://%s/jmxproxy/%s/%s/%s", $jmxproxy,
                rawurlencode($host), rawurlencode($mbean), rawurlencode($val[1])
            );
            $attribute = json_decode(file_get_contents($request));
            if (is_object($attribute)) {
                if (sizeof($val) == 3) {
                    if (array_key_exists($key, $data)) {
                        $data[$key] += $attribute->$val[2];
                    } else {
                        $data[$key] = $attribute->$val[2];
                    }
                }
            } else {
                $data[$key] = $attribute;
            }
        }
    }

    return implode(' ', array_map(function($k,$v) { return $k . ':' . (is_numeric($v) ? (int)$v : 'U'); }, array_keys($data), array_values($data)));
}
