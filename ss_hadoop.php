<?php

include_once("ss_jmxproxy.php");

function ss_hadoop($host, $jmxproxy = 'localhost:8080') {
    $hadoop_stats = array(
        'ds_capacity' => array('hadoop:service=DataNode,name=FSDatasetState-DS.*', 'Capacity'),
        'ds_used' => array('hadoop:service=DataNode,name=FSDatasetState-DS.*', 'DfsUsed'),
        'ds_failed' => array('hadoop:service=DataNode,name=FSDatasetState-DS.*', 'NumFailedVolumes'),
    );

    return ss_jmxproxy($host, $jmxproxy, $hadoop_stats);
}
