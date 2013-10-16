#!/usr/bin/env python
# -*- coding: utf-8 -*-

import os
import sys

from TwitterEngine import DownloadTweetsREST, DownloadTweetsStream

if __name__ == "__main__":
  mechanism = 'rest'
  if len(sys.argv) > 1: mechanism = sys.argv[1]

  if mechanism not in ['rest', 'stream']:
    print 'Wrong mechanism passed, assuming rest.'
    mechanism = 'rest'

  lock_file = '/var/lock/twitter.lock'
  if os.path.exists(lock_file):
    print "Stopping because another process is already running."
    print "This script is locked with %s" % lock_file
    sys.exit(-1)
  else:
    open(lock_file, "w").write("1")
    try:
      if mechanism == 'rest': engine = DownloadTweetsREST('oAuth2')
      else: engine = DownloadTweetsStream('oAuth1')

      engine.ProcessTweets()
    except Exception as e:
      print "Received exception: %s" % e
    finally:
      os.remove(lock_file)

