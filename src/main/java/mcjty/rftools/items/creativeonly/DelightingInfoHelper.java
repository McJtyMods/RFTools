package mcjty.rftools.items.creativeonly;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class DelightingInfoHelper {
    static void parseNBT(TileEntity tileEntity, Map<String, NBTDescription> nbtData) {
        NBTTagCompound tagCompound = new NBTTagCompound();
        tileEntity.writeToNBT(tagCompound);
        Set<String> tags = tagCompound.getKeySet();
        for (String c : tags) {
            NBTBase nbtBase = tagCompound.getTag(c);
            NBTDescription description = new NBTDescription(NBTBase.NBT_TYPES[nbtBase.getId()], nbtBase.toString());
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

    static int fillDelightingData(int x, int y, int z, World world, List<String> blockClasses, List<String> teClasses, Map<String, NBTDescription> nbtData) {
        BlockPos pos = new BlockPos(x, y, z);
        IBlockState state = world.getBlockState(pos);
        Block block = state.getBlock();
        addSuperTypes(blockClasses, block.getClass());
        TileEntity tileEntity = world.getTileEntity(pos);
        if (tileEntity != null) {
            addSuperTypes(teClasses, tileEntity.getClass());
            parseNBT(tileEntity, nbtData);
        }
        return block.getMetaFromState(state);
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
