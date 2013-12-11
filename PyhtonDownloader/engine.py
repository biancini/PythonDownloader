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

class Engine(object):
  engine = None
  mechanism = None
  running = False
  waittime = 5 * 60
  
  engine_name = None
  lock_file_download = None
  lock_file_byperson = None

  def __init__(self, engine_name, mechanism):
    self.engine_name = engine_name
    
    self.lock_file_download = '/var/lock/twitter_%s_download.lock' % engine_name
    self.lock_file_byperson = '/var/lock/twitter_%s_byperson.lock' % engine_name
    
    self.mechanism = mechanism
    try:
      if mechanism == 'rest': self.engine = DownloadTweetsREST(self.engine_name, 'oAuth2')
      else: self.engine = DownloadTweetsStream(self.engine_name, 'oAuth1')
    except Exception as e:
      print "Received exception: %s" % e

  def signal_handler(self, signum, frame):
    if not self.running:
      print 'You pressed Ctrl+C! The program will be interrupted IMMEDIATELY!'
      if os.path.exists(self.lock_file_download): os.remove(self.lock_file_download)
      if os.path.exists(self.lock_file_byperson): os.remove(self.lock_file_byperson)
      sys.exit(-1)
    else:
      print 'You pressed Ctrl+C! The program will be interrupted after this execution.'
      self.running = False
      TwitterApiCall.stop_run()

  def run(self):
    if not background:
      print "Running engine only one time..."
      self.running = False
      self.download()
    else:
      print "Running the engine in background mode, continuously.... (press Ctrl+C or send SIGINT to interrupt)"
      self.running = True

      while (self.running):
        download_thread = threading.Thread(target=self.download)
        download_thread.start()

        time.sleep(self.waittime)

  def download(self):
    print "Called download with parameter %s" % self.mechanism
    if os.path.exists(self.lock_file_download):
      print "Stopping because another process is already running."
      print "This script is locked with %s" % self.lock_file_download
      sys.exit(-1)
    else:
      open(self.lock_file_download, "w").write("1")
      try:
        self.engine.ProcessTweets()
      except Exception as e:
        print "Received exception: %s" % e
      finally:
        if os.path.exists(self.lock_file_download):
          os.remove(self.lock_file_download)

def parseargs(name, argv):
  background = False
  mechanism = 'rest'
  engine_name = 'default'

  try:
    opts, _args = getopt.getopt(argv, "hn:bm:", ["name=", "background", "mechanism="])
  except getopt.GetoptError:
    print '%s [-b] -m <mechanism>' % name
    sys.exit(2)

  for opt, arg in opts:
    if opt == '-h':
      print '%s [-b] -m <mechanism>' % name
      sys.exit()
    elif opt in ("-n", "--name"):
      engine_name = arg
    elif opt in ("-b", "--background"):
      background = True
    elif opt in ("-m", "--mechanism"):
      if arg not in ['rest', 'stream']: print 'Wrong mechanism passed, assuming rest.'
      else: mechanism = arg

  return (engine_name, background, mechanism)

if __name__ == "__main__":
  (engine_name, background, mechanism) = parseargs(sys.argv[0], sys.argv[1:])
  engine = Engine(engine_name, mechanism)

  signal.signal(signal.SIGINT, engine.signal_handler)
  engine.run()
