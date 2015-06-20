package mcjty.rftools.blocks.security;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import mcjty.entity.GenericTileEntity;
import mcjty.rftools.RFTools;
import mcjty.rftools.network.PacketHandler;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IIcon;
import net.minecraft.world.World;
import org.lwjgl.input.Keyboard;

import java.util.List;

public class SecurityCardItem extends Item {

    private IIcon activeIcon;

    public static String channelNameFromServer = "";
    private static long lastTime = 0;

    public SecurityCardItem() {
        setMaxStackSize(1);
    }

    @Override
    public int getMaxItemUseDuration(ItemStack stack) {
        return 1;
    }

    @Override
    public void registerIcons(IIconRegister iconRegister) {
        super.registerIcons(iconRegister);
        activeIcon = iconRegister.registerIcon(RFTools.MODID + ":securityCardItem_linked");
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void addInformation(ItemStack itemStack, EntityPlayer player, List list, boolean whatIsThis) {
        super.addInformation(itemStack, player, list, whatIsThis);
        NBTTagCompound tagCompound = itemStack.getTagCompound();
        int channel = -1;
        if (tagCompound != null && tagCompound.hasKey("channel")) {
            channel = tagCompound.getInteger("channel");
        }
        if (channel != -1) {
            if (System.currentTimeMillis() - lastTime > 250) {
                lastTime = System.currentTimeMillis();
                PacketHandler.INSTANCE.sendToServer(new PacketGetSecurityName(channel));
            }
            list.add(EnumChatFormatting.YELLOW + "Channel: " + channel + " (" + channelNameFromServer + ")");
        } else {
            list.add(EnumChatFormatting.YELLOW + "Channel is not set!");
        }
        if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT)) {
            list.add(EnumChatFormatting.WHITE + "Manage security channels in the Security Manager");
            list.add(EnumChatFormatting.WHITE + "and link this card to a channel. Sneak right-click");
            list.add(EnumChatFormatting.WHITE + "a block to link the channel to that block.");
            list.add(EnumChatFormatting.WHITE + "If you want to copy the channel from a block to");
            list.add(EnumChatFormatting.WHITE + "a card you can right click with an unlinked card");
        } else {
            list.add(EnumChatFormatting.WHITE + RFTools.SHIFT_MESSAGE);
        }
    }

    @Override
    public boolean onItemUse(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int side, float sx, float sy, float sz) {
        if (!world.isRemote) {
            TileEntity te = world.getTileEntity(x, y, z);
            if (te instanceof GenericTileEntity) {
                GenericTileEntity genericTileEntity = (GenericTileEntity) te;
                if (genericTileEntity.getOwnerUUID() == null) {
                    RFTools.message(player, EnumChatFormatting.RED + "This block has no owner!");
                } else {
                    if (isAdmin(player) || isOwner(player, genericTileEntity)) {
                        NBTTagCompound tagCompound = stack.getTagCompound();
                        if (tagCompound == null || !tagCompound.hasKey("channel")) {
                            int blockSecurity = genericTileEntity.getSecurityChannel();
                            if (blockSecurity == -1) {
                                RFTools.message(player, EnumChatFormatting.RED + "This security card is not setup correctly!");
                            } else {
                                if (tagCompound == null) {
                                    tagCompound = new NBTTagCompound();
                                    stack.setTagCompound(tagCompound);
                                }
                                tagCompound.setInteger("channel", blockSecurity);
                                RFTools.message(player, EnumChatFormatting.RED + "Copied security channel from block to card!");
                            }
                        } else {
                            int channel = tagCompound.getInteger("channel");
                            toggleSecuritySettings(player, genericTileEntity, channel);
                        }
                    } else {
                        RFTools.message(player, EnumChatFormatting.RED + "You cannot change security settings of a block you don't own!");
                    }
                }
            } else {
                RFTools.message(player, EnumChatFormatting.RED + "Security is not supported on this block!");
            }
            return true;
        }
       return true;
    }

    private boolean isOwner(EntityPlayer player, GenericTileEntity genericTileEntity) {
        return genericTileEntity.getOwnerUUID().equals(player.getPersistentID());
    }

    private boolean isAdmin(EntityPlayer player) {
        return player.capabilities.isCreativeMode || MinecraftServer.getServer().getConfigurationManager().func_152596_g(player.getGameProfile());
    }

    @SideOnly(Side.CLIENT)
    @Override
    public IIcon getIconIndex(ItemStack stack) {
        NBTTagCompound tagCompound = stack.getTagCompound();
        if (tagCompound != null && tagCompound.hasKey("channel")) {
            return activeIcon;
        } else {
            return itemIcon;
        }
    }


    private void toggleSecuritySettings(EntityPlayer player, GenericTileEntity genericTileEntity, int channel) {
        int sec = genericTileEntity.getSecurityChannel();
        if (sec == channel) {
            genericTileEntity.setSecurityChannel(-1);
            RFTools.message(player, "Security settings cleared from block!");
        } else {
            genericTileEntity.setSecurityChannel(channel);
            RFTools.message(player, "Security settings applied on block!");
        }
    }
}