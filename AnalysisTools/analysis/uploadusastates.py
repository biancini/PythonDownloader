#!/usr/bin/env python
# -*- coding: utf-8 -*-

import sys
import os.path
sys.path.append(os.path.abspath(os.path.join(os.path.dirname(__file__), os.path.pardir)))

import urllib
import urllib2
import json

from TwitterEngine import BackendChooser
from TwitterEngine.secrets import google_refresh_token, google_client_id, google_client_secret

def getGoogleAccessToken():
  data = urllib.urlencode({
    'client_id': google_client_id,
    'client_secret': google_client_secret,
    'refresh_token': google_refresh_token,
    'grant_type': 'refresh_token'})
  request = urllib2.Request(
    url='https://accounts.google.com/o/oauth2/token',
    data=data)
  request_open = urllib2.urlopen(request)
  response = request_open.read()
  request_open.close()
  tokens = json.loads(response)
  return tokens['access_token']
    
def sqlGetFusionTable(access_token, sql):
  request = urllib2.Request(url='https://www.googleapis.com/fusiontables/v1/query?%s' % \
                            (urllib.urlencode({'access_token': access_token,
                             'sql': sql})))
  request_open = urllib2.urlopen(request)
  response = request_open.read()
  request_open.close()
  return response

def sqlPostFusionTable(access_token, sql):
    print access_token + " " + sql
    data = urllib.urlencode({'sql': sql})
    request = urllib2.Request(url='https://www.googleapis.com/fusiontables/v1/query',
                              data=data)
    request.add_header('Authorization', 'Bearer %s' % access_token)
    request_open = urllib2.urlopen(request)
    response = request_open.read()
    request_open.close()
    return response

if __name__ == "__main__":
  access_token = getGoogleAccessToken()
  tablename = '1KU0yvyS5glqa5NmHHl9j9_v-12s_b5YA90qUn9Y'
  field_list = 'name,id,geometry'
  response = sqlPostFusionTable(access_token, 'UPDATE %s SET happiness = 1 WHERE id = "%s"' % (tablename, 'AS'))
  result = json.loads(response)
  print result
