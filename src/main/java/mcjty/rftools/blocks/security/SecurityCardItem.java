package mcjty.rftools.blocks.security;

import mcjty.lib.tileentity.GenericTileEntity;
import mcjty.lib.typed.TypedMap;
import mcjty.lib.varia.Logging;
import mcjty.rftools.CommandHandler;
import mcjty.rftools.gui.GuiProxy;
import mcjty.rftools.items.GenericRFToolsItem;
import mcjty.rftools.network.RFToolsMessages;
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
    public void addInformation(ItemStack itemStack, World player, List<String> list, ITooltipFlag whatIsThis) {
        super.addInformation(itemStack, player, list, whatIsThis);
        NBTTagCompound tagCompound = itemStack.getTagCompound();
        int channel = -1;
        if (tagCompound != null && tagCompound.hasKey("channel")) {
            channel = tagCompound.getInteger("channel");
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
        if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT)) {
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
    public EnumActionResult onItemUse(EntityPlayer player, World world, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
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
                        NBTTagCompound tagCompound = stack.getTagCompound();
                        if (tagCompound == null || !tagCompound.hasKey("channel")) {
                            int blockSecurity = genericTileEntity.getSecurityChannel();
                            if (blockSecurity == -1) {
                                Logging.message(player, TextFormatting.RED + "This security card is not setup correctly!");
                            } else {
                                if (tagCompound == null) {
                                    tagCompound = new NBTTagCompound();
                                    stack.setTagCompound(tagCompound);
                                }
                                tagCompound.setInteger("channel", blockSecurity);
                                Logging.message(player, TextFormatting.RED + "Copied security channel from block to card!");
                            }
                        } else {
                            int channel = tagCompound.getInteger("channel");
                            toggleSecuritySettings(player, genericTileEntity, channel);
                        }
                    } else {
                        Logging.message(player, TextFormatting.RED + "You cannot change security settings of a block you don't own!");
                    }
                }
            } else {
                Logging.message(player, TextFormatting.RED + "Security is not supported on this block!");
            }
            return EnumActionResult.SUCCESS;
        }
       return EnumActionResult.SUCCESS;
    }

    private boolean isOwner(EntityPlayer player, GenericTileEntity genericTileEntity) {
        return genericTileEntity.getOwnerUUID().equals(player.getPersistentID());
    }

//    @SideOnly(Side.CLIENT)
//    @Override
//    public IIcon getIconIndex(ItemStack stack) {
//        NBTTagCompound tagCompound = stack.getTagCompound();
//        if (tagCompound != null && tagCompound.hasKey("channel")) {
//            return activeIcon;
//        } else {
//            return itemIcon;
//        }
//    }


    private void toggleSecuritySettings(EntityPlayer player, GenericTileEntity genericTileEntity, int channel) {
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