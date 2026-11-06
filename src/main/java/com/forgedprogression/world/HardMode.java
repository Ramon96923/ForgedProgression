package com.forgedprogression.world;

import com.forgedprogression.ForgedProgression;
import com.forgedprogression.ModConfig;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.boss.EntityDragon;
import net.minecraft.entity.boss.EntityWither;
import net.minecraft.entity.monster.AbstractSkeleton;
import net.minecraft.entity.monster.EntityCreeper;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.entity.monster.IMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.MobEffects;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.PotionEffect;
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
 * Two-stage hard mode for the whole world: stage I the first time any player enters the Nether,
 * stage II the first time any player enters the End. Hostile mobs get more health, hit harder,
 * spawn more often, and zombies/skeletons can spawn wearing (sometimes enchanted) mod armor and
 * wielding mod weapons. Stage II also adds fast mobs and charged creepers.
 */
@Mod.EventBusSubscriber(modid = ForgedProgression.MODID)
public final class HardMode {

    private static final UUID HEALTH_ID = UUID.fromString("6f2c8a3e-3b8f-4c41-9d5e-2a7b1c9e4f10");
    private static final UUID FOLLOW_ID = UUID.fromString("0b5d2c71-8e44-4f0a-a3c6-5d9e7f21b843");
    private static final UUID KNOCKBACK_ID = UUID.fromString("c4e19a02-6d3b-47f8-9b15-e2a8d0f37c56");
    /** Saved on each mob so a stage's buff is applied once, not again every time the chunk reloads. */
    private static final String[] STAGE_TAGS = {null, ForgedProgression.MODID + ".hardmode", ForgedProgression.MODID + ".hardmode2"};
    private static final int VANILLA_MONSTER_CAP = EnumCreatureType.MONSTER.getMaxNumberOfCreature();

    // ---------------------------------------------------------------- saved state

    /** Per-world stage (0 = off, 1 = Nether, 2 = End), saved in data/forgedprogression_hardmode.dat. */
    public static class Data extends WorldSavedData {
        public static final String NAME = ForgedProgression.MODID + "_hardmode";
        public int stage;

        public Data() {
            super(NAME);
        }

        public Data(String name) {
            super(name);
        }

        @Override
        public void readFromNBT(NBTTagCompound nbt) {
            // worlds from 1.4.0 only saved "active"
            stage = nbt.hasKey("stage") ? nbt.getInteger("stage") : (nbt.getBoolean("active") ? 1 : 0);
        }

        @Override
        public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
            nbt.setInteger("stage", stage);
            nbt.setBoolean("active", stage > 0);
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

    /** Current stage number, 0 when hard mode is off. */
    public static int stage(World world) {
        return ModConfig.hardMode.enabled ? Math.max(0, Math.min(2, data(world).stage)) : 0;
    }

    /** Settings for the current stage, or null when hard mode is off. */
    public static ModConfig.HardModeStage settings(World world) {
        return settingsFor(stage(world));
    }

    private static ModConfig.HardModeStage settingsFor(int stage) {
        return stage == 2 ? ModConfig.hardMode.end : stage == 1 ? ModConfig.hardMode.nether : null;
    }

    // ---------------------------------------------------------------- activation

    @SubscribeEvent
    public static void onChangeDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        World world = event.player.world;
        int reached = event.toDim == -1 ? 1 : event.toDim == 1 ? 2 : 0;
        if (world.isRemote || reached == 0 || !ModConfig.hardMode.enabled) {
            return;
        }
        Data d = data(world);
        if (d.stage >= reached) {
            return;
        }
        d.stage = reached;
        d.markDirty();
        applyMobCap(settingsFor(reached));
        announce(world.getMinecraftServer(), reached);
    }

    private static void announce(MinecraftServer server, int stage) {
        if (server == null) {
            return;
        }
        TextComponentString msg = stage == 2
                ? new TextComponentString("The End has been reached. Hard mode II is now active: "
                        + "monsters are far stronger, heavily armed, faster, and creepers may be charged.")
                : new TextComponentString("The Nether has been opened. Hard mode is now active: "
                        + "monsters are stronger, better armed and more numerous.");
        msg.getStyle().setColor(stage == 2 ? TextFormatting.DARK_PURPLE : TextFormatting.DARK_RED).setBold(true);
        server.getPlayerList().sendMessage(msg);
        for (EntityPlayerMP p : server.getPlayerList().getPlayers()) {
            p.world.playSound(null, p.posX, p.posY, p.posZ,
                    stage == 2 ? SoundEvents.ENTITY_ENDERDRAGON_GROWL : SoundEvents.ENTITY_WITHER_SPAWN,
                    SoundCategory.HOSTILE, 0.6F, 0.7F);
        }
    }

    /** Re-applies the mob cap when a saved world with hard mode already active is loaded. */
    @SubscribeEvent
    public static void onWorldLoad(WorldEvent.Load event) {
        World world = event.getWorld();
        if (!world.isRemote && world.provider.getDimension() == 0) {
            applyMobCap(settings(world));
        }
    }

    /**
     * The hostile mob cap is a final field on the vanilla enum with no API to change it, so set it by reflection.
     * If that ever fails (e.g. another mod changed it), hard mode still works through the extra-spawn chance.
     */
    private static void applyMobCap(ModConfig.HardModeStage stage) {
        int cap = stage != null ? (int) Math.round(VANILLA_MONSTER_CAP * stage.mobCapMultiplier) : VANILLA_MONSTER_CAP;
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
                || e instanceof EntityDragon || e instanceof EntityWither) {
            return;
        }
        int stage = stage(world);
        if (stage == 0 || e.getTags().contains(STAGE_TAGS[stage])) {
            return;
        }
        // mobs that already got stage I keep their gear, but their health is raised to stage II
        boolean newMob = !e.getTags().contains(STAGE_TAGS[1]) && !e.getTags().contains(STAGE_TAGS[2]);
        e.addTag(STAGE_TAGS[stage]);
        EntityLiving mob = (EntityLiving) e;
        ModConfig.HardModeStage cfg = settingsFor(stage);

        float fraction = mob.getMaxHealth() > 0 ? mob.getHealth() / mob.getMaxHealth() : 1.0F;
        // operation 1: +X% of base value; operation 0: flat
        setModifier(mob, SharedMonsterAttributes.MAX_HEALTH, HEALTH_ID, "Hard mode health", cfg.mobHealthMultiplier - 1.0, 1);
        setModifier(mob, SharedMonsterAttributes.FOLLOW_RANGE, FOLLOW_ID, "Hard mode follow range", cfg.followRangeMultiplier - 1.0, 1);
        setModifier(mob, SharedMonsterAttributes.KNOCKBACK_RESISTANCE, KNOCKBACK_ID, "Hard mode knockback resistance", cfg.knockbackResistance, 0);
        mob.setHealth(mob.getMaxHealth() * (newMob ? 1.0F : fraction));

        if (newMob) {
            equip(mob, world.rand, stage, cfg);
            if (world.rand.nextDouble() < cfg.speedChance) {
                mob.addPotionEffect(new PotionEffect(MobEffects.SPEED, Integer.MAX_VALUE, 0, false, false));
            }
            if (world.rand.nextDouble() < cfg.resistanceChance) {
                mob.addPotionEffect(new PotionEffect(MobEffects.RESISTANCE, Integer.MAX_VALUE, 0, false, false));
            }
            if (world.rand.nextDouble() < cfg.fireResistanceChance) {
                mob.addPotionEffect(new PotionEffect(MobEffects.FIRE_RESISTANCE, Integer.MAX_VALUE, 0, false, false));
            }
            if (mob instanceof EntityCreeper && world.rand.nextDouble() < cfg.chargedCreeperChance) {
                // creepers have no setter for "powered", so round-trip it through their saved data
                NBTTagCompound nbt = new NBTTagCompound();
                mob.writeEntityToNBT(nbt);
                nbt.setBoolean("powered", true);
                mob.readEntityFromNBT(nbt);
            }
        }
    }

    private static void setModifier(EntityLiving mob, net.minecraft.entity.ai.attributes.IAttribute attribute, UUID id,
                                    String name, double amount, int operation) {
        IAttributeInstance instance = mob.getEntityAttribute(attribute);
        if (instance == null) {
            return;
        }
        instance.removeModifier(id);
        if (amount != 0) {
            instance.applyModifier(new AttributeModifier(id, name, amount, operation));
        }
    }

    @SubscribeEvent
    public static void onHurt(LivingHurtEvent event) {
        if (!(event.getEntityLiving() instanceof EntityPlayer) || event.getEntityLiving().world.isRemote) {
            return;
        }
        Entity attacker = event.getSource().getTrueSource();
        if (attacker instanceof IMob && !(attacker instanceof EntityDragon) && !(attacker instanceof EntityWither)) {
            ModConfig.HardModeStage cfg = settings(attacker.world);
            if (cfg != null) {
                EntityLivingBase player = event.getEntityLiving();
                event.setAmount(event.getAmount() * (float) cfg.mobDamageMultiplier);
                if (player.world.rand.nextDouble() < cfg.debuffOnHitChance) {
                    player.addPotionEffect(new PotionEffect(player.world.rand.nextBoolean() ? MobEffects.SLOWNESS : MobEffects.WEAKNESS, 60, 0));
                }
            }
        }
    }

    /** Each natural hostile spawn can bring extra mobs of the same kind with it. */
    @SubscribeEvent
    public static void onCheckSpawn(LivingSpawnEvent.CheckSpawn event) {
        World world = event.getWorld();
        if (world.isRemote || event.isSpawner() || event.getResult() == Event.Result.DENY
                || !(event.getEntityLiving() instanceof EntityLiving) || !(event.getEntityLiving() instanceof IMob)) {
            return;
        }
        ModConfig.HardModeStage cfg = settings(world);
        if (cfg == null) {
            return;
        }
        int count = (int) cfg.extraSpawns + (world.rand.nextDouble() < cfg.extraSpawns % 1.0 ? 1 : 0);
        for (int i = 0; i < count; i++) {
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
    }

    // ---------------------------------------------------------------- gear

    private static final EntityEquipmentSlot[] ARMOR_SLOTS = {
            EntityEquipmentSlot.HEAD, EntityEquipmentSlot.CHEST, EntityEquipmentSlot.LEGS, EntityEquipmentSlot.FEET};
    private static final String[] ARMOR_NAMES = {"helmet", "chestplate", "leggings", "boots"};
    private static final String[] TIERS = {"copper", "bronze", "steel", "ruby", "uranium", "plutonium", "neutronium"};
    // gear weights per stage: stage I is mostly early metals, stage II mostly ruby to plutonium
    private static final int[][] WEIGHTS = {
            {},
            {40, 30, 20, 8, 2, 0, 0},
            {0, 0, 10, 25, 30, 25, 10},
    };

    private static void equip(EntityLiving mob, Random rand, int stage, ModConfig.HardModeStage cfg) {
        boolean zombie = mob instanceof EntityZombie;
        boolean skeleton = mob instanceof AbstractSkeleton;
        if (!zombie && !skeleton) {
            return;
        }
        for (int i = 0; i < ARMOR_SLOTS.length; i++) {
            if (mob.getItemStackFromSlot(ARMOR_SLOTS[i]).isEmpty() && rand.nextDouble() < cfg.armorChance) {
                ItemStack piece = modItem(randomTier(rand, stage) + "_" + ARMOR_NAMES[i]);
                if (!piece.isEmpty()) {
                    mob.setItemStackToSlot(ARMOR_SLOTS[i], maybeEnchant(piece, rand, stage, cfg));
                }
            }
        }
        if (zombie && mob.getHeldItemMainhand().isEmpty() && rand.nextDouble() < cfg.weaponChance) {
            ItemStack weapon = modItem(randomTier(rand, stage) + (rand.nextBoolean() ? "_sword" : "_axe"));
            if (!weapon.isEmpty()) {
                mob.setItemStackToSlot(EntityEquipmentSlot.MAINHAND, maybeEnchant(weapon, rand, stage, cfg));
            }
        }
        if (skeleton && !mob.getHeldItemMainhand().isEmpty() && !mob.getHeldItemMainhand().isItemEnchanted()) {
            // skeletons keep their vanilla bow (their AI only works with it) but it may be enchanted
            maybeEnchant(mob.getHeldItemMainhand(), rand, stage, cfg);
        }
    }

    private static String randomTier(Random rand, int stage) {
        int[] weights = WEIGHTS[stage];
        int total = 0;
        for (int w : weights) {
            total += w;
        }
        int r = rand.nextInt(total);
        for (int i = 0; i < TIERS.length; i++) {
            r -= weights[i];
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

    private static ItemStack maybeEnchant(ItemStack stack, Random rand, int stage, ModConfig.HardModeStage cfg) {
        if (rand.nextDouble() < cfg.enchantChance) {
            // stage II enchants like a level 15-30 enchanting table
            int level = stage == 2 ? 15 + rand.nextInt(16) : 5 + rand.nextInt(20);
            EnchantmentHelper.addRandomEnchantment(rand, stack, level, false);
        }
        return stack;
    }

    private HardMode() {}
}
