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

  def InsertTweetIntoDb(self, vals):
    try:
      if vals is None: return 0

      host = "%s/twitter/tweets/%s" % (es_server, vals['id'])
      present = requests.head(host)
      if int(present.status_code) != 404:
        #print "HEAD returned %s" % present.status_code
        return 0

      data = {}
      data['created_at'] = vals['created_at']
      data['text'] = vals['text']
      data['userid'] = vals['userid']
      #data['hashtags'] = vals['hashtags']
      data['location'] = vals['location']
      data['num_friends'] = vals['num_friends']
      data['happiness'] = vals['happiness']
      data['relevance'] = vals['relevance']

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

      ids = [None, None, None]
      for hit in ret['hits']['hits']:
        ids[0] = hit['_source']['max_id']
        ids[1] = hit['_source']['since_id']
        ids[2] = hit['_source']['top_id']

      return ids
    except Exception as e:
      raise BackendError("Error while retrieving last call ids from ElasticSearch: %s" % e)

  def UpdateLastCallIds(self, top_id, max_id = None, since_id = None):
    print "Updating lastcall with values top_id = %s, max_id = %s and since_id = %s." % (top_id, max_id, since_id)
    try:
      data = { 'top_id'   : top_id,
               'max_id'   : max_id,
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
      start    = 0
      pagesize = 10
      last     = None

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
            curhit.append(hit['fields']['created_at'])
            coordinates = hit['fields']['coordinates']
            if ',' in coordinates:
              curhit.append(coordinates.split(',')[1])
              curhit.append(coordinates.split(',')[0])
            else:
              curhit.append(None)
              curhit.append(None)
            tweets.append(curhit)

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
      start    = 0
      pagesize = 10
      last     = None

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
    print "Updating coordinate for location %s: [%s, %s]." % (location, lat, lng)
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

  def GetAllTweetByPerson(self, date_from, date_to):
    try:
      start    = 0
      pagesize = 10
      last     = None

      tweeters = {}
      data = { 'query'   : { 'match_all' : { } },
               'from'    : 0,
               'size'    : 0,
               'filter'  : { 'range': { 'created_at': { 'from': date_from.strftime("%Y-%m-%d 00:00:00"),
                                                        'to': date_to.strftime("%Y-%m-%d 23:59:59") } } },
               'facets'  : { 'top_tweeters' : { 'terms': { 'field': 'userid',
                                                           'size': 10000000 } } },
               'sort'    : [ { 'created_at' : 'asc' } ] }
      data_json = json.dumps(data, indent=2)
      host = "%s/twitter/tweets/_search" % es_server
      req = requests.get(host, data=data_json)
      ret = json.loads(req.content)

      for term in ret['facets']['top_tweeters']['terms']:
        tweeters[str(term['term'])] = term['count']

      return tweeters
    except Exception as e:
      raise BackendError("Error while retrieving top tweeters from ElasticSearch: %s" % e)

  def GetAllTweetsForUserId(self, user, num_tweets, date_from, date_to):
    print "Retrieving all tweets for user %s." % user
    try:
      start    = 0
      pagesize = 10
      last     = None

      coordinates = None
      location = None
      num_friends = None
      happiness = 0.0
      relevance = 0.0
      count_tweets = 0

      while True:
        data = { 'query' : { 'term': { 'userid': user } },
                 'from' : start,
                 'size' : pagesize,
                 'filter'  : { 'range': { 'created_at': { 'from': date_from.strftime("%Y-%m-%d 00:00:00"),
                                                          'to': date_to.strftime("%Y-%m-%d 23:59:59") } } }, }
        data_json = json.dumps(data, indent=2)
        host = "%s/twitter/tweets/_search" % es_server
        req = requests.get(host, data=data_json)
        ret = json.loads(req.content)

        for hit in ret['hits']['hits']:
          if coordinates is None and 'coordinates' in hit['_source']:
            coordinates = hit['_source']['coordinates']

          if location is None and 'location' in hit['_source']:
            location = hit['_source']['location']

          if num_friends is None and 'num_friends' in hit['_source']:
            num_friends = hit['_source']['num_friends']

          if 'happiness' in hit['_source'] and 'relevance' in hit['_source']:
            happiness += hit['_source']['happiness']
            relevance += hit['_source']['relevance']
            count_tweets += 1

        last = ret['hits']['total']
        start += pagesize
        if start > last: break

      if not count_tweets == num_tweets:
        raise Exception("Got %d tweets and expecting %d." % (count_tweets, num_tweets))

      happiness /= count_tweets
      relevance /= count_tweets

      tweetperson = {
        'date': date_from.strftime("%Y-%m-%d"),
        'location': location,
        'num_friends': num_friends,
        'coordinates': coordinates,
        'happiness': round(happiness, 2),
        'relevance': round(relevance, 2)
      }
      return tweetperson
    except Exception as e:
      raise BackendError("Error while retrieving tweets from ElasticSearch: %s" % e)

  def InsertByPersonData(self, tweetsdata):
    try:
      if tweetsdata is None: return 0

      host = "%s/twitter/byperson" % es_server
      present = requests.head(host)
      if int(present.status_code) != 404:
        #print "HEAD returned %s" % present.status_code
        return 0

      data_json = json.dumps(tweetsdata, indent=2)
      req = requests.put(host, data=data_json)
      ret = json.loads(req.content)

      if not ret["ok"]: raise BackendError("Insert not ok")
      return 1
    except Exception as e:
      print "Exception while inserting byperson data: %s" % e
      return 0
