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
from secrets import consumer_key, consumer_secret, access_token_key, access_token_secret


class TwitterApiCall(object):
  api = None
  backend = None
  auth_type = None
  apiid = -1

  def __init__(self, auth_type='oAuth2'):
    self.auth_type = auth_type
    self.InitializeTwitterApi()

  def InitializeTwitterApi(self):
    self.apiid += 1
    if self.apiid > len(consumer_key):
      raise Exception("Application keys terminated.")

    if self.auth_type == 'oAuth2':
      self.api = TwitterAPI(consumer_key = consumer_key[self.apiid],
                            consumer_secret = consumer_secret[self.apiid],
                            auth_type = self.auth_type)
    else:
      self.api = TwitterAPI(consumer_key = consumer_key[self.apiid],
                            consumer_secret = consumer_secret[self.apiid],
                            auth_type = auth_type[self.apiid],
                            access_token_key = access_token_key[self.apiid],
                            access_token_secret = access_token_secret[self.apiid])

  def ProcessTweets(self):
    raise NotImplementedError

  def GetRateLimits(self):
    params = {}
    response = self.api.request('application/rate_limit_status', params)
    return json.loads(response.text)

  def PrintRateLimit(self):
    pp = pprint.PrettyPrinter(depth=6)
    pp.pprint(self.GetRateLimits())

  def Geolocate(self, location):
    #g = geocoders.GoogleV3(google_clientid, google_secret)
    #g = geocoders.MapQuest(api_key=mapquest_appid)
    g = geocoders.GeoNames()
 
    try:
      for place, (lat, lng) in g.geocode(location, exactly_one=False):
        #print "Computed coordinates for %s: %s, %s." % (location, lat, lng)
        coordinates= [str(lat), str(lng)]
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
    text = tweet['text'].encode(encoding='ascii', errors='ignore').decode(encoding='ascii', errors='ignore')
    location = tweet['user']['location']

    coordinates = ['NULL', 'NULL']
    try:
      if tweet['coordinates'] and tweet['coordinates']['type'] == 'Point':
        coordinates = tweet['coordinates']['coordinates']
      elif tweet['place'] and tweet['place']['bounding_box']:
        kml_json = json.loads(json.dumps(tweet['place']['bounding_box']))
        geom = shape(kml_json).centroid
        if type(geom) == "GeometryCollection":
          coordinates = [list(geom.geoms)[0].y, list(geom.geoms)[0].x]
        else:
          print type(geom)
          coordinates = [geom.y, geom.x]
    except Exception as e:
      print "Error while parsing coordinates: %s" % e
      
    if (coordinates[0] == 'NULL' or coordinates[1] == 'NULL') and geolocate:
      coordinates = self.Geolocate(location)

    #if (coordinates[0] == 'NULL' or coordinates[1] == 'NULL') and location == '':
    #   print tweet

    kmls = None
    if exclude_out:
      if coordinates[0] == 'NULL' or coordinates[1] == 'NULL': return None
      kmls = self.backend.GetKmls()
    if kmls and not self.CheckPointInKml(kmls, float(coordinates[0]), float(coordinates[1])): return None

    ret_vals = {}
    ret_vals['id'] = tweet['id']
    ret_vals['created_at'] = date_object.strftime('%Y-%m-%d %H:%M:%S')
    ret_vals['text'] = text
    ret_vals['hashtags'] = ', '.join([h['text'] for h in tweet['entities']['hashtags']])
    ret_vals['location'] = location
    ret_vals['latitude'] = coordinates[0]
    ret_vals['longitude'] = coordinates[1]

    return ret_vals
