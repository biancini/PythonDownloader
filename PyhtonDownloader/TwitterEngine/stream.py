__author__ = "Andrea Biancini"
__date__ = "October 2, 2013"

from twitterapi import TwitterApiCall
from backend import BackendChooser, BackendError


class DownloadTweetsStream(TwitterApiCall):

  def __init__(self, engine_config, language, auth_type):
    super(DownloadTweetsStream, self).__init__(engine_config, language, auth_type)
    self.backend = BackendChooser.GetBackend(self.logger)
    
  def getMechanism(self):
    return 'stream'

  def ProcessTweets(self):
    squares = self.filters
    lang = self.language
    self.log('Executing Twitter API calls')

    params = {'locations':','.join(squares)}
    r = self.api.request('statuses/filter', params)

    for item in r.get_iterator():
      if lang and item['lang'] != lang: continue
      # self.log(item['text'])

      sql_vals = self.FromTweetToSQLVals(item, True, True)
      if not sql_vals: continue
      try:
        self.backend.InsertTweetIntoDb(sql_vals)
      except BackendError as be:
        self.log("Error inserting tweet in the backend: %s" % be)
