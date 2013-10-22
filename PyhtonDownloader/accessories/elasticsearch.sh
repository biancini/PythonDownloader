#!/bin/bash

SERVER="http://localhost:9200"

curl -XPUT "${SERVER}/twitter/"

#"EEE M dd HH:mm:ss +0000 yyyy"
curl -XPOST "${SERVER}/twitter/tweets/_mapping" -d '{ "tweets" : {
	"properties" : {
		"created_at": { "type": "date", "format" : "yyyy-MM-dd HH:mm:ss" },
		"location" : { "type": "string", "index": "not_analyzed" },
		"num_friends" : { "type": "integer", "index": "not_analyzed" },
		"coordinates": { "type": "geo_point" },
                "text": { "type": "string", "analyzer": "french" },
                "userid": { "type": "long" }
	}
} }' 

if [ "$?" -eq "0" ];
then
	curl -XGET "${SERVER}/twitter/tweets/_mapping?pretty=true"
fi

