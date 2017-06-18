package mcjty.rftools.varia;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.monster.EntityStray;
import net.minecraft.entity.monster.EntityWitherSkeleton;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.datafix.fixes.EntityId;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

import java.lang.reflect.InvocationTargetException;

public class EntityTools {

    private static final EntityId FIXER = new EntityId();

    /**
     * This method attempts to fix an old-style (1.10.2) entity Id and convert it to the
     * string representation of the new ResourceLocation. The 1.10 version of this function will just return
     * the given id
     * This does not work for modded entities.
     * @param id an old-style entity id as used in 1.10
     * @return
     */
    public static String fixEntityId(String id) {
        NBTTagCompound nbt = new NBTTagCompound();
        nbt.setString("id", id);
        nbt = FIXER.fixTagCompound(nbt);
        return nbt.getString("id");
    }

    /**
     * On 1.11: return the string representation of the ResourceLocation that belongs with the entity class
     * On 1.10: return the entity name
     */
    public static String findEntityIdByClass(Class<? extends Entity> clazz) {
        ResourceLocation key = EntityList.getKey(clazz);
        return key == null ? null : key.toString();
    }

    /**
     * Get the localized name of an entity based on class
     */
    public static String findEntityLocNameByClass(Class<? extends Entity> clazz) {
        String nameByClass = findEntityIdByClass(clazz);
        if (nameByClass == null) {
            return null;
        }
        return I18n.translateToLocal(nameByClass);
    }


    /**
     * Create an entity given a mobId. On 1.11 this should be the string representation of
     * the ResourceLocation for that entity. On 1.10 the entity name. In either case the
     * result of fixEntityId will work.
     */
    public static EntityLiving createEntity(World world, String mobId) {
        Class<? extends Entity> clazz;
        if ("WitherSkeleton".equals(mobId)) {
            clazz = EntityWitherSkeleton.class;
        } else if ("StraySkeleton".equals(mobId)) {
            clazz = EntityStray.class;
        } else {
            clazz = ForgeRegistries.ENTITIES.getValue(new ResourceLocation(mobId)).getEntityClass();
        }
        EntityLiving entityLiving = null;
        try {
            entityLiving = (EntityLiving) clazz.getConstructor(World.class).newInstance(world);
        } catch (InstantiationException e) {
            return null;
        } catch (IllegalAccessException e) {
            return null;
        } catch (InvocationTargetException e) {
            return null;
        } catch (NoSuchMethodException e) {
            return null;
        }
        return entityLiving;
    }
}
