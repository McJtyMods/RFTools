package mcjty.rftools.blocks.elevator;


import mcjty.lib.container.BaseBlock;
import mcjty.lib.entity.GenericEnergyReceiverTileEntity;
import mcjty.lib.network.Argument;
import mcjty.lib.varia.Broadcaster;
import mcjty.rftools.blocks.builder.BuilderTileEntity;
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
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.*;

public class ElevatorTileEntity extends GenericEnergyReceiverTileEntity implements ITickable {

    public static String CMD_SETNAME = "setName";

    private boolean prevIn = false;

    private double movingY = -1;
    private int startY;
    private int stopY;

    private String name = "";

    // The positions of the blocks we are currently moving (with 'y' set to the height of the controller)
    private Set<BlockPos> positions = new HashSet<>();
    private List<Bounds> bounds = new ArrayList<>();
    // The state that is moving
    private IBlockState movingState;

    // Cache: points to the current controller (bottom elevator block)
    private BlockPos cachedControllerPos;
    private int cachedLevels;       // Cached number of levels
    private int cachedCurrent = -1;

    private boolean redstoneOut = false;

    // All entities currently on the platform (server side only)
    private Set<Entity> entitiesOnPlatform = new HashSet<>();
    private boolean entitiesOnPlatformComplete = false; // If true then we know entitiesOnPlatform is complete, otherwise it only contains players.

    public ElevatorTileEntity() {
        super(ElevatorConfiguration.MAXENERGY, ElevatorConfiguration.RFPERTICK);
    }

    public void clearCaches(EnumFacing side) {
        for (int y = 0 ; y < getWorld().getHeight() ; y++) {
            BlockPos pos2 = getPosAtY(getPos(), y);
            if (getWorld().getBlockState(pos2).getBlock() == ElevatorSetup.elevatorBlock) {
                TileEntity te = getWorld().getTileEntity(pos2);
                if (te instanceof ElevatorTileEntity) {
                    EnumFacing side2 = getWorld().getBlockState(pos2).getValue(BaseBlock.FACING_HORIZ);
                    if (side2 == side) {
                        ElevatorTileEntity tileEntity = (ElevatorTileEntity) te;
                        tileEntity.cachedControllerPos = null;
                        tileEntity.cachedLevels = 0;
                        tileEntity.cachedCurrent = -1;
                    }
                }
            }
        }
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
        markDirtyClient();
    }

    private void setRedstoneState() {
        markDirty();
        getWorld().notifyNeighborsOfStateChange(this.pos, this.getBlockType(), false);
    }


    @Override
    public void update() {
        if (!getWorld().isRemote) {
            boolean newout = isPlatformHere();
            if (newout != redstoneOut) {
                redstoneOut = newout;
                setRedstoneState();
            }

            if (isMoving()) {
                markDirty();

                double d = calculateSpeed();
                boolean stopped = handlePlatformMovement(d);
                if (stopped) {
                    stopMoving();
                    moveEntities(0, true);
                    clearMovement();
                } else {
                    moveEntities(d, false);
                }
                return;
            }

            if ((powerLevel > 0) == prevIn) {
                return;
            }
            prevIn = (powerLevel > 0);
            markDirty();

            if (powerLevel > 0) {
                movePlatformHere();
            }
        } else {
            if (isMoving()) {
                handleClientMovement();
            }
            handleSound();
        }
    }

    @Override
    public void invalidate() {
        super.invalidate();
        if (getWorld().isRemote) {
            stopSounds();
        }
    }

    @SideOnly(Side.CLIENT)
    private void stopSounds() {
        ElevatorSounds.stopSound(getWorld(), getPos());
    }

    @SideOnly(Side.CLIENT)
    protected void handleSound() {
        if (ElevatorConfiguration.baseElevatorVolume < 0.01f) {
            // No sounds.
            return;
        }

        if (!isMoving()) {
            stopSounds();
            return;
        }
        boolean startup = Math.abs(startY-movingY) < ElevatorConfiguration.maxSpeedDistanceStart;
        boolean shutdown = Math.abs(movingY-stopY) < ElevatorConfiguration.maxSpeedDistanceEnd * 2;

        if (shutdown) {
            if (!ElevatorSounds.isStopPlaying(getWorld(), pos)) {
                ElevatorSounds.playStop(getWorld(), pos);
            }
        } else if (startup) {
            if (!ElevatorSounds.isStartupPlaying(getWorld(), pos)) {
                ElevatorSounds.playStartup(getWorld(), pos);
            }
        } else {
            if (!ElevatorSounds.isLoopPlaying(getWorld(), pos)) {
                ElevatorSounds.playLoop(getWorld(), pos);
            }
        }
        ElevatorSounds.moveSound(getWorld(), pos, (float) movingY);
    }

    @SideOnly(Side.CLIENT)
    private void handleClientMovement() {
        double d = calculateSpeed();
        handlePlatformMovement(d);
        if (!bounds.isEmpty()) {
            EntityPlayerSP player = Minecraft.getMinecraft().player;
            for(Bounds strip : bounds){
                AxisAlignedBB aabb = getAABBAtStrip(d, strip);
                boolean on = player.getEntityBoundingBox().intersects(aabb);
                if (on) {
                    player.setPosition(player.posX, movingY + 1, player.posZ);
                    break;
                }
            }
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
    private void moveEntities(double speed, boolean stop) {
        if (bounds == null) {
            return;
        }
        double offset = speed > 0 ? speed * 2 : speed;
        Set<Entity> oldEntities = this.entitiesOnPlatform;
        entitiesOnPlatform = new HashSet<>();

        for(Bounds strip : bounds){
            List<Entity> entities = getWorld().getEntitiesWithinAABB(Entity.class, getAABBAtStrip(speed, strip));
            for (Entity entity : entities) {
                if(entitiesOnPlatform.contains(entity)){
                    continue;
                }
                entitiesOnPlatform.add(entity);
                moveEntityOnPlatform(stop, offset, entity);
            }
        }

        for (Entity entity : oldEntities) {
            if (!this.entitiesOnPlatform.contains(entity)) {
                // Entity was on the platform before but it isn't anymore. If it was a player we do a safety check
                // to ensure it is still in the patform shaft and in that case put it back on the platform.
                // We also put back the entity if we know the list is complete.
                if (entity instanceof EntityPlayer || entitiesOnPlatformComplete) {
                    for(Bounds strip : bounds){
                        if (entity.getEntityBoundingBox().intersects(getAABBBigMarginAtStrip(strip))) {
                            // Entity is no longer on the platform but was on the platform before and
                            // is still in the elevator shaft. In that case we put it back.
                            entitiesOnPlatform.add(entity);
                            moveEntityOnPlatform(stop, offset, entity);
                        }
                    }
                }

                if (entity instanceof EntityPlayer) {
                    BuffProperties.disableElevatorMode((EntityPlayer) entity);
                }
            }
        }

        // Entities on platform is now complete so set this to true
        entitiesOnPlatformComplete = true;
    }

    private void moveEntityOnPlatform(boolean stop, double offset, Entity entity) {
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
            }
        } else {
            double dy = 1.2 + offset;
            entity.posY = movingY + dy;
            entity.setPositionAndUpdate(entity.posX, movingY + dy, entity.posZ);
        }
        entity.onGround = true;
        entity.fallDistance = 0;
    }

    // Find the position of the bottom elevator.
    public BlockPos findBottomElevator() {
        if (cachedControllerPos != null) {
            return cachedControllerPos;
        }

        // The orientation of this elevator.
        IBlockState blockState = getWorld().getBlockState(getPos());
        if (blockState.getBlock() != ElevatorSetup.elevatorBlock) {
            return null;
        }
        EnumFacing side = blockState.getValue(BaseBlock.FACING_HORIZ);

        for (int y = 0; y < getWorld().getHeight(); y++) {
            BlockPos elevatorPos = getPosAtY(getPos(), y);
            IBlockState otherState = getWorld().getBlockState(elevatorPos);
            if (otherState.getBlock() == ElevatorSetup.elevatorBlock) {
                EnumFacing otherSide = otherState.getValue(BaseBlock.FACING_HORIZ);
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
        EnumFacing side = getWorld().getBlockState(getPos()).getValue(BaseBlock.FACING_HORIZ);

        for (int y = 0; y < getWorld().getHeight(); y++) {
            BlockPos elevatorPos = getPosAtY(getPos(), y);
            IBlockState otherState = getWorld().getBlockState(elevatorPos);
            if (otherState.getBlock() == ElevatorSetup.elevatorBlock) {
                EnumFacing otherSide = otherState.getValue(BaseBlock.FACING_HORIZ);
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
        World world = getWorld();
        IBlockState state = world.getBlockState(frontPos);
        Block block = state.getBlock();
        return !block.isAir(state, world, frontPos) && !block.hasTileEntity(state);
    }

    public Set<BlockPos> getPositions() {
        return positions;
    }

    public IBlockState getMovingState() {
        return movingState;
    }

    private void stopMoving() {
        movingY = stopY;
        for (BlockPos pos : positions) {
            if (getWorld().getBlockState(pos).getBlock().isReplaceable(getWorld(), pos)) {
                getWorld().setBlockState(getPosAtY(pos, stopY), movingState, 3);
            }
        }
        // Current level will have to be recalculated
        cachedCurrent = -1;
        markDirtyClient();
    }

    private void clearMovement() {
        positions.clear();
        entitiesOnPlatform.clear();
        movingState = null;
        bounds.clear();
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
    private boolean startMoving(BlockPos start, BlockPos stop, IBlockState state) {
        movingState = state;
        positions.clear();

        getBounds(start);
        if(bounds.isEmpty()) {
            // No blocks were added to bounds. This happens when canMoveBlock
            // returns false for the platform block right in front of the elevator.
            // If this is the case, we can't move at all.
            return false;
        }

        movingY = start.getY();
        startY = start.getY();
        stopY = stop.getY();

        // @todo
        // Make sure positions is only sent to client at the beginning
        markDirtyClient();
        return true;
    }

    private boolean canMoveBlock(BlockPos pos) {
        World world = getWorld();
        IBlockState state = world.getBlockState(pos);
        return state == movingState && state.getBlockHardness(world, pos) >= 0 && BuilderTileEntity.allowedToBreak(state, world, pos, BuilderTileEntity.getHarvester());
    }

    // Always called on controller TE (bottom one)
    private void getBounds(BlockPos start) {
        EnumFacing side = getWorld().getBlockState(getPos()).getValue(BaseBlock.FACING_HORIZ);
        bounds.clear();
        for (int a = 1; a < ElevatorConfiguration.maxPlatformSize; a++) {
            BlockPos offset = start.offset(side, a);
            if (canMoveBlock(offset)) {
                getWorld().setBlockToAir(offset);
                Bounds strip = new Bounds();
                strip.addPos(getPosAtY(offset, getPos().getY()));
                positions.add(getPosAtY(offset, getPos().getY()));

                for (int b = 1; b <= (ElevatorConfiguration.maxPlatformSize / 2); b++) {
                    BlockPos offsetLeft = offset.offset(side.rotateY(), b);
                    if (canMoveBlock(offsetLeft)) {
                        getWorld().setBlockToAir(offsetLeft);
                        strip.addPos(getPosAtY(offsetLeft, getPos().getY()));
                        positions.add(getPosAtY(offsetLeft, getPos().getY()));
                    } else {
                        break;
                    }
                }

                for (int b = 1; b <= (ElevatorConfiguration.maxPlatformSize / 2); b++) {
                    BlockPos offsetRight = offset.offset(side.rotateYCCW(), b);
                    if (canMoveBlock(offsetRight)) {
                        getWorld().setBlockToAir(offsetRight);
                        strip.addPos(getPosAtY(offsetRight, getPos().getY()));
                        positions.add(getPosAtY(offsetRight, getPos().getY()));
                    } else {
                        break;
                    }
                }
                
                bounds.add(strip);
            } else {
                break;
            }
        }
    }

    public boolean intersects(AxisAlignedBB aabb) {
        for(Bounds strip : bounds){
            if(getAABBAtStrip(0, strip).intersects(aabb)) {
                return true;
            }
        }
        return false;
    }
    
    private AxisAlignedBB getAABBBigMarginAtStrip(Bounds strip) {
        return new AxisAlignedBB(strip.getMinX(), movingY-150, strip.getMinZ(), strip.getMaxX() + 1, movingY + 150, strip.getMaxZ() + 1);
    }

    private AxisAlignedBB getAABBAtStrip(double speed, Bounds strip) {
        double o1 = 0;
        double o2 = 0;
        if (speed > 0) {
            o1 = -speed * 2;
        } else {
            o2 = -speed * 2;
        }
        return new AxisAlignedBB(strip.getMinX(), movingY-1+o1, strip.getMinZ(), strip.getMaxX() + 1, movingY + 3+o2, strip.getMaxZ() + 1);
    }
    
    public boolean isMoving() {
        return movingY >= 0;
    }

    public double getMovingY() {
        return movingY;
    }

    // Go to the specific level (levels start at 0)
    public void toLevel(int level) {
        EnumFacing side = getWorld().getBlockState(getPos()).getValue(BaseBlock.FACING_HORIZ);
        BlockPos controllerPos = findBottomElevator();
        for (int y = controllerPos.getY() ; y < getWorld().getHeight() ; y++) {
            BlockPos pos2 = getPosAtY(controllerPos, y);
            if (getWorld().getBlockState(pos2).getBlock() == ElevatorSetup.elevatorBlock) {
                TileEntity te2 = getWorld().getTileEntity(pos2);
                if (te2 instanceof ElevatorTileEntity) {
                    EnumFacing side2 = getWorld().getBlockState(pos2).getValue(BaseBlock.FACING_HORIZ);
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
    }

    public void findElevatorBlocks(List<Integer> heights) {
        BlockPos controllerPos = findBottomElevator();
        if (controllerPos == null) {
            // Cannot happen
            return;
        }
        IBlockState blockState = getWorld().getBlockState(getPos());
        if (blockState.getBlock() != ElevatorSetup.elevatorBlock) {
            return;
        }
        EnumFacing side = blockState.getValue(BaseBlock.FACING_HORIZ);
        for (int y = controllerPos.getY() ; y < getWorld().getHeight() ; y++) {
            BlockPos pos2 = getPosAtY(controllerPos, y);
            TileEntity te2 = getWorld().getTileEntity(pos2);
            if (te2 instanceof ElevatorTileEntity) {
                EnumFacing side2 = getWorld().getBlockState(pos2).getValue(BaseBlock.FACING_HORIZ);
                if (side == side2) {
                    heights.add(y);
                }
            }
        }
    }


    public int getCurrentLevel(List<Integer> heights) {
        BlockPos controllerPos = findBottomElevator();
        TileEntity te = getWorld().getTileEntity(controllerPos);
        if (te instanceof ElevatorTileEntity) {
            EnumFacing side = getWorld().getBlockState(controllerPos).getValue(BaseBlock.FACING_HORIZ);
            ElevatorTileEntity controller = (ElevatorTileEntity) te;
            if (controller.cachedCurrent == -1) {
                int level = 0;
                for (Integer y : heights) {
                    BlockPos pos2 = getPosAtY(controllerPos, y);
                    BlockPos frontPos = pos2.offset(side);
                    if (isValidPlatformBlock(frontPos)) {
                        controller.cachedCurrent = level;
                    }
                    level++;
                }
            }
            return controller.cachedCurrent;
        }
        return 0;
    }

    public int getLevelCount(List<Integer> heights) {
        return heights.size();
    }

    // Return true if the platform is here (i.e. there is a block in front of the elevator)
    public boolean isPlatformHere() {
        IBlockState blockState = getWorld().getBlockState(getPos());
        if (blockState.getBlock() != ElevatorSetup.elevatorBlock) {
            return false;
        }
        EnumFacing side = blockState.getValue(BaseBlock.FACING_HORIZ);
        BlockPos frontPos = getPos().offset(side);
        return isValidPlatformBlock(frontPos);
    }

    // Can be called on any elevator block. Not only the contoller (bottom one)
    private void movePlatformHere() {
        // Try to find a platform and move it to this elevator.
        // What about TE blocks in front of platform?

        // First check if the platform is here already:
        IBlockState blockState = getWorld().getBlockState(getPos());
        if (blockState.getBlock() != ElevatorSetup.elevatorBlock) {
            return;
        }
        EnumFacing side = blockState.getValue(BaseBlock.FACING_HORIZ);
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
        ElevatorTileEntity controller = (ElevatorTileEntity) getWorld().getTileEntity(controllerPos);

        if (controller.isMoving()) {
            // Already moving, do nothing
            return;
        }

        // Check if we have enough energy
        int rfNeeded = (int) (ElevatorConfiguration.rfPerHeightUnit * Math.abs(getPos().getY() - platformPos.getY()) * (3.0f - getInfusedFactor()) / 3.0f);
        if (controller.getEnergyStored() < rfNeeded) {
            Broadcaster.broadcast(getWorld(), getPos().getX(), getPos().getY(), getPos().getZ(), TextFormatting.RED + "Not enough power to move the elevator platform!", 10);
            return;
        }

        if(controller.startMoving(platformPos, getPos(), getWorld().getBlockState(platformPos.offset(side)))) {
            controller.consumeEnergy(rfNeeded);
        } else {
            Broadcaster.broadcast(getWorld(), getPos().getX(), getPos().getY(), getPos().getZ(), TextFormatting.RED + "The block in front of the elevator platform could not be moved!", 10);
        }

    }

    public static BlockPos getPosAtY(BlockPos p, int y) {
        return new BlockPos(p.getX(), y, p.getZ());
    }

    @SideOnly(Side.CLIENT)
    @Override
    public AxisAlignedBB getRenderBoundingBox() {
        if (isMoving()) {
            return new AxisAlignedBB(getPos().add(-9, 0, -9), getPos().add(9, 255, 9));
        }
        return super.getRenderBoundingBox();
    }

    @SideOnly(Side.CLIENT)
    @Override
    public double getMaxRenderDistanceSquared() {
        if (isMoving()) {
            return 256 * 256;
        } else {
            return super.getMaxRenderDistanceSquared();
        }
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
        redstoneOut = tagCompound.getBoolean("rs");
        entitiesOnPlatformComplete = false;
        if (tagCompound.hasKey("players")) {
            entitiesOnPlatform.clear();
            WorldServer world = DimensionManager.getWorld(0);
            List<EntityPlayerMP> serverPlayers = world.getMinecraftServer().getPlayerList().getPlayers();
            NBTTagList playerList = tagCompound.getTagList("players", Constants.NBT.TAG_COMPOUND);
            for (int i = 0; i < playerList.tagCount(); i++) {
                NBTTagCompound p = playerList.getCompoundTagAt(i);
                long lsb = p.getLong("lsb");
                long msb = p.getLong("msb");
                UUID uuid = new UUID(msb, lsb);
                for (EntityPlayerMP serverPlayer : serverPlayers) {
                    if (serverPlayer.getGameProfile().getId().equals(uuid)) {
                        entitiesOnPlatform.add(serverPlayer);
                        break;
                    }
                }
            }
        }
    }

    private void readFromNBTCommon(NBTTagCompound tagCompound) {
        super.readFromNBT(tagCompound);
        prevIn = tagCompound.getBoolean("prevIn");
        movingY = tagCompound.getDouble("movingY");
        startY = tagCompound.getInteger("startY");
        stopY = tagCompound.getInteger("stopY");
        byte[] byteArray = tagCompound.getByteArray("relcoords");
        positions.clear();
        for (int i = 0; i < byteArray.length; i += 6) {
            short dx = bytesToShort(byteArray[i + 0], byteArray[i + 1]);
            short dy = bytesToShort(byteArray[i + 2], byteArray[i + 3]);
            short dz = bytesToShort(byteArray[i + 4], byteArray[i + 5]);
            RelCoordinate c = new RelCoordinate(dx, dy, dz);
            positions.add(new BlockPos(getPos().getX() + c.getDx(), getPos().getY() + c.getDy(), getPos().getZ() + c.getDz()));
        }
        
        if(tagCompound.hasKey("bounds")){
            bounds.clear();
            int[] strips = tagCompound.getIntArray("bounds");
            for (int i = 0; i < strips.length; i += 4) {
                bounds.add(new Bounds(strips[i + 0], strips[i + 1], strips[i + 2], strips[i + 3]));
            }
        }
        
        if (tagCompound.hasKey("movingId")) {
            Integer id = tagCompound.getInteger("movingId");
            movingState = Block.getStateById(id);
        } else if (tagCompound.hasKey("movingBlock")) {
            // Deprecated (@todo remove in 1.13)
            String id = tagCompound.getString("movingBlock");
            int meta = tagCompound.getInteger("movingMeta");
            movingState = Block.REGISTRY.getObject(new ResourceLocation(id)).getStateFromMeta(meta);
        }
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound tagCompound) {
        super.writeToNBT(tagCompound);
        tagCompound.setBoolean("rs", redstoneOut);
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
        tagCompound.setByteArray("relcoords", blocks);
        
        if (!bounds.isEmpty()) {
            int i = 0;
            int[] strips = new int[bounds.size() * 4];
            for(Bounds strip : bounds){
                strips[i + 0] = strip.getMinX();
                strips[i + 1] = strip.getMinZ();
                strips[i + 2] = strip.getMaxX();
                strips[i + 3] = strip.getMaxZ();
                i += 4;
            }
            tagCompound.setIntArray("bounds", strips);
        }

        if (movingState != null) {
            tagCompound.setInteger("movingId", Block.getStateId(movingState));
        }
        if (!getWorld().isRemote) {
            // Only do this server side
            if (!entitiesOnPlatform.isEmpty()) {
                NBTTagList playerList = new NBTTagList();
                for (Entity entity : entitiesOnPlatform) {
                    if (entity instanceof EntityPlayer) {
                        EntityPlayer player = (EntityPlayer) entity;
                        UUID id = player.getGameProfile().getId();
                        NBTTagCompound p = new NBTTagCompound();
                        p.setLong("lsb", id.getLeastSignificantBits());
                        p.setLong("msb", id.getMostSignificantBits());
                        playerList.appendTag(p);
                    }
                }
                tagCompound.setTag("players", playerList);
            }
        }
        return tagCompound;
    }

    @Override
    public void readRestorableFromNBT(NBTTagCompound tagCompound) {
        super.readRestorableFromNBT(tagCompound);
        name = tagCompound.getString("levelName");
    }

    @Override
    public void writeRestorableToNBT(NBTTagCompound tagCompound) {
        super.writeRestorableToNBT(tagCompound);
        tagCompound.setString("levelName", name == null ? "" : name);
    }

    @Override
    public boolean execute(EntityPlayerMP playerMP, String command, Map<String, Argument> args) {
        boolean rc = super.execute(playerMP, command, args);
        if (rc) {
            return true;
        }
        if (CMD_SETNAME.equals(command)) {
            setName(args.get("name").getString());
            return true;
        }
        return false;
    }

}
