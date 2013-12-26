#!/usr/bin/python
# -*- coding: utf-8 -*-

# PyGtalkRobot: A simple jabber/xmpp bot framework using Regular Expression Pattern as command controller
# Copyright (c) 2008 Demiao Lin <ldmiao@gmail.com>
#
# This program is free software; you can redistribute it and/or modify
# it under the terms of the GNU General Public License as published by
# the Free Software Foundation; either version 3 of the License, or
# (at your option) any later version.
#
# This program is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
# GNU General Public License for more details.
#
# You should have received a copy of the GNU General Public License
# along with this program.  If not, see <http://www.gnu.org/licenses/>.
#
# Homepage: http://code.google.com/p/pygtalkrobot/
#

import sys, traceback
import xmpp
import re
import inspect

"""A simple jabber/xmpp bot framework

This is a simple jabber/xmpp bot framework using Regular Expression Pattern as command controller.
Copyright (c) 2008 Demiao Lin <ldmiao@gmail.com>

To use, subclass the "GtalkRobot" class and implement "command_NUM_" methods
(or whatever you set the command_prefix to), like sampleRobot.py.

"""

class GtalkRobot:
  conn = None
  show = "available"
  status = "PyGtalkRobot"
  commands = None
  command_prefix = 'command_'
  GO_TO_NEXT_COMMAND = 'go_to_next'
  logger = None

  # Pattern Tips:
  # I or IGNORECASE <=> (?i)      case insensitive matching
  # L or LOCALE <=> (?L)          make \w, \W, \b, \B dependent on the current locale
  # M or MULTILINE <=> (?m)       matches every new line and not only start/end of the whole string
  # S or DOTALL <=> (?s)          '.' matches ALL chars, including newline
  # U or UNICODE <=> (?u)         Make \w, \W, \b, and \B dependent on the Unicode character properties database.
  # X or VERBOSE <=> (?x)         Ignores whitespace outside character sets
  
  # This method is the default action for all pattern in lowest priviledge
  def command_999_default(self, user, message, args):
    # the __doc__ of the function is the Regular Expression of this command, if matched, this command method will be called. 
    # The parameter "args" is a list, which will hold the matched string in parenthesis of Regular Expression.
    """.*?(?s)(?m)"""
    self.replyMessage(user, "Unknown command: %s" % message)

  ########################################################################################################################
  # These following methods can be only used after bot has been successfully started

  # show : xa,away---away   dnd---busy   available--online
  def setState(self, show, status_text):
    if show:
      show = show.lower()
    if show == "online" or show == "on" or show == "available":
      show = "available"
    elif show == "busy" or show == "dnd":
      show = "dnd"
    elif show == "away" or show == "idle" or show == "off" or show == "out" or show == "xa":
      show = "xa"
    else:
      show = "available"
      
    self.show = show

    if status_text:
      self.status = status_text
    
    if self.conn:
      pres = xmpp.Presence(priority=5, show=self.show, status=self.status)
      self.conn.send(pres)

  def getState(self):
    return self.show, self.status
  
  def replyMessage(self, user, message):
    status_code = self.conn.send(xmpp.Message(user, message, typ='chat'))
    self.logger.debug("Status code: %s" % status_code)
  
  def getRoster(self):
    return self.conn.getRoster()
  
  def getResources(self, jid):
    roster = self.getRoster()
    if roster:
      return roster.getResources(jid)
  
  def getShow(self, jid):
    roster = self.getRoster()
    if roster:
      return roster.getShow(jid)
  
  def getStatus(self, jid):
    roster = self.getRoster()
    if roster:
      return roster.getStatus(jid)
  
  def authorize(self, jid):
    """ Authorise JID 'jid'. Works only if these JID requested auth previously. """
    self.getRoster().Authorize(jid)
    
  def initCommands(self):
    if self.commands:
      self.commands.clear()
    else:
      self.commands = list()
      
    for (name, value) in inspect.getmembers(self):
      if inspect.ismethod(value) and name.startswith(self.command_prefix):
        self.commands.append((value.__doc__, value))
        
    self.logger.debug(self.commands)

  def controller(self, conn, message):
    text = message.getBody()
    user = message.getFrom()
    
    if text:
      text = text.encode('utf-8', 'ignore')

      if not self.commands: self.initCommands()

      for (pattern, bounded_method) in self.commands:
        match_obj = re.match(pattern, text)

        if (match_obj):
          try:
            return_value = bounded_method(user, text, match_obj.groups())
            if return_value == self.GO_TO_NEXT_COMMAND: pass
            else: break
          except:
            # self.logger.error("Error: %s" % sys.exc_info())
            self.replyMessage(user, traceback.format_exc())

  def presenceHandler(self, conn, presence):
    self.logger.debug("Presence: %s." % presence)
    
    if presence:
      self.logger.debug("From: %s, Resource: %s, Type: %s, Status: %s, Show: %s" % (presence.getFrom(), presence.getFrom().getResource(), presence.getType(), presence.getStatus(), presence.getShow()))
      if presence.getType() == 'subscribe':
        jid = presence.getFrom().getStripped()
        self.authorize(jid)

  def StepOn(self):
    try:
      self.conn.Process(1)
    except KeyboardInterrupt: 
      return 0
    return 1

  def GoOn(self):
    while self.StepOn(): pass

  ########################################################################################################################
  # "debug" parameter specifies the debug IDs that will go into debug output.
  # You can either specifiy an "include" or "exclude" list. The latter is done via adding "always" pseudo-ID to the list.
  # Full list: ['nodebuilder', 'dispatcher', 'gen_auth', 'SASL_auth', 'bind', 'socket', 'CONNECTproxy', 'TLS', 'roster', 'browser', 'ibb'].
  def __init__(self, logger, server_host="talk.google.com", server_port=5223, debug=[]):
    self.debug = debug
    self.server_host = server_host
    self.server_port = server_port
    self.logger = logger

  def start(self, gmail_account, password):
    jid = xmpp.JID(gmail_account)
    user, server, password = jid.getNode(), jid.getDomain(), password
    
    self.conn = xmpp.Client(server, debug=self.debug)
    conres = self.conn.connect(server=(self.server_host, self.server_port))

    if not conres:
      self.logger.error("Unable to connect to server %s!" % server)
      sys.exit(1)
    
    if conres <> 'tls':
      self.logger.warning("Unable to estabilish secure connection - TLS failed!")
    
    authres = self.conn.auth(user, password)
    if not authres:
      self.logger.error("Unable to authorize on %s - Plsese check your name/password." % server)
      sys.exit(1)
    if authres <> "sasl":
      self.logger.warn("Unable to perform SASL auth os %s. Old authentication method used!" % server)
    
    self.conn.RegisterHandler('message', self.controller)
    self.conn.RegisterHandler('presence', self.presenceHandler)
    
    self.conn.sendInitPresence()
    
    self.setState(self.show, self.status)
    
    self.logger.info('Bot started.')
    self.GoOn()
