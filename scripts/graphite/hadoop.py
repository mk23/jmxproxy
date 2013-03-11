#!/usr/bin/env python2.7

import argparse
import jmxproxy

HADOOP_STATS = {
    'namenode': {
        'cluster_capacity':           ('hadoop:service=NameNode,name=NameNodeInfo', 'Total'),
        'cluster_remaining':          ('hadoop:service=NameNode,name=NameNodeInfo', 'Free'),
        'cluster_used':               ('hadoop:service=NameNode,name=NameNodeInfo', 'Used'),
        'ops_create_file':            ('hadoop:service=NameNode,name=NameNodeActivity', 'CreateFileOps'),
        'ops_delete_file':            ('hadoop:service=NameNode,name=NameNodeActivity', 'DeleteFileOps'),
        'ops_file_info':              ('hadoop:service=NameNode,name=NameNodeActivity', 'FileInfoOps'),
        'ops_syncs':                  ('hadoop:service=NameNode,name=NameNodeActivity', 'SyncsNumOps'),
        'ops_transactions':           ('hadoop:service=NameNode,name=NameNodeActivity', 'TransactionsNumOps')
    },
    'datanode': {
        'ds_capacity':                ('hadoop:service=DataNode,name=FSDatasetState-DS.*', 'Capacity'),
        'ds_remaining':               ('hadoop:service=DataNode,name=FSDatasetState-DS.*', 'Remaining'),
        'ds_used':                    ('hadoop:service=DataNode,name=FSDatasetState-DS.*', 'DfsUsed'),
        'ds_failed':                  ('hadoop:service=DataNode,name=FSDatasetState-DS.*', 'NumFailedVolumes'),
        'blocks_read':                ('hadoop:service=DataNode,name=DataNodeActivity-.*', 'blocks_read'),
        'blocks_removed':             ('hadoop:service=DataNode,name=DataNodeActivity-.*', 'blocks_removed'),
        'blocks_replicated':          ('hadoop:service=DataNode,name=DataNodeActivity-.*', 'blocks_replicated'),
        'blocks_verified':            ('hadoop:service=DataNode,name=DataNodeActivity-.*', 'blocks_verified'),
        'blocks_written':             ('hadoop:service=DataNode,name=DataNodeActivity-.*', 'blocks_written'),
        'bytes_read':                 ('hadoop:service=DataNode,name=DataNodeActivity-.*', 'bytes_read'),
        'bytes_written':              ('hadoop:service=DataNode,name=DataNodeActivity-.*', 'bytes_written'),
        'reads_from_local_client':    ('hadoop:service=DataNode,name=DataNodeActivity-.*', 'reads_from_local_client'),
        'writes_from_local_client':   ('hadoop:service=DataNode,name=DataNodeActivity-.*', 'writes_from_local_client'),
        'reads_from_remote_client':   ('hadoop:service=DataNode,name=DataNodeActivity-.*', 'reads_from_remote_client'),
        'writes_from_remote_client':  ('hadoop:service=DataNode,name=DataNodeActivity-.*', 'writes_from_remote_client'),
        'ops_block_copy':             ('hadoop:service=DataNode,name=DataNodeActivity-.*', 'copyBlockOpNumOps'),
        'ops_block_read':             ('hadoop:service=DataNode,name=DataNodeActivity-.*', 'readBlockOpNumOps'),
        'ops_block_write':            ('hadoop:service=DataNode,name=DataNodeActivity-.*', 'writeBlockOpNumOps'),
        'ops_block_replace':          ('hadoop:service=DataNode,name=DataNodeActivity-.*', 'replaceBlockOpNumOps'),
        'ops_block_checksum':         ('hadoop:service=DataNode,name=DataNodeActivity-.*', 'blockChecksumOpNumOps'),
        'ops_block_reports':          ('hadoop:service=DataNode,name=DataNodeActivity-.*', 'blockReportsNumOps'),
        'ops_heartbeat':              ('hadoop:service=DataNode,name=DataNodeActivity-.*', 'heartBeatsNumOps'),
    },
    'jobtracker': {
        'nodes_total':                ('hadoop:service=JobTracker,name=JobTrackerInfo', 'SummaryJson', 'nodes'),
        'nodes_alive':                ('hadoop:service=JobTracker,name=JobTrackerInfo', 'SummaryJson', 'alive'),
        'map_slots':                  ('hadoop:service=JobTracker,name=JobTrackerInfo', 'SummaryJson', 'slots', 'map_slots'),
        'map_slots_used':             ('hadoop:service=JobTracker,name=JobTrackerInfo', 'SummaryJson', 'slots', 'map_slots_used'),
        'reduce_slots':               ('hadoop:service=JobTracker,name=JobTrackerInfo', 'SummaryJson', 'slots', 'reduce_slots'),
        'reduce_slots_used':          ('hadoop:service=JobTracker,name=JobTrackerInfo', 'SummaryJson', 'slots', 'reduce_slots_used'),
        'jobs':                       ('hadoop:service=JobTracker,name=JobTrackerInfo', 'SummaryJson', 'jobs'),
    },
    'tasktracker': {
        'tasks_running':              ('hadoop:service=TaskTracker,name=TaskTrackerInfo', 'TasksInfoJson', 'running'),
        'tasks_failed':               ('hadoop:service=TaskTracker,name=TaskTrackerInfo', 'TasksInfoJson', 'failed'),
        'commit_pending':             ('hadoop:service=TaskTracker,name=TaskTrackerInfo', 'TasksInfoJson', 'commit_pending'),
    },
    'masterserver': {
        'cluster_requests':           ('hadoop:service=Master,name=MasterStatistics', 'cluster_requests'),
    },
    'regionserver': {
        'cache_capacity':             ('hadoop:service=RegionServer,name=RegionServerStatistics', 'blockCacheSize'),
        'cache_remianing':            ('hadoop:service=RegionServer,name=RegionServerStatistics', 'blockCacheFree'),
        'cache_used':                 ('hadoop:service=RegionServer,name=RegionServerStatistics', 'blockCacheCount'),
        'cache_hit_count':            ('hadoop:service=RegionServer,name=RegionServerStatistics', 'blockCacheHitCount'),
        'cache_miss_count':           ('hadoop:service=RegionServer,name=RegionServerStatistics', 'blockCacheMissCount'),
        'regions':                    ('hadoop:service=RegionServer,name=RegionServerStatistics', 'regions'),
        'requests':                   ('hadoop:service=RegionServer,name=RegionServerStatistics', 'requests'),
        'stores':                     ('hadoop:service=RegionServer,name=RegionServerStatistics', 'stores'),
        'storefiles':                 ('hadoop:service=RegionServer,name=RegionServerStatistics', 'storefiles'),
    },
    'zookeeper': {
        'packets_received':           ('org.apache.ZooKeeperService:name0=ReplicatedServer_id[0-9]+,name1=replica.[0-9]+,name2=Follower', 'PacketsReceived'),
        'packets_sent':               ('org.apache.ZooKeeperService:name0=ReplicatedServer_id[0-9]+,name1=replica.[0-9]+,name2=Follower', 'PacketsSent'),
        'outstanding_requests':       ('org.apache.ZooKeeperService:name0=ReplicatedServer_id[0-9]+,name1=replica.[0-9]+,name2=Follower', 'OutstandingRequests'),
        'avg_request_latency':        ('org.apache.ZooKeeperService:name0=ReplicatedServer_id[0-9]+,name1=replica.[0-9]+,name2=Follower', 'AvgRequestLatency'),
    },
}


def main():
    parser = argparse.ArgumentParser(parents=[jmxproxy.parser], add_help=False, description='jmxproxy hadoop poller for graphite')
    parser.add_argument('--service-name', required=True, choices=sorted(HADOOP_STATS.keys()),
                        help='hadoop service name')
    args = parser.parse_args()

    jmxproxy.main(args, args.service_name, HADOOP_STATS[args.service_name])


if __name__ == '__main__':
    main()
