package com.mcjty.rftools.blocks.shards;

import com.mcjty.rftools.RFTools;
import com.mcjty.rftools.blocks.BlockTools;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

public abstract class AbstractDirectionalBlock extends Block {

    private IIcon iconSideHoriz;
    private IIcon iconSideVert;
    private IIcon iconTop;

    public AbstractDirectionalBlock() {
        super(Material.rock);
        setCreativeTab(RFTools.tabRfTools);
    }

    @Override
    public int getLightValue() {
        return 6;
    }

    protected abstract String getHorizTexture();
    protected abstract String getVertTexture();
    protected abstract String getTopBottomTexture();

    @Override
    public void onBlockPlacedBy(World world, int x, int y, int z, EntityLivingBase entityLivingBase, ItemStack itemStack) {
        ForgeDirection dir = BlockTools.determineOrientation(x, y, z, entityLivingBase);
        int meta = world.getBlockMetadata(x, y, z);
        world.setBlockMetadataWithNotify(x, y, z, BlockTools.setOrientation(meta, dir), 2);
    }

    @Override
    public void registerBlockIcons(IIconRegister iconRegister) {
        iconSideVert = iconRegister.registerIcon(RFTools.MODID + ":" + getVertTexture());
        iconSideHoriz = iconRegister.registerIcon(RFTools.MODID + ":" + getHorizTexture());
        iconTop = iconRegister.registerIcon(RFTools.MODID + ":" + getTopBottomTexture());
    }

    @Override
    public IIcon getIcon(IBlockAccess world, int x, int y, int z, int side) {
        int meta = world.getBlockMetadata(x, y, z);
        ForgeDirection k = BlockTools.getOrientation(meta);

        if (side == k.ordinal() || side == k.getOpposite().ordinal()) {
            return iconTop;
        } else {
            if (k == ForgeDirection.DOWN || k == ForgeDirection.UP) {
                return iconSideVert;
            } else if (k == ForgeDirection.SOUTH || k == ForgeDirection.NORTH) {
                if (side == ForgeDirection.DOWN.ordinal() || side == ForgeDirection.UP.ordinal()) {
                    return iconSideVert;
                } else {
                    return iconSideHoriz;
                }
            } else {
                return iconSideHoriz;
            }
        }
    }

    @Override
    public IIcon getIcon(int side, int meta) {
        if (side == ForgeDirection.UP.ordinal() || side == ForgeDirection.DOWN.ordinal()) {
            return iconTop;
        } else {
            return iconSideVert;
        }
    }
}
