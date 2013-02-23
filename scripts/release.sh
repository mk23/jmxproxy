#!/bin/bash -e

cd $(dirname $(dirname $(readlink -e $0)))
exec scripts/release.py -e pom.xml '<artifactId>jmxproxy</artifactId>\s+<version>{version}</version>' --commit $@
