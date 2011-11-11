<?php

$no_http_headers = true;

/* display no errors */
error_reporting(E_ERROR);
include_once(dirname(__FILE__) . "/../include/config.php");
include_once(dirname(__FILE__) . "/../lib/snmp.php");

if (!isset($called_by_script_server)) {
    error_reporting(E_ALL);
    array_shift($_SERVER["argv"]);
    echo call_user_func_array("ss_jmxproxy", $_SERVER["argv"]), "\n";
}

function ss_jmxproxy($host, $jmxproxy = 'localhost:8080') {
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

    preg_match_all('/<a href=.+?>(.+?)<\/a>/', file_get_contents("http://{$jmxproxy}/jmxproxy/{$host}/java.lang"), $mbeans);
    $mbeans = array_pop($mbeans);

    $data = array();
    foreach ($jvm_stats as $key => $val) {
        $attribute = json_decode(file_get_contents("http://{$jmxproxy}/jmxproxy/{$host}/{$val[0]}/{$val[1]}"));
        if (is_object($attribute)) {
            if (sizeof($val) == 3) {
                $data[$key] = $attribute->$val[2];
            }
        } else {
            $data[$key] = $attribute;
        }
    }

    return implode(' ', array_map(function($k,$v) { return $k . ':' . (is_null($v) ? 'U' : $v); }, array_keys($data), array_values($data)));
}
