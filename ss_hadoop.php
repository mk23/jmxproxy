<?php

include_once("ss_jmxproxy.php");

function ss_hadoop($host, $type, $jmxproxy = 'localhost:8080') {
    $hadoop_stats = array($type => array());
    $hadoop_stats['namenode'] = array(
        'cluster_capacity' => array('hadoop:service=NameNode,name=NameNodeInfo', 'Total'),
        'cluster_remaining' => array('hadoop:service=NameNode,name=NameNodeInfo', 'Free'),
        'cluster_used' => array('hadoop:service=NameNode,name=NameNodeInfo', 'Used'),
        'ops_create_file' => array('hadoop:service=NameNode,name=NameNodeActivity', 'CreateFileOps'),
        'ops_delete_file' => array('hadoop:service=NameNode,name=NameNodeActivity', 'DeleteFileOps'),
        'ops_file_info' => array('hadoop:service=NameNode,name=NameNodeActivity', 'FileInfoOps'),
        'ops_syncs' => array('hadoop:service=NameNode,name=NameNodeActivity', 'SyncsNumOps'),
        'ops_transactions' => array('hadoop:service=NameNode,name=NameNodeActivity', 'TransactionsNumOps'),
    );
    $hadoop_stats['datanode'] = array(
        'ds_capacity' => array('hadoop:service=DataNode,name=FSDatasetState-DS.*', 'Capacity'),
        'ds_remaining' => array('hadoop:service=DataNode,name=FSDatasetState-DS.*', 'Remaining'),
        'ds_used' => array('hadoop:service=DataNode,name=FSDatasetState-DS.*', 'DfsUsed'),
        'ds_failed' => array('hadoop:service=DataNode,name=FSDatasetState-DS.*', 'NumFailedVolumes'),
        'blocks_read' => array('hadoop:service=DataNode,name=DataNodeActivity-.*', 'blocks_read'),
        'blocks_removed' => array('hadoop:service=DataNode,name=DataNodeActivity-.*', 'blocks_removed'),
        'blocks_replicated' => array('hadoop:service=DataNode,name=DataNodeActivity-.*', 'blocks_replicated'),
        'blocks_verified' => array('hadoop:service=DataNode,name=DataNodeActivity-.*', 'blocks_verified'),
        'blocks_written' => array('hadoop:service=DataNode,name=DataNodeActivity-.*', 'blocks_written'),
        'bytes_read' => array('hadoop:service=DataNode,name=DataNodeActivity-.*', 'bytes_read'),
        'bytes_written' => array('hadoop:service=DataNode,name=DataNodeActivity-.*', 'bytes_written'),
        'reads_from_local_client' => array('hadoop:service=DataNode,name=DataNodeActivity-.*', 'reads_from_local_client'),
        'writes_from_local_client' => array('hadoop:service=DataNode,name=DataNodeActivity-.*', 'writes_from_local_client'),
        'reads_from_remote_client' => array('hadoop:service=DataNode,name=DataNodeActivity-.*', 'reads_from_remote_client'),
        'writes_from_remote_client' => array('hadoop:service=DataNode,name=DataNodeActivity-.*', 'writes_from_remote_client'),
    );
    $hadoop_stats['master'] = array(
        'cluster_requests' => array('hadoop:service=Master,name=MasterStatistics', 'cluster_requests'),
    );
    $hadoop_stats['regionserver'] = array(
        'cache_capacity' => array('hadoop:service=RegionServer,name=RegionServerStatistics', 'blockCacheSize'),
        'cache_remianing' => array('hadoop:service=RegionServer,name=RegionServerStatistics', 'blockCacheFree'),
        'cache_used' => array('hadoop:service=RegionServer,name=RegionServerStatistics', 'blockCacheCount'),
        'cache_hit_count' => array('hadoop:service=RegionServer,name=RegionServerStatistics', 'blockCacheHitCount'),
        'cache_miss_count' => array('hadoop:service=RegionServer,name=RegionServerStatistics', 'blockCacheMissCount'),
        'regions' => array('hadoop:service=RegionServer,name=RegionServerStatistics', 'regions'),
        'requests' => array('hadoop:service=RegionServer,name=RegionServerStatistics', 'requests'),
        'stores' => array('hadoop:service=RegionServer,name=RegionServerStatistics', 'stores'),
        'storefiles' => array('hadoop:service=RegionServer,name=RegionServerStatistics', 'storefiles'),
    );

    return ss_jmxproxy($host, $jmxproxy, $hadoop_stats[$type]);
}
