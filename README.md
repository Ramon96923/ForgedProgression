# Forged Progression (Forge 1.12.2)

Adds copper, tin, bronze, steel, ruby, uranium, plutonium and neutronium, with tools and armor for each
tier, and raises the pickaxe tier needed for vanilla ores so every material is a required step.

## Progression

| Tier | Pickaxe               | Unlocks                                         |
|------|-----------------------|-------------------------------------------------|
| 0    | Wood                  | Stone, coal                                     |
| 1    | Stone / **Copper**    | Copper ore, tin ore                             |
| 2    | **Bronze** / Iron     | Iron, gold, redstone, lapis                     |
| 3    | **Steel** / Diamond   | Diamond, emerald, **ruby ore**                  |
| 4    | **Ruby**              | **Uranium ore**                                 |
| 5    | **Uranium**           | **Plutonium ore** (in the End)                  |
| 6    | **Plutonium**         | Everything                                      |
| 7    | **Neutronium**        | Top tier (crafted, no ore)                      |

Stone pickaxes can no longer harvest iron, and iron pickaxes can no longer harvest diamonds.

## Materials

- **Copper ore**: Overworld y 30–96, common. Smelt it into copper ingots.
- **Tin ore**: Overworld y 10–64, less common. Smelt it into tin ingots.
- **Bronze**: craft 3 copper ingots + 1 tin ingot (shapeless) into 4 bronze ingots.
- **Steel**: craft 1 iron ingot + 2 coal/charcoal (shapeless) into steel blend, then smelt it into a steel ingot.
- **Ruby ore**: Overworld y 4–24, rare. Drops a ruby directly, like diamond ore (Fortune works).
- **Uranium ore**: Overworld y 4–18, very rare. Smelt it into uranium ingots.
- **Plutonium ore**: generates in **End stone**. Smelt it into plutonium ingots.
- **Neutronium**: craft 4 plutonium (corners) + 4 uranium (sides) + 1 iron (center) into 1 neutronium ingot.
- Every material has a storage block (9 ↔ 1), usable as a beacon base.

## Stats

| Material  | Harvest | Durability | Speed | Sword dmg | Armor (helm/chest/legs/boots) | Toughness |
|-----------|---------|------------|-------|-----------|-------------------------------|-----------|
| Copper    | 1       | 200        | 5.0   | 5.5       | 1 / 4 / 3 / 1                 | 0         |
| Bronze    | 2       | 350        | 6.0   | 6         | 2 / 5 / 4 / 2                 | 0         |
| Steel     | 3       | 900        | 7.0   | 6.5       | 3 / 7 / 6 / 2                 | 1         |
| Ruby      | 4       | 1800       | 9.0   | 7.5       | 3 / 8 / 6 / 3                 | 2.5       |
| Uranium   | 5       | 2600       | 10.0  | 8         | 3 / 9 / 7 / 3                 | 3         |
| Plutonium | 6       | 3500       | 12.0  | 9         | 4 / 9 / 7 / 4                 | 4         |
| Neutronium| 7       | 5000       | 14.0  | 10        | 4 / 10 / 8 / 4                | 5         |
| *Diamond (vanilla)* | 3 | 1561     | 8.0   | 7         | 3 / 8 / 6 / 3                 | 2         |

Tools and armor use vanilla crafting shapes and repair in an anvil with their ingot or gem.
Everything is registered in the Ore Dictionary (`oreCopper`, `ingotBronze`, `gemRuby`, `ingotUranium`, ...),
so recipes accept the same metals from other mods.

## Textures

There are no texture files. Every texture is built when the game loads by recoloring the matching
vanilla one: copper tools are recolored iron tools, ruby gear is recolored diamond gear, the ores are
recolored iron/diamond ore, and plutonium ore puts the spots on End stone. Resource packs that change
the vanilla iron or diamond textures change these too. The colors live in `client/ClientTextures.java`.

## Bosses

The Ender Dragon and the Wither are scaled up to keep pace with the new gear:

| Boss   | Health          | Damage dealt | Damage taken | Extra                                                       |
|--------|-----------------|--------------|--------------|-------------------------------------------------------------|
| Dragon | 500 (×2.5)      | ×2           | ×0.75        | Its hits and breath give 4 s of Wither II                   |
| Wither | 900 (×3)        | ×2           | ×0.75        | Below half health, summons 3 wither skeletons every 25 s    |

All of it can be changed or turned off in the `bosses` config section.

## Config

`config/forgedprogression.cfg` (also editable in-game via Mods → Config):
- `progression`: turn gating off, or change the required level of each vanilla ore.
- `worldgen`: enable/disable, vein size, veins per chunk and height range for each ore.
- `bosses`: health, damage and extras for the Ender Dragon and Wither.

## Getting the jar

Every push is built by GitHub Actions. Open the **Actions** tab, click the latest **Build** run and
download **forgedprogression-1.12.2** under Artifacts. The `.jar` inside goes in your `mods` folder.

### Building locally

1. Download the **1.12.2 MDK** (14.23.5.2860) from files.minecraftforge.net and unzip it.
2. Copy this repo's `src/`, `build.gradle` and `settings.gradle` into it (keep the MDK's `gradlew` and `gradle/`).
3. With Java 8 installed, run `gradlew build`.
4. The jar is in `build/libs/`.
