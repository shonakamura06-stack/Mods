"""Draw original, deterministic brass-and-parchment Minecraft GUI sprites."""
from pathlib import Path
from math import cos, pi, sin
from PIL import Image, ImageDraw
import random

ROOT = Path(__file__).resolve().parents[1] / "pack" / "assets" / "minecraft" / "textures" / "gui" / "sprites" / "widget"
ROOT.mkdir(parents=True, exist_ok=True)
random.seed(81721)

PALETTES = {
    "button": ((225, 211, 175, 255), (82, 57, 35, 255), (190, 143, 65, 255), (113, 77, 43, 255)),
    "button_highlighted": ((242, 225, 185, 255), (91, 62, 35, 255), (238, 190, 84, 255), (133, 86, 40, 255)),
    "button_disabled": ((164, 153, 128, 255), (87, 78, 63, 255), (139, 123, 94, 255), (106, 91, 69, 255)),
    "slider": ((186, 153, 105, 255), (70, 48, 31, 255), (196, 152, 75, 255), (113, 77, 43, 255)),
    "slider_highlighted": ((205, 169, 111, 255), (76, 49, 28, 255), (239, 190, 80, 255), (137, 86, 37, 255)),
}


def bevel_sprite(name, fill, dark, gold, ink, size=(200, 20), border=3):
    width, height = size
    image = Image.new("RGBA", size, (0, 0, 0, 0))
    draw = ImageDraw.Draw(image)
    # A restrained bevel keeps the text legible and survives GUI nine-slice stretching.
    draw.rectangle((0, 0, width - 1, height - 1), fill=dark)
    draw.line((1, 1, width - 2, 1), fill=gold)
    draw.line((1, 1, 1, height - 2), fill=gold)
    draw.rectangle((border, border, width - border - 1, height - border - 1), fill=fill)
    draw.line((border + 1, border, width - border - 2, border), fill=(255, 239, 198, 255))
    draw.line((border, border + 1, border, height - border - 2), fill=gold)
    draw.line((border + 1, height - border - 1, width - border - 2, height - border - 1), fill=ink)
    draw.line((width - border - 1, border + 1, width - border - 1, height - border - 2), fill=ink)
    # Small hand-drawn fasteners sit inside the fixed nine-slice corners.
    for x in (2, width - 3):
        for y in (2, height - 3):
            draw.point((x, y), fill=(242, 205, 123, 255))
    if name.startswith("button"):
        # Small original gear seals at the ends keep the center clear for text.
        for cx in (10, width - 11):
            points = []
            for index in range(16):
                angle = index * pi / 8
                radius = 4.2 if index % 2 == 0 else 3.0
                points.append((round(cx + radius * cos(angle)),
                               round(height / 2 + radius * sin(angle))))
            draw.polygon(points, fill=gold, outline=dark)
            draw.ellipse((cx - 1, height // 2 - 1, cx + 1, height // 2 + 1), fill=ink)
        # Sparse deterministic fibers make the center parchment-like without visual noise.
        for _ in range(95):
            x = random.randint(border + 2, width - border - 3)
            y = random.randint(border + 2, height - border - 3)
            draw.point((x, y), fill=(157, 123, 78, 255) if random.random() < 0.55 else (244, 231, 198, 255))
    image.save(ROOT / f"{name}.png", optimize=True)
    (ROOT / f"{name}.png.mcmeta").write_text(
        '{\n  "gui": {\n    "scaling": {\n      "type": "nine_slice",\n'
        f'      "width": {width},\n      "height": {height},\n'
        f'      "border": {border}\n    }}\n  }}\n}}\n', encoding="utf-8"
    )


def slider_handle(name, fill, dark, gold, ink):
    width, height = 8, 20
    image = Image.new("RGBA", (width, height), (0, 0, 0, 0))
    draw = ImageDraw.Draw(image)
    draw.rectangle((1, 1, 6, 18), fill=dark)
    draw.rectangle((2, 2, 5, 17), fill=fill)
    draw.line((2, 2, 5, 2), fill=gold)
    draw.line((2, 3, 2, 16), fill=gold)
    draw.line((3, 17, 5, 17), fill=ink)
    draw.point((3, 7), fill=(244, 220, 165, 255))
    draw.point((4, 11), fill=(116, 78, 39, 255))
    image.save(ROOT / f"{name}.png", optimize=True)
    (ROOT / f"{name}.png.mcmeta").write_text(
        '{\n  "gui": {\n    "scaling": {\n      "type": "nine_slice",\n'
        '      "width": 8,\n      "height": 20,\n'
        '      "border": {"left": 2, "top": 2, "right": 2, "bottom": 3}\n'
        '    }\n  }\n}\n', encoding="utf-8"
    )


for sprite, colors in PALETTES.items():
    bevel_sprite(sprite, *colors, border=3)
slider_handle("slider_handle", (210, 175, 106, 255), (77, 52, 31, 255), (224, 174, 75, 255), (91, 62, 34, 255))
slider_handle("slider_handle_highlighted", (240, 207, 131, 255), (77, 52, 31, 255), (255, 220, 116, 255), (91, 62, 34, 255))
print(f"Wrote original widget sprites to {ROOT}")
