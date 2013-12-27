__title__ = 'gtalk'
__version__ = '1.0'
__author__ = 'Andrea Biancini'
__copyright__ = 'Copyright 2013 Andrea Biancini'
__date__ = "December 26, 2013"


from .handler import GTalkHandler
from .talkbot import TwitterDownloaderBot

__all__ = [
           'GTalkHandler',
           'TwitterDownloaderBot'
]