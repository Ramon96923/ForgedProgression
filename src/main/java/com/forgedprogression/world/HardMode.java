package com.forgedprogression.world;

import com.forgedprogression.ForgedProgression;
import com.forgedprogression.ModConfig;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.boss.EntityDragon;
import net.minecraft.entity.boss.EntityWither;
import net.minecraft.entity.monster.AbstractSkeleton;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.entity.monster.IMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraft.world.storage.MapStorage;
import net.minecraft.world.storage.WorldSavedData;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.living.LivingSpawnEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;

import java.util.Random;
import java.util.UUID;

/**
 * Hard mode: switches on for the whole world the first time any player enters the Nether.
 * Hostile mobs then get more health, hit harder, spawn more often, and zombies/skeletons
 * can spawn wearing (sometimes enchanted) mod armor and wielding mod weapons.
 */
@Mod.EventBusSubscriber(modid = ForgedProgression.MODID)
public final class HardMode {

    private static final UUID HEALTH_ID = UUID.fromString("6f2c8a3e-3b8f-4c41-9d5e-2a7b1c9e4f10");
    /** Saved on each mob so its buff is applied once, not again every time the chunk reloads. */
    private static final String TAG = ForgedProgression.MODID + ".hardmode";
    private static final int VANILLA_MONSTER_CAP = EnumCreatureType.MONSTER.getMaxNumberOfCreature();

    // ---------------------------------------------------------------- saved state

    /** Per-world flag, saved in data/forgedprogression_hardmode.dat. */
    public static class Data extends WorldSavedData {
        public static final String NAME = ForgedProgression.MODID + "_hardmode";
        public boolean active;

        public Data() {
            super(NAME);
        }

        public Data(String name) {
            super(name);
        }

        @Override
        public void readFromNBT(NBTTagCompound nbt) {
            active = nbt.getBoolean("active");
        }

        @Override
        public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
            nbt.setBoolean("active", active);
            return nbt;
        }
    }

    private static Data data(World world) {
        MapStorage storage = world.getMapStorage(); // shared by all dimensions
        Data d = (Data) storage.getOrLoadData(Data.class, Data.NAME);
        if (d == null) {
            d = new Data();
            storage.setData(Data.NAME, d);
        }
        return d;
    }

    public static boolean isActive(World world) {
        return ModConfig.hardMode.enabled && data(world).active;
    }

    // ---------------------------------------------------------------- activation

    @SubscribeEvent
    public static void onChangeDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        World world = event.player.world;
        if (world.isRemote || event.toDim != -1 || !ModConfig.hardMode.enabled) {
            return;
        }
        Data d = data(world);
        if (d.active) {
            return;
        }
        d.active = true;
        d.markDirty();
        applyMobCap(true);
        announce(world.getMinecraftServer());
    }

    private static void announce(MinecraftServer server) {
        if (server == null) {
            return;
        }
        TextComponentString msg = new TextComponentString("The Nether has been opened. Hard mode is now active: "
                + "monsters are stronger, better armed and more numerous.");
        msg.getStyle().setColor(TextFormatting.DARK_RED).setBold(true);
        server.getPlayerList().sendMessage(msg);
        for (EntityPlayerMP p : server.getPlayerList().getPlayers()) {
            p.world.playSound(null, p.posX, p.posY, p.posZ, SoundEvents.ENTITY_WITHER_SPAWN, SoundCategory.HOSTILE, 0.6F, 0.7F);
        }
    }

    /** Re-applies the mob cap when a saved world with hard mode already active is loaded. */
    @SubscribeEvent
    public static void onWorldLoad(WorldEvent.Load event) {
        World world = event.getWorld();
        if (!world.isRemote && world.provider.getDimension() == 0) {
            applyMobCap(isActive(world));
        }
    }

    /**
     * The hostile mob cap is a final field on the vanilla enum with no API to change it, so set it by reflection.
     * If that ever fails (e.g. another mod changed it), hard mode still works through the extra-spawn chance.
     */
    private static void applyMobCap(boolean active) {
        int cap = active ? (int) Math.round(VANILLA_MONSTER_CAP * ModConfig.hardMode.mobCapMultiplier) : VANILLA_MONSTER_CAP;
        try {
            ObfuscationReflectionHelper.setPrivateValue(EnumCreatureType.class, EnumCreatureType.MONSTER, cap,
                    "field_75606_e", "maxNumberOfCreature");
        } catch (Exception e) {
            ForgedProgression.LOGGER.warn("Could not change the hostile mob cap; hard mode will rely on extra spawns", e);
        }
    }

    // ---------------------------------------------------------------- mob buffs

    @SubscribeEvent
    public static void onJoin(EntityJoinWorldEvent event) {
        Entity e = event.getEntity();
        World world = event.getWorld();
        if (world.isRemote || !(e instanceof EntityLiving) || !(e instanceof IMob)
                || e instanceof EntityDragon || e instanceof EntityWither
                || e.getTags().contains(TAG) || !isActive(world)) {
            return;
        }
        e.addTag(TAG);
        EntityLiving mob = (EntityLiving) e;

        IAttributeInstance health = mob.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH);
        if (health != null && health.getModifier(HEALTH_ID) == null) {
            // operation 1: +X% of base health
            health.applyModifier(new AttributeModifier(HEALTH_ID, "Hard mode health",
                    ModConfig.hardMode.mobHealthMultiplier - 1.0, 1));
            mob.setHealth(mob.getMaxHealth());
        }
        equip(mob, world.rand);
    }

    @SubscribeEvent
    public static void onHurt(LivingHurtEvent event) {
        if (!(event.getEntityLiving() instanceof EntityPlayer) || event.getEntityLiving().world.isRemote) {
            return;
        }
        Entity attacker = event.getSource().getTrueSource();
        if (attacker instanceof IMob && !(attacker instanceof EntityDragon) && !(attacker instanceof EntityWither)
                && isActive(attacker.world)) {
            event.setAmount(event.getAmount() * (float) ModConfig.hardMode.mobDamageMultiplier);
        }
    }

    /** Each natural hostile spawn has a chance to bring a second mob of the same kind with it. */
    @SubscribeEvent
    public static void onCheckSpawn(LivingSpawnEvent.CheckSpawn event) {
        World world = event.getWorld();
        if (world.isRemote || event.isSpawner() || event.getResult() == Event.Result.DENY
                || !(event.getEntityLiving() instanceof EntityLiving) || !(event.getEntityLiving() instanceof IMob)
                || !isActive(world) || world.rand.nextDouble() >= ModConfig.hardMode.extraSpawnChance) {
            return;
        }
        Entity created = EntityList.newEntity(event.getEntityLiving().getClass(), world);
        if (!(created instanceof EntityLiving)) {
            return;
        }
        EntityLiving extra = (EntityLiving) created;
        double x = event.getX() + world.rand.nextInt(3) - 1;
        double z = event.getZ() + world.rand.nextInt(3) - 1;
        extra.setLocationAndAngles(x, event.getY(), z, world.rand.nextFloat() * 360F, 0F);
        if (extra.getCanSpawnHere() && extra.isNotColliding()) {
            extra.onInitialSpawn(world.getDifficultyForLocation(new BlockPos(extra)), null);
            world.spawnEntity(extra);
        }
    }

    // ---------------------------------------------------------------- gear

    private static final EntityEquipmentSlot[] ARMOR_SLOTS = {
            EntityEquipmentSlot.HEAD, EntityEquipmentSlot.CHEST, EntityEquipmentSlot.LEGS, EntityEquipmentSlot.FEET};
    private static final String[] ARMOR_NAMES = {"helmet", "chestplate", "leggings", "boots"};
    // tier and weight: mostly early metals, occasionally something nasty
    private static final String[] TIERS = {"copper", "bronze", "steel", "ruby", "uranium"};
    private static final int[] WEIGHTS = {40, 30, 20, 8, 2};

    private static void equip(EntityLiving mob, Random rand) {
        boolean zombie = mob instanceof EntityZombie;
        boolean skeleton = mob instanceof AbstractSkeleton;
        if (!zombie && !skeleton) {
            return;
        }
        ModConfig.HardMode cfg = ModConfig.hardMode;
        for (int i = 0; i < ARMOR_SLOTS.length; i++) {
            if (mob.getItemStackFromSlot(ARMOR_SLOTS[i]).isEmpty() && rand.nextDouble() < cfg.armorChance) {
                ItemStack piece = modItem(randomTier(rand) + "_" + ARMOR_NAMES[i]);
                if (!piece.isEmpty()) {
                    mob.setItemStackToSlot(ARMOR_SLOTS[i], maybeEnchant(piece, rand));
                }
            }
        }
        if (zombie && mob.getHeldItemMainhand().isEmpty() && rand.nextDouble() < cfg.weaponChance) {
            ItemStack weapon = modItem(randomTier(rand) + (rand.nextBoolean() ? "_sword" : "_axe"));
            if (!weapon.isEmpty()) {
                mob.setItemStackToSlot(EntityEquipmentSlot.MAINHAND, maybeEnchant(weapon, rand));
            }
        }
        if (skeleton && !mob.getHeldItemMainhand().isEmpty() && !mob.getHeldItemMainhand().isItemEnchanted()) {
            // skeletons keep their vanilla bow (their AI only works with it) but it may be enchanted
            maybeEnchant(mob.getHeldItemMainhand(), rand);
        }
    }

    private static String randomTier(Random rand) {
        int total = 0;
        for (int w : WEIGHTS) {
            total += w;
        }
        int r = rand.nextInt(total);
        for (int i = 0; i < TIERS.length; i++) {
            r -= WEIGHTS[i];
            if (r < 0) {
                return TIERS[i];
            }
        }
        return TIERS[0];
    }

    private static ItemStack modItem(String name) {
        Item item = Item.getByNameOrId(ForgedProgression.MODID + ":" + name);
        return item == null ? ItemStack.EMPTY : new ItemStack(item);
    }

    private static ItemStack maybeEnchant(ItemStack stack, Random rand) {
        if (rand.nextDouble() < ModConfig.hardMode.enchantChance) {
            EnchantmentHelper.addRandomEnchantment(rand, stack, 5 + rand.nextInt(20), false);
        }
        return stack;
    }

    private HardMode() {}
}
