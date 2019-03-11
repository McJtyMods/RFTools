package mcjty.rftools.items.builder;

import mcjty.lib.varia.Logging;
import mcjty.rftools.RFTools;
import mcjty.rftools.blocks.builder.BuilderConfiguration;
import mcjty.rftools.blocks.builder.SpaceChamberControllerTileEntity;
import mcjty.rftools.proxy.GuiProxy;
import mcjty.rftools.items.GenericRFToolsItem;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
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
    public void addInformation(ItemStack itemStack, World player, List<String> list, ITooltipFlag whatIsThis) {
        super.addInformation(itemStack, player, list, whatIsThis);
        NBTTagCompound tagCompound = itemStack.getTagCompound();
        int channel = -1;
        if (tagCompound != null) {
            channel = tagCompound.getInteger("channel");
        }
        if (channel != -1) {
            list.add(TextFormatting.YELLOW + "Channel: " + channel);
        } else {
            list.add(TextFormatting.YELLOW + "Channel is not set!");
        }
        if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT)) {
            list.add(TextFormatting.WHITE + "Sneak right-click on a space chamber controller");
            list.add(TextFormatting.WHITE + "to set the channel for this card.");
            list.add(TextFormatting.WHITE + "Right-click in the air to show an overview of");
            list.add(TextFormatting.WHITE + "the area contents.");
            list.add(TextFormatting.WHITE + "Insert it in a builder to copy/move the");
            list.add(TextFormatting.WHITE + "linked area");
            list.add(TextFormatting.GREEN + "Base cost: " + BuilderConfiguration.builderRfPerOperation + " RF/t per block");
            list.add(TextFormatting.GREEN + "(final cost depends on infusion level)");
        } else {
            list.add(TextFormatting.WHITE + GuiProxy.SHIFT_MESSAGE);
        }
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
        if (!player.isSneaking()) {
            showDetails(world, player, player.getHeldItem(hand));
        }
        return super.onItemRightClick(world, player, hand);
    }

    @Override
    public EnumActionResult onItemUse(EntityPlayer player, World world, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        ItemStack stack = player.getHeldItem(hand);
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
        return EnumActionResult.SUCCESS;
    }

    private void showDetails(World world, EntityPlayer player, ItemStack stack) {
        if (stack.getTagCompound() != null && stack.getTagCompound().hasKey("channel")) {
            int channel = stack.getTagCompound().getInteger("channel");
            if (channel != -1) {
                showDetailsGui(world, player);
            } else {
                Logging.message(player, TextFormatting.YELLOW + "Card is not linked!");
            }
        }
    }

    private void showDetailsGui(World world, EntityPlayer player) {
        if (world.isRemote) {
            player.openGui(RFTools.instance, GuiProxy.GUI_CHAMBER_DETAILS, player.getEntityWorld(), (int) player.posX, (int) player.posY, (int) player.posZ);
        }
    }

}