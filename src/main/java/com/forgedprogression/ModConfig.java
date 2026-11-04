package com.forgedprogression;

import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.ConfigManager;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Config(modid = ForgedProgression.MODID)
public class ModConfig {

    @Config.Comment("Harvest-level gating of vanilla ores. Tiers: 0=wood, 1=stone/copper, 2=bronze/iron, 3=steel/diamond. Requires restart.")
    public static Progression progression = new Progression();

    @Config.Comment("Ore generation in the Overworld.")
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
        public boolean copperEnabled = true;
        @Config.RangeInt(min = 1, max = 32) public int copperVeinSize = 9;
        @Config.RangeInt(min = 0, max = 64) public int copperVeinsPerChunk = 14;
        @Config.RangeInt(min = 1, max = 255) public int copperMinY = 30;
        @Config.RangeInt(min = 1, max = 255) public int copperMaxY = 96;

        public boolean tinEnabled = true;
        @Config.RangeInt(min = 1, max = 32) public int tinVeinSize = 8;
        @Config.RangeInt(min = 0, max = 64) public int tinVeinsPerChunk = 10;
        @Config.RangeInt(min = 1, max = 255) public int tinMinY = 10;
        @Config.RangeInt(min = 1, max = 255) public int tinMaxY = 64;
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
