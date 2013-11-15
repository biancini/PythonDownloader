#!/usr/bin/env python
# -*- coding: utf-8 -*-

import os
import sys
import signal
import time
import getopt
import threading

from TwitterEngine import TwitterApiCall
from TwitterEngine import DownloadTweetsREST, DownloadTweetsStream

lock_file = '/var/lock/twitter.lock'


class Engine(object):
  engine = None
  running = False

  def __init__(self, method):
    try:
      if mechanism == 'rest': self.engine = DownloadTweetsREST('oAuth2')
      else: self.engine = DownloadTweetsStream('oAuth1')

    except Exception as e:
      print "Received exception: %s" % e

  def signal_handler(self, signum, frame):
    if not self.running:
      print 'You pressed Ctrl+C! The program will interrupt IMMEDIATELY!'
      os.remove(lock_file)
      sys.exit(-1)
    else:
      print 'You pressed Ctrl+C! The program will interrupt after this execution.'
      self.running = False
      TwitterApiCall.stop_run()

  def run(self):
    if not background:
      print "Running engine only one time..."
      self.running = False
      download(mechanism)
    else:
      print "Running the engine in background mode, continuously.... (press Ctrl+C or send SIGINT to interrupt)"
      self.running = True
      while (self.running):
        download_thread = threading.Thread(target=self.download)
        download_thread.start()
        time.sleep(5*60)

  def download(self):
    print "called download with parameter %s" % mechanism
    if os.path.exists(lock_file):
      print "Stopping because another process is already running."
      print "This script is locked with %s" % lock_file
      sys.exit(-1)
    else:
      open(lock_file, "w").write("1")
      try:
        self.engine.ProcessTweets()
      except Exception as e:
        print "Received exception: %s" % e
      finally:
        os.remove(lock_file)

def parseargs(name, argv):
  background = False
  mechanism = 'rest'

  try:
    opts, args = getopt.getopt(argv,"hbm:",["background","mechanism="])
  except getopt.GetoptError:
    print '%s [-b] -m <mechanism>' % name
    sys.exit(2)

  for opt, arg in opts:
    if opt == '-h':
      print '%s [-b] -m <mechanism>' % name
      sys.exit()
    elif opt in ("-b", "--background"):
      background = True
    elif opt in ("-m", "--mechanism"):
      if arg not in ['rest', 'stream']: print 'Wrong mechanism passed, assuming rest.'
      else: mechanism = arg

  return (background, mechanism)

if __name__ == "__main__":
  (background, mechanism) = parseargs(sys.argv[0], sys.argv[1:])
  engine = Engine(mechanism)

  signal.signal(signal.SIGINT, engine.signal_handler)
  engine.run()
