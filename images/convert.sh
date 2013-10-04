#!/bin/bash

#ffmpeg -f image2 -r 24 -i france%05d.png -vcodec mpeg4 -y movie.mp4
ffmpeg -r 24 -f image2 -s 502x490 -i france%04d.png -vcodec libx264 -crf 15 -vpre normal movie.mp4

#rm france*.png
