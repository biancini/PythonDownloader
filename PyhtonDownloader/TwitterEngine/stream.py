__author__ = "Andrea Biancini"
__date__ = "October 2, 2013"

from twitterapi import TwitterApiCall
from backend import BackendChooser, BackendError


class DownloadTweetsStream(TwitterApiCall):

  def __init__(self, engine_name, auth_type):
    super(DownloadTweetsStream, self).__init__(engine_name, auth_type)
    self.backend = BackendChooser.GetBackend()

  def ProcessTweets(self):
    squares = ['-5.1,43.1,7.3,50.1']  # Rectangle covering all French territory
    lang = 'fr'  # French language
    print 'Executing Twitter API calls '

    params = {'locations':','.join(squares)}
    r = self.api.request('statuses/filter', params)

    for item in r.get_iterator():
      if lang and item['lang'] != lang: continue
      # print item['text']

      sql_vals = self.FromTweetToSQLVals(item, True, True)
      if not sql_vals: continue
      try:
        self.backend.InsertTweetIntoDb(sql_vals)
      except BackendError as be:
        print "Error inserting tweet in the backend: %s" % be
