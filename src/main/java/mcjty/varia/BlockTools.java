package mcjty.varia;

import net.minecraft.block.Block;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

import java.util.Random;

import static net.minecraftforge.common.util.ForgeDirection.*;

public class BlockTools {
    private static final Random random = new Random();

    // Use these flags if you want to support a single redstone signal and 3 bits for orientation.
    public static final int MASK_ORIENTATION = 0x7;
    public static final int MASK_REDSTONE = 0x8;

    // Use these flags if you want to support both redstone in and output and only 2 bits for orientation.
    public static final int MASK_ORIENTATION_HORIZONTAL = 0x3;          // Only two bits for orientation
    public static final int MASK_REDSTONE_IN = 0x8;                     // Redstone in
    public static final int MASK_REDSTONE_OUT = 0x4;                    // Redstone out
    public static final int MASK_STATE = 0xc;                           // If redstone is not used: state

    public static ForgeDirection getOrientation(int metadata) {
        return ForgeDirection.getOrientation(metadata & MASK_ORIENTATION);
    }

    // Given the metavalue of a block, reorient the world direction to the internal block direction
    // so that the front side will be SOUTH.
    public static ForgeDirection reorient(ForgeDirection side, int meta) {
        return reorient(side, getOrientation(meta));
    }

    // Given the metavalue of a block, reorient the world direction to the internal block direction
    // so that the front side will be SOUTH.
    public static ForgeDirection reorientHoriz(ForgeDirection side, int meta) {
        return reorient(side, getOrientationHoriz(meta));
    }

    public static ForgeDirection reorient(ForgeDirection side, ForgeDirection blockDirection) {
        switch (blockDirection) {
            case DOWN:
                switch (side) {
                    case DOWN: return SOUTH;
                    case UP: return NORTH;
                    case NORTH: return UP;
                    case SOUTH: return DOWN;
                    case WEST: return EAST;
                    case EAST: return WEST;
                    case UNKNOWN: return side;
                }
            case UP:
                switch (side) {
                    case DOWN: return NORTH;
                    case UP: return SOUTH;
                    case NORTH: return UP;
                    case SOUTH: return DOWN;
                    case WEST: return WEST;
                    case EAST: return EAST;
                    case UNKNOWN: return side;
                }
            case NORTH:
                if (side == DOWN || side == UP) {
                    return side;
                }
                return side.getOpposite();
            case SOUTH:
                return side;
            case WEST:
                if (side == DOWN || side == UP) {
                    return side;
                } else if (side == WEST) {
                    return SOUTH;
                } else if (side == NORTH) {
                    return WEST;
                } else if (side == EAST) {
                    return NORTH;
                } else {
                    return EAST;
                }
            case EAST:
                if (side == DOWN || side == UP) {
                    return side;
                } else if (side == WEST) {
                    return NORTH;
                } else if (side == NORTH) {
                    return EAST;
                } else if (side == EAST) {
                    return SOUTH;
                } else {
                    return WEST;
                }
            case UNKNOWN:
                return side;
        }
        return side;
    }

    public static ForgeDirection getTopDirection(ForgeDirection direction) {
        switch(direction) {
            case DOWN:
                return ForgeDirection.SOUTH;
            case UP:
                return ForgeDirection.NORTH;
            default:
                return ForgeDirection.UP;
        }
    }

    public static ForgeDirection getBottomDirection(ForgeDirection direction) {
        switch(direction) {
            case DOWN:
                return ForgeDirection.NORTH;
            case UP:
                return ForgeDirection.SOUTH;
            default:
                return DOWN;
        }
    }

    public static int setOrientation(int metadata, ForgeDirection orientation) {
        return (metadata & ~MASK_ORIENTATION) | orientation.ordinal();
    }

    public static ForgeDirection getOrientationHoriz(int metadata) {
        return ForgeDirection.getOrientation((metadata & MASK_ORIENTATION_HORIZONTAL)+2);
    }

    public static int setOrientationHoriz(int metadata, ForgeDirection orientation) {
        return (metadata & ~MASK_ORIENTATION_HORIZONTAL) | (orientation.ordinal()-2);
    }

    public static boolean getRedstoneSignal(int metadata) {
        return (metadata & MASK_REDSTONE) != 0;
    }

    public static int setRedstoneSignal(int metadata, boolean signal) {
        if (signal) {
            return metadata | MASK_REDSTONE;
        } else {
            return metadata & ~MASK_REDSTONE;
        }
    }

    public static boolean getRedstoneSignalIn(int metadata) {
        return (metadata & MASK_REDSTONE_IN) != 0;
    }

    public static int setRedstoneSignalIn(int metadata, boolean signal) {
        if (signal) {
            return metadata | MASK_REDSTONE_IN;
        } else {
            return metadata & ~MASK_REDSTONE_IN;
        }
    }

    public static boolean getRedstoneSignalOut(int metadata) {
        return (metadata & MASK_REDSTONE_OUT) != 0;
    }

    public static int setRedstoneSignalOut(int metadata, boolean signal) {
        if (signal) {
            return metadata | MASK_REDSTONE_OUT;
        } else {
            return metadata & ~MASK_REDSTONE_OUT;
        }
    }

    public static int setState(int metadata, int value) {
        return (metadata & ~MASK_STATE) | (value << 2);
    }

    public static int getState(int metadata) {
        return (metadata & MASK_STATE) >> 2;
    }

    public static ForgeDirection determineOrientation(int x, int y, int z, EntityLivingBase entityLivingBase) {
        if (MathHelper.abs((float) entityLivingBase.posX - x) < 2.0F && MathHelper.abs((float)entityLivingBase.posZ - z) < 2.0F) {
            double d0 = entityLivingBase.posY + 1.82D - entityLivingBase.yOffset;

            if (d0 - y > 2.0D) {
                return ForgeDirection.UP;
            }

            if (y - d0 > 0.0D) {
                return DOWN;
            }
        }
        int l = MathHelper.floor_double((entityLivingBase.rotationYaw * 4.0F / 360.0F) + 0.5D) & 3;
        return l == 0 ? ForgeDirection.NORTH : (l == 1 ? ForgeDirection.EAST : (l == 2 ? ForgeDirection.SOUTH : (l == 3 ? ForgeDirection.WEST : DOWN)));
    }

    public static ForgeDirection determineOrientationHoriz(EntityLivingBase entityLivingBase) {
        int l = MathHelper.floor_double((entityLivingBase.rotationYaw * 4.0F / 360.0F) + 0.5D) & 3;
        return l == 0 ? ForgeDirection.NORTH : (l == 1 ? ForgeDirection.EAST : (l == 2 ? ForgeDirection.SOUTH : (l == 3 ? ForgeDirection.WEST : DOWN)));
    }


    public static void emptyInventoryInWorld(World world, int x, int y, int z, Block block, IInventory inventory) {
        for (int i = 0; i < inventory.getSizeInventory(); ++i) {
            ItemStack itemstack = inventory.getStackInSlot(i);
            spawnItemStack(world, x, y, z, itemstack);
            inventory.setInventorySlotContents(i, null);
        }

        world.func_147453_f(x, y, z, block);
    }

    public static void spawnItemStack(World world, int x, int y, int z, ItemStack itemstack) {
        if (itemstack != null) {
            float f = random.nextFloat() * 0.8F + 0.1F;
            float f1 = random.nextFloat() * 0.8F + 0.1F;
            EntityItem entityitem;

            float f2 = random.nextFloat() * 0.8F + 0.1F;
            while (itemstack.stackSize > 0) {
                int j = random.nextInt(21) + 10;

                if (j > itemstack.stackSize) {
                    j = itemstack.stackSize;
                }

                itemstack.stackSize -= j;
                entityitem = new EntityItem(world, (x + f), (y + f1), (z + f2), new ItemStack(itemstack.getItem(), j, itemstack.getItemDamage()));
                float f3 = 0.05F;
                entityitem.motionX = ((float)random.nextGaussian() * f3);
                entityitem.motionY = ((float)random.nextGaussian() * f3 + 0.2F);
                entityitem.motionZ = ((float)random.nextGaussian() * f3);

                if (itemstack.hasTagCompound()) {
                    entityitem.getEntityItem().setTagCompound((NBTTagCompound)itemstack.getTagCompound().copy());
                }
                world.spawnEntityInWorld(entityitem);
            }
        }
    }

    public static Block getBlock(ItemStack stack) {
        if (stack.getItem() instanceof ItemBlock) {
            return ((ItemBlock) stack.getItem()).field_150939_a;
        } else {
            return null;
        }
    }
}
