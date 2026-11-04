package com.forgedprogression.init;

import com.forgedprogression.ForgedProgression;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.oredict.OreDictionary;
import net.minecraftforge.registries.IForgeRegistry;

@Mod.EventBusSubscriber(modid = ForgedProgression.MODID)
public final class RegistryHandler {

    @SubscribeEvent
    public static void registerBlocks(RegistryEvent.Register<Block> event) {
        event.getRegistry().registerAll(ModBlocks.BLOCKS.toArray(new Block[0]));
    }

    @SubscribeEvent
    public static void registerItems(RegistryEvent.Register<Item> event) {
        IForgeRegistry<Item> registry = event.getRegistry();
        for (Block block : ModBlocks.BLOCKS) {
            ItemBlock itemBlock = new ItemBlock(block);
            itemBlock.setRegistryName(block.getRegistryName());
            registry.register(itemBlock);
        }
        for (Item item : ModItems.ITEMS) {
            registry.register(item);
        }
        registerOreDictionary();
    }

    /** Ore dictionary names make these interchangeable with the same metals from other mods. */
    private static void registerOreDictionary() {
        OreDictionary.registerOre("oreCopper", ModBlocks.COPPER_ORE);
        OreDictionary.registerOre("oreTin", ModBlocks.TIN_ORE);
        OreDictionary.registerOre("oreRuby", ModBlocks.RUBY_ORE);
        OreDictionary.registerOre("oreUranium", ModBlocks.URANIUM_ORE);
        OreDictionary.registerOre("orePlutonium", ModBlocks.PLUTONIUM_ORE);

        OreDictionary.registerOre("blockCopper", ModBlocks.COPPER_BLOCK);
        OreDictionary.registerOre("blockTin", ModBlocks.TIN_BLOCK);
        OreDictionary.registerOre("blockBronze", ModBlocks.BRONZE_BLOCK);
        OreDictionary.registerOre("blockSteel", ModBlocks.STEEL_BLOCK);
        OreDictionary.registerOre("blockRuby", ModBlocks.RUBY_BLOCK);
        OreDictionary.registerOre("blockUranium", ModBlocks.URANIUM_BLOCK);
        OreDictionary.registerOre("blockPlutonium", ModBlocks.PLUTONIUM_BLOCK);

        OreDictionary.registerOre("ingotCopper", ModItems.COPPER_INGOT);
        OreDictionary.registerOre("ingotTin", ModItems.TIN_INGOT);
        OreDictionary.registerOre("ingotBronze", ModItems.BRONZE_INGOT);
        OreDictionary.registerOre("ingotSteel", ModItems.STEEL_INGOT);
        OreDictionary.registerOre("gemRuby", ModItems.RUBY);
        OreDictionary.registerOre("ingotUranium", ModItems.URANIUM_INGOT);
        OreDictionary.registerOre("ingotPlutonium", ModItems.PLUTONIUM_INGOT);
    }

    private RegistryHandler() {}
}
