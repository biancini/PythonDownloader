__author__ = "Andrea Biancini"
__date__ = "October 2, 2013"

import MySQLdb
import threading

from backend import Backend, LastcallBackendError
from ..secrets import dbhost, dbuser, dbpass, dbname

def synchronized(func):
  func.__lock__ = threading.Lock()

  def synced_func(*args, **kws):
    with func.__lock__:
      return func(*args, **kws)

  return synced_func

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
      self.logger.info("Connected to MySQL db %s:%s." % (dbhost, dbname))
      self.cur = self.con.cursor()
    except Exception as e:
      raise LastcallBackendError("Error connecting to MySQL db %s:%s: %s" % (dbhost, dbname, e))

  def __del__(self):
    if self.con: self.con.close()
    
  @synchronized
  def GetLastCallIds(self, engine_name, pop=False):
    try:
      self.cur.execute("SELECT `key`, `value` from lastcall WHERE `enginename` = '%s'" % engine_name)
      rows = self.cur.fetchall()

      ids = [None, None, None]
      for row in rows:
        if row[0] == 'max_id': ids[0] = row[1]
        elif row[0] == 'since_id': ids[1] = row[1]
        elif row[0] == 'top_id': ids[2] = row[1]
        
        if pop: self.DeleteLastCallId(engine_name, row[0])

      return ids
    except Exception as e:
      raise LastcallBackendError("Error while retrieving last call ids from DB: %s" % e)

  @synchronized
  def DeleteLastCallId(self, engine_name, lastcall_id):
    self.logger.info("Deleting lastcall id = %s." % (lastcall_id))

    try:
      sql = "DELETE FROM lastcall WHERE `engine_name` = '%s' AND id = %s" % (engine_name, lastcall_id)
      self.cur.execute(sql)
      self.con.commit()
    except Exception as e:
      raise LastcallBackendError("Error while deleting lastcall id %s from SQLite: %s" % (lastcall_id, e))    

  @synchronized
  def InsertLastCallIds(self, engine_name, max_id=None, since_id=None):
    if since_id is None:
      self.logger.error("Wrong parameters in lastcall update, since_id cannot be None.")
      return
    
    self.logger.info("Updating lastcall with values max_id = %s and since_id = %s." % (max_id, since_id))

    try:
      self.cur.execute('''SELECT MAX(since_id) FROM lastcall WHERE engine_name = ? AND max_id IS NULL;''', (engine_name,))
      row = self.cur.fetchone()
      if max_id is not None or row[0] is None:
        self.logger.debug("Inserting lastcall since no clashing lastcall already present into DB")
        self.cur.execute('''INSERT INTO lastcall (engine_name, max_id, since_id) VALUES (?, ?, ?);''', (engine_name, max_id, since_id))
        self.con.commit()
      elif row[0] < since_id:
        self.logger.debug("Updating lastcall overwriting clashing lastcall already present into DB")
        self.cur.execute('''UPDATE lastcall SET since_id = ? WHERE engine_name = ? AND since_id = ? AND max_id IS NULL;''', (since_id, engine_name, row[0]))
        self.con.commit()
      else:
        self.logger.warn("Doing nothing.. database already updated...")
    except Exception as e:
      raise LastcallBackendError("Error while inserting lastcall ids into SQLite: %s" % e)
    