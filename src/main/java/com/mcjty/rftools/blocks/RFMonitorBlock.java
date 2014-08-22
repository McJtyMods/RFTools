package com.mcjty.rftools.blocks;

import cofh.api.energy.IEnergyHandler;
import com.mcjty.rftools.BlockInfo;
import com.mcjty.rftools.Coordinate;
import com.mcjty.rftools.RFTools;
import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

public class RFMonitorBlock extends Block implements ITileEntityProvider {

    private List<BlockInfo> adjacentBlocks = new ArrayList<BlockInfo>();

    private IIcon iconFront;
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
        if (world.isRemote) {
            player.openGui(RFTools.instance, RFTools.GUI_RF_MONITOR, player.worldObj, x, y, z);
            return true;
        } else {
            adjacentBlocks.clear();
            findAdjacentBlocks(adjacentBlocks, world, x, y, z);
        }
        return false;
    }

    @Override
    public void registerBlockIcons(IIconRegister iconRegister) {
        iconFront = iconRegister.registerIcon(RFTools.MODID + ":" + "machineFront");
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


    synchronized public List<BlockInfo> getAdjacentBlocks() {
        return adjacentBlocks;
    }


    @Override
    public IIcon getIcon(int side, int meta) {
        if (side == 2) {
            return iconFront;
        } else {
            return iconSide;
        }
    }
}
