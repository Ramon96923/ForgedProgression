package com.forgedprogression.world;

import com.forgedprogression.ForgedProgression;
import com.forgedprogression.ModConfig;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.boss.EntityDragon;
import net.minecraft.entity.boss.EntityWither;
import net.minecraft.entity.monster.EntityWitherSkeleton;
import net.minecraft.init.MobEffects;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.living.LivingHealEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/**
 * Makes the Ender Dragon and Wither keep up with the new gear:
 * more health, harder hits, less damage taken, plus a twist each
 * (the dragon's attacks wither you; the Wither calls in wither skeletons when below half health).
 * Everything is tunable in the "bosses" config section.
 */
@Mod.EventBusSubscriber(modid = ForgedProgression.MODID)
public final class BossHandler {

    /** Saved on the entity so health is only scaled once, not again every time the chunk reloads. */
    private static final String BUFFED_TAG = ForgedProgression.MODID + ".buffed";

    @SubscribeEvent
    public static void onJoin(EntityJoinWorldEvent event) {
        Entity e = event.getEntity();
        if (event.getWorld().isRemote || e.getTags().contains(BUFFED_TAG)) {
            return;
        }
        ModConfig.Bosses cfg = ModConfig.bosses;
        if (e instanceof EntityDragon && cfg.dragon.enabled) {
            scaleHealth((EntityLivingBase) e, cfg.dragon.healthMultiplier);
            e.addTag(BUFFED_TAG);
        } else if (e instanceof EntityWither && cfg.wither.enabled) {
            EntityWither wither = (EntityWither) e;
            scaleHealth(wither, cfg.wither.healthMultiplier);
            if (wither.getInvulTime() > 0) {
                // vanilla spawns the Wither at 1/3 health and charges it up; keep that ratio
                wither.setHealth(wither.getMaxHealth() / 3.0F);
            }
            e.addTag(BUFFED_TAG);
        }
    }

    private static void scaleHealth(EntityLivingBase boss, double multiplier) {
        IAttributeInstance health = boss.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH);
        health.setBaseValue(health.getBaseValue() * multiplier);
        boss.setHealth(boss.getMaxHealth());
    }

    /** While the Wither charges up after spawning, scale its healing so it still reaches full health. */
    @SubscribeEvent
    public static void onHeal(LivingHealEvent event) {
        if (event.getEntityLiving() instanceof EntityWither && ModConfig.bosses.wither.enabled
                && ((EntityWither) event.getEntityLiving()).getInvulTime() > 0) {
            event.setAmount(event.getAmount() * (float) ModConfig.bosses.wither.healthMultiplier);
        }
    }

    @SubscribeEvent
    public static void onHurt(LivingHurtEvent event) {
        EntityLivingBase victim = event.getEntityLiving();
        if (victim.world.isRemote) {
            return;
        }
        ModConfig.Bosses cfg = ModConfig.bosses;

        // boss takes damage
        if (victim instanceof EntityDragon && cfg.dragon.enabled) {
            event.setAmount(event.getAmount() * (float) cfg.dragon.damageTakenMultiplier);
        } else if (victim instanceof EntityWither && cfg.wither.enabled) {
            event.setAmount(event.getAmount() * (float) cfg.wither.damageTakenMultiplier);
        }

        // boss deals damage (melee, dragon breath, fireballs and wither skulls all trace back to the boss)
        Entity attacker = event.getSource().getTrueSource();
        if (attacker == victim) {
            return;
        }
        if (attacker instanceof EntityDragon && cfg.dragon.enabled) {
            event.setAmount(event.getAmount() * (float) cfg.dragon.damageMultiplier);
            if (cfg.dragon.witherOnHitSeconds > 0) {
                victim.addPotionEffect(new PotionEffect(MobEffects.WITHER, cfg.dragon.witherOnHitSeconds * 20, 1));
            }
        } else if (attacker instanceof EntityWither && cfg.wither.enabled) {
            event.setAmount(event.getAmount() * (float) cfg.wither.damageMultiplier);
        }
    }

    /** The Wither summons wither skeletons every so often once it drops below half health. */
    @SubscribeEvent
    public static void onUpdate(LivingEvent.LivingUpdateEvent event) {
        if (!(event.getEntityLiving() instanceof EntityWither)) {
            return;
        }
        EntityWither wither = (EntityWither) event.getEntityLiving();
        ModConfig.WitherSettings cfg = ModConfig.bosses.wither;
        World world = wither.world;
        if (world.isRemote || !cfg.enabled || cfg.minionsPerWave <= 0 || cfg.minionIntervalSeconds <= 0
                || wither.getInvulTime() > 0 || wither.getHealth() > wither.getMaxHealth() / 2.0F
                || wither.ticksExisted % (cfg.minionIntervalSeconds * 20) != 0) {
            return;
        }
        for (int i = 0; i < cfg.minionsPerWave; i++) {
            BlockPos spot = findSpawnSpot(world, wither);
            if (spot == null) {
                continue;
            }
            EntityWitherSkeleton skeleton = new EntityWitherSkeleton(world);
            skeleton.setLocationAndAngles(spot.getX() + 0.5, spot.getY(), spot.getZ() + 0.5,
                    world.rand.nextFloat() * 360F, 0F);
            skeleton.onInitialSpawn(world.getDifficultyForLocation(spot), null);
            world.spawnEntity(skeleton);
        }
    }

    /** A random nearby spot with solid ground and 3 blocks of air, searching down from the Wither. */
    private static BlockPos findSpawnSpot(World world, EntityWither wither) {
        for (int attempt = 0; attempt < 8; attempt++) {
            int x = (int) Math.floor(wither.posX) + world.rand.nextInt(9) - 4;
            int z = (int) Math.floor(wither.posZ) + world.rand.nextInt(9) - 4;
            int top = (int) Math.floor(wither.posY) + 2;
            for (int y = top; y > top - 16 && y > 1; y--) {
                BlockPos feet = new BlockPos(x, y, z);
                if (world.getBlockState(feet.down()).isSideSolid(world, feet.down(), net.minecraft.util.EnumFacing.UP)
                        && world.isAirBlock(feet) && world.isAirBlock(feet.up()) && world.isAirBlock(feet.up(2))) {
                    return feet;
                }
            }
        }
        return null;
    }

    private BossHandler() {}
}
