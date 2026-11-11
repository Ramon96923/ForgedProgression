package com.forgedprogression.compressor;

import com.forgedprogression.ForgedProgression;
import com.forgedprogression.init.ModItems;
import net.minecraft.init.Items;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.oredict.ShapedOreRecipe;

import java.util.ArrayList;
import java.util.List;

/**
 * Singularity Compressor recipes: the same layouts as the normal crafting recipes, but more output.
 * Each slot always takes exactly one item per craft.
 */
public final class CompressorRecipes {

    private static List<ShapedOreRecipe> recipes;

    private static List<ShapedOreRecipe> recipes() {
        if (recipes == null) {
            ResourceLocation group = new ResourceLocation(ForgedProgression.MODID, "compressor");
            recipes = new ArrayList<>();
            // normal crafting: 1 neutronium
            recipes.add(new ShapedOreRecipe(group, new ItemStack(ModItems.NEUTRONIUM_INGOT, 4),
                    "PUP", "UIU", "PUP", 'P', "ingotPlutonium", 'U', "ingotUranium", 'I', "ingotIron"));
            // normal crafting: 1 quasium
            recipes.add(new ShapedOreRecipe(group, new ItemStack(ModItems.QUASIUM_INGOT, 3),
                    "QNQ", "NCN", "QNQ", 'Q', "gemQuasium", 'N', "ingotNeutronium", 'C', new ItemStack(Items.END_CRYSTAL)));
            // normal crafting: 1 borealium
            recipes.add(new ShapedOreRecipe(group, new ItemStack(ModItems.BOREALIUM_INGOT, 4),
                    "NQN", "NQN", "NQN", 'N', "blockNeutronium", 'Q', "blockQuasium"));
        }
        return recipes;
    }

    /** The compressed result for what's in the grid, or EMPTY. */
    public static ItemStack getResult(InventoryCrafting grid, World world) {
        for (ShapedOreRecipe recipe : recipes()) {
            if (recipe.matches(grid, world)) {
                return recipe.getCraftingResult(grid);
            }
        }
        return ItemStack.EMPTY;
    }

    private CompressorRecipes() {}
}
