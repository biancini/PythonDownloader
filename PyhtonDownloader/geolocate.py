#!/usr/bin/env python
# -*- coding: utf-8 -*-

import sys
from TwitterEngine import GeolocateBackend

if __name__ == "__main__":
  engine = GeolocateBackend('oAuth2')
  engine.UpdateBackendData()
