<?php

include_once("ss_jmxproxy.php");

function ss_hadoop($host, $type, $jmxproxy = 'localhost:8080') {
    $hadoop_stats = array($type => array());
    $hadoop_stats['namenode'] = array(
        'cluster_capacity'  => array('hadoop:service=NameNode,name=NameNodeInfo', 'Total'),
        'cluster_remaining' => array('hadoop:service=NameNode,name=NameNodeInfo', 'Free'),
        'cluster_used'      => array('hadoop:service=NameNode,name=NameNodeInfo', 'Used'),
        'ops_create_file'   => array('hadoop:service=NameNode,name=NameNodeActivity', 'CreateFileOps'),
        'ops_delete_file'   => array('hadoop:service=NameNode,name=NameNodeActivity', 'DeleteFileOps'),
        'ops_file_info'     => array('hadoop:service=NameNode,name=NameNodeActivity', 'FileInfoOps'),
        'ops_syncs'         => array('hadoop:service=NameNode,name=NameNodeActivity', 'SyncsNumOps'),
        'ops_transactions'  => array('hadoop:service=NameNode,name=NameNodeActivity', 'TransactionsNumOps'),
    );
    $hadoop_stats['datanode'] = array(
        'ds_capacity'               => array('hadoop:service=DataNode,name=FSDatasetState-.*', 'Capacity'),
        'ds_remaining'              => array('hadoop:service=DataNode,name=FSDatasetState-.*', 'Remaining'),
        'ds_used'                   => array('hadoop:service=DataNode,name=FSDatasetState-.*', 'DfsUsed'),
        'ds_failed'                 => array('hadoop:service=DataNode,name=FSDatasetState-.*', 'NumFailedVolumes'),
        'blocks_read'               => array('hadoop:service=DataNode,name=DataNodeActivity-.*', 'blocks_?read'),
        'blocks_removed'            => array('hadoop:service=DataNode,name=DataNodeActivity-.*', 'blocks_?removed'),
        'blocks_replicated'         => array('hadoop:service=DataNode,name=DataNodeActivity-.*', 'blocks_?replicated'),
        'blocks_verified'           => array('hadoop:service=DataNode,name=DataNodeActivity-.*', 'blocks_?verified'),
        'blocks_written'            => array('hadoop:service=DataNode,name=DataNodeActivity-.*', 'blocks_?written'),
        'bytes_read'                => array('hadoop:service=DataNode,name=DataNodeActivity-.*', 'bytes_?read'),
        'bytes_written'             => array('hadoop:service=DataNode,name=DataNodeActivity-.*', 'bytes_?written'),
        'reads_from_local_client'   => array('hadoop:service=DataNode,name=DataNodeActivity-.*', 'reads_?from_?local_?client'),
        'writes_from_local_client'  => array('hadoop:service=DataNode,name=DataNodeActivity-.*', 'writes_?from_?local_?client'),
        'reads_from_remote_client'  => array('hadoop:service=DataNode,name=DataNodeActivity-.*', 'reads_?from_?remote_?client'),
        'writes_from_remote_client' => array('hadoop:service=DataNode,name=DataNodeActivity-.*', 'writes_?from_?remote_?client'),
        'ops_block_copy'            => array('hadoop:service=DataNode,name=DataNodeActivity-.*', 'copyBlockOpNumOps'),
        'ops_block_read'            => array('hadoop:service=DataNode,name=DataNodeActivity-.*', 'readBlockOpNumOps'),
        'ops_block_write'           => array('hadoop:service=DataNode,name=DataNodeActivity-.*', 'writeBlockOpNumOps'),
        'ops_block_replace'         => array('hadoop:service=DataNode,name=DataNodeActivity-.*', 'replaceBlockOpNumOps'),
        'ops_block_checksum'        => array('hadoop:service=DataNode,name=DataNodeActivity-.*', 'blockChecksumOpNumOps'),
        'ops_block_reports'         => array('hadoop:service=DataNode,name=DataNodeActivity-.*', 'blockReportsNumOps'),
        'ops_heartbeat'             => array('hadoop:service=DataNode,name=DataNodeActivity-.*', 'heartBeatsNumOps'),
    );
    $hadoop_stats['jobtracker'] = array(
        'nodes_total'       => array('hadoop:service=JobTracker,name=JobTrackerInfo', 'SummaryJson', 'nodes'),
        'nodes_alive'       => array('hadoop:service=JobTracker,name=JobTrackerInfo', 'SummaryJson', 'alive'),
        'map_slots'         => array('hadoop:service=JobTracker,name=JobTrackerInfo', 'SummaryJson', 'slots', 'map_slots'),
        'map_slots_used'    => array('hadoop:service=JobTracker,name=JobTrackerInfo', 'SummaryJson', 'slots', 'map_slots_used'),
        'reduce_slots'      => array('hadoop:service=JobTracker,name=JobTrackerInfo', 'SummaryJson', 'slots', 'reduce_slots'),
        'reduce_slots_used' => array('hadoop:service=JobTracker,name=JobTrackerInfo', 'SummaryJson', 'slots', 'reduce_slots_used'),
        'jobs'              => array('hadoop:service=JobTracker,name=JobTrackerInfo', 'SummaryJson', 'jobs'),
    );
    $hadoop_stats['tasktracker'] = array(
        'tasks_running'  => array('hadoop:service=TaskTracker,name=TaskTrackerInfo', 'TasksInfoJson', 'running'),
        'tasks_failed'   => array('hadoop:service=TaskTracker,name=TaskTrackerInfo', 'TasksInfoJson', 'failed'),
        'commit_pending' => array('hadoop:service=TaskTracker,name=TaskTrackerInfo', 'TasksInfoJson', 'commit_pending'),
    );
    $hadoop_stats['master'] = array(
        'cluster_requests' => array('hadoop:service=Master,name=MasterStatistics', 'cluster_requests'),
    );
    $hadoop_stats['regionserver'] = array(
        'cache_capacity'   => array('hadoop:service=RegionServer,name=RegionServerStatistics', 'blockCacheSize'),
        'cache_remaining'  => array('hadoop:service=RegionServer,name=RegionServerStatistics', 'blockCacheFree'),
        'cache_used'       => array('hadoop:service=RegionServer,name=RegionServerStatistics', 'blockCacheCount'),
        'cache_hit_count'  => array('hadoop:service=RegionServer,name=RegionServerStatistics', 'blockCacheHitCount'),
        'cache_miss_count' => array('hadoop:service=RegionServer,name=RegionServerStatistics', 'blockCacheMissCount'),
        'regions'          => array('hadoop:service=RegionServer,name=RegionServerStatistics', 'regions'),
        'requests'         => array('hadoop:service=RegionServer,name=RegionServerStatistics', 'requests'),
        'stores'           => array('hadoop:service=RegionServer,name=RegionServerStatistics', 'stores'),
        'storefiles'       => array('hadoop:service=RegionServer,name=RegionServerStatistics', 'storefiles'),
        'compaction_queue' => array('hadoop:service=RegionServer,name=RegionServerStatistics', 'compactionQueueSize'),
        'flush_queue'      => array('hadoop:service=RegionServer,name=RegionServerStatistics', 'flushQueueSize'),
    );
    $hadoop_stats['zookeeper'] = array(
        'packets_received'     => array('org.apache.ZooKeeperService:name0=ReplicatedServer_id[0-9]+,name1=replica.[0-9]+,name2=(Leader|Follower)', 'PacketsReceived'),
        'packets_sent'         => array('org.apache.ZooKeeperService:name0=ReplicatedServer_id[0-9]+,name1=replica.[0-9]+,name2=(Leader|Follower)', 'PacketsSent'),
        'outstanding_requests' => array('org.apache.ZooKeeperService:name0=ReplicatedServer_id[0-9]+,name1=replica.[0-9]+,name2=(Leader|Follower)', 'OutstandingRequests'),
        'avg_request_latency' => array('org.apache.ZooKeeperService:name0=ReplicatedServer_id[0-9]+,name1=replica.[0-9]+,name2=(Leader|Follower)', 'AvgRequestLatency'),
    );

    return ss_jmxproxy($host, $jmxproxy, $hadoop_stats[$type]);
}
