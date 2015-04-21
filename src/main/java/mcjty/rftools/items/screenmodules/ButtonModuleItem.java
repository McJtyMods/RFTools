package mcjty.rftools.items.screenmodules;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import mcjty.rftools.RFTools;
import mcjty.rftools.blocks.logic.RedstoneReceiverTileEntity;
import mcjty.rftools.blocks.logic.RedstoneTransmitterTileEntity;
import mcjty.rftools.blocks.screens.ModuleProvider;
import mcjty.rftools.blocks.screens.modules.ButtonScreenModule;
import mcjty.rftools.blocks.screens.modules.ScreenModule;
import mcjty.rftools.blocks.screens.modulesclient.ButtonClientScreenModule;
import mcjty.rftools.blocks.screens.modulesclient.ClientScreenModule;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.world.World;

import java.util.List;

public class ButtonModuleItem extends Item implements ModuleProvider {

    public ButtonModuleItem() {
        setMaxStackSize(1);
    }

    @Override
    public int getMaxItemUseDuration(ItemStack stack) {
        return 1;
    }

    @Override
    public Class<? extends ScreenModule> getServerScreenModule() {
        return ButtonScreenModule.class;
    }

    @Override
    public Class<? extends ClientScreenModule> getClientScreenModule() {
        return ButtonClientScreenModule.class;
    }

    @Override
    public String getName() {
        return "Button";
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void addInformation(ItemStack itemStack, EntityPlayer player, List list, boolean whatIsThis) {
        super.addInformation(itemStack, player, list, whatIsThis);
        list.add(EnumChatFormatting.GREEN + "Uses " + ButtonScreenModule.RFPERTICK + " RF/tick");
        NBTTagCompound tagCompound = itemStack.getTagCompound();
        if (tagCompound != null) {
            list.add(EnumChatFormatting.YELLOW + "Label: " + tagCompound.getString("text"));
            int channel = tagCompound.getInteger("channel");
            if (channel != -1) {
                list.add(EnumChatFormatting.YELLOW + "Channel: " + channel);
            }
        }
        list.add(EnumChatFormatting.WHITE + "Sneak right-click on a redstone transmitter or");
        list.add(EnumChatFormatting.WHITE + "receiver to set the channel for this module.");
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
        }

        tagCompound.removeTag("dim");
        tagCompound.removeTag("monitorx");
        tagCompound.removeTag("monitory");
        tagCompound.removeTag("monitorz");
        tagCompound.removeTag("monitorside");

        if (channel != -1) {
            tagCompound.setInteger("channel", channel);
            if (world.isRemote) {
                RFTools.message(player, "Button module is set to channel '" + channel + "'");
            }
        } else {
            tagCompound.removeTag("channel");
            if (world.isRemote) {
                RFTools.message(player, "Button module is cleared");
            }
        }
        return true;
    }
}