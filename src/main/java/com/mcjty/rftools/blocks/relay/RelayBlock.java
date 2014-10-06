package com.mcjty.rftools.blocks.relay;

import com.mcjty.container.GenericBlock;
import com.mcjty.rftools.RFTools;
import com.mcjty.rftools.blocks.BlockTools;
import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
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
    public String getFrontIconName() {
        return "machineRelay_on";
    }

    @Override
    public void onNeighborBlockChange(World world, int x, int y, int z, Block block) {
        int meta = world.getBlockMetadata(x, y, z);
        boolean powered = world.isBlockIndirectlyGettingPowered(x, y, z);
        meta = BlockTools.setRedstoneSignal(meta, powered);
        world.setBlockMetadataWithNotify(x, y, z, meta, 2);
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
                return iconFront;
            } else {
                return iconFront_off;
            }
        } else {
            return iconSide;
        }
    }
}
