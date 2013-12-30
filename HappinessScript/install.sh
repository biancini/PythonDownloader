#!/bin/bash

ES_PLUGIN=/usr/share/elasticsearch/bin/plugin

mvn package
sudo $ES_PLUGIN -remove happiness-plugin
sudo $ES_PLUGIN -install happiness-plugin -url file://`pwd`/target/elasticsearch-happiness-script-0.0.1-SNAPSHOT.jar
sudo service elasticsearch restart
