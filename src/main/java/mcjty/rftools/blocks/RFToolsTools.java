package mcjty.rftools.blocks;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.play.server.SPacketSoundEffect;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.GameData;
import org.apache.commons.lang3.StringUtils;

public class RFToolsTools {

    public static boolean chunkLoaded(World world, BlockPos pos) {
        return world.getChunkProvider().getLoadedChunk(pos.getX() >> 4, pos.getZ() >> 4) != null && world.getChunkFromBlockCoords(pos).isLoaded();
    }

    // Server side: play a sound to all nearby players
    public static void playSound(World worldObj, SoundEvent sound, double x, double y, double z, double volume, double pitch) {
        SPacketSoundEffect soundEffect = new SPacketSoundEffect(sound, SoundCategory.BLOCKS, x, y, z, (float) volume, (float) pitch);

        for (int j = 0; j < worldObj.playerEntities.size(); ++j) {
            EntityPlayerMP entityplayermp = (EntityPlayerMP)worldObj.playerEntities.get(j);
            double d7 = x - entityplayermp.posX;
            double d8 = y - entityplayermp.posY;
            double d9 = z - entityplayermp.posZ;
            double d10 = d7 * d7 + d8 * d8 + d9 * d9;

            if (d10 <= 256.0D) {
                entityplayermp.playerNetServerHandler.sendPacket(soundEffect);
            }
        }
    }

    public static StringBuffer appendIndent(StringBuffer buffer, int indent) {
        return buffer.append(StringUtils.repeat(' ', indent));
    }

    public static void convertNBTtoJson(StringBuffer buffer, NBTTagList tagList, int indent) {
        for (int i = 0 ; i < tagList.tagCount() ; i++) {
            NBTTagCompound compound = tagList.getCompoundTagAt(i);
            appendIndent(buffer, indent).append("{\n");
            convertNBTtoJson(buffer, compound, indent + 4);
            appendIndent(buffer, indent).append("},\n");
        }
    }

    public static void convertNBTtoJson(StringBuffer buffer, NBTTagCompound tagCompound, int indent) {
        boolean first = true;
        for (Object o : tagCompound.getKeySet()) {
            if (!first) {
                buffer.append(",\n");
            }
            first = false;

            String key = (String) o;
            NBTBase tag = tagCompound.getTag(key);
            appendIndent(buffer, indent).append(key).append(':');
            if (tag instanceof NBTTagCompound) {
                NBTTagCompound compound = (NBTTagCompound) tag;
                buffer.append("{\n");
                convertNBTtoJson(buffer, compound, indent + 4);
                appendIndent(buffer, indent).append('}');
            } else if (tag instanceof NBTTagList) {
                NBTTagList list = (NBTTagList) tag;
                buffer.append("[\n");
                convertNBTtoJson(buffer, list, indent + 4);
                appendIndent(buffer, indent).append(']');
            } else {
                buffer.append(tag);
            }
        }
        if (!first) {
            buffer.append("\n");
        }
    }

    public static String getModidForBlock(Block block) {
        ResourceLocation nameForObject = GameData.getBlockRegistry().getNameForObject(block);
        if (nameForObject == null) {
            return "?";
        }
        return nameForObject.getResourceDomain();
    }
}
