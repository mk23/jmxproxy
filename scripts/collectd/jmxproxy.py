#!/usr/bin/env python
"""
Author: David Markus <dmarkus@apple.com>
Compatability: Python2.7, Python3 - only non stdlib dependency is collectd

Description
-----------
Fetches specified list of attributes for collectd from specified MBean,
using JMX Proxy for the connection to the JVM.

MBean Descriptor Normalization
------------------------------
The plugin normalizes MBean keys as returned by jmxproxy into something that
better resembles a graphite key.

It makes substitutions in, order, based on CollectdJMXProxy.REGEX_GKEY_REPLACEMENTS:

1. replace any existing instances of '.' with '-' (e.g. in IPv4 addresses)
2. replace any existing instances whitespace with '_'
3. replace some delimiting characters (see regex for full list) with '.'
4. delete instances of backslash or quotation marks
5. finally, clean up edge cases that occur around underscore and dot replacements

Example Replacement Case:
java.lang:type=Threading DaemonThreadCount

becomes

jmxproxy.java-lang.type.Threading.DaemonThreadCount

Example Collectd Config
-----------------------
Example collectd config, configured as part of the python plugin
(The only required option is the URL of the jmxproxy instance):

***
Import "collectd_jmxproxy_python"

  <Module "collectd_jmxproxy_python">
    Namespace "jmxproxy"
    Url "http://localhost:9090/jmxproxy/localhost:18104/"
    Pattern "^(jmxproxy.java.lang.*)+$"
  </Module>
***

Note: gathers numeric types only, treats all values as 'gauge' type.
"""
from __future__ import print_function, absolute_import

import logging
import re
from numbers import Number

# resolve json module in order of performance
try:
    import ujson as json
except ImportError:
    import json

# resolve urlopen for python2/python3
try:
    from urllib.request import urlopen
    from urllib.parse import urlencode
except ImportError:
    from urllib import urlencode
    from urllib2 import urlopen

class CollectdJMXProxy(object):
    """
    namespace to contain plugin methods, since global config is required
    """
    REGEX_GKEY_REPLACEMENTS = [
        (re.compile(r'[\.]'), '-'),
        (re.compile(r'\s'), '_'),
        (re.compile(r'[,\/=:]'), '.'),
        (re.compile(r'[\\"\']'), ''),
        (re.compile(r'_?\._?(\.)*'), '.')]

    def __init__(self):
        self.logger = None
        self.conf = {}

    def _normalize_string_list_as_graphite_key(self, str_list):
        """
        replaces invalid key characters, and joins list with '.'

        :param str_list: list of strings to be normalized to graphite key
        :type str_list: list
        :returns: normalized graphite key string
        :rtype: str
        """
        str_list = list(str_list)

        for idx, _ in enumerate(str_list):
            str_list[idx] = str_list[idx].strip()
            for pat in self.REGEX_GKEY_REPLACEMENTS:
                str_list[idx] = pat[0].subn(pat[1], str_list[idx])[0]
        return '.'.join(str_list)

    def send_results_console(self, key, val):
        """
        log (key, val) to console
        """
        self.logger.info('%s: %s', str(key), str(val))

    @staticmethod
    def send_results_collectd(key, val):
        """
        send (key, val) to collectd dispatcher
        """
        w_val = collectd.Values(type='gauge')
        w_val.plugin = str(key)
        w_val.dispatch(values=[val])

    def select_data(self, data, send_cbk):
        """
        get data from jmxproxy, filter by regex pattern,
        call processing callback

        :param data: mbean data
        :type data: dict
        :param send_cbk: callback that takes (key, val) as arg
        :type send_cbk: callable
        :returns: None
        """
        if not self.conf.get('pattern', None):
            self.conf['pattern'] = '^(.*)$'

        pat = re.compile(self.conf['pattern'])
        if not isinstance(data, dict):
            raise TypeError(
                'data argument must be of type dict, not %s', type(data))

        for key, val in data.items():
            match = pat.match(key)
            if match:
                if not len(match.groups()):
                    raise ValueError('must specify one or more regex match groups')
                for name, attr in val.items():
                    attr = attr[0]
                    if not isinstance(attr, Number):
                        # only handle numeric types
                        continue
                    if self.conf.get('raw_keys', None):
                        send_cbk(
                            key + ':\n    %s' % (name),
                            float(attr)) 
                    else:
                        gkey = sum([
                            [],
                            list(match.groups()),
                            [name]], [])
                        send_cbk(
                            '%s.%s' % (
                                self.conf.get('namespace', 'jmxproxy'),
                                self._normalize_string_list_as_graphite_key(gkey)),
                            float(attr))

    def poll(self, data_proc_callback):
        """
        retrieves set of mbeans/attrs from jmxproxy, sends to callback 
        
        :param data_proc_callback: method to call with data dict as arg
        :type data_proc_callback: callable
        """
        params = urlencode({
            'full': True,
            'limit': 1})
        url = self.conf['url'] + '?' + params

        self.logger.debug(url)
        return data_proc_callback(json.loads(
            urlopen(url).read().decode('utf-8')))

    def cli_main(self):
        """
        provides CLI interface for testing collectd plugin
        """
        import argparse
        import sys

        # configure logger
        self.logger = logging.getLogger(__name__)
        logging.basicConfig(
            format='%(message)s',
            level=logging.INFO,
            stream=sys.stdout)

        # configure global cli settings
        parser = argparse.ArgumentParser(
            description='collect JMX data through jmxproxy',
            formatter_class=argparse.ArgumentDefaultsHelpFormatter)
        parser.add_argument('url', help='URL for jmxproxy instance')
        parser.add_argument(
            '-r', '--raw-keys',
            action='store_true',
            help=('when set, omit graphite key normalization. '
                  'Useful for regex debugging.'))
        parser.add_argument(
            'pattern', nargs='?', help='regex pattern to filter MBeans')
        self.conf.update(vars(parser.parse_args()))

        # run query
        self.poll(lambda data: self.select_data(
            data, self.send_results_console))

    def collectd_main(self):
        """
        registers callbacks for plugin use with collectd
        """

        #configure logger
        self.logger = collectd

        # set config callback
        collectd.register_config(
            lambda conf: self.conf.update({
                n.key.lower(): n.values[0] for n in conf.children}))

        # set read callback 
        collectd.register_read(
            lambda: self.poll(lambda data: self.select_data(
                data, self.send_results_collectd)))

if __name__ == '__main__':
    CollectdJMXProxy().cli_main()
else:
    import collectd
    CollectdJMXProxy().collectd_main()
