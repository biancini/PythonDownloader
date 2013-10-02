#!/usr/bin/env python
# -*- coding: utf-8 -*-

import MySQLdb
import pprint
import sys
import json

from datetime import datetime

from secrets import dbhost, dbuser, dbpass, dbname


class DatabaseBackend(object):
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
    except Exception, e:
      print "Error connecting to MySQL db %s:%s: %s" % (dbhost, dbname, e)

  def __del__(self):
    if self.con: self.con.close()

  def SelectMaxTweetId(self):
    if not self.cur: return None

    try:
      self.cur.execute("SELECT MAX(`tweetid`) FROM tweets")
      firstid = self.cur.fetchone()[0]
      print "The last tweetid in table is %s." % firstid
    except Exception, e:
      firstid = None
    return firstid

  def InsertTweetIntoDb(self, sql_vals):
    if not self.cur:
      print "Not inserting into DB because connection is not valid."
      return False, 0

    sql  = 'INSERT INTO tweets (`tweetid`, `timestamp`, `text`, `hashtags`, `user_location`, `latitude`, `longitude`) '
    sql += 'VALUES (\'%s\', \'%s\', \'%s\', \'%s\', \'%s\', %s, %s)' % sql_vals

    try:
      self.cur.execute(sql)
    except Exception as e:
      code, msg = e
      if code == 1062:
        print "Exiting because tried to insert a tweet already present in the DB."
        self.conn.rollback()
        return False, 0
      else:
        print "Exception while inserting tweet %s: %s" % (sql_vals[0], e)
        self.conn.commit()
        return True, 0

    self.conn.commit()
    return True, 1

  def GetKmls(self):
    if not self.cur: return None

    self.cur.execute("SELECT NOM_REG, KML FROM french_deps")
    rows = self.cur.fetchall()

    kmls = []
    for row in rows:
      kmls.append((row[0], row[1]));

    return kmls
