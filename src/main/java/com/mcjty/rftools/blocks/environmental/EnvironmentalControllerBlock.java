package com.mcjty.rftools.blocks.environmental;

import com.mcjty.container.GenericContainerBlock;
import com.mcjty.rftools.RFTools;
import com.mcjty.rftools.blocks.Infusable;
import net.minecraft.block.material.Material;
import net.minecraft.world.IBlockAccess;

public class EnvironmentalControllerBlock extends GenericContainerBlock implements Infusable {

    public EnvironmentalControllerBlock() {
        super(Material.iron, EnvironmentalControllerTileEntity.class);
        setBlockName("environmentalControllerBlock");
        setCreativeTab(RFTools.tabRfTools);
    }

    @Override
    public int getLightValue(IBlockAccess world, int x, int y, int z) {
        return 13;
//        int meta = world.getBlockMetadata(x, y, z);
//        int state = BlockTools.getState(meta);
//        if (state == 0) {
//            return 10;
//        } else {
//            return getLightValue();
//        }
    }

    @Override
    public String getIdentifyingIconName() {
        return "machineEnvironmentalController";
    }

    @Override
    public int getGuiID() {
        return -1;
    }
}
