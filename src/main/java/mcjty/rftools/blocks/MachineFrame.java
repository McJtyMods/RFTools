package mcjty.rftools.blocks;

import mcjty.lib.McJtyRegister;
import mcjty.rftools.RFTools;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.client.model.ModelLoader;



public class MachineFrame extends Block {
    public MachineFrame() {
        super(Material.IRON);
        setUnlocalizedName("rftools.machine_frame");
        setRegistryName("machine_frame");
        setCreativeTab(RFTools.setup.getTab());
        McJtyRegister.registerLater(this, RFTools.instance, ItemBlock::new);
    }

    @SideOnly(Side.CLIENT)
    public void initModel() {
        ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(this), 0, new ModelResourceLocation(getRegistryName(), "inventory"));
    }

    @Override
    public boolean canPlaceBlockAt(World worldIn, BlockPos pos) {
        return false;
    }
}
