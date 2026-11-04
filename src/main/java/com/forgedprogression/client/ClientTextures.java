package com.forgedprogression.client;

import com.forgedprogression.ForgedProgression;
import com.forgedprogression.client.Recolor.Match;
import com.forgedprogression.client.Recolor.Spec;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Every texture in the mod is a recolor of a vanilla one, built when textures load.
 * This also means resource packs that change iron/diamond items change these too.
 */
@Mod.EventBusSubscriber(modid = ForgedProgression.MODID, value = Side.CLIENT)
public final class ClientTextures {

    /** A material's color and which vanilla set it's recolored from. */
    private static final class Metal {
        final String name;
        final int color;
        final boolean gem; // recolor from diamond instead of iron

        Metal(String name, int color, boolean gem) {
            this.name = name;
            this.color = color;
            this.gem = gem;
        }

        String base() { return gem ? "diamond" : "iron"; }
        Match match() { return gem ? Match.CYAN : Match.GRAY; }
    }

    private static final Metal COPPER    = new Metal("copper",    0xE07C48, false);
    private static final Metal TIN       = new Metal("tin",       0xB4C3D7, false);
    private static final Metal BRONZE    = new Metal("bronze",    0xCD9646, false);
    private static final Metal STEEL     = new Metal("steel",     0x78829A, false);
    private static final Metal RUBY      = new Metal("ruby",      0xDC283C, true);
    private static final Metal URANIUM   = new Metal("uranium",   0x6EDC3C, false);
    private static final Metal PLUTONIUM = new Metal("plutonium", 0xAA46DC, false);

    private static final Metal[] ALL = {COPPER, TIN, BRONZE, STEEL, RUBY, URANIUM, PLUTONIUM};
    private static final Metal[] GEAR = {COPPER, BRONZE, STEEL, RUBY, URANIUM, PLUTONIUM};
    private static final String[] PIECES = {"pickaxe", "axe", "shovel", "hoe", "sword",
            "helmet", "chestplate", "leggings", "boots"};

    private static final String MC = "minecraft:textures/";

    /** Atlas sprite name -> how to build it. */
    private static Map<String, Spec> sprites() {
        Map<String, Spec> map = new LinkedHashMap<>();

        for (Metal m : ALL) {
            // ingot / gem
            String item = m.gem ? m.name : m.name + "_ingot";
            String src = m.gem ? MC + "items/diamond.png" : MC + "items/iron_ingot.png";
            map.put(item(item), new Spec(src, m.match(), m.color));
            // storage block
            map.put(block(m.name + "_block"), new Spec(MC + "blocks/" + m.base() + "_block.png", m.match(), m.color));
        }
        map.put(item("steel_blend"), new Spec(MC + "items/gunpowder.png", Match.GRAY, 0x5A606E));

        for (Metal m : GEAR) {
            for (String piece : PIECES) {
                map.put(item(m.name + "_" + piece),
                        new Spec(MC + "items/" + m.base() + "_" + piece + ".png", m.match(), m.color));
            }
        }

        // ores: recolor the spots on iron/diamond ore
        map.put(block("copper_ore"), new Spec(MC + "blocks/iron_ore.png", Match.COLORED, COPPER.color));
        map.put(block("tin_ore"), new Spec(MC + "blocks/iron_ore.png", Match.COLORED, TIN.color));
        map.put(block("ruby_ore"), new Spec(MC + "blocks/diamond_ore.png", Match.COLORED, RUBY.color));
        map.put(block("uranium_ore"), new Spec(MC + "blocks/iron_ore.png", Match.COLORED, URANIUM.color));
        // plutonium generates in the End, so its spots sit on end stone
        map.put(block("plutonium_ore"), new Spec(MC + "blocks/iron_ore.png", Match.COLORED, PLUTONIUM.color,
                MC + "blocks/end_stone.png"));
        return map;
    }

    private static String item(String name) { return ForgedProgression.MODID + ":items/" + name; }
    private static String block(String name) { return ForgedProgression.MODID + ":blocks/" + name; }

    @SubscribeEvent
    public static void onTextureStitch(TextureStitchEvent.Pre event) {
        for (Map.Entry<String, Spec> e : sprites().entrySet()) {
            event.getMap().setTextureEntry(new RecoloredSprite(e.getKey(), e.getValue()));
        }
    }

    /** Armor as worn on the body (not in the atlas). Called once during client init. */
    public static void registerArmorTextures() {
        TextureManager tm = Minecraft.getMinecraft().getTextureManager();
        for (Metal m : GEAR) {
            for (int layer = 1; layer <= 2; layer++) {
                ResourceLocation target = new ResourceLocation(ForgedProgression.MODID,
                        "textures/models/armor/" + m.name + "_layer_" + layer + ".png");
                Spec spec = new Spec(MC + "models/armor/" + m.base() + "_layer_" + layer + ".png", m.match(), m.color);
                tm.loadTexture(target, new RecoloredTexture(spec));
            }
        }
    }

    private ClientTextures() {}
}
