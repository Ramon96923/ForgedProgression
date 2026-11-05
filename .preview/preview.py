# Temporary: renders recolor previews from the real 1.12.2 textures (Python port of Recolor.java)
import base64, colorsys, io, json, sys, urllib.request, zipfile
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
def recolor(src, m, color, pct=0.9, hl=0.4, gamma=1.0):
    img = tex(src); px = list(img.getdata()); hit = []; ls = []
    for (r, g, b, a) in px:
        h, sa, v = colorsys.rgb_to_hsv(r / 255, g / 255, b / 255)
        ok = a > 0 and match(m, h, sa, v); hit.append(ok)
        if ok: ls.append(luma(r, g, b))
    ls.sort(); ref = max(1, ls[min(len(ls) - 1, int(len(ls) * pct))]) if ls else 128
    lo = ls[0] if ls else 0
    tr, tg, tb = (color >> 16) & 255, (color >> 8) & 255, color & 255
    def shade(c, k):
        v = c * k if k <= 1 else c + (255 - c) * min(1.0, (k - 1) * hl)
        return max(0, min(255, round(v)))
    out = []
    for i, (r, g, b, a) in enumerate(px):
        if hit[i]:
            k = luma(r, g, b) / ref
            if gamma != 1.0: k = k ** gamma  # >1 stretches contrast around the reference
            out.append((shade(tr, k), shade(tg, k), shade(tb, k), a))
        else: out.append((r, g, b, a))
    o = Image.new("RGBA", img.size); o.putdata(out); return o

def grid(rows, cell, gap=2, bg=(70, 70, 70, 255)):
    W = max(sum(im.width + gap for im in r) for r in rows); H = sum(max(im.height for im in r) + gap for r in rows)
    s = Image.new("RGBA", (W, H), bg); y = 0
    for r in rows:
        x = 0
        for im in r: s.paste(im, (x, y), im); x += im.width + gap
        y += max(im.height for im in r) + gap
    return s

C = {"copper": 0xE07C48, "bronze": 0xCD9646, "steel": 0x78829A, "uranium": 0x6EDC3C, "plutonium": 0xAA46DC}
VARIANTS = [("now", 0.9, 0.4, 1.0), ("A", 0.5, 1.0, 1.0), ("B", 0.6, 1.2, 1.4), ("C", 0.75, 1.0, 1.8)]
# armor worn layers: one row per variant, columns = vanilla iron ref + metals
rows = []
for name, p, h, g in VARIANTS:
    row = [tex("models/armor/iron_layer_1.png"), tex("models/armor/diamond_layer_1.png")]
    row += [recolor("models/armor/iron_layer_1.png", "GRAY", c, p, h, g) for c in C.values()]
    rows.append(row)
armor = grid(rows, 64, 4)
# armor items: same variants
irows = []
for name, p, h, g in VARIANTS:
    r = []
    for c in C.values():
        r += [recolor(f"items/iron_{x}.png", "GRAY", c, p, h, g) for x in ["helmet", "chestplate", "leggings", "boots"]]
    irows.append(r)
items = grid(irows, 16, 2)
# neutronium candidates: ingot, block, sword, pickaxe, chestplate item, worn layer 1 (variant B)
N = [0x2E3440, 0x3C4660, 0x1E2A4A, 0xDDE6F0]
nrows = []
for c in N:
    r = [recolor("items/iron_ingot.png", "GRAY", c), recolor("blocks/iron_block.png", "GRAY", c),
         recolor("items/iron_sword.png", "GRAY", c), recolor("items/iron_pickaxe.png", "GRAY", c),
         recolor("items/iron_chestplate.png", "GRAY", c, 0.6, 1.2, 1.4)]
    nrows.append(r)
neut = grid(nrows, 16, 2)
nl = grid([[recolor("models/armor/iron_layer_1.png", "GRAY", c, 0.6, 1.2, 1.4) for c in N]], 64, 4)

start = int(sys.argv[1]); chunks = []
for name, im in [("armor", armor), ("items", items), ("neut", neut), ("nl", nl)]:
    b = io.BytesIO(); im.save(b, "PNG", optimize=True); d = base64.b64encode(b.getvalue()).decode()
    chunks += [(f"{name}-{i // 4000:02d}", d[i:i + 4000]) for i in range(0, len(d), 4000)]
for t, c in chunks[start:start + 9]:
    print(f"::notice title={t}::" + c)
print("total chunks", len(chunks))
