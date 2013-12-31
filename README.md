TwitterAnalyzer
===============

Implementation of a module to download and analyze tweets from Twitter REST and Stream API.
This project consists of different parts each executing a specific task, as described in the following.

PyhtonDownloader
----------------

This is the engine which is able to download tweets from Twitter API as by its configuration.
It can be configured to use REST or STREAM API and to access Twitter it needs to have some authorization key specified.

The configuration of the downloading engines can be specified here: ``TwitterEngine/secrets.py`` as specified in ``TwitterEngine/secrets.py.txt``.

This engine also permits to configure logging facilities by modifying the file ``logging.conf``.

Kibana
------

This folder contains a modified version of the Kibana interface over Elasticsearch.
This version of the interface implements the needed dashboards to show tweet data and uses the plugins developed within this project.

HappinessScript
---------------

This project contains classes used to implement the scripts to compute happiness on individual or faceted tweets.

This project uses Maven and is itself a Maven module.

Elasticsearch-Exporter
----------------------

This project contains a JavaScript module able to clone an existing ElasticSearch database into another newly created database.

accessories
-----------

This project contains a set of scripts to be used for configuration purposes.

Among them the more relevant scripts are:

 * ``elasticsearch.sh``: creates the needed indexes on the backend when using Elasticsearch
 * ``mysql.sql``: creates the needed indexes on the backend when using MySQL
 * ``rounding``: permits to create geographical circles to cover all the US territory
 * ``heatmap``: creates an heatmap of tweets based on geolocalization information

License
=======

This sofware is licensed under the Apache License, Version 2.0.

Information can be found here:
 [Apache License 2.0](http://www.apache.org/licenses/LICENSE-2.0).


Installation and configuration
==============================

To install prerequisites use the command:
```
sudo pip install -r requirements.txt
```
