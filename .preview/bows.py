import base64, colorsys, io, json, urllib.request, zipfile
from PIL import Image
def get(url): return urllib.request.urlopen(url).read()
man = json.loads(get("https://piston-meta.mojang.com/mc/game/version_manifest_v2.json"))
ver = json.loads(get(next(v["url"] for v in man["versions"] if v["id"] == "1.12.2")))
jar = zipfile.ZipFile(io.BytesIO(get(ver["downloads"]["client"]["url"])))
def tex(p): return Image.open(io.BytesIO(jar.read("assets/minecraft/textures/" + p))).convert("RGBA")
def luma(r, g, b): return 0.299 * r + 0.587 * g + 0.114 * b
def recolor(src, color, skip):
    img = tex(src); w = img.width; px = list(img.getdata()); hit = []; ls = []
    for i, (r, g, b, a) in enumerate(px):
        h, s, v = colorsys.rgb_to_hsv(r / 255, g / 255, b / 255)
        x, y = i % w, i // w
        ok = a > 0 and not (skip and (x == y or x == y + 1)) and s > 0.35 and 20 <= h * 360 <= 55
        hit.append(ok)
        if ok: ls.append(luma(r, g, b))
    ls.sort(); ref = max(1, ls[min(len(ls) - 1, int(len(ls) * 0.9))])
    tr, tg, tb = (color >> 16) & 255, (color >> 8) & 255, color & 255
    def sh(c, k): return max(0, min(255, round(c * k if k <= 1 else c + (255 - c) * min(1, (k - 1) * 0.4))))
    out = [(sh(tr, luma(r, g, b) / ref), sh(tg, luma(r, g, b) / ref), sh(tb, luma(r, g, b) / ref), a) if hit[i] else (r, g, b, a)
           for i, (r, g, b, a) in enumerate(px)]
    o = Image.new("RGBA", img.size); o.putdata(out); return o
rows = [[tex(f"items/bow_{n}.png") for n in ["standby", "pulling_0", "pulling_1", "pulling_2"]]]
for c in [0xC4A46C, 0x78829A, 0x8C3CC8]:
    rows.append([recolor("items/bow_standby.png", c, False)] + [recolor(f"items/bow_pulling_{n}.png", c, True) for n in range(3)])
sheet = Image.new("RGBA", (4 * 18, len(rows) * 18), (70, 70, 70, 255))
for y, r in enumerate(rows):
    for x, im in enumerate(r): sheet.paste(im, (x * 18 + 1, y * 18 + 1), im)
b = io.BytesIO(); sheet.save(b, "PNG", optimize=True); d = base64.b64encode(b.getvalue()).decode()
for i in range(0, len(d), 4000): print(f"::notice title=bows-{i // 4000:02d}::" + d[i:i + 4000])
