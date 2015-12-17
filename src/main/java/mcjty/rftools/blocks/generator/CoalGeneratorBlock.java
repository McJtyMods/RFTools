package mcjty.rftools.blocks.generator;


import mcjty.rftools.RFTools;
import mcjty.rftools.blocks.GenericRFToolsBlock;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class CoalGeneratorBlock extends GenericRFToolsBlock<CoalGeneratorTileEntity> {

    public CoalGeneratorBlock() {
        super(Material.iron, CoalGeneratorTileEntity.class, "coalgenerator", true);
    }

    @SideOnly(Side.CLIENT)
    public void initModel() {
        Minecraft.getMinecraft().getRenderItem().getItemModelMesher()
                .register(Item.getItemFromBlock(this), 0, new ModelResourceLocation(RFTools.MODID + ":" + getUnlocalizedName().substring(5), "inventory"));
    }

    @Override
    public int getGuiID() {
        return 0;
    }
}
