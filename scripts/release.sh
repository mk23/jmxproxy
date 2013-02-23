#!/bin/bash -x

cd $(dirname $(readlink -e $0))
exec scripts/release.py -e pom.xml '<artifactId>jmxproxy</artifactId>\s+<version>{version}</version>' --commit
