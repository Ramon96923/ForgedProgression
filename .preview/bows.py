import base64, collections, io, json, sys, urllib.request, zipfile
from PIL import Image
def get(url): return urllib.request.urlopen(url).read()
man = json.loads(get("https://piston-meta.mojang.com/mc/game/version_manifest_v2.json"))
ver = json.loads(get(next(v["url"] for v in man["versions"] if v["id"] == "1.12.2")))
jar = zipfile.ZipFile(io.BytesIO(get(ver["downloads"]["client"]["url"])))
names = ["bow_standby", "bow_pulling_0", "bow_pulling_1", "bow_pulling_2"]
ims = [Image.open(io.BytesIO(jar.read(f"assets/minecraft/textures/items/{n}.png"))).convert("RGBA") for n in names]
# per-pixel dump: one line per texture, '.' transparent, else palette index
pal = {}
for n, im in zip(names, ims):
    rows = []
    for y in range(16):
        row = ""
        for x in range(16):
            p = im.getpixel((x, y))
            if p[3] == 0: row += "."
            else:
                if p not in pal: pal[p] = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"[len(pal)]
                row += pal[p]
        rows.append(row)
    print(f"::notice title=bow-{n}::" + "|".join(rows))
print("::notice title=bow-palette::" + " ".join(f"{v}={k[0]},{k[1]},{k[2]},{k[3]}" for k, v in pal.items()))
