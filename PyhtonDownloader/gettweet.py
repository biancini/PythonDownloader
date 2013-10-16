#!/usr/bin/env python
# -*- coding: utf-8 -*-

from TwitterAPI import TwitterAPI
import json
import pprint
import sys
from  TwitterEngine.secrets import consumer_key, consumer_secret, auth_type, access_token_key, access_token_secret

class TwitterApiCall(object):
  api = None

  def __init__:
    self.api = TwitterAPI(consumer_key = consumer_key[i],
                          consumer_secret = consumer_secret[i],
                          #auth_type = 'oAuth2')
                          access_token_key = access_token_key[i],
                          access_token_secret = access_token_secret[i])

  def GetTweetFromId(self, i, tweetid):
    params = { 'id': tweetid }
    response = self.api.request('statuses/show', params)
    return json.loads(response.text)

if __name__ == "__main__":
  engine = TwitterApiCall()

  if len(sys.argv) > 1:
    tweetid = sys.argv[1]
  else:
    print "Insert tweet id to search: "
    tweetid = sys.stdin.readline()

  i = input("Insert app id to use: ")

  pp = pprint.PrettyPrinter(depth=2)
  pp.pprint(engine.GetTweetFromId(i, tweetid))
