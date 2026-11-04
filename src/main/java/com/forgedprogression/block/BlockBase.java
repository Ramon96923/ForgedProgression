package com.forgedprogression.block;

import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

public class BlockBase extends Block {

    private final boolean beaconBase;

    public BlockBase(Material material, SoundType sound, float hardness, float resistance, int harvestLevel, boolean beaconBase) {
        super(material);
        this.beaconBase = beaconBase;
        setHardness(hardness);
        setResistance(resistance);
        setSoundType(sound);
        setHarvestLevel("pickaxe", harvestLevel);
    }

    @Override
    public boolean isBeaconBase(IBlockAccess world, BlockPos pos, BlockPos beacon) {
        return beaconBase;
    }
}
