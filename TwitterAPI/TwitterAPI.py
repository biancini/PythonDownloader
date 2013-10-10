__author__ = "Andrea Biancini"
__date__ = "September 27, 2013"

from .constants import *
import json
import requests
import base64
import urllib
import urllib2
import sys
from requests_oauthlib import OAuth1

class BearerAuth(requests.auth.AuthBase):
  def __init__(self, token_url, consumer_key, consumer_secret):
    self._token_url = token_url
    self._consumer_key = consumer_key
    self._consumer_secret = consumer_secret
    self._bearer_token = self.GetAccessToken()

  def GetAccessToken(self):
    b64_bearer_token_creds = base64.b64encode(self._consumer_key + ':' + self._consumer_secret)
    header = {}
    values = {}
    header['User-Agent'] = 'Mozilla/6.0 (Windows NT 6.2; WOW64; rv:16.0.1) Gecko/20121011 Firefox/16.0.1'
    header['Authorization'] = 'Basic ' + b64_bearer_token_creds
    header['Content-Type'] = 'application/x-www-form-urlencoded;charset=UTF-8'
    #header['Accept-Encoding'] = 'gzip'
    values['grant_type'] = 'client_credentials'

    data = urllib.urlencode(values)
    req = urllib2.Request(self._token_url, data, header)
    try:
      response = urllib2.urlopen(req)
      data = json.loads(response.read())
      return data['access_token']
    except urllib2.HTTPError as e:
      print >> sys.stderr, 'Error while requesting bearer access token: %s' % e
      raise Exception('Twitter error in retrieving bearer access token')

  def __call__(self, r):
    auth_list = [self._consumer_key, self._consumer_secret, self._bearer_token]
    if all(auth_list):
      r.headers['Authorization'] = "Bearer %s" % self._bearer_token
      return r
    else:
      raise Exception('No enough keys passed to Bearer token manager.')

class TwitterAPI(object):	
	"""Access REST API or Streaming API resources."""
	
	def __init__(self, auth_type="oAuth1", consumer_key=None, consumer_secret=None, access_token_key=None, access_token_secret=None):
		"""Initialize with your Twitter application credentials"""
		if auth_type is "oAuth1":
			if not all([consumer_key, consumer_secret, access_token_key, access_token_secret]):
				raise Exception("Missing authentication parameter.")
			self.auth = OAuth1(consumer_key, consumer_secret, access_token_key, access_token_secret)
		elif auth_type is "oAuth2":
			if not all([consumer_key, consumer_secret]):
				raise Exception("Missing authentication parameter.")
			token_url = '%s://%s.%s/%s' % (PROTOCOL, TOKEN_SUBDOMAIN, DOMAIN, TOKEN_ENDPOINT)
			self.auth = BearerAuth(token_url, consumer_key, consumer_secret)
				
	def _prepare_url(self, subdomain, path):
		return '%s://%s.%s/%s/%s.json' % (PROTOCOL, subdomain, DOMAIN, VERSION, path)
	
	def request(self, resource, params=None):
		"""Request a resource from Twitter
		
		:param resource: A string with the resource path (ex. search/tweets)
		:param params: A dict of resource parameters
		:returns: A TwitterResponse object
		"""
		session = requests.Session() 
		session.auth = self.auth
		session.headers = {'User-Agent':USER_AGENT}
		if resource in REST_ENDPOINTS:
			session.stream = False
			method = REST_ENDPOINTS[resource][0]
			url = self._prepare_url(REST_SUBDOMAIN, resource)
			timeout = REST_SOCKET_TIMEOUT
		elif resource in STREAMING_ENDPOINTS:
			session.stream = True
			method = 'GET' if params is None else 'POST'
			url = self._prepare_url(STREAMING_ENDPOINTS[resource][0], resource)
			timeout = STREAMING_SOCKET_TIMEOUT
		else:
			raise Exception('"%s" is not valid endpoint' % resource)
		r = session.request(method, url, params=params, timeout=timeout)
		return TwitterResponse(r, session.stream)


class TwitterResponse(object):
	"""Response returned by TwitterAPI.request()"""
	
	def __init__(self, response, stream):
		"""Initialize a response from either the REST or Streaming API
		
		:param response: A requests.Response object 
		:param stream: A boolean, True for a streaming connection
		"""
		self.response = response
		self.stream = stream

	@property
	def status_code(self):
		"""A property containing HTTP status code returned by Twitter
		
		:returns: An integer, 200 when no error
		"""
		return self.response.status_code

	@property
	def text(self):
		"""A property containing the data returned by Twitter
		
		:returns: A UTF-8 string with the raw text response
		"""
		return self.response.text

	def get_iterator(self):
		"""Gets an iterator for the data returned by Twitter
		
		:returns: Either a StreamingIterator or a RestIterator object
		"""
		if self.stream:
			return StreamingIterator(self.response) 
		else:
			return RestIterator(self.response)
		
	def __iter__(self):
		"""Iterates through data returned by Twitter
		
		:returns: A JSON object representing the return value
		"""
		for item in self.get_iterator():
			yield item

	def get_rest_quota(self):
		"""Gets API quota from the response header of a REST API call
		
		:returns: A dict containing the number of calls remaining
		"""
		remaining, limit, reset = None, None, None
		if self.response:
			if 'x-rate-limit-remaining' in self.response.headers:
				remaining = int(self.response.headers['x-rate-limit-remaining'])
				if remaining == 0:
					limit = int(self.response.headers['x-rate-limit-limit'])
					reset = int(self.response.headers['x-rate-limit-reset'])
					reset = datetime.fromtimestamp(reset)
		return {'remaining': remaining, 'limit': limit, 'reset': reset}

				
class RestIterator(object):
	"""Iterator of REST API response data"""
	
	def __init__(self, response):
		"""Initialize the iterator
		
		:param response: A request.Response object
		"""
		resp = response.json()
		
		# Extract iterable parts in the response
		if 'errors' in resp:
			self.results = resp['errors']
		elif 'statuses' in resp:
			self.results = resp['statuses']
		elif hasattr(resp, '__iter__') and type(resp) is not dict:
			if len(resp) > 0 and 'trends' in resp[0]:
				self.results = resp[0]['trends']
			else:
				self.results = resp
		else:		
			self.results = (resp,)
		
	def __iter__(self):
		"""Iterates through data returned by Twitter
		
		:returns: A JSON object representing the return value
		"""
		for item in self.results:
			yield item
				
				
class StreamingIterator(object):
	"""Iterator of Streaming API response data"""

	def __init__(self, response):
		"""Initialize the iterator
		
		:param response: A request.Response object
		"""
		self.results = response.iter_lines()
		
	def __iter__(self):
		"""Iterates through data returned by Twitter
		
		:returns: A JSON object representing the return value
		"""
		for item in self.results:
			if item:
				yield json.loads(item.decode('utf-8'))

