#!/usr/bin/env python2.7

import argparse
import json
import re
import socket
import time
import urllib2

JVM_STATS = {
    'jvm.thread_count':     ('java.lang:type=Threading', 'ThreadCount'),
    'jvm.thread_peak':      ('java.lang:type=Threading', 'PeakThreadCount'),
    'jvm.memory_heap_used': ('java.lang:type=Memory', 'HeapMemoryUsage', 'used'),
    'jvm.memory_heap_max':  ('java.lang:type=Memory', 'HeapMemoryUsage', 'max'),
    'jvm.gc_count':         ('java.lang:type=GarbageCollector,name=.*', 'CollectionCount'),
    'jvm.classes_loaded':   ('java.lang:type=ClassLoading', 'LoadedClassCount'),
    'jvm.classes_total':    ('java.lang:type=ClassLoading', 'TotalLoadedClassCount'),
    'jvm.classes_unloaded': ('java.lang:type=ClassLoading', 'UnloadedClassCount'),
}
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


def fetch_jmx(data, service_host, service_port, jmxproxy_host, jmxproxy_port):
    attrs = {}
    beans = json.loads(urllib2.urlopen('http://%s:%d/%s:%d' % (jmxproxy_host, jmxproxy_port, service_host, service_port)).read())

    for key, val in data.items():
        for mbean in beans:
            if re.match('%s$' % val[0], mbean):
                attr = beans[mbean]
                for i in xrange(1, len(val)):
                    attr = attr[val[i]]

                if key not in attrs:
                    attrs[key] = 0

                attrs[key] += attr

    return attrs

def send_data(data, path, graphite_host, graphite_port, send=True):
    t = int(time.time())
    m = '\n'.join("%s.%s %s %d" %  (path, k, v, t) for k, v in sorted(data.items()))

    if send:
        s = socket.socket()
        s.connect((graphite_host, graphite_port))
        s.send(m)
    else:
        print m


if __name__ == '__main__':
    parser = argparse.ArgumentParser(description='jvm/hadoop jmx poller for graphite')
    parser.add_argument('--service-host', default=socket.gethostname(),
                        help='hadoop service hostname')
    parser.add_argument('--service-port', type=int, required=True,
                        help='hadoop service jmx port')
    parser.add_argument('--service-name', choices=sorted(HADOOP_STATS.keys()),
                        help='hadoop service name')
    parser.add_argument('--graphite-key',
                        help='graphite key prefix')
    parser.add_argument('--graphite-host', default='localhost',
                        help='graphite host')
    parser.add_argument('--graphite-port', type=int, default=2003,
                        help='graphite port')
    parser.add_argument('--jmxproxy-host', default='localhost',
                        help='jmxproxy host')
    parser.add_argument('--jmxproxy-port', type=int, default=8080,
                        help='jmxproxy port')
    parser.add_argument('-n', '--dry-run', default=False, action='store_true',
                        help='print results instead of sending to graphite')
    args = parser.parse_args()

    JVM_STATS.update(HADOOP_STATS.get(args.service_name, {}))
    data = fetch_jmx(JVM_STATS, args.service_host, args.service_port, args.jmxproxy_host, args.jmxproxy_port)
    path = '.'.join(i for i in (args.graphite_key, args.service_host, args.service_name) if i)

    send_data(data, path, args.graphite_host, args.graphite_port, send=not args.dry_run)
