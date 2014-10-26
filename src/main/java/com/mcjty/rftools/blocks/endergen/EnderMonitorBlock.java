package com.mcjty.rftools.blocks.endergen;

import com.mcjty.rftools.RFTools;
import com.mcjty.rftools.blocks.logic.LogicSlabBlock;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public class EnderMonitorBlock extends LogicSlabBlock {

    public EnderMonitorBlock(Material material) {
        super(material, "enderMonitorBlock", EnderMonitorTileEntity.class);
    }

    @Override
    public int getGuiID() {
        return RFTools.GUI_ENDERMONITOR;
    }

    @Override
    public String getIdentifyingIconName() {
        return "machineEnderMonitorTop";
    }

    @Override
    public void onNeighborBlockChange(World world, int x, int y, int z, Block block) {
        // We don't want to do what LogicSlabBlock does as we don't react on redstone input.
    }
}
