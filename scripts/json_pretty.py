#!/usr/bin/env python2.7

import argparse
import json
import sys

def parse(obj, path):
    for part in path.strip('/').split('/'):
        if part in obj:
            obj = obj[part]

    return obj

if __name__ == '__main__':
    parser = argparse.ArgumentParser('json output prettyfier')
    parser.add_argument('-p', '--path', default='',
                        help='path to the interesting object')
    parser.add_argument('-s', '--size', default=False, action='store_true',
                        help='print size of the object instead')
    parser.add_argument('-f', '--file', default=sys.stdin, type=argparse.FileType(),
                        help='file to use instead of stdin')
    args = parser.parse_args()

    item = parse(json.loads(args.file.read()), args.path)

    if args.size:
        print len(item)
    else:
        print json.dumps(item, indent=4)
