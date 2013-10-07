#!/bin/bash

curl -XPOST 'http://localhost:9200/twitter/tweets/_mapping' -d '{ "tweets" : { "properties" : { "created_at": { "type": "date", "format" : "EEE M dd HH:mm:ss +0000 yyyy" } } } }'

