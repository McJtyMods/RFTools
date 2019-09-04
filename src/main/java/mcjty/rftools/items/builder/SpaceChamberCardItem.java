package mcjty.rftools.items.builder;

import mcjty.lib.McJtyLib;
import mcjty.lib.varia.Logging;
import mcjty.rftools.RFTools;
import mcjty.rftools.blocks.builder.BuilderConfiguration;
import mcjty.rftools.blocks.builder.SpaceChamberControllerTileEntity;
import mcjty.rftools.setup.GuiProxy;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;

import java.util.List;

public class SpaceChamberCardItem extends Item {

    public SpaceChamberCardItem() {
        super(new Properties().maxStackSize(1).defaultMaxDamage(1).group(RFTools.setup.getTab()));
        setRegistryName("space_chamber_card");
    }

//    @Override
//    public int getMaxItemUseDuration(ItemStack stack) {
//        return 1;
//    }

    @Override
    public void addInformation(ItemStack itemStack, World world, List<ITextComponent> list, ITooltipFlag flag) {
        super.addInformation(itemStack, world, list, flag);
        CompoundNBT tagCompound = itemStack.getTag();
        int channel = -1;
        if (tagCompound != null) {
            channel = tagCompound.getInt("channel");
        }
        if (channel != -1) {
            list.add(new StringTextComponent(TextFormatting.YELLOW + "Channel: " + channel));
        } else {
            list.add(new StringTextComponent(TextFormatting.YELLOW + "Channel is not set!"));
        }
        if (McJtyLib.proxy.isShiftKeyDown()) {
            list.add(new StringTextComponent(TextFormatting.WHITE + "Sneak right-click on a space chamber controller"));
            list.add(new StringTextComponent(TextFormatting.WHITE + "to set the channel for this card."));
            list.add(new StringTextComponent(TextFormatting.WHITE + "Right-click in the air to show an overview of"));
            list.add(new StringTextComponent(TextFormatting.WHITE + "the area contents."));
            list.add(new StringTextComponent(TextFormatting.WHITE + "Insert it in a builder to copy/move the"));
            list.add(new StringTextComponent(TextFormatting.WHITE + "linked area"));
            list.add(new StringTextComponent(TextFormatting.GREEN + "Base cost: " + BuilderConfiguration.builderRfPerOperation.get() + " RF/t per block"));
            list.add(new StringTextComponent(TextFormatting.GREEN + "(final cost depends on infusion level)"));
        } else {
            list.add(new StringTextComponent(TextFormatting.WHITE + GuiProxy.SHIFT_MESSAGE));
        }
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, PlayerEntity player, Hand hand) {
        if (!player.isSneaking()) {
            showDetails(world, player, player.getHeldItem(hand));
        }
        return super.onItemRightClick(world, player, hand);
    }

    @Override
    public ActionResultType onItemUse(ItemUseContext context) {
        ItemStack stack = context.getItem();
        World world = context.getWorld();
        BlockPos pos = context.getPos();
        PlayerEntity player = context.getPlayer();
        TileEntity te = world.getTileEntity(pos);
        CompoundNBT tagCompound = stack.getTag();
        if (tagCompound == null) {
            tagCompound = new CompoundNBT();
            stack.setTag(tagCompound);
        }

        int channel = -1;
        if (te instanceof SpaceChamberControllerTileEntity) {
            channel = ((SpaceChamberControllerTileEntity) te).getChannel();
        }

        if (channel == -1) {
            showDetails(world, player, stack);
        } else {
            tagCompound.putInt("channel", channel);
            if (world.isRemote) {
                Logging.message(player, "Card is set to channel '" + channel + "'");
            }
        }
        return ActionResultType.SUCCESS;
    }

    private void showDetails(World world, PlayerEntity player, ItemStack stack) {
        if (stack.getTag() != null && stack.getTag().contains("channel")) {
            int channel = stack.getTag().getInt("channel");
            if (channel != -1) {
                showDetailsGui(world, player);
            } else {
                Logging.message(player, TextFormatting.YELLOW + "Card is not linked!");
            }
        }
    }

    private void showDetailsGui(World world, PlayerEntity player) {
        if (world.isRemote) {
            // @todo 1.14
//            player.openGui(RFTools.instance, GuiProxy.GUI_CHAMBER_DETAILS, player.getEntityWorld(), (int) player.posX, (int) player.posY, (int) player.posZ);
        }
    }

}