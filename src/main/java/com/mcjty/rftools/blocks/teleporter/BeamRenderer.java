package com.mcjty.rftools.blocks.teleporter;

import com.mcjty.rftools.Constants;
import com.mcjty.rftools.blocks.ModBlocks;
import com.mcjty.rftools.render.DefaultISBRH;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.common.util.ForgeDirection;

public class BeamRenderer extends DefaultISBRH {

    @Override
    public boolean renderWorldBlock(IBlockAccess world, int x, int y, int z, Block block, int modelId, RenderBlocks renderer) {
        Tessellator tessellator = Tessellator.instance;
        tessellator.setColorOpaque(255, 255, 255);

        if (MatterTransmitterBlock.currentPass == 0) {
            renderer.renderStandardBlock(block, x, y, z);
        } else if (MatterTransmitterBlock.currentPass == 1) {
            MatterTransmitterTileEntity matterTransmitterTileEntity = (MatterTransmitterTileEntity) world.getTileEntity(x, y, z);
            if (matterTransmitterTileEntity.isDialed()) {
                tessellator.addTranslation(x, y+1, z);
                tessellator.setBrightness(190);

                int status = matterTransmitterTileEntity.getStatus();
                IIcon beamIcon = null;
                switch (status) {
                    case MatterTransmitterTileEntity.STATUS_OK: beamIcon = ModBlocks.matterTransmitterBlock.iconBeam; break;
                    case MatterTransmitterTileEntity.STATUS_WARN: beamIcon = ModBlocks.matterTransmitterBlock.iconWarn; break;
                    default: beamIcon = ModBlocks.matterTransmitterBlock.iconUnknown; break;
                }
                addSideHeightWithRotation(tessellator, Constants.SIDE_NORTH, 4, ForgeDirection.SOUTH, beamIcon);
                addSideHeightWithRotation(tessellator, Constants.SIDE_SOUTH, 4, ForgeDirection.NORTH, beamIcon);
                addSideHeightWithRotation(tessellator, Constants.SIDE_WEST, 4, ForgeDirection.NORTH, beamIcon);
                addSideHeightWithRotation(tessellator, Constants.SIDE_EAST, 4, ForgeDirection.NORTH, beamIcon);

                tessellator.addTranslation(-x, -y-1, -z);
            }
            return false;
        }

        return true;
    }

    @Override
    public int getRenderId() {
        return MatterTransmitterBlock.RENDERID_BEAM;
    }

    @Override
    public boolean shouldRender3DInInventory(int modelId) {
        return true;
    }
}
