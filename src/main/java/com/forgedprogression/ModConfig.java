package com.forgedprogression;

import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.ConfigManager;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Config(modid = ForgedProgression.MODID)
public class ModConfig {

    @Config.Comment({"Harvest-level gating of vanilla ores. Tiers: 0=wood, 1=stone/copper, 2=bronze/iron,",
            "3=steel/diamond, 4=ruby, 5=uranium, 6=plutonium, 7=neutronium, 8=quasium, 9=borealium. Requires restart."})
    public static Progression progression = new Progression();

    @Config.Comment("Ore generation. Copper, tin, ruby and uranium spawn in the Overworld; plutonium and quasium spawn in End stone.")
    public static WorldGen worldgen = new WorldGen();

    @Config.Comment({"Ender Dragon and Wither difficulty, scaled up to match the new gear.", "Minecraft caps max health at 1024, so very high health multipliers stop having an effect."})
    public static Bosses bosses = new Bosses();

    public static class Bosses {
        @Config.Comment("Vanilla dragon: 200 health.")
        public DragonSettings dragon = new DragonSettings();
        @Config.Comment("Vanilla Wither: 300 health.")
        public WitherSettings wither = new WitherSettings();
    }

    public static class DragonSettings {
        public boolean enabled = true;
        @Config.RangeDouble(min = 0.1, max = 100) public double healthMultiplier = 4.0;
        @Config.Comment("Multiplies damage from the dragon's melee, breath and fireballs.")
        @Config.RangeDouble(min = 0, max = 100) public double damageMultiplier = 3.0;
        @Config.Comment("Multiplies damage the dragon takes. Below 1 = tankier.")
        @Config.RangeDouble(min = 0.01, max = 10) public double damageTakenMultiplier = 0.6;
        @Config.Comment("Seconds of Wither II applied when the dragon hits you. 0 = off.")
        @Config.RangeInt(min = 0, max = 60) public int witherOnHitSeconds = 6;
        @Config.Comment("Seconds between lightning strikes on a random player fighting the dragon. 0 = off.")
        @Config.RangeInt(min = 0, max = 600) public int lightningIntervalSeconds = 15;
        @Config.Comment("Endermen summoned onto a random player per wave. 0 = off.")
        @Config.RangeInt(min = 0, max = 10) public int endermenPerWave = 2;
        @Config.Comment("Seconds between endermen waves.")
        @Config.RangeInt(min = 1, max = 600) public int endermenIntervalSeconds = 30;
    }

    public static class WitherSettings {
        public boolean enabled = true;
        @Config.RangeDouble(min = 0.1, max = 100) public double healthMultiplier = 3.4;
        @Config.Comment("Multiplies damage from wither skulls.")
        @Config.RangeDouble(min = 0, max = 100) public double damageMultiplier = 2.5;
        @Config.Comment("Multiplies damage the Wither takes. Below 1 = tankier.")
        @Config.RangeDouble(min = 0.01, max = 10) public double damageTakenMultiplier = 0.6;
        @Config.Comment("Wither skeletons summoned per wave (doubled below half health). 0 = off.")
        @Config.RangeInt(min = 0, max = 10) public int minionsPerWave = 2;
        @Config.Comment("Seconds between minion waves.")
        @Config.RangeInt(min = 1, max = 600) public int minionIntervalSeconds = 20;
        @Config.Comment("Seconds between extra volleys of blue (block-breaking) skulls at its target. 0 = off.")
        @Config.RangeInt(min = 0, max = 600) public int skullVolleyIntervalSeconds = 6;
        @Config.RangeInt(min = 1, max = 10) public int skullVolleySize = 3;
    }

    @Config.Comment({"Hard mode has two stages for the whole world: stage I starts the first time any player enters",
            "the Nether, stage II the first time any player enters the End. It only affects hostile mobs;",
            "the Ender Dragon and Wither have their own settings."})
    public static HardMode hardMode = new HardMode();

    public static class HardMode {
        public boolean enabled = true;
        @Config.Comment("Stage I: after entering the Nether.")
        public HardModeStage nether = new HardModeStage(1.5, 1.5, 1.5, 0.5, 0.35, 0.4, 0.3, 0.0, 0.0);
        @Config.Comment("Stage II: after entering the End. Replaces stage I's numbers (they don't stack).")
        public HardModeStage end = new HardModeStage(3.5, 3.0, 2.5, 1.5, 0.85, 0.8, 0.75, 0.5, 0.4)
                .extras(0.3, 0.6, 2.0, 0.5, 0.25).warBows(0.2);
    }

    public static class HardModeStage {
        @Config.RangeDouble(min = 1, max = 10) public double mobHealthMultiplier;
        @Config.Comment("Multiplies all damage hostile mobs deal to players (melee, arrows, explosions, fireballs).")
        @Config.RangeDouble(min = 0, max = 10) public double mobDamageMultiplier;
        @Config.Comment("Multiplies the hostile mob cap, so more mobs are around at once.")
        @Config.RangeDouble(min = 1, max = 5) public double mobCapMultiplier;
        @Config.Comment({"Extra mobs that come with each natural hostile spawn. The whole part is guaranteed,",
                "the fraction is a chance for one more (0.5 = 50% chance of 1 extra, 1.5 = 1 extra plus 50% chance of 2)."})
        @Config.RangeDouble(min = 0, max = 3) public double extraSpawns;
        @Config.Comment("Chance per armor slot that a zombie or skeleton spawns wearing mod armor.")
        @Config.RangeDouble(min = 0, max = 1) public double armorChance;
        @Config.Comment("Chance that a zombie spawns holding a mod sword or axe.")
        @Config.RangeDouble(min = 0, max = 1) public double weaponChance;
        @Config.Comment("Chance that each piece of mob gear (and skeleton bows) is enchanted.")
        @Config.RangeDouble(min = 0, max = 1) public double enchantChance;
        @Config.Comment("Chance that a skeleton spawns holding a war bow instead of its normal bow (rarely dropped).")
        @Config.RangeDouble(min = 0, max = 1) public double warBowChance = 0;
        @Config.Comment("Chance that a hostile mob spawns with permanent Speed.")
        @Config.RangeDouble(min = 0, max = 1) public double speedChance;
        @Config.Comment("Chance that a creeper spawns charged.")
        @Config.RangeDouble(min = 0, max = 1) public double chargedCreeperChance;
        @Config.Comment("Chance that a hostile mob spawns with permanent Resistance I.")
        @Config.RangeDouble(min = 0, max = 1) public double resistanceChance = 0;
        @Config.Comment("Chance that a hostile mob spawns with permanent Fire Resistance (zombies and skeletons stop burning in daylight).")
        @Config.RangeDouble(min = 0, max = 1) public double fireResistanceChance = 0;
        @Config.Comment("Multiplies how far away mobs notice and chase players.")
        @Config.RangeDouble(min = 1, max = 4) public double followRangeMultiplier = 1;
        @Config.Comment("Knockback resistance given to every hostile mob (0 = none, 1 = immune).")
        @Config.RangeDouble(min = 0, max = 1) public double knockbackResistance = 0;
        @Config.Comment("Chance that a hit from a hostile mob also gives you Slowness or Weakness for a few seconds.")
        @Config.RangeDouble(min = 0, max = 1) public double debuffOnHitChance = 0;

        public HardModeStage(double health, double damage, double cap, double extraSpawn, double armor,
                             double weapon, double enchant, double speed, double chargedCreeper) {
            this.mobHealthMultiplier = health;
            this.mobDamageMultiplier = damage;
            this.mobCapMultiplier = cap;
            this.extraSpawns = extraSpawn;
            this.armorChance = armor;
            this.weaponChance = weapon;
            this.enchantChance = enchant;
            this.speedChance = speed;
            this.chargedCreeperChance = chargedCreeper;
        }

        HardModeStage warBows(double chance) {
            this.warBowChance = chance;
            return this;
        }

        HardModeStage extras(double resistance, double fireResistance, double followRange, double knockback, double debuff) {
            this.resistanceChance = resistance;
            this.fireResistanceChance = fireResistance;
            this.followRangeMultiplier = followRange;
            this.knockbackResistance = knockback;
            this.debuffOnHitChance = debuff;
            return this;
        }
    }

    @Config.Comment("Wearing a full set of borealium armor gives permanent Resistance I and Fire Resistance.")
    public static boolean borealiumSetBonus = true;

    @Config.Comment("Adds plutonium, uranium and rare neutronium ingots and quasium shards to End City treasure chests.")
    public static boolean endCityLoot = true;

    public static class Progression {
        @Config.Comment("Turn off to leave vanilla harvest levels untouched.")
        public boolean enabled = true;

        @Config.RangeInt(min = 0, max = 10) public int ironOre = 2;
        @Config.RangeInt(min = 0, max = 10) public int goldOre = 2;
        @Config.RangeInt(min = 0, max = 10) public int lapisOre = 2;
        @Config.RangeInt(min = 0, max = 10) public int redstoneOre = 2;
        @Config.RangeInt(min = 0, max = 10) public int diamondOre = 3;
        @Config.RangeInt(min = 0, max = 10) public int emeraldOre = 3;
    }

    public static class WorldGen {
        public OreSettings copper    = new OreSettings(true, 9, 14, 30, 96);
        public OreSettings tin       = new OreSettings(true, 8, 10, 10, 64);
        public OreSettings ruby      = new OreSettings(true, 5, 3, 4, 24);
        public OreSettings uranium   = new OreSettings(true, 4, 2, 4, 18);
        public OreSettings plutonium = new OreSettings(true, 5, 6, 10, 80);
        public OreSettings quasium   = new OreSettings(true, 4, 3, 10, 80);
        @Config.Comment("Quasium only generates at least this many blocks from the End's center (the outer islands start around 1000).")
        @Config.RangeInt(min = 0, max = 100000) public int quasiumMinDistance = 1000;
    }

    public static class OreSettings {
        public boolean enabled;
        @Config.RangeInt(min = 1, max = 32) public int veinSize;
        @Config.RangeInt(min = 0, max = 64) public int veinsPerChunk;
        @Config.RangeInt(min = 1, max = 255) public int minY;
        @Config.RangeInt(min = 1, max = 255) public int maxY;

        public OreSettings(boolean enabled, int veinSize, int veinsPerChunk, int minY, int maxY) {
            this.enabled = enabled;
            this.veinSize = veinSize;
            this.veinsPerChunk = veinsPerChunk;
            this.minY = minY;
            this.maxY = maxY;
        }
    }

    @Mod.EventBusSubscriber(modid = ForgedProgression.MODID)
    private static class Handler {
        @SubscribeEvent
        public static void onConfigChanged(ConfigChangedEvent.OnConfigChangedEvent event) {
            if (event.getModID().equals(ForgedProgression.MODID)) {
                ConfigManager.sync(ForgedProgression.MODID, Config.Type.INSTANCE);
            }
        }
    }
}
