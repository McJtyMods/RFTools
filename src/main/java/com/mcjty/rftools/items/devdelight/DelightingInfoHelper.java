package com.mcjty.rftools.items.devdelight;

import net.minecraft.block.Block;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class DelightingInfoHelper {
    static void parseNBT(TileEntity tileEntity, Map<String, NBTDescription> nbtData) {
        NBTTagCompound tagCompound = new NBTTagCompound();
        tileEntity.writeToNBT(tagCompound);
        Set<String> tags = tagCompound.func_150296_c();
        for (String c : tags) {
            NBTBase nbtBase = tagCompound.getTag(c);
            NBTDescription description = new NBTDescription(NBTBase.NBTTypes[nbtBase.getId()], nbtBase.toString());
            nbtData.put(c, description);
        }
    }

    private static void addSuperTypes(List<String> classes, Class clazz) {
        if (clazz == null || Object.class.equals(clazz)) {
            return;
        }
        classes.add(clazz.getName());
        addSuperTypes(classes, clazz.getSuperclass());
        for (Class c : clazz.getInterfaces()) {
            addSuperTypes(classes, c);
        }
    }

    static int fillDelightingData(int x, int y, int z, World world, List<String> blockClasses, List<String> teClasses, Map<String, NBTDescription> nbtData) {
        Block block = world.getBlock(x, y, z);
        addSuperTypes(blockClasses, block.getClass());
        TileEntity tileEntity = world.getTileEntity(x, y, z);
        if (tileEntity != null) {
            addSuperTypes(teClasses, tileEntity.getClass());
            parseNBT(tileEntity, nbtData);
        }
        return world.getBlockMetadata(x, y, z);
    }

    public static class NBTDescription {
        private String type;
        private String value;

        public NBTDescription(String type, String value) {
            this.type = type;
            this.value = value;
        }

        public String getType() {
            return type;
        }

        public String getValue() {
            return value;
        }
    }
}
