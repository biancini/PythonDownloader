__author__ = "Andrea Biancini"
__date__ = "October 2, 2013"

import threading
import sqlite3
from ..secrets import sqlite_db_path

from backend import Backend, LastcallBackendError

def synchronized(func):
  func.__lock__ = threading.Lock()

  def synced_func(*args, **kws):
    with func.__lock__:
      return func(*args, **kws)

  return synced_func

class SQLiteBackend(Backend):

  def __init__(self, engine_config):
    super(SQLiteBackend, self).__init__(engine_config)
  
  def _GetLastCallDb(self):
    db = sqlite3.connect('%s/lastcall.db' % sqlite_db_path)
    cursor = db.cursor()
    cursor.execute('''SELECT name FROM sqlite_master WHERE type='table' AND name='lastcall';''')

    if cursor.fetchone() is None:
      cursor.execute('''CREATE TABLE lastcall (id INTEGER PRIMARY KEY AUTOINCREMENT, engine_name TEXT, max_id LONG, since_id LONG NOT NULL, UNIQUE (engine_name, max_id, since_id) ON CONFLICT REPLACE)''')
      db.commit()

    return db

  @synchronized
  def GetLastCallIds(self, engine_name, pop=False):
    db = None
    cursor = None
    try:
      db = self._GetLastCallDb()
      cursor = db.cursor()
      cursor.execute('''SELECT id, max_id, since_id FROM lastcall WHERE engine_name = ?;''', (engine_name,))
      all_rows = cursor.fetchall()

      ids = []
      for row in all_rows:
        new_id = {}
        new_id['id'] = row[0]
        new_id['max_id'] = row[1]
        new_id['since_id'] = row[2]
        ids.append(new_id)
        
        if pop: self.DeleteLastCallId(engine_name, row[0])

      db.commit()
      return ids
    except Exception as e:
      if cursor is not None: db.rollback()
      raise LastcallBackendError("Error while retrieving last call ids from SQLite: %s" % e)
    finally:
      if db is not None: db.close()
  
  @synchronized
  def DeleteLastCallId(self, engine_name, lastcall_id):
    self.logger.info("Deleting lastcall id = %s." % (lastcall_id))

    db = None
    cursor = None
    try:
      db = self._GetLastCallDb()
      cursor = db.cursor()
      cursor.execute('''DELETE FROM lastcall WHERE engine_name = ? AND id = ?''', (engine_name, lastcall_id))
      db.commit()
    except Exception as e:
      if cursor is not None: db.rollback()
      raise LastcallBackendError("Error while deleting lastcall id %s from SQLite: %s" % (lastcall_id, e))    
    finally:
      if db is not None: db.close()

  @synchronized
  def InsertLastCallIds(self, engine_name, max_id=None, since_id=None):
    if since_id is None:
      self.logger.error("Wrong parameters in lastcall update, since_id cannot be None.")
      return

    self.logger.info("Updating lastcall with values max_id = %s and since_id = %s." % (max_id, since_id))

    db = None
    cursor = None
    try:
      db = self._GetLastCallDb()
      cursor = db.cursor()
      
      cursor.execute('''SELECT MAX(since_id) FROM lastcall WHERE engine_name = ? AND max_id IS NULL;''', (engine_name,))
      row = cursor.fetchone()
      if max_id is not None or row[0] is None:
        self.logger.debug("Inserting lastcall since no clashing lastcall already present into DB")
        cursor.execute('''INSERT INTO lastcall (engine_name, max_id, since_id) VALUES (?, ?, ?);''', (engine_name, max_id, since_id))
        db.commit()
      elif row[0] < since_id:
        self.logger.debug("Updating lastcall overwriting clashing lastcall already present into DB")
        cursor.execute('''UPDATE lastcall SET since_id = ? WHERE engine_name = ? AND since_id = ? AND max_id IS NULL;''', (since_id, engine_name, row[0]))
        db.commit()
      else:
        self.logger.warn("Doing nothing.. database already updated...")
    except Exception as e:
      if cursor is not None: db.rollback()
      raise LastcallBackendError("Error while inserting lastcall ids into SQLite: %s" % e)
    finally:
      if db is not None: db.close()
