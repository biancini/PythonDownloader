# create an empty image with PIL and put pixels inside
from PIL import Image

# use a (r, g, b) tuple to represent colors
red = (255,0,0)
#white = (255,255,255)

# create a new 256x256 pixel image surface
# make the background white (default bg=black)
#img = Image.new("RGB", [256,256], white)
img = Image.open("france.png")

x = 50
y = 40
# put a red pixel at point (x, y)
img.putpixel((x, y), red)
img.putpixel((x+1, y), red)
img.putpixel((x, y+1), red)
img.putpixel((x+1, y+1), red)

# save the image
img.save("output/test1.png")
# optionally look at the image you have created
img.show()
