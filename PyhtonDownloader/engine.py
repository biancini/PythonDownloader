#!/usr/bin/env python
# -*- coding: utf-8 -*-

import os
import sys
import signal
import time
import getopt
import threading

from datetime import date, timedelta, datetime

from TwitterEngine import TwitterApiCall
from TwitterEngine import DownloadTweetsREST, DownloadTweetsStream

lock_file_download = '/var/lock/twitter_download.lock'
lock_file_byperson = '/var/lock/twitter_byperson.lock'

class Engine(object):
  engine = None
  activity = None
  running = False
  waittime = 5*60

  def __init__(self, method, activity):
    try:
      self.activity = activity

      if mechanism == 'rest': self.engine = DownloadTweetsREST('oAuth2')
      else: self.engine = DownloadTweetsStream('oAuth1')
    except Exception as e:
      print "Received exception: %s" % e

  def signal_handler(self, signum, frame):
    if not self.running:
      print 'You pressed Ctrl+C! The program will interrupt IMMEDIATELY!'
      if os.path.exists(lock_file_download): os.remove(lock_file_download)
      if os.path.exists(lock_file_byperson): os.remove(lock_file_byperson)
      sys.exit(-1)
    else:
      print 'You pressed Ctrl+C! The program will interrupt after this execution.'
      self.running = False
      TwitterApiCall.stop_run()

  def run(self):
    if not background:
      print "Running engine only one time..."
      self.running = False
      if activity == 'download' or actitivity == 'all':
        self.download()
      elif activity == 'byperson':
        self.byperson()
    else:
      print "Running the engine in background mode, continuously.... (press Ctrl+C or send SIGINT to interrupt)"
      self.running = True

      while (self.running):
        download_thread = threading.Thread(target=self.download)
        download_thread.start()

        now = datetime.now()
	seconds = (now - now.replace(hour=0, minute=0, second=0, microsecond=0)).total_seconds()
        seconds -= 3600 # move one hour after midnight
        if seconds > 0 and seconds <= self.waittime: # five minutes
          byperson_thread = threading.Thread(target=self.byperson)
          byperson_thread.start()

        time.sleep(self.waittime)

  def byperson(self):
    print "Called byperson with parameter %s" % self.mechanism
    if os.path.exists(lock_file_byperson):
      print "Stopping because another process is already running."
      print "This script is locked with %s" % lock_file_byperson
      sys.exit(-1)
    else:
      open(lock_file_byperson, "w").write("1")
      try:
        yesterday = date.today() - timedelta(1)
        self.engine.AggregateByPerson(yesterday)
      except Exception as e:
        print "Received exception: %s" % e
      finally:
        os.remove(lock_file_byperson)

  def download(self):
    print "Called download with parameter %s" % self.mechanism
    if os.path.exists(lock_file_download):
      print "Stopping because another process is already running."
      print "This script is locked with %s" % lock_file_download
      sys.exit(-1)
    else:
      open(lock_file_download, "w").write("1")
      try:
        self.engine.ProcessTweets()
      except Exception as e:
        print "Received exception: %s" % e
      finally:
        os.remove(lock_file_download)

def parseargs(name, argv):
  background = False
  mechanism = 'rest'
  activity = 'all'

  try:
    opts, args = getopt.getopt(argv,"hbm:a:",["background","mechanism=","activity="])
  except getopt.GetoptError:
    print '%s [-b] -m <mechanism> -a <activity>' % name
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
    elif opt in ("-a", "--activity"):
      if arg not in ['all', 'download', 'byperson']: print 'Wrong activity passed, assuming all.'
      else: activity = arg

  return (background, mechanism, activity)

if __name__ == "__main__":
  (background, mechanism, activity) = parseargs(sys.argv[0], sys.argv[1:])
  engine = Engine(mechanism, activity)

  signal.signal(signal.SIGINT, engine.signal_handler)
  engine.run()
