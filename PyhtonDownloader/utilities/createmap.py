#!/usr/bin/env python
# -*- coding: utf-8 -*-

import sys
import os.path
sys.path.append(os.path.abspath(os.path.join(os.path.dirname(__file__), os.path.pardir)))

from TwitterEngine import DrawMap

if __name__ == "__main__":
  engine = DrawMap('oAuth2')
  engine.ProduceImages()
