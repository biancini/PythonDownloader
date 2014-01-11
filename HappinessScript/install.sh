#!/bin/bash

ES_DIR=/usr/share/elasticsearch
ES_PLUGIN=${ES_DIR}/bin/plugin
ES_LIBDIR=${ES_DIR}/lib
# 2179  jar tvf /home/andrea/Documents/uni/research/economics/twitter/TwitterAnalyzer/java-geojson/target/java-geojson-1.2-ABSNAPSHOT.jar


if ! ls ${ES_LIBDIR}/java-geojson*.jar &> /dev/null;
then
  echo "You have first to install the java-geojson dependency!"
  exit 1
fi

mvn package

sudo $ES_PLUGIN -remove happiness-plugin
sudo $ES_PLUGIN -install happiness-plugin -url file://`pwd`/target/elasticsearch-happiness-script-0.0.1-SNAPSHOT.jar
sudo service elasticsearch restart
