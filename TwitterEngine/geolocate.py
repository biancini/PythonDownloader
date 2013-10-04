__author__ = "Andrea Biancini"
__date__ = "October 2, 2013"

import os
import sys

from twitterapi import TwitterApiCall
from backend import BackendError
from mysqlbackend import MySQLBackend


class Geolocate(TwitterApiCall): 
  def __init__(self, auth_type):
    super(Geolocate, self).__init__(auth_type)
    self.backend = MySQLBackend()

  def UpdateBackendData(self):
    locations = self.backend.GetLocations()
    for location in locations:
      coords = self.Geolocate(location)
      if coords[0] == 'NULL' or coords[1] == 'NULL': continue
      self.UpdateCoordinates(location, coords[0], coords[0])
