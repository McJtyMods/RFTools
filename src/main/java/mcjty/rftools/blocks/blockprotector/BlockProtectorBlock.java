package mcjty.rftools.blocks.blockprotector;

import mcjty.api.Infusable;
import mcjty.container.GenericContainerBlock;
import mcjty.rftools.RFTools;
import net.minecraft.block.material.Material;

public class BlockProtectorBlock extends GenericContainerBlock implements Infusable {

    public BlockProtectorBlock() {
        super(Material.iron, BlockProtectorTileEntity.class);
        setBlockName("blockProtectorBlock");
        setHorizRotation(true);
        setCreativeTab(RFTools.tabRfTools);
    }

    @Override
    public int getGuiID() {
        return -1;
    }

    @Override
    public String getIdentifyingIconName() {
        return "machineBlockProtector";
    }
}
