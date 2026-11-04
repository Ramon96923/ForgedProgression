package com.forgedprogression.client;

import com.forgedprogression.ForgedProgression;
import com.forgedprogression.init.ModBlocks;
import com.forgedprogression.init.ModItems;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;

@Mod.EventBusSubscriber(modid = ForgedProgression.MODID, value = Side.CLIENT)
public final class ClientRegistryHandler {

    @SubscribeEvent
    public static void registerModels(ModelRegistryEvent event) {
        for (Item item : ModItems.ITEMS) {
            model(item);
        }
        for (Block block : ModBlocks.BLOCKS) {
            model(Item.getItemFromBlock(block));
        }
    }

    private static void model(Item item) {
        ModelLoader.setCustomModelResourceLocation(item, 0, new ModelResourceLocation(item.getRegistryName(), "inventory"));
    }

    private ClientRegistryHandler() {}
}
