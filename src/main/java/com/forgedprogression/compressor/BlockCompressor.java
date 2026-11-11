package com.forgedprogression.compressor;

import com.forgedprogression.ForgedProgression;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.List;

/** Singularity Compressor: right-click to open a crafting grid that gives bonus output for the densest metals. */
public class BlockCompressor extends Block {

    public BlockCompressor() {
        super(Material.IRON);
        setHardness(25.0F);
        setResistance(2000.0F);
        setSoundType(SoundType.METAL);
        setHarvestLevel("pickaxe", 6);
    }

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand,
                                    EnumFacing facing, float hitX, float hitY, float hitZ) {
        if (!world.isRemote) {
            player.openGui(ForgedProgression.instance, GuiHandler.COMPRESSOR, world, pos.getX(), pos.getY(), pos.getZ());
        }
        return true;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World world, List<String> tooltip, ITooltipFlag flag) {
        tooltip.add(TextFormatting.GRAY + "Same layouts as the crafting table, more output:");
        tooltip.add(TextFormatting.DARK_AQUA + " 4 Plutonium + 4 Uranium + Iron -> 4 Neutronium");
        tooltip.add(TextFormatting.LIGHT_PURPLE + " 4 Quasium Shards + 4 Neutronium + End Crystal -> 3 Quasium");
        tooltip.add(TextFormatting.GREEN + " 6 Neutronium Blocks + 3 Quasium Blocks -> 4 Borealium");
    }
}
