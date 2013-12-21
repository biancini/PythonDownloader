__author__ = "Andrea Biancini"
__date__ = "October 2, 2013"

import json
import time
import threading

from backend import TwitterApiCall, BackendChooser, BackendError
from lastcallbackend import LastcallBackendChooser 

class DownloadTweetsREST(TwitterApiCall):
  bulk = True
  
  def __init__(self, engine_config, language, auth_type):
    super(DownloadTweetsREST, self).__init__(engine_config, language, auth_type)
    self.backend = BackendChooser.GetBackend(engine_config)
    self.lastcall_backend = LastcallBackendChooser.GetBackend(engine_config)
    self.bulk = engine_config['bulk']
    
  def getMechanism(self):
    return 'rest'

  def GetCurrentLimit(self):
    try:
      limits = self.GetRateLimits()['resources']['search']['/search/tweets']
      return int(limits['remaining'])
    except BackendError as be:
      self.logger.error('Error while retrieving current limit: %s' % be)
      return 0
    
  def GetNextCreds(self, ratelimit=0):
    try:
      while ratelimit <= 2:
        self.logger.info('Using another set of credentials because reached limit.')
        self.InitializeTwitterApi()
        ratelimit = self.GetCurrentLimit()
        self.logger.debug('New limit for this set of credentials: %d' % ratelimit)
      
      return ratelimit
    except Exception as e:
      self.logger.error('Reached ratelimit.')
      raise e
    
  def BulkInsert(self, statuses):
    vals = []
    for s in statuses:
      vals.append(self.FromTweetToVals(s, False, False))

    try:
      return self.backend.BulkInsertTweetIntoDb(vals)
    except BackendError as be:
      self.logger.error('Backend error during bulk insert: %s' % be)
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
        self.logger.error('Backend error during insert: %s' % be)
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
        last_errcode = error['code']
        self.logger.warning('Got error from API, retrying in 5 seconds: %s' % jsonresp)
        time.sleep(5)
        must_continue = True
      else:
        self.logger.error('Call did not return expected results: %s' % jsonresp)
        raise Exception()

    return [must_continue, last_errcode, ratelimit]
  
  def ExecuteCall(self, params, max_id, since_id):
    params['max_id'] = max_id
    params['since_id'] = since_id
    
    if max_id is not None and since_id is not None:
      if max_id < since_id:
        raise Exception("Wrong max and min id")

    try:
      response = self.api.request('search/tweets', params)
      ratelimit = response.headers['x-rate-limit-remaining']
      jsonresp = json.loads(response.text)
      
      return [ratelimit, jsonresp]
    except Exception as e:
      self.logger.error('Error during API call: %s.' % e)
      raise e
    
  def ProcessCallResults(self, jsonresp):
    max_tweetid = None
    min_tweetid = None
    inserted = 0
    
    if 'statuses' in jsonresp:
      statuses = jsonresp['statuses']
      
      if len(statuses) is 0:
        self.logger.info('API returned no tweet.')
        return [inserted, None, None]
  
      if self.bulk: [newins, max_tweetid, min_tweetid] = self.BulkInsert(statuses)
      else: [newins, max_tweetid, min_tweetid] = self.SingleInsert(statuses)

      inserted += newins
      
      if newins != len(statuses):
        self.logger.error("Error inserted %d tweets instead of %d." % (newins, len(statuses)))
        raise Exception()
    else:
      self.logger.error('Unexpected call result: %s' % jsonresp)

    return [inserted, max_tweetid, min_tweetid]

  def PartialProcessTweets(self, params, max_id, since_id):
    calls = 0
    inserted = 0
    isGap = max_id is not None
    
    ratelimit = self.GetCurrentLimit()
    last_errcode = None
    
    self.logger.info('Executing Twitter API calls with max_id = %s and since_id = %s' % (max_id, since_id))

    max_tweetid = since_id
    while TwitterApiCall.continuing():
      try:
        if ratelimit <= 2: ratelimit = self.GetNextCreds(ratelimit)
        [ratelimit, jsonresp] = self.ExecuteCall(params, max_id, since_id)
        
        [must_continue, last_errcode, ratelimit] = self.ManagingCallError(jsonresp, last_errcode, ratelimit)
        if must_continue: continue
      
        [newinserted, max_tweetid, min_tweetid] = self.ProcessCallResults(jsonresp)
        if not isGap and calls == 0 and max_tweetid is not None:
          self.lastcall_backend.InsertLastCallIds(self.engine_name, None, max_tweetid)
        
        if min_tweetid is not None:
          calls += 1
          inserted += newinserted
          if max_tweetid < since_id or max_tweetid == min_tweetid:
            max_id = None
            raise Exception()
          else:
            max_id = min_tweetid
        else:
          max_id = None
          since_id = None
          raise Exception()
        
        if since_id is None:
          self.logger.info('Performing only one call to initialize DB.')
          raise Exception()
        
        if max_id is None: raise Exception()
      except Exception as e:
        self.logger.info('Exiting download cycle %s' % e)
        break

    self.logger.info('Total number of calls executed: \t%d.' % calls)
    self.logger.info('Total number of tweets inserted:\t%d.' % inserted)
    return [max_id, since_id]

  def rescue_lastcall(self, max_tweetid):
    if max_tweetid is None: return
    self.logger.warn("No lastcall in database, rescuing engine.")
    try:
      call_ids = self.lastcall_backend.GetLastCallIds(self.engine_name, False)
      if len(call_ids) == 0:
        self.lastcall_backend.InsertLastCallIds(self.engine_name, None, max_tweetid)
    except Exception as e:
      self.logger.error("Error in rescuing last call: %s" % e)

  def runcall(self, params, call_id, db_initialization):
    # if not db_initialization:
    #  self.backend.DeleteLastCallId(self.engine_name, call_id['id'])
    
    try:
      max_id = call_id['max_id']
      since_id = call_id['since_id']
    
      [max_id, since_id] = self.PartialProcessTweets(params, max_id, since_id)
    except Exception as e:
      self.logger.error("Exception during runcall: %s" % e)
    finally:
      if not db_initialization:
        isGap = max_id is not None
        if isGap: self.lastcall_backend.InsertLastCallIds(self.engine_name, max_id, since_id)
        else: self.lastcall_backend.InsertLastCallIds(self.engine_name, None, max_id)
      self.rescue_lastcall(max_id)
      
  def ProcessTweets(self):
    try:
      call_ids = self.lastcall_backend.GetLastCallIds(self.engine_name, True)
      self.logger.debug('Obtained call_ids = %s' % call_ids)
    except Exception as be:
      self.logger.error('Error while checking last call state: %s' % be)

    lat = self.filters[0]
    lng = self.filters[1]
    radius = self.filters[2]
    count = 100  # Number of tweets to retrieve (max. 100)

    params = { 'geocode':     ','.join(map(str, (lat, lng, radius))),
               'count':       count,
               'lang':        self.language,
               'result_type': 'recent',
               'max_id':      None,
               'since_id':    None }

    first_call = False
    if len(call_ids) == 0:
      first_call = True
      call_ids = [{ 'max_id': None, 'since_id': None}]

    for call_id in call_ids:
      threading.Thread(target=self.runcall, args=[params, call_id, first_call]).start()
