#!/usr/bin/env python
# -*- coding: utf-8 -*-

import logging
import logging.config
from functools import wraps

from PyGtalkRobot import GtalkRobot
from secrets import google_user_name, google_password, administrator_jid

logging.config.fileConfig("logging.conf")
logger = logging.getLogger('root')

def authorized(command_function):
  @wraps(command_function)
  def authorized_command(self, user, message, args):
    self.logger.debug('Authorizing command: %s' % message)
    
    jid = user.getStripped()
    if not jid == administrator_jid:
      self.logger.warning("Tried to call unauthorized command with jid: %s, message: %s" % (jid, message))
      self.replyMessage(user, "You do not have access to this command!!")
    else:
      self.logger.debug("Command authorized for user: %s." % jid)
      return command_function(self, user, message, args)
  
  return authorized_command

class TwitterDownloaderBot(GtalkRobot):
  def __init__(self, logger):
    GtalkRobot.__init__(self, logger, debug=[])
  
  # Regular Expression Pattern Tips:
  # I or IGNORECASE <=> (?i)      case insensitive matching
  # L or LOCALE <=> (?L)          make \w, \W, \b, \B dependent on the current locale
  # M or MULTILINE <=> (?m)       matches every new line and not only start/end of the whole string
  # U or UNICODE <=> (?u)         Make \w, \W, \b, and \B dependent on the Unicode character properties database.
  # S or DOTALL <=> (?s)          '.' matches ALL chars, including newline
  # X or VERBOSE <=> (?x)         Ignores whitespace outside character sets
  
  def command_001_hello(self, user, message, args):
    '''hello'''
    jid = user.getStripped()
    
    self.logger.info("Received hello message from jid: %s." % jid)
    self.replyMessage(jid, "Hello, nice to talk to you!")
  
  @authorized
  def command_002_setstate(self, user, message, args):
    '''setstate (available|online|on|busy|dnd|away|idle|out|off|xa)( +(.*))?$(?i)'''
    show = args[0]
    status = args[1]
    jid = user.getStripped()
    
    self.logger.info("Received setstate message from jid: %s." % jid)
    # self.logger.debug("Jid: %s, resources: %s, show: %s, status: %s" % (jid, bot.getResources(jid), bot.getShow(jid), bot.getStatus(jid)))

    self.setState(show, status)
    self.replyMessage(user, "State settings changed!")
  
if __name__ == "__main__":
  bot = TwitterDownloaderBot(logger)
  bot.setState('available', 'Twitter Downloader')
  bot.start(google_user_name, google_password)
