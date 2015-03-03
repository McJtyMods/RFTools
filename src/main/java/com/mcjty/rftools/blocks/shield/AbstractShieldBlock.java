package com.mcjty.rftools.blocks.shield;

import com.mcjty.rftools.RFTools;
import com.mcjty.rftools.blocks.shield.filters.*;
import com.mcjty.varia.Coordinate;
import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.IMob;
import net.minecraft.entity.passive.IAnimals;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.IIcon;
import net.minecraft.world.Explosion;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

import java.util.List;
import java.util.Random;

public class AbstractShieldBlock extends Block implements ITileEntityProvider {

    protected IIcon icon;
    protected IIcon[] icons = new IIcon[4];

    public static final int META_ITEMS = 1;             // If set then blocked for items
    public static final int META_PASSIVE = 2;           // If set the blocked for passive mobs
    public static final int META_HOSTILE = 4;           // If set the blocked for hostile mobs
    public static final int META_PLAYERS = 8;           // If set the blocked for (some) players


    public AbstractShieldBlock() {
        super(Material.portal);
        setBlockUnbreakable();
        setResistance(6000000.0F);
        setCreativeTab(RFTools.tabRfTools);
    }

    public IIcon[] getIcons() {
        return icons;
    }

    @Override
    public boolean canEntityDestroy(IBlockAccess world, int x, int y, int z, Entity entity) {
        return false;
    }

    @Override
    public void onBlockExploded(World world, int x, int y, int z, Explosion explosion) {
    }

    @Override
    public int quantityDropped(Random random) {
        return 0;
    }

    @Override
    public int getMobilityFlag() {
        return 2;
    }

    @Override
    public void addCollisionBoxesToList(World world, int x, int y, int z, AxisAlignedBB mask, List list, Entity entity) {
        ShieldBlockTileEntity shieldBlockTileEntity = (ShieldBlockTileEntity) world.getTileEntity(x, y, z);
        int cdData = shieldBlockTileEntity.getCollisionData();

        if (cdData == 0) {
            // No collision for anything.
            return;
        }
        if ((cdData & META_HOSTILE) != 0) {
            if (entity instanceof IMob) {
                if (checkEntityCD(world, x, y, z, HostileFilter.HOSTILE)) {
                    super.addCollisionBoxesToList(world, x, y, z, mask, list, entity);
                }
                return;
            }
        }
        if ((cdData & META_PASSIVE) != 0) {
            if (entity instanceof IAnimals && !(entity instanceof IMob)) {
                if (checkEntityCD(world, x, y, z, AnimalFilter.ANIMAL)) {
                    super.addCollisionBoxesToList(world, x, y, z, mask, list, entity);
                }
                return;
            }
        }
        if ((cdData & META_PLAYERS) != 0) {
            if (entity instanceof EntityPlayer) {
                if (checkPlayerCD(world, x, y, z, (EntityPlayer) entity)) {
                    super.addCollisionBoxesToList(world, x, y, z, mask, list, entity);
                }
            }
        }
        if ((cdData & META_ITEMS) != 0) {
            if (!(entity instanceof EntityLivingBase)) {
                if (checkEntityCD(world, x, y, z, ItemFilter.ITEM)) {
                    super.addCollisionBoxesToList(world, x, y, z, mask, list, entity);
                }
                return;
            }
        }
    }

    private boolean checkEntityCD(World world, int x, int y, int z, String filterName) {
        ShieldBlockTileEntity shieldBlockTileEntity = (ShieldBlockTileEntity) world.getTileEntity(x, y, z);
        Coordinate shieldBlock = shieldBlockTileEntity.getShieldBlock();
        if (shieldBlock != null) {
            ShieldTEBase shieldTileEntity = (ShieldTEBase) world.getTileEntity(shieldBlock.getX(), shieldBlock.getY(), shieldBlock.getZ());
            if (shieldTileEntity != null) {
                List<ShieldFilter> filters = shieldTileEntity.getFilters();
                for (ShieldFilter filter : filters) {
                    if (DefaultFilter.DEFAULT.equals(filter.getFilterName())) {
                        return (filter.getAction() & ShieldFilter.ACTION_SOLID) != 0;
                    } else if (filterName.equals(filter.getFilterName())) {
                        return (filter.getAction() & ShieldFilter.ACTION_SOLID) != 0;
                    }
                }
            }
        }
        return false;
    }


    private boolean checkPlayerCD(World world, int x, int y, int z, EntityPlayer entity) {
        ShieldBlockTileEntity shieldBlockTileEntity = (ShieldBlockTileEntity) world.getTileEntity(x, y, z);
        Coordinate shieldBlock = shieldBlockTileEntity.getShieldBlock();
        if (shieldBlock != null) {
            ShieldTEBase shieldTileEntity = (ShieldTEBase) world.getTileEntity(shieldBlock.getX(), shieldBlock.getY(), shieldBlock.getZ());
            if (shieldTileEntity != null) {
                List<ShieldFilter> filters = shieldTileEntity.getFilters();
                for (ShieldFilter filter : filters) {
                    if (DefaultFilter.DEFAULT.equals(filter.getFilterName())) {
                        return (filter.getAction() & ShieldFilter.ACTION_SOLID) != 0;
                    } else if (PlayerFilter.PLAYER.equals(filter.getFilterName())) {
                        PlayerFilter playerFilter = (PlayerFilter) filter;
                        String name = playerFilter.getName();
                        if ((name == null || name.isEmpty())) {
                            return (filter.getAction() & ShieldFilter.ACTION_SOLID) != 0;
                        } else if (name.equals(entity.getDisplayName())) {
                            return (filter.getAction() & ShieldFilter.ACTION_SOLID) != 0;
                        }
                    }
                }
            }
        }
        return false;
    }

    @Override
    public void onEntityCollidedWithBlock(World world, int x, int y, int z, Entity entity) {
        if (!(entity instanceof EntityLivingBase)) {
            ShieldBlockTileEntity shieldBlockTileEntity = (ShieldBlockTileEntity) world.getTileEntity(x, y, z);
            int cdData = shieldBlockTileEntity.getCollisionData();
            if ((cdData & META_ITEMS) == 0) {
                // Items should be able to pass through. We just move the entity to below this block.
                entity.setPosition(entity.posX, entity.posY-1, entity.posZ);
            }
        }

        // Possibly check for damage.
    }

    @Override
    public void registerBlockIcons(IIconRegister iconRegister) {
        icon = iconRegister.registerIcon(RFTools.MODID + ":shieldtexture");
        icons[0] = iconRegister.registerIcon(RFTools.MODID + ":shield/shield0");
        icons[1] = iconRegister.registerIcon(RFTools.MODID + ":shield/shield1");
        icons[2] = iconRegister.registerIcon(RFTools.MODID + ":shield/shield2");
        icons[3] = iconRegister.registerIcon(RFTools.MODID + ":shield/shield3");
    }

    // Subclasses can call this to override the slightly more expensive version in this class.
    protected boolean blockShouldSideBeRendered(IBlockAccess world, int x, int y, int z, int side) {
        return super.shouldSideBeRendered(world, x, y, z, side);
    }

    @Override
    public boolean shouldSideBeRendered(IBlockAccess world, int x, int y, int z, int side) {
        int thisx = x - ForgeDirection.values()[side].offsetX;
        int thisy = y - ForgeDirection.values()[side].offsetY;
        int thisz = z - ForgeDirection.values()[side].offsetZ;

        ShieldBlockTileEntity shieldBlockTileEntity = (ShieldBlockTileEntity) world.getTileEntity(thisx, thisy, thisz);
        if (shieldBlockTileEntity == null) {
            return super.shouldSideBeRendered(world, x, y, z, side);
        }
        Block block = shieldBlockTileEntity.getBlock();
        if (block == null) {
            return super.shouldSideBeRendered(world, x, y, z, side);
        } else {
            return block.shouldSideBeRendered(world, x, y, z, side);
        }
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
