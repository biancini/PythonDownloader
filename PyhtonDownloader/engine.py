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
  engine = None
  mechanism = None
  running = False
  waittime = 5 * 60
  
  lock_file_download = None
  lock_file_byperson = None

  def __init__(self, engine_name, mechanism, language, filters):
    self.mechanism = mechanism
  
    self.lock_file_download = '/var/lock/twitter_%s_download.lock' % engine_name
    self.lock_file_byperson = '/var/lock/twitter_%s_byperson.lock' % engine_name
    
    try:
      if mechanism == 'rest':
        self.engine = DownloadTweetsREST(engine_name, language, filters, 'oAuth2')
        print 'Initialized rest engine %s' % engine_name
      else:
        self.engine = DownloadTweetsStream(engine_name, language, filters, 'oAuth1')
        print 'Initialized stream engine %s' % engine_name
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

engines = []

def signal_handler(signum, frame):
  for cur_engine in engines:
    cur_engine.signal_handler(signum, frame)
    
def run():
  for cur_engine in engines:
    engine_thread = threading.Thread(target=cur_engine.run)
    engine_thread.start()
    
if __name__ == "__main__":
  (background, start_name) = parseargs(sys.argv[0], sys.argv[1:])
  language = instances.LANGUAGE
  engine_instances = instances.INSTANCES
  
  signal.signal(signal.SIGINT, signal_handler)
  
  for cur_engine in engine_instances:
    if start_name is None or start_name == cur_engine['name']:
      engine_name = cur_engine['name'] or 'default'
      mechanism = cur_engine['type'] or 'rest'
      filters = cur_engine['filters'] or []
    
      cur_engine = Engine(engine_name, mechanism, language, filters)
      engines.append(cur_engine)
    
  run()