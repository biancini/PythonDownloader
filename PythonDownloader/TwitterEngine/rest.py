__author__ = "Andrea Biancini"
__date__ = "October 2, 2013"

import json
import time
import threading
import logging

from backend import TwitterApiCall, BackendChooser, BackendError
from lastcallbackend import LastcallBackendChooser 

class DownloadTweetsREST(TwitterApiCall):
  bulk = True
  logger = None
  
  def __init__(self, engine_config, language, auth_type):
    super(DownloadTweetsREST, self).__init__(engine_config, language, auth_type)
    self.logger = logging.getLogger('engine-%s' % engine_config['name'])
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
      raise Exception()
    
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
      except BackendError as be:
        self.logger.error('Backend error during insert: %s' % be)
        if max_tweetid is None or min_tweetid is None: raise be
      
    return [inserted, max_tweetid, min_tweetid]
  
  def ManagingCallError(self, jsonresp, last_errcode, ratelimit):
    must_continue = False
    
    if 'statuses' in jsonresp:
      return [must_continue, last_errcode, ratelimit]
    elif 'errors' in jsonresp:
      if type(jsonresp['errors']).__name__ == 'list': errors = jsonresp['errors']
      else: errors = [jsonresp['errors']]
      
      for error in errors:
        if 'code' in error and error['code'] == 88:
          ratelimit = self.GetNextCreds()
          must_continue = True
          return [must_continue, last_errcode, ratelimit]
          
      if error['code'] != last_errcode:
        last_errcode = error['code']
        self.logger.warning('Got error from API, retrying in 5 seconds: %s' % jsonresp)
        time.sleep(5)
        must_continue = True
        return [must_continue, last_errcode, ratelimit]

    self.logger.error('Call did not return expected results: %s' % jsonresp)
    raise Exception()
  
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
    
    statuses = jsonresp['statuses']
    if len(statuses) is 0:
      self.logger.info('API returned no tweet.')
      raise Exception()
  
    if self.bulk: [newins, max_tweetid, min_tweetid] = self.BulkInsert(statuses)
    else: [newins, max_tweetid, min_tweetid] = self.SingleInsert(statuses)

    inserted += newins
      
    if newins != len(statuses):
      self.logger.error("Error inserted %d tweets instead of %d." % (newins, len(statuses)))
      raise Exception()

    return [inserted, max_tweetid, min_tweetid]

  def PartialProcessTweets(self, db_initialization, params, max_id, since_id):
    calls = 0
    inserted = 0
    updateAfterFirstCall = max_id is None
    
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
        else: last_errcode = None
      
        [newinserted, max_tweetid, min_tweetid] = self.ProcessCallResults(jsonresp)
        if updateAfterFirstCall and calls == 0 and max_tweetid is not None:
          self.UpdateLastCallAfterCallExecution(None, None, max_tweetid)

        calls += 1
        inserted += newinserted
        [max_id, since_id] = self.UpdateCallIds(max_id, since_id, max_tweetid, min_tweetid)
        
        if db_initialization:
          self.logger.info('Performing only one call to initialize DB.')
          raise Exception()
        
        if max_id is None: raise Exception()
      except Exception as e:
        if updateAfterFirstCall and calls == 0 and max_id is not None:
          self.UpdateLastCallAfterCallExecution(max_id, max_tweetid, since_id)
          
        self.logger.info('Exiting download cycle %s' % e)
        break

    self.logger.info('Total number of calls executed: \t%d.' % calls)
    self.logger.info('Total number of tweets inserted:\t%d.' % inserted)
    return [max_id, since_id]
  
  def UpdateCallIds(self, max_id, since_id, max_tweetid, min_tweetid):
    if max_tweetid < since_id or max_tweetid == min_tweetid:
      self.logger.info('The call obtained all tweets, stopping the loop.')
      return [None, None]
      
    return [min_tweetid, since_id]

  #def RaescueLastcall(self):
  #  try:
  #    call_ids = self.lastcall_backend.GetLastCallIds(self.engine_name, False)
  #    if len(call_ids) == 0:
  #      self.logger.warn("No lastcall in database, rescuing engine.")
  #      max_tweetid = self.backend.GetMaxId()
  #      self.logger.debug("Found max_tweetid = %s in database." % max_tweetid)
  #      self.lastcall_backend.InsertLastCallIds(self.engine_name, None, max_tweetid)
  #  except Exception as e:
  #    self.logger.error("Error in rescuing last call: %s" % e)

  def RescueLastcall(self, max_id, since_id):
    max_tweetid = max_id if max_id is not None else since_id
    if max_tweetid is None: return

    try:
      call_ids = self.lastcall_backend.GetLastCallIds(self.engine_name, False)
      if len(call_ids) == 0:
        self.logger.warn("No lastcall in database, rescuing engine.")
        self.lastcall_backend.InsertLastCallIds(self.engine_name, None, max_tweetid)
    except Exception as e:
      self.logger.error("Error in rescuing last call: %s" % e)

  def UpdateLastCallAfterCallExecution(self, orig_max_id, max_id, since_id):
    self.logger.info("Updating lastcall with values orig_max_id = %s, max_id = %s, since_id = %s." % (orig_max_id, max_id, since_id))

    if since_id is None and max_id is None:
      self.logger.debug("Performed all calls, no insert in lastcall needed.")
      return
    
    if since_id is None:
      if orig_max_id is None or max_id < orig_max_id: min_max_id = max_id 
      else: min_max_id = orig_max_id
      
      self.lastcall_backend.InsertLastCallIds(self.engine_name, None, min_max_id)
    else:
      if max_id is not None and orig_max_id is not None and max_id > orig_max_id:
        self.logger.warning("Attention, the gap seems to be widening! Old max_id = %s new max_id = %s." % (orig_max_id, max_id))
      self.lastcall_backend.InsertLastCallIds(self.engine_name, max_id, since_id)

  def RunCallEngine(self, params, call_id, db_initialization):
    orig_max_id = None
    # orig_since_id = None

    try:
      max_id = call_id['max_id']
      since_id = call_id['since_id']
      orig_max_id = max_id
      # orig_since_id = since_id
    
      [max_id, since_id] = self.PartialProcessTweets(db_initialization, params, max_id, since_id)
    except Exception as e:
      self.logger.error("Exception during RunCallEngine: %s" % e)
    finally:
      if not db_initialization: self.UpdateLastCallAfterCallExecution(orig_max_id, max_id, since_id)
      #self.RescueLastcall()
      self.RescueLastcall(max_id, since_id)
      
  def ProcessTweets(self, initialize=False):
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
      if initialize:
        first_call = True
        call_ids = [{ 'max_id': None, 'since_id': None}]
      else:
        self.logger.info("Engine colliding with other executions. Exiting.")
        return

    for call_id in call_ids:
      threading.Thread(target=self.RunCallEngine, args=[params, call_id, first_call]).start()
