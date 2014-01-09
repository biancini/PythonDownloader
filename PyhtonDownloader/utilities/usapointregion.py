#!/usr/bin/env python
# -*- coding: utf-8 -*-

import sys
import os.path
sys.path.append(os.path.abspath(os.path.join(os.path.dirname(__file__), os.path.pardir)))

import json

from TwitterEngine.instances import INSTANCES
from TwitterEngine import BackendChooser
from shapely.geometry import shape, Point

def CheckPointInKml(kml_db, lat, lng):
  p = Point(lat, lng)

  if 'geometry' in kml_db:
    kml_json = json.loads(json.dumps(kml_db['geometry']))
    return shape(kml_json).contains(p)
  elif 'geometries' in kml_db:
    kml_jsons = json.loads(json.dumps(kml_db['geometries']))
    for kml_json in kml_jsons:
      if shape(kml_json).contains(p): return True

  return False

if __name__ == "__main__":
  lat = 40.75
  lng = -74

  backend = BackendChooser.GetBackend(INSTANCES[0])
  rows = backend.GetUSAKmls()

  inside = False
  for row in rows:
    inside = CheckPointInKml(eval(row[1]), lat, lng)
    if row[0] == "New York": print row[1]
    if inside:
      print "Point [%s,%s] is in State %s." % (lat, lng, row[0])
      break

  if not inside:
    print "Point [%s,%s] is NOT part of any USA State." % (lat, lng)
