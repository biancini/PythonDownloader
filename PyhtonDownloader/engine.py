#!/usr/bin/env python
# -*- coding: utf-8 -*-

import os
import sys
import signal
import time
import getopt
import threading

from TwitterEngine import instances
from TwitterEngine import TwitterApiCall
from TwitterEngine import DownloadTweetsREST, DownloadTweetsStream

class Engine(object):
  engines = []
  running = False
  waittime = 5 * 60

  def __init__(self, engines, language):
    for cur_engine in engines:
      try:
        if cur_engine['type'] == 'rest': engine = DownloadTweetsREST(cur_engine['name'], language, cur_engine['filters'], 'oAuth2')
        else: engine = DownloadTweetsStream(cur_engine['name'], language, cur_engine['filters'], 'oAuth1')
        
        engine.SetLockFileDownload('/var/lock/twitter_%s_download.lock' % cur_engine['name'])
        self.engines.append(engine)
      except Exception as e:
        print "Received exception: %s" % e
      
      print 'Initialized %s engine %s' % (cur_engine['type'], cur_engine['name'])
      
    signal.signal(signal.SIGINT, self.signal_handler)

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
      for cur_engine in self.engines:
        self.download(cur_engine)
    else:
      print "Running the engine in background mode, continuously.... (press Ctrl+C or send SIGINT to interrupt)"
      self.running = True

      while (self.running):
        for cur_engine in self.engines:
          threading.Thread(target=self.download, args=[cur_engine]).start()
        time.sleep(self.waittime)

  def download(self, cur_engine):
    print "Starting engine %s" % cur_engine.GetEngineName()
    
    if os.path.exists(cur_engine.GetLockFileDownload()):
      print "Stopping because another process is already running."
      print "This script is locked with %s" % cur_engine.GetLockFileDownload()
      sys.exit(-1)
    else:
      open(cur_engine.GetLockFileDownload(), "w").write("1")
      try:
        cur_engine.ProcessTweets()
      except Exception as e:
        print "Received exception: %s" % e
      finally:
        if os.path.exists(cur_engine.GetLockFileDownload()):
          os.remove(cur_engine.GetLockFileDownload())

def parseargs(name, argv):
  background = False
  engine_name = None
  
  try:
    opts, _args = getopt.getopt(argv, "hbe:", ["background", "enginename="])
  except getopt.GetoptError:
    print '%s [-b]' % name
    sys.exit(2)

  for opt, arg in opts:
    if opt == '-h':
      print '%s [-b] [-e default]' % name
      sys.exit()
    elif opt in ("-b", "--background"):
      background = True
    elif opt in ("-e", "--enginename"):
      engine_name = arg

  return (background, engine_name)
    
if __name__ == "__main__":
  (background, start_name) = parseargs(sys.argv[0], sys.argv[1:])
  engine = Engine(instances.INSTANCES, instances.LANGUAGE)
  engine.run()
