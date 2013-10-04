__author__ = "Andrea Biancini"
__date__ = "October 2, 2013"

class Backend(object):
  def SelectMaxTweetId(self):
    raise NotImplementedError

  def InsertTweetIntoDb(self, sql_vals):
    raise NotImplementedError

  def GetKmls(self):
    raise NotImplementedError

  def GetLastCallIds(self):
    raise NotImplementedError

  def UpdateLastCallIds(self, max_id = None, since_id = None):
    raise NotImplementedError

  def GetAllTweetCoordinates(self):
    raise NotImplementedError

  def UpdateCoordinates(self, location, lat, lng):
    raise NotImplementedError

  def GetLocations(self):
    raise NotImplementedError

class BackendError(Exception):
    pass
