package mcjty.rftools.blocks.elevator;


import mcjty.lib.entity.GenericEnergyReceiverTileEntity;
import mcjty.rftools.blocks.shield.RelCoordinate;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ElevatorTileEntity extends GenericEnergyReceiverTileEntity implements ITickable {

    private boolean prevIn = false;
    private boolean powered = false;

    private double movingY = -1;
    private double startY;
    private double stopY;

    // The positions of the blocks we are currently moving (with 'y' set to the height of the controller)
    private List<BlockPos> positions = new ArrayList<>();
    // The state that is moving
    private IBlockState movingState;

    public ElevatorTileEntity() {
        super(ElevatorConfiguration.MAXENERGY, ElevatorConfiguration.RFPERTICK);
    }

    @Override
    public void update() {
        if (!worldObj.isRemote) {
            if (isMoving()) {
                markDirtyClient();

                int rfNeeded = (int) (ElevatorConfiguration.rfPerTickMoving * (3.0f - getInfusedFactor()) / 3.0f);
                if (getEnergyStored(EnumFacing.DOWN) < rfNeeded) {
                    return;
                }
                consumeEnergy(rfNeeded);

                handlePlatformMovement();
                return;
            }

            if (powered == prevIn) {
                return;
            }
            prevIn = powered;
            markDirty();

            if (powered) {
                movePlatformHere();
            }
        } else {
//            if (isMoving()) {
//                double d = calculateSpeed();
//                if (stopY > startY) {
//                    moveEntities(d * 3, d);
//                } else {
//                    moveEntities(0, -d);
//                }

//                handlePlatformMovement();
//            }
        }
    }

    private List<Pair<Entity, Double>> entities = new ArrayList<>();


    private double calculateSpeed() {
        // The speed center y location is the location at which speed is maximum.
        // It is located closer to the end to make sure slowing down is a shorter period then speeding up.
        double speedDiff = ElevatorConfiguration.maximumSpeed - ElevatorConfiguration.minimumSpeed;
        double speedFromStart = ElevatorConfiguration.minimumSpeed + speedDiff * Math.abs((movingY - startY) / ElevatorConfiguration.maxSpeedDistanceStart);
        double speedFromStop = ElevatorConfiguration.minimumSpeed + speedDiff * Math.abs((movingY - stopY) / ElevatorConfiguration.maxSpeedDistanceEnd);
        return Math.min(speedFromStart, speedFromStop);
    }

    private void handlePlatformMovement() {
        double d = calculateSpeed();
        if (stopY > startY) {
            if (movingY >= stopY) {
                stopMoving();
                return;
            }
            movingY += d;

            moveEntities(d * 3, d);

            if (movingY >= stopY) {
                movingY = stopY;
            }
        } else {
            if (movingY <= stopY) {
                stopMoving();
                return;
            }
            movingY -= d;

            moveEntities(0, -d);

            if (movingY <= stopY) {
                movingY = stopY;
            }

        }
    }

    private void moveEntities(double offset, double speed) {
        for (Pair<Entity, Double> pair : entities) {
            Entity entity = pair.getLeft();

            Double dy = pair.getRight() + offset;
            if (entity instanceof EntityPlayer) {
//                entity.motionY += speed;

                entity.posY = movingY + dy;
                entity.setPositionAndUpdate(entity.posX, movingY + dy, entity.posZ);


//                entity.setPosition(entity.posX, movingY + dy, entity.posZ);
//                worldObj.updateEntityWithOptionalForce(entity, false);
            } else {
                entity.posY = movingY + dy;
                entity.setPositionAndUpdate(entity.posX, movingY + dy, entity.posZ);
            }
            entity.onGround = true;
            entity.fallDistance = 0;
        }
    }

    // Find the position of the bottom elevator.
    private BlockPos findBottomElevator() {
        // The orientation of this elevator.
        EnumFacing side = worldObj.getBlockState(getPos()).getValue(ElevatorBlock.FACING_HORIZ);

        for (int y = 0 ; y < worldObj.getHeight() ; y++) {
            BlockPos elevatorPos = setY(getPos(), y);
            IBlockState otherState = worldObj.getBlockState(elevatorPos);
            if (otherState.getBlock() == ElevatorSetup.elevatorBlock) {
                EnumFacing otherSide = otherState.getValue(ElevatorBlock.FACING_HORIZ);
                if (otherSide == side) {
                    return elevatorPos;
                }
            }
        }
        return null;
    }

    // Find the position of the elevator that has the platform.
    private BlockPos findElevatorWithPlatform() {
        // The orientation of this elevator.
        EnumFacing side = worldObj.getBlockState(getPos()).getValue(ElevatorBlock.FACING_HORIZ);

        for (int y = 0 ; y < worldObj.getHeight() ; y++) {
            BlockPos elevatorPos = setY(getPos(), y);
            IBlockState otherState = worldObj.getBlockState(elevatorPos);
            if (otherState.getBlock() == ElevatorSetup.elevatorBlock) {
                EnumFacing otherSide = otherState.getValue(ElevatorBlock.FACING_HORIZ);
                if (otherSide == side) {
                    BlockPos frontPos = elevatorPos.offset(side);
                    if (isValidPlatformBlock(frontPos)) {
                        return elevatorPos;
                    }
                }
            }
        }
        return null;
    }

    private boolean isValidPlatformBlock(BlockPos frontPos) {
        if (worldObj.isAirBlock(frontPos)) {
            return false;
        }
        if (worldObj.getTileEntity(frontPos) != null) {
            return false;
        }
        return true;
    }

    public List<BlockPos> getPositions() {
        return positions;
    }

    public IBlockState getMovingState() {
        return movingState;
    }

    private void stopMoving() {
        movingY = stopY;
        for (BlockPos pos : positions) {
            worldObj.setBlockState(setY(pos, (int) stopY), movingState, 3);
        }
        moveEntities(0, 0);

        positions.clear();
        movingState = null;
        movingY = -1;
    }

    private static class Bounds {
        int minX = 1000000000;
        int minZ = 1000000000;
        int maxX = -1000000000;
        int maxZ = -1000000000;

        public void addPos(BlockPos pos) {
            if (pos.getX() < minX) {
                minX = pos.getX();
            }
            if (pos.getX() > maxX) {
                maxX = pos.getX();
            }
            if (pos.getZ() < minZ) {
                minZ = pos.getZ();
            }
            if (pos.getZ() > maxZ) {
                maxZ = pos.getZ();
            }
        }

        public int getMinX() {
            return minX;
        }

        public int getMinZ() {
            return minZ;
        }

        public int getMaxX() {
            return maxX;
        }

        public int getMaxZ() {
            return maxZ;
        }
    }

    // Only call this on the controller (bottom elevator)
    private void startMoving(BlockPos start, BlockPos stop, BlockPos controllerPos, IBlockState state) {
        System.out.println("Start moving: ystart = " + start.getY() + ", ystop = " + stop.getY());
        EnumFacing side = worldObj.getBlockState(getPos()).getValue(ElevatorBlock.FACING_HORIZ);
        movingY = start.getY();
        startY = start.getY();
        stopY = stop.getY();
        movingState = state;
        positions.clear();

        Bounds bounds = new Bounds();

        for (int a = 1 ; a < ElevatorConfiguration.maxPlatformSize ; a++) {
            BlockPos offset = start.offset(side, a);
            if (worldObj.getBlockState(offset) == movingState) {
                worldObj.setBlockToAir(offset);
                bounds.addPos(offset);
                positions.add(setY(offset, controllerPos.getY()));

                for (int b = 1 ; b <= (ElevatorConfiguration.maxPlatformSize / 2) ; b++) {
                    BlockPos offsetLeft = offset.offset(side.rotateY(), b);
                    if (worldObj.getBlockState(offsetLeft) == movingState) {
                        worldObj.setBlockToAir(offsetLeft);
                        bounds.addPos(offsetLeft);
                        positions.add(setY(offsetLeft, controllerPos.getY()));
                    } else {
                        break;
                    }
                }

                for (int b = 1 ; b <= (ElevatorConfiguration.maxPlatformSize / 2) ; b++) {
                    BlockPos offsetRight = offset.offset(side.rotateYCCW(), b);
                    if (worldObj.getBlockState(offsetRight) == movingState) {
                        worldObj.setBlockToAir(offsetRight);
                        bounds.addPos(offsetRight);
                        positions.add(setY(offsetRight, controllerPos.getY()));
                    } else {
                        break;
                    }
                }
            } else {
                break;
            }
        }

        List<Entity> entityList = worldObj.getEntitiesWithinAABB(Entity.class, new AxisAlignedBB(bounds.getMinX(), start.getY(), bounds.getMinZ(), bounds.getMaxX()+1, start.getY() + 3, bounds.getMaxZ()+1));
        entities = entityList.stream().map(e -> Pair.of(e, e.posY - startY)).collect(Collectors.toList());

        // @todo
        // Make sure positions is only sent to client at the beginning
        markDirtyClient();
    }

    public boolean isMoving() {
        return movingY >= 0;
    }

    public double getMovingY() {
        return movingY;
    }

    private void movePlatformHere() {
        // Try to find a platform and move it to this elevator.
        // What about TE blocks in front of platform?

        // First check if the platform is here already:
        EnumFacing side = worldObj.getBlockState(getPos()).getValue(ElevatorBlock.FACING_HORIZ);
        BlockPos frontPos = getPos().offset(side);
        if (isValidPlatformBlock(frontPos)) {
            // Platform is already here (or something is blocking here)
            return;
        }

        // Find where the platform is.
        BlockPos platformPos = findElevatorWithPlatform();
        if (platformPos == null) {
            // No elevator platform found
            return;
        }

        // Find the bottom elevator (this is the one doing the work).
        BlockPos controllerPos = findBottomElevator();
        ElevatorTileEntity controller = (ElevatorTileEntity) worldObj.getTileEntity(controllerPos);

        if (controller.isMoving()) {
            // Already moving, do nothing
            return;
        }

        controller.startMoving(platformPos, getPos(), controllerPos, worldObj.getBlockState(platformPos.offset(side)));
    }

    private BlockPos setY(BlockPos p, int y) {
        return new BlockPos(p.getX(), y, p.getZ());
    }

    @Override
    public void setPowered(int powered) {
        boolean p = powered > 0;
        if (this.powered != p) {
            this.powered = p;
            markDirty();
        }
    }

    @SideOnly(Side.CLIENT)
    @Override
    public AxisAlignedBB getRenderBoundingBox() {
        if (isMoving()) {
            return new AxisAlignedBB(getPos().add(-7, 0, -7), getPos().add(7, 255, 7));
        }
        return super.getRenderBoundingBox();
    }

    private static short bytesToShort(byte b1, byte b2) {
        short s1 = (short) (b1 & 0xff);
        short s2 = (short) (b2 & 0xff);
        return (short) (s1 * 256 + s2);
    }

    private static byte shortToByte1(short s) {
        return (byte) ((s & 0xff00) >> 8);
    }

    private static byte shortToByte2(short s) {
        return (byte) (s & 0xff);
    }

    @Override
    public void readFromNBT(NBTTagCompound tagCompound) {
        super.readFromNBT(tagCompound);
        prevIn = tagCompound.getBoolean("prevIn");
        powered = tagCompound.getBoolean("powered");
        movingY = tagCompound.getDouble("movingY");
        startY = tagCompound.getDouble("startY");
        stopY = tagCompound.getDouble("stopY");
        byte[] byteArray = tagCompound.getByteArray("relcoords");
        positions.clear();
        int j = 0;
        for (int i = 0 ; i < byteArray.length / 6 ; i++) {
            short dx = bytesToShort(byteArray[j+0], byteArray[j+1]);
            short dy = bytesToShort(byteArray[j+2], byteArray[j+3]);
            short dz = bytesToShort(byteArray[j+4], byteArray[j+5]);
            j += 6;
            RelCoordinate c = new RelCoordinate(dx, dy, dz);
            positions.add(new BlockPos(getPos().getX() + c.getDx(), getPos().getY() + c.getDy(), getPos().getZ() + c.getDz()));
        }
        if (tagCompound.hasKey("movingBlock")) {
            String id = tagCompound.getString("movingBlock");
            int meta = tagCompound.getInteger("movingMeta");
            movingState = Block.blockRegistry.getObject(new ResourceLocation(id)).getStateFromMeta(meta);
        }
    }

    @Override
    public void writeToNBT(NBTTagCompound tagCompound) {
        super.writeToNBT(tagCompound);
        tagCompound.setBoolean("powered", powered);
        tagCompound.setBoolean("prevIn", prevIn);
        tagCompound.setDouble("movingY", movingY);
        tagCompound.setDouble("startY", startY);
        tagCompound.setDouble("stopY", stopY);
        byte[] blocks = new byte[positions.size() * 6];
        int j = 0;
        for (BlockPos pos : positions) {
            RelCoordinate c = new RelCoordinate(pos.getX() - getPos().getX(), pos.getY() - getPos().getY(), pos.getZ() - getPos().getZ());
            blocks[j+0] = shortToByte1((short) c.getDx());
            blocks[j+1] = shortToByte2((short) c.getDx());
            blocks[j+2] = shortToByte1((short) c.getDy());
            blocks[j+3] = shortToByte2((short) c.getDy());
            blocks[j+4] = shortToByte1((short) c.getDz());
            blocks[j+5] = shortToByte2((short) c.getDz());
            j += 6;
        }
        tagCompound.setByteArray("relcoords", blocks);
        if (movingState != null) {
            tagCompound.setString("movingBlock", movingState.getBlock().getRegistryName().toString());
            tagCompound.setInteger("movingMeta", movingState.getBlock().getMetaFromState(movingState));
        }
    }

    @Override
    public void readRestorableFromNBT(NBTTagCompound tagCompound) {
        super.readRestorableFromNBT(tagCompound);
    }

    @Override
    public void writeRestorableToNBT(NBTTagCompound tagCompound) {
        super.writeRestorableToNBT(tagCompound);
    }
}
