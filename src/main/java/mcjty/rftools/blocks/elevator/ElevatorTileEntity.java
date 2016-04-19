package mcjty.rftools.blocks.elevator;


import mcjty.lib.entity.GenericEnergyReceiverTileEntity;
import mcjty.rftools.blocks.shield.RelCoordinate;
import mcjty.rftools.playerprops.BuffProperties;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.*;

public class ElevatorTileEntity extends GenericEnergyReceiverTileEntity implements ITickable {

    private boolean prevIn = false;
    private boolean powered = false;

    private double movingY = -1;
    private int startY;
    private int stopY;

    // The positions of the blocks we are currently moving (with 'y' set to the height of the controller)
    private List<BlockPos> positions = new ArrayList<>();
    private Bounds bounds;
    // The state that is moving
    private IBlockState movingState;

    // Cache: points to the current controller (bottom elevator block)
    private BlockPos cachedControllerPos;
    private int cachedLevels;       // Cached number of levels
    private int cachedCurrent;

    // All players currently on the platform (server side only)
    private Set<EntityPlayer> players = new HashSet<>();

    public ElevatorTileEntity() {
        super(ElevatorConfiguration.MAXENERGY, ElevatorConfiguration.RFPERTICK);
    }

    public void clearCaches() {
        EnumFacing side = worldObj.getBlockState(getPos()).getValue(ElevatorBlock.FACING_HORIZ);
        for (int y = 0 ; y < worldObj.getHeight() ; y++) {
            BlockPos pos2 = getPosAtY(getPos(), y);
            TileEntity te = worldObj.getTileEntity(pos2);
            if (te instanceof ElevatorTileEntity) {
                EnumFacing side2 = worldObj.getBlockState(pos2).getValue(ElevatorBlock.FACING_HORIZ);
                if (side2 == side) {
                    ElevatorTileEntity tileEntity = (ElevatorTileEntity) te;
                    tileEntity.cachedControllerPos = null;
                    tileEntity.cachedLevels = 0;
                    tileEntity.cachedCurrent = -1;
                }
            }
        }
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

                double d = calculateSpeed();
                boolean stopped = handlePlatformMovement(d);
                if (stopped) {
                    stopMoving();
                    moveEntities(0, true);
                    clearMovement();
                } else {
                    moveEntities(d > 0 ? d * 4 : d, false);
                }
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
            if (isMoving()) {
                handleClientMovement();
            }
        }
    }

    @SideOnly(Side.CLIENT)
    private void handleClientMovement() {
        double d = calculateSpeed();
        handlePlatformMovement(d);
        EntityPlayerSP player = Minecraft.getMinecraft().thePlayer;
        AxisAlignedBB aabb = getAABBAboveElevator();
        boolean on = Minecraft.getMinecraft().thePlayer.getEntityBoundingBox().intersectsWith(aabb);
        if (on) {
            player.setPosition(player.posX, movingY + 1, player.posZ);
        }
    }

    private double calculateSpeed() {
        // The speed center y location is the location at which speed is maximum.
        // It is located closer to the end to make sure slowing down is a shorter period then speeding up.
        double speedDiff = ElevatorConfiguration.maximumSpeed - ElevatorConfiguration.minimumSpeed;
        double speedFromStart = ElevatorConfiguration.minimumSpeed + speedDiff * Math.abs((movingY - startY) / ElevatorConfiguration.maxSpeedDistanceStart);
        double speedFromStop = ElevatorConfiguration.minimumSpeed + speedDiff * Math.abs((movingY - stopY) / ElevatorConfiguration.maxSpeedDistanceEnd);
        double d = Math.min(speedFromStart, speedFromStop);
        if (stopY < startY) {
            d = -d;
        }
        return d;
    }

    private boolean handlePlatformMovement(double d) {
        if (stopY > startY) {
            if (movingY >= stopY) {
                return true;
            }
            movingY += d;

            if (movingY >= stopY) {
                movingY = stopY;
            }
        } else {
            if (movingY <= stopY) {
                return true;
            }
            movingY += d;

            if (movingY <= stopY) {
                movingY = stopY;
            }
        }
        return false;
    }

    // Only server side
    private void moveEntities(double offset, boolean stop) {
        if (bounds == null) {
            return;
        }
        Set<EntityPlayer> oldPlayers = this.players;
        players = new HashSet<>();
        List<Entity> entities = worldObj.getEntitiesWithinAABB(Entity.class, getAABBAboveElevator());
        for (Entity entity : entities) {

            entity.fallDistance = 0;
            if (entity instanceof EntityPlayer) {
                double dy = 1;
                EntityPlayer player = (EntityPlayer) entity;
                if (stop) {
                    BuffProperties.disableElevatorMode(player);
                    entity.posY = movingY + dy;
                    entity.setPositionAndUpdate(entity.posX, movingY + dy, entity.posZ);
                } else {
                    BuffProperties.enableElevatorMode(player);
                    entity.setPosition(entity.posX, movingY + dy, entity.posZ);
                    players.add(player);
                }
            } else {
                double dy = 1.2 + offset;
                entity.posY = movingY + dy;
                entity.setPositionAndUpdate(entity.posX, movingY + dy, entity.posZ);
            }
            entity.onGround = true;
            entity.fallDistance = 0;
        }

        for (EntityPlayer player : oldPlayers) {
            if (!players.contains(player)) {
                BuffProperties.disableElevatorMode(player);
            }
        }

    }

    // Find the position of the bottom elevator.
    private BlockPos findBottomElevator() {
        if (cachedControllerPos != null) {
            return cachedControllerPos;
        }

        // The orientation of this elevator.
        EnumFacing side = worldObj.getBlockState(getPos()).getValue(ElevatorBlock.FACING_HORIZ);

        for (int y = 0; y < worldObj.getHeight(); y++) {
            BlockPos elevatorPos = getPosAtY(getPos(), y);
            IBlockState otherState = worldObj.getBlockState(elevatorPos);
            if (otherState.getBlock() == ElevatorSetup.elevatorBlock) {
                EnumFacing otherSide = otherState.getValue(ElevatorBlock.FACING_HORIZ);
                if (otherSide == side) {
                    cachedControllerPos = elevatorPos;
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

        for (int y = 0; y < worldObj.getHeight(); y++) {
            BlockPos elevatorPos = getPosAtY(getPos(), y);
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
            worldObj.setBlockState(getPosAtY(pos, (int) stopY), movingState, 3);
        }
        // Current level will have to be recalculated
        cachedCurrent = -1;
    }

    private void clearMovement() {
        positions.clear();
        players.clear();
        movingState = null;
        bounds = null;
        movingY = -1;
    }

    private static class Bounds {
        private int minX = 1000000000;
        private int minZ = 1000000000;
        private int maxX = -1000000000;
        private int maxZ = -1000000000;

        public Bounds() {
        }

        public Bounds(int minX, int minZ, int maxX, int maxZ) {
            this.maxX = maxX;
            this.maxZ = maxZ;
            this.minX = minX;
            this.minZ = minZ;
        }

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
    private void startMoving(BlockPos start, BlockPos stop, IBlockState state) {
        System.out.println("Start moving: ystart = " + start.getY() + ", ystop = " + stop.getY());
        movingY = start.getY();
        startY = start.getY();
        stopY = stop.getY();
        movingState = state;
        positions.clear();

        getBounds(start);

        // @todo
        // Make sure positions is only sent to client at the beginning
        markDirtyClient();
    }

    // Always called on controller TE (bottom one)
    private void getBounds(BlockPos start) {
        EnumFacing side = worldObj.getBlockState(getPos()).getValue(ElevatorBlock.FACING_HORIZ);
        bounds = new Bounds();
        for (int a = 1; a < ElevatorConfiguration.maxPlatformSize; a++) {
            BlockPos offset = start.offset(side, a);
            if (worldObj.getBlockState(offset) == movingState) {
                worldObj.setBlockToAir(offset);
                bounds.addPos(offset);
                positions.add(getPosAtY(offset, getPos().getY()));

                for (int b = 1; b <= (ElevatorConfiguration.maxPlatformSize / 2); b++) {
                    BlockPos offsetLeft = offset.offset(side.rotateY(), b);
                    if (worldObj.getBlockState(offsetLeft) == movingState) {
                        worldObj.setBlockToAir(offsetLeft);
                        bounds.addPos(offsetLeft);
                        positions.add(getPosAtY(offsetLeft, getPos().getY()));
                    } else {
                        break;
                    }
                }

                for (int b = 1; b <= (ElevatorConfiguration.maxPlatformSize / 2); b++) {
                    BlockPos offsetRight = offset.offset(side.rotateYCCW(), b);
                    if (worldObj.getBlockState(offsetRight) == movingState) {
                        worldObj.setBlockToAir(offsetRight);
                        bounds.addPos(offsetRight);
                        positions.add(getPosAtY(offsetRight, getPos().getY()));
                    } else {
                        break;
                    }
                }
            } else {
                break;
            }
        }
    }

    public AxisAlignedBB getAABBAboveElevator() {
        return new AxisAlignedBB(bounds.getMinX(), movingY, bounds.getMinZ(), bounds.getMaxX() + 1, movingY + 3, bounds.getMaxZ() + 1);
    }

    public boolean isMoving() {
        return movingY >= 0;
    }

    public double getMovingY() {
        return movingY;
    }

    // Go to the specific level (levels start at 0)
    public void toLevel(int level) {
        EnumFacing side = worldObj.getBlockState(getPos()).getValue(ElevatorBlock.FACING_HORIZ);
        BlockPos controllerPos = findBottomElevator();
        for (int y = controllerPos.getY() ; y < worldObj.getHeight() ; y++) {
            BlockPos pos2 = getPosAtY(controllerPos, y);
            TileEntity te2 = worldObj.getTileEntity(pos2);
            if (te2 instanceof ElevatorTileEntity) {
                EnumFacing side2 = worldObj.getBlockState(pos2).getValue(ElevatorBlock.FACING_HORIZ);
                if (side == side2) {
                    if (level == 0) {
                        ((ElevatorTileEntity) te2).movePlatformHere();
                        return;
                    }
                    level--;
                }
            }
        }
    }

    public int getCurrentLevel() {
        BlockPos controllerPos = findBottomElevator();
        EnumFacing side = worldObj.getBlockState(getPos()).getValue(ElevatorBlock.FACING_HORIZ);
        TileEntity te = worldObj.getTileEntity(controllerPos);
        if (te instanceof ElevatorTileEntity) {
            ElevatorTileEntity controller = (ElevatorTileEntity) te;
            if (controller.cachedCurrent == -1) {
                int level = 0;
                for (int y = controllerPos.getY() ; y < worldObj.getHeight() ; y++) {
                    BlockPos pos2 = getPosAtY(controllerPos, y);
                    TileEntity te2 = worldObj.getTileEntity(pos2);
                    if (te2 instanceof ElevatorTileEntity) {
                        EnumFacing side2 = worldObj.getBlockState(pos2).getValue(ElevatorBlock.FACING_HORIZ);
                        if (side == side2) {
                            BlockPos frontPos = pos2.offset(side);
                            if (isValidPlatformBlock(frontPos)) {
                                controller.cachedCurrent = level;
                            }
                            level++;
                        }
                    }
                }
            }
            return controller.cachedCurrent;
        }
        return 0;
    }

    public int getLevelCount() {
        BlockPos controllerPos = findBottomElevator();
        EnumFacing side = worldObj.getBlockState(getPos()).getValue(ElevatorBlock.FACING_HORIZ);
        TileEntity te = worldObj.getTileEntity(controllerPos);
        if (te instanceof ElevatorTileEntity) {
            ElevatorTileEntity controller = (ElevatorTileEntity) te;
            if (controller.cachedLevels == 0) {
                for (int y = controllerPos.getY() ; y < worldObj.getHeight() ; y++) {
                    BlockPos pos2 = getPosAtY(controllerPos, y);
                    TileEntity te2 = worldObj.getTileEntity(pos2);
                    if (te2 instanceof ElevatorTileEntity) {
                        EnumFacing side2 = worldObj.getBlockState(pos2).getValue(ElevatorBlock.FACING_HORIZ);
                        if (side == side2) {
                            controller.cachedLevels++;
                        }
                    }
                }
            }
            return controller.cachedLevels;
        }
        return 0;
    }

    // Can be called on any elevator block. Not only the contoller (bottom one)
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

        controller.startMoving(platformPos, getPos(), worldObj.getBlockState(platformPos.offset(side)));
    }

    private BlockPos getPosAtY(BlockPos p, int y) {
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
            return new AxisAlignedBB(getPos().add(-9, 0, -9), getPos().add(9, 255, 9));
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
    public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity packet) {
        NBTTagCompound compound = packet.getNbtCompound();
        this.readFromNBTCommon(compound);
    }

    @Override
    public void readFromNBT(NBTTagCompound tagCompound) {
        readFromNBTCommon(tagCompound);
        if (tagCompound.hasKey("players")) {
            players.clear();
            WorldServer world = DimensionManager.getWorld(0);
            List<EntityPlayerMP> serverPlayers = world.getMinecraftServer().getPlayerList().getPlayerList();
            NBTTagList playerList = tagCompound.getTagList("players", Constants.NBT.TAG_COMPOUND);
            for (int i = 0; i < playerList.tagCount(); i++) {
                NBTTagCompound p = playerList.getCompoundTagAt(i);
                long lsb = p.getLong("lsb");
                long msb = p.getLong("msb");
                UUID uuid = new UUID(msb, lsb);
                for (EntityPlayerMP serverPlayer : serverPlayers) {
                    if (serverPlayer.getGameProfile().getId().equals(uuid)) {
                        players.add(serverPlayer);
                        break;
                    }
                }
            }
        }
    }

    private void readFromNBTCommon(NBTTagCompound tagCompound) {
        super.readFromNBT(tagCompound);
        prevIn = tagCompound.getBoolean("prevIn");
        powered = tagCompound.getBoolean("powered");
        movingY = tagCompound.getDouble("movingY");
        startY = tagCompound.getInteger("startY");
        stopY = tagCompound.getInteger("stopY");
        byte[] byteArray = tagCompound.getByteArray("relcoords");
        positions.clear();
        int j = 0;
        for (int i = 0; i < byteArray.length / 6; i++) {
            short dx = bytesToShort(byteArray[j + 0], byteArray[j + 1]);
            short dy = bytesToShort(byteArray[j + 2], byteArray[j + 3]);
            short dz = bytesToShort(byteArray[j + 4], byteArray[j + 5]);
            j += 6;
            RelCoordinate c = new RelCoordinate(dx, dy, dz);
            positions.add(new BlockPos(getPos().getX() + c.getDx(), getPos().getY() + c.getDy(), getPos().getZ() + c.getDz()));
        }
        if (tagCompound.hasKey("bminX")) {
            bounds = new Bounds(tagCompound.getInteger("bminX"), tagCompound.getInteger("bminZ"), tagCompound.getInteger("bmaxX"), tagCompound.getInteger("bmaxZ"));
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
        tagCompound.setInteger("startY", startY);
        tagCompound.setInteger("stopY", stopY);
        byte[] blocks = new byte[positions.size() * 6];
        int j = 0;
        for (BlockPos pos : positions) {
            RelCoordinate c = new RelCoordinate(pos.getX() - getPos().getX(), pos.getY() - getPos().getY(), pos.getZ() - getPos().getZ());
            blocks[j + 0] = shortToByte1((short) c.getDx());
            blocks[j + 1] = shortToByte2((short) c.getDx());
            blocks[j + 2] = shortToByte1((short) c.getDy());
            blocks[j + 3] = shortToByte2((short) c.getDy());
            blocks[j + 4] = shortToByte1((short) c.getDz());
            blocks[j + 5] = shortToByte2((short) c.getDz());
            j += 6;
        }
        if (bounds != null) {
            tagCompound.setInteger("bminX", bounds.getMinX());
            tagCompound.setInteger("bminZ", bounds.getMinZ());
            tagCompound.setInteger("bmaxX", bounds.getMaxX());
            tagCompound.setInteger("bmaxZ", bounds.getMaxZ());
        }
        tagCompound.setByteArray("relcoords", blocks);
        if (movingState != null) {
            tagCompound.setString("movingBlock", movingState.getBlock().getRegistryName().toString());
            tagCompound.setInteger("movingMeta", movingState.getBlock().getMetaFromState(movingState));
        }
        if (!worldObj.isRemote) {
            // Only do this server side
            if (!players.isEmpty()) {
                NBTTagList playerList = new NBTTagList();
                for (EntityPlayer player : players) {
                    UUID id = player.getGameProfile().getId();
                    NBTTagCompound p = new NBTTagCompound();
                    p.setLong("lsb", id.getLeastSignificantBits());
                    p.setLong("msb", id.getMostSignificantBits());
                    playerList.appendTag(p);
                }
                tagCompound.setTag("players", playerList);
            }
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
