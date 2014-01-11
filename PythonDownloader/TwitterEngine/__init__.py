__title__ = 'TwitterEngine'
__version__ = '1.0'
__author__ = 'Andrea Biancini'
__copyright__ = 'Copyright 2013 Andrea Biancini'
__date__ = "October 2, 2013"


from .rest import DownloadTweetsREST
from .stream import DownloadTweetsStream

from .backend import TwitterApiCall, BackendChooser, BackendError, MySQLBackend, ElasticSearchBackend

__all__ = [
           'TwitterApiCall',
           'DownloadTweetsREST',
           'DownloadTweetsStream',
           'BackendError',
           'MySQLBackend',
           'ElasticSearchBackend'
]
