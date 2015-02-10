package com.mcjty.rftools.blocks.shield;

import com.mcjty.rftools.blocks.ModBlocks;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.common.util.ForgeDirection;

public class SolidShieldBlock extends AbstractShieldBlock {

    public SolidShieldBlock() {
        super();
        setBlockName("solidShieldBlock");
    }

//    @Override
//    public boolean isOpaqueCube() {
//        return false;
//    }
//
//    @Override
//    public boolean renderAsNormalBlock() {
//        return false;
//    }
//

    @SideOnly(Side.CLIENT)
    @Override
    public int getRenderBlockPass() {
        return 1;
    }

    @Override
    public boolean shouldSideBeRendered(IBlockAccess world, int x, int y, int z, int side) {
//        int thisx = x - ForgeDirection.values()[side].offsetX;
//        int thisy = y - ForgeDirection.values()[side].offsetY;
//        int thisz = z - ForgeDirection.values()[side].offsetZ;
        if (world.getBlock(x, y, z) == ModBlocks.solidShieldBlock) {
            return false;
        }
        return true;
    }


}
