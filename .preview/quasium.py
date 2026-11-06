# Temporary: previews Quasium color candidates from the real 1.12.2 textures (Python port of Recolor.java)
import base64, colorsys, io, json, urllib.request, zipfile
from PIL import Image
def get(url): return urllib.request.urlopen(url).read()
man = json.loads(get("https://piston-meta.mojang.com/mc/game/version_manifest_v2.json"))
ver = json.loads(get(next(v["url"] for v in man["versions"] if v["id"] == "1.12.2")))
jar = zipfile.ZipFile(io.BytesIO(get(ver["downloads"]["client"]["url"])))
def tex(p): return Image.open(io.BytesIO(jar.read("assets/minecraft/textures/" + p))).convert("RGBA")
def luma(r, g, b): return 0.299 * r + 0.587 * g + 0.114 * b
def match(m, h, s, v):
    if m == "GRAY": return s < 0.22
    if m == "CYAN": return s > 0.12 and 140 <= h * 360 <= 220
    return s > 0.15 and v > 0.15
DEFAULT = (0.9, 0.4, 1.0, 0, 0); ITEM = (0.6, 1.2, 1.4, 0, 0); WORN = (0.7, 1.5, 2.2, 0.08, 0.85)
def recolor(src, m, color, sh=DEFAULT, bg=None):
    pct, hl, con, spk, spa = sh
    img = tex(src); px = list(img.getdata()); hit = []; ls = []
    for (r, g, b, a) in px:
        h, s, v = colorsys.rgb_to_hsv(r / 255, g / 255, b / 255)
        ok = a > 0 and match(m, h, s, v); hit.append(ok)
        if ok: ls.append(luma(r, g, b))
    ls.sort(); ref = max(1, ls[min(len(ls) - 1, int(len(ls) * pct))])
    sfrom = ls[min(len(ls) - 1, int(len(ls) * (1 - spk)))] if spk > 0 else 1e9
    bgp = list(tex(bg).getdata()) if bg else None
    tr, tg, tb = (color >> 16) & 255, (color >> 8) & 255, color & 255
    def f(c, k): return max(0, min(255, round(c * k if k <= 1 else c + (255 - c) * min(1, (k - 1) * hl))))
    out = []
    for i, (r, g, b, a) in enumerate(px):
        if hit[i]:
            l = luma(r, g, b); k = (l / ref) ** con
            o = [f(tr, k), f(tg, k), f(tb, k)]
            if l >= sfrom: o = [round(c + (255 - c) * spa) for c in o]
            out.append((o[0], o[1], o[2], a))
        elif bgp and a: out.append(bgp[i])
        else: out.append((r, g, b, a))
    o = Image.new("RGBA", img.size); o.putdata(out); return o
CANDIDATES = [0xF0389B, 0x2EE6D6, 0xFFB52E, 0xE8F4FF]  # hot pink, electric teal, star gold, white-hot
rows = []
for c in CANDIDATES:
    rows.append([recolor("items/iron_ingot.png", "GRAY", c), recolor("items/quartz.png", "GRAY", c),
                 recolor("blocks/diamond_ore.png", "COLORED", c, DEFAULT, "blocks/end_stone.png"),
                 recolor("blocks/iron_block.png", "GRAY", c), recolor("items/iron_sword.png", "GRAY", c),
                 recolor("items/iron_pickaxe.png", "GRAY", c), recolor("items/iron_chestplate.png", "GRAY", c, ITEM),
                 recolor("models/armor/iron_layer_1.png", "GRAY", c, WORN)])
W = 7 * 18 + 66; sheet = Image.new("RGBA", (W, len(rows) * 34), (70, 70, 70, 255))
for y, r in enumerate(rows):
    for x, im in enumerate(r[:7]): sheet.paste(im, (x * 18 + 1, y * 34 + 1), im)
    sheet.paste(r[7], (7 * 18 + 1, y * 34 + 1), r[7])
b = io.BytesIO(); sheet.save(b, "PNG", optimize=True); d = base64.b64encode(b.getvalue()).decode()
for i in range(0, len(d), 4000): print(f"::notice title=q-{i // 4000:02d}::" + d[i:i + 4000])
