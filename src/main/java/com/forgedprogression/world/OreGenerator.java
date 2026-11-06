package com.forgedprogression.world;

import com.forgedprogression.ModConfig;
import com.forgedprogression.init.ModBlocks;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.block.state.pattern.BlockMatcher;
import net.minecraft.init.Blocks;
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
        ModConfig.WorldGen cfg = ModConfig.worldgen;
        int x = chunkX * 16;
        int z = chunkZ * 16;

        switch (world.provider.getDimension()) {
            case 0: // Overworld
                spawn(ModBlocks.COPPER_ORE, Blocks.STONE, cfg.copper, world, random, x, z);
                spawn(ModBlocks.TIN_ORE, Blocks.STONE, cfg.tin, world, random, x, z);
                spawn(ModBlocks.RUBY_ORE, Blocks.STONE, cfg.ruby, world, random, x, z);
                spawn(ModBlocks.URANIUM_ORE, Blocks.STONE, cfg.uranium, world, random, x, z);
                break;
            case 1: // The End
                spawn(ModBlocks.PLUTONIUM_ORE, Blocks.END_STONE, cfg.plutonium, world, random, x, z);
                // quasium only on the outer islands, so you need to beat the dragon and go through a gateway first
                if ((long) x * x + (long) z * z > (long) cfg.quasiumMinDistance * cfg.quasiumMinDistance) {
                    spawn(ModBlocks.QUASIUM_ORE, Blocks.END_STONE, cfg.quasium, world, random, x, z);
                }
                break;
            default:
                break;
        }
    }

    private static void spawn(Block ore, Block replaces, ModConfig.OreSettings s, World world, Random random, int x, int z) {
        if (!s.enabled || s.veinsPerChunk <= 0) {
            return;
        }
        int minY = Math.min(s.minY, s.maxY);
        int maxY = Math.max(s.minY, s.maxY);
        int range = Math.max(1, maxY - minY);
        IBlockState state = ore.getDefaultState();
        WorldGenMinable gen = new WorldGenMinable(state, s.veinSize, BlockMatcher.forBlock(replaces));
        for (int i = 0; i < s.veinsPerChunk; i++) {
            // +8 offset keeps generation inside already-loaded chunks (avoids cascading worldgen)
            BlockPos pos = new BlockPos(x + 8 + random.nextInt(16), minY + random.nextInt(range), z + 8 + random.nextInt(16));
            gen.generate(world, random, pos);
        }
    }
}
