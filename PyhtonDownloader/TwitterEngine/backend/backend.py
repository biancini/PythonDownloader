__author__ = "Andrea Biancini"
__date__ = "October 2, 2013"

import logging

class BackendChooser(object):
  @staticmethod
  def GetBackend(logger):
    # from mysqlbackend import MySQLBackend
    # backend = MySQLBackend(logger)
    from elasticsearchbackend import ElasticSearchBackend
    backend = ElasticSearchBackend(logger)
    return backend
  
class Backend(object):
  logger = None
  
  def __init__(self, engine_config):
    self.logger = logging.getLogger('engine-%s' % engine_config['name'])
    
  def BulkInsertTweetIntoDb(self, vals):
    raise NotImplementedError
  
  def InsertTweetIntoDb(self, sql_vals):
    raise NotImplementedError

  def DeleteTweetFromDb(self, tweet_id):
    raise NotImplementedError

  def GetKmls(self):
    raise NotImplementedError

  def GetMaxId(self):
    raise NotImplementedError

  def RemoveOldTweets(self, max_date):
    raise NotImplementedError

  def GetAllTweetCoordinates(self):
    raise NotImplementedError

  def UpdateCoordinates(self, location, lat, lng):
    raise NotImplementedError

  def GetLocations(self):
    raise NotImplementedError
  
  def InsertUSAStates(self, vals):
    raise NotImplementedError

  def InsertFrenchDepartments(self, vals):
    raise NotImplementedError

class BackendError(Exception):
    pass
