package mcjty.rftools.blocks.environmental;

import cpw.mods.fml.common.Optional;
import dan200.computercraft.api.lua.ILuaContext;
import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.api.peripheral.IComputerAccess;
import dan200.computercraft.api.peripheral.IPeripheral;
import li.cil.oc.api.machine.Arguments;
import li.cil.oc.api.machine.Callback;
import li.cil.oc.api.machine.Context;
import li.cil.oc.api.network.SimpleComponent;
import mcjty.lib.container.InventoryHelper;
import mcjty.lib.entity.GenericEnergyReceiverTileEntity;
import mcjty.lib.network.Argument;
import mcjty.lib.varia.Logging;
import mcjty.rftools.blocks.RedstoneMode;
import mcjty.rftools.blocks.environmental.modules.EnvironmentModule;
import mcjty.rftools.blocks.teleporter.PlayerName;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.common.util.ForgeDirection;

import java.util.*;

@Optional.InterfaceList({
        @Optional.Interface(iface = "li.cil.oc.api.network.SimpleComponent", modid = "OpenComputers"),
        @Optional.Interface(iface = "dan200.computercraft.api.peripheral.IPeripheral", modid = "ComputerCraft")})
public class EnvironmentalControllerTileEntity extends GenericEnergyReceiverTileEntity implements ISidedInventory, SimpleComponent, IPeripheral {

    public static final String CMD_SETRADIUS = "setRadius";
    public static final String CMD_SETBOUNDS = "setBounds";
    public static final String CMD_RSMODE = "rsMode";
    public static final String CMD_SETBLACKLIST = "setBlacklist";
    public static final String CMD_ADDPLAYER = "addPlayer";
    public static final String CMD_DELPLAYER = "delPlayer";
    public static final String CMD_GETPLAYERS = "getPlayers";
    public static final String CLIENTCMD_GETPLAYERS = "getPlayers";

    public static final String COMPONENT_NAME = "environmental_controller";

    private InventoryHelper inventoryHelper = new InventoryHelper(this, EnvironmentalControllerContainer.factory, EnvironmentalControllerContainer.ENV_MODULES);

    // Cached server modules
    private List<EnvironmentModule> environmentModules = null;
    private Set<String> players = new HashSet<String>();
    private boolean whitelistMode = false;
    private int totalRfPerTick = 0;     // The total rf per tick for all modules.
    private int radius = 50;
    private int miny = 30;
    private int maxy = 70;
    private int volume = -1;
    private boolean active = false;

    private RedstoneMode redstoneMode = RedstoneMode.REDSTONE_IGNORED;
    private int powered = 0;

    private int powerTimeout = 0;

    public EnvironmentalControllerTileEntity() {
        super(EnvironmentalConfiguration.ENVIRONMENTAL_MAXENERGY, EnvironmentalConfiguration.ENVIRONMENTAL_RECEIVEPERTICK);
    }

    @Override
    @Optional.Method(modid = "ComputerCraft")
    public String getType() {
        return COMPONENT_NAME;
    }

    @Override
    @Optional.Method(modid = "ComputerCraft")
    public String[] getMethodNames() {
        return new String[] { "getRedstoneMode", "setRedstoneMode" };
    }

    @Override
    @Optional.Method(modid = "ComputerCraft")
    public Object[] callMethod(IComputerAccess computer, ILuaContext context, int method, Object[] arguments) throws LuaException, InterruptedException {
        switch (method) {
            case 0: return new Object[] { getRedstoneMode().getDescription() };
            case 1: return setRedstoneMode((String) arguments[0]);
        }
        return new Object[0];
    }

    @Override
    @Optional.Method(modid = "ComputerCraft")
    public void attach(IComputerAccess computer) {

    }

    @Override
    @Optional.Method(modid = "ComputerCraft")
    public void detach(IComputerAccess computer) {

    }

    @Override
    @Optional.Method(modid = "ComputerCraft")
    public boolean equals(IPeripheral other) {
        return false;
    }

    @Override
    @Optional.Method(modid = "OpenComputers")
    public String getComponentName() {
        return COMPONENT_NAME;
    }

    @Callback(doc = "Get the current redstone mode. Values are 'Ignored', 'Off', or 'On'", getter = true)
    @Optional.Method(modid = "OpenComputers")
    public Object[] getRedstoneMode(Context context, Arguments args) throws Exception {
        return new Object[] { getRedstoneMode().getDescription() };
    }

    @Callback(doc = "Set the current redstone mode. Values are 'Ignored', 'Off', or 'On'", setter = true)
    @Optional.Method(modid = "OpenComputers")
    public Object[] setRedstoneMode(Context context, Arguments args) throws Exception {
        String mode = args.checkString(0);
        return setRedstoneMode(mode);
    }

    public boolean isWhitelistMode() {
        return whitelistMode;
    }

    public void setWhitelistMode(boolean w) {
        whitelistMode = w;
        markDirty();
    }

    public boolean isPlayerAffected(EntityPlayer player) {
        if (whitelistMode) {
            return players.contains(player.getDisplayName());
        } else {
            return !players.contains(player.getDisplayName());
        }
    }

    public List<PlayerName> getPlayersAsList() {
        List<PlayerName> p = new ArrayList<PlayerName>();
        for (String player : players) {
            p.add(new PlayerName(player));
        }
        return p;
    }

    public void addPlayer(String player) {
        if (!players.contains(player)) {
            players.add(player);
            worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
            markDirty();
        }
    }

    public void delPlayer(String player) {
        if (players.contains(player)) {
            players.remove(player);
            worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
            markDirty();
        }
    }

    public boolean isActive() {
        return active;
    }

    public int getTotalRfPerTick() {
        if (environmentModules == null) {
            getEnvironmentModules();
        }
        int rfNeeded = (int) (totalRfPerTick * (4.0f - getInfusedFactor()) / 4.0f);
        if (environmentModules.isEmpty()) {
            return rfNeeded;
        }
        if (rfNeeded < EnvironmentalConfiguration.MIN_USAGE) {
            rfNeeded = EnvironmentalConfiguration.MIN_USAGE;
        }
        return rfNeeded;
    }

    public int getVolume() {
        if (volume == -1) {
            volume = (int) ((radius * radius * Math.PI) * (maxy-miny + 1));
        }
        return volume;
    }

    public int getRadius() {
        return radius;
    }

    public void setRadius(int radius) {
        this.radius = radius;
        volume = -1;
        environmentModules = null;
        markDirty();
        worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
    }

    public int getMiny() {
        return miny;
    }

    public void setMiny(int miny) {
        this.miny = miny;
        volume = -1;
        environmentModules = null;
        markDirty();
        worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
    }

    public int getMaxy() {
        return maxy;
    }

    public void setMaxy(int maxy) {
        this.maxy = maxy;
        volume = -1;
        environmentModules = null;
        markDirty();
        worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
    }

    @Override
    protected void checkStateServer() {
        if (powerTimeout > 0) {
            powerTimeout--;
            return;
        }

        int rf = getEnergyStored(ForgeDirection.DOWN);

        if (redstoneMode != RedstoneMode.REDSTONE_IGNORED) {
            boolean rs = powered > 0;
            if (redstoneMode == RedstoneMode.REDSTONE_OFFREQUIRED) {
                if (rs) {
                    rf = 0;         // Turn of by simulating no power.
                }
            } else if (redstoneMode == RedstoneMode.REDSTONE_ONREQUIRED) {
                if (!rs) {
                    rf = 0;         // Turn of by simulating no power.
                }
            }
        }

        getEnvironmentModules();

        int rfNeeded = getTotalRfPerTick();
        if (rfNeeded > rf || environmentModules.isEmpty()) {
            for (EnvironmentModule module : environmentModules) {
                module.activate(false);
            }
            powerTimeout = 20;
            if (active) {
                active = false;
                markDirty();
                worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
            }
        } else {
            consumeEnergy(rfNeeded);
            for (EnvironmentModule module : environmentModules) {
                module.activate(true);
                module.tick(worldObj, xCoord, yCoord, zCoord, radius, miny, maxy, this);
            }
            if (!active) {
                active = true;
                markDirty();
                worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
            }
        }
    }

    private Object[] setRedstoneMode(String mode) {
        RedstoneMode redstoneMode = RedstoneMode.getMode(mode);
        if (redstoneMode == null) {
            throw new IllegalArgumentException("Not a valid mode");
        }
        setRedstoneMode(redstoneMode);
        return null;
    }

    public void setRedstoneMode(RedstoneMode redstoneMode) {
        this.redstoneMode = redstoneMode;
        worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
        markDirty();
    }

    public RedstoneMode getRedstoneMode() {
        return redstoneMode;
    }

    @Override
    public void setPowered(int powered) {
        if (this.powered != powered) {
            this.powered = powered;
            powerTimeout = 0;
            markDirty();
        }
    }

    // This is called server side.
    public List<EnvironmentModule> getEnvironmentModules() {
        if (environmentModules == null) {
            int volume = getVolume();
            totalRfPerTick = 0;
            environmentModules = new ArrayList<EnvironmentModule>();
            for (int i = 0 ; i < inventoryHelper.getCount() ; i++) {
                ItemStack itemStack = inventoryHelper.getStackInSlot(i);
                if (itemStack != null && itemStack.getItem() instanceof EnvModuleProvider) {
                    EnvModuleProvider moduleProvider = (EnvModuleProvider) itemStack.getItem();
                    Class<? extends EnvironmentModule> moduleClass = moduleProvider.getServerEnvironmentModule();
                    EnvironmentModule environmentModule;
                    try {
                        environmentModule = moduleClass.newInstance();
                    } catch (InstantiationException e) {
                        Logging.log("Failed to instantiate controller module!");
                        continue;
                    } catch (IllegalAccessException e) {
                        Logging.log("Failed to instantiate controller module!");
                        continue;
                    }
                    environmentModules.add(environmentModule);
                    totalRfPerTick += (int) (environmentModule.getRfPerTick() * volume);
                }
            }

        }
        return environmentModules;
    }

    @Override
    public int[] getAccessibleSlotsFromSide(int side) {
        return EnvironmentalControllerContainer.factory.getAccessibleSlots();
    }

    @Override
    public boolean canInsertItem(int index, ItemStack stack, int side) {
        return EnvironmentalControllerContainer.factory.isInputSlot(index);
    }

    @Override
    public boolean canExtractItem(int index, ItemStack stack, int side) {
        return EnvironmentalControllerContainer.factory.isOutputSlot(index);
    }

    @Override
    public int getSizeInventory() {
        return inventoryHelper.getCount();
    }

    @Override
    public ItemStack getStackInSlot(int index) {
        return inventoryHelper.getStackInSlot(index);
    }

    @Override
    public ItemStack decrStackSize(int index, int amount) {
        environmentModules = null;
        return inventoryHelper.decrStackSize(index, amount);
    }

    @Override
    public ItemStack getStackInSlotOnClosing(int index) {
        return null;
    }

    @Override
    public void setInventorySlotContents(int index, ItemStack stack) {
        inventoryHelper.setInventorySlotContents(getInventoryStackLimit(), index, stack);
        environmentModules = null;
    }

    @Override
    public String getInventoryName() {
        return "Environmental Inventory";
    }

    @Override
    public boolean hasCustomInventoryName() {
        return false;
    }

    @Override
    public int getInventoryStackLimit() {
        return 1;
    }

    @Override
    public boolean isUseableByPlayer(EntityPlayer player) {
        return canPlayerAccess(player);
    }

    @Override
    public void openInventory() {

    }

    @Override
    public void closeInventory() {

    }

    @Override
    public boolean isItemValidForSlot(int index, ItemStack stack) {
        return true;
    }

    @Override
    public void readFromNBT(NBTTagCompound tagCompound) {
        super.readFromNBT(tagCompound);
        totalRfPerTick = tagCompound.getInteger("rfPerTick");
        active = tagCompound.getBoolean("active");
        powered = tagCompound.getByte("powered");
    }

    @Override
    public void readRestorableFromNBT(NBTTagCompound tagCompound) {
        super.readRestorableFromNBT(tagCompound);
        readBufferFromNBT(tagCompound);
        radius = tagCompound.getInteger("radius");
        miny = tagCompound.getInteger("miny");
        maxy = tagCompound.getInteger("maxy");
        volume = -1;
        int m = tagCompound.getByte("rsMode");
        redstoneMode = RedstoneMode.values()[m];

        whitelistMode = tagCompound.getBoolean("whitelist");

        players.clear();
        NBTTagList playerList = tagCompound.getTagList("players", Constants.NBT.TAG_STRING);
        if (playerList != null) {
            for (int i = 0 ; i < playerList.tagCount() ; i++) {
                String player = playerList.getStringTagAt(i);
                players.add(player);
            }
        }
    }

    private void readBufferFromNBT(NBTTagCompound tagCompound) {
        NBTTagList bufferTagList = tagCompound.getTagList("Items", Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < bufferTagList.tagCount(); i++) {
            NBTTagCompound nbtTagCompound = bufferTagList.getCompoundTagAt(i);
            inventoryHelper.setStackInSlot(i + EnvironmentalControllerContainer.SLOT_MODULES, ItemStack.loadItemStackFromNBT(nbtTagCompound));
        }
        environmentModules = null;
    }

    @Override
    public void writeToNBT(NBTTagCompound tagCompound) {
        super.writeToNBT(tagCompound);
        tagCompound.setInteger("rfPerTick", totalRfPerTick);
        tagCompound.setBoolean("active", active);
        tagCompound.setByte("powered", (byte) powered);
    }

    @Override
    public void writeRestorableToNBT(NBTTagCompound tagCompound) {
        super.writeRestorableToNBT(tagCompound);
        writeBufferToNBT(tagCompound);
        tagCompound.setInteger("radius", radius);
        tagCompound.setInteger("miny", miny);
        tagCompound.setInteger("maxy", maxy);
        tagCompound.setByte("rsMode", (byte) redstoneMode.ordinal());

        tagCompound.setBoolean("whitelist", whitelistMode);

        NBTTagList playerTagList = new NBTTagList();
        for (String player : players) {
            playerTagList.appendTag(new NBTTagString(player));
        }
        tagCompound.setTag("players", playerTagList);
    }

    private void writeBufferToNBT(NBTTagCompound tagCompound) {
        NBTTagList bufferTagList = new NBTTagList();
        for (int i = EnvironmentalControllerContainer.SLOT_MODULES; i < inventoryHelper.getCount(); i++) {
            ItemStack stack = inventoryHelper.getStackInSlot(i);
            NBTTagCompound nbtTagCompound = new NBTTagCompound();
            if (stack != null) {
                stack.writeToNBT(nbtTagCompound);
            }
            bufferTagList.appendTag(nbtTagCompound);
        }
        tagCompound.setTag("Items", bufferTagList);
    }

    @Override
    public boolean execute(EntityPlayerMP playerMP, String command, Map<String, Argument> args) {
        boolean rc = super.execute(playerMP, command, args);
        if (rc) {
            return true;
        }
        if (CMD_SETRADIUS.equals(command)) {
            setRadius(args.get("radius").getInteger());
            return true;
        } else if (CMD_SETBOUNDS.equals(command)) {
            int miny = args.get("miny").getInteger();
            int maxy = args.get("maxy").getInteger();
            setMiny(miny);
            setMaxy(maxy);
            return true;
        } else if (CMD_RSMODE.equals(command)) {
            String m = args.get("rs").getString();
            setRedstoneMode(RedstoneMode.getMode(m));
            return true;
        } else if (CMD_ADDPLAYER.equals(command)) {
            addPlayer(args.get("player").getString());
            return true;
        } else if (CMD_DELPLAYER.equals(command)) {
            delPlayer(args.get("player").getString());
            return true;
        } else if (CMD_SETBLACKLIST.equals(command)) {
            setWhitelistMode(!args.get("blacklist").getBoolean());
            return true;
        }
        return false;
    }

    @Override
    public List executeWithResultList(String command, Map<String, Argument> args) {
        List rc = super.executeWithResultList(command, args);
        if (rc != null) {
            return rc;
        }
        if (CMD_GETPLAYERS.equals(command)) {
            return getPlayersAsList();
        }
        return null;
    }

    @Override
    public boolean execute(String command, List list) {
        boolean rc = super.execute(command, list);
        if (rc) {
            return true;
        }
        if (CLIENTCMD_GETPLAYERS.equals(command)) {
            GuiEnvironmentalController.storePlayersForClient(list);
            return true;
        }
        return false;
    }

    @Override
    public boolean shouldRenderInPass(int pass) {
        return pass == 1;
    }
}
