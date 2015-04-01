package mcjty.rftools.items.screenmodules;

import mcjty.rftools.RFTools;
import mcjty.rftools.blocks.logic.RedstoneReceiverTileEntity;
import mcjty.rftools.blocks.logic.RedstoneTransmitterTileEntity;
import mcjty.rftools.blocks.screens.ModuleProvider;
import mcjty.rftools.blocks.screens.modules.RedstoneScreenModule;
import mcjty.rftools.blocks.screens.modules.ScreenModule;
import mcjty.rftools.blocks.screens.modulesclient.ClientScreenModule;
import mcjty.rftools.blocks.screens.modulesclient.RedstoneClientScreenModule;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.world.World;

import java.util.List;

public class RedstoneModuleItem extends Item implements ModuleProvider {

    public RedstoneModuleItem() {
        setMaxStackSize(1);
    }

    @Override
    public int getMaxItemUseDuration(ItemStack stack) {
        return 1;
    }

    @Override
    public Class<? extends ScreenModule> getServerScreenModule() {
        return RedstoneScreenModule.class;
    }

    @Override
    public Class<? extends ClientScreenModule> getClientScreenModule() {
        return RedstoneClientScreenModule.class;
    }

    @Override
    public String getName() {
        return "Red";
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void addInformation(ItemStack itemStack, EntityPlayer player, List list, boolean whatIsThis) {
        super.addInformation(itemStack, player, list, whatIsThis);
        list.add(EnumChatFormatting.GREEN + "Uses " + RedstoneScreenModule.RFPERTICK + " RF/tick");
        NBTTagCompound tagCompound = itemStack.getTagCompound();
        if (tagCompound != null) {
            list.add(EnumChatFormatting.YELLOW + "Label: " + tagCompound.getString("text"));
            int channel = tagCompound.getInteger("channel");
            if (channel != -1) {
                list.add(EnumChatFormatting.YELLOW + "Channel: " + channel);
            } else if (tagCompound.hasKey("monitorx")) {
                int mx = tagCompound.getInteger("monitorx");
                int my = tagCompound.getInteger("monitory");
                int mz = tagCompound.getInteger("monitorz");
                list.add(EnumChatFormatting.YELLOW + "Block at: " + mx + "," + my + "," + mz);
            }
        }
        list.add(EnumChatFormatting.WHITE + "Sneak right-click on a redstone transmitter or");
        list.add(EnumChatFormatting.WHITE + "receiver to set the channel for this module.");
        list.add(EnumChatFormatting.WHITE + "Or else sneak right-click on the side of any");
        list.add(EnumChatFormatting.WHITE + "block to monitor the redstone output on that side");
    }

    @Override
    public boolean onItemUse(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int side, float sx, float sy, float sz) {
        TileEntity te = world.getTileEntity(x, y, z);
        NBTTagCompound tagCompound = stack.getTagCompound();
        if (tagCompound == null) {
            tagCompound = new NBTTagCompound();
            stack.setTagCompound(tagCompound);
        }
        int channel = -1;
        if (te instanceof RedstoneReceiverTileEntity) {
            channel = ((RedstoneReceiverTileEntity) te).getChannel();
        } else if (te instanceof RedstoneTransmitterTileEntity) {
            channel = ((RedstoneTransmitterTileEntity) te).getChannel();
        } else {
            // We selected a random block.
            tagCompound.setInteger("channel", -1);
            tagCompound.setInteger("dim", world.provider.dimensionId);
            tagCompound.setInteger("monitorx", x);
            tagCompound.setInteger("monitory", y);
            tagCompound.setInteger("monitorz", z);
            tagCompound.setInteger("monitorside", side);
            if (world.isRemote) {
                RFTools.message(player, "Redstone module is set to " + x + "," + y + "," + z);
            }

            return true;
        }

        tagCompound.removeTag("dim");
        tagCompound.removeTag("monitorx");
        tagCompound.removeTag("monitory");
        tagCompound.removeTag("monitorz");
        tagCompound.removeTag("monitorside");

        if (channel != -1) {
            tagCompound.setInteger("channel", channel);
            if (world.isRemote) {
                RFTools.message(player, "Redstone module is set to channel '" + channel + "'");
            }
        } else {
            tagCompound.removeTag("channel");
            if (world.isRemote) {
                RFTools.message(player, "Redstone module is cleared");
            }
        }
        return true;
    }
}