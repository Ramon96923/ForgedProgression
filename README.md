# Forged Progression (Forge 1.12.2)

Adds copper, tin, bronze, steel, ruby, uranium, plutonium, neutronium and quasium, with tools and armor for each
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
| 7    | **Neutronium**        | **Quasium ore** (outer End islands)             |
| 8    | **Quasium**           | Top tier                                        |

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
- **Quasium ore**: only in End stone on the **outer End islands** (1000+ blocks from the center, past the
  gateway), rare. Needs a neutronium pickaxe. Smelt it into quasium shards.
- **Quasium ingot**: 4 quasium shards (corners) + 4 neutronium ingots (sides) + 1 end crystal (center).
  That's 16 plutonium, 16 uranium and an end crystal per ingot.
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
| Quasium   | 8       | 7000       | 16.0  | 11.5      | 5 / 11 / 9 / 5 (30, the max)  | 7         |
| *Diamond (vanilla)* | 3 | 1561     | 8.0   | 7         | 3 / 8 / 6 / 3                 | 2         |

Tools and armor use vanilla crafting shapes and repair in an anvil with their ingot or gem.
Everything is registered in the Ore Dictionary (`oreCopper`, `ingotBronze`, `gemRuby`, `ingotUranium`, ...),
so recipes accept the same metals from other mods.

## Textures

There are no texture files. Every texture is built when the game loads by recoloring the matching
vanilla one: copper tools are recolored iron tools, ruby gear is recolored diamond gear, the ores are
recolored iron/diamond ore, and plutonium ore puts the spots on End stone. Resource packs that change
the vanilla iron or diamond textures change these too. The colors live in `client/ClientTextures.java`.

## Bows

| Bow          | When       | Recipe                                               | Draw time | Arrow damage* | Durability | Extra                |
|--------------|------------|------------------------------------------------------|-----------|---------------|------------|----------------------|
| Hunting Bow  | Day one    | 3 sticks + 1 string (` #` / `#S` / ` #`)             | 0.7 s     | ~5            | 160        | Fast to draw         |
| *Bow (vanilla)* |         | 3 sticks + 3 string                                  | 1.0 s     | ~6            | 384        |                      |
| Recurve Bow  | Mid game   | 2 steel ingots + 1 stick + 3 string                  | 0.9 s     | ~9            | 700        | Repairs with steel   |
| War Bow      | Late game  | 2 plutonium ingots + 1 blaze rod + 3 string          | 1.8 s     | ~14           | 2000       | Built-in Punch I, repairs with plutonium |

\*Fully drawn, before crits and Power. Faster arrows also fly farther and flatter. The bows zoom in while drawing
like the vanilla bow, take all the usual bow enchantments, and their textures are recolors of the vanilla bow.

## Bosses

The Ender Dragon and the Wither are scaled up well past vanilla to keep pace with the new gear:

| Boss   | Health          | Damage dealt | Damage taken | Extras |
|--------|-----------------|--------------|--------------|--------|
| Dragon | 800 (×4)        | ×3           | ×0.6         | Hits and breath give 6 s of Wither II · lightning strikes near a random player every 15 s · 2 endermen summoned onto a player every 30 s |
| Wither | 1020 (×3.4)     | ×2.5         | ×0.6         | Extra volley of 3 blue (block-breaking) skulls every 6 s · 2 wither skeletons every 20 s, 4 once below half health |

Minecraft caps health at 1024, so the Wither is about as tanky as health alone can make it.
All of it can be changed or turned off in the `bosses` config section.

## Hard mode

Hard mode has two stages for the whole world, each announced in chat:

| | Stage I | Stage II |
|---|---|---|
| Starts when | anyone first enters **the Nether** | anyone first enters **the End** |
| Mob health | +50% | +125% |
| Mob damage to players | ×1.5 | ×2 |
| Mob cap | ×1.5 | ×2 |
| Extra mob with each spawn | 50% | 75% |
| Zombie/skeleton armor (per slot) | 35%, mostly copper–steel | 60%, mostly steel–uranium, some plutonium |
| Zombie swords/axes | 40% | 60% |
| Enchanted gear | 30% | 50% |
| Permanent Speed | none | 25% of mobs |
| Charged creepers | none | 15% |

Stage II replaces stage I's numbers rather than stacking. Mobs already around when stage II starts get the new
health but keep their gear. Hard mode is saved with the world and never turns off; bosses aren't affected (they
have their own settings). Tune or disable each stage in the `hardMode` config section.

## End City loot

End City treasure chests get an extra 1–3 rolls of: 2–6 plutonium ingots (common), 1–3 uranium ingots,
a rare neutronium ingot, or 1–2 quasium shards (rare). Toggle with `endCityLoot`.

## Config

`config/forgedprogression.cfg` (also editable in-game via Mods → Config):
- `progression`: turn gating off, or change the required level of each vanilla ore.
- `worldgen`: enable/disable, vein size, veins per chunk and height range for each ore.
- `bosses`: health, damage and extras for the Ender Dragon and Wither.
- `hardMode`: everything about Nether-triggered hard mode.
- `endCityLoot`: plutonium in End City chests.

If you already have a config file from an older version, Forge keeps your old values instead of the new defaults.
Delete `config/forgedprogression.cfg` (or just its `bosses` section) to pick up the new boss numbers.

## Getting the jar

Every push is built by GitHub Actions. Open the **Actions** tab, click the latest **Build** run and
download **forgedprogression-1.12.2** under Artifacts. The `.jar` inside goes in your `mods` folder.

### Building locally

1. Download the **1.12.2 MDK** (14.23.5.2860) from files.minecraftforge.net and unzip it.
2. Copy this repo's `src/`, `build.gradle` and `settings.gradle` into it (keep the MDK's `gradlew` and `gradle/`).
3. With Java 8 installed, run `gradlew build`.
4. The jar is in `build/libs/`.
