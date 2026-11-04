package com.forgedprogression.block;

import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.Item;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.IBlockAccess;

import java.util.Random;
import java.util.function.Supplier;

/** Ore that drops a gem directly (like diamond ore), with Fortune and XP. */
public class BlockGemOre extends BlockBase {

    private final Supplier<Item> drop;
    private final int minXp;
    private final int maxXp;

    public BlockGemOre(float hardness, int harvestLevel, Supplier<Item> drop, int minXp, int maxXp) {
        super(Material.ROCK, SoundType.STONE, hardness, hardness + 2.0F, harvestLevel, false);
        this.drop = drop;
        this.minXp = minXp;
        this.maxXp = maxXp;
    }

    @Override
    public Item getItemDropped(IBlockState state, Random rand, int fortune) {
        return drop.get();
    }

    @Override
    public int quantityDroppedWithBonus(int fortune, Random random) {
        if (fortune > 0) {
            int bonus = random.nextInt(fortune + 2) - 1;
            return Math.max(0, bonus) + 1;
        }
        return 1;
    }

    @Override
    public int getExpDrop(IBlockState state, IBlockAccess world, BlockPos pos, int fortune) {
        Random rand = world instanceof net.minecraft.world.World ? ((net.minecraft.world.World) world).rand : new Random();
        return MathHelper.getInt(rand, minXp, maxXp);
    }
}
