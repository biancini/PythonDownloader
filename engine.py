#!/usr/bin/env python
# -*- coding: utf-8 -*-

import sys
from TwitterEngine import DownloadTweetsREST, DownloadTweetsStream

if __name__ == "__main__":
  mechanism = 'rest'
  if len(sys.argv) > 1: mechanism = sys.argv[1]

  if mechanism not in ['rest', 'stream']:
    print 'Wrong mechanism passed, assuming rest.'
    mechanism = 'rest'

  if mechanism == 'rest':
    engine = DownloadTweetsREST('oAuth2')
  else:
    engine = DownloadTweetsStream('oAuth1')

  #engine.SelectMaxTweetId()
  #print engine.GetCurrentLimit()
  #print engine.PrintRateLimit()
  engine.ProcessTweets()
