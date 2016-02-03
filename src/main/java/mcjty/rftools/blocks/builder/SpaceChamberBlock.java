package mcjty.rftools.blocks.builder;

import mcjty.rftools.RFTools;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraft.util.EnumWorldBlockLayer;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class SpaceChamberBlock extends Block {

    public SpaceChamberBlock() {
        super(Material.iron);
        setHardness(2.0f);
        setStepSound(soundTypeMetal);
        setHarvestLevel("pickaxe", 0);
        setUnlocalizedName("space_chamber");
        setRegistryName("space_chamber");
        setCreativeTab(RFTools.tabRfTools);
        GameRegistry.registerBlock(this);
    }

    @SideOnly(Side.CLIENT)
    public void initModel() {
        ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(this), 0, new ModelResourceLocation(getRegistryName(), "inventory"));
    }

    @Override
    public boolean isBlockNormalCube() {
        return false;
    }

    @Override
    public boolean isOpaqueCube() {
        return false;
    }

    @Override
    public EnumWorldBlockLayer getBlockLayer() {
        return EnumWorldBlockLayer.CUTOUT;
    }
}
