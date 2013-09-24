import twitter
from secrets import consumer_key, consumer_secret, access_token_key, access_token_secret

api = twitter.Api(consumer_key = consumer_key,
                  consumer_secret = consumer_secret,
                  access_token_key = access_token_key,
                  access_token_secret = access_token_secret)

#print api.VerifyCredentials()

#statuses = api.GetUserTimeline('andreabiancini')
#statuses = api.GetSearch('renzi :)') #UserTimeline('andreabiancini')

lastid = None
for i in [1, 2, 3, 4]:
	statuses = api.GetSearch(geocode=(41.892916, 12.482520, '500km'), count=100, max_id=lastid) #UserTimeline('andreabiancini')
	lastid = min([s.id for s in statuses])
	printouts = [s.text for s in statuses]
	for curprint in printouts:
		print curprint


