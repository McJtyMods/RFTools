package mcjty.rftools.blocks.spaceprojector;

import mcjty.rftools.RFTools;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;

import java.util.Random;

public class SupportBlock extends Block {

    private IIcon icon;
    private IIcon iconRed;

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
    public int quantityDropped(Random random) {
        return 0;
    }

    @Override
    public void registerBlockIcons(IIconRegister iconRegister) {
        icon = iconRegister.registerIcon(RFTools.MODID + ":" + "supportBlock");
        iconRed = iconRegister.registerIcon(RFTools.MODID + ":" + "supportRedBlock");
    }

    @Override
    public IIcon getIcon(int side, int meta) {
        return meta == 1 ? iconRed : icon;
    }
}
