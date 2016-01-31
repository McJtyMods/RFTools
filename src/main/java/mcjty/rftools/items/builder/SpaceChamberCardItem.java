package mcjty.rftools.items.builder;

import mcjty.lib.varia.Logging;
import mcjty.rftools.RFTools;
import mcjty.rftools.blocks.builder.BuilderConfiguration;
import mcjty.rftools.blocks.builder.SpaceChamberControllerTileEntity;
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
import org.lwjgl.input.Keyboard;

import java.util.List;

public class SpaceChamberCardItem extends GenericRFToolsItem {

    public SpaceChamberCardItem() {
        super("space_chamber_card");
        setMaxStackSize(1);
    }

    @Override
    public int getMaxItemUseDuration(ItemStack stack) {
        return 1;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void addInformation(ItemStack itemStack, EntityPlayer player, List<String> list, boolean whatIsThis) {
        super.addInformation(itemStack, player, list, whatIsThis);
        NBTTagCompound tagCompound = itemStack.getTagCompound();
        int channel = -1;
        if (tagCompound != null) {
            channel = tagCompound.getInteger("channel");
        }
        if (channel != -1) {
            list.add(EnumChatFormatting.YELLOW + "Channel: " + channel);
        } else {
            list.add(EnumChatFormatting.YELLOW + "Channel is not set!");
        }
        if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT)) {
            list.add(EnumChatFormatting.WHITE + "Sneak right-click on a space chamber controller");
            list.add(EnumChatFormatting.WHITE + "to set the channel for this card.");
            list.add(EnumChatFormatting.WHITE + "Right-click in the air to show an overview of");
            list.add(EnumChatFormatting.WHITE + "the area contents.");
            list.add(EnumChatFormatting.WHITE + "Insert it in a builder to copy/move the");
            list.add(EnumChatFormatting.WHITE + "linked area");
            list.add(EnumChatFormatting.GREEN + "Base cost: " + BuilderConfiguration.builderRfPerOperation + " RF/t per block");
            list.add(EnumChatFormatting.GREEN + "(final cost depends on infusion level)");
        } else {
            list.add(EnumChatFormatting.WHITE + RFTools.SHIFT_MESSAGE);
        }
    }

    @Override
    public ItemStack onItemRightClick(ItemStack stack, World world, EntityPlayer player) {
        if (!player.isSneaking()) {
            showDetails(world, player, stack);
        }
        return super.onItemRightClick(stack, world, player);
    }

    @Override
    public boolean onItemUse(ItemStack stack, EntityPlayer player, World world, BlockPos pos, EnumFacing side, float hitX, float hitY, float hitZ) {
        TileEntity te = world.getTileEntity(pos);
        NBTTagCompound tagCompound = stack.getTagCompound();
        if (tagCompound == null) {
            tagCompound = new NBTTagCompound();
            stack.setTagCompound(tagCompound);
        }

        int channel = -1;
        if (te instanceof SpaceChamberControllerTileEntity) {
            channel = ((SpaceChamberControllerTileEntity) te).getChannel();
        }

        if (channel == -1) {
            showDetails(world, player, stack);
        } else {
            tagCompound.setInteger("channel", channel);
            if (world.isRemote) {
                Logging.message(player, "Card is set to channel '" + channel + "'");
            }
        }
        return true;
    }

    private void showDetails(World world, EntityPlayer player, ItemStack stack) {
        if (stack.getTagCompound() != null && stack.getTagCompound().hasKey("channel")) {
            int channel = stack.getTagCompound().getInteger("channel");
            if (channel != -1) {
                showDetailsGui(world, player);
            } else {
                Logging.message(player, EnumChatFormatting.YELLOW + "Card is not linked!");
            }
        }
    }

    private void showDetailsGui(World world, EntityPlayer player) {
        if (world.isRemote) {
            player.openGui(RFTools.instance, RFTools.GUI_CHAMBER_DETAILS, player.worldObj, (int) player.posX, (int) player.posY, (int) player.posZ);
        }
    }

}