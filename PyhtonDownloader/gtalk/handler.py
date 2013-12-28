import sys
import os.path
sys.path.append(os.path.abspath(os.path.join(os.path.dirname(__file__), os.path.pardir)))

import threading

from logging import Handler
from talkbot import TwitterDownloaderBot

from TwitterEngine.secrets import google_user_name, google_password, administrator_jid

class GTalkHandler(Handler):
  bot = None
  
  def __init__(self, recipients=None):
    Handler.__init__(self)
    self.bot = TwitterDownloaderBot()
    self.bot.setState('available', 'Twitter Downloader')
    self.bot.recipients = recipients if recipients is not None else administrator_jid[:]
    
    threading.Thread(target=self.bot.start, args=[google_user_name, google_password]).start()

  def emit(self, record):
    try:
      message = self.format(record)
      for user in self.bot.recipients:
        self.bot.replyMessage(user, message, typ='chat')
    except:
      pass
