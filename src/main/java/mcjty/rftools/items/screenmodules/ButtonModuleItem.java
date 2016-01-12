package mcjty.rftools.items.screenmodules;

import mcjty.lib.varia.Logging;
import mcjty.rftools.blocks.logic.RedstoneChannels;
import mcjty.rftools.blocks.logic.RedstoneReceiverTileEntity;
import mcjty.rftools.blocks.logic.RedstoneTransmitterTileEntity;
import mcjty.rftools.blocks.screens.ModuleProvider;
import mcjty.rftools.blocks.screens.ScreenConfiguration;
import mcjty.rftools.blocks.screens.modules.ButtonScreenModule;
import mcjty.rftools.blocks.screens.modules.ScreenModule;
import mcjty.rftools.blocks.screens.modulesclient.ButtonClientScreenModule;
import mcjty.rftools.blocks.screens.modulesclient.ClientScreenModule;
import mcjty.rftools.items.GenericRFToolsItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.List;

public class ButtonModuleItem extends GenericRFToolsItem implements ModuleProvider {

    public ButtonModuleItem() {
        super("button_module");
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
        list.add(EnumChatFormatting.WHITE + "set it to the receiver. You can also use this");
        list.add(EnumChatFormatting.WHITE + "on a transmitter or already set receiver to copy");
        list.add(EnumChatFormatting.WHITE + "the channel to the button");
    }

    @Override
    public boolean onItemUse(ItemStack stack, EntityPlayer player, World world, BlockPos pos, EnumFacing side, float hitX, float hitY, float hitZ) {
        if (world.isRemote) {
            return true;
        }

        TileEntity te = world.getTileEntity(pos);
        NBTTagCompound tagCompound = stack.getTagCompound();
        if (tagCompound == null) {
            tagCompound = new NBTTagCompound();
            stack.setTagCompound(tagCompound);
        }
        int channel = -1;
        if (tagCompound.hasKey("channel")) {
            channel = tagCompound.getInteger("channel");
        }

        if (te instanceof RedstoneTransmitterTileEntity) {
            int blockChannel = ((RedstoneTransmitterTileEntity)te).getChannel();

            if (channel == -1) {
                if (blockChannel != -1) {
                    Logging.message(player, "Copied channel " + blockChannel + " to Button module");
                    tagCompound.setInteger("channel", blockChannel);
                    return true;
                }
            }
            Logging.message(player, EnumChatFormatting.RED + "Nothing happens!");

        } else if (te instanceof RedstoneReceiverTileEntity) {
            int blockChannel = ((RedstoneReceiverTileEntity)te).getChannel();

            if (channel == -1) {
                if (blockChannel != -1) {
                    Logging.message(player, "Copied channel " + blockChannel + " to Button module");
                    tagCompound.setInteger("channel", blockChannel);
                    return true;
                } else {
                    RedstoneChannels redstoneChannels = RedstoneChannels.getChannels(world);
                    channel = redstoneChannels.newChannel();
                    redstoneChannels.save(world);
                    Logging.message(player, "Created channel " + channel + " for Button module");
                    tagCompound.setInteger("channel", channel);
                }
            }

            ((RedstoneReceiverTileEntity) te).setChannel(channel);
            Logging.message(player, "Receiver is set to channel " + channel);
        } else {
            Logging.message(player, EnumChatFormatting.RED + "You can only use this on a redstone receiver/transmitter!");
        }

        return true;
    }
}