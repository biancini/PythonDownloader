#!/usr/bin/env python
# -*- coding: utf-8 -*-

import MySQLdb
import pprint
import sys
import json

from datetime import datetime

from twitterapi import TwitterApiCall
from dbbackend import DatabaseBackend


class DownloadTweetsREST(TwitterApiCall): 

  def __init__(self, auth_type):
    super(DownloadTweetsREST, self).__init__(auth_type)
    self.backend = DatabaseBackend()

  def GetCurrentLimit(self):
    limits = self.GetRateLimits()['resources']['search']['/search/tweets']
    return int(limits['remaining'])

  def ProcessTweets(self):
    lat    = 45.776665 # Latitude and longitude of Clermont-Ferrand
    lng    = 3.07723
    radius = '400km'   # Radius of France territory
    count  = 100       # Number of tweets to retrieve (max. 100)
    lang   = 'fr'      # French language

    calls     = 0
    twits     = []
    ratelimit = self.GetCurrentLimit()
    max_id    = None
    since_id  = None
    since_id = self.backend.SelectMaxTweetId()

    sys.stdout.write('Executing Twitter API calls ')
    sys.stdout.flush()

    while True:
      calls += 1
      inserted = 0
      if calls >= ratelimit - 2:
        print "Exiting because reached ratelimit."
        break

      params = { 'geocode':     ','.join(map(str, (lat, lng, radius))),
                 'count':       count,
                 'lang':        lang,
                 'result_type': 'recent',
                 'max_id':      max_id,
                 'since_id':    since_id }
      response = self.api.request('search/tweets', params)
      jsonresp = json.loads(response.text)
      if not 'statuses' in jsonresp:
        print "Exiting because call did not return expected results.\n%s" % jsonresp
        break

      statuses = jsonresp['statuses']
      if (len(statuses) == 0):
        print "Exiting because API returned no tweet."
        break
      #print "Number of tweets downloaded:\t%d." % len(statuses)

      #pp = pprint.PrettyPrinter(depth=6)
      #pp.pprint(statuses[0])
      #break

      for s in statuses:
	#print s
        sql_vals = self.FromTweetToSQLVals(s, True, True)
        if not sql_vals:
          print "Exiting because point in not within any valid KML region."
          break

        cycle, newins = self.backend.InsertTweetIntoDb(sql_vals)
        inserted += newins

        if not cycle:
          print "Exiting as requested by backend."
          break

      sys.stdout.write('.')
      sys.stdout.flush()
      if (since_id is None):
        print "Exiting because performing only one call to initialize DB."
        break

      twits.append(inserted)
      #print "Numer of tweets inserted:\t%d." % inserted
      max_id = min([s['id'] for s in statuses]) - 1

    print "\nExecuted %d calls to insert a total number of %d tweets." % (calls, sum(twits))
    #for i in range(0, len(twits)):
    #  print "Call %d inserted %d tweets." % (i+1, twits[i])

