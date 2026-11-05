package com.forgedprogression.world;

import com.forgedprogression.ForgedProgression;
import com.forgedprogression.ModConfig;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.boss.EntityDragon;
import net.minecraft.entity.boss.EntityWither;
import net.minecraft.entity.boss.dragon.phase.PhaseList;
import net.minecraft.entity.effect.EntityLightningBolt;
import net.minecraft.entity.monster.EntityEnderman;
import net.minecraft.entity.monster.EntityWitherSkeleton;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityWitherSkull;
import net.minecraft.init.MobEffects;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.living.LivingHealEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.ArrayList;
import java.util.List;

/**
 * Makes the Ender Dragon and Wither keep up with the new gear.
 * Dragon: more health, harder hits, wither on hit, lightning strikes, endermen reinforcements.
 * Wither: more health, harder skulls, extra blue-skull volleys, wither skeleton minions.
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

    @SubscribeEvent
    public static void onUpdate(LivingEvent.LivingUpdateEvent event) {
        EntityLivingBase entity = event.getEntityLiving();
        if (entity.world.isRemote) {
            return;
        }
        if (entity instanceof EntityWither) {
            tickWither((EntityWither) entity);
        } else if (entity instanceof EntityDragon) {
            tickDragon((EntityDragon) entity);
        }
    }

    // ---------------------------------------------------------------- dragon

    private static void tickDragon(EntityDragon dragon) {
        ModConfig.DragonSettings cfg = ModConfig.bosses.dragon;
        if (!cfg.enabled || dragon.getHealth() <= 0
                || dragon.getPhaseManager().getCurrentPhase().getType() == PhaseList.DYING) {
            return;
        }
        World world = dragon.world;
        int t = dragon.ticksExisted;

        if (cfg.lightningIntervalSeconds > 0 && t % (cfg.lightningIntervalSeconds * 20) == 0) {
            EntityPlayer target = randomFighter(dragon);
            if (target != null) {
                // lands near the player, not dead on, so moving keeps you safe
                double x = target.posX + (world.rand.nextDouble() - 0.5) * 3.0;
                double z = target.posZ + (world.rand.nextDouble() - 0.5) * 3.0;
                world.addWeatherEffect(new EntityLightningBolt(world, x, target.posY, z, false));
            }
        }

        if (cfg.endermenPerWave > 0 && t % (cfg.endermenIntervalSeconds * 20) == 0) {
            EntityPlayer target = randomFighter(dragon);
            if (target != null) {
                for (int i = 0; i < cfg.endermenPerWave; i++) {
                    BlockPos spot = findSpawnSpot(world, target, 6, 3);
                    if (spot == null) {
                        continue;
                    }
                    EntityEnderman enderman = new EntityEnderman(world);
                    enderman.setLocationAndAngles(spot.getX() + 0.5, spot.getY(), spot.getZ() + 0.5,
                            world.rand.nextFloat() * 360F, 0F);
                    enderman.onInitialSpawn(world.getDifficultyForLocation(spot), null);
                    world.spawnEntity(enderman);
                    enderman.setAttackTarget(target);
                }
            }
        }
    }

    /** A random survival player within 150 blocks of the dragon. */
    private static EntityPlayer randomFighter(EntityDragon dragon) {
        List<EntityPlayer> nearby = new ArrayList<>();
        for (EntityPlayer p : dragon.world.playerEntities) {
            if (!p.isSpectator() && !p.capabilities.isCreativeMode && p.isEntityAlive()
                    && p.getDistanceSq(dragon) < 150 * 150) {
                nearby.add(p);
            }
        }
        return nearby.isEmpty() ? null : nearby.get(dragon.world.rand.nextInt(nearby.size()));
    }

    // ---------------------------------------------------------------- wither

    private static void tickWither(EntityWither wither) {
        ModConfig.WitherSettings cfg = ModConfig.bosses.wither;
        if (!cfg.enabled || wither.getInvulTime() > 0 || wither.getHealth() <= 0) {
            return;
        }
        World world = wither.world;
        int t = wither.ticksExisted;

        // extra volley of blue skulls at its current target
        EntityLivingBase target = wither.getAttackTarget();
        if (cfg.skullVolleyIntervalSeconds > 0 && target != null && t % (cfg.skullVolleyIntervalSeconds * 20) == 0) {
            double x = wither.posX, y = wither.posY + 3.0, z = wither.posZ;
            for (int i = 0; i < cfg.skullVolleySize; i++) {
                double dx = target.posX - x + (world.rand.nextDouble() - 0.5) * 3.0;
                double dy = target.posY + target.getEyeHeight() * 0.5 - y;
                double dz = target.posZ - z + (world.rand.nextDouble() - 0.5) * 3.0;
                EntityWitherSkull skull = new EntityWitherSkull(world, wither, dx, dy, dz);
                skull.setPosition(x, y, z);
                skull.setInvulnerable(true);
                world.spawnEntity(skull);
            }
            world.playEvent(null, 1024, new BlockPos(wither), 0); // wither shoot sound
        }

        // wither skeleton minions; twice as many below half health
        if (cfg.minionsPerWave > 0 && t % (cfg.minionIntervalSeconds * 20) == 0) {
            int count = cfg.minionsPerWave * (wither.getHealth() < wither.getMaxHealth() / 2.0F ? 2 : 1);
            for (int i = 0; i < count; i++) {
                BlockPos spot = findSpawnSpot(world, wither, 4, 2);
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
    }

    /** A random spot near the entity with solid ground and 3 blocks of air, searching down from just above it. */
    static BlockPos findSpawnSpot(World world, Entity center, int radius, int above) {
        for (int attempt = 0; attempt < 10; attempt++) {
            int x = (int) Math.floor(center.posX) + world.rand.nextInt(radius * 2 + 1) - radius;
            int z = (int) Math.floor(center.posZ) + world.rand.nextInt(radius * 2 + 1) - radius;
            int top = (int) Math.floor(center.posY) + above;
            for (int y = top; y > top - 20 && y > 1; y--) {
                BlockPos feet = new BlockPos(x, y, z);
                if (world.getBlockState(feet.down()).isSideSolid(world, feet.down(), EnumFacing.UP)
                        && world.isAirBlock(feet) && world.isAirBlock(feet.up()) && world.isAirBlock(feet.up(2))) {
                    return feet;
                }
            }
        }
        return null;
    }

    private BossHandler() {}
}
