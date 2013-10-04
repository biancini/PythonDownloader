#!/usr/bin/env python
# -*- coding: utf-8 -*-

from PIL import Image, ImageDraw

def GetXY(size, lat, lng):
  lower_left = [41.0, -5.5]
  top_right  = [51.6, 10.0]

  x = size[0] * (lng - lower_left[1]) / (top_right[1] - lower_left[1])
  y = size[1] * (lat - top_right[0]) / (lower_left[0] - top_right[0])

  return [int(x), int(y)]

def DrawPoint(img, x, y):
  red = (255,0,0)
  radius = 5

  draw = ImageDraw.Draw(img)
  draw.ellipse((x - radius/2, y - radius/2, x + radius/2, y + radius/2), fill=red)
  #img.putpixel((x, y), red)
  del draw

if __name__ == "__main__":
  red = (255,0,0)

  img = Image.open("france.png")

  points = [[48.851755, 2.355194], [43.283829, 5.388794], [43.609063, 1.431656]]
  for point in points:
    [x, y] = GetXY(img.size, point[0], point[1])
    DrawPoint(img, x, y)

  img.save("output/test1.png")
  img.show()
