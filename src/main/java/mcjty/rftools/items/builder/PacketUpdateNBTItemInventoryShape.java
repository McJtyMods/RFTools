package mcjty.rftools.items.builder;

import mcjty.lib.network.PacketUpdateNBTItemInventory;
import mcjty.lib.network.PacketUpdateNBTItemInventoryHandler;
import mcjty.rftools.blocks.shaper.ComposerTileEntity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class PacketUpdateNBTItemInventoryShape extends PacketUpdateNBTItemInventory {

    public PacketUpdateNBTItemInventoryShape() {
    }

    public PacketUpdateNBTItemInventoryShape(BlockPos pos, int slotIndex, NBTTagCompound tagCompound) {
        super(pos, slotIndex, tagCompound);
    }

    @Override
    protected boolean isValidBlock(World world, BlockPos blockPos, TileEntity tileEntity) {
        return tileEntity instanceof ComposerTileEntity;
    }

    public static class Handler extends PacketUpdateNBTItemInventoryHandler<PacketUpdateNBTItemInventoryShape> {

    }
}
