__author__ = "Andrea Biancini"
__date__ = "October 2, 2013"

import MySQLdb

from backend import Backend, BackendError
from ..secrets import dbhost, dbuser, dbpass, dbname

class MySQLBackend(Backend):
  con = None
  cur = None

  def __init__(self, engine_config):
    Backend.__init__(self, engine_config)
    try:
      self.con = MySQLdb.connect(host=dbhost,
                                 user=dbuser,
                                 passwd=dbpass,
                                 db=dbname,
                                 charset='utf8')
      self.logger.info("Connected to MySQL db %s:%s." % (dbhost, dbname))
      self.cur = self.con.cursor()
    except Exception as e:
      raise BackendError("Error connecting to MySQL db %s:%s: %s" % (dbhost, dbname, e))

  def __del__(self):
    if self.con: self.con.close()
    
  def BulkInsertTweetIntoDb(self, vals):
    num_inserted = 0
    top_id = None
    
    try:
      for val in vals:
        num_inserted += self.InsertTweetIntoDb(val)
        if top_id is None or top_id < long(val['id']):
            top_id = long(val['id'])
    except BackendError as e:
      self.logger.error("Bulk insert not ok for tweet %s: %s " % (val['id'], e))
      
    return (num_inserted, top_id)

  def InsertTweetIntoDb(self, vals):
    if vals is None: return 0

    try:
      text = vals['text'].encode(encoding='ascii', errors='ignore').decode(encoding='ascii', errors='ignore')
      sql_vals = (vals['id'],
                  vals['created_at'],
                  text.replace('\\', '\\\\').replace('\'', '\\\''),
                  vals['userid'],
                  vals['location'].replace('\\', '\\\\').replace('\'', '\\\''),
                  vals['latitude'],
                  vals['longitude'],
                  vals['num_friends'],
                  vals['happiness'],
                  vals['relevance'])

      sql = 'INSERT INTO tweets (`tweetid`, `timestamp`, `text`, `userid`, `user_location`, `latitude`, `longitude`, `num_friends`, `happiness`, `relevance`) '
      sql += 'VALUES (%s, \'%s\', \'%s\', \'%s\', \'%s\', %s, %s, %s, %s, %s)' % sql_vals

      self.cur.execute(sql)
      self.con.commit()
      return 1
    except Exception as e:
      code, _msg = e
      if code == 1062:
        self.con.rollback()
        raise BackendError("Tried to insert a tweet already present in the DB: %s" % vals[0])
      else:
        self.logger.error("Exception while inserting tweet %s: %s" % (vals[0], e))

      self.con.rollback()
      return 0

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

  def GetMaxId(self):
    self.logger.info("Retrieving max tweet id from database.")
    try:
      self.cur.execute("SELECT max(`tweetid`) FROM tweets")
      row = self.cur.fetchone()

      return long(row[0])
    except Exception as e:
      raise BackendError("Error while retrieving max tweet id from DB: %s" % e)

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
    self.logger.info("Updating coordinate for location %s: [%s, %s]." % (location, lat, lng))
    try:
      self.cur.execute("UPDATE tweets SET latitude = %s, longitude = %s WHERE user_location = '%s'" % (lat, lng, location.replace('\\', '\\\\').replace('\'', '\\\'')))
      self.con.commit()
    except Exception as e:
      raise BackendError("Error while updating coordinates for location into DB: %s" % e)

  def InsertUSAStates(self, vals):
    self.logger.info("Inserting row for %s (%s)." % (vals[0], vals[1]))
    field_list = 'id, name, geometry'
    try:
      sql = "INSERT INTO usa_states (%s) " % field_list
      sql += "VALUES ('%s','%s','%s')" % vals
      self.logger.debug(sql)

      self.cur.execute(sql)
      self.con.commit()
    except Exception as e:
      raise BackendError("Error while inserting USA State into DB: %s" % e)

  def InsertFrenchDepartments(self, vals):
    self.logger.info("Inserting row for %s, %s." % (vals[2], vals[4]))
    field_list = 'ID_GEOFLA,CODE_DEPT,NOM_DEPT,CODE_CHF,NOM_CHF,CODE_REG,NOM_REG,KML'
    try:
      sql = "INSERT INTO french_deps (%s) " % field_list
      sql += "VALUES (%d,'%s','%s','%s','%s','%s','%s','%s')" % vals
      self.logger.debug(sql)

      self.cur.execute(sql)
      self.con.commit()
    except Exception as e:
      raise BackendError("Error while inserting French department into DB: %s" % e)
