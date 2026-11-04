package com.forgedprogression.init;

import com.forgedprogression.ForgedProgression;
import com.forgedprogression.ModConfig;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;

/**
 * Raises the pickaxe tier needed for vanilla ores, so the new metals become required steps:
 *   stone pick  -> copper & tin
 *   bronze pick -> iron, gold, redstone, lapis
 *   steel pick  -> diamond & emerald
 * Forge's ForgeHooks.canHarvestBlock compares tool level against these values for every pickaxe.
 */
public final class Progression {

    public static void apply() {
        ModConfig.Progression cfg = ModConfig.progression;
        if (!cfg.enabled) {
            ForgedProgression.LOGGER.info("Progression gating disabled in config");
            return;
        }
        set(Blocks.IRON_ORE, cfg.ironOre);
        set(Blocks.GOLD_ORE, cfg.goldOre);
        set(Blocks.LAPIS_ORE, cfg.lapisOre);
        set(Blocks.REDSTONE_ORE, cfg.redstoneOre);
        set(Blocks.LIT_REDSTONE_ORE, cfg.redstoneOre);
        set(Blocks.DIAMOND_ORE, cfg.diamondOre);
        set(Blocks.EMERALD_ORE, cfg.emeraldOre);
        ForgedProgression.LOGGER.info("Applied ore harvest-level progression");
    }

    private static void set(Block block, int level) {
        block.setHarvestLevel("pickaxe", level);
    }

    private Progression() {}
}
