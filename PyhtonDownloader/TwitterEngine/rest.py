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
      self.log('Error while retrieving current limit: %s' % be)
      return 0
    
  def GetNextCreds(self, ratelimit=0):
    try:
      while ratelimit <= 2:
        self.InitializeTwitterApi()
        ratelimit = self.GetCurrentLimit()
        self.log('\nUsing another set of credentials because reached limit.')
      
      return ratelimit
    except Exception as e:
      self.log('\Reached ratelimit.')
      raise e
    
  def BulkInsert(self, statuses):
    vals = []
    for s in statuses:
      vals.append(self.FromTweetToVals(s, False, False))

    try:
      [inserted, max_tweetid, min_tweetid] = self.backend.BulkInsertTweetIntoDb(vals)
      return [inserted, max_tweetid, min_tweetid]
    except BackendError as be:
      self.log('\nBackend error during bulk insert: %s' % be)
      return [0, None, None]
    
  def SingleInsert(self, statuses):
    max_tweetid = None
    min_tweetid = None
    inserted = 0
    
    for s in statuses:
      vals = self.FromTweetToVals(s, False, False)
  
      try:
        newins = self.backend.InsertTweetIntoDb(vals)
        inserted += newins

        if max_tweetid is None or max_tweetid < long(vals['id']):
            max_tweetid = long(vals['id'])
        if min_tweetid is None or min_tweetid > long(vals['id']):
          min_tweetid = long(vals['id'])

        return [inserted, max_tweetid, min_tweetid]
      except BackendError as be:
        self.log('\nBackend error during insert: %s' % be)
        return [inserted, max_tweetid, min_tweetid]
  
  def ManagingCallError(self, jsonresp, last_errcode, ratelimit):
    must_continue = False
    if 'errors' in jsonresp:
      if type(jsonresp['errors']).__name__ == 'list': errors = jsonresp['errors']
      else: errors = [jsonresp['errors']]
      
      for error in errors:
        if 'code' in error and error['code'] == 88:
          ratelimit = self.GetNextCreds()
          break
          
      if error['code'] != last_errcode:
        self.log('\nGot error from API, retrying in 5 seconds: %s' % jsonresp)
        time.sleep(5)
        must_continue = True
      else:
        self.log('\nCall did not return expected results.\n%s' % jsonresp)
        raise Exception()

    return [must_continue, last_errcode, ratelimit]
  
  def ExecuteCall(self, params, max_id, since_id):
    try:
      params['max_id'] = max_id
      params['since_id'] = since_id
      
      response = self.api.request('search/tweets', params)
      ratelimit = response.headers['x-rate-limit-remaining']
      jsonresp = json.loads(response.text)
      
      return [ratelimit, jsonresp]
    except Exception as e:
      self.log('\nError during API call: %s.' % e)
      raise e
    
  def ProcessCallResults(self, jsonresp):
    max_tweetid = None
    min_tweetid = None
    inserted = 0
    
    if 'statuses' in jsonresp:
      statuses = jsonresp['statuses']
      
      if len(statuses) is 0:
        self.log('\nAPI returned no tweet.')
        return [inserted, None, None]
  
      if self.bulk: [newins, max_tweetid, min_tweetid] = self.BulkInsert(statuses)
      else: [newins, max_tweetid, min_tweetid] = self.SingleInsert(statuses)

      inserted += newins
      self.log('.', False)
      
      if newins != len(statuses): raise Exception()

    return [inserted, max_tweetid, min_tweetid]

  def PartialProcessTweets(self, params, max_id, since_id):
    calls = 0
    inserted = 0
    
    ratelimit = self.GetCurrentLimit()
    last_errcode = None
    
    self.log('Executing Twitter API calls ', False)

    while TwitterApiCall.continuing():
      try:
        if ratelimit <= 2: ratelimit = self.GetNextCreds(ratelimit)
        [ratelimit, jsonresp] = self.ExecuteCall(params, max_id, since_id)
        
        [must_continue, last_errcode, ratelimit] = self.ManagingCallError(jsonresp, last_errcode, ratelimit)
        if must_continue: continue
        
        [newinserted, max_tweetid, min_tweetid] = self.ProcessCallResults(jsonresp)
        if calls == 0: self.backend.InsertLastCallIds(self.engine_name, None, max_tweetid)
        max_id = min_tweetid
        if since_id is None:
          self.log('\nPerforming only one call to initialize DB.')
          break
        
        calls += 1
        inserted += newinserted
        if max_id is None: raise Exception()
      except Exception as e:
        self.log('Exiting download cycle: %s' % e)
        break

    self.log('Total number of calls executed: \t%d.' % calls)
    self.log('Total number of tweets inserted:\t%d.' % inserted)
    return [max_id, since_id]

  def ProcessTweets(self):
    lat = self.filters[0]
    lng = self.filters[1]
    radius = self.filters[2]
    count = 100  # Number of tweets to retrieve (max. 100)

    try:
      call_ids = self.backend.GetLastCallIds(self.engine_name)
      self.log("Obtained call_ids = %s" % call_ids)
    except BackendError as be:
      self.log('Error while checking last call state: %s' % be)

    params = { 'geocode':     ','.join(map(str, (lat, lng, radius))),
               'count':       count,
               'lang':        self.language,
               'result_type': 'recent',
               'max_id':      None,
               'since_id':    None }

    if len(call_ids) == 0:
      self.log('Executing call with max_id = %s and since_id = %s' % (None, None))      
      [max_id, since_id] = self.PartialProcessTweets(params, None, None)
    else:
      # TODO multithread executing for each call id to add parallelism
      for call_id in call_ids:
        self.log('Executing call with max_id = %s and since_id = %s' % (call_id['max_id'], call_id['since_id']))
        self.backend.DeleteLastCallId(self.engine_name, call_id['id'])
        
        [max_id, since_id] = self.PartialProcessTweets(params, call_id['max_id'], call_id['since_id'])
        if max_id is not None:
          self.backend.InsertLastCallIds(self.engine_name, max_id, since_id)

  def AggregateByPerson(self, date):
    tweeters = self.backend.GetAllTweetByPerson(date, date)

    for tweeter in tweeters.items():
      tweets = self.backend.GetAllTweetsForUserId(tweeter[0], tweeter[1], date, date)
      byperson = self.analyzer.AggretateTweetsByPerson(tweets)
      self.backend.InsertByPersonData(byperson)
