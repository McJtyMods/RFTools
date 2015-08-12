package mcjty.rftools.blocks.shield;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import mcjty.rftools.RFTools;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlockWithMetadata;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;

import java.util.List;

public class ShieldTemplateBlock extends Block {

    private IIcon icons[] = new IIcon[4];

    public ShieldTemplateBlock() {
        super(Material.glass);
        setBlockName("shieldTemplateBlock");
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
    public int damageDropped(int meta) {
        return meta;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void getSubBlocks(Item item, CreativeTabs creativeTabs, List list) {
        for (int i = 0; i < 4; ++i) {
            list.add(new ItemStack(item, 1, i));
        }
    }

    @Override
    public void registerBlockIcons(IIconRegister iconRegister) {
        icons[0] = iconRegister.registerIcon(RFTools.MODID + ":" + "shieldTemplate");
        icons[1] = iconRegister.registerIcon(RFTools.MODID + ":" + "shieldTemplate1");
        icons[2] = iconRegister.registerIcon(RFTools.MODID + ":" + "shieldTemplate2");
        icons[3] = iconRegister.registerIcon(RFTools.MODID + ":" + "shieldTemplate3");
    }

    @Override
    public IIcon getIcon(IBlockAccess blockAccess, int x, int y, int z, int side) {
        int meta = blockAccess.getBlockMetadata(x, y, z);
        return icons[meta & 3];
    }

    @Override
    public IIcon getIcon(int side, int meta) {
        return icons[meta & 3];
    }

    public static class ShieldTemplateItemBlock extends ItemBlockWithMetadata{

        public ShieldTemplateItemBlock(Block block) {
            super(block, block);
        }
    }
}
