package com.mcjty.rftools.items.screenmodules;

import com.mcjty.rftools.RFTools;
import com.mcjty.rftools.blocks.logic.RedstoneReceiverTileEntity;
import com.mcjty.rftools.blocks.logic.RedstoneTransmitterTileEntity;
import com.mcjty.rftools.blocks.screens.ModuleProvider;
import com.mcjty.rftools.blocks.screens.modules.RedstoneScreenModule;
import com.mcjty.rftools.blocks.screens.modules.ScreenModule;
import com.mcjty.rftools.blocks.screens.modulesclient.ClientScreenModule;
import com.mcjty.rftools.blocks.screens.modulesclient.RedstoneClientScreenModule;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.world.World;

import java.util.List;

public class RedstoneModuleItem extends Item implements ModuleProvider {

    public RedstoneModuleItem() {
        setMaxStackSize(1);
    }

    @Override
    public int getMaxItemUseDuration(ItemStack stack) {
        return 1;
    }

    @Override
    public Class<? extends ScreenModule> getServerScreenModule() {
        return RedstoneScreenModule.class;
    }

    @Override
    public Class<? extends ClientScreenModule> getClientScreenModule() {
        return RedstoneClientScreenModule.class;
    }

    @Override
    public String getName() {
        return "Red";
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void addInformation(ItemStack itemStack, EntityPlayer player, List list, boolean whatIsThis) {
        super.addInformation(itemStack, player, list, whatIsThis);
        list.add(EnumChatFormatting.GREEN + "Uses " + RedstoneScreenModule.RFPERTICK + " RF/tick");
        NBTTagCompound tagCompound = itemStack.getTagCompound();
        if (tagCompound != null) {
            list.add(EnumChatFormatting.YELLOW + "Label: " + tagCompound.getString("text"));
            list.add(EnumChatFormatting.YELLOW + "Channel: " + tagCompound.getInteger("channel"));
        }
        list.add(EnumChatFormatting.YELLOW + "Sneak right-click on a redstone transmitter or");
        list.add(EnumChatFormatting.YELLOW + "receiver to set the channel for this module");
    }

    @Override
    public boolean onItemUse(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int side, float sx, float sy, float sz) {
        TileEntity te = world.getTileEntity(x, y, z);
        NBTTagCompound tagCompound = stack.getTagCompound();
        if (tagCompound == null) {
            tagCompound = new NBTTagCompound();
        }
        int channel = -1;
        if (te instanceof RedstoneReceiverTileEntity) {
            channel = ((RedstoneReceiverTileEntity) te).getChannel();
        } else if (te instanceof RedstoneTransmitterTileEntity) {
            channel = ((RedstoneTransmitterTileEntity) te).getChannel();
        }

        if (channel != -1) {
            tagCompound.setInteger("channel", channel);
            if (world.isRemote) {
                RFTools.message(player, "Redstone module is set to channel '" + channel + "'");
            }
        } else {
            tagCompound.removeTag("channel");
            if (world.isRemote) {
                RFTools.message(player, "Redstone module is cleared");
            }
        }
        stack.setTagCompound(tagCompound);
        return true;
    }
}