__author__ = "Andrea Biancini"
__date__ = "October 2, 2013"

import os

class Logger():
  file_log = None
  
  def __init__(self, engine_name):
    log_dir = '/var/log/twitter'
    if not os.path.exists(log_dir): os.makedirs(log_dir)
    
    file_name = '%s/log_%s.log' % (log_dir, engine_name)
    self.file_log = open(file_name, "a+") 
    
  def log(self, message, newline=True):
    self.file_log.write(message)
    if newline: self.file_log.write('\n') 
    self.file_log.flush()
