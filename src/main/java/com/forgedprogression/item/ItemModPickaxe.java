package com.forgedprogression.item;

import net.minecraft.item.Item;
import net.minecraft.item.ItemPickaxe;

/** ItemPickaxe's constructor is protected in 1.12.2, so expose it. */
public class ItemModPickaxe extends ItemPickaxe {
    public ItemModPickaxe(Item.ToolMaterial material) {
        super(material);
    }
}
