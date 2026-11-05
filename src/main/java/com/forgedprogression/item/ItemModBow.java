package com.forgedprogression.item;

import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.init.Enchantments;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.IItemPropertyGetter;
import net.minecraft.item.ItemArrow;
import net.minecraft.item.ItemBow;
import net.minecraft.item.ItemStack;
import net.minecraft.stats.StatList;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.world.World;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.oredict.OreDictionary;

import javax.annotation.Nullable;

/**
 * A bow with its own draw time, arrow speed, arrow damage and built-in knockback.
 * Vanilla bow for reference: 20 tick draw, speed 3.0, arrow base damage 2.0, 384 durability.
 */
public class ItemModBow extends ItemBow {

    private final int drawTicks;
    private final float velocityMultiplier;
    private final double damageMultiplier;
    private final int bonusKnockback;
    private final int enchantability;
    @Nullable private final String repairOre;

    public ItemModBow(int durability, int drawTicks, float velocityMultiplier, double damageMultiplier,
                      int bonusKnockback, int enchantability, @Nullable String repairOre) {
        this.drawTicks = drawTicks;
        this.velocityMultiplier = velocityMultiplier;
        this.damageMultiplier = damageMultiplier;
        this.bonusKnockback = bonusKnockback;
        this.enchantability = enchantability;
        this.repairOre = repairOre;
        setMaxDamage(durability);

        // replaces ItemBow's "pull" so the drawing animation matches this bow's draw time
        addPropertyOverride(new ResourceLocation("pull"), new IItemPropertyGetter() {
            @Override
            @SideOnly(Side.CLIENT)
            public float apply(ItemStack stack, @Nullable World world, @Nullable EntityLivingBase entity) {
                if (entity == null || entity.getActiveItemStack().getItem() != ItemModBow.this) {
                    return 0.0F;
                }
                return (stack.getMaxItemUseDuration() - entity.getItemInUseCount()) / (float) ItemModBow.this.drawTicks;
            }
        });
    }

    public int getDrawTicks() {
        return drawTicks;
    }

    /** Same curve as vanilla, stretched over this bow's draw time. 0..1 */
    public float getPower(int ticksUsed) {
        float f = ticksUsed / (float) drawTicks;
        f = (f * f + f * 2.0F) / 3.0F;
        return Math.min(f, 1.0F);
    }

    @Override
    public void onPlayerStoppedUsing(ItemStack stack, World world, EntityLivingBase shooter, int timeLeft) {
        if (!(shooter instanceof EntityPlayer)) {
            return;
        }
        EntityPlayer player = (EntityPlayer) shooter;
        boolean infiniteOrCreative = player.capabilities.isCreativeMode
                || EnchantmentHelper.getEnchantmentLevel(Enchantments.INFINITY, stack) > 0;
        ItemStack ammo = findAmmoStack(player);

        int ticksUsed = getMaxItemUseDuration(stack) - timeLeft;
        ticksUsed = ForgeEventFactory.onArrowLoose(stack, world, player, ticksUsed, !ammo.isEmpty() || infiniteOrCreative);
        if (ticksUsed < 0 || (ammo.isEmpty() && !infiniteOrCreative)) {
            return;
        }
        if (ammo.isEmpty()) {
            ammo = new ItemStack(Items.ARROW);
        }

        float power = getPower(ticksUsed);
        if (power < 0.1F) {
            return;
        }
        boolean noConsume = player.capabilities.isCreativeMode
                || (ammo.getItem() instanceof ItemArrow && ((ItemArrow) ammo.getItem()).isInfinite(ammo, stack, player));

        if (!world.isRemote) {
            ItemArrow arrowItem = (ItemArrow) (ammo.getItem() instanceof ItemArrow ? ammo.getItem() : Items.ARROW);
            EntityArrow arrow = arrowItem.createArrow(world, ammo, player);
            arrow = customizeArrow(arrow);
            arrow.shoot(player, player.rotationPitch, player.rotationYaw, 0.0F, power * 3.0F * velocityMultiplier, 1.0F);
            if (power == 1.0F) {
                arrow.setIsCritical(true);
            }
            arrow.setDamage(arrow.getDamage() * damageMultiplier);

            int powerLevel = EnchantmentHelper.getEnchantmentLevel(Enchantments.POWER, stack);
            if (powerLevel > 0) {
                arrow.setDamage(arrow.getDamage() + powerLevel * 0.5D + 0.5D);
            }
            int knockback = EnchantmentHelper.getEnchantmentLevel(Enchantments.PUNCH, stack) + bonusKnockback;
            if (knockback > 0) {
                arrow.setKnockbackStrength(knockback);
            }
            if (EnchantmentHelper.getEnchantmentLevel(Enchantments.FLAME, stack) > 0) {
                arrow.setFire(100);
            }
            stack.damageItem(1, player);
            if (noConsume || player.capabilities.isCreativeMode
                    && (ammo.getItem() == Items.SPECTRAL_ARROW || ammo.getItem() == Items.TIPPED_ARROW)) {
                arrow.pickupStatus = EntityArrow.PickupStatus.CREATIVE_ONLY;
            }
            world.spawnEntity(arrow);
        }

        // heavier bows sound deeper
        float pitch = (1.0F / (itemRand.nextFloat() * 0.4F + 1.2F) + power * 0.5F) / (float) Math.sqrt(velocityMultiplier);
        world.playSound(null, player.posX, player.posY, player.posZ, SoundEvents.ENTITY_ARROW_SHOOT,
                SoundCategory.PLAYERS, 1.0F, pitch);

        if (!noConsume && !player.capabilities.isCreativeMode) {
            ammo.shrink(1);
            if (ammo.isEmpty()) {
                player.inventory.deleteStack(ammo);
            }
        }
        player.addStat(StatList.getObjectUseStats(this));
    }

    /** Same search order as vanilla: offhand, main hand, then the inventory. */
    private static ItemStack findAmmoStack(EntityPlayer player) {
        if (isAmmo(player.getHeldItem(EnumHand.OFF_HAND))) {
            return player.getHeldItem(EnumHand.OFF_HAND);
        }
        if (isAmmo(player.getHeldItem(EnumHand.MAIN_HAND))) {
            return player.getHeldItem(EnumHand.MAIN_HAND);
        }
        for (int i = 0; i < player.inventory.getSizeInventory(); i++) {
            ItemStack s = player.inventory.getStackInSlot(i);
            if (isAmmo(s)) {
                return s;
            }
        }
        return ItemStack.EMPTY;
    }

    private static boolean isAmmo(ItemStack stack) {
        return !stack.isEmpty() && stack.getItem() instanceof ItemArrow;
    }

    @Override
    public int getItemEnchantability() {
        return enchantability;
    }

    @Override
    public boolean getIsRepairable(ItemStack toRepair, ItemStack repair) {
        if (repairOre == null || repair.isEmpty()) {
            return false;
        }
        for (int id : OreDictionary.getOreIDs(repair)) {
            if (OreDictionary.getOreName(id).equals(repairOre)) {
                return true;
            }
        }
        return false;
    }
}
