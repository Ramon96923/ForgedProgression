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
    public static final Item COPPER_INGOT    = reg(new Item(), "copper_ingot");
    public static final Item TIN_INGOT       = reg(new Item(), "tin_ingot");
    public static final Item BRONZE_INGOT    = reg(new Item(), "bronze_ingot");
    public static final Item STEEL_BLEND     = reg(new Item(), "steel_blend");
    public static final Item STEEL_INGOT     = reg(new Item(), "steel_ingot");
    public static final Item RUBY            = reg(new Item(), "ruby");
    public static final Item URANIUM_INGOT   = reg(new Item(), "uranium_ingot");
    public static final Item PLUTONIUM_INGOT = reg(new Item(), "plutonium_ingot");

    // tools: material, axe damage, axe speed
    public static final Item BRONZE_PICKAXE;

    static {
        tools("copper", TOOL_COPPER, 7.0F, -3.2F);
        BRONZE_PICKAXE = tools("bronze", TOOL_BRONZE, 7.5F, -3.15F);
        tools("steel", TOOL_STEEL, 8.0F, -3.0F);
        tools("ruby", TOOL_RUBY, 8.5F, -3.0F);
        tools("uranium", TOOL_URANIUM, 9.0F, -3.0F);
        tools("plutonium", TOOL_PLUTONIUM, 10.0F, -2.9F);

        armor("copper", ARMOR_COPPER);
        armor("bronze", ARMOR_BRONZE);
        armor("steel", ARMOR_STEEL);
        armor("ruby", ARMOR_RUBY);
        armor("uranium", ARMOR_URANIUM);
        armor("plutonium", ARMOR_PLUTONIUM);

        // anvil repair materials
        TOOL_COPPER.setRepairItem(new ItemStack(COPPER_INGOT));
        TOOL_BRONZE.setRepairItem(new ItemStack(BRONZE_INGOT));
        TOOL_STEEL.setRepairItem(new ItemStack(STEEL_INGOT));
        TOOL_RUBY.setRepairItem(new ItemStack(RUBY));
        TOOL_URANIUM.setRepairItem(new ItemStack(URANIUM_INGOT));
        TOOL_PLUTONIUM.setRepairItem(new ItemStack(PLUTONIUM_INGOT));
        ARMOR_COPPER.setRepairItem(new ItemStack(COPPER_INGOT));
        ARMOR_BRONZE.setRepairItem(new ItemStack(BRONZE_INGOT));
        ARMOR_STEEL.setRepairItem(new ItemStack(STEEL_INGOT));
        ARMOR_RUBY.setRepairItem(new ItemStack(RUBY));
        ARMOR_URANIUM.setRepairItem(new ItemStack(URANIUM_INGOT));
        ARMOR_PLUTONIUM.setRepairItem(new ItemStack(PLUTONIUM_INGOT));
    }

    /** Registers pickaxe, axe, shovel, hoe and sword; returns the pickaxe. */
    private static Item tools(String name, Item.ToolMaterial mat, float axeDamage, float axeSpeed) {
        Item pick = reg(new ItemModPickaxe(mat), name + "_pickaxe");
        reg(new ItemModAxe(mat, axeDamage, axeSpeed), name + "_axe");
        reg(new ItemSpade(mat), name + "_shovel");
        reg(new ItemHoe(mat), name + "_hoe");
        reg(new ItemSword(mat), name + "_sword");
        return pick;
    }

    private static void armor(String name, ItemArmor.ArmorMaterial mat) {
        reg(new ItemArmor(mat, 0, EntityEquipmentSlot.HEAD), name + "_helmet");
        reg(new ItemArmor(mat, 0, EntityEquipmentSlot.CHEST), name + "_chestplate");
        reg(new ItemArmor(mat, 0, EntityEquipmentSlot.LEGS), name + "_leggings");
        reg(new ItemArmor(mat, 0, EntityEquipmentSlot.FEET), name + "_boots");
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
