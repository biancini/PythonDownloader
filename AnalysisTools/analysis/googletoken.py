#!/usr/bin/env python
# -*- coding: utf-8 -*-

import sys
import os.path
sys.path.append(os.path.abspath(os.path.join(os.path.dirname(__file__), os.path.pardir)))

from TwitterEngine.secrets import google_client_id, google_client_secret

# Create AppEngine application
# Enter Google APIs Console -> API Access
# Create Oauth 2.0 Service Key -> (CLIENT_ID, CLIENT_SECRET, REDIRECT_URI)
# Create Application Key -> API_KEY
 
import urllib
import urllib2
import json
import webbrowser
 
redirect_uri = 'http://localhost/oauth2callback'
 
def generateInstallAppUrl(scope):
    return ('https://accounts.google.com/o/oauth2/auth?' + \
            'access_type=offline&response_type=code&' + \
            'client_id=%s&redirect_uri=%s&' + \
            'scope=%s') % (google_client_id, redirect_uri, scope)
 
def firstTime():
    # in this case, the scope is https://www.googleapis.com/auth/drive
    #scope = "https://www.googleapis.com/auth/drive"
    scope = "https://www.googleapis.com/auth/fusiontables"
 
    # Call install app url
    url = generateInstallAppUrl(scope)
    
    webbrowser.open_new(url)
 
    # get one shot token
    one_shot_token = raw_input('Insert token: ')
    
    # Call https://accounts.google.com/o/oauth2/token to get access_token and refresh_token
    # (Handling Response in https://developers.google.com/accounts/docs/OAuth2InstalledApp)
    data = urllib.urlencode({
          'code': one_shot_token,
          'client_id': google_client_id,
          'client_secret': google_client_secret,
          'redirect_uri': redirect_uri,
          'grant_type': 'authorization_code'})
    request = urllib2.Request(
          url='https://accounts.google.com/o/oauth2/token',
          data=data)
    request_open = urllib2.urlopen(request)
    response = request_open.read()
    request_open.close()
    tokens = json.loads(response)
    return (tokens['access_token'], tokens['refresh_token'])
    
# Memorize in some way refresh token or just set REFRESH_TOKEN and set isFirstTime = false
access_token = ''
refresh_token = ''
(access_token, refresh_token) = firstTime()
 
print "Access token: %s" % access_token
print "Refresh token: %s" % refresh_token
