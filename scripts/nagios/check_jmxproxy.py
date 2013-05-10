#!/usr/bin/env python2.7

import argparse
import json
import re
import sys
import traceback
import urllib
import urllib2

SUCCESS, WARNING, CRITICAL, UNKNOWN = range(4)
ERRORS = dict((v, k) for k, v in vars(sys.modules[__name__]).items() if type(v) == int)

def lookup_val(host, port, expr, auth=None, proxy='localhost:8080', cache={}):
    item = expr.split('//')
    name = item.pop(0)
    attr = item.pop(0)

    if auth:
        auth = urllib.urlencode(zip(('username', 'password'), auth.split(':')))

    if not cache:
        cache.update(dict((i, {}) for i in json.loads(urllib2.urlopen('http://%s/%s:%d' % (proxy, host, port), auth).read())))

    for bean in cache:
        if re.match(name + '$', bean, re.I):
            if not cache[bean]:
                cache[bean].update(dict((i, None) for i in json.loads(urllib2.urlopen('http://%s/%s:%d/%s' % (proxy, host, port, urllib.quote_plus(bean)), auth).read())))

            if cache[bean][attr] is None:
                cache[bean][attr] = json.loads(urllib2.urlopen('http://%s/%s:%d/%s/%s' % (proxy, host, port, urllib.quote_plus(bean), urllib.quote_plus(attr)), auth).read())

            val = cache[bean][attr]
            for key in item:
                val = val[key] if isinstance(val, dict) else json.loads(val)[key]

            if type(val) in (list, tuple):
                return len(val)
            elif unicode(val).isnumeric():
                return float(val)
            else:
                return val

    raise KeyError('%s[%s]: attribute not available' % (bean, attr))

if __name__ == '__main__':
    master_parser = argparse.ArgumentParser(description='nagios jmxproxy plugin')
    master_parser.add_argument('-a', '--host', required=True,
                        help='jmx agent host')
    master_parser.add_argument('-p', '--port', type=int, required=True,
                        help='jmx agent port')
    master_parser.add_argument('-c', '--auth',
                        help='jmx agent credentials as username:password')
    master_parser.add_argument('-e', '--expr', type=unicode, required=True,
                        help='text attribute or rpn expression to calculate')
    master_parser.add_argument('-j', '--proxy', default='localhost:8080',
                        help='jmxproxy host:port')
    master_parser.add_argument('-i', '--invert', action='store_true',
                        help='negates text match or forces negative crit/warn thresholds')
    master_parser.add_argument('-f', '--format', default=None,
                        help='output format string for successul check')

    slave_parsers = master_parser.add_subparsers(dest='mode')

    parser = slave_parsers.add_parser('textual')
    parser.add_argument('text', help='expected attribute value')

    parser = slave_parsers.add_parser('metrics')
    parser.add_argument('-w', '--warn', type=float, required=True,
                        help='warning threshold value')
    parser.add_argument('-c', '--crit', type=float, required=True,
                        help='critical threshold value')
    args = master_parser.parse_args()

    results = [[] for i in range(4)]
    try:
        if args.mode == 'textual':
            item = lookup_val(args.host, args.port, args.expr, args.auth, args.proxy)
            if not args.invert and item == args.text or args.invert and item != args.text:
                results[SUCCESS].append((args.format or 'valid attribute value: {result}').format(result=item))
            else:
                results[CRITICAL].append((args.format or 'invalid attribute value: {result}').format(result=item))

        elif args.mode == 'metrics':
            stack = []
            for item in args.expr.split():
                if item == '+':
                    stack.append(stack.pop() + stack.pop())
                elif item == '-':
                    stack.append(stack.pop() - stack.pop())
                elif item == '/':
                    stack.append(stack.pop() / stack.pop())
                elif item == '*':
                    stack.append(stack.pop() * stack.pop())
                elif item == '%':
                    stack.append(stack.pop() % stack.pop())
                elif '//' in item:
                    stack.append(lookup_val(args.host, args.port, item, args.auth, args.proxy))
                elif item.isnumeric():
                    stack.append(float(item))
                else:
                    raise ValueError('invalid rpn expression')

            results[SUCCESS].append((args.format or 'calculated value: {result:.02f}  threshold: {limit:.02f}').format(result=stack[0], limit=args.crit))

            if args.invert:
                if stack[0] < args.crit:
                    results[CRITICAL].append((args.format or 'derived expression result {result:.02f} is below critical threshold of {limit:.02f}').format(result=stack[0], limit=args.crit))
                if stack[0] < args.warn:
                    results[WARNING].append((args.format or 'derived expression result {result:.02f} is below warning threshold of {limit:.02f}').format(result=stack[0], limit=args.warn))
            else:
                if stack[0] > args.crit:
                    results[CRITICAL].append((args.format or 'derived expression result {result:.02f} is above critical threshold of {limit:.02f}').format(result=stack[0], limit=args.crit))
                if stack[0] > args.warn:
                    results[WARNING].append((args.format or 'derived expression result {result:.02f} is above warning threshold of {limit:.02f}').format(result=stack[0], limit=args.warn))

    except urllib2.HTTPError as e:
        results[CRITICAL].append('(%d) %s' % (e.code, e.msg))
    except urllib2.URLError as e:
        results[CRITICAL].append(e[0][1])
    except Exception:
        print 'unknown error'
        traceback.print_exc(file=sys.stdout)
        sys.exit(UNKNOWN)


    for code in (CRITICAL, UNKNOWN, WARNING, SUCCESS):
            size = len(results[code])
            if size > 1:
                print '%d %s results' % (size, ERRORS[code])
                print '\n'.join(results[code])
                sys.exit(code)
            if size == 1:
                print results[code][0]
                sys.exit(code)

    print 'no results found'
    sys.exit(UNKNOWN)

