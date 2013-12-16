# LANGUAGE = "fr"
#    
# INSTANCES = [{
#               'name': 'France',
#               'apikey': 0,
#               'type': 'rest',
#               'bulk': True,
#               'locking': False,
#               'filters': [45.776665, 3.07723, '400km'],
#            }]

# LANGUAGE = "fr"
# INSTANCES = [{
#               'name': 'France',
#               'apikey': 0,
#               'type': 'stream',
#               'locking': True,
#               'filters': ['-5.1,43.1,7.3,50.1'],
#               }]

LANGUAGE = "en"
INSTANCES = [{
              'name': 'USA-WestCoast',
              'apikey': 0,
              'type': 'rest',
              'bulk': True,
              'locking': False,
              'filters': [39., -115., '1050km'],
           }, {
              'name': 'USA-MiddleWest1',
              'apikey': 1,
              'type': 'rest',
              'bulk': True,
              'locking': False,
              'filters': [41., -100., '650km'],
           }, {
              'name': 'USA-MiddleWest2',
              'apikey': 2,
              'type': 'rest',
              'bulk': True,
              'locking': False,
              'filters': [33., -98., '750km'],
           }, {
              'name': 'USA-GreatLakes',
              'apikey': 3,
              'type': 'rest',
              'bulk': True,
              'locking': False,
              'filters': [40., -86., '600km'],
           }, {
              'name': 'USA-SouthEast',
              'apikey': 4,
              'type': 'rest',
              'bulk': True,
              'locking': False,
              'filters': [30., -83., '650km'],
           }, {
              'name': 'USA-NorthEast',
              'apikey': 5,
              'type': 'rest',
              'bulk': True,
              'locking': False,
              'filters': [40., -71., '700km'],
           }]