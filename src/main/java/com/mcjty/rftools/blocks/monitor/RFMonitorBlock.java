package com.mcjty.rftools.blocks.monitor;

import cofh.api.energy.IEnergyHandler;
import com.mcjty.rftools.BlockInfo;
import com.mcjty.varia.Coordinate;
import com.mcjty.rftools.RFTools;
import com.mcjty.rftools.blocks.BlockTools;
import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;

public class RFMonitorBlock extends Block implements ITileEntityProvider {

    private List<BlockInfo> adjacentBlocks = new ArrayList<BlockInfo>();

    private IIcon iconFront;
    private IIcon iconFront0;
    private IIcon iconFront1;
    private IIcon iconFront2;
    private IIcon iconFront3;
    private IIcon iconFront4;
    private IIcon iconSide;

    public RFMonitorBlock(Material material) {
        super(material);
        setBlockName("rfMonitorBlock");
    }

    @Override
    public TileEntity createNewTileEntity(World world, int i) {
        return null;
    }

    @Override
    public TileEntity createTileEntity(World world, int metadata) {
        return new RFMonitorBlockTileEntity();
    }

    @Override
    public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int side, float sidex, float sidey, float sidez) {
        adjacentBlocks.clear();
        if (world.isRemote) {
            player.openGui(RFTools.instance, RFTools.GUI_RF_MONITOR, player.worldObj, x, y, z);
            return true;
        } else {
            findAdjacentBlocks(adjacentBlocks, world, x, y, z);
        }
        return true;
    }

    @Override
    public void onBlockPlacedBy(World world, int x, int y, int z, EntityLivingBase entityLivingBase, ItemStack itemStack) {
        int l = BlockTools.determineOrientation(world, x, y, z, entityLivingBase);
        int meta = world.getBlockMetadata(x, y, z);
        world.setBlockMetadataWithNotify(x, y, z, BlockTools.setOrientation(meta, l), 2);
    }

    @Override
    public void registerBlockIcons(IIconRegister iconRegister) {
        iconFront = iconRegister.registerIcon(RFTools.MODID + ":" + "machineFront");
        iconFront0 = iconRegister.registerIcon(RFTools.MODID + ":" + "machineFront_0");
        iconFront1 = iconRegister.registerIcon(RFTools.MODID + ":" + "machineFront_1");
        iconFront2 = iconRegister.registerIcon(RFTools.MODID + ":" + "machineFront_2");
        iconFront3 = iconRegister.registerIcon(RFTools.MODID + ":" + "machineFront_3");
        iconFront4 = iconRegister.registerIcon(RFTools.MODID + ":" + "machineFront_4");
        iconSide = iconRegister.registerIcon(RFTools.MODID + ":" + "machineSide");
    }

    private void findAdjacentBlocks(List<BlockInfo> adjacentBlocks, World world, int x, int y, int z) {
        for (int dy = -1 ; dy <= 1 ; dy++) {
            int yy = y + dy;
            if (yy >= 0 && yy < world.getActualHeight()) {
                for (int dz = -1 ; dz <= 1 ; dz++) {
                    int zz = z + dz;
                    for (int dx = -1 ; dx <= 1 ; dx++) {
                        int xx = x + dx;
                        if (dx != 0 || dy != 0 || dz != 0) {
                            Coordinate c = new Coordinate(xx, yy, zz);
                            TileEntity tileEntity = world.getTileEntity(xx, yy, zz);
                            if (tileEntity != null) {
                                if (tileEntity instanceof IEnergyHandler) {
                                    Block block = world.getBlock(xx, yy, zz);
                                    adjacentBlocks.add(new BlockInfo(tileEntity, block, world.getBlockMetadata(xx, yy, zz), c, false));
                                }
                            }
                        }
                    }
                }
            }
        }
    }


    public synchronized List<BlockInfo> getAdjacentBlocks() {
        return adjacentBlocks;
    }

    @Override
    public boolean canProvidePower() {
        return true;
    }

    @Override
    public int isProvidingWeakPower(IBlockAccess blockAccess, int x, int y, int z, int side) {
        int metadata = blockAccess.getBlockMetadata(x, y, z);
        return BlockTools.getRedstoneSignal(metadata) ? 15 : 0;
    }

    @Override
    public int isProvidingStrongPower(IBlockAccess blockAccess, int x, int y, int z, int side) {
        int metadata = blockAccess.getBlockMetadata(x, y, z);
        return BlockTools.getRedstoneSignal(metadata) ? 15 : 0;
    }

    @Override
    public IIcon getIcon(IBlockAccess blockAccess, int x, int y, int z, int side) {
        TileEntity tileEntity = blockAccess.getTileEntity(x, y, z);
        int meta = blockAccess.getBlockMetadata(x, y, z);
        int k = BlockTools.getOrientation(meta);
        if (side == k) {
            RFMonitorBlockTileEntity monitorBlockTileEntity = (RFMonitorBlockTileEntity) tileEntity;
            int rflevel = monitorBlockTileEntity.getRflevel();
            switch (rflevel) {
                case 1: return iconFront0;
                case 2: return iconFront1;
                case 3: return iconFront2;
                case 4: return iconFront3;
                case 5: return iconFront4;
                default: return iconFront;

            }
        } else {
            return iconSide;
        }
    }

    @Override
    public IIcon getIcon(int side, int meta) {
//        int k = getOrientation(meta);
        if (side == 3) {
            return iconFront;
        } else {
            return iconSide;
        }
    }
}
