package mcjty.rftools.blocks.security;

import mcjty.lib.tileentity.GenericTileEntity;
import mcjty.lib.typed.TypedMap;
import mcjty.lib.varia.Logging;
import mcjty.rftools.network.RFToolsMessages;
import mcjty.rftools.setup.CommandHandler;
import mcjty.rftools.setup.GuiProxy;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;


import org.lwjgl.input.Keyboard;

import java.util.List;

public class SecurityCardItem extends GenericRFToolsItem {

    public static String channelNameFromServer = "";
    private static long lastTime = 0;

    public SecurityCardItem() {
        super("security_card");
        setMaxStackSize(1);
    }

    @Override
    public int getMaxItemUseDuration(ItemStack stack) {
        return 1;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void addInformation(ItemStack itemStack, World player, List<ITextComponent> list, ITooltipFlag whatIsThis) {
        super.addInformation(itemStack, player, list, whatIsThis);
        CompoundNBT tagCompound = itemStack.getTag();
        int channel = -1;
        if (tagCompound != null && tagCompound.hasKey("channel")) {
            channel = tagCompound.getInt("channel");
        }
        if (channel != -1) {
            if (System.currentTimeMillis() - lastTime > 250) {
                lastTime = System.currentTimeMillis();
                RFToolsMessages.sendToServer(CommandHandler.CMD_GET_SECURITY_NAME, TypedMap.builder().put(CommandHandler.PARAM_ID, channel));
            }
            list.add(TextFormatting.YELLOW + "Channel: " + channel + " (" + channelNameFromServer + ")");
        } else {
            list.add(TextFormatting.YELLOW + "Channel is not set!");
        }
        if (McJtyLib.proxy.isShiftKeyDown()) {
            list.add(TextFormatting.WHITE + "Manage security channels in the Security Manager");
            list.add(TextFormatting.WHITE + "and link this card to a channel. Sneak right-click");
            list.add(TextFormatting.WHITE + "a block to link the channel to that block.");
            list.add(TextFormatting.WHITE + "If you want to copy the channel from a block to");
            list.add(TextFormatting.WHITE + "a card you can right click with an unlinked card");
        } else {
            list.add(TextFormatting.WHITE + GuiProxy.SHIFT_MESSAGE);
        }
    }

    @Override
    public ActionResultType onItemUse(PlayerEntity player, World world, BlockPos pos, Hand hand, Direction facing, float hitX, float hitY, float hitZ) {
        ItemStack stack = player.getHeldItem(hand);
        if (!world.isRemote) {
            TileEntity te = world.getTileEntity(pos);
            if (te instanceof GenericTileEntity) {
                // @tod security api
                GenericTileEntity genericTileEntity = (GenericTileEntity) te;
                if (genericTileEntity.getOwnerUUID() == null) {
                    Logging.message(player, TextFormatting.RED + "This block has no owner!");
                } else {
                    if (OrphaningCardItem.isPrivileged(player, world) || isOwner(player, genericTileEntity)) {
                        CompoundNBT tagCompound = stack.getTag();
                        if (tagCompound == null || !tagCompound.hasKey("channel")) {
                            int blockSecurity = genericTileEntity.getSecurityChannel();
                            if (blockSecurity == -1) {
                                Logging.message(player, TextFormatting.RED + "This security card is not setup correctly!");
                            } else {
                                if (tagCompound == null) {
                                    tagCompound = new CompoundNBT();
                                    stack.setTagCompound(tagCompound);
                                }
                                tagCompound.putInt("channel", blockSecurity);
                                Logging.message(player, TextFormatting.RED + "Copied security channel from block to card!");
                            }
                        } else {
                            int channel = tagCompound.getInt("channel");
                            toggleSecuritySettings(player, genericTileEntity, channel);
                        }
                    } else {
                        Logging.message(player, TextFormatting.RED + "You cannot change security settings of a block you don't own!");
                    }
                }
            } else {
                Logging.message(player, TextFormatting.RED + "Security is not supported on this block!");
            }
            return ActionResultType.SUCCESS;
        }
       return ActionResultType.SUCCESS;
    }

    private boolean isOwner(PlayerEntity player, GenericTileEntity genericTileEntity) {
        return genericTileEntity.getOwnerUUID().equals(player.getPersistentID());
    }

//    @SideOnly(Side.CLIENT)
//    @Override
//    public IIcon getIconIndex(ItemStack stack) {
//        CompoundNBT tagCompound = stack.getTag();
//        if (tagCompound != null && tagCompound.hasKey("channel")) {
//            return activeIcon;
//        } else {
//            return itemIcon;
//        }
//    }


    private void toggleSecuritySettings(PlayerEntity player, GenericTileEntity genericTileEntity, int channel) {
        int sec = genericTileEntity.getSecurityChannel();
        if (sec == channel) {
            genericTileEntity.setSecurityChannel(-1);
            Logging.message(player, "Security settings cleared from block!");
        } else {
            genericTileEntity.setSecurityChannel(channel);
            Logging.message(player, "Security settings applied on block!");
        }
    }
}