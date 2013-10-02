__author__ = "Andrea Biancini"
__date__ = "October 2, 2013"

class Backend(object):
  def SelectMaxTweetId(self):
    raise NotImplementedError

  def InsertTweetIntoDb(self, sql_vals):
    raise NotImplementedError

  def GetKmls(self):
    raise NotImplementedError

class BackendError(Exception):
    pass
