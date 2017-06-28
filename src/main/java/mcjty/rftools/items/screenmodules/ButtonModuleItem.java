package mcjty.rftools.items.screenmodules;

import mcjty.lib.varia.Logging;
import mcjty.rftools.api.screens.IClientScreenModule;
import mcjty.rftools.api.screens.IModuleProvider;
import mcjty.rftools.api.screens.IScreenModule;
import mcjty.rftools.blocks.logic.wireless.RedstoneChannels;
import mcjty.rftools.blocks.logic.wireless.RedstoneReceiverTileEntity;
import mcjty.rftools.blocks.logic.wireless.RedstoneTransmitterTileEntity;
import mcjty.rftools.blocks.screens.ScreenConfiguration;
import mcjty.rftools.blocks.screens.modules.ButtonScreenModule;
import mcjty.rftools.blocks.screens.modulesclient.ButtonClientScreenModule;
import mcjty.rftools.items.GenericRFToolsItem;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.List;

public class ButtonModuleItem extends GenericRFToolsItem implements IModuleProvider {

    public ButtonModuleItem() {
        super("button_module");
    }

    @Override
    public int getMaxItemUseDuration(ItemStack stack) {
        return 1;
    }

    @Override
    public Class<? extends IScreenModule> getServerScreenModule() {
        return ButtonScreenModule.class;
    }

    @Override
    public Class<? extends IClientScreenModule> getClientScreenModule() {
        return ButtonClientScreenModule.class;
    }

    @Override
    public String getName() {
        return "Button";
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void addInformation(ItemStack itemStack, @Nullable World player, List<String> list, ITooltipFlag advanced) {
        super.addInformation(itemStack, player, list, advanced);
        list.add(TextFormatting.GREEN + "Uses " + ScreenConfiguration.BUTTON_RFPERTICK + " RF/tick");
        NBTTagCompound tagCompound = itemStack.getTagCompound();
        if (tagCompound != null) {
            list.add(TextFormatting.YELLOW + "Label: " + tagCompound.getString("text"));
            int channel = tagCompound.getInteger("channel");
            if (channel != -1) {
                list.add(TextFormatting.YELLOW + "Channel: " + channel);
            }
        }
        list.add(TextFormatting.WHITE + "Sneak right-click on a redstone receiver");
        list.add(TextFormatting.WHITE + "to create a channel for this module and also");
        list.add(TextFormatting.WHITE + "set it to the receiver. You can also use this");
        list.add(TextFormatting.WHITE + "on a transmitter or already set receiver to copy");
        list.add(TextFormatting.WHITE + "the channel to the button");
    }

    @Override
    public EnumActionResult onItemUse(EntityPlayer player, World world, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        ItemStack stack = player.getHeldItem(hand);
        if (world.isRemote) {
            return EnumActionResult.SUCCESS;
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
                    return EnumActionResult.SUCCESS;
                }
            }
            Logging.message(player, TextFormatting.RED + "Nothing happens!");

        } else if (te instanceof RedstoneReceiverTileEntity) {
            int blockChannel = ((RedstoneReceiverTileEntity)te).getChannel();

            if (channel == -1) {
                if (blockChannel != -1) {
                    Logging.message(player, "Copied channel " + blockChannel + " to Button module");
                    tagCompound.setInteger("channel", blockChannel);
                    return EnumActionResult.SUCCESS;
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
            Logging.message(player, TextFormatting.RED + "You can only use this on a redstone receiver/transmitter!");
        }

        return EnumActionResult.SUCCESS;
    }
}