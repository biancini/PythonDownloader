__author__ = "Andrea Biancini"
__date__ = "October 2, 2013"

import MySQLdb
import pprint
import sys
import json

from datetime import datetime

from twitterapi import TwitterApiCall
from backend import BackendError
from mysqlbackend import MySQLBackend


class DownloadTweetsREST(TwitterApiCall): 

  def __init__(self, auth_type):
    super(DownloadTweetsREST, self).__init__(auth_type)
    self.backend = MySQLBackend()

  def GetCurrentLimit(self):
    limits = self.GetRateLimits()['resources']['search']['/search/tweets']
    return int(limits['remaining'])

  def PartialProcessTweets(self, params, max_id, since_id):
    calls     = 0
    twits     = []

    ratelimit = self.GetCurrentLimit()

    sys.stdout.write('Executing Twitter API calls ')
    sys.stdout.flush()

    while True:
      calls += 1
      inserted = 0

      if calls >= ratelimit - 2:
        try:
          self.InitializeTwitterApi()
          calls = 0
          ratelimit = self.GetCurrentLimit()
        except Exception as e:
          print "Exiting because reached ratelimit."
          return [max_id, since_id]

      params['max_id']   = max_id
      params['since_id'] = since_id

      response = self.api.request('search/tweets', params)
      jsonresp = json.loads(response.text)
      if not 'statuses' in jsonresp:
        print "Exiting because call did not return expected results.\n%s" % jsonresp
        return [max_id, since_id]

      statuses = jsonresp['statuses']
      if (len(statuses) == 0):
        print "Exiting because API returned no tweet."
        return [None, None]

      for s in statuses:
        sql_vals = self.FromTweetToSQLVals(s, False, False)
        if not sql_vals:
          break

        try:
          newins = self.backend.InsertTweetIntoDb(sql_vals)
          inserted += newins
        except BackendError as be:
          print "Exiting as requested by backend: %s" % be
          return [None, None]

      sys.stdout.write('.')
      sys.stdout.flush()
      if (since_id is None):
        print "Exiting because performing only one call to initialize DB."
        return [max_id, since_id]

      twits.append(inserted)
      #print "Numer of tweets inserted:\t%d." % inserted
      max_id = min([s['id'] for s in statuses]) - 1

    return [None, None]

  def ProcessTweets(self):
    lat    = 45.776665 # Latitude and longitude of Clermont-Ferrand
    lng    = 3.07723
    radius = '400km'   # Radius of France territory
    count  = 100       # Number of tweets to retrieve (max. 100)
    lang   = 'fr'      # French language

    max_ids   = [None, None]
    since_ids = [None, None]

    try:
      ret = self.backend.GetLastCallIds()
      max_ids[0]   = ret[0]
      since_ids[0] = ret[1]
    except BackendError as be:
      print "Error while checking last call state."

    max_ids[1] = None
    try:
      since_ids[1] = self.backend.SelectMaxTweetId()
    except BackendError as be:
      since_ids[1] = None

    params = { 'geocode':     ','.join(map(str, (lat, lng, radius))),
               'count':       count,
               'lang':        lang,
               'result_type': 'recent',
               'max_id':      None,
               'since_id':    None }

    if max_ids[0] is not None and since_ids[0] is not None:
      print "Executing set of calls to fill previously unfilled gap..."
      ret = self.PartialProcessTweets(params, max_ids[0], since_ids[0])
      if ret[0] is not None and ret[1] is not None:
        print "Error with the fill-the-gaps mechanisms."
        return

    print "Executing call with max_id = %s and since_id = %s" % (max_ids[1], since_ids[1])
    ret = self.PartialProcessTweets(params, max_ids[1], since_ids[1])
    self.backend.UpdateLastCallIds(ret[0], ret[1])
