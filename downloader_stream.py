#!/usr/bin/env python
# -*- coding: utf-8 -*-

from TwitterAPI import TwitterAPI
import MySQLdb
from datetime import datetime
from secrets import consumer_key, consumer_secret, auth_type, access_token_key, access_token_secret
from secrets import dbhost, dbuser, dbpass, dbname

class TwitterApiCall(object):
  api = TwitterAPI(consumer_key = consumer_key,
                   consumer_secret = consumer_secret,
                   #auth_type = auth_type)
                   access_token_key = access_token_key,
                   access_token_secret = access_token_secret)

class DownloadFrenchTweets(TwitterApiCall):
  con = None

  def __init__(self):
    try:
      self.con = MySQLdb.connect(host = dbhost,
                                 user = dbuser,
                                 passwd = dbpass,
                                 db = dbname,
                                 charset = 'utf8')
      print "Connected to MySQL db %s:%s." % (dbhost, dbname)
    except Exception, e:
      print "Error connecting to MySQL db %s:%s: %s" % (dbhost, dbname, e)

  def __del__(self):
    if self.con: self.con.close()

  def SelectMaxTweetId(self):
    cur = self.con.cursor()
    try:
      cur.execute("SELECT MAX(`tweetid`) FROM tweets")
      firstid = cur.fetchone()[0]
      print "The last tweetid in table is %s." % firstid
    except Exception, e:
      firstid = None
    return firstid

  def InsertTweetsIntoDb(self):
    squares = ['-5.1,43.1,7.3,50.1'] # Rectangle covering all French territory
    lang    = 'fr'                   # French language
    print 'Executing Twitter API calls '

    cur = self.con.cursor()

    params = {'locations':','.join(squares)}
    r = self.api.request('statuses/filter', params)

    for item in r.get_iterator():
      if lang and item['lang'] != lang: continue
      #print item['text']
      date_object = datetime.strptime(item['created_at'], '%a %b %d %H:%M:%S +0000 %Y')
      text = item['text'].encode(encoding='ascii', errors='ignore').decode(encoding='ascii', errors='ignore')
      if item['coordinates'] and item['coordinates']['type'] == 'Point':
        coordinates = item['coordinates']['coordinates']
      else:
        coordinates = ['NULL', 'NULL']

      sql_vals = (item['id'],
                  date_object.strftime('%Y-%m-%d %H:%M:%S'),
                  text.replace('\\', '\\\\').replace('\'', '\\\''),
                  ', '.join([h['text'] for h in item['entities']['hashtags']]),
                  coordinates[0],
                  coordinates[1])

      sql  = 'INSERT INTO tweets (`tweetid`, `timestamp`, `text`, `hashtags`, `latitude`, `longitude`)'
      sql += 'VALUES (\'%s\', \'%s\', \'%s\', \'%s\', %s, %s)' % sql_vals

      try:
        cur.execute(sql)
        self.con.commit()
      except Exception as e:
        print "Error while executing query for tweet %s: %s" % (item['id'], e)
        self.con.rollback()

if __name__ == "__main__":
  engine = DownloadFrenchTweets()
  #engine.SelectMaxTweetId()
  #print engine.GetCurrentLimit()
  #print engine.PrintRateLimit()
  engine.InsertTweetsIntoDb()
