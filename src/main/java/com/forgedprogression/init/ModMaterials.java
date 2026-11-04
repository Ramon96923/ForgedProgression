package com.forgedprogression.init;

import com.forgedprogression.ForgedProgression;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemArmor;
import net.minecraftforge.common.util.EnumHelper;

/**
 * Tiers (vanilla for reference):
 *   wood 0 / 59 uses, stone 1 / 131, iron 2 / 250, diamond 3 / 1561
 *   copper 1 / 200, bronze 2 / 350, steel 3 / 900
 */
public final class ModMaterials {

    // name, harvestLevel, maxUses, efficiency, damage, enchantability
    public static final Item.ToolMaterial TOOL_COPPER = EnumHelper.addToolMaterial("FP_COPPER", 1, 200, 5.0F, 1.5F, 8);
    public static final Item.ToolMaterial TOOL_BRONZE = EnumHelper.addToolMaterial("FP_BRONZE", 2, 350, 6.0F, 2.0F, 12);
    public static final Item.ToolMaterial TOOL_STEEL  = EnumHelper.addToolMaterial("FP_STEEL",  3, 900, 7.0F, 2.5F, 12);

    // durability factor, protection {boots, legs, chest, helmet}, enchantability, sound, toughness
    public static final ItemArmor.ArmorMaterial ARMOR_COPPER = EnumHelper.addArmorMaterial("FP_COPPER",
            ForgedProgression.MODID + ":copper", 10, new int[]{1, 3, 4, 1}, 10, SoundEvents.ITEM_ARMOR_EQUIP_CHAIN, 0.0F);
    public static final ItemArmor.ArmorMaterial ARMOR_BRONZE = EnumHelper.addArmorMaterial("FP_BRONZE",
            ForgedProgression.MODID + ":bronze", 14, new int[]{2, 4, 5, 2}, 11, SoundEvents.ITEM_ARMOR_EQUIP_IRON, 0.0F);
    public static final ItemArmor.ArmorMaterial ARMOR_STEEL = EnumHelper.addArmorMaterial("FP_STEEL",
            ForgedProgression.MODID + ":steel", 24, new int[]{2, 6, 7, 3}, 10, SoundEvents.ITEM_ARMOR_EQUIP_IRON, 1.0F);

    private ModMaterials() {}
}
