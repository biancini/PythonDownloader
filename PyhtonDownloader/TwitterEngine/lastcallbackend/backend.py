__author__ = "Andrea Biancini"
__date__ = "October 2, 2013"

import logging

class LastcallBackendChooser(object):
  @staticmethod
  def GetBackend(engine_config):
    # from mysqlbackend import MySQLBackend
    # backend = MySQLBackend(engine_config)
    from sqllitebackend import SQLiteBackend
    backend = SQLiteBackend(engine_config)
    return backend
  
class Backend(object):
  logger = None
  
  def __init__(self, engine_config):
    self.logger = logging.getLogger('engine-%s' % engine_config['name'])
  
  def GetLastCallIds(self, engine_name, pop=False):
    raise NotImplementedError
  
  def DeleteLastCallId(self, engine_name, lastcall_id):
    raise NotImplementedError

  def InsertLastCallIds(self, engine_name, max_id=None, since_id=None):
    raise NotImplementedError

class LastcallBackendError(Exception):
    pass
