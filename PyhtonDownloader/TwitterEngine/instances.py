# LANGUAGE = "fr"
#   
# INSTANCES = [{
#               'name': 'France',
#               'apikey': 0,
#               'type': 'rest',
#               'bulk': False,
#               'filters': [45.776665, 3.07723, '400km'],
#            }]

# LANGUAGE = "fr"
# INSTANCES = [{
#               'name': 'France',
#               'apikey': 0,
#               'type': 'stream',
#               'filters': ['-5.1,43.1,7.3,50.1'],
#               }]

LANGUAGE = "en"
INSTANCES = [{
              'name': 'USA-WestCoast',
              'apikey': 0,
              'type': 'rest',
              'bulk': True,
              'filters': [40., -112., '1050km'],
           }, {
              'name': 'USA-Central',
              'apikey': 1,
              'type': 'rest',
              'bulk': True,
              'filters': [37., -95., '1050km'],
           }, {
              'name': 'USA-EastCoast1',
              'apikey': 2,
              'type': 'rest',
              'bulk': True,
              'filters': [40., -76., '500km'],
           }, {
              'name': 'USA-EastCoast2',
              'apikey': 4,
              'type': 'rest',
              'bulk': True,
              'filters': [31., -80., '600km'],
           }]
