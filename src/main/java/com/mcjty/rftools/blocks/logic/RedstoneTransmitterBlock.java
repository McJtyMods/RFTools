package com.mcjty.rftools.blocks.logic;

import com.mcjty.rftools.RFTools;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import mcp.mobius.waila.api.IWailaConfigHandler;
import mcp.mobius.waila.api.IWailaDataAccessor;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.world.World;
import org.lwjgl.input.Keyboard;

import java.util.List;

public class RedstoneTransmitterBlock extends LogicSlabBlock {
    public RedstoneTransmitterBlock() {
        super(Material.iron, "redstoneTransmitterBlock", RedstoneTransmitterTileEntity.class);
        setCreativeTab(RFTools.tabRfTools);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void addInformation(ItemStack itemStack, EntityPlayer player, List list, boolean whatIsThis) {
        super.addInformation(itemStack, player, list, whatIsThis);
        NBTTagCompound tagCompound = itemStack.getTagCompound();
        if (tagCompound != null) {
            int channel = tagCompound.getInteger("channel");
            list.add(EnumChatFormatting.GREEN + "Channel: " + channel);
        }

        if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT)) {
            list.add(EnumChatFormatting.WHITE + "This logic block accepts redstone signals and");
            list.add(EnumChatFormatting.WHITE + "sends them out wirelessly to linked receivers");
        } else {
            list.add(EnumChatFormatting.WHITE + RFTools.SHIFT_MESSAGE);
        }
    }

    @SideOnly(Side.CLIENT)
    @Override
    public List<String> getWailaBody(ItemStack itemStack, List<String> currenttip, IWailaDataAccessor accessor, IWailaConfigHandler config) {
        super.getWailaBody(itemStack, currenttip, accessor, config);
        NBTTagCompound tagCompound = accessor.getNBTData();
        if (tagCompound != null) {
            int channel = tagCompound.getInteger("channel");
            currenttip.add(EnumChatFormatting.GREEN + "Channel: " + channel);
        }
        return currenttip;
    }

    @Override
    public void onNeighborBlockChange(World world, int x, int y, int z, Block block) {
        super.onNeighborBlockChange(world, x, y, z, block);
        TileEntity te = world.getTileEntity(x, y, z);
        te.updateEntity();
    }

    @Override
    public void onBlockPlacedBy(World world, int x, int y, int z, EntityLivingBase entityLivingBase, ItemStack itemStack) {
        super.onBlockPlacedBy(world, x, y, z, entityLivingBase, itemStack);
        if (!world.isRemote) {
            RedstoneTransmitterTileEntity te = (RedstoneTransmitterTileEntity) world.getTileEntity(x, y, z);
            if (te.getChannel() == -1) {
                RedstoneChannels redstoneChannels = RedstoneChannels.getChannels(world);
                int id = redstoneChannels.newChannel();
                te.setChannel(id);
                redstoneChannels.save(world);
            }
            onNeighborBlockChange(world, x, y, z, this);
        }
    }

    @Override
    public void breakBlock(World world, int x, int y, int z, Block block, int meta) {
        if (!world.isRemote) {
            RedstoneChannels redstoneChannels = RedstoneChannels.getChannels(world);
            RedstoneTransmitterTileEntity te = (RedstoneTransmitterTileEntity) world.getTileEntity(x, y, z);
            if (te.getChannel() != -1) {
                redstoneChannels.deleteChannel(te.getChannel());
                redstoneChannels.save(world);
            }
        }
        super.breakBlock(world, x, y, z, block, meta);
    }


    @Override
    public int getGuiID() {
        return -1;
    }

    @Override
    public String getIdentifyingIconName() {
        return "machineRedstoneTransmitter";
    }

}
