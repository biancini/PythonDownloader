__author__ = "Andrea Biancini"
__date__ = "October 2, 2013"

import pprint
import sys
import json

import os
root_path = os.path.abspath(os.path.join(__file__, '..', '..'))
lib_path = os.path.join(root_path, 'lib')
sys.path.insert(0, lib_path)

from datetime import datetime
from TwitterAPI import TwitterAPI
from geopy import geocoders

from shapely.geometry import shape, Point
from shapely.geometry.collection import GeometryCollection
from secrets import consumer_key, consumer_secret, access_token_key, access_token_secret

from logger import Logger
# from happyanalyzer import HappyAnalyzer

class TwitterApiCall(object):
  continuerun = True

  @staticmethod
  def stop_run():
    TwitterApiCall.continuerun = False

  @staticmethod
  def continuing():
    return TwitterApiCall.continuerun

  engine_name = None
  language = None
  filters = None
  
  lock_file_download = None
  
  api = None
  backend = None
  analyzer = None
  auth_type = None
  initial_apiid = -1
  apiid = -1
  
  logger = None

  def __init__(self, engine_config, language, locking, auth_type='oAuth2'):
    self.engine_name = engine_config['name']
    self.apiid = engine_config['apikey']
    self.language = language
    self.filters = engine_config['filters']
    self.auth_type = auth_type
    self.logger = Logger(engine_config['name'])
    # self.analyzer = HappyAnalyzer(self.language)
    
    self.InitializeTwitterApi()
    
  def GetEngineName(self):
    return self.engine_name
  
  def IsLocking(self):
    return self.lock_file_download is not None
  
  def SetLockFileDownload(self, lock_file_download):
    self.lock_file_download = lock_file_download
  
  def GetLockFileDownload(self):
    return self.lock_file_download

  def getMechanism(self):
    raise NotImplementedError
  
  def log(self, message, newline=True):
    if self.logger is None:
      if newline:
          print message
      else:
        sys.stdout.write('.')
        sys.stdout.flush()
    else: self.logger.log(message, newline)

  def InitializeTwitterApi(self):
    self.apiid += 1
    self.apiid %= len(consumer_key)
    
    if self.apiid == self.initial_apiid:
      raise Exception("Tried to use every application key, no more available.")
    
    if self.initial_apiid == -1:
      self.initial_apiid = self.apiid

    self.logger.log("Initializing engine with consumer_key = %s" % consumer_key[self.apiid])
    if self.auth_type == 'oAuth2':
      self.api = TwitterAPI(consumer_key=consumer_key[self.apiid],
                            consumer_secret=consumer_secret[self.apiid],
                            auth_type=self.auth_type)
    else:
      self.api = TwitterAPI(consumer_key=consumer_key[self.apiid],
                            consumer_secret=consumer_secret[self.apiid],
                            auth_type=self.auth_type,
                            access_token_key=access_token_key[self.apiid],
                            access_token_secret=access_token_secret[self.apiid])

  def ProcessTweets(self):
    raise NotImplementedError

  def AggregateByPerson(self, date):
    raise NotImplementedError

  def GetRateLimits(self):
    params = {}
    response = self.api.request('application/rate_limit_status', params)
    return json.loads(response.text)

  def PrintRateLimit(self):
    pp = pprint.PrettyPrinter(depth=6)
    pp.pprint(self.GetRateLimits())

  def Geolocate(self, location):
    # g = geocoders.GoogleV3(google_clientid, google_secret)
    # g = geocoders.MapQuest(api_key=mapquest_appid)
    g = geocoders.GeoNames()
 
    try:
      for _place, (lat, lng) in g.geocode(location, exactly_one=False):
        # print "Computed coordinates for %s: %s, %s." % (location, lat, lng)
        coordinates = [str(lat), str(lng)]
    except Exception as e:
      print "Error while geocoding: %s" % e
      coordinates = ['NULL', 'NULL']

    return coordinates

  def CheckPointInKml(self, kmls, lat, lng):
    p = Point(lng, lat)
    found = False

    for (name, kml_txt) in kmls:
      kml = eval(kml_txt)
      if 'geometry' in kml:
        kml_json = json.loads(json.dumps(kml['geometry']))
        found = shape(kml_json).contains(p)
        if found: return name
      elif 'geometries' in kml:
        kml_jsons = json.loads(json.dumps(kml['geometries']))
        for kml_json in kml_jsons:
          if shape(kml_json).contains(p): return name
    
    return None

  def FromTweetToVals(self, tweet, geolocate=True, exclude_out=True):
    date_object = datetime.strptime(tweet['created_at'], '%a %b %d %H:%M:%S +0000 %Y')
    # text = tweet['text'].encode(encoding='ascii', errors='ignore').decode(encoding='ascii', errors='ignore')
    text = tweet['text']
    location = tweet['user']['location']

    coordinates = ['NULL', 'NULL']
    try:
      if tweet['coordinates'] and tweet['coordinates']['type'] == 'Point':
        c = tweet['coordinates']['coordinates']
        coordinates[0] = c[1]
        coordinates[1] = c[0]
      elif tweet['place'] and tweet['place']['bounding_box']:
        kml_json = json.loads(json.dumps(tweet['place']['bounding_box']))
        geom = shape(kml_json).centroid
        if type(geom) == GeometryCollection:
          if len(list(geom.geoms)) >= 1:
            coordinates = [list(geom.geoms)[0].y, list(geom.geoms)[0].x]
        elif type(geom) == Point:
          coordinates = [geom.y, geom.x]
        else:
          print "Tweet place is of unknown type: %s." % type(geom)
    except Exception as e:
      print "Error while parsing coordinates: %s" % e
      
    if (coordinates[0] == 'NULL' or coordinates[1] == 'NULL') and geolocate:
      coordinates = self.Geolocate(location)

    kmls = None
    if exclude_out:
      if coordinates[0] == 'NULL' or coordinates[1] == 'NULL': return None
      kmls = self.backend.GetKmls()
    if kmls and not self.CheckPointInKml(kmls, float(coordinates[0]), float(coordinates[1])): return None

    # pp = pprint.PrettyPrinter(depth=6)
    # pp.pprint(tweet)
    # sys.exit("Fine")

    ret_vals = {}
    ret_vals['id'] = tweet['id']
    ret_vals['created_at'] = date_object.strftime('%Y-%m-%d %H:%M:%S')
    ret_vals['text'] = text
    ret_vals['userid'] = tweet['user']['id']
    # ret_vals['hashtags'] = ', '.join([h['text'] for h in tweet['entities']['hashtags']])
    ret_vals['location'] = location
    ret_vals['latitude'] = coordinates[0]
    ret_vals['longitude'] = coordinates[1]
    ret_vals['num_friends'] = tweet['user']['friends_count']

    # happy = self.analyzer.ScoreTweetHappiness(text)
    # ret_vals['happiness'] = happy[0]
    # ret_vals['relevance'] = happy[1]

    return ret_vals
