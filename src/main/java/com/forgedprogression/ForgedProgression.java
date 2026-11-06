package com.forgedprogression;

import com.forgedprogression.init.ModItems;
import com.forgedprogression.init.ModSmelting;
import com.forgedprogression.init.Progression;
import com.forgedprogression.world.OreGenerator;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(modid = ForgedProgression.MODID, name = ForgedProgression.NAME, version = ForgedProgression.VERSION,
        acceptedMinecraftVersions = "[1.12.2]")
public class ForgedProgression {

    public static final String MODID = "forgedprogression";
    public static final String NAME = "Forged Progression";
    public static final String VERSION = "1.6.0";

    public static final Logger LOGGER = LogManager.getLogger(NAME);

    @SidedProxy(clientSide = "com.forgedprogression.client.ClientProxy", serverSide = "com.forgedprogression.CommonProxy")
    public static CommonProxy proxy;

    public static final CreativeTabs TAB = new CreativeTabs(MODID) {
        @Override
        public ItemStack getTabIconItem() {
            return new ItemStack(ModItems.BRONZE_PICKAXE);
        }
    };

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        GameRegistry.registerWorldGenerator(new OreGenerator(), 3);
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        ModSmelting.register();
        Progression.apply();
        proxy.init();
    }
}
