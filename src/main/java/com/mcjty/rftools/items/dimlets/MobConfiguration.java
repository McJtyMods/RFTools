package com.mcjty.rftools.items.dimlets;

import com.mcjty.rftools.RFTools;
import com.mcjty.rftools.dimension.description.MobDescriptor;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.boss.EntityDragon;
import net.minecraft.entity.boss.EntityWither;
import net.minecraft.entity.monster.*;
import net.minecraft.entity.passive.*;
import net.minecraftforge.common.config.ConfigCategory;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.Map;

public class MobConfiguration {
    public static final Map<String,MobDescriptor> mobClasses = new HashMap<String, MobDescriptor>();

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

        ConfigCategory category = cfg.getCategory(KnownDimletConfiguration.CATEGORY_MOBSPAWNS);
        for (Map.Entry<String, Property> entry : category.entrySet()) {
            String name = entry.getKey();
            String[] splitted = StringUtils.split(name, ".");
            if (!mobClasses.containsKey(splitted[0])) {
                registerCustomMobItem(category, splitted[0]);
            }
        }
    }

    private static void registerCustomMobItem(ConfigCategory category, String name) {
        Property entityNameProperty = checkProperty(category, name, "entityname");
        if (entityNameProperty == null) {
            return;
        }
        String entityName = entityNameProperty.getString();
        Object o = EntityList.stringToClassMapping.get(entityName);
        if (!EntityLiving.class.isAssignableFrom((Class)o)) {
            RFTools.logError("Invalid custom mob item: '" + entityName +"' is not a living entity!");
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
