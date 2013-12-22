#!/usr/bin/env python
# -*- coding: utf-8 -*-a

import sys
import os.path
sys.path.append(os.path.abspath(os.path.join(os.path.dirname(__file__), os.path.pardir)))

import json

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
  lat = 48.822768
  lng = 2.345388

  backend = BackendChooser.GetBackend()
  rows = backend.GetKmls()

  inside = False
  for row in rows:
    inside = CheckPointInKml(eval(row[1]), lat, lng)
    if inside:
      print "Point [%s,%s] is in region %s." % (lat, lng, row[0])
      break

  if not inside:
    print "Point [%s,%s] is NOT part of any French department." % (lat, lng)
