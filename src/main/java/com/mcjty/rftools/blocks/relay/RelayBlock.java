package com.mcjty.rftools.blocks.relay;

import com.mcjty.container.EmptyContainer;
import com.mcjty.container.GenericBlock;
import com.mcjty.rftools.RFTools;
import com.mcjty.rftools.blocks.BlockTools;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

public class RelayBlock extends GenericBlock {

    private IIcon iconFront_off;

    public RelayBlock(Material material) {
        super(material, RelayTileEntity.class);
        setBlockName("relayBlock");
    }

    @Override
    public int getGuiID() {
        return RFTools.GUI_RELAY;
    }

    @Override
    public GuiContainer createClientGui(EntityPlayer entityPlayer, TileEntity tileEntity) {
        RelayTileEntity relayTileEntity = (RelayTileEntity) tileEntity;
        return new GuiRelay(relayTileEntity, new EmptyContainer(entityPlayer));
    }

    @Override
    public String getIdentifyingIconName() {
        return "machineRelay_on";
    }

    @Override
    public void onNeighborBlockChange(World world, int x, int y, int z, Block block) {
        checkRedstone(world, x, y, z);
    }

    @Override
    public void registerBlockIcons(IIconRegister iconRegister) {
        super.registerBlockIcons(iconRegister);
        iconFront_off = iconRegister.registerIcon(RFTools.MODID + ":" + "machineRelay");
    }

    @Override
    public IIcon getIcon(IBlockAccess blockAccess, int x, int y, int z, int side) {
        int meta = blockAccess.getBlockMetadata(x, y, z);
        ForgeDirection k = BlockTools.getOrientation(meta);
        if (side == k.ordinal()) {
            boolean rs = BlockTools.getRedstoneSignal(meta);
            if (rs) {
                return iconInd;
            } else {
                return iconFront_off;
            }
        } else {
            return iconSide;
        }
    }
}
