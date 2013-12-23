#!/usr/bin/env python
# -*- coding: utf-8 -*-

import os
import sys
import signal
import time
import getopt
import threading

import logging
import logging.config

from TwitterEngine import instances
from TwitterEngine import TwitterApiCall
from TwitterEngine import DownloadTweetsREST, DownloadTweetsStream

logging.config.fileConfig("logging.conf")
logger = logging.getLogger('root')

class Engine(object):
  engines = []
  running = False
  waittime = 3 * 60

  def __init__(self, engine_instances, language):
    engine_index = -1
    for engine_config in engine_instances:
      engine_index += 1
      try:
        if engine_config['type'] == 'rest': cur_engine = DownloadTweetsREST(engine_config, language, 'oAuth2')
        else: cur_engine = DownloadTweetsStream(engine_config, language, 'oAuth1')
        
        if engine_config['locking']:
          cur_engine.SetLockFileDownload('/var/lock/twitter_%s_download.lock' % engine_config['name'])
        self.engines.append(cur_engine)
      except Exception as e:
        logger.error('Received exception: %s' % e)
      
      logger.info('Initialized %s cur_engine %s to use key number %d' % (engine_config['type'], engine_config['name'], engine_config['apikey']))
      
    signal.signal(signal.SIGINT, self.signal_handler)

  def signal_handler(self, signum, frame):
    if not self.running:
      logger.warning('You pressed Ctrl+C! The program will be interrupted IMMEDIATELY!')
      sys.exit(-1)
    else:
      logger.warning('You pressed Ctrl+C! The program will be interrupted after this execution.')
      self.running = False
      TwitterApiCall.stop_run()

  def run(self):
    if not background:
      logger.info('Running cur_engine only one time...')
      self.running = False
      for cur_engine in self.engines:
        self.download(cur_engine)
    else:
      logger.info('Running the cur_engine in background mode, continuously.... (press Ctrl+C or send SIGINT to interrupt)')
      self.running = True

      while (self.running):
        for cur_engine in self.engines:
          threading.Thread(target=self.download, args=[cur_engine]).start()
        time.sleep(self.waittime)

  def download(self, cur_engine):
    logger.debug('Starting download from cur_engine %s' % cur_engine.GetEngineName())
    
    if cur_engine.IsLocking() and os.path.exists(cur_engine.GetLockFileDownload()):
      logger.debug('Stopping because another process is already running.')
      logger.debug('This script is locked with %s' % cur_engine.GetLockFileDownload())
      return
    else:
      if cur_engine.IsLocking():
        open(cur_engine.GetLockFileDownload(), 'w').write('1')
      
      try:
        cur_engine.ProcessTweets(initializedb)
      except Exception as e:
        logger.error('Received exception: %s' % e)
      finally:
        if cur_engine.IsLocking() and os.path.exists(cur_engine.GetLockFileDownload()):
          os.remove(cur_engine.GetLockFileDownload())

def parseargs(name, argv):
  background = False
  initializedb = False
  engine_name = None
  
  try:
    opts, _args = getopt.getopt(argv, 'hbie:', ['background', 'initializedb', 'enginename='])
  except getopt.GetoptError:
    logger.error('%s [-b]' % name)
    sys.exit(2)

  for opt, arg in opts:
    if opt == '-h':
      print '%s [-b] [-i] [-e default]' % name
      sys.exit()
    elif opt in ('-b', '--background'):
      background = True
    elif opt in ('-i', '--initializedb'):
      initializedb = True
    elif opt in ('-e', '--enginename'):
      engine_name = arg

  return (background, initializedb, engine_name)
    
if __name__ == '__main__':
  (background, initializedb, start_name) = parseargs(sys.argv[0], sys.argv[1:])
  cur_engine = Engine(instances.INSTANCES, instances.LANGUAGE)
  cur_engine.run()
