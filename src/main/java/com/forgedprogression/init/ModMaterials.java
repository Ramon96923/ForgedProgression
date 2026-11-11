package com.forgedprogression.init;

import com.forgedprogression.ForgedProgression;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemArmor;
import net.minecraftforge.common.util.EnumHelper;

/**
 * Harvest tiers (vanilla for reference: wood 0, stone 1, iron 2, diamond 3):
 *   copper 1 / 200 uses     bronze 2 / 350     steel 3 / 900
 *   ruby 4 / 1800           uranium 5 / 2600   plutonium 6 / 3500
 *   neutronium 7 / 5000 (crafted from iron + uranium + plutonium, no ore)
 *   quasium 8 / 7000 (ore in the outer End, ingot also needs neutronium and an end crystal)
 *   borealium 9 / 12000 (no ore; one ingot costs 6 neutronium blocks + 3 quasium blocks)
 */
public final class ModMaterials {

    // name, harvestLevel, maxUses, efficiency, damage, enchantability
    public static final Item.ToolMaterial TOOL_COPPER    = EnumHelper.addToolMaterial("FP_COPPER",    1,  200,  5.0F, 1.5F,  8);
    public static final Item.ToolMaterial TOOL_BRONZE    = EnumHelper.addToolMaterial("FP_BRONZE",    2,  350,  6.0F, 2.0F, 12);
    public static final Item.ToolMaterial TOOL_STEEL     = EnumHelper.addToolMaterial("FP_STEEL",     3,  900,  7.0F, 2.5F, 12);
    public static final Item.ToolMaterial TOOL_RUBY      = EnumHelper.addToolMaterial("FP_RUBY",      4, 1800,  9.0F, 3.5F, 14);
    public static final Item.ToolMaterial TOOL_URANIUM   = EnumHelper.addToolMaterial("FP_URANIUM",   5, 2600, 10.0F, 4.0F, 12);
    public static final Item.ToolMaterial TOOL_PLUTONIUM = EnumHelper.addToolMaterial("FP_PLUTONIUM", 6, 3500, 12.0F, 5.0F, 16);
    public static final Item.ToolMaterial TOOL_NEUTRONIUM = EnumHelper.addToolMaterial("FP_NEUTRONIUM", 7, 5000, 14.0F, 6.0F, 18);
    public static final Item.ToolMaterial TOOL_QUASIUM = EnumHelper.addToolMaterial("FP_QUASIUM", 8, 7000, 16.0F, 7.5F, 22);
    public static final Item.ToolMaterial TOOL_BOREALIUM = EnumHelper.addToolMaterial("FP_BOREALIUM", 9, 12000, 20.0F, 9.0F, 25);

    // texture, durability factor, protection {boots, legs, chest, helmet}, enchantability, sound, toughness
    public static final ItemArmor.ArmorMaterial ARMOR_COPPER = armor("copper", 10, new int[]{1, 3, 4, 1}, 10, 0.0F, false);
    public static final ItemArmor.ArmorMaterial ARMOR_BRONZE = armor("bronze", 14, new int[]{2, 4, 5, 2}, 11, 0.0F, false);
    public static final ItemArmor.ArmorMaterial ARMOR_STEEL  = armor("steel",  24, new int[]{2, 6, 7, 3}, 10, 1.0F, false);
    public static final ItemArmor.ArmorMaterial ARMOR_RUBY      = armor("ruby",      36, new int[]{3, 6, 8, 3}, 14, 2.5F, true);
    public static final ItemArmor.ArmorMaterial ARMOR_URANIUM   = armor("uranium",   42, new int[]{3, 7, 9, 3}, 12, 3.0F, true);
    public static final ItemArmor.ArmorMaterial ARMOR_PLUTONIUM = armor("plutonium", 50, new int[]{4, 7, 9, 4}, 16, 4.0F, true);
    public static final ItemArmor.ArmorMaterial ARMOR_NEUTRONIUM = armor("neutronium", 66, new int[]{4, 8, 10, 4}, 18, 5.0F, true);
    // 30 total armor is Minecraft's cap, so quasium maxes it out
    public static final ItemArmor.ArmorMaterial ARMOR_QUASIUM = armor("quasium", 85, new int[]{5, 9, 11, 5}, 22, 7.0F, true);
    // armor points are already at the cap, so borealium adds toughness, durability and a full-set bonus (see ArmorSetBonus)
    public static final ItemArmor.ArmorMaterial ARMOR_BOREALIUM = armor("borealium", 120, new int[]{5, 9, 11, 5}, 25, 10.0F, true);

    private static ItemArmor.ArmorMaterial armor(String name, int durability, int[] protection, int enchant,
                                                 float toughness, boolean diamondSound) {
        return EnumHelper.addArmorMaterial("FP_" + name.toUpperCase(), ForgedProgression.MODID + ":" + name,
                durability, protection, enchant,
                diamondSound ? SoundEvents.ITEM_ARMOR_EQUIP_DIAMOND : SoundEvents.ITEM_ARMOR_EQUIP_IRON, toughness);
    }

    private ModMaterials() {}
}
