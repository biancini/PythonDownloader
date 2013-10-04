#!/usr/bin/env python
# -*- coding: utf-8 -*-

import sys
from TwitterEngine import Geolocate

if __name__ == "__main__":
  engine = Geolocate('oAuth2')
  engine.UpdateBackendData()
