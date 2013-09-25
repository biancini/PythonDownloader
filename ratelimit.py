#!/usr/bin/python
# -*- coding: utf-8 -*-

import twitter
import pprint
from secrets import consumer_key, consumer_secret, access_token_key, access_token_secret

api = twitter.Api(consumer_key = consumer_key,
                  consumer_secret = consumer_secret,
                  access_token_key = access_token_key,
                  access_token_secret = access_token_secret)

pp = pprint.PrettyPrinter(depth=6)
pp.pprint(api.GetRateLimitStatus())
