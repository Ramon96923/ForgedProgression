package com.forgedprogression.init;

import com.forgedprogression.ForgedProgression;
import com.forgedprogression.block.BlockBase;
import com.forgedprogression.block.BlockGemOre;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;

import java.util.ArrayList;
import java.util.List;

public final class ModBlocks {

    public static final List<Block> BLOCKS = new ArrayList<>();

    // ores — the last number is the pickaxe tier needed
    public static final Block COPPER_ORE    = reg(new BlockBase(Material.ROCK, SoundType.STONE, 3.0F, 5.0F, 1, false), "copper_ore");
    public static final Block TIN_ORE       = reg(new BlockBase(Material.ROCK, SoundType.STONE, 3.0F, 5.0F, 1, false), "tin_ore");
    public static final Block RUBY_ORE      = reg(new BlockGemOre(4.0F, 3, () -> ModItems.RUBY, 3, 7), "ruby_ore");
    public static final Block URANIUM_ORE   = reg(new BlockBase(Material.ROCK, SoundType.STONE, 5.0F, 8.0F, 4, false), "uranium_ore");
    public static final Block PLUTONIUM_ORE = reg(new BlockBase(Material.ROCK, SoundType.STONE, 6.0F, 12.0F, 5, false), "plutonium_ore");

    // storage blocks (beacon bases)
    public static final Block COPPER_BLOCK    = reg(metal(5.0F, 1), "copper_block");
    public static final Block TIN_BLOCK       = reg(metal(5.0F, 1), "tin_block");
    public static final Block BRONZE_BLOCK    = reg(metal(5.0F, 2), "bronze_block");
    public static final Block STEEL_BLOCK     = reg(metal(6.0F, 2), "steel_block");
    public static final Block RUBY_BLOCK      = reg(metal(6.0F, 3), "ruby_block");
    public static final Block URANIUM_BLOCK   = reg(metal(7.0F, 4), "uranium_block");
    public static final Block PLUTONIUM_BLOCK = reg(metal(8.0F, 5), "plutonium_block");
    public static final Block NEUTRONIUM_BLOCK = reg(metal(10.0F, 6), "neutronium_block");

    private static Block metal(float hardness, int level) {
        return new BlockBase(Material.IRON, SoundType.METAL, hardness, hardness * 2, level, true);
    }

    private static Block reg(Block block, String name) {
        block.setRegistryName(ForgedProgression.MODID, name);
        block.setUnlocalizedName(ForgedProgression.MODID + "." + name);
        block.setCreativeTab(ForgedProgression.TAB);
        BLOCKS.add(block);
        return block;
    }

    private ModBlocks() {}
}
