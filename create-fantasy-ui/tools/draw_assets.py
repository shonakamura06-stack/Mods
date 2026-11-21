"""Procedural hand-authored pixel/vector texture studies; no AI-generated imagery.

These are original assets. The map motifs (mountains, river, settlements, compass)
are drawn from primitives and deterministic paths, not copied from a reference image.
"""
from math import cos, sin, pi
from pathlib import Path
import random
from PIL import Image, ImageDraw, ImageFont, ImageFilter

ROOT = Path(__file__).resolve().parents[1]
OUT = ROOT / "pack" / "assets" / "create_fantasy_ui" / "textures" / "gui"
OUT.mkdir(parents=True, exist_ok=True)
S = 3
# Logical canvas is 256 px; render at 3x for clean lines, then export at 768 px.
W = H = 256
random.seed(24601)
INK = (63, 47, 33, 255)
INK2 = (92, 68, 43, 255)
BRASS = (163, 111, 48, 255)
BRASS_L = (213, 167, 88, 255)
BRASS_D = (87, 56, 30, 255)
PAPER = (225, 211, 175, 255)


def font(size, serif=False):
    paths = [
        "/usr/share/fonts/truetype/dejavu/DejaVuSerif.ttf" if serif else "/usr/share/fonts/truetype/dejavu/DejaVuSans.ttf",
        "/usr/share/fonts/truetype/liberation2/LiberationSerif-Regular.ttf",
    ]
    for p in paths:
        try:
            return ImageFont.truetype(p, size)
        except OSError:
            pass
    return ImageFont.load_default()


def cubic(p0, p1, p2, p3, steps=120):
    pts = []
    for i in range(steps + 1):
        t = i / steps
        u = 1 - t
        x = u**3*p0[0] + 3*u*u*t*p1[0] + 3*u*t*t*p2[0] + t**3*p3[0]
        y = u**3*p0[1] + 3*u*u*t*p1[1] + 3*u*t*t*p2[1] + t**3*p3[1]*1
        pts.append((x*S, y*S))
    return pts


def draw_gear(draw, cx, cy, radius, teeth=12, fill=BRASS, outline=INK, width=2*S, hole=True):
    pts = []
    for i in range(teeth * 4):
        a = (i / (teeth * 4)) * 2*pi
        phase = i % 4
        r = radius * (1.18 if phase in (1, 2) else 0.94)
        pts.append((cx*S + cos(a)*r*S, cy*S + sin(a)*r*S))
    draw.polygon(pts, fill=fill, outline=outline)
    draw.ellipse((cx*S-radius*.54*S, cy*S-radius*.54*S, cx*S+radius*.54*S, cy*S+radius*.54*S), fill=(232,216,180,255), outline=outline, width=width)
    if hole:
        r = radius*.19*S
        draw.ellipse((cx*S-r,cy*S-r,cx*S+r,cy*S+r), fill=BRASS_D, outline=outline, width=max(1,width//2))


def make_map():
    im = Image.new("RGBA", (W*S,H*S), (34,29,24,255))
    d = ImageDraw.Draw(im)
    # Layered, machined brass frame with dark bevel and fine engraved rules.
    d.rounded_rectangle((8*S,8*S,248*S,248*S), radius=18*S, fill=BRASS_D, outline=(33,28,22,255), width=2*S)
    d.rounded_rectangle((13*S,13*S,243*S,243*S), radius=15*S, fill=BRASS, outline=BRASS_L, width=2*S)
    d.rounded_rectangle((20*S,20*S,236*S,236*S), radius=12*S, fill=(101,68,37,255), outline=(52,39,27,255), width=2*S)
    d.rounded_rectangle((27*S,27*S,229*S,229*S), radius=7*S, fill=PAPER, outline=BRASS_L, width=2*S)
    # Hand-aged paper grain and fibers, seeded for reproducibility.
    for _ in range(6200):
        x = random.randint(31*S,225*S); y = random.randint(31*S,225*S)
        c = random.choice([(121,91,57,24),(255,245,211,36),(81,62,42,18)])
        d.point((x,y), fill=c)
    # Map border and engraved ticks.
    d.rectangle((32*S,32*S,224*S,224*S), outline=(104,77,49,220), width=1*S)
    for x in range(37,221,8):
        d.line((x*S,32*S,x*S,(38 if x%16 else 41)*S), fill=INK2, width=1*S)
        d.line((x*S,218*S,x*S,(224 if x%16 else 221)*S), fill=INK2, width=1*S)
    for y in range(40,218,8):
        d.line((32*S,y*S,(38 if y%16 else 41)*S,y*S), fill=INK2, width=1*S)
        d.line((218*S,y*S,(224 if y%16 else 221)*S,y*S), fill=INK2, width=1*S)

    # River delta: two ink banks, pale water, and short hand-marked ripples.
    river1 = cubic((26,103),(67,81),(94,129),(130,105),70)
    river2 = cubic((130,105),(160,83),(181,116),(229,91),80)
    river3 = cubic((30,167),(76,151),(91,182),(138,159),70)
    for pts in (river1,river2,river3):
        d.line(pts, fill=INK, width=3*S, joint="curve")
        d.line([(x,y+2*S) for x,y in pts], fill=(248,235,201,255), width=2*S, joint="curve")
        d.line([(x,y+5*S) for x,y in pts], fill=(160,132,91,255), width=1*S, joint="curve")
    for x,y in [(48,96),(56,93),(72,99),(151,105),(163,102),(188,104),(58,163),(69,160),(111,166),(169,160)]:
        d.arc((x*S,y*S,(x+7)*S,(y+4)*S), 8, 170, fill=INK2, width=1*S)

    # Mountain ranges with shaded faces and small contour lines.
    def mountain(cx,cy,scale=1):
        w=11*scale; h=13*scale
        pts=[((cx-w)*S,(cy+5*scale)*S),(cx*S,(cy-h)*S),((cx+w)*S,(cy+5*scale)*S)]
        d.polygon(pts, fill=(194,174,132,255), outline=INK, width=1*S)
        d.line((cx*S,(cy-h)*S,(cx+2*scale)*S,(cy+1*scale)*S,(cx+w)*S,(cy+5*scale)*S), fill=INK2, width=1*S)
        d.line((cx*S,(cy-h)*S,(cx-4*scale)*S,(cy+1*scale)*S), fill=(248,237,206,255), width=2*S)
        d.line(((cx-7*scale)*S,(cy+6*scale)*S,(cx-1*scale)*S,(cy+4*scale)*S,(cx+6*scale)*S,(cy+6*scale)*S),fill=INK2,width=1*S)
    for base_x,base_y,n in [(56,70,5),(183,64,4),(151,146,5),(209,184,4)]:
        for i in range(n):
            x=base_x+i*8+random.randint(-2,2); y=base_y+random.randint(-6,6)
            mountain(x,y,random.choice([.7,.8,1]))

    # Forest glyphs: original etched trees, with direction-changing silhouettes.
    def tree(x,y,r=2.4):
        d.line((x*S,(y+4*r)*S,x*S,(y-1*r)*S), fill=INK2,width=1*S)
        for j in range(3):
            yy=(y-1*r+j*2.1*r)*S
            d.line((x*S,yy,(x-2*r)*S,yy+2.0*r*S),fill=INK,width=1*S)
            d.line((x*S,yy,(x+2*r)*S,yy+2.0*r*S),fill=INK,width=1*S)
    forest_centers=[(75,124),(194,133),(53,196),(180,197),(108,77)]
    for fx,fy in forest_centers:
        for _ in range(16):
            x=fx+random.randint(-16,16); y=fy+random.randint(-10,10)
            if 39<x<217 and 42<y<214:
                tree(x,y,random.choice([1.6,2,2.3]))

    # Hill contours and dotted footpaths.
    for box in [(39,111,71,137),(157,119,190,143),(101,185,129,207),(77,48,105,69)]:
        d.arc(tuple(v*S for v in box),15,325,fill=(104,79,52,190),width=1*S)
        b=(box[0]+3,box[1]+3,box[2]-3,box[3]-3)
        d.arc(tuple(v*S for v in b),20,300,fill=(104,79,52,130),width=1*S)
    path=[(43,147),(65,142),(80,151),(99,147),(117,151),(141,139),(163,146),(185,152),(209,143)]
    for (x1,y1),(x2,y2) in zip(path,path[1:]):
        steps=10
        for i in range(steps):
            t=i/steps
            x=x1+(x2-x1)*t; y=y1+(y2-y1)*t
            d.ellipse((x*S-1,y*S-1,x*S+1,y*S+1),fill=(95,72,49,255))

    # Settlement icons: coherent roofs, timber frames, windows, and a mill wheel.
    def house(x,y,s=1,large=False):
        w=(9 if large else 6)*s; h=(6 if large else 4)*s
        d.rectangle(((x-w/2)*S,(y-h/2)*S,(x+w/2)*S,(y+h/2)*S),fill=(194,164,116,255),outline=INK,width=1*S)
        d.polygon([((x-w/2-1*s)*S,(y-h/2)*S),(x*S,(y-h/2-5*s)*S),((x+w/2+1*s)*S,(y-h/2)*S)],fill=(123,78,48,255),outline=INK)
        d.line((x*S,(y-h/2-4*s)*S,x*S,(y+h/2)*S),fill=(112,78,48,255),width=1*S)
        d.rectangle(((x-1*s)*S,(y)*S,(x+1*s)*S,(y+h/2)*S),fill=(78,58,41,255),outline=INK,width=1*S)
        d.point(((x-2*s)*S,(y-h/2+1*s)*S),fill=(246,219,153,255))
    # Central brassworks town, connected to modest hamlets.
    for x,y,s in [(111,111,1),(121,108,1),(131,112,1),(115,119,1),(127,119,1),(122,101,1.2)]: house(x,y,s,large=(x==122 and y==101))
    # tower and clock face
    d.rectangle((119*S,89*S,124*S,102*S),fill=(173,143,96,255),outline=INK,width=1*S)
    d.polygon([(118*S,89*S),(121.5*S,82*S),(125*S,89*S)],fill=(118,72,45,255),outline=INK)
    d.ellipse((120*S,92*S,123*S,95*S),fill=(232,216,180,255),outline=INK,width=1*S)
    d.line((121.5*S,93.5*S,121.5*S,92*S),fill=INK,width=1*S)
    # small outer settlements and isolated cabins
    for x,y in [(46,181),(65,187),(203,111),(211,113),(149,201),(88,91)]:
        house(x,y,.9)
    # Mill icon: gear and mechanical waterwheel as a hint of Create, kept cartographic.
    draw_gear(d, 150, 188, 5.5, teeth=8, fill=(178,128,58,255), width=1*S)
    d.line((145*S,194*S,155*S,194*S),fill=INK,width=1*S)
    # Dotted expedition route and flag markers.
    route=[(53,174),(73,171),(91,177),(107,169),(132,173),(150,161),(173,169),(198,158)]
    for (x1,y1),(x2,y2) in zip(route,route[1:]):
        for i in range(0,12,2):
            t=i/12; x=x1+(x2-x1)*t; y=y1+(y2-y1)*t
            d.ellipse((x*S-1,y*S-1,x*S+1,y*S+1),fill=(109,71,42,255))
    for x,y in [(53,174),(198,158)]:
        d.line((x*S,y*S,(x+2)*S,(y-8)*S),fill=INK,width=1*S)
        d.polygon([(x*S,(y-8)*S),((x+6)*S,(y-6)*S),((x+2)*S,(y-4)*S)],fill=(147,57,39,255),outline=INK)

    # Compass rose in a brass roundel.
    cx,cy=205,52
    d.ellipse(((cx-13)*S,(cy-13)*S,(cx+13)*S,(cy+13)*S),fill=(193,155,93,255),outline=INK,width=1*S)
    d.ellipse(((cx-10)*S,(cy-10)*S,(cx+10)*S,(cy+10)*S),fill=(231,216,180,255),outline=BRASS_D,width=1*S)
    for i in range(8):
        a=i*pi/4-pi/2; r1=1.5; r2=9
        tip=(cx+cos(a)*r2,cy+sin(a)*r2)
        left=(cx+cos(a+2.45)*r1,cy+sin(a+2.45)*r1)
        right=(cx+cos(a-2.45)*r1,cy+sin(a-2.45)*r1)
        d.polygon([(cx*S,cy*S),(tip[0]*S,tip[1]*S),(left[0]*S,left[1]*S)],fill=BRASS_D if i%2==0 else (244,233,207,255),outline=INK)
        d.polygon([(cx*S,cy*S),(tip[0]*S,tip[1]*S),(right[0]*S,right[1]*S)],fill=(201,76,48,255) if i==0 else (114,82,53,255),outline=INK)
    d.ellipse((cx*S-1*S,cy*S-1*S,cx*S+1*S,cy*S+1*S),fill=BRASS_L,outline=INK,width=1*S)
    d.text(((cx-3)*S,(cy-20)*S),"N",font=font(5*S,True),fill=INK,stroke_width=0)
    # Engraved title ribbon.
    d.rounded_rectangle((91*S,210*S,169*S,220*S),radius=2*S,fill=(180,137,73,255),outline=INK,width=1*S)
    d.text((98*S,211*S),"THE BRASS MARCHES",font=font(4*S,True),fill=(47,37,27,255))
    # Four corner fasteners and hairline trim.
    for x,y in [(17,17),(239,17),(17,239),(239,239)]:
        d.ellipse(((x-2)*S,(y-2)*S,(x+2)*S,(y+2)*S),fill=BRASS_L,outline=BRASS_D,width=1*S)
        d.line(((x-1)*S,y*S,(x+1)*S,y*S),fill=BRASS_D,width=1*S)
    # Downsample with high quality for clean hand-ink-like strokes.
    im = im.resize((768,768), Image.Resampling.LANCZOS)
    im.save(OUT / "atlas_map_study.png", optimize=True)


def make_panel():
    size=384
    im=Image.new("RGBA",(size,size),(0,0,0,0)); d=ImageDraw.Draw(im)
    # Tileable-looking UI panel study, defined as a nine-slice source later.
    d.rounded_rectangle((8,8,376,376),radius=27,fill=(75,51,30,255),outline=(29,25,21,255),width=5)
    d.rounded_rectangle((16,16,368,368),radius=23,fill=(181,127,58,255),outline=(227,181,101,255),width=4)
    d.rounded_rectangle((27,27,357,357),radius=17,fill=(108,75,42,255),outline=(66,48,32,255),width=4)
    d.rounded_rectangle((36,36,348,348),radius=13,fill=(225,211,175,255),outline=(224,180,95,255),width=3)
    # etched inset lines and parchment flecks
    d.rounded_rectangle((43,43,341,341),radius=9,outline=(134,99,57,255),width=2)
    for _ in range(3000):
        x=random.randint(45,339); y=random.randint(45,339)
        col=random.choice([(107,76,48,20),(255,248,218,40),(80,59,37,15)])
        d.point((x,y),fill=col)
    for x,y in [(22,22),(362,22),(22,362),(362,362)]:
        d.ellipse((x-7,y-7,x+7,y+7),fill=(217,173,89,255),outline=(70,49,29,255),width=3)
        d.ellipse((x-2,y-2,x+2,y+2),fill=(95,62,31,255))
    draw_gear(d,34,192,15,teeth=10,fill=(191,140,67,255),width=2)
    draw_gear(d,350,192,15,teeth=10,fill=(191,140,67,255),width=2)
    for y in (72,312):
        d.line((60,y,324,y),fill=(142,101,55,130),width=1)
    im.save(OUT / "brass_parchment_panel.png", optimize=True)


def make_gear_sheet():
    frames=16; f=64
    sheet=Image.new("RGBA",(f,frames*f),(0,0,0,0))
    for k in range(frames):
        layer=Image.new("RGBA",(f,f),(0,0,0,0)); d=ImageDraw.Draw(layer)
        cx=cy=32; r=21; teeth=12
        rotation=2*pi*k/frames
        pts=[]
        for i in range(teeth*4):
            a=rotation+i*2*pi/(teeth*4)
            rr=r*(1.16 if i%4 in (1,2) else .94)
            pts.append((cx+cos(a)*rr,cy+sin(a)*rr))
        d.polygon(pts,fill=(181,124,49,255),outline=(58,40,26,255))
        d.ellipse((cx-12,cy-12,cx+12,cy+12),fill=(222,205,166,255),outline=(58,40,26,255),width=2)
        d.ellipse((cx-4,cy-4,cx+4,cy+4),fill=(117,78,35,255),outline=(56,39,25,255),width=2)
        d.ellipse((cx-1,cy-1,cx+1,cy+1),fill=(220,170,77,255))
        sheet.alpha_composite(layer,(0,k*f))
    sheet.save(OUT / "brass_gear_animated.png", optimize=True)
    (OUT / "brass_gear_animated.png.mcmeta").write_text('''{\n  "animation": {\n    "frametime": 3,\n    "interpolate": false\n  }\n}\n''', encoding="utf-8")


if __name__ == "__main__":
    make_map(); make_panel(); make_gear_sheet()
    print(f"Wrote original studies to {OUT}")
