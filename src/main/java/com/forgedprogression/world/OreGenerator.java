package com.forgedprogression.world;

import com.forgedprogression.ModConfig;
import com.forgedprogression.init.ModBlocks;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.IChunkGenerator;
import net.minecraft.world.gen.feature.WorldGenMinable;
import net.minecraftforge.fml.common.IWorldGenerator;

import java.util.Random;

public class OreGenerator implements IWorldGenerator {

    @Override
    public void generate(Random random, int chunkX, int chunkZ, World world,
                         IChunkGenerator chunkGenerator, IChunkProvider chunkProvider) {
        if (world.provider.getDimension() != 0) {
            return;
        }
        ModConfig.WorldGen cfg = ModConfig.worldgen;
        int x = chunkX * 16;
        int z = chunkZ * 16;

        if (cfg.copperEnabled) {
            spawn(ModBlocks.COPPER_ORE.getDefaultState(), world, random, x, z,
                    cfg.copperVeinSize, cfg.copperVeinsPerChunk, cfg.copperMinY, cfg.copperMaxY);
        }
        if (cfg.tinEnabled) {
            spawn(ModBlocks.TIN_ORE.getDefaultState(), world, random, x, z,
                    cfg.tinVeinSize, cfg.tinVeinsPerChunk, cfg.tinMinY, cfg.tinMaxY);
        }
    }

    private static void spawn(IBlockState state, World world, Random random, int x, int z,
                              int veinSize, int veins, int minY, int maxY) {
        if (maxY < minY) {
            int t = minY; minY = maxY; maxY = t;
        }
        int range = Math.max(1, maxY - minY);
        WorldGenMinable gen = new WorldGenMinable(state, veinSize);
        for (int i = 0; i < veins; i++) {
            // +8 offset keeps generation inside already-loaded chunks (avoids cascading worldgen)
            BlockPos pos = new BlockPos(x + 8 + random.nextInt(16), minY + random.nextInt(range), z + 8 + random.nextInt(16));
            gen.generate(world, random, pos);
        }
    }
}
