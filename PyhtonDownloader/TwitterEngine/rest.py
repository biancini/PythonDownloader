__author__ = "Andrea Biancini"
__date__ = "October 2, 2013"

import json
import time

from twitterapi import TwitterApiCall
from backend import BackendChooser, BackendError

class DownloadTweetsREST(TwitterApiCall):
  bulk = True
  
  def __init__(self, engine_config, language, auth_type):
    super(DownloadTweetsREST, self).__init__(engine_config, language, auth_type)
    self.backend = BackendChooser.GetBackend(self.logger)
    self.bulk = engine_config['bulk']
    
  def getMechanism(self):
    return 'rest'

  def GetCurrentLimit(self):
    try:
      limits = self.GetRateLimits()['resources']['search']['/search/tweets']
      return int(limits['remaining'])
    except BackendError as be:
      self.log("Error while retrieving current limit: %s" % be)
      return 0

  def PartialProcessTweets(self, params, top_id, max_id, since_id):
    calls = 0
    callbykey = []
    twits = []

    ratelimit = self.GetCurrentLimit()
    self.log('Executing Twitter API calls ', False)

    cycle = True
    ritorno = [top_id, None, None]

    last_errcode = None

    while cycle and TwitterApiCall.continuing():
      calls += 1
      inserted = 0

      if calls >= ratelimit - 2:
        try:
          self.InitializeTwitterApi()
          callbykey.append(calls)
          calls = 0
          ratelimit = self.GetCurrentLimit()
          self.log("\nUsing another set of credentials because reached limit.")
          continue
        except Exception as e:
          self.log("\nExiting because reached ratelimit.")
          twits.append(inserted)
          ritorno = [top_id, max_id, since_id]
          break

      params['max_id'] = max_id
      params['since_id'] = since_id

      try:
        response = self.api.request('search/tweets', params)
        jsonresp = json.loads(response.text)
      except Exception as e:
        self.log("\nExiting because of an error during API call: %s." % e)
        twits.append(inserted)
        ritorno = [top_id, max_id, since_id]
        break

      if not 'statuses' in jsonresp:
        if 'errors' in jsonresp:
          if type(jsonresp['errors']).__name__ == 'list': errors = jsonresp['errors']
          else: errors = [jsonresp['errors']]
          
          error_88 = False
          reached_limit = False
          for error in errors:
            if 'code' in error and error['code'] == 88:
              try:
                self.InitializeTwitterApi()
                callbykey.append(calls)
                calls = 0
                ratelimit = self.GetCurrentLimit()
                self.log("\nUsing another set of credentials because reached limit.")
                error_88 = True
                break
              except Exception as e:
                self.log("\nExiting because reached ratelimit.")
                break
              
          if error_88:
            if reached_limit: break
            else:
              twits.append(inserted)
              ritorno = [top_id, max_id, since_id]
              reached_limit = True
              continue

          if error['code'] != last_errcode:
            self.log("\nGot error from API, retrying in 5 seconds: %s" % jsonresp)
            time.sleep(5)
            last_errcode = error['code']
            continue

        self.log("\nExiting because call did not return expected results.\n%s" % jsonresp)
        twits.append(inserted)
        ritorno = [top_id, max_id, since_id]
        break

      statuses = jsonresp['statuses']
      if len(statuses) is 0:
        self.log("\nExiting because API returned no tweet.")
        twits.append(inserted)
        ritorno = [top_id, None, None]
        break

      if self.bulk:
        vals = []
        for s in statuses:
          vals.append(self.FromTweetToVals(s, False, False))
  
        try:
          (newins, new_topid) = self.backend.BulkInsertTweetIntoDb(vals)
          inserted += newins
          if top_id is None or top_id < new_topid:
            top_id = new_topid
        except BackendError as be:
          self.log("\nExiting as requested by backend: %s" % be)
          twits.append(inserted)
          ritorno = [top_id, max_id, since_id]
          cycle = False
      else:
        for s in statuses:
          vals = self.FromTweetToVals(s, False, False)
  
          try:
            newins = self.backend.InsertTweetIntoDb(vals)
            inserted += newins
            if top_id is None or top_id < long(vals['id']):
              top_id = long(vals['id'])
          except BackendError as be:
            self.log("\nExiting as requested by backend: %s" % be)
            twits.append(inserted)
            ritorno = [top_id, max_id, since_id]
            cycle = False
            break

      self.log('.', False)
      if since_id is None:
        self.log("\nExiting because performing only one call to initialize DB.")
        twits.append(inserted)
        ritorno = [top_id, None, None]
        break

      twits.append(inserted)
      # self.log("Numer of tweets inserted:\t%d." % inserted)
      max_id = min([s['id'] for s in statuses]) - 1

    callbykey.append(calls)
    self.log("Total number of calls executed: \t%d." % sum(callbykey))
    self.log("Total number of tweets inserted:\t%d." % sum(twits))
    return ritorno

  def ProcessTweets(self):
    lat = self.filters[0]
    lng = self.filters[1]
    radius = self.filters[2]
    count = 100  # Number of tweets to retrieve (max. 100)

    max_ids = [None, None]
    since_ids = [None, None]
    top_id = None

    try:
      ret = self.backend.GetLastCallIds(self.engine_name)
      max_ids[0] = ret[0]
      since_ids[0] = ret[1]

      top_id = ret[2]
      max_ids[1] = None
      since_ids[1] = top_id
    except BackendError as be:
      self.log("Error while checking last call state: %s" % be)

    params = { 'geocode':     ','.join(map(str, (lat, lng, radius))),
               'count':       count,
               'lang':        self.language,
               'result_type': 'recent',
               'max_id':      None,
               'since_id':    None }

    if max_ids[0] is not None and since_ids[0] is not None:
      self.log("Executing set of calls to fill previously unfilled gap...")
      self.log("Executing call with max_id = %s and since_id = %s" % (max_ids[0], since_ids[0]))
      ret = self.PartialProcessTweets(params, top_id, max_ids[0], since_ids[0])
      self.backend.UpdateLastCallIds(self.engine_name, ret[0], ret[1], ret[2])
      if ret[0] is not None and ret[1] is not None:
        self.log("Error with the fill-the-gaps mechanisms.")
        return

    self.log("Executing call with max_id = %s and since_id = %s" % (max_ids[1], since_ids[1]))
    ret = self.PartialProcessTweets(params, top_id, max_ids[1], since_ids[1])
    self.backend.UpdateLastCallIds(self.engine_name, ret[0], ret[1], ret[2])

  def AggregateByPerson(self, date):
    tweeters = self.backend.GetAllTweetByPerson(date, date)

    for tweeter in tweeters.items():
      tweets = self.backend.GetAllTweetsForUserId(tweeter[0], tweeter[1], date, date)
      byperson = self.analyzer.AggretateTweetsByPerson(tweets)
      self.backend.InsertByPersonData(byperson)

