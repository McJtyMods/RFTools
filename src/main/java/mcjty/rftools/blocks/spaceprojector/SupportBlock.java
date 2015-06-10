package mcjty.rftools.blocks.spaceprojector;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import mcjty.rftools.RFTools;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.init.Blocks;
import net.minecraft.util.Facing;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;

import java.util.Random;

public class SupportBlock extends Block {

    private IIcon icon;
    private IIcon iconRed;
    private IIcon iconYellow;

    public static final int STATUS_OK = 0;
    public static final int STATUS_WARN = 1;
    public static final int STATUS_ERROR = 2;

    public SupportBlock() {
        super(Material.glass);
        setBlockName("supportBlock");
        setCreativeTab(RFTools.tabRfTools);
    }

    @Override
    public boolean isOpaqueCube() {
        return false;
    }

    @Override
    public int getRenderBlockPass() {
        return 1;
    }

    @Override
    public boolean renderAsNormalBlock() {
        return false;
    }

    @Override
    public int quantityDropped(Random random) {
        return 0;
    }

    @Override
    public void registerBlockIcons(IIconRegister iconRegister) {
        icon = iconRegister.registerIcon(RFTools.MODID + ":" + "supportBlock");
        iconRed = iconRegister.registerIcon(RFTools.MODID + ":" + "supportRedBlock");
        iconYellow = iconRegister.registerIcon(RFTools.MODID + ":" + "supportYellowBlock");
    }

    @Override
    public IIcon getIcon(int side, int meta) {
        if (meta == STATUS_ERROR) {
            return iconRed;
        } else if (meta == STATUS_WARN) {
            return iconYellow;
        } else {
            return icon;
        }
    }

    /**
     * Returns true if the given side of this block type should be rendered, if the adjacent block is at the given
     * coordinates.  Args: blockAccess, x, y, z, side
     */
    @Override
    @SideOnly(Side.CLIENT)
    public boolean shouldSideBeRendered(IBlockAccess blockAccess, int x, int y, int z, int side) {
        Block block = blockAccess.getBlock(x, y, z);
        if (blockAccess.getBlockMetadata(x, y, z) != blockAccess.getBlockMetadata(x - Facing.offsetsXForSide[side], y - Facing.offsetsYForSide[side], z - Facing.offsetsZForSide[side])) {
            return true;
        }

        if (block == this) {
            return false;
        }

        return block != this && super.shouldSideBeRendered(blockAccess, x, y, z, side);
    }


}
