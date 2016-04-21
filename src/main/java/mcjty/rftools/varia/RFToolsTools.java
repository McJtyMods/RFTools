package mcjty.rftools.varia;

import mcjty.rftools.api.screens.IModuleProvider;
import mcjty.rftools.blocks.screens.ScreenContainer;
import mcjty.rftools.blocks.screens.ScreenTileEntity;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.play.server.SPacketSoundEffect;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.GameData;
import org.apache.commons.lang3.StringUtils;

public class RFToolsTools {

    public static boolean chunkLoaded(World world, BlockPos pos) {
        return world.getChunkProvider().getLoadedChunk(pos.getX() >> 4, pos.getZ() >> 4) != null && world.getChunkFromBlockCoords(pos).isLoaded();
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


    /**
     * Inject a module that the player is holding into the appropriate slots
     * @return true if successful
     */
    public static boolean installModule(EntityPlayer player, ItemStack heldItem, EnumHand hand, BlockPos pos, int start, int stop) {
        World world = player.getEntityWorld();
        TileEntity te = world.getTileEntity(pos);
        if (te instanceof IInventory) {
            IInventory inventory = (IInventory) te;
            for (int i = start ; i <= stop  ; i++) {
                if (inventory.getStackInSlot(i) == null) {
                    ItemStack copy = heldItem.copy();
                    copy.stackSize = 1;
                    inventory.setInventorySlotContents(i, copy);
                    heldItem.stackSize--;
                    if (heldItem.stackSize == 0) {
                        player.setHeldItem(hand, null);
                    }
                    if (world.isRemote) {
                        player.addChatComponentMessage(new TextComponentString("Installed module"));
                    }
                    return true;
                }
            }
        }
        return false;
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
