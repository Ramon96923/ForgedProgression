package com.forgedprogression.init;

import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.registry.GameRegistry;

public final class ModSmelting {

    public static void register() {
        GameRegistry.addSmelting(ModBlocks.COPPER_ORE, new ItemStack(ModItems.COPPER_INGOT), 0.5F);
        GameRegistry.addSmelting(ModBlocks.TIN_ORE, new ItemStack(ModItems.TIN_INGOT), 0.5F);
        GameRegistry.addSmelting(ModItems.STEEL_BLEND, new ItemStack(ModItems.STEEL_INGOT), 1.0F);
    }

    private ModSmelting() {}
}
