package com.forgedprogression.item;

import net.minecraft.item.Item;
import net.minecraft.item.ItemAxe;

/**
 * ItemAxe(material) only works for vanilla materials (it indexes a damage array by enum ordinal
 * and crashes for custom ones), so custom axes must pass damage and speed explicitly.
 */
public class ItemModAxe extends ItemAxe {
    public ItemModAxe(Item.ToolMaterial material, float damage, float speed) {
        super(material, damage, speed);
    }
}
