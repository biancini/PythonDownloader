import sys
import os.path
import xmpp

sys.path.append(os.path.abspath(os.path.join(os.path.dirname(__file__), os.path.pardir)))

import logging

from TwitterEngine.secrets import google_user_name, google_password

class GTalkHandler(logging.Handler):
  recipients = None
  conn = None
  
  def __init__(self, recipients=[]):
    logging.Handler.__init__(self)
    self.recipients = recipients

    try:
      jid = xmpp.JID(google_user_name)
      user, server = jid.getNode(), jid.getDomain()
      self.conn = xmpp.Client(server, debug=[])
      
      conres = self.conn.connect()
      if not conres:
        raise Exception("Unable to connect to server %s!" % server)
      
      authres = self.conn.auth(user, google_password)
      if not authres:
        raise Exception("Unable to authorize on %s - Plsese check your name/google_password." % server)
    except Exception as e:
      sys.stderr.write("Error during GTalkHandler initialization: %s" % e)
  
  def emit(self, record):
    try:
      message = self.format(record)
      for user in self.recipients:
        self.conn.send(xmpp.Message(user, message, typ='chat'))
    except:
      pass
