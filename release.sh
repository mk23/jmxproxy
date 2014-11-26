#!/bin/bash -e

BRANCH=$(git rev-parse --abbrev-ref HEAD)
if [ "${BRANCH}" = "HEAD" ] ; then
	echo "Cannot release on detached HEAD.  Please switch to a branch."
	exit 1
fi

read -p "Would you like to commit ${BRANCH} branch? [y/N] " -r
if [[ $REPLY =~ ^[Yy]$ ]] ; then
	COMMIT=--commit
fi

echo
(
	/usr/bin/curl -L -s https://raw.github.com/mk23/sandbox/master/misc/release.py ||
	echo 'raise Exception("unable to load release.py")'
) |
	exec /usr/bin/env python2.7 - ${COMMIT} --release stable \
		-e pom.xml '<artifactId>jmxproxy</artifactId>\s+<version>{version}</version>' \
		-e README.md 'jmxproxy-{version}.jar' \
		-e README.md 'jmxproxy.{version}.tar.gz' \
		-e README.md '/download/jmxproxy.{version}/' \
		-e scripts/server/start.sh 'jmxproxy-{version}.jar' \
		"$@"

if [ -n "${COMMIT}" ] ; then
	echo
	TAG=$(git describe --abbrev=0 --tags)
	echo "Created tag ${TAG}"

	for REMOTE in $(git remote -v | cut -f1 | uniq) ; do
		read -p "Would you like to push to ${REMOTE}? [y/N] " -r
		if [[ $REPLY =~ ^[Yy]$ ]] ; then
		    git push "${REMOTE}" "${BRANCH}" "${TAG}"
		fi
	done
fi
