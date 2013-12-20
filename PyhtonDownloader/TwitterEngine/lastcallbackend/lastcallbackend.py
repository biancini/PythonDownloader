__author__ = "Andrea Biancini"
__date__ = "October 2, 2013"

import logging

class BackendChooser(object):
  @staticmethod
  def GetBackend(engine_config):
    # from mysqlbackend import MySQLBackend
    # backend = MySQLBackend()
    from elasticsearchbackend import ElasticSearchBackend
    backend = ElasticSearchBackend(engine_config)
    return backend
  
class Backend(object):
  logger = None
  
  def __init__(self, engine_config):
    self.logger = logging.getLogger('engine-%s' % engine_config['name'])
  
  def BulkInsertTweetIntoDb(self, vals):
    raise NotImplementedError
  
  def InsertTweetIntoDb(self, sql_vals):
    raise NotImplementedError

  def GetKmls(self):
    raise NotImplementedError

  def GetLastCallIds(self):
    raise NotImplementedError

  def UpdateLastCallIds(self, max_id=None, since_id=None):
    raise NotImplementedError

  def GetAllTweetCoordinates(self):
    raise NotImplementedError

  def UpdateCoordinates(self, location, lat, lng):
    raise NotImplementedError

  def GetLocations(self):
    raise NotImplementedError

  def InsertFrenchDepartments(self, vals):
    raise NotImplementedError

class BackendError(Exception):
    pass
