package com.forgedprogression.world;

import com.forgedprogression.ForgedProgression;
import com.forgedprogression.ModConfig;
import com.forgedprogression.init.ModItems;
import net.minecraft.item.Item;
import net.minecraft.world.storage.loot.LootEntry;
import net.minecraft.world.storage.loot.LootEntryEmpty;
import net.minecraft.world.storage.loot.LootEntryItem;
import net.minecraft.world.storage.loot.LootPool;
import net.minecraft.world.storage.loot.LootTableList;
import net.minecraft.world.storage.loot.RandomValueRange;
import net.minecraft.world.storage.loot.conditions.LootCondition;
import net.minecraft.world.storage.loot.functions.LootFunction;
import net.minecraft.world.storage.loot.functions.SetCount;
import net.minecraftforge.event.LootTableLoadEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/** Adds an extra pool to End City treasure chests: plutonium, some uranium, and a rare neutronium ingot. */
@Mod.EventBusSubscriber(modid = ForgedProgression.MODID)
public final class EndCityLoot {

    private static final LootCondition[] NONE = new LootCondition[0];

    @SubscribeEvent
    public static void onLootLoad(LootTableLoadEvent event) {
        if (!ModConfig.endCityLoot || !event.getName().equals(LootTableList.CHESTS_END_CITY_TREASURE)) {
            return;
        }
        LootEntry[] entries = {
                item(ModItems.PLUTONIUM_INGOT, 10, 2, 6, "plutonium_ingot"),
                item(ModItems.URANIUM_INGOT, 5, 1, 3, "uranium_ingot"),
                item(ModItems.NEUTRONIUM_INGOT, 1, 1, 1, "neutronium_ingot"),
                new LootEntryEmpty(4, 0, NONE, ForgedProgression.MODID + ":nothing"),
        };
        // 1 to 3 rolls per chest
        event.getTable().addPool(new LootPool(entries, NONE, new RandomValueRange(1, 3), new RandomValueRange(0, 0),
                ForgedProgression.MODID + "_end_city"));
    }

    private static LootEntry item(Item item, int weight, int min, int max, String name) {
        LootFunction[] count = {new SetCount(NONE, new RandomValueRange(min, max))};
        return new LootEntryItem(item, weight, 0, count, NONE, ForgedProgression.MODID + ":" + name);
    }

    private EndCityLoot() {}
}
