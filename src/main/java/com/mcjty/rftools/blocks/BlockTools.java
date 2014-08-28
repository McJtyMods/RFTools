package com.mcjty.rftools.blocks;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;

public class BlockTools {
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



}
