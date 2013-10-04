#!/usr/bin/env python
# -*- coding: utf-8 -*-

import sys
from TwitterEngine import DrawMap

if __name__ == "__main__":
  engine = DrawMap('oAuth2')
  engine.ProduceImages()
