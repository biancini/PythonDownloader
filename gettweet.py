#!/usr/bin/env python
# -*- coding: utf-8 -*-

from TwitterAPI import TwitterAPI
import json
import pprint
import sys
from secrets import consumer_key, consumer_secret, auth_type, access_token_key, access_token_secret

class TwitterApiCall(object):
  api = TwitterAPI(consumer_key = consumer_key,
                   consumer_secret = consumer_secret,
                   #auth_type = auth_type)
                   access_token_key = access_token_key,
                   access_token_secret = access_token_secret)

  def GetTweetFromId(self, tweetid):
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

  pp = pprint.PrettyPrinter(depth=2)
  pp.pprint(engine.GetTweetFromId(tweetid))
