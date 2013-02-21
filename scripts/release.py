#!/usr/bin/env python2.7

import argparse
import datetime
import re
import subprocess
import sys


def package_info(key, cache={}):
    if not cache:
        lines = subprocess.check_output(['dpkg-parsechangelog']).split('\n')
        items = [line.split(': ', 1) for line in lines if line and not line.startswith(' ') and line != 'Changes: ']
        cache.update(dict((k.lower(), v) for k, v in items))

    return cache.get(key)

def bump_version(bump_major=False, bump_minor=False, bump_patch=False):
    types = {
        'ds': r'(?P<MAJOR>\d{8})\.(?P<PATCH>\d{3})',
        'mm': r'(?P<MAJOR>\d+)\.(?P<MINOR>\d+)\.(?P<BUILD>\d+)(?:\.(?P<PATCH>\.\d))?',
    }

    for label, regex in types.items():
        match = re.match(regex, package_info('version'))
        if match:
            if label == 'ds':
                today = datetime.date.today().strftime('%Y%m%d')
                major = match.group('MAJOR') if bump_patch else today
                patch = int(match.group('PATCH')) + 1 if bump_patch or match.group('MAJOR') == today else 1

                return '%s.%03d' % (major, patch)
            elif label == 'mm':
                major = int(match.group('MAJOR'))
                minor = int(match.group('MINOR'))
                build = int(match.group('BUILD'))
                patch = int(match.group('PATCH')) if match.group('PATCH') is not None else None
                v_fmt = '%%0%dd.%%0%dd.%%0%dd' % (len(match.group('MAJOR')), len(match.group('MINOR')), len(match.group('BUILD')))
                v_arg = []
                if bump_major:
                    major += 1
                    minor  = 0
                    build  = 0
                elif bump_minor:
                    minor += 1
                    build  = 0
                elif bump_patch:
                    v_fmt += '.%%0%dd' % (len(match.group('PATCH')) if patch is not None else 0)
                    patch  = 1 if patch is None else patch + 1
                else:
                    build += 1

                v_arg = [major, minor, build]
                if patch is not None:
                    v_arg.append(patch)

                return v_fmt % tuple(v_arg)

    raise RuntimeError('unknown version format detected')

def main(argv=sys.argv[1:]):
    parser = argparse.ArgumentParser(description='debian package release helper')
    action = parser.add_mutually_exclusive_group()
    parser.add_argument('-e', '--extra', default=[], nargs=2, action='append',
                        help='extra files to update with specified regex')
    action.add_argument('-j', '--major', default=False, action='store_true',
                        help='force increment major number')
    action.add_argument('-n', '--minor', default=False, action='store_true',
                        help='force increment minor number')
    action.add_argument('-t', '--patch', default=False, action='store_true',
                        help='force increment patch number')
    action.add_argument('-v', '--version',
                        help='force explicit version number')
    parser.add_argument('-p', '--package', default=package_info('source'),
                        help='package name')
    parser.add_argument('-c', '--commit', default=False, action='store_true',
                        help='commit and tag new changelog')
    args = parser.parse_args(argv)

    version = args.version or bump_version(args.major, args.minor, args.patch)
    changes = subprocess.check_output(['git', 'log', '--oneline', '%s.%s..HEAD' % (args.package, package_info('version'))]).strip().split('\n')[::-1]
    changed = ['debian/changelog']

    print 'creating changelog entry for %s ...' % version
    subprocess.check_output(['dch', '-b', '--newversion', version, 'Tagging %s' % version])
    for line in reversed(changes):
        if not line:
            continue
        else:
            sha1, text = line.strip().split(' ', 1)

        print '\tappending changelog message for %s ...' % sha1
        subprocess.check_output(['dch', '--append', '[%s] %s' % (sha1, text)])

    for name, text in args.extra:
        print 'checking %s ...' % name
        part = '(?P<PATTERN>%s)' % text.format(version='(?P<VERSION>.*?)')
        orig = open(name).read()
        find = re.search(part, orig)
        if find:
            print '\tmodifying %s ...' % name
            data = open(name, 'w')
            data.write(orig.replace(find.group('PATTERN'), find.group('PATTERN').replace(find.group('VERSION'), version)))
            data.close()
            changed.append(name)

    if args.commit:
        print 'updating git ...'
        for name in changed:
            print '\tadding changed file to git %s ...' % name
            subprocess.check_output(['git', 'add', name])
        print '\tcommitting changelog to git ...'
        subprocess.check_output(['git', 'commit', '-m', 'Tagging %s' % version] + changed)
        print '\ttagging changelog in git ...'
        subprocess.check_output(['git', 'tag', '%s.%s' % (args.package, version)])
        print 'release prep complete, verify and push the changes and tag'

if __name__ == '__main__':
    main()
