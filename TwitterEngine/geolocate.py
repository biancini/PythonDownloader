__author__ = "Andrea Biancini"
__date__ = "October 2, 2013"

import os
import sys

from twitterapi import TwitterApiCall
from backend import BackendChooser, BackendError


class GeolocateBackend(TwitterApiCall): 
  def __init__(self, auth_type):
    super(GeolocateBackend, self).__init__(auth_type)
    self.backend = Backend.ChooserGetBackend()

  def UpdateBackendData(self):
    locations = self.backend.GetLocations()
    for location in locations:
      coords = self.Geolocate(location)
      if coords[0] == 'NULL' or coords[1] == 'NULL': continue
      try:
        self.backend.UpdateCoordinates(location, coords[0], coords[1])
      except Exception as e:
        print "Error while updating coordinates: %s" % e
