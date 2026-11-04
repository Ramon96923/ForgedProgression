package com.forgedprogression.init;

import com.forgedprogression.ForgedProgression;
import com.forgedprogression.block.BlockBase;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;

import java.util.ArrayList;
import java.util.List;

public final class ModBlocks {

    public static final List<Block> BLOCKS = new ArrayList<>();

    // ores: mined with stone pickaxe or better (level 1)
    public static final Block COPPER_ORE = reg(new BlockBase(Material.ROCK, SoundType.STONE, 3.0F, 5.0F, 1, false), "copper_ore");
    public static final Block TIN_ORE    = reg(new BlockBase(Material.ROCK, SoundType.STONE, 3.0F, 5.0F, 1, false), "tin_ore");

    // storage blocks (beacon bases)
    public static final Block COPPER_BLOCK = reg(new BlockBase(Material.IRON, SoundType.METAL, 5.0F, 10.0F, 1, true), "copper_block");
    public static final Block TIN_BLOCK    = reg(new BlockBase(Material.IRON, SoundType.METAL, 5.0F, 10.0F, 1, true), "tin_block");
    public static final Block BRONZE_BLOCK = reg(new BlockBase(Material.IRON, SoundType.METAL, 5.0F, 10.0F, 2, true), "bronze_block");
    public static final Block STEEL_BLOCK  = reg(new BlockBase(Material.IRON, SoundType.METAL, 6.0F, 12.0F, 2, true), "steel_block");

    private static Block reg(Block block, String name) {
        block.setRegistryName(ForgedProgression.MODID, name);
        block.setUnlocalizedName(ForgedProgression.MODID + "." + name);
        block.setCreativeTab(ForgedProgression.TAB);
        BLOCKS.add(block);
        return block;
    }

    private ModBlocks() {}
}
