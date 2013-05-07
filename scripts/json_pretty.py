#!/usr/bin/env python2.7

import json
import sys

print json.dumps(json.loads(sys.stdin.read()), indent=4)
