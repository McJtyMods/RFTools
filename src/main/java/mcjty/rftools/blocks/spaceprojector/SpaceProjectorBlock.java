package mcjty.rftools.blocks.spaceprojector;

import mcjty.container.GenericContainerBlock;
import mcjty.rftools.RFTools;
import net.minecraft.block.material.Material;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public class SpaceProjectorBlock extends GenericContainerBlock {

    public SpaceProjectorBlock() {
        super(Material.iron, SpaceProjectorTileEntity.class);
        setBlockName("spaceProjectorBlock");
        setCreativeTab(RFTools.tabRfTools);
    }

    @Override
    public int getGuiID() {
        return -1;
    }

    @Override
    public void onBlockPlacedBy(World world, int x, int y, int z, EntityLivingBase entityLivingBase, ItemStack itemStack) {
        super.onBlockPlacedBy(world, x, y, z, entityLivingBase, itemStack);
        if (!world.isRemote) {
            SpaceProjectorTileEntity spaceProjectorTileEntity = (SpaceProjectorTileEntity) world.getTileEntity(x, y, z);
            RFTools.message((EntityPlayer) entityLivingBase, "Start projecting...");
            spaceProjectorTileEntity.project();
        }
    }
}
