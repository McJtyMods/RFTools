package com.mcjty.rftools.blocks;

import com.mcjty.rftools.blocks.crafter.CrafterBlockTileEntity;
import net.minecraft.block.Block;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;

import java.util.Random;

public class BlockTools {
    private static final Random random = new Random();

    public static final int MASK_ORIENTATION = 0x7;
    public static final int MASK_REDSTONE = 0x8;

    public static int getOrientation(int metadata) {
        return metadata & MASK_ORIENTATION;
    }

    public static int setOrientation(int metadata, int orientation) {
        return (metadata & ~MASK_ORIENTATION) | orientation;
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

    public static int determineOrientation(World world, int x, int y, int z, EntityLivingBase entityLivingBase) {
        if (MathHelper.abs((float) entityLivingBase.posX - x) < 2.0F && MathHelper.abs((float)entityLivingBase.posZ - z) < 2.0F) {
            double d0 = entityLivingBase.posY + 1.82D - entityLivingBase.yOffset;

            if (d0 - y > 2.0D) {
                return 1;
            }

            if (y - d0 > 0.0D) {
                return 0;
            }
        }
        int l = MathHelper.floor_double((entityLivingBase.rotationYaw * 4.0F / 360.0F) + 0.5D) & 3;
        return l == 0 ? 2 : (l == 1 ? 5 : (l == 2 ? 3 : (l == 3 ? 4 : 0)));
    }


    public static void emptyInventoryInWorld(World world, int x, int y, int z, Block block, IInventory inventory) {
        for (int i = 0; i < inventory.getSizeInventory(); ++i) {
            ItemStack itemstack = inventory.getStackInSlot(i);

            if (itemstack != null) {
                float f = random.nextFloat() * 0.8F + 0.1F;
                float f1 = random.nextFloat() * 0.8F + 0.1F;
                EntityItem entityitem;

                for (float f2 = random.nextFloat() * 0.8F + 0.1F; itemstack.stackSize > 0; world.spawnEntityInWorld(entityitem)) {
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
                }
            }
        }

        world.func_147453_f(x, y, z, block);
    }
}
