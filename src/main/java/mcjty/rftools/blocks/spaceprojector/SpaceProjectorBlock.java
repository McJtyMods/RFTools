package mcjty.rftools.blocks.spaceprojector;

import mcjty.container.GenericContainerBlock;
import mcjty.rftools.RFTools;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

public class SpaceProjectorBlock extends GenericContainerBlock {

    private IIcon iconTop;

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

    @Override
    public void registerBlockIcons(IIconRegister iconRegister) {
        iconTop = iconRegister.registerIcon(RFTools.MODID + ":" + "machineSpaceProjector");
        iconSide = iconRegister.registerIcon(RFTools.MODID + ":" + "machineSide");
        iconBottom = iconRegister.registerIcon(RFTools.MODID + ":" + "machineBottom");
    }

    @Override
    public IIcon getIcon(IBlockAccess blockAccess, int x, int y, int z, int side) {
        if (side == ForgeDirection.UP.ordinal()) {
            return iconTop;
        } else if (side == ForgeDirection.DOWN.ordinal()) {
            return iconBottom;
        } else {
            return iconSide;
        }
    }

    @Override
    public IIcon getIcon(int side, int meta) {
        if (side == ForgeDirection.UP.ordinal()) {
            return iconTop;
        } else if (side == ForgeDirection.DOWN.ordinal()) {
            return iconBottom;
        } else {
            return iconSide;
        }
    }


}
