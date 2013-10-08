#!/bin/bash

#"EEE M dd HH:mm:ss +0000 yyyy"
curl -XPOST 'http://localhost:9200/twitter/tweets/_mapping' -d '{ "tweets" : { "properties" : { "created_at": { "type": "date", "format" : "yyyy-MM-dd HH:mm:ss" }, "location" : { "type": "string", "index": "not_analyzed" }, "coordinates": { "type": "geo_point" } } } }'

curl -XGET 'http://localhost:9200/twitter/tweets/_mapping?pretty=true'
