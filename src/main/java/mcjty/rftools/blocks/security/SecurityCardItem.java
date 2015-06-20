package mcjty.rftools.blocks.security;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import mcjty.entity.GenericTileEntity;
import mcjty.rftools.RFTools;
import mcjty.rftools.network.PacketHandler;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.world.World;
import org.lwjgl.input.Keyboard;

import java.util.List;

public class SecurityCardItem extends Item {

    public static String channelNameFromServer = "";
    private static long lastTime = 0;

    public SecurityCardItem() {
        setMaxStackSize(1);
    }

    @Override
    public int getMaxItemUseDuration(ItemStack stack) {
        return 1;
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
            list.add(EnumChatFormatting.WHITE + "a block to link the channel to that block");
        } else {
            list.add(EnumChatFormatting.WHITE + RFTools.SHIFT_MESSAGE);
        }
    }

    @Override
    public boolean onItemUse(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int side, float sx, float sy, float sz) {
        if (!world.isRemote) {
            NBTTagCompound tagCompound = stack.getTagCompound();
            if (tagCompound == null || !tagCompound.hasKey("channel")) {
                RFTools.message(player, EnumChatFormatting.RED + "This security card is not setup correctly!");
            }
            int channel = tagCompound.getInteger("channel");
            TileEntity te = world.getTileEntity(x, y, z);
            if (te instanceof GenericTileEntity) {
                GenericTileEntity genericTileEntity = (GenericTileEntity) te;
                if (genericTileEntity.getOwnerUUID() == null) {
                    RFTools.message(player, EnumChatFormatting.RED + "This block has no owner!");
                } else {
                    if (player.capabilities.isCreativeMode || MinecraftServer.getServer().getConfigurationManager().func_152596_g(player.getGameProfile())) {
                        int sec = genericTileEntity.getSecurityChannel();
                        if (sec == channel) {
                            genericTileEntity.setSecurityChannel(-1);
                            RFTools.message(player, "Security settings cleared!");
                        } else {
                            genericTileEntity.setSecurityChannel(channel);
                            RFTools.message(player, "Security settings applied!");
                        }
                    } else if (genericTileEntity.getOwnerUUID().equals(player.getPersistentID())) {
                        int sec = genericTileEntity.getSecurityChannel();
                        if (sec == channel) {
                            genericTileEntity.setSecurityChannel(-1);
                            RFTools.message(player, "Security settings cleared!");
                        } else {
                            genericTileEntity.setSecurityChannel(channel);
                            RFTools.message(player, "Security settings applied!");
                        }
                    } else {
                        RFTools.message(player, EnumChatFormatting.RED + "You cannot change security settings of a block you don't own!");
                    }
                }
            } else {
                RFTools.message(player, EnumChatFormatting.RED + "Onwership is not supported on this block!");
            }
            return true;
        }
       return true;
    }
}