#!/usr/bin/python
# -*- coding: utf-8 -*-

import twitter
import MySQLdb as mdb
from datetime import datetime
from secrets import consumer_key, consumer_secret, access_token_key, access_token_secret
from secrets import dbhost, dbuser, dbpass, dbname

api = twitter.Api(consumer_key = consumer_key,
                  consumer_secret = consumer_secret,
                  access_token_key = access_token_key,
                  access_token_secret = access_token_secret)

lat = 45.776665
lng = 3.07723
radius = '400km'
count = 100
lastid = None
firstid = None
lang = 'fr'

from secrets import dbhost, dbuser, dbpass, dbname
con = mdb.connect(host = dbhost,
                  user = dbuser,
                  passwd = dbpass,
                  db = dbname,
                  charset = 'utf8')
with con:
	cur = con.cursor()

	try:
		cur.execute("SELECT MAX(`tweetid`) FROM tweets")
		firstid = cur.fetchone()[0]
		print "The last tweetid in table is %s" % firstid
	except Exception, e:
		firsid = None

	while True:
		statuses = api.GetSearch(geocode = (lat, lng, radius),
					 count = count,
					 lang = lang,
					 result_type = 'recent',
					 include_entities = False,
					 max_id = lastid,
		                         since_id = firstid)
		if (len(statuses) == 0): break
		print "Number of tweets downloaded:\t%d" % len(statuses)
		for s in statuses:
			if (firstid is None): break

			date_object = datetime.strptime(s.created_at, '%a %b %d %H:%M:%S +0000 %Y')
			sql_vals = (s.id,
				    date_object.strftime('%Y-%m-%d %H:%M:%S'),
				    s.text.replace('\'', '\\\''),
                                    ', '.join([h.text for h in s.hashtags]))
			sql  = "INSERT INTO tweets (`tweetid`, `timestamp`, `text`, `hashtags`) "
			sql += "VALUES ('%s', '%s', '%s', '%s')" % sql_vals
			#print s.text

			try:
				cur.execute(sql)
				con.commit()
			except Exception as e:
				code, msg = e

				if code == 1062: break
				else: print "%s\nException: %s" % (sql, e)

				con.rollback()

		lastid = min([s.id for s in statuses]) - 1

print "Finish"
