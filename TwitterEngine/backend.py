#!/usr/bin/env python
# -*- coding: utf-8 -*-

class Backend(object):
  def SelectMaxTweetId(self):
    raise NotImplementedError

  def InsertTweetIntoDb(self, sql_vals):
    raise NotImplementedError

  def GetKmls(self):
    raise NotImplementedError

class BackendError(Exception):
    pass
