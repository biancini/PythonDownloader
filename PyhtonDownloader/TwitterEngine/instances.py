# LANGUAGE = "fr"
#   
# INSTANCES = [{
#               'name': 'France',
#               'apikey': 0,
#               'type': 'rest',
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
              'filters': [40., -115., '900km'],
           }, {
              'name': 'USA-Central',
              'apikey': 2,
              'type': 'rest',
              'filters': [38., -100., '1050km'],
           }, {
              'name': 'USA-EastCoast',
              'apikey': 4,
              'type': 'rest',
              'filters': [35., -85., '1050km'],
           }]
