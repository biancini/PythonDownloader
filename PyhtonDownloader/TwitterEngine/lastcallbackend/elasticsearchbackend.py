__author__ = "Andrea Biancini"
__date__ = "October 2, 2013"

import requests
import json
import threading
import sqlite3

from datetime import datetime

from backend import Backend, BackendError
from secrets import es_server

def synchronized(func):
  func.__lock__ = threading.Lock()

  def synced_func(*args, **kws):
    with func.__lock__:
      return func(*args, **kws)

  return synced_func

class ElasticSearchBackend(Backend):

  def __init__(self, engine_config):
    super(ElasticSearchBackend, self).__init__(engine_config)
  
  def BulkInsertTweetIntoDb(self, vals):
    max_tweetid = None
    min_tweetid = None
    inserted = 0
    
    if vals is None or len(vals) == 0:
      self.logger.warning("Asked to bulk insert 0 tweets.")
      return [inserted, max_tweetid, min_tweetid]
    
    try:
      bulk_uploads = ""
      
      for val in vals:
        bulk_uploads += '{ "index" : { "_index" : "twitter", "_type" : "tweets", "_id" : "%s" } }\n' % val['id']
        
        data = {}
        data['created_at'] = val['created_at']
        data['text'] = val['text']
        data['userid'] = val['userid']
        # data['hashtags'] = val['hashtags']
        data['location'] = val['location']
        data['num_friends'] = val['num_friends']
        # data['happiness'] = val['happiness']
        # data['relevance'] = val['relevance']
  
        if val['latitude'] == 'NULL' or val['longitude'] == 'NULL':
          data['coordinates'] = None
        else:
          data['coordinates'] = "%s,%s" % (val['latitude'], val['longitude'])
        
        bulk_uploads += json.dumps(data) + "\n"
      
      host = "%s/_bulk" % es_server
      req = requests.put(host, data=bulk_uploads)
      ret = json.loads(req.content)
      
      for item in ret["items"]:
        if item["index"]["ok"]:
          inserted += 1
          if max_tweetid is None or max_tweetid < long(item["index"]['_id']):
            max_tweetid = long(item["index"]['_id'])
          if min_tweetid is None or min_tweetid > long(item["index"]['_id']):
            min_tweetid = long(item["index"]['_id'])
        else:
          self.logger.warning("Bulk insert not ok for tweet: " % item["index"]["_id"])
          
      return [inserted, max_tweetid, min_tweetid]
    except Exception as e:
      self.logger.error("Exception while bulk inserting tweets: %s" % e)
      return [inserted, max_tweetid, min_tweetid]
    
  def InsertTweetIntoDb(self, vals):
    try:
      if vals is None: return 0

      host = "%s/twitter/tweets/%s" % (es_server, vals['id'])
      present = requests.head(host)
      if int(present.status_code) != 404:
        self.logger.debug("HEAD returned %s" % present.status_code)
        return 0

      data = {}
      data['created_at'] = vals['created_at']
      data['text'] = vals['text']
      data['userid'] = vals['userid']
      # data['hashtags'] = vals['hashtags']
      data['location'] = vals['location']
      data['num_friends'] = vals['num_friends']
      # data['happiness'] = vals['happiness']
      # data['relevance'] = vals['relevance']

      if vals['latitude'] == 'NULL' or vals['longitude'] == 'NULL':
        data['coordinates'] = None
      else:
        data['coordinates'] = "%s,%s" % (vals['latitude'], vals['longitude'])
    
      data_json = json.dumps(data, indent=2)
      req = requests.put(host, data=data_json)
      ret = json.loads(req.content)

      if not ret["ok"]: raise BackendError("Insert not ok")
      if ret["_version"] > 1: raise BackendError("Tweet already present in the DB.")
      return 1
    except Exception as e:
      self.logger.error("Exception while inserting tweet %s: %s" % (vals['id'], e))
      return 0

  def GetKmls(self):
    self.logger.info("Retrieving all French departments")
    try:
      start = 0
      pagesize = 10
      last = None

      rows = []
      while True:
        data = { 'query' : { 'match_all' : { } },
                 'from' : start,
                 'size' : pagesize }
        data_json = json.dumps(data, indent=2)
        host = "%s/twitter/french_depts/_search" % es_server
        req = requests.get(host, data=data_json)
        ret = json.loads(req.content)

        for hit in ret['hits']['hits']:
          curhit = []
          if 'NOM_REG' in hit['_source'] and 'KML' in hit['_source']:
            curhit.append(hit['_source']['NOM_REG'].replace('\\\'', '\''))
            curhit.append(hit['_source']['KML'].replace('\\\'', '\''))
            rows.append(curhit)

        last = ret['hits']['total']
        start += pagesize
        if start > last: break

      return rows
    except Exception as e:
      raise BackendError("Error while retrieving kmls from ElasticSearch: %s" % e)
  
  def _GetLastCallDb(self):
    db = sqlite3.connect('/usr/share/twitter/lastcall.db')
    cursor = db.cursor()
    cursor.execute('''SELECT name FROM sqlite_master WHERE type='table' AND name='lastcall';''')

    if cursor.fetchone() is None:
      cursor.execute('''CREATE TABLE lastcall (id INTEGER PRIMARY KEY AUTOINCREMENT, engine_name TEXT, max_id LONG, since_id LONG NOT NULL, UNIQUE (engine_name, max_id, since_id) ON CONFLICT REPLACE)''')
      db.commit()

    return db

  @synchronized
  def GetLastCallIds(self, engine_name, pop=False):
    db = None
    cursor = None
    try:
      db = self._GetLastCallDb()
      cursor = db.cursor()
      cursor.execute('''SELECT id, max_id, since_id FROM lastcall WHERE engine_name = ?;''', (engine_name,))
      all_rows = cursor.fetchall()

      ids = []
      for row in all_rows:
        new_id = {}
        new_id['id'] = row[0]
        new_id['max_id'] = row[1]
        new_id['since_id'] = row[2]
        ids.append(new_id)
        
        if pop: self.DeleteLastCallId(engine_name, row[0])

      db.commit()
      return ids
    except Exception as e:
      if cursor is not None: db.rollback()
      raise BackendError("Error while retrieving last call ids from SQLite: %s" % e)
    finally:
      if db is not None: db.close()
  
  @synchronized
  def DeleteLastCallId(self, engine_name, lastcall_id):
    self.logger.info("Deleting lastcall id = %s." % (lastcall_id))

    db = None
    cursor = None
    try:
      db = self._GetLastCallDb()
      cursor = db.cursor()
      cursor.execute('''DELETE FROM lastcall WHERE engine_name = ? AND id = ?''', (engine_name, lastcall_id))
      db.commit()
    except Exception as e:
      if cursor is not None: db.rollback()
      raise BackendError("Error while deleting lastcall id %s from SQLite: %s" % (lastcall_id, e))    
    finally:
      if db is not None: db.close()

  @synchronized
  def InsertLastCallIds(self, engine_name, max_id=None, since_id=None):
    if since_id is None:
      self.logger.error("Wrong parameters in lastcall update, since_id cannot be None.")
      return

    self.logger.info("Updating lastcall with values max_id = %s and since_id = %s." % (max_id, since_id))

    db = None
    cursor = None
    try:
      db = self._GetLastCallDb()
      cursor = db.cursor()
      
      cursor.execute('''SELECT MAX(since_id) FROM lastcall WHERE engine_name = ? AND max_id IS NULL;''', (engine_name,))
      row = cursor.fetchone()
      if max_id is not None or row[0] is None:
        self.logger.debug("Inserting lastcall since no clashing lastcall already present into DB")
        cursor.execute('''INSERT INTO lastcall (engine_name, max_id, since_id) VALUES (?, ?, ?);''', (engine_name, max_id, since_id))
        db.commit()
      elif row[0] < since_id:
        self.logger.debug("Updating lastcall overwriting clashing lastcall already present into DB")
        cursor.execute('''UPDATE lastcall SET since_id = ? WHERE engine_name = ? AND since_id = ? AND max_id IS NULL;''', (since_id, engine_name, row[0]))
        db.commit()
      else:
        self.logger.warn("Doing nothing.. database already updated...")
    except Exception as e:
      if cursor is not None: db.rollback()
      raise BackendError("Error while inserting lastcall ids into SQLite: %s" % e)
    finally:
      if db is not None: db.close()

  def GetAllTweetCoordinates(self):
    try:
      start = 0
      pagesize = 10
      last = None

      tweets = []
      while True:
        data = { 'query'  : { 'match_all' : { } },
                 'from'   : start,
                 'size'   : pagesize,
                 'fields' : ['coordinates', 'created_at'],
                 'sort'   : [ { 'created_at' : 'asc' } ] }
        data_json = json.dumps(data, indent=2)
        host = "%s/twitter/tweets/_search" % es_server
        req = requests.get(host, data=data_json)
        ret = json.loads(req.content)

        for hit in ret['hits']['hits']:
          curhit = []
          if 'created_at' in hit['fields'] and 'coordinates' in hit['fields']:
            created_at = datetime.strptime(hit['fields']['created_at'], '%Y-%m-%d %H:%M:%S')
            curhit.append(created_at)
            coordinates = hit['fields']['coordinates']
            if ',' in coordinates:
              curhit.append(coordinates.split(',')[1])
              curhit.append(coordinates.split(',')[0])
            else:
              curhit.append(None)
              curhit.append(None)
            tweets.append(curhit)
            self.logger.debug("new google.maps.LatLng(%s, %s)," % (curhit[2], curhit[1]))

        last = ret['hits']['total']
        start += pagesize
        if start > last: break

      return tweets
    except Exception as e:
      raise BackendError("Error while retrieving tweet coordinates from ElasticSearch: %s" % e)

  def GetLocations(self):
    try:
      data = { 'size'   : 0,
               'facets' : { 'locations': { 'terms' : { 'field' : 'location', 'size' : 20 }, 'global': True } } }
      data_json = json.dumps(data, indent=2)
      host = "%s/twitter/tweets/_search" % es_server
      req = requests.get(host, data=data_json)
      ret = json.loads(req.content)

      locations = []
      for hit in ret['facets']['locations']['terms']:
        locations.append(hit['term'])

      return locations
    except Exception as e:
      raise BackendError("Error while retrieving locations from ElasticSearch: %s" % e)

  def _GetTweetsIdForLocation(self, location):
    try:
      start = 0
      pagesize = 10
      last = None

      rows = []
      while True:
        data = { 'query' : { "term": { "location" : location } },
                 'fields' : [ ],
                 'from'  : start,
                 'size'  : pagesize }
        data_json = json.dumps(data, indent=2)
        host = "%s/twitter/tweets/_search" % es_server
        req = requests.get(host, data=data_json)
        ret = json.loads(req.content)

        for hit in ret['hits']['hits']:
          if '_id' in hit:
            rows.append(hit['_id'])

        last = ret['hits']['total']
        start += pagesize
        if start > last: break

      return rows
    except Exception as e:
      raise BackendError("Error while retrieving kmls from ElasticSearch: %s" % e)
    
  def UpdateCoordinates(self, location, lat, lng):
    self.logger.info("Updating coordinate for location %s: [%s, %s]." % (location, lat, lng))
    try:
      tweetids = self._GetTweetsIdForLocation(location)

      for tweetid in tweetids:
        data = { 'script' : 'ctx._source.coordinates = newcoords',
                 'params' : { 'newcoords' : "%s,%s" % (lat, lng) } }
        data_json = json.dumps(data, indent=2)
        host = "%s/twitter/tweets/%s/_update" % (es_server, tweetid)
        req = requests.post(host, data=data_json)
        ret = json.loads(req.content)
        if not ret["ok"]: raise Exception("Insert not ok")
    except Exception as e:
      raise BackendError("Error while updating coordinates for location into ElasticSearch: %s" % e)

  def InsertFrenchDepartments(self, vals):
    self.logger.info("Inserting row for %s, %s." % (vals[2], vals[6]))
    try:
      data = { 'ID_GEOFLA' : vals[0],
               'CODE_DEPT' : vals[1],
               'NOM_DEPT'  : vals[2],
               'CODE_CHF'  : vals[3],
               'NOM_CHF'   : vals[4],
               'CODE_REG'  : vals[5],
               'NOM_REG'   : vals[6],
               'KML'       : vals[7] }
      data_json = json.dumps(data, indent=2)
      host = "%s/twitter/french_depts/%s" % (es_server, vals[0])
      req = requests.put(host, data=data_json)
      ret = json.loads(req.content)
      if not ret["ok"]: raise BackendError("Insert not ok")
    except Exception as e:
      raise BackendError("Error while inserting French department into ElasticSearch: %s" % e)
