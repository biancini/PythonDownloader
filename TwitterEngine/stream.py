#!/usr/bin/env python
# -*- coding: utf-8 -*-

import MySQLdb

from datetime import datetime

from twitterapi import TwitterApiCall
from backend import BackendError
from mysqlbackend import MySQLBackend


class DownloadTweetsStream(TwitterApiCall):

  def __init__(self, auth_type):
    super(DownloadTweetsStream, self).__init__(auth_type)
    self.backend = MySQLBackend()

  def ProcessTweets(self):
    squares = ['-5.1,43.1,7.3,50.1'] # Rectangle covering all French territory
    lang    = 'fr'                   # French language
    print 'Executing Twitter API calls '

    params = {'locations':','.join(squares)}
    r = self.api.request('statuses/filter', params)

    for item in r.get_iterator():
      if lang and item['lang'] != lang: continue
      #print item['text']

      sql_vals = self.FromTweetToSQLVals(item, True, True)
      if not sql_vals: continue
      try:
        self.backend.InsertTweetIntoDb(sql_vals)
      except BackendError as be:
        print "Error inserting tweet in the backend: %s" % be
