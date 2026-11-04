package com.forgedprogression.init;

import com.forgedprogression.ForgedProgression;
import com.forgedprogression.item.ItemModAxe;
import com.forgedprogression.item.ItemModPickaxe;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemHoe;
import net.minecraft.item.ItemSpade;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;

import java.util.ArrayList;
import java.util.List;

import static com.forgedprogression.init.ModMaterials.*;

public final class ModItems {

    public static final List<Item> ITEMS = new ArrayList<>();

    // materials
    public static final Item COPPER_INGOT = reg(new Item(), "copper_ingot");
    public static final Item TIN_INGOT    = reg(new Item(), "tin_ingot");
    public static final Item BRONZE_INGOT = reg(new Item(), "bronze_ingot");
    public static final Item STEEL_BLEND  = reg(new Item(), "steel_blend");
    public static final Item STEEL_INGOT  = reg(new Item(), "steel_ingot");

    // copper tools (tier 1)
    public static final Item COPPER_PICKAXE = reg(new ItemModPickaxe(TOOL_COPPER), "copper_pickaxe");
    public static final Item COPPER_AXE     = reg(new ItemModAxe(TOOL_COPPER, 7.0F, -3.2F), "copper_axe");
    public static final Item COPPER_SHOVEL  = reg(new ItemSpade(TOOL_COPPER), "copper_shovel");
    public static final Item COPPER_HOE     = reg(new ItemHoe(TOOL_COPPER), "copper_hoe");
    public static final Item COPPER_SWORD   = reg(new ItemSword(TOOL_COPPER), "copper_sword");

    // bronze tools (tier 2, mines iron)
    public static final Item BRONZE_PICKAXE = reg(new ItemModPickaxe(TOOL_BRONZE), "bronze_pickaxe");
    public static final Item BRONZE_AXE     = reg(new ItemModAxe(TOOL_BRONZE, 7.5F, -3.15F), "bronze_axe");
    public static final Item BRONZE_SHOVEL  = reg(new ItemSpade(TOOL_BRONZE), "bronze_shovel");
    public static final Item BRONZE_HOE     = reg(new ItemHoe(TOOL_BRONZE), "bronze_hoe");
    public static final Item BRONZE_SWORD   = reg(new ItemSword(TOOL_BRONZE), "bronze_sword");

    // steel tools (tier 3, mines diamond)
    public static final Item STEEL_PICKAXE = reg(new ItemModPickaxe(TOOL_STEEL), "steel_pickaxe");
    public static final Item STEEL_AXE     = reg(new ItemModAxe(TOOL_STEEL, 8.0F, -3.0F), "steel_axe");
    public static final Item STEEL_SHOVEL  = reg(new ItemSpade(TOOL_STEEL), "steel_shovel");
    public static final Item STEEL_HOE     = reg(new ItemHoe(TOOL_STEEL), "steel_hoe");
    public static final Item STEEL_SWORD   = reg(new ItemSword(TOOL_STEEL), "steel_sword");

    // armor
    public static final Item COPPER_HELMET     = reg(new ItemArmor(ARMOR_COPPER, 0, EntityEquipmentSlot.HEAD), "copper_helmet");
    public static final Item COPPER_CHESTPLATE = reg(new ItemArmor(ARMOR_COPPER, 0, EntityEquipmentSlot.CHEST), "copper_chestplate");
    public static final Item COPPER_LEGGINGS   = reg(new ItemArmor(ARMOR_COPPER, 0, EntityEquipmentSlot.LEGS), "copper_leggings");
    public static final Item COPPER_BOOTS      = reg(new ItemArmor(ARMOR_COPPER, 0, EntityEquipmentSlot.FEET), "copper_boots");

    public static final Item BRONZE_HELMET     = reg(new ItemArmor(ARMOR_BRONZE, 0, EntityEquipmentSlot.HEAD), "bronze_helmet");
    public static final Item BRONZE_CHESTPLATE = reg(new ItemArmor(ARMOR_BRONZE, 0, EntityEquipmentSlot.CHEST), "bronze_chestplate");
    public static final Item BRONZE_LEGGINGS   = reg(new ItemArmor(ARMOR_BRONZE, 0, EntityEquipmentSlot.LEGS), "bronze_leggings");
    public static final Item BRONZE_BOOTS      = reg(new ItemArmor(ARMOR_BRONZE, 0, EntityEquipmentSlot.FEET), "bronze_boots");

    public static final Item STEEL_HELMET     = reg(new ItemArmor(ARMOR_STEEL, 0, EntityEquipmentSlot.HEAD), "steel_helmet");
    public static final Item STEEL_CHESTPLATE = reg(new ItemArmor(ARMOR_STEEL, 0, EntityEquipmentSlot.CHEST), "steel_chestplate");
    public static final Item STEEL_LEGGINGS   = reg(new ItemArmor(ARMOR_STEEL, 0, EntityEquipmentSlot.LEGS), "steel_leggings");
    public static final Item STEEL_BOOTS      = reg(new ItemArmor(ARMOR_STEEL, 0, EntityEquipmentSlot.FEET), "steel_boots");

    static {
        // anvil repair materials
        TOOL_COPPER.setRepairItem(new ItemStack(COPPER_INGOT));
        TOOL_BRONZE.setRepairItem(new ItemStack(BRONZE_INGOT));
        TOOL_STEEL.setRepairItem(new ItemStack(STEEL_INGOT));
        ARMOR_COPPER.setRepairItem(new ItemStack(COPPER_INGOT));
        ARMOR_BRONZE.setRepairItem(new ItemStack(BRONZE_INGOT));
        ARMOR_STEEL.setRepairItem(new ItemStack(STEEL_INGOT));
    }

    private static <T extends Item> T reg(T item, String name) {
        item.setRegistryName(ForgedProgression.MODID, name);
        item.setUnlocalizedName(ForgedProgression.MODID + "." + name);
        item.setCreativeTab(ForgedProgression.TAB);
        ITEMS.add(item);
        return item;
    }

    private ModItems() {}
}
