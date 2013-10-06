#!/bin/bash

#ffmpeg -f image2 -r 24 -i france%05d.png -vcodec mpeg4 -y movie.mp4
ffmpeg -f image2 -r 24 -i france%05d.png -vcodec mpeg4 -crf 15 -preset normal -y movie.mp4

rm france*.png
