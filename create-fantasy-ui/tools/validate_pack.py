"""Validate resource-pack metadata and sprite dimensions without game dependencies."""
import json
import struct
from pathlib import Path

PACK = Path(__file__).resolve().parents[1] / "pack"
metadata = json.loads((PACK / "pack.mcmeta").read_text(encoding="utf-8"))
assert metadata["pack"]["pack_format"] == 34, "Expected Minecraft 1.21.1 resource pack format 34"

for metadata_file in PACK.rglob("*.mcmeta"):
    json.loads(metadata_file.read_text(encoding="utf-8"))
for json_file in PACK.rglob("*.json"):
    json.loads(json_file.read_text(encoding="utf-8"))

for image in PACK.rglob("*.png"):
    with image.open("rb") as stream:
        header = stream.read(24)
    assert header[:8] == b"\x89PNG\r\n\x1a\n", f"Invalid PNG signature: {image}"
    width, height = struct.unpack(">II", header[16:24])
    meta_file = image.with_name(image.name + ".mcmeta")
    if meta_file.exists():
        sprite_meta = json.loads(meta_file.read_text(encoding="utf-8"))
        if "gui" in sprite_meta:
            scaling = sprite_meta["gui"]["scaling"]
            assert scaling["type"] == "nine_slice", f"Unexpected GUI scaling: {meta_file}"
            assert scaling["width"] == width and scaling["height"] == height, f"Nine-slice source size mismatch: {meta_file}"

widget_dir = PACK / "assets/minecraft/textures/gui/sprites/widget"
for filename in ("button.png", "button_disabled.png", "button_highlighted.png", "slider.png", "slider_highlighted.png"):
    with (widget_dir / filename).open("rb") as stream:
        stream.seek(16)
        size = struct.unpack(">II", stream.read(8))
    assert size == (200, 20), f"Unexpected vanilla widget sprite size for {filename}: {size}"

print("Resource pack metadata and PNG sprite dimensions are valid.")
