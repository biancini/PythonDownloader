__author__ = "Andrea Biancini"
__date__ = "October 2, 2013"

import MySQLdb

from backend import Backend, BackendError
from ..secrets import dbhost, dbuser, dbpass, dbname

class MySQLBackend(Backend):
  con = None
  cur = None

  def __init__(self):
    try:
      self.con = MySQLdb.connect(host=dbhost,
                                 user=dbuser,
                                 passwd=dbpass,
                                 db=dbname,
                                 charset='utf8')
      print("Connected to MySQL db %s:%s." % (dbhost, dbname))
      self.cur = self.con.cursor()
    except Exception as e:
      raise BackendError("Error connecting to MySQL db %s:%s: %s" % (dbhost, dbname, e))

  def __del__(self):
    if self.con: self.con.close()
    
  def GetUSAKmls(self):
    try:
      self.cur.execute("SELECT name, geometry FROM usa_states")
      rows = self.cur.fetchall()

      kmls = []
      for row in rows:
        kmls.append((row[0], row[1]));

      return kmls
    except Exception as e:
      raise BackendError("Error while retrieving USA kmls from DB: %s" % e)

  def GetFrenchKmls(self):
    try:
      self.cur.execute("SELECT NOM_REG, KML FROM french_deps")
      rows = self.cur.fetchall()

      kmls = []
      for row in rows:
        kmls.append((row[0], row[1]));

      return kmls
    except Exception as e:
      raise BackendError("Error while retrieving French kmls from DB: %s" % e)

  def GetAllTweetCoordinates(self):
    try:
      # self.cur.execute("SELECT `timestamp`, `latitude`, `longitude` FROM tweets ORDER BY `timestamp` LIMIT 100")
      self.cur.execute("SELECT `timestamp`, `latitude`, `longitude` FROM tweets ORDER BY `timestamp`")
      rows = self.cur.fetchall()

      tweets = []
      for row in rows:
        tweets.append([row[0], row[1], row[2]]);

      return tweets
    except Exception as e:
      raise BackendError("Error while retrieving tweet coordinates from DB: %s" % e)

  def GetLocations(self):
    try:
      self.cur.execute("SELECT user_location, COUNT(*) AS `number` FROM tweets WHERE latitude IS NULL GROUP BY user_location ORDER BY number DESC")
      rows = self.cur.fetchall()

      locations = []
      for row in rows:
        locations.append(row[0]);

      return locations
    except Exception as e:
      raise BackendError("Error while retrieving locations from DB: %s" % e)

  def UpdateCoordinates(self, location, lat, lng):
    print("Updating coordinate for location %s: [%s, %s]." % (location, lat, lng))
    try:
      self.cur.execute("UPDATE tweets SET latitude = %s, longitude = %s WHERE user_location = '%s'" % (lat, lng, location.replace('\\', '\\\\').replace('\'', '\\\'')))
      self.con.commit()
    except Exception as e:
      raise BackendError("Error while updating coordinates for location into DB: %s" % e)

  def InsertUSAStates(self, vals):
    print("Inserting row for %s (%s)." % (vals[0], vals[1]))
    field_list = 'id, name, geometry'
    try:
      sql = "INSERT INTO usa_states (%s) " % field_list
      sql += "VALUES ('%s','%s','%s')" % vals
      print(sql)

      self.cur.execute(sql)
      self.con.commit()
    except Exception as e:
      raise BackendError("Error while inserting USA State into DB: %s" % e)

  def InsertFrenchDepartments(self, vals):
    print("Inserting row for %s, %s." % (vals[2], vals[4]))
    field_list = 'ID_GEOFLA,CODE_DEPT,NOM_DEPT,CODE_CHF,NOM_CHF,CODE_REG,NOM_REG,KML'
    try:
      sql = "INSERT INTO french_deps (%s) " % field_list
      sql += "VALUES (%d,'%s','%s','%s','%s','%s','%s','%s')" % vals
      print(sql)

      self.cur.execute(sql)
      self.con.commit()
    except Exception as e:
      raise BackendError("Error while inserting French department into DB: %s" % e)
