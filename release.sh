#!/bin/bash -e

cd $(dirname $(readlink -e $0))
/usr/bin/curl -s https://raw.github.com/mk23/sandbox/master/misc/release.py | exec /usr/bin/env python2.7 - --commit -e pom.xml '<artifactId>jmxproxy</artifactId>\s+<version>{version}</version>' -e README.md 'jmxproxy-{version}.jar' -e scripts/server/start.sh 'jmxproxy-{version}.jar' $@
