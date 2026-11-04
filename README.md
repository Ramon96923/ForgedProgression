# Forged Progression (Forge 1.12.2)

Adds copper, tin, bronze and steel, with tools and armor for each tier, and raises the
pickaxe tier needed for vanilla ores so every metal is a required step.

## Progression

| Tier | Pickaxe            | Unlocks                                  |
|------|--------------------|------------------------------------------|
| 0    | Wood               | Stone, coal                              |
| 1    | Stone / **Copper** | Copper ore, tin ore                      |
| 2    | **Bronze** / Iron  | Iron, gold, redstone, lapis              |
| 3    | **Steel** / Diamond| Diamond, emerald                         |

Stone pickaxes can no longer harvest iron, and iron pickaxes can no longer harvest diamonds.

## Materials

- **Copper ore** — y 30–96, common. Smelt → copper ingot.
- **Tin ore** — y 10–64, less common. Smelt → tin ingot.
- **Bronze** — 3 copper ingots + 1 tin ingot (shapeless) → 4 bronze ingots.
- **Steel** — 1 iron ingot + 2 coal/charcoal (shapeless) → steel blend; smelt → steel ingot.
- Storage blocks for all four metals (9 ingots ↔ 1 block, usable as beacon bases).

## Stats

| Material | Harvest | Durability | Speed | Armor (helm/chest/legs/boots) | Armor durability factor |
|----------|---------|------------|-------|-------------------------------|-------------------------|
| Copper   | 1       | 200        | 5.0   | 1 / 4 / 3 / 1                 | 10                      |
| Bronze   | 2       | 350        | 6.0   | 2 / 5 / 4 / 2                 | 14                      |
| Steel    | 3       | 900        | 7.0   | 3 / 7 / 6 / 2 (+1 toughness)  | 24                      |
| *Iron (vanilla)* | 2 | 250      | 6.0   | 2 / 6 / 5 / 2                 | 15                      |

Tools and armor use vanilla crafting shapes and repair in an anvil with their ingot.
Everything is registered in the Ore Dictionary (`oreCopper`, `ingotBronze`, `ingotSteel`, ...),
so recipes accept metals from other mods too.

## Config

`config/forgedprogression.cfg` (also editable in-game via Mods → Config):
- `progression` — turn gating off or change the required level of each vanilla ore.
- `worldgen` — vein size, veins per chunk and height range for copper and tin.

## Building

1. Download the **1.12.2 MDK** (14.23.5.2860) from files.minecraftforge.net and unzip it.
2. Delete the MDK's `src` folder, then copy this project's `src/`, `build.gradle` and `README.md` into it
   (keep the MDK's `gradlew`, `gradlew.bat`, `gradle/` and `settings.gradle`).
3. Make sure Java 8 is installed (the build uses a Java 8 toolchain).
4. Run `gradlew build` (Windows) or `./gradlew build` (macOS/Linux).
5. The mod jar is in `build/libs/forgedprogression-1.12.2-1.0.0.jar` — drop it in your `mods` folder.

To test in a dev client: `gradlew genIntellijRuns` (or `genEclipseRuns`), then run `runClient`.

Textures are simple generated placeholders; replace the PNGs under
`src/main/resources/assets/forgedprogression/textures/` with your own any time.
