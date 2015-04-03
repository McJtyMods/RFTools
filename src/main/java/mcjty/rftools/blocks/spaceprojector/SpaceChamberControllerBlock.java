package mcjty.rftools.blocks.spaceprojector;

import mcjty.container.GenericContainerBlock;
import mcjty.container.WrenchUsage;
import mcjty.rftools.RFTools;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
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


}
