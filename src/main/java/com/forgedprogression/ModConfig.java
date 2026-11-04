package com.forgedprogression;

import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.ConfigManager;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Config(modid = ForgedProgression.MODID)
public class ModConfig {

    @Config.Comment({"Harvest-level gating of vanilla ores. Tiers: 0=wood, 1=stone/copper, 2=bronze/iron,",
            "3=steel/diamond, 4=ruby, 5=uranium, 6=plutonium. Requires restart."})
    public static Progression progression = new Progression();

    @Config.Comment("Ore generation. Copper, tin, ruby and uranium spawn in the Overworld; plutonium spawns in End stone.")
    public static WorldGen worldgen = new WorldGen();

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
