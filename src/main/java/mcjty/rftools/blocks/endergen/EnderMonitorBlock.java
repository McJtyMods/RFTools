package mcjty.rftools.blocks.endergen;

import mcjty.container.EmptyContainer;
import mcjty.rftools.RFTools;
import mcjty.rftools.blocks.BlockTools;
import mcjty.rftools.blocks.logic.LogicSlabBlock;
import mcjty.varia.Coordinate;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

import java.util.List;

public class EnderMonitorBlock extends LogicSlabBlock {

    public EnderMonitorBlock() {
        super(Material.iron, "enderMonitorBlock", EnderMonitorTileEntity.class);
        setCreativeTab(RFTools.tabRfTools);
    }

    @Override
    public int getGuiID() {
        return RFTools.GUI_ENDERMONITOR;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public GuiContainer createClientGui(EntityPlayer entityPlayer, TileEntity tileEntity) {
        EnderMonitorTileEntity enderMonitorTileEntity = (EnderMonitorTileEntity) tileEntity;
        return new GuiEnderMonitor(enderMonitorTileEntity, new EmptyContainer(entityPlayer));
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void addInformation(ItemStack itemStack, EntityPlayer player, List list, boolean whatIsThis) {
        super.addInformation(itemStack, player, list, whatIsThis);
        NBTTagCompound tagCompound = itemStack.getTagCompound();
        if (tagCompound != null) {
            int mode = tagCompound.getInteger("mode");
            String smode = EnderMonitorMode.values()[mode].getDescription();
            list.add(EnumChatFormatting.GREEN + "Mode: " + smode);
        }
    }

    @Override
    public String getIdentifyingIconName() {
        return "machineEnderMonitorTop";
    }

    @Override
    protected void rotateBlock(World world, int x, int y, int z) {
        if (!world.isRemote) {
            unregisterFromEndergenic(world, x, y, z);
        }
        super.rotateBlock(world, x, y, z);
        if (!world.isRemote) {
            registerWithEndergenic(world, x, y, z);
        }
    }

    @Override
    public void onBlockPlacedBy(World world, int x, int y, int z, EntityLivingBase entityLivingBase, ItemStack itemStack) {
        super.onBlockPlacedBy(world, x, y, z, entityLivingBase, itemStack);
        if (!world.isRemote) {
            registerWithEndergenic(world, x, y, z);
        }
    }

    public void registerWithEndergenic(World world, int x, int y, int z) {
        int meta = world.getBlockMetadata(x, y, z);
        ForgeDirection k = BlockTools.getOrientationHoriz(meta);
        TileEntity te = world.getTileEntity(x + k.offsetX, y + k.offsetY, z + k.offsetZ);
        if (te instanceof EndergenicTileEntity) {
            EndergenicTileEntity endergenicTileEntity = (EndergenicTileEntity) te;
            endergenicTileEntity.addMonitor(new Coordinate(x, y, z));
        }
    }

    @Override
    public void breakBlock(World world, int x, int y, int z, Block block, int meta) {
        unregisterFromEndergenic(world, x, y, z);
        super.breakBlock(world, x, y, z, block, meta);
    }

    private void unregisterFromEndergenic(World world, int x, int y, int z) {
        int meta = world.getBlockMetadata(x, y, z);
        ForgeDirection k = BlockTools.getOrientationHoriz(meta);
        TileEntity te = world.getTileEntity(x + k.offsetX, y + k.offsetY, z + k.offsetZ);
        if (te instanceof EndergenicTileEntity) {
            EndergenicTileEntity endergenicTileEntity = (EndergenicTileEntity) te;
            endergenicTileEntity.removeMonitor(new Coordinate(x, y, z));
        }
    }

    @Override
    public void onNeighborBlockChange(World world, int x, int y, int z, Block block) {
        // We don't want to do what LogicSlabBlock does as we don't react on redstone input.
    }
}
