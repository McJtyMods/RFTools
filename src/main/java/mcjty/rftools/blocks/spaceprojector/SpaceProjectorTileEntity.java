package mcjty.rftools.blocks.spaceprojector;

import mcjty.entity.GenericEnergyReceiverTileEntity;
import mcjty.varia.Coordinate;
import net.minecraft.block.Block;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;

public class SpaceProjectorTileEntity extends GenericEnergyReceiverTileEntity {

    private Coordinate chamberController;
    private int dimension = 0;
    private int channel = -1;

    public SpaceProjectorTileEntity() {
        super(SpaceProjectorConfiguration.SPACEPROJECTOR_MAXENERGY, SpaceProjectorConfiguration.SPACEPROJECTOR_RECEIVEPERTICK);
    }

    public void project() {
        if (channel == -1) {
            return;
        }

        SpaceChamberRepository repository = SpaceChamberRepository.getChannels(worldObj);
        SpaceChamberRepository.SpaceChamberChannel chamberChannel = repository.getChannel(channel);
        if (chamberChannel == null) {
            return;
        }
        Coordinate minCorner = chamberChannel.getMinCorner();
        Coordinate maxCorner = chamberChannel.getMaxCorner();
        if (minCorner == null || maxCorner == null) {
            return;
        }

        World world = DimensionManager.getWorld(chamberChannel.getDimension());
        TileEntity te = world.getTileEntity(chamberController.getX(), chamberController.getY(), chamberController.getZ());
        if (te instanceof SpaceChamberControllerTileEntity) {
            int dx = xCoord + 1 - minCorner.getX();
            int dy = yCoord + 1 - minCorner.getY();
            int dz = zCoord + 1 - minCorner.getZ();
            for (int x = minCorner.getX() ; x <= maxCorner.getX() ; x++) {
                for (int y = minCorner.getY() ; y <= maxCorner.getY() ; y++) {
                    for (int z = minCorner.getZ() ; z <= maxCorner.getZ() ; z++) {
                        Block block = world.getBlock(x, y, z);
                        if (block != null && !block.isAir(world, x, y, z)) {
                            world.setBlock(dx + x, dy + y, dz + z, SpaceProjectorSetup.proxyBlock, world.getBlockMetadata(x, y, z), 3);
                            ProxyBlockTileEntity proxyBlockTileEntity = (ProxyBlockTileEntity) world.getTileEntity(dx + x, dy + y, dz + z);
                            proxyBlockTileEntity.setCamoBlock(Block.blockRegistry.getIDForObject(block));
                            proxyBlockTileEntity.setOrigCoordinate(new Coordinate(x, y, z), dimension);
                        }
                    }
                }
            }
        }
    }

    @Override
    public boolean canUpdate() {
        return false;
    }

    @Override
    public void readFromNBT(NBTTagCompound tagCompound) {
        super.readFromNBT(tagCompound);
    }

    @Override
    public void readRestorableFromNBT(NBTTagCompound tagCompound) {
        super.readRestorableFromNBT(tagCompound);
        channel = tagCompound.getInteger("channel");
    }

    @Override
    public void writeToNBT(NBTTagCompound tagCompound) {
        super.writeToNBT(tagCompound);
    }

    @Override
    public void writeRestorableToNBT(NBTTagCompound tagCompound) {
        super.writeRestorableToNBT(tagCompound);
        tagCompound.setInteger("channel", channel);
    }
}
