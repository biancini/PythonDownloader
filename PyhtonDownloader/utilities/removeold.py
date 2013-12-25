#!/usr/bin/env python
# -*- coding: utf-8 -*-

import sys
import getopt
import os.path
sys.path.append(os.path.abspath(os.path.join(os.path.dirname(__file__), os.path.pardir)))

from datetime import datetime
from TwitterEngine import instances, BackendChooser

def parseargs(name, argv):
  date = datetime.now()
  execute = False

  try:
    opts, _args = getopt.getopt(argv, 'hed:', ['execute', 'date'])
  except getopt.GetoptError:
    print('%s [-h]' % name)
    sys.exit(2)

  for opt, arg in opts:
    if opt == '-h':
      print '%s [-d "YYYY-MM-DD [HH:mm:SS]"]' % name
      sys.exit()
    elif opt in ('-e', '--execute'):
      execute = True
    elif opt in ('-d', '--date'):
      try:
        if len(arg) > 10:
          date = datetime.strptime(arg, '%Y-%m-%d %H:%M:%S')
        else:
          date = datetime.strptime(arg, '%Y-%m-%d')
      except ValueError as e:
        print "Date format accepted: YYYY-MM-DD [HH:mm:SS]"
        raise e

  return (date, execute)

if __name__ == '__main__':
  try:
    engine_config = instances.INSTANCES[0]
    (max_date, execute) = parseargs(sys.argv[0], sys.argv[1:])
  except ValueError:
    sys.exit(1)

  backend = BackendChooser.GetBackend(engine_config)
  print "Calling delete with parameters max_date = %s, execute = %s." % (max_date, execute)
  backend.RemoveOldTweets(max_date, execute)
