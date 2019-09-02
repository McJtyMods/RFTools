package mcjty.rftools.items.creativeonly;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class DelightingInfoHelper {
    static void parseNBT(TileEntity tileEntity, Map<String, NBTDescription> nbtData) {
        CompoundNBT tagCompound = new CompoundNBT();
        tileEntity.write(tagCompound);
        Set<String> tags = tagCompound.keySet();
        for (String c : tags) {
            INBT nbtBase = tagCompound.get(c);
            NBTDescription description = new NBTDescription(INBT.NBT_TYPES[nbtBase.getId()], nbtBase.toString());
            nbtData.put(c, description);
        }
    }

    private static void addSuperTypes(List<String> classes, Class<?> clazz) {
        if (clazz == null || Object.class.equals(clazz)) {
            return;
        }
        classes.add(clazz.getName());
        addSuperTypes(classes, clazz.getSuperclass());
        for (Class<?> c : clazz.getInterfaces()) {
            addSuperTypes(classes, c);
        }
    }

    static void fillDelightingData(int x, int y, int z, World world, List<String> blockClasses, List<String> teClasses, Map<String, NBTDescription> nbtData) {
        BlockPos pos = new BlockPos(x, y, z);
        BlockState state = world.getBlockState(pos);
        Block block = state.getBlock();
        addSuperTypes(blockClasses, block.getClass());
        TileEntity tileEntity = world.getTileEntity(pos);
        if (tileEntity != null) {
            addSuperTypes(teClasses, tileEntity.getClass());
            parseNBT(tileEntity, nbtData);
        }
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
