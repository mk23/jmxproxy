#!/opt/python/bin/python

# TODO: lots more error checking and logging

import re, json, time, socket, urllib2
from xml.etree.cElementTree import fromstring as parseXML

JVM_STATS = {
    'java.thread_count':     ('java.lang:type=Threading', 'ThreadCount'),
    'java.thread_peak':      ('java.lang:type=Threading', 'PeakThreadCount'),
    'java.memory_heap_used': ('java.lang:type=Memory', 'HeapMemoryUsage', 'used'),
    'java.memory_heap_max':  ('java.lang:type=Memory', 'HeapMemoryUsage', 'max'),
    'java.gc_count':         ('java.lang:type=GarbageCollector,name=.*', 'CollectionCount'),
    'java.classes_loaded':   ('java.lang:type=ClassLoading', 'LoadedClassCount'),
    'java.classes_total':    ('java.lang:type=ClassLoading', 'TotalLoadedClassCount'),
    'java.classes_unloaded': ('java.lang:type=ClassLoading', 'UnloadedClassCount'),
}
HADOOP_STATS = {
    'namenode': {
        'hdfs.namenode.cluster_capacity':           ('hadoop:service=NameNode,name=NameNodeInfo', 'Total'),
        'hdfs.namenode.cluster_remaining':          ('hadoop:service=NameNode,name=NameNodeInfo', 'Free'),
        'hdfs.namenode.cluster_used':               ('hadoop:service=NameNode,name=NameNodeInfo', 'Used'),
        'hdfs.namenode.ops_create_file':            ('hadoop:service=NameNode,name=NameNodeActivity', 'CreateFileOps'),
        'hdfs.namenode.ops_delete_file':            ('hadoop:service=NameNode,name=NameNodeActivity', 'DeleteFileOps'),
        'hdfs.namenode.ops_file_info':              ('hadoop:service=NameNode,name=NameNodeActivity', 'FileInfoOps'),
        'hdfs.namenode.ops_syncs':                  ('hadoop:service=NameNode,name=NameNodeActivity', 'SyncsNumOps'),
        'hdfs.namenode.ops_transactions':           ('hadoop:service=NameNode,name=NameNodeActivity', 'TransactionsNumOps')
    },
    'datanode': {
        'hdfs.datanode.ds_capacity':                ('hadoop:service=DataNode,name=FSDatasetState-DS.*', 'Capacity'),
        'hdfs.datanode.ds_remaining':               ('hadoop:service=DataNode,name=FSDatasetState-DS.*', 'Remaining'),
        'hdfs.datanode.ds_used':                    ('hadoop:service=DataNode,name=FSDatasetState-DS.*', 'DfsUsed'),
        'hdfs.datanode.ds_failed':                  ('hadoop:service=DataNode,name=FSDatasetState-DS.*', 'NumFailedVolumes'),
        'hdfs.datanode.blocks_read':                ('hadoop:service=DataNode,name=DataNodeActivity-.*', 'blocks_read'),
        'hdfs.datanode.blocks_removed':             ('hadoop:service=DataNode,name=DataNodeActivity-.*', 'blocks_removed'),
        'hdfs.datanode.blocks_replicated':          ('hadoop:service=DataNode,name=DataNodeActivity-.*', 'blocks_replicated'),
        'hdfs.datanode.blocks_verified':            ('hadoop:service=DataNode,name=DataNodeActivity-.*', 'blocks_verified'),
        'hdfs.datanode.blocks_written':             ('hadoop:service=DataNode,name=DataNodeActivity-.*', 'blocks_written'),
        'hdfs.datanode.bytes_read':                 ('hadoop:service=DataNode,name=DataNodeActivity-.*', 'bytes_read'),
        'hdfs.datanode.bytes_written':              ('hadoop:service=DataNode,name=DataNodeActivity-.*', 'bytes_written'),
        'hdfs.datanode.reads_from_local_client':    ('hadoop:service=DataNode,name=DataNodeActivity-.*', 'reads_from_local_client'),
        'hdfs.datanode.writes_from_local_client':   ('hadoop:service=DataNode,name=DataNodeActivity-.*', 'writes_from_local_client'),
        'hdfs.datanode.reads_from_remote_client':   ('hadoop:service=DataNode,name=DataNodeActivity-.*', 'reads_from_remote_client'),
        'hdfs.datanode.writes_from_remote_client':  ('hadoop:service=DataNode,name=DataNodeActivity-.*', 'writes_from_remote_client'),
        'hdfs.datanode.ops_block_copy':             ('hadoop:service=DataNode,name=DataNodeActivity-.*', 'copyBlockOpNumOps'),
        'hdfs.datanode.ops_block_read':             ('hadoop:service=DataNode,name=DataNodeActivity-.*', 'readBlockOpNumOps'),
        'hdfs.datanode.ops_block_write':            ('hadoop:service=DataNode,name=DataNodeActivity-.*', 'writeBlockOpNumOps'),
        'hdfs.datanode.ops_block_replace':          ('hadoop:service=DataNode,name=DataNodeActivity-.*', 'replaceBlockOpNumOps'),
        'hdfs.datanode.ops_block_checksum':         ('hadoop:service=DataNode,name=DataNodeActivity-.*', 'blockChecksumOpNumOps'),
        'hdfs.datanode.ops_block_reports':          ('hadoop:service=DataNode,name=DataNodeActivity-.*', 'blockReportsNumOps'),
        'hdfs.datanode.ops_heartbeat':              ('hadoop:service=DataNode,name=DataNodeActivity-.*', 'heartBeatsNumOps'),
    },
    'jobtracker': {
        'mapred.jobtracker.nodes_total':            ('hadoop:service=JobTracker,name=JobTrackerInfo', 'SummaryJson', 'nodes'),
        'mapred.jobtracker.nodes_alive':            ('hadoop:service=JobTracker,name=JobTrackerInfo', 'SummaryJson', 'alive'),
        'mapred.jobtracker.map_slots':              ('hadoop:service=JobTracker,name=JobTrackerInfo', 'SummaryJson', 'slots', 'map_slots'),
        'mapred.jobtracker.map_slots_used':         ('hadoop:service=JobTracker,name=JobTrackerInfo', 'SummaryJson', 'slots', 'map_slots_used'),
        'mapred.jobtracker.reduce_slots':           ('hadoop:service=JobTracker,name=JobTrackerInfo', 'SummaryJson', 'slots', 'reduce_slots'),
        'mapred.jobtracker.reduce_slots_used':      ('hadoop:service=JobTracker,name=JobTrackerInfo', 'SummaryJson', 'slots', 'reduce_slots_used'),
        'mapred.jobtracker.jobs':                   ('hadoop:service=JobTracker,name=JobTrackerInfo', 'SummaryJson', 'jobs'),
    },
    'tasktracker': {
        'mapred.tasktracker.tasks_running':         ('hadoop:service=TaskTracker,name=TaskTrackerInfo', 'TasksInfoJson', 'running'),
        'mapred.tasktracker.tasks_failed':          ('hadoop:service=TaskTracker,name=TaskTrackerInfo', 'TasksInfoJson', 'failed'),
        'mapred.tasktracker.commit_pending':        ('hadoop:service=TaskTracker,name=TaskTrackerInfo', 'TasksInfoJson', 'commit_pending'),
    },
    'masterserver': {
        'hbase.masterserver.cluster_requests':      ('hadoop:service=Master,name=MasterStatistics', 'cluster_requests'),
    },
    'regionserver': {
        'hbase.regionserver.cache_capacity':        ('hadoop:service=RegionServer,name=RegionServerStatistics', 'blockCacheSize'),
        'hbase.regionserver.cache_remianing':       ('hadoop:service=RegionServer,name=RegionServerStatistics', 'blockCacheFree'),
        'hbase.regionserver.cache_used':            ('hadoop:service=RegionServer,name=RegionServerStatistics', 'blockCacheCount'),
        'hbase.regionserver.cache_hit_count':       ('hadoop:service=RegionServer,name=RegionServerStatistics', 'blockCacheHitCount'),
        'hbase.regionserver.cache_miss_count':      ('hadoop:service=RegionServer,name=RegionServerStatistics', 'blockCacheMissCount'),
        'hbase.regionserver.regions':               ('hadoop:service=RegionServer,name=RegionServerStatistics', 'regions'),
        'hbase.regionserver.requests':              ('hadoop:service=RegionServer,name=RegionServerStatistics', 'requests'),
        'hbase.regionserver.stores':                ('hadoop:service=RegionServer,name=RegionServerStatistics', 'stores'),
        'hbase.regionserver.storefiles':            ('hadoop:service=RegionServer,name=RegionServerStatistics', 'storefiles'),
    },
    'zookeeper': {
        'packets_received':                         ('org.apache.ZooKeeperService:name0=ReplicatedServer_id[0-9]+,name1=replica.[0-9]+,name2=Follower', 'PacketsReceived'),
        'packets_sent':                             ('org.apache.ZooKeeperService:name0=ReplicatedServer_id[0-9]+,name1=replica.[0-9]+,name2=Follower', 'PacketsSent'),
        'outstanding_requests':                     ('org.apache.ZooKeeperService:name0=ReplicatedServer_id[0-9]+,name1=replica.[0-9]+,name2=Follower', 'OutstandingRequests'),
        'avg_request_latency':                      ('org.apache.ZooKeeperService:name0=ReplicatedServer_id[0-9]+,name1=replica.[0-9]+,name2=Follower', 'AvgRequestLatency'),
    },
}


def fetch_jmx(data, host, port, jmxproxy_host, jmxproxy_port):
    attribute = {}
    bean_list = []
    for domain in dict((d[0].split(':')[0], True) for d in data.values()).keys():
        bean_host = 'http://%s:%d/jmxproxy/%s:%d/%s' % (jmxproxy_host, jmxproxy_port, host, port, domain)
        bean_html = parseXML(urllib2.urlopen(bean_host).read())
        bean_list += [urllib2.unquote(item.find('a').get('href')) for item in bean_html.find('body/ul')]

    for key, val in data.items():
        for bean in bean_list:
            if re.match('^%s$' % val[0], bean):
                attr_host = 'http://%s:%d/jmxproxy/%s:%d/%s/%s' % (jmxproxy_host, jmxproxy_port, host, port, urllib2.quote(bean), urllib2.quote(val[1]))
                attr_json = json.loads(urllib2.urlopen(attr_host).read())

                if type(attr_json) == dict:
                    for i in xrange(2, len(val)):
                        attr_json = attr_json[val[i]]

                attribute[key] = attr_json + attribute.get(key, 0)

    return attribute

def send_data(data, graphite_host, graphite_port):
    s = socket.socket()
    try:
        s.connect((graphite_host, graphite_port))
    except:
        pass

    t = int(time.time())
    m = ["%s %s %d" %  (key, val, t) for key, val in data.items()]

    s.send('\n'.join(m))


if __name__ == '__main__':
    host = socket.gethostname()

    import optparse
    parser = optparse.OptionParser(usage="%prog [options] <port>")
    parser.add_option('-m', '--host', default=host,
                      help='host to probe, defaults to local hostname')
    parser.add_option('-e', '--hadoop_service', default=None,
                      help='extra module to load and map to import')
    parser.add_option('--graphite_key', default='hadoop',
                      help='graphite key prefix')
    parser.add_option('--graphite_host', default='localhost',
                      help='graphite host')
    parser.add_option('--graphite_port', type='int', default=2003,
                      help='graphite port')
    parser.add_option('--jmxproxy_host', default='localhost',
                      help='jmxproxy host')
    parser.add_option('--jmxproxy_port', type='int', default=8080,
                      help='jmxproxy port')
    opts, args = parser.parse_args()

    port = int(args[0])

    request = dict(JVM_STATS.items() + HADOOP_STATS.get(opts.hadoop_service, {}).items())
    data = fetch_jmx(request, host, port, opts.jmxproxy_host, opts.jmxproxy_port)
    data = dict(('%s.%s.%s' % (opts.graphite_key, k, host), v) for k, v in data.items())

    send_data(data, opts.graphite_host, opts.graphite_port)
