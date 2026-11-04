# Temporary: renders recolor previews from the real 1.12.2 textures (Python port of Recolor.java)
import base64, colorsys, io, json, urllib.request, zipfile
from PIL import Image

def get(url): return urllib.request.urlopen(url).read()
man = json.loads(get("https://piston-meta.mojang.com/mc/game/version_manifest_v2.json"))
ver = json.loads(get(next(v["url"] for v in man["versions"] if v["id"] == "1.12.2")))
jar = zipfile.ZipFile(io.BytesIO(get(ver["downloads"]["client"]["url"])))
def tex(p): return Image.open(io.BytesIO(jar.read("assets/minecraft/textures/" + p))).convert("RGBA")

def match(m, h, s, v):
    if m == "GRAY": return s < 0.22
    if m == "CYAN": return s > 0.12 and 140 <= h * 360 <= 220
    return s > 0.15 and v > 0.15
def luma(r, g, b): return 0.299 * r + 0.587 * g + 0.114 * b
PCT, HL = 0.5, 1.3
def shade(c, ratio):
    v = c * ratio if ratio <= 1 else c + (255 - c) * min(1.0, (ratio - 1) * HL)
    return max(0, min(255, round(v)))
def recolor(src, m, color, bg=None):
    img = tex(src); px = list(img.getdata()); hit = []; ls = []
    for (r, g, b, a) in px:
        h, sa, v = colorsys.rgb_to_hsv(r / 255, g / 255, b / 255)
        ok = a > 0 and match(m, h, sa, v); hit.append(ok)
        if ok: ls.append(luma(r, g, b))
    ls.sort(); mean = max(1, ls[min(len(ls) - 1, int(len(ls) * PCT))]) if ls else 128
    bgpx = list(tex(bg).getdata()) if bg else None
    tr, tg, tb = (color >> 16) & 255, (color >> 8) & 255, color & 255
    out = []
    for i, (r, g, b, a) in enumerate(px):
        if hit[i]:
            k = luma(r, g, b) / mean; out.append((shade(tr, k), shade(tg, k), shade(tb, k), a))
        elif bgpx and a: out.append(bgpx[i])
        else: out.append((r, g, b, a))
    o = Image.new("RGBA", img.size); o.putdata(out); return o

C = {"copper": 0xE07C48, "tin": 0xB4C3D7, "bronze": 0xCD9646, "steel": 0x78829A,
     "ruby": 0xDC283C, "uranium": 0x6EDC3C, "plutonium": 0xAA46DC}
def build():
  rows = []
  for m in ["copper", "tin", "bronze", "steel", "ruby", "uranium", "plutonium"]:
      gem = m == "ruby"; base = "diamond" if gem else "iron"; mt = "CYAN" if gem else "GRAY"
      row = [tex(f"items/{base}_sword.png"), recolor("items/diamond.png" if gem else "items/iron_ingot.png", mt, C[m]),
             recolor(f"blocks/{base}_block.png", mt, C[m])]
      ore = "blocks/diamond_ore.png" if gem else "blocks/iron_ore.png"
      row.append(recolor(ore, "COLORED", C[m], "blocks/end_stone.png" if m == "plutonium" else None))
      if m != "tin":
          for p in ["pickaxe", "axe", "shovel", "hoe", "sword", "helmet", "chestplate", "leggings", "boots"]:
              row.append(recolor(f"items/{base}_{p}.png", mt, C[m]))
      rows.append(row[1:])
  rows.append([recolor("items/gunpowder.png", "GRAY", 0x5A606E)])
  sheet = Image.new("RGBA", (13 * 18, len(rows) * 18), (70, 70, 70, 255))
  for y, row in enumerate(rows):
      for x, im in enumerate(row): sheet.paste(im, (x * 18 + 1, y * 18 + 1), im)

  return sheet
sheets = []
for PCT, HL in [(0.8, 0.6), (0.9, 0.4), (0.95, 0.3)]:
    sheets.append(build())
sheet = Image.new("RGBA", (sheets[0].width * 3 + 20, sheets[0].height), (30, 30, 30, 255))
for i, sh in enumerate(sheets): sheet.paste(sh, (i * (sh.width + 10), 0))
PCT, HL = 0.9, 0.4
armor = Image.new("RGBA", (64 * 3, 32 * 2), (70, 70, 70, 255))
for i, m in enumerate(["copper", "ruby", "plutonium"]):
    base, mt = ("diamond", "CYAN") if m == "ruby" else ("iron", "GRAY")
    for l in (1, 2):
        im = recolor(f"models/armor/{base}_layer_{l}.png", mt, C[m]); armor.paste(im, (i * 64, (l - 1) * 32), im)
import sys
start = int(sys.argv[1]); chunks = []
for name, im in [("sheet", sheet), ("armor", armor)]:
    b = io.BytesIO(); im.save(b, "PNG", optimize=True); d = base64.b64encode(b.getvalue()).decode()
    chunks += [(f"{name}-{i // 4000:02d}", d[i:i + 4000]) for i in range(0, len(d), 4000)]
for t, c in chunks[start:start + 9]:
    print(f"::notice title={t}::" + c)
print("total chunks", len(chunks))
