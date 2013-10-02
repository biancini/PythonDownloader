#!/usr/bin/env python
# -*- coding: utf-8 -*-

import MySQLdb
import pprint
import sys
import json

from datetime import datetime

from backend import Backend, BackendError
from secrets import dbhost, dbuser, dbpass, dbname


class MySQLBackend(Backend):
  con = None
  cur = None

  def __init__(self):
    try:
      self.con = MySQLdb.connect(host = dbhost,
                                 user = dbuser,
                                 passwd = dbpass,
                                 db = dbname,
                                 charset = 'utf8')
      print "Connected to MySQL db %s:%s." % (dbhost, dbname)
      self.cur = self.con.cursor()
    except Exception as e:
      raise BackendError("Error connecting to MySQL db %s:%s: %s" % (dbhost, dbname, e))

  def __del__(self):
    if self.con: self.con.close()

  def SelectMaxTweetId(self):
    try:
      self.cur.execute("SELECT MAX(`tweetid`) FROM tweets")
      firstid = self.cur.fetchone()[0]

      print "The last tweetid in table is %s." % firstid
      return firstid
    except Exception as e:
      raise BackendError("Error while retrieving firstid: %s" % e)

  def InsertTweetIntoDb(self, sql_vals):
    try:
      sql  = 'INSERT INTO tweets (`tweetid`, `timestamp`, `text`, `hashtags`, `user_location`, `latitude`, `longitude`) '
      sql += 'VALUES (\'%s\', \'%s\', \'%s\', \'%s\', \'%s\', %s, %s)' % sql_vals

      self.cur.execute(sql)
      self.conn.commit()
      return 1
    except Exception as e:
      if type(e) is tuple: code, msg = e
      else:
        code = 0
        msg = str(e)

      if code == 1062:
        self.conn.rollback()
        raise BackendError("Exiting because tried to insert a tweet already present in the DB.")
      else:
        print "Exception while inserting tweet %s: %s" % (sql_vals[0], e)
        self.conn.rollback()
	return 0

  def GetKmls(self):
    try:
      self.cur.execute("SELECT NOM_REG, KML FROM french_deps")
      rows = self.cur.fetchall()

      kmls = []
      for row in rows:
        kmls.append((row[0], row[1]));

      return kmls
    except Exception as e:
      raise BackendError("Error while retrieving kmls from DB: %s" % e)
