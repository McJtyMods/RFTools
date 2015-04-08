package mcjty.rftools.blocks.spaceprojector;

import mcjty.container.GenericContainerBlock;
import mcjty.container.WrenchUsage;
import mcjty.rftools.RFTools;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class SpaceChamberControllerBlock extends GenericContainerBlock {

    public SpaceChamberControllerBlock() {
        super(Material.iron, SpaceChamberControllerTileEntity.class);
        setBlockName("spaceChamberControllerBlock");
        setCreativeTab(RFTools.tabRfTools);
    }

    @Override
    public int getGuiID() {
        return -1;
    }

    @Override
    public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int side, float sx, float sy, float sz) {
        WrenchUsage wrenchUsed = testWrenchUsage(x, y, z, player);
        if (wrenchUsed == WrenchUsage.NORMAL) {
            if (world.isRemote) {
                world.playSound(x, y, z, "note.pling", 1.0f, 1.0f, false);
            } else {
                SpaceChamberControllerTileEntity chamberControllerTileEntity = (SpaceChamberControllerTileEntity) world.getTileEntity(x, y, z);
                chamberControllerTileEntity.createChamber(player);
            }
            return true;
        } else if (wrenchUsed == WrenchUsage.SNEAKING) {
            breakAndRemember(world, x, y, z);
            return true;
        } else {
            return openGui(world, x, y, z, player);
        }
    }

    @Override
    public boolean renderAsNormalBlock() {
        return false;
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
    public void registerBlockIcons(IIconRegister iconRegister) {
        iconSide = iconRegister.registerIcon(RFTools.MODID + ":machineSpaceChamberController");
    }

    @Override
    public IIcon getIcon(IBlockAccess world, int x, int y, int z, int side) {
        return iconSide;
    }

    @Override
    public IIcon getIcon(int side, int meta) {
        return iconSide;
    }

}
