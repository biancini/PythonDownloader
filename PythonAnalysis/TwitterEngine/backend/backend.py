__author__ = "Andrea Biancini"
__date__ = "October 2, 2013"

class BackendChooser(object):
  @staticmethod
  def GetBackend():
    # from mysqlbackend import MySQLBackend
    # backend = MySQLBackend()
    from elasticsearchbackend import ElasticSearchBackend
    backend = ElasticSearchBackend()
    return backend
  
class Backend(object):
  def GetUSAKmls(self):
    raise NotImplementedError

  def GetFrenchKmls(self):
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
