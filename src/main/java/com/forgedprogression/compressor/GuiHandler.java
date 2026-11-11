package com.forgedprogression.compressor;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.IGuiHandler;

public class GuiHandler implements IGuiHandler {

    public static final int COMPRESSOR = 0;

    @Override
    public Object getServerGuiElement(int id, EntityPlayer player, World world, int x, int y, int z) {
        return id == COMPRESSOR ? new ContainerCompressor(player.inventory, world, new BlockPos(x, y, z)) : null;
    }

    @Override
    public Object getClientGuiElement(int id, EntityPlayer player, World world, int x, int y, int z) {
        return id == COMPRESSOR ? new com.forgedprogression.client.GuiCompressor(player.inventory, world, new BlockPos(x, y, z)) : null;
    }
}
