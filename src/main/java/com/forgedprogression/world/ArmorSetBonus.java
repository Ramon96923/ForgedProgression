package com.forgedprogression.world;

import com.forgedprogression.ForgedProgression;
import com.forgedprogression.ModConfig;
import com.forgedprogression.init.ModMaterials;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.MobEffects;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionEffect;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

/**
 * Full borealium armor: permanent Resistance I and Fire Resistance while worn.
 * Armor points are already at Minecraft's cap of 30 from quasium, so this is what sets the top set apart.
 */
@Mod.EventBusSubscriber(modid = ForgedProgression.MODID)
public final class ArmorSetBonus {

    private static final EntityEquipmentSlot[] SLOTS = {
            EntityEquipmentSlot.HEAD, EntityEquipmentSlot.CHEST, EntityEquipmentSlot.LEGS, EntityEquipmentSlot.FEET};

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        EntityPlayer player = event.player;
        if (event.phase != TickEvent.Phase.END || player.world.isRemote || !ModConfig.borealiumSetBonus
                || player.ticksExisted % 20 != 0 || !wearingFullSet(player)) {
            return;
        }
        // refreshed every second with a short duration, so it ends soon after a piece comes off
        player.addPotionEffect(new PotionEffect(MobEffects.RESISTANCE, 60, 0, true, false));
        player.addPotionEffect(new PotionEffect(MobEffects.FIRE_RESISTANCE, 60, 0, true, false));
    }

    private static boolean wearingFullSet(EntityPlayer player) {
        for (EntityEquipmentSlot slot : SLOTS) {
            ItemStack stack = player.getItemStackFromSlot(slot);
            if (!(stack.getItem() instanceof ItemArmor)
                    || ((ItemArmor) stack.getItem()).getArmorMaterial() != ModMaterials.ARMOR_BOREALIUM) {
                return false;
            }
        }
        return true;
    }

    private ArmorSetBonus() {}
}
