package com.mcjty.rftools.blocks.shield;

import com.mcjty.rftools.RFTools;
import com.mcjty.varia.Coordinate;
import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.monster.IMob;
import net.minecraft.entity.passive.IAnimals;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class AbstractShieldBlock extends Block implements ITileEntityProvider {

    protected IIcon icon;

    public static final int META_ITEMS = 1;             // If set then blocked for items
    public static final int META_PASSIVE = 2;           // If set the blocked for passive mobs
    public static final int META_HOSTILE = 4;           // If set the blocked for hostile mobs
    public static final int META_PLAYERS = 8;           // If set the blocked for (some) players


    public AbstractShieldBlock(Material material) {
        super(material);
        setBlockUnbreakable();
    }

    @Override
    public boolean isOpaqueCube() {
        return false;
    }

//    @Override
//    public AxisAlignedBB getCollisionBoundingBoxFromPool(World world, int x, int y, int z) {
//        return null;
//    }
//
    @Override
    public void addCollisionBoxesToList(World world, int x, int y, int z, AxisAlignedBB mask, List list, Entity entity) {
        int meta = world.getBlockMetadata(x, y, z);
        if (meta == 0) {
            // No collision for anything.
            return;
        }
        if ((meta & META_HOSTILE) != 0) {
            if (entity instanceof IMob) {
                super.addCollisionBoxesToList(world, x, y, z, mask, list, entity);
                return;
            }
        }
        if ((meta & META_PASSIVE) != 0) {
            if (entity instanceof IAnimals && !(entity instanceof IMob)) {
                super.addCollisionBoxesToList(world, x, y, z, mask, list, entity);
                return;
            }
        }
        if ((meta & META_ITEMS) != 0) {
            if (entity instanceof EntityItem) {
                super.addCollisionBoxesToList(world, x, y, z, mask, list, entity);
                return;
            }
        }
        if ((meta & META_PLAYERS) != 0) {
            // @todo check TE for more detailed data.
            if (entity instanceof EntityPlayer) {
                super.addCollisionBoxesToList(world, x, y, z, mask, list, entity);
                return;
            }
        }
    }

    @Override
    public void onEntityCollidedWithBlock(World world, int x, int y, int z, Entity entity) {
        super.onEntityCollidedWithBlock(world, x, y, z, entity);
    }

    @Override
    public void registerBlockIcons(IIconRegister iconRegister) {
        icon = iconRegister.registerIcon(RFTools.MODID + ":" + "shieldtexture");
    }

    @Override
    public IIcon getIcon(IBlockAccess blockAccess, int x, int y, int z, int side) {
        return icon;
    }

    @Override
    public IIcon getIcon(int side, int meta) {
        return icon;
    }


    @Override
    public TileEntity createNewTileEntity(World world, int metadata) {
        return new ShieldBlockTileEntity();
    }
}
