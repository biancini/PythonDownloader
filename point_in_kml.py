#!/usr/bin/env python
# -*- coding: utf-8 -*-

import sys
import keytree

from xml.etree import ElementTree
from shapely.geometry import Point, shape

# Parse the KML doc
doc = open("paris.kml").read()
tree = ElementTree.fromstring(doc)
kmlns = tree.tag.split('}')[0][1:]

# Find all Polygon elements anywhere in the doc
elems = tree.findall(".//{%s}Polygon" % kmlns)

# Here's our point of interest
#lat = 37.707799701548467
#lng = 28.722144580890763
lat = 48.822768
lng = 2.345388

p = Point(lng, lat)

# Filter polygon elements using this lambda (anonymous function)
# keytree.geometry() makes a GeoJSON-like geometry object from an
# element and shape() makes a Shapely object of that.
hits = filter(lambda e: shape(keytree.geometry(e)).contains(p), elems)

if len(hits) > 0:
  print "Point is at Paris"
else:
  print "Point is NOT at Paris"
