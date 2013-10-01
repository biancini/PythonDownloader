#!/usr/bin/env python
# -*- coding: utf-8 -*-

import sys
import json
import MySQLdb

from shapely.geometry import shape, Point
from secrets import dbhost, dbuser, dbpass, dbname

def CheckPointInKml(kml_db, lat, lng):
  p = Point(lng, lat)

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

  con = MySQLdb.connect(host = dbhost,
                        user = dbuser,
                        passwd = dbpass,
                        db = dbname,
                        charset = 'utf8')
  print "Connected to MySQL db %s:%s." % (dbhost, dbname)
  cur = con.cursor()

  cur.execute("SELECT NOM_REG, KML FROM french_deps")
  rows = cur.fetchall()
  con.close()

  inside = False
  for row in rows:
    inside = CheckPointInKml(eval(row[1]), lat, lng)
    if inside:
      print "Point [%s,%s] is in region %s." % (lat, lng, row[0])
      break

  if not inside:
    print "Point [%s,%s] is NOT part of any French department." % (lat, lng)
