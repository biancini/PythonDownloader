__author__ = "Andrea Biancini"
__date__ = "October 2, 2013"

import pprint
import sys
import requests
import json
import pprint
import StringIO

from datetime import datetime

from backend import Backend, BackendError
from secrets import es_server


class ElasticSearchBackend(Backend):
  def BackendLanguage(self):
    return "JSON"

  def SelectMaxTweetId(self):
    try:
      data = { 'query'  : { 'match_all' : { } },
               'sort'   : [ { 'id' : 'desc' } ],
               'fields' : [ 'id' ],
               'size'   : 1 }
      data_json = json.dumps(data, indent=2)
      host = "%s/twitter/tweets/_search" % es_server
      req = requests.get(host, data=data_json)
      ret = json.loads(req.content)
      for hit in ret['hits']['hits']:
        return hit['fields']['id']

      raise BackendError("No tweet found")      
    except Exception as e:
      raise BackendError("Error while retrieving firstid: %s" % e)

  def InsertTweetIntoDb(self, vals):
    if vals is None: return 0

    try:
      data = vals
      data_json = json.dumps(data, indent=2)
      host = "%s/twitter/tweets/%s" % (es_server, vals['id'])
      req = requests.put(host, data=data_json)
      ret = json.loads(req.content)
      if not ret["ok"]: raise BackendError("Insert not ok")
      if ret["_version"] > 1: raise BackendError("Tried to insert a tweet already present in the DB: %s" % vals['id'])
      return 1
    except Exception as e:
      print "Exception while inserting tweet %s: %s" % (vals['id'], e)
      return 0

  def GetKmls(self):
    print "Retrieving all French departments"
    try:
      start    = 0
      pagesize = 10
      last     = None

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

  def GetLastCallIds(self):
    try:
      data = { 'query' : { 'match_all' : { } } }
      data_json = json.dumps(data, indent=2)
      host = "%s/twitter/lastcall/_search" % es_server
      req = requests.get(host, data=data_json)
      ret = json.loads(req.content)

      ids = [None, None]
      for hit in ret['hits']['hits']:
        ids[0] = hit['max_id']
        ids[1] = hit['since_id']

      return ids
    except Exception as e:
      raise BackendError("Error while retrieving last call ids from ElasticSearch: %s" % e)

  def UpdateLastCallIds(self, max_id = None, since_id = None):
    print "Updating lastcall with values max_id = %s and since_id = %s." % (max_id, since_id)
    try:
      data = { 'max_id'   : max_id,
               'since_id' : since_id }
      data_json = json.dumps(data, indent=2)
      host = "%s/twitter/lastcall/1" % es_server
      req = requests.put(host, data=data_json)
      ret = json.loads(req.content)
      if not ret["ok"]: raise BackendError("Insert not ok")
    except Exception as e:
      raise BackendError("Error while updating last call ids into ElasticSearch: %s" % e)

  def GetAllTweetCoordinates(self):
    try:
      #self.cur.execute("SELECT `timestamp`, `latitude`, `longitude` FROM tweets ORDER BY `timestamp` LIMIT 100")
      self.cur.execute("SELECT `timestamp`, `latitude`, `longitude` FROM tweets ORDER BY `timestamp`")
      rows = self.cur.fetchall()

      tweets = []
      for row in rows:
        tweets.append([row[0], row[1], row[2]]);

      return tweets
    except Exception as e:
      raise BackendError("Error while retrieving tweet coordinates from DB: %s" % e)

  def GetLocations(self):
    try:
      self.cur.execute("SELECT user_location, COUNT(*) AS `number` FROM tweets WHERE latitude IS NULL GROUP BY user_location ORDER BY number DESC")
      rows = self.cur.fetchall()

      locations = []
      for row in rows:
        locations.append(row[0]);

      return locations
    except Exception as e:
      raise BackendError("Error while retrieving locations from DB: %s" % e)

  def UpdateCoordinates(self, location, lat, lng):
    print "Updating coordinate for location %s: [%s, %s]." % (location, lat, lng)
    try:
      self.cur.execute("UPDATE tweets SET latitude = %s, longitude = %s WHERE user_location = '%s'" % (lat, lng, location.replace('\\', '\\\\').replace('\'', '\\\'')))
      self.con.commit()
    except Exception as e:
      raise BackendError("Error while updating coordinates for location into DB: %s" % e)

  def InsertFrenchDepartments(self, vals):
    print "Inserting row for %s, %s." % (vals[2], vals[4])
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
