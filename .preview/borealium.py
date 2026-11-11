# Temporary: previews Borealium aurora-gradient candidates from the real 1.12.2 textures (Python port of Recolor.java)
import base64, colorsys, io, json, urllib.request, zipfile
from PIL import Image
def get(url): return urllib.request.urlopen(url).read()
man = json.loads(get("https://piston-meta.mojang.com/mc/game/version_manifest_v2.json"))
ver = json.loads(get(next(v["url"] for v in man["versions"] if v["id"] == "1.12.2")))
jar = zipfile.ZipFile(io.BytesIO(get(ver["downloads"]["client"]["url"])))
def tex(p): return Image.open(io.BytesIO(jar.read("assets/minecraft/textures/" + p))).convert("RGBA")
def luma(r, g, b): return 0.299 * r + 0.587 * g + 0.114 * b
DEFAULT = (0.9, 0.4, 1.0, 0, 0); ITEM = (0.6, 1.2, 1.4, 0, 0); WORN = (0.7, 1.5, 2.2, 0.08, 0.85)
def lerp(a, b, t): return tuple(round(a[i] + (b[i] - a[i]) * t) for i in range(3))
def rgb(c): return ((c >> 16) & 255, (c >> 8) & 255, c & 255)
def recolor(src, c1, c2, sh=DEFAULT):
    pct, hl, con, spk, spa = sh
    img = tex(src); w, h = img.size; px = list(img.getdata()); hit = []; ls = []
    for (r, g, b, a) in px:
        hh, s, v = colorsys.rgb_to_hsv(r / 255, g / 255, b / 255)
        ok = a > 0 and s < 0.22; hit.append(ok)
        if ok: ls.append(luma(r, g, b))
    ls.sort(); ref = max(1, ls[min(len(ls) - 1, int(len(ls) * pct))])
    sfrom = ls[min(len(ls) - 1, int(len(ls) * (1 - spk)))] if spk > 0 else 1e9
    def f(c, k): return max(0, min(255, round(c * k if k <= 1 else c + (255 - c) * min(1, (k - 1) * hl))))
    out = []
    for i, (r, g, b, a) in enumerate(px):
        if hit[i]:
            x, y = i % w, i // w
            t = (x + (h - 1 - y)) / max(1, (w - 1) + (h - 1))  # bottom-left -> top-right
            tr, tg, tb = lerp(rgb(c1), rgb(c2), t)
            l = luma(r, g, b); k = (l / ref) ** con
            o = [f(tr, k), f(tg, k), f(tb, k)]
            if l >= sfrom: o = [round(c + (255 - c) * spa) for c in o]
            out.append((o[0], o[1], o[2], a))
        else: out.append((r, g, b, a))
    o = Image.new("RGBA", img.size); o.putdata(out); return o
CANDIDATES = [(0x2EF0A0, 0x8A5CFF), (0x2EE6D6, 0x4CFF6A), (0x5CFFB0, 0xFF5CD6)]
rows = []
for c1, c2 in CANDIDATES:
    rows.append([recolor("items/iron_ingot.png", c1, c2), recolor("blocks/iron_block.png", c1, c2),
                 recolor("items/iron_sword.png", c1, c2), recolor("items/iron_pickaxe.png", c1, c2),
                 recolor("items/iron_chestplate.png", c1, c2, ITEM), recolor("models/armor/iron_layer_1.png", c1, c2, WORN)])
sheet = Image.new("RGBA", (5 * 18 + 66, len(rows) * 34), (70, 70, 70, 255))
for y, r in enumerate(rows):
    for x, im in enumerate(r[:5]): sheet.paste(im, (x * 18 + 1, y * 34 + 1), im)
    sheet.paste(r[5], (5 * 18 + 1, y * 34 + 1), r[5])
b = io.BytesIO(); sheet.save(b, "PNG", optimize=True); d = base64.b64encode(b.getvalue()).decode()
for i in range(0, len(d), 4000): print(f"::notice title=b-{i // 4000:02d}::" + d[i:i + 4000])
