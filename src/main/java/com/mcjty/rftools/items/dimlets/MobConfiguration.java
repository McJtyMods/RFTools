package com.mcjty.rftools.items.dimlets;

import com.mcjty.rftools.RFTools;
import com.mcjty.rftools.dimension.description.MobDescriptor;
import net.minecraft.block.Block;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.boss.EntityDragon;
import net.minecraft.entity.boss.EntityWither;
import net.minecraft.entity.monster.*;
import net.minecraft.entity.passive.*;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraftforge.common.config.ConfigCategory;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MobConfiguration {
    public static final Map<String,MobDescriptor> mobClasses = new HashMap<String, MobDescriptor>();
    public static final Map<String,List<MobSpawnAmount>> mobSpawnAmounts = new HashMap<String, List<MobSpawnAmount>>();

    public static class MobSpawnAmount {
        private final Object object;
        private final int meta;
        private final float amount;

        public MobSpawnAmount(Object object, int meta, float amount) {
            this.object = object;
            this.meta = meta;
            this.amount = amount;
        }

        public Object getObject() {
            return object;
        }

        public float getAmount() {
            return amount;
        }
    }

    public static void readMobConfig(Configuration cfg) {
        initMobItem(cfg, null, "Default", 1, 1, 1, 1);
        initMobItem(cfg, EntityZombie.class, "Zombie", 100, 8, 8, 60);
        initMobItem(cfg, EntitySkeleton.class, "Skeleton", 100, 8, 8, 60);
        initMobItem(cfg, EntityEnderman.class, "Enderman", 20, 2, 4, 20);
        initMobItem(cfg, EntityBlaze.class, "Blaze", 20, 2, 4, 20);
        initMobItem(cfg, EntityCreeper.class, "Creeper", 100, 8, 8, 60);
        initMobItem(cfg, EntityCaveSpider.class, "Cave Spider", 100, 8, 8, 60);
        initMobItem(cfg, EntityGhast.class, "Ghast", 20, 2, 4, 20);
        initMobItem(cfg, EntityIronGolem.class, "Iron Golem", 20, 1, 2, 6);
        initMobItem(cfg, EntityMagmaCube.class, "Magma Cube", 50, 2, 4, 30);
        initMobItem(cfg, EntityPigZombie.class, "Zombie Pigman", 20, 2, 4, 10);
        initMobItem(cfg, EntitySlime.class, "Slime", 50, 2, 4, 30);
        initMobItem(cfg, EntitySnowman.class, "Snowman", 50, 2, 4, 30);
        initMobItem(cfg, EntitySpider.class, "Spider", 100, 8, 8, 60);
        initMobItem(cfg, EntityWitch.class, "Witch", 10, 1, 1, 20);
        initMobItem(cfg, EntityBat.class, "Bat", 10, 8, 8, 20);
        initMobItem(cfg, EntityChicken.class, "Chicken", 10, 3, 4, 40);
        initMobItem(cfg, EntityCow.class, "Cow", 10, 3, 4, 40);
        initMobItem(cfg, EntityHorse.class, "Horse", 10, 3, 4, 40);
        initMobItem(cfg, EntityMooshroom.class, "Mooshroom", 10, 3, 4, 40);
        initMobItem(cfg, EntityOcelot.class, "Ocelot", 5, 2, 3, 20);
        initMobItem(cfg, EntityPig.class, "Pig", 10, 3, 4, 40);
        initMobItem(cfg, EntitySheep.class, "Sheep", 10, 3, 4, 40);
        initMobItem(cfg, EntitySquid.class, "Squid", 10, 3, 4, 40);
        initMobItem(cfg, EntityWolf.class, "Wolf", 10, 3, 4, 20);
        initMobItem(cfg, EntityVillager.class, "Villager", 10, 3, 4, 20);
        initMobItem(cfg, EntityWither.class, "Wither", 5, 1, 2, 5);
        initMobItem(cfg, EntityDragon.class, "Dragon", 4, 1, 2, 4);

        addMobSpawnAmount(cfg, "Bat", "1", Blocks.dirt, 0, 20);
        addMobSpawnAmount(cfg, "Bat", "2", "living", 0, 3);
        addMobSpawnAmount(cfg, "Bat", "3", "null", 0, 3);
        addMobSpawnAmount(cfg, "Blaze", "1", Items.blaze_rod, 0, 0.1f);
        addMobSpawnAmount(cfg, "Blaze", "2", Blocks.netherrack, 0, 60);
        addMobSpawnAmount(cfg, "Blaze", "3", "living", 0, 9);
        addMobSpawnAmount(cfg, "Cave Spider", "1", Items.string, 0, 0.1f);
        addMobSpawnAmount(cfg, "Cave Spider", "2", Blocks.dirt, 0, 40);
        addMobSpawnAmount(cfg, "Cave Spider", "3", "living", 0, 5);
        addMobSpawnAmount(cfg, "Chicken", "1", Items.feather, 0, 0.1f);
        addMobSpawnAmount(cfg, "Chicken", "2", Blocks.dirt, 0, 30);
        addMobSpawnAmount(cfg, "Chicken", "3", "living", 0, 5);
        addMobSpawnAmount(cfg, "Cow", "1", Items.leather, 0, 0.1f);
        addMobSpawnAmount(cfg, "Cow", "2", Blocks.dirt, 0, 50);
        addMobSpawnAmount(cfg, "Cow", "3", "living", 0, 8);
        addMobSpawnAmount(cfg, "Creeper", "1", Items.gunpowder, 0, 0.1f);
        addMobSpawnAmount(cfg, "Creeper", "2", Blocks.dirt, 0, 60);
        addMobSpawnAmount(cfg, "Creeper", "3", "living", 0, 5);
        addMobSpawnAmount(cfg, "Dragon", "1", Items.experience_bottle, 0, 0.1f);
        addMobSpawnAmount(cfg, "Dragon", "2", Blocks.end_stone, 0, 200);
        addMobSpawnAmount(cfg, "Dragon", "3", "living", 0, 200);
        addMobSpawnAmount(cfg, "Enderman", "1", Items.ender_pearl, 0, 0.1f);
        addMobSpawnAmount(cfg, "Enderman", "2", Blocks.end_stone, 0, 20);
        addMobSpawnAmount(cfg, "Enderman", "3", "living", 0, 11);
        addMobSpawnAmount(cfg, "Ghast", "1", Items.ghast_tear, 0, 0.1f);
        addMobSpawnAmount(cfg, "Ghast", "2", Blocks.netherrack, 0, 50);
        addMobSpawnAmount(cfg, "Ghast", "3", "living", 0, 12);
        addMobSpawnAmount(cfg, "Horse", "1", Items.leather, 0, 0.1f);
        addMobSpawnAmount(cfg, "Horse", "2", Blocks.dirt, 0, 50);
        addMobSpawnAmount(cfg, "Horse", "3", "living", 0, 8);
        addMobSpawnAmount(cfg, "Iron Golem", "1", Items.iron_ingot, 0, 0.1f);
        addMobSpawnAmount(cfg, "Iron Golem", "2", Blocks.dirt, 0, 50);
        addMobSpawnAmount(cfg, "Iron Golem", "3", Items.dye, 0, 0.1f);
        addMobSpawnAmount(cfg, "Magma Cube", "1", Items.magma_cream, 0, 0.1f);
        addMobSpawnAmount(cfg, "Magma Cube", "2", Blocks.netherrack, 0, 50);
        addMobSpawnAmount(cfg, "Magma Cube", "3", "living", 0, 5);
        addMobSpawnAmount(cfg, "Mooshroom", "1", Items.leather, 0, 0.1f);
        addMobSpawnAmount(cfg, "Mooshroom", "2", Blocks.dirt, 0, 50);
        addMobSpawnAmount(cfg, "Mooshroom", "3", "living", 0, 8);
        addMobSpawnAmount(cfg, "Ocelot", "1", Items.fish, 0, 0.1f);
        addMobSpawnAmount(cfg, "Ocelot", "2", Blocks.dirt, 0, 50);
        addMobSpawnAmount(cfg, "Ocelot", "3", "living", 0, 8);
        addMobSpawnAmount(cfg, "Pig", "1", Items.carrot, 0, 0.1f);
        addMobSpawnAmount(cfg, "Pig", "2", Blocks.dirt, 0, 50);
        addMobSpawnAmount(cfg, "Pig", "3", "living", 0, 8);
        addMobSpawnAmount(cfg, "Sheep", "1", Blocks.wool, 0, 0.1f);
        addMobSpawnAmount(cfg, "Sheep", "2", Blocks.dirt, 0, 50);
        addMobSpawnAmount(cfg, "Sheep", "3", "living", 0, 8);
        addMobSpawnAmount(cfg, "Skeleton", "1", Items.bone, 0, 0.1f);
        addMobSpawnAmount(cfg, "Skeleton", "2", Blocks.dirt, 0, 30);
        addMobSpawnAmount(cfg, "Skeleton", "3", "living", 0, 5);
        addMobSpawnAmount(cfg, "Slime", "1", Items.slime_ball, 0, 0.1f);
        addMobSpawnAmount(cfg, "Slime", "2", Blocks.dirt, 0, 40);
        addMobSpawnAmount(cfg, "Slime", "3", "living", 0, 5);
        addMobSpawnAmount(cfg, "Snowman", "1", Items.snowball, 0, 0.1f);
        addMobSpawnAmount(cfg, "Snowman", "2", Blocks.dirt, 0, 40);
        addMobSpawnAmount(cfg, "Snowman", "3", "living", 0, 4);
        addMobSpawnAmount(cfg, "Spider", "1", Items.string, 0, 0.1f);
        addMobSpawnAmount(cfg, "Spider", "2", Blocks.dirt, 0, 40);
        addMobSpawnAmount(cfg, "Spider", "3", "living", 0, 5);
        addMobSpawnAmount(cfg, "Squid", "1", Items.dye, 0, 0.1f);
        addMobSpawnAmount(cfg, "Squid", "2", Blocks.dirt, 0, 40);
        addMobSpawnAmount(cfg, "Squid", "3", "living", 0, 4);
        addMobSpawnAmount(cfg, "Villager", "1", Items.emerald, 0, 0.1f);
        addMobSpawnAmount(cfg, "Villager", "2", Blocks.dirt, 0, 40);
        addMobSpawnAmount(cfg, "Villager", "3", "living", 0, 7);
        addMobSpawnAmount(cfg, "Witch", "1", Items.glowstone_dust, 0, 0.1f);
        addMobSpawnAmount(cfg, "Witch", "2", Blocks.dirt, 0, 40);
        addMobSpawnAmount(cfg, "Witch", "3", "living", 0, 7);
        addMobSpawnAmount(cfg, "Wither", "1", Items.nether_star, 0, 0.1f);
        addMobSpawnAmount(cfg, "Wither", "2", Blocks.soul_sand, 0, 40);
        addMobSpawnAmount(cfg, "Wither", "3", "living", 0, 6);
        addMobSpawnAmount(cfg, "Wolf", "1", Items.bone, 0, 0.1f);
        addMobSpawnAmount(cfg, "Wolf", "2", Blocks.dirt, 0, 50);
        addMobSpawnAmount(cfg, "Wolf", "3", "living", 0, 8);
        addMobSpawnAmount(cfg, "Zombie Pigman", "1", Items.gold_nugget, 0, 0.1f);
        addMobSpawnAmount(cfg, "Zombie Pigman", "2", Blocks.netherrack, 0, 30);
        addMobSpawnAmount(cfg, "Zombie Pigman", "3", "living", 0, 5);
        addMobSpawnAmount(cfg, "Zombie", "1", Items.rotten_flesh, 0, 0.1f);
        addMobSpawnAmount(cfg, "Zombie", "2", Blocks.dirt, 0, 30);
        addMobSpawnAmount(cfg, "Zombie", "3", "living", 0, 5);

        ConfigCategory category = cfg.getCategory(KnownDimletConfiguration.CATEGORY_MOBSPAWNS);
        for (Map.Entry<String, Property> entry : category.entrySet()) {
            String name = entry.getKey();
            String[] splitted = StringUtils.split(name, ".");
            if (!mobClasses.containsKey(splitted[0])) {
                registerCustomMobItem(category, splitted[0]);
                addMobSpawnAmount(cfg, splitted[0], "1", Blocks.dirt, 0, 100);
                addMobSpawnAmount(cfg, splitted[0], "2", "living", 0, 100);
                addMobSpawnAmount(cfg, splitted[0], "3", "null", 0, 100);
            }
        }

    }

    private static void addMobSpawnAmount(Configuration cfg, String name, String suffix, Object object, int meta, float amount) {
        String type;
        String itemname;
        if (object instanceof Item) {
            type = "I";
            itemname = Item.itemRegistry.getNameForObject(object);
        } else if (object instanceof Block) {
            type = "B";
            itemname = Block.blockRegistry.getNameForObject(object);
        } else if (object instanceof String) {
            type = "S";
            itemname = (String) object;
        } else {
            return;
        }
        String v = cfg.get(KnownDimletConfiguration.CATEGORY_MOBSPAWNS, name + ".spawnamount." + suffix,
                type + "/" + itemname + "/" + meta + "/" + amount).getString();

        try {
            String[] splitted = StringUtils.split(v, "/");
            type = splitted[0];
            itemname = splitted[1];
            meta = Integer.parseInt(splitted[2]);
            amount = Float.parseFloat(splitted[3]);
        } catch (NumberFormatException e) {
            RFTools.logError("Something went wrong parsing the spawnamount setting for '" + name + "'!");
            return;
        }
        if ("I".equals(type)) {
            object = Item.itemRegistry.getObject(itemname);
        } else if ("B".equals(type)) {
            object = Block.blockRegistry.getObject(itemname);
        } else if ("S".equals(type)) {
            object = itemname;
        }

        List<MobSpawnAmount> list = mobSpawnAmounts.get(name);
        if (list == null) {
            list = new ArrayList<MobSpawnAmount>();
            mobSpawnAmounts.put(name, list);
        }
        list.add(new MobSpawnAmount(object, meta, amount));
    }

    private static void registerCustomMobItem(ConfigCategory category, String name) {
        Property entityNameProperty = checkProperty(category, name, "entityname");
        if (entityNameProperty == null) {
            return;
        }
        String entityName = entityNameProperty.getString();
        Object o = EntityList.stringToClassMapping.get(entityName);
        if (!EntityLiving.class.isAssignableFrom((Class)o)) {
            RFTools.logError("Invalid custom mob item: '" + entityName + "' is not a living entity!");
            return;
        }
        Class<? extends EntityLiving> clazz = (Class<? extends EntityLiving>) o;

        Property chanceProperty = checkProperty(category, name, "chance");
        if (chanceProperty == null) {
            return;
        }
        int chance = chanceProperty.getInt();

        Property maxentityProperty = checkProperty(category, name, "maxentity");
        if (maxentityProperty == null) {
            return;
        }
        int maxentity = chanceProperty.getInt();

        Property maxgroupProperty = checkProperty(category, name, "maxgroup");
        if (maxgroupProperty == null) {
            return;
        }
        int maxgroup = chanceProperty.getInt();

        Property mingroupProperty = checkProperty(category, name, "mingroup");
        if (mingroupProperty == null) {
            return;
        }
        int mingroup = chanceProperty.getInt();

        initMobItem(null, clazz, name, chance, mingroup, maxgroup, maxentity);
    }

    private static Property checkProperty(ConfigCategory category, String name, String propname) {
        Property entityNameProperty = category.get(name + "." + propname);
        if (entityNameProperty == null) {
            RFTools.logError("Invalid custom mob item: '" +  propname + "' property is missing!");
            return null;
        }
        return entityNameProperty;
    }

    private static void initMobItem(Configuration cfg, Class<? extends EntityLiving> entity, String name,
                                    int chance, int mingroup, int maxgroup, int maxentity) {
        if (cfg != null) {
            chance = cfg.get(KnownDimletConfiguration.CATEGORY_MOBSPAWNS, name + ".chance", chance).getInt();
            mingroup = cfg.get(KnownDimletConfiguration.CATEGORY_MOBSPAWNS, name + ".mingroup", mingroup).getInt();
            maxgroup = cfg.get(KnownDimletConfiguration.CATEGORY_MOBSPAWNS, name + ".maxgroup", maxgroup).getInt();
            maxentity = cfg.get(KnownDimletConfiguration.CATEGORY_MOBSPAWNS, name + ".maxentity", maxentity).getInt();
        }
        mobClasses.put(name, new MobDescriptor(entity, chance, mingroup, maxgroup, maxentity));
    }

}
