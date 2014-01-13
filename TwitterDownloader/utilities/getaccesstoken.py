#!/usr/bin/env python
# -*- coding: utf-8 -*-
# parse_qsl moved to urlparse module in v2.6

import sys
import os.path
sys.path.append(os.path.abspath(os.path.join(os.path.dirname(__file__), os.path.pardir)))

import webbrowser
import oauth2 as oauth

from urlparse import parse_qsl

REQUEST_TOKEN_URL = 'https://api.twitter.com/oauth/request_token'
ACCESS_TOKEN_URL = 'https://api.twitter.com/oauth/access_token'
AUTHORIZATION_URL = 'https://api.twitter.com/oauth/authorize'
SIGNIN_URL = 'https://api.twitter.com/oauth/authenticate'

from TwitterEngine.secrets import consumer_key, consumer_secret

def get_access_token(i, consumer_key, consumer_secret):

    # signature_method_hmac_sha1 = oauth.SignatureMethod_HMAC_SHA1()
    oauth_consumer = oauth.Consumer(key=consumer_key[i], secret=consumer_secret[i])
    oauth_client = oauth.Client(oauth_consumer)

    print 'Requesting temp token from Twitter'

    resp, content = oauth_client.request(REQUEST_TOKEN_URL, 'POST', body="oauth_callback=oob")

    if resp['status'] != '200':
        print 'Invalid respond from Twitter requesting temp token: %s' % resp['status']
    else:
        request_token = dict(parse_qsl(content))
        url = '%s?oauth_token=%s' % (AUTHORIZATION_URL, request_token['oauth_token'])

        print ''
        print 'I will try to start a browser to visit the following Twitter page'
        print 'if a browser will not start, copy the URL to your browser'
        print 'and retrieve the pincode to be used'
        print 'in the next step to obtaining an Authentication Token:'
        print ''
        print url
        print ''

        webbrowser.open(url)
        pincode = raw_input('Pincode? ')

        token = oauth.Token(request_token['oauth_token'], request_token['oauth_token_secret'])
        token.set_verifier(pincode)

        print ''
        print 'Generating and signing request for an access token'
        print ''

        oauth_client = oauth.Client(oauth_consumer, token)
        resp, content = oauth_client.request(ACCESS_TOKEN_URL, method='POST', body='oauth_callback=oob&oauth_verifier=%s' % pincode)
        access_token = dict(parse_qsl(content))

        if resp['status'] != '200':
            print 'The request for a Token did not succeed: %s' % resp['status']
            print access_token
        else:
            print 'Your Twitter Access Token key: %s' % access_token['oauth_token']
            print '          Access Token secret: %s' % access_token['oauth_token_secret']
            print ''

if __name__ == "__main__":
    i = input("Inserisci l'indice della app che vuoi usare: ")
    # consumer_key = raw_input('Enter your consumer key: ')
    # consumer_secret = raw_input("Enter your consumer secret: ")
    get_access_token(i, consumer_key, consumer_secret)
