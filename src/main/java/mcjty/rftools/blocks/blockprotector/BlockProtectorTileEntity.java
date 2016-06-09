package mcjty.rftools.blocks.blockprotector;

import mcjty.lib.api.smartwrench.SmartWrenchSelector;
import mcjty.lib.entity.GenericEnergyReceiverTileEntity;
import mcjty.lib.network.Argument;
import mcjty.lib.varia.BlockPosTools;
import mcjty.lib.varia.GlobalCoordinate;
import mcjty.lib.varia.Logging;
import mcjty.lib.varia.RedstoneMode;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.common.util.Constants;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

//@Optional.InterfaceList({
//        @Optional.Interface(iface = "li.cil.oc.api.network.SimpleComponent", modid = "OpenComputers"),
//        @Optional.Interface(iface = "dan200.computercraft.api.peripheral.IPeripheral", modid = "ComputerCraft")})
public class BlockProtectorTileEntity extends GenericEnergyReceiverTileEntity implements SmartWrenchSelector, ITickable /*, SimpleComponent, IPeripheral*/ {

    public static final String CMD_RSMODE = "rsMode";
    public static final String COMPONENT_NAME = "block_protector";

    private int id = -1;
    private boolean active = false;

    // Relative coordinates (relative to this tile entity)
    private Set<BlockPos> protectedBlocks = new HashSet<>();

//    @Override
//    @Optional.Method(modid = "ComputerCraft")
//    public String getType() {
//        return COMPONENT_NAME;
//    }
//
//    @Override
//    @Optional.Method(modid = "ComputerCraft")
//    public String[] getMethodNames() {
//        return new String[] { "getRedstoneMode", "setRedstoneMode" };
//    }
//
//    @Override
//    @Optional.Method(modid = "ComputerCraft")
//    public Object[] callMethod(IComputerAccess computer, ILuaContext context, int method, Object[] arguments) throws LuaException, InterruptedException {
//        switch (method) {
//            case 0: return new Object[] { getRedstoneMode().getDescription() };
//            case 1: return setRedstoneMode((String) arguments[0]);
//        }
//        return new Object[0];
//    }
//
//    @Override
//    @Optional.Method(modid = "ComputerCraft")
//    public void attach(IComputerAccess computer) {
//
//    }
//
//    @Override
//    @Optional.Method(modid = "ComputerCraft")
//    public void detach(IComputerAccess computer) {
//
//    }
//
//    @Override
//    @Optional.Method(modid = "ComputerCraft")
//    public boolean equals(IPeripheral other) {
//        return false;
//    }
//
//    @Override
//    @Optional.Method(modid = "OpenComputers")
//    public String getComponentName() {
//        return COMPONENT_NAME;
//    }
//
//    @Callback(doc = "Get the current redstone mode. Values are 'Ignored', 'Off', or 'On'", getter = true)
//    @Optional.Method(modid = "OpenComputers")
//    public Object[] getRedstoneMode(Context context, Arguments args) throws Exception {
//        return new Object[] { getRedstoneMode().getDescription() };
//    }
//
//    @Callback(doc = "Set the current redstone mode. Values are 'Ignored', 'Off', or 'On'", setter = true)
//    @Optional.Method(modid = "OpenComputers")
//    public Object[] setRedstoneMode(Context context, Arguments args) throws Exception {
//        String mode = args.checkString(0);
//        return setRedstoneMode(mode);
//    }

    public BlockProtectorTileEntity() {
        super(BlockProtectorConfiguration.MAXENERGY, BlockProtectorConfiguration.RECEIVEPERTICK);
    }

    @Override
    public void update() {
        if (!worldObj.isRemote) {
            checkStateServer();
        }
    }

    private void checkStateServer() {
        if (protectedBlocks.isEmpty()) {
            setActive(false);
            return;
        }

        if (!isMachineEnabled()) {
            setActive(false);
            return;
        } else {
            setActive(true);
        }

        consumeEnergy(protectedBlocks.size() * BlockProtectorConfiguration.rfPerProtectedBlock);
    }

    @Override
    public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity packet) {
        boolean oldActive = active;

        super.onDataPacket(net, packet);

        if (worldObj.isRemote) {
            // If needed send a render update.
            if (active != oldActive) {
                worldObj.markBlockRangeForRenderUpdate(getPos(), getPos());
            }
        }
    }

    public boolean isActive() {
        return active;
    }

    private void setActive(boolean a) {
        active = a;
        markDirtyClient();
    }


    public boolean attemptHarvestProtection() {
        if (!isMachineEnabled()) return false;
        int rf = getEnergyStored(EnumFacing.DOWN);
        if (BlockProtectorConfiguration.rfForHarvestAttempt > rf) {
            return false;
        }
        consumeEnergy(BlockProtectorConfiguration.rfForHarvestAttempt);
        return true;
    }

    // Distance is relative with 0 being closes to the explosion and 1 being furthest away.
    public int attemptExplosionProtection(float distance, float radius) {
        if (!isMachineEnabled()) return -1;
        int rf = getEnergyStored(EnumFacing.DOWN);
        int rfneeded = (int) (BlockProtectorConfiguration.rfForExplosionProtection * (1.0 - distance) * radius / 8.0f) + 1;
        rfneeded = (int) (rfneeded * (2.0f - getInfusedFactor()) / 2.0f);

        if (rfneeded > rf) {
            return -1;
        }
        if (rfneeded <= 0) {
            rfneeded = 1;
        }
        consumeEnergy(rfneeded);
        return rfneeded;
    }

    public Set<BlockPos> getProtectedBlocks() {
        return protectedBlocks;
    }

    public BlockPos absoluteToRelative(BlockPos c) {
        return absoluteToRelative(c.getX(), c.getY(), c.getZ());
    }

    public BlockPos absoluteToRelative(int x, int y, int z) {
        return new BlockPos(x - getPos().getX(), y - getPos().getY(), z - getPos().getZ());
    }

    // Test if this relative coordinate is protected.
    public boolean isProtected(BlockPos c) {
        return protectedBlocks.contains(c);
    }

    // Used by the explosion event handler.
    public void removeProtection(BlockPos relative) {
        protectedBlocks.remove(relative);
        markDirtyClient();
    }

    // Toggle a coordinate to be protected or not. The coordinate given here is absolute.
    public void toggleCoordinate(GlobalCoordinate c) {
        if (c.getDimension() != worldObj.provider.getDimension()) {
            // Wrong dimension. Don't do anything.
            return;
        }
        BlockPos relative = absoluteToRelative(c.getCoordinate());
        if (protectedBlocks.contains(relative)) {
            protectedBlocks.remove(relative);
        } else {
            protectedBlocks.add(relative);
        }
        markDirtyClient();
    }

    @Override
    public void selectBlock(EntityPlayer player, BlockPos pos) {
        // This is always called server side.
        int xCoord = getPos().getX();
        int yCoord = getPos().getY();
        int zCoord = getPos().getZ();
        if (Math.abs(pos.getX()-xCoord) > BlockProtectorConfiguration.maxProtectDistance
                || Math.abs(pos.getY()-yCoord) > BlockProtectorConfiguration.maxProtectDistance
                || Math.abs(pos.getZ()-zCoord) > BlockProtectorConfiguration.maxProtectDistance) {
            Logging.message(player, TextFormatting.RED + "Block out of range of the block protector!");
            return;
        }
        GlobalCoordinate gc = new GlobalCoordinate(pos, worldObj.provider.getDimension());
        toggleCoordinate(gc);
    }

    public int getOrCalculateID() {
        if (id == -1) {
            BlockProtectors protectors = BlockProtectors.getProtectors(worldObj);
            GlobalCoordinate gc = new GlobalCoordinate(getPos(), worldObj.provider.getDimension());
            id = protectors.getNewId(gc);

            protectors.save(worldObj);
            setId(id);
        }
        return id;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
        markDirtyClient();
    }

    /**
     * This method is called after putting down a protector that was earlier wrenched. We need to fix the data in
     * the destination.
     */
    public void updateDestination() {
        BlockProtectors protectors = BlockProtectors.getProtectors(worldObj);

        GlobalCoordinate gc = new GlobalCoordinate(getPos(), worldObj.provider.getDimension());

        if (id == -1) {
            id = protectors.getNewId(gc);
            markDirty();
        } else {
            protectors.assignId(gc, id);
        }

        protectors.save(worldObj);
        markDirtyClient();
    }


    @Override
    public void readFromNBT(NBTTagCompound tagCompound) {
        super.readFromNBT(tagCompound);

        NBTTagList tagList = tagCompound.getTagList("coordinates", Constants.NBT.TAG_COMPOUND);
        protectedBlocks.clear();
        for (int i = 0 ; i < tagList.tagCount() ; i++) {
            NBTTagCompound tag = (NBTTagCompound) tagList.get(i);
            protectedBlocks.add(BlockPosTools.readFromNBT(tag, "c"));
        }
        active = tagCompound.getBoolean("active");
    }

    @Override
    public void readRestorableFromNBT(NBTTagCompound tagCompound) {
        super.readRestorableFromNBT(tagCompound);
        if (tagCompound.hasKey("protectorId")) {
            id = tagCompound.getInteger("protectorId");
        } else {
            id = -1;
        }
        int m = tagCompound.getByte("rsMode");
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound tagCompound) {
        super.writeToNBT(tagCompound);
        NBTTagList list = new NBTTagList();
        for (BlockPos block : protectedBlocks) {
            list.appendTag(BlockPosTools.writeToNBT(block));
        }
        tagCompound.setTag("coordinates", list);
        tagCompound.setBoolean("active", active);
        return tagCompound;
    }


    @Override
    public void writeRestorableToNBT(NBTTagCompound tagCompound) {
        super.writeRestorableToNBT(tagCompound);
        tagCompound.setInteger("protectorId", id);
    }

    @Override
    public boolean execute(EntityPlayerMP playerMP, String command, Map<String, Argument> args) {
        boolean rc = super.execute(playerMP, command, args);
        if (rc) {
            return true;
        }
        if (CMD_RSMODE.equals(command)) {
            String m = args.get("rs").getString();
            setRSMode(RedstoneMode.getMode(m));
            return true;
        }
        return false;
    }

}
