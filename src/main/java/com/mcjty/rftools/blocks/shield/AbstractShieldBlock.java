package com.mcjty.rftools.blocks.shield;

import com.mcjty.rftools.RFTools;
import com.mcjty.rftools.blocks.shield.filters.PlayerFilter;
import com.mcjty.rftools.blocks.shield.filters.ShieldFilter;
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
import net.minecraftforge.common.util.ForgeDirection;

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
        if (entity instanceof EntityItem) {
            if ((meta & META_ITEMS) != 0) {
                super.addCollisionBoxesToList(world, x, y, z, mask, list, entity);
                return;
            }
        }
        if ((meta & META_PLAYERS) != 0) {
            if (entity instanceof EntityPlayer) {
                boolean blocked = checkPlayerCD(world, x, y, z, (EntityPlayer) entity);
                if (blocked) {
                    super.addCollisionBoxesToList(world, x, y, z, mask, list, entity);
                }
                return;
            }
        }
    }

    private boolean checkPlayerCD(World world, int x, int y, int z, EntityPlayer entity) {
        boolean blocked = false;
        ShieldBlockTileEntity shieldBlockTileEntity = (ShieldBlockTileEntity) world.getTileEntity(x, y, z);
        Coordinate shieldBlock = shieldBlockTileEntity.getShieldBlock();
        if (shieldBlock != null) {
            ShieldTileEntity shieldTileEntity = (ShieldTileEntity) world.getTileEntity(shieldBlock.getX(), shieldBlock.getY(), shieldBlock.getZ());
            if (shieldTileEntity != null) {
                List<ShieldFilter> filters = shieldTileEntity.getFilters();
                for (ShieldFilter filter : filters) {
                    if (filter.getAction() == ShieldFilter.ACTION_SOLID) {
                        if (PlayerFilter.PLAYER.equals(filter.getFilterName())) {
                            PlayerFilter playerFilter = (PlayerFilter) filter;
                            String name = playerFilter.getName();
                            if ((name == null || name.isEmpty())) {
                                blocked = true;
                                break;
                            } else if (name.equals(entity.getDisplayName())) {
                                blocked = true;
                                break;
                            }
                        }
                    }
                }
            }
        }
        return blocked;
    }

    @Override
    public void onEntityCollidedWithBlock(World world, int x, int y, int z, Entity entity) {
        if (entity instanceof EntityItem) {
            int meta = world.getBlockMetadata(x, y, z);
            if ((meta & META_ITEMS) == 0) {
                // Items should be able to pass through. We just move the entity to below this block.
                entity.setPosition(entity.posX, entity.posY-1, entity.posZ);
            }
            return;
        }

        // Possibly check for damage.
    }

    @Override
    public void registerBlockIcons(IIconRegister iconRegister) {
        icon = iconRegister.registerIcon(RFTools.MODID + ":" + "shieldtexture");
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
        ShieldBlockTileEntity shieldBlockTileEntity = (ShieldBlockTileEntity) blockAccess.getTileEntity(x, y, z);
        if (shieldBlockTileEntity == null) {
            return icon;
        }

        Block block = shieldBlockTileEntity.getBlock();
        if (block == null) {
            return icon;
        } else {
            return block.getIcon(blockAccess, x, y, z, side);
        }
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
