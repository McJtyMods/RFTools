package mcjty.rftools.items;

import mcjty.lib.McJtyRegister;
import mcjty.rftools.RFTools;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class GenericRFToolsItem extends Item {

    public GenericRFToolsItem(String name) {
        setUnlocalizedName(name);
        setRegistryName(name);
        setCreativeTab(RFTools.tabRfTools);
        McJtyRegister.registerLater(this, RFTools.instance);
    }

    @SideOnly(Side.CLIENT)
    public void initModel() {
        ModelLoader.setCustomModelResourceLocation(this, 0, new ModelResourceLocation(getRegistryName(), "inventory"));
    }
}
