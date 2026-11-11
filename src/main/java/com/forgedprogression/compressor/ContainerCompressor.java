package com.forgedprogression.compressor;

import com.forgedprogression.init.ModBlocks;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryCraftResult;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * Crafting-table-style container: 3x3 grid, one output slot, player inventory.
 * Slot layout matches the vanilla crafting table so its GUI texture lines up.
 * Slots: 0 = output, 1-9 = grid, 10-36 = inventory, 37-45 = hotbar.
 */
public class ContainerCompressor extends Container {

    private final InventoryCrafting grid = new InventoryCrafting(this, 3, 3);
    private final InventoryCraftResult output = new InventoryCraftResult();
    private final World world;
    private final BlockPos pos;

    public ContainerCompressor(InventoryPlayer playerInv, World world, BlockPos pos) {
        this.world = world;
        this.pos = pos;
        addSlotToContainer(new OutputSlot(output, 0, 124, 35));
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 3; col++) {
                addSlotToContainer(new Slot(grid, col + row * 3, 30 + col * 18, 17 + row * 18));
            }
        }
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                addSlotToContainer(new Slot(playerInv, col + row * 9 + 9, 8 + col * 18, 84 + row * 18));
            }
        }
        for (int col = 0; col < 9; col++) {
            addSlotToContainer(new Slot(playerInv, col, 8 + col * 18, 142));
        }
    }

    @Override
    public void onCraftMatrixChanged(IInventory inventory) {
        output.setInventorySlotContents(0, CompressorRecipes.getResult(grid, world));
    }

    @Override
    public void onContainerClosed(EntityPlayer player) {
        super.onContainerClosed(player);
        if (!world.isRemote) {
            clearContainer(player, world, grid); // give back whatever is left in the grid
        }
    }

    @Override
    public boolean canInteractWith(EntityPlayer player) {
        return world.getBlockState(pos).getBlock() == ModBlocks.SINGULARITY_COMPRESSOR
                && player.getDistanceSq(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5) <= 64.0;
    }

    @Override
    public boolean canMergeSlot(ItemStack stack, Slot slot) {
        return slot.inventory != output && super.canMergeSlot(stack, slot);
    }

    /** Shift-click, same rules as the vanilla crafting table. */
    @Override
    public ItemStack transferStackInSlot(EntityPlayer player, int index) {
        ItemStack copy = ItemStack.EMPTY;
        Slot slot = inventorySlots.get(index);
        if (slot == null || !slot.getHasStack()) {
            return copy;
        }
        ItemStack stack = slot.getStack();
        copy = stack.copy();
        if (index == 0) {
            stack.getItem().onCreated(stack, world, player);
            if (!mergeItemStack(stack, 10, 46, true)) {
                return ItemStack.EMPTY;
            }
            slot.onSlotChange(stack, copy);
        } else if (index >= 10 && index < 37) {
            if (!mergeItemStack(stack, 37, 46, false)) {
                return ItemStack.EMPTY;
            }
        } else if (index >= 37 && index < 46) {
            if (!mergeItemStack(stack, 10, 37, false)) {
                return ItemStack.EMPTY;
            }
        } else if (!mergeItemStack(stack, 10, 46, false)) {
            return ItemStack.EMPTY;
        }

        if (stack.isEmpty()) {
            slot.putStack(ItemStack.EMPTY);
        } else {
            slot.onSlotChanged();
        }
        if (stack.getCount() == copy.getCount()) {
            return ItemStack.EMPTY;
        }
        ItemStack leftover = slot.onTake(player, stack);
        if (index == 0) {
            player.dropItem(leftover, false);
        }
        return copy;
    }

    /** Output slot: can't put items in; taking the result uses up one item from every filled grid slot. */
    private class OutputSlot extends Slot {

        OutputSlot(IInventory inventory, int index, int x, int y) {
            super(inventory, index, x, y);
        }

        @Override
        public boolean isItemValid(ItemStack stack) {
            return false;
        }

        @Override
        public ItemStack onTake(EntityPlayer player, ItemStack stack) {
            for (int i = 0; i < grid.getSizeInventory(); i++) {
                if (!grid.getStackInSlot(i).isEmpty()) {
                    grid.decrStackSize(i, 1); // also recalculates the output
                }
            }
            if (!world.isRemote) {
                world.playSound(null, pos, SoundEvents.BLOCK_ANVIL_LAND, SoundCategory.BLOCKS, 0.4F, 0.5F);
            }
            return super.onTake(player, stack);
        }
    }
}
