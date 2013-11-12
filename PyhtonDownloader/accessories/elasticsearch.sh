#!/bin/bash

SERVER="http://localhost:9200"

curl -XDELETE "${SERVER}/twitter"

#curl -XPUT "${SERVER}/twitter/"

curl -XPOST "${SERVER}/twitter/" -d '{
	"settings": {
		"analysis" : {
			"analyzer": {
				"tweettext": {
					"type": "snowball",
					"language": "French"
				}
			}
		}
	},
	"mappings": {
		"tweets": {
			"properties" : {
 				"created_at": { "type": "date", "format" : "yyyy-MM-dd HH:mm:ss" },
				"location" : { "type": "string", "index": "not_analyzed" },
				"num_friends" : { "type": "integer", "index": "not_analyzed" },
				"coordinates": { "type": "geo_point" },
        	        	"text": { "type": "string", "store": "yes", "analyzer": "tweettext" },
	        	        "userid": { "type": "long", "index": "not_analyzed" },
                                "happiness": { "type": "integer" },
                                "relevance": { "type": "float" }
			}
		}
	}
}'

if [ "$?" -eq "0" ];
then
	curl -XGET "${SERVER}/twitter/tweets/_mapping?pretty=true"
fi

