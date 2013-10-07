#!/usr/bin/env python
# -*- coding: utf-8 -*-

import urllib
import urllib2
import json
import sys

from TwitterEngine import BackendChooser, BackendError
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

if __name__ == "__main__":
  backend = BackendChooser.GetBackend()

  access_token = getGoogleAccessToken()
  tablename = '1_r_DC9mlrFCJB93hNfzD9P8murmI4_2tmgaQcZ4'
  field_list = 'ID_GEOFLA,CODE_DEPT,NOM_DEPT,CODE_CHF,NOM_CHF,CODE_REG,NOM_REG,KML'
  response = sqlGetFusionTable(access_token, 'SELECT %s FROM %s' % (field_list,tablename))
  result_set = json.loads(response)

  for row in result_set['rows']:
    vals = (int(row[0]),
            row[1].replace('\'', '\\\''),
            row[2].replace('\'', '\\\''),
            row[3].replace('\'', '\\\''),
            row[4].replace('\'', '\\\''),
            row[5].replace('\'', '\\\''),
            row[6].replace('\'', '\\\''),
            str(row[7]).replace('\'', '\\\''))

    backend.InsertFrenchDepartments(vals)
