package com.forgedprogression.client;

import com.forgedprogression.ForgedProgression;
import com.forgedprogression.item.ItemModBow;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.event.FOVUpdateEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;

/** Vanilla only zooms in for the vanilla bow; give the mod's bows the same zoom while drawing. */
@Mod.EventBusSubscriber(modid = ForgedProgression.MODID, value = Side.CLIENT)
public final class BowZoomHandler {

    @SubscribeEvent
    public static void onFov(FOVUpdateEvent event) {
        EntityPlayer player = event.getEntity();
        if (!player.isHandActive()) {
            return;
        }
        ItemStack using = player.getActiveItemStack();
        if (using.getItem() instanceof ItemModBow) {
            ItemModBow bow = (ItemModBow) using.getItem();
            float f = player.getItemInUseMaxCount() / (float) bow.getDrawTicks();
            f = f > 1.0F ? 1.0F : f * f;
            event.setNewfov(event.getNewfov() * (1.0F - f * 0.15F));
        }
    }

    private BowZoomHandler() {}
}
