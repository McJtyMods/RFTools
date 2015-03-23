package com.mcjty.rftools.dimension.description;

import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLiving;

public class MobDescriptor {
    private final String className;
    private Class<? extends EntityLiving> entityClass;
    private final int spawnChance;
    private final int minGroup;
    private final int maxGroup;
    private final int maxLoaded;

    public MobDescriptor(String className, Class<? extends EntityLiving> entityClass, int spawnChance, int minGroup, int maxGroup, int maxLoaded) {
        this.className = className;
        this.entityClass = entityClass;
        this.spawnChance = spawnChance;
        this.minGroup = minGroup;
        this.maxGroup = maxGroup;
        this.maxLoaded = maxLoaded;
    }

    public Class<? extends EntityLiving> getEntityClass() {
        if (entityClass == null) {
            Class clazz = (Class) EntityList.stringToClassMapping.get(className);
            if (clazz == null || !EntityLiving.class.isAssignableFrom(clazz)) {
                return null;
            }
            return clazz;
        }
        return entityClass;
    }

    public int getSpawnChance() {
        return spawnChance;
    }

    public int getMinGroup() {
        return minGroup;
    }

    public int getMaxGroup() {
        return maxGroup;
    }

    public int getMaxLoaded() {
        return maxLoaded;
    }
}
