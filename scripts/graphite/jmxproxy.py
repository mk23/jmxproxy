#!/usr/bin/env python2.7

import argparse
import json
import re
import socket
import time
import urllib2

mbeans = {
    'jvm.thread_count':     ('java.lang:type=Threading', 'ThreadCount'),
    'jvm.thread_peak':      ('java.lang:type=Threading', 'PeakThreadCount'),
    'jvm.memory_heap_used': ('java.lang:type=Memory', 'HeapMemoryUsage', 'used'),
    'jvm.memory_heap_max':  ('java.lang:type=Memory', 'HeapMemoryUsage', 'max'),
    'jvm.gc_count':         ('java.lang:type=GarbageCollector,name=.*', 'CollectionCount'),
    'jvm.classes_loaded':   ('java.lang:type=ClassLoading', 'LoadedClassCount'),
    'jvm.classes_total':    ('java.lang:type=ClassLoading', 'TotalLoadedClassCount'),
    'jvm.classes_unloaded': ('java.lang:type=ClassLoading', 'UnloadedClassCount'),
}

parser = argparse.ArgumentParser(description='jmxproxy poller for graphite')
parser.add_argument('--service-host', default=socket.gethostname(),
                    help='jvm jmx agent service host')
parser.add_argument('--service-port', type=int, required=True,
                    help='jvm jmx agent service port')
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


def main(args=None, service='', extra_stats={}):
    if args is None:
        args = parser.parse_args()

    mbeans.update(extra_stats)
    data = fetch_jmx(mbeans, args.service_host, args.service_port, args.jmxproxy_host, args.jmxproxy_port)
    path = '.'.join(i for i in (args.graphite_key, args.service_host, service) if i)

    send_data(data, path, args.graphite_host, args.graphite_port, send=not args.dry_run)

if __name__ == '__main__':
    main()
