__author__ = "Andrea Biancini"
__date__ = "October 2, 2013"

import os
import sys

from PIL import Image, ImageDraw, ImageFont

from twitterapi import TwitterApiCall
from backend import BackendError
from mysqlbackend import MySQLBackend


class DrawMap(TwitterApiCall): 
  color = (255,0,0)
  lower_left = [41.0, -5.5]
  top_right  = [51.6, 10.0]

  def __init__(self, auth_type):
    super(DrawMap, self).__init__(auth_type)
    self.backend = MySQLBackend()

  def GetXY(self, size, lat, lng):
    x = size[0] * (lng - self.lower_left[1]) / (self.top_right[1] - self.lower_left[1])
    y = size[1] * (lat - self.top_right[0]) / (self.lower_left[0] - self.top_right[0])

    return [int(x), int(y)]

  def DrawPoint(self, draw, x, y):
    radius = 5
    draw.ellipse((x - radius/2, y - radius/2, x + radius/2, y + radius/2), fill=self.color)
    del draw

  def ProduceImages(self):
    root_path = os.path.abspath(os.path.join(__file__, '..'))
    red = (255,0,0)

    interval = 30

    advancement = 0
    imgnum = 1
    persistence = 10

    show = []
    for i in range(0, persistence): show.append([])

    tweets = self.backend.GetAllTweetCoordinates()
    if tweets is None: return

    start_time = tweets[0][0]
    end_time = tweets[-1][0]

    font = ImageFont.truetype("%s/freesansbold.ttf" % root_path, 16)

    print "Creating images of France for all tweets in DB."
    print "Will be creating %d images." % ((end_time - start_time).seconds / interval)

    for tweet in tweets:
      if tweet[1] is None or tweet[2] is None: continue
      if tweet[2] < self.lower_left[0] or tweet[2] > self.top_right[0]: continue
      if tweet[1] < self.lower_left[1] or tweet[1] > self.top_right[1]: continue

      delta_secs = (tweet[0] - start_time).seconds

      if delta_secs <= interval:
        show[persistence-1].append([tweet[2], tweet[1]])
      else:
        img = Image.open("%s/france.png" % root_path)
        draw = ImageDraw.Draw(img)

        allpoints = [item for sublist in show for item in sublist]
        for point in allpoints:
          [x, y] = self.GetXY(img.size, point[0], point[1])
          self.DrawPoint(draw, x, y)

        draw.text((0, 0), tweet[0].strftime("%d/%m/%Y %H:%M"), self.color, font=font)
        img.save("%s/../images/france%05d.png" % (root_path, imgnum))
        del draw

        advancement += 1
        if advancement >= interval:
          print "Last image saved is number: %05d." % imgnum
          advancement = 0

        imgnum += 1
        start_time = tweet[0]

        for i in range(1, persistence):
          show[i-1] = show[i]
        show[persistence-1] = []
