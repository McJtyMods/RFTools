package mcjty.rftools.items.screenmodules;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import mcjty.rftools.RFTools;
import mcjty.rftools.blocks.logic.RedstoneChannels;
import mcjty.rftools.blocks.logic.RedstoneReceiverTileEntity;
import mcjty.rftools.blocks.screens.ModuleProvider;
import mcjty.rftools.blocks.screens.ScreenConfiguration;
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
        list.add(EnumChatFormatting.GREEN + "Uses " + ScreenConfiguration.BUTTON_RFPERTICK + " RF/tick");
        NBTTagCompound tagCompound = itemStack.getTagCompound();
        if (tagCompound != null) {
            list.add(EnumChatFormatting.YELLOW + "Label: " + tagCompound.getString("text"));
            int channel = tagCompound.getInteger("channel");
            if (channel != -1) {
                list.add(EnumChatFormatting.YELLOW + "Channel: " + channel);
            }
        }
        list.add(EnumChatFormatting.WHITE + "Sneak right-click on a redstone receiver");
        list.add(EnumChatFormatting.WHITE + "to create a channel for this module and also");
        list.add(EnumChatFormatting.WHITE + "set it to the receiver.");
    }

    @Override
    public boolean onItemUse(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int side, float sx, float sy, float sz) {
        if (world.isRemote) {
            return true;
        }

        TileEntity te = world.getTileEntity(x, y, z);
        NBTTagCompound tagCompound = stack.getTagCompound();
        if (tagCompound == null) {
            tagCompound = new NBTTagCompound();
            stack.setTagCompound(tagCompound);
        }
        int channel = -1;
        if (tagCompound.hasKey("channel")) {
            channel = tagCompound.getInteger("channel");
        }

        if (channel == -1) {
            RedstoneChannels redstoneChannels = RedstoneChannels.getChannels(world);
            channel = redstoneChannels.newChannel();
            redstoneChannels.save(world);
            RFTools.message(player, "Created channel " + channel + " for Button module");
            tagCompound.setInteger("channel", channel);
        }

        if (te instanceof RedstoneReceiverTileEntity) {
            ((RedstoneReceiverTileEntity) te).setChannel(channel);
            RFTools.message(player, "Receiver is set to channel " + channel);
        }
        return true;
    }
}