#!/usr/bin/python

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
ripetizioni = 1
lastid = None
lang = 'fr'

from secrets import dbhost, dbuser, dbpass, dbname
con = mdb.connect(dbhost, dbuser, dbpass, dbname)
with con:
	cur = con.cursor()

	for i in range(0, ripetizioni):
		statuses = api.GetSearch(geocode=(lat, lng, radius), count=count, max_id=lastid, lang=lang)
	        #statuses = api.GetUserTimeline('andreabiancini')
		lastid = min([s.id for s in statuses])

		print "Number of tweets downloaded:\t%d\n" % len(statuses)
		for s in statuses:
			date_object = datetime.strptime(s.created_at, '%a %b %d %H:%M:%S +0000 %Y')
			sql = "INSERT INTO tweets (`tweetid`, `timestamp`, `text`, `hashtags`) VALUES ('%s', '%s', '%s', '%s')" % (s.id, date_object.strftime('%Y-%m-%d %H:%M:%S'), s.text.replace('\'', '\\\''), s.hashtags)
			try:
				cur.execute(sql)
				con.commit()
			except Exception, e:
				print sql
				print "Exception: %s" % e
				con.rollback()

		#printouts = [s.text for s in statuses]
		#for curprint in printouts: print "[%s]" % curprint

print "Finish"
