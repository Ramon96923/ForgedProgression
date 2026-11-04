package com.forgedprogression.init;

import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.registry.GameRegistry;

public final class ModSmelting {

    public static void register() {
        GameRegistry.addSmelting(ModBlocks.COPPER_ORE, new ItemStack(ModItems.COPPER_INGOT), 0.5F);
        GameRegistry.addSmelting(ModBlocks.TIN_ORE, new ItemStack(ModItems.TIN_INGOT), 0.5F);
        GameRegistry.addSmelting(ModItems.STEEL_BLEND, new ItemStack(ModItems.STEEL_INGOT), 1.0F);
        GameRegistry.addSmelting(ModBlocks.RUBY_ORE, new ItemStack(ModItems.RUBY), 1.0F);
        GameRegistry.addSmelting(ModBlocks.URANIUM_ORE, new ItemStack(ModItems.URANIUM_INGOT), 1.2F);
        GameRegistry.addSmelting(ModBlocks.PLUTONIUM_ORE, new ItemStack(ModItems.PLUTONIUM_INGOT), 1.5F);
    }

    private ModSmelting() {}
}
