#!/bin/bash

SERVER="http://localhost:9200"

curl -XDELETE "${SERVER}/twitter"

#curl -XPUT "${SERVER}/twitter/"

# ByPerson aggregation
#		},
#		"byperson": {
#			"properties" : {
#				"userid": { "type": "long", "index": "not_analyzed" },
# 				"date": { "type": "date", "format" : "yyyy-MM-dd||date_time" },
#				"location" : { "type": "string", "index": "not_analyzed" },
#				"num_friends" : { "type": "integer", "index": "not_analyzed" },
#				"coordinates": { "type": "geo_point" },
#			}
curl -XPOST "${SERVER}/twitter/" -d '{
	"settings": {
		"analysis" : {
			"analyzer": {
				"tweettext": {
					"type": "snowball",
					"language": "English"
				}
			}
		}
	},
	"mappings": {
		"tweets": {
			"properties" : {
 				"created_at": { "type": "date", "format" : "yyyy-MM-dd HH:mm:ss||yyyy-MM-dd" },
				"location" : { "type": "string", "index": "not_analyzed" },
				"num_friends" : { "type": "integer", "index": "not_analyzed" },
				"coordinates": { "type": "geo_point" },
        	        	"text": { "type": "string", "store": "yes", "analyzer": "tweettext" },
	        	        "userid": { "type": "long", "index": "not_analyzed" }
			}
		}
	}
}'

if [ "$?" -eq "0" ];
then
	curl -XGET "${SERVER}/twitter/tweets/_mapping?pretty=true"
fi

