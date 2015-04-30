package mcjty.rftools.blocks.blockprotector;

import mcjty.api.Infusable;
import mcjty.container.GenericContainerBlock;
import mcjty.rftools.RFTools;
import mcjty.rftools.items.smartwrench.SmartWrenchItem;
import mcjty.varia.Coordinate;
import mcjty.varia.GlobalCoordinate;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.world.World;

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

    @Override
    protected boolean wrenchSneakSelect(World world, int x, int y, int z, EntityPlayer player) {
        if (!world.isRemote) {
            GlobalCoordinate currentBlock = SmartWrenchItem.getCurrentBlock(player.getHeldItem());
            if (currentBlock == null) {
                SmartWrenchItem.setCurrentBlock(player.getHeldItem(), new GlobalCoordinate(new Coordinate(x, y, z), world.provider.dimensionId));
                RFTools.message(player, EnumChatFormatting.YELLOW + "Selected block");
            } else {
                SmartWrenchItem.setCurrentBlock(player.getHeldItem(), null);
                RFTools.message(player, EnumChatFormatting.YELLOW + "Cleared selected block");
            }
        }
        return true;
    }
}
