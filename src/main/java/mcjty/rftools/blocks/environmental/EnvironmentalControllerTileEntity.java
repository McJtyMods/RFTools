package mcjty.rftools.blocks.environmental;

import li.cil.oc.api.machine.Arguments;
import li.cil.oc.api.machine.Callback;
import li.cil.oc.api.machine.Context;
import li.cil.oc.api.network.SimpleComponent;
import mcjty.lib.api.information.IMachineInformation;
import mcjty.lib.container.ContainerFactory;
import mcjty.lib.container.DefaultSidedInventory;
import mcjty.lib.container.InventoryHelper;
import mcjty.lib.tileentity.GenericEnergyReceiverTileEntity;
import mcjty.lib.gui.widgets.ImageChoiceLabel;
import mcjty.lib.gui.widgets.ScrollableLabel;
import mcjty.lib.varia.Logging;
import mcjty.lib.varia.ModuleSupport;
import mcjty.lib.varia.RedstoneMode;
import mcjty.rftools.RFTools;
import mcjty.rftools.blocks.environmental.modules.EnvironmentModule;
import mcjty.theoneprobe.api.IProbeHitData;
import mcjty.theoneprobe.api.IProbeInfo;
import mcjty.theoneprobe.api.ProbeMode;
import mcjty.lib.typed.Key;
import mcjty.lib.typed.Type;
import mcjty.lib.typed.TypedMap;
import mcp.mobius.waila.api.IWailaConfigHandler;
import mcp.mobius.waila.api.IWailaDataAccessor;
import net.minecraft.block.state.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.monster.IMob;
import net.minecraft.entity.passive.IAnimals;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.util.Direction;
import net.minecraft.util.ITickable;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.common.Optional;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

@Optional.InterfaceList({
        @Optional.Interface(iface = "li.cil.oc.api.network.SimpleComponent", modid = "opencomputers"),
//        @Optional.Interface(iface = "dan200.computercraft.api.peripheral.IPeripheral", modid = "ComputerCraft")
})
public class EnvironmentalControllerTileEntity extends GenericEnergyReceiverTileEntity implements DefaultSidedInventory, ITickable,
        IMachineInformation, SimpleComponent /*, IPeripheral*/ {

    public static final String CMD_SETRADIUS = "env.setRadius";
    public static final String CMD_RSMODE = "env.setRsMode";

    public static final String CMD_SETBOUNDS = "env.setBounds";
    public static final Key<Integer> PARAM_MIN = new Key<>("min", Type.INTEGER);
    public static final Key<Integer> PARAM_MAX = new Key<>("max", Type.INTEGER);

    public static final String CMD_SETMODE = "env.setBlacklist";
    public static final Key<Integer> PARAM_MODE = new Key<>("mode", Type.INTEGER);

    public static final String CMD_ADDPLAYER = "env.addPlayer";
    public static final String CMD_DELPLAYER = "env.delPlayer";
    public static final Key<String> PARAM_NAME = new Key<>("name", Type.STRING);

    public static final String CMD_GETPLAYERS = "getPlayers";
    public static final String CLIENTCMD_GETPLAYERS = "getPlayers";

    public static final String COMPONENT_NAME = "environmental_controller";

    public static final int ENV_MODULES = 7;
    public static final ContainerFactory CONTAINER_FACTORY = new ContainerFactory(new ResourceLocation(RFTools.MODID, "gui/environmental.gui"));
    public static final int SLOT_MODULES = 0;

    public static final ModuleSupport MODULE_SUPPORT = new ModuleSupport(SLOT_MODULES, SLOT_MODULES + ENV_MODULES - 1) {
        @Override
        public boolean isModule(ItemStack itemStack) {
            return itemStack.getItem() instanceof EnvModuleProvider;
        }
    };
    public static final String CONTAINER_INVENTORY = "container";

    private InventoryHelper inventoryHelper = new InventoryHelper(this, CONTAINER_FACTORY, ENV_MODULES);

    public enum EnvironmentalMode {
        MODE_BLACKLIST,
        MODE_WHITELIST,
        MODE_HOSTILE,
        MODE_PASSIVE,
        MODE_MOBS,
        MODE_ALL
    }

    // Cached server modules
    private List<EnvironmentModule> environmentModules = null;
    Set<String> players = new HashSet<>();
    private EnvironmentalMode mode = EnvironmentalMode.MODE_BLACKLIST;
    private int totalRfPerTick = 0;     // The total rf per tick for all modules.
    private int radius = 50;
    private int miny = 30;
    private int maxy = 70;
    private int volume = -1;
    private boolean active = false;

    private int powerTimeout = 0;

    public EnvironmentalControllerTileEntity() {
        super(EnvironmentalConfiguration.ENVIRONMENTAL_MAXENERGY.get(), EnvironmentalConfiguration.ENVIRONMENTAL_RECEIVEPERTICK.get());
    }

    @Override
    public long getEnergyDiffPerTick() {
        return isActive() ? -getTotalRfPerTick() : 0;
    }

    @Nullable
    @Override
    public String getEnergyUnitName() {
        return "RF";
    }

    @Override
    public boolean isMachineActive() {
        return isActive();
    }

    @Override
    public boolean isMachineRunning() {
        return isActive();
    }

    @Nullable
    @Override
    public String getMachineStatus() {
        return isActive() ? "active" : "idle";
    }

    @Override
    protected boolean needsRedstoneMode() {
        return true;
    }

    @Override
    protected boolean needsCustomInvWrapper() {
        return true;
    }


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

    @Override
    @Optional.Method(modid = "opencomputers")
    public String getComponentName() {
        return COMPONENT_NAME;
    }

    @Callback(doc = "Get or set the current redstone mode. Values are 'Ignored', 'Off', or 'On'", getter = true, setter = true)
    @Optional.Method(modid = "opencomputers")
    public Object[] redstoneMode(Context context, Arguments args) {
        if (args.count() == 0) {
            return new Object[]{getRSMode().getDescription()};
        } else {
            String mode = args.checkString(0);
            return setRedstoneMode(mode);
        }
    }

    public EnvironmentalMode getMode() {
        return mode;
    }

    public void setMode(EnvironmentalMode mode) {
        this.mode = mode;
        markDirtyClient();
    }

    private float getPowerMultiplier() {
        switch (mode) {
            case MODE_BLACKLIST:
            case MODE_WHITELIST:
                return 1.0f;
            case MODE_HOSTILE:
            case MODE_PASSIVE:
            case MODE_MOBS:
            case MODE_ALL:
                return (float) EnvironmentalConfiguration.mobsPowerMultiplier.get();
        }
        return 1.0f;
    }

    public boolean isEntityAffected(Entity entity) {
        switch (mode) {
            case MODE_BLACKLIST:
                if (entity instanceof PlayerEntity) {
                    return isPlayerAffected((PlayerEntity) entity);
                } else {
                    return false;
                }
            case MODE_WHITELIST:
                if (entity instanceof PlayerEntity) {
                    return isPlayerAffected((PlayerEntity) entity);
                } else {
                    return false;
                }
            case MODE_HOSTILE:
                return entity instanceof IMob;
            case MODE_PASSIVE:
                return entity instanceof IAnimals && !(entity instanceof IMob);
            case MODE_MOBS:
                return entity instanceof IAnimals;
            case MODE_ALL:
                if (entity instanceof PlayerEntity) {
                    return isPlayerAffected((PlayerEntity) entity);
                } else {
                    return true;
                }
        }
        return false;
    }

    public boolean isPlayerAffected(PlayerEntity player) {
        if (mode == EnvironmentalMode.MODE_WHITELIST) {
            return players.contains(player.getName());
        } else if (mode == EnvironmentalMode.MODE_BLACKLIST) {
            return !players.contains(player.getName());
        } else {
            return mode == EnvironmentalMode.MODE_ALL;
        }
    }

    private List<String> getPlayersAsList() {
        return new ArrayList<>(players);
    }

    private void addPlayer(String player) {
        if (!players.contains(player)) {
            players.add(player);
            markDirtyClient();
        }
    }

    private void delPlayer(String player) {
        if (players.contains(player)) {
            players.remove(player);
            markDirtyClient();
        }
    }

    public boolean isActive() {
        return active;
    }

    public int getTotalRfPerTick() {
        if (environmentModules == null) {
            getEnvironmentModules();
        }
        int rfNeeded = (int) (totalRfPerTick * getPowerMultiplier() * (4.0f - getInfusedFactor()) / 4.0f);
        if (environmentModules.isEmpty()) {
            return rfNeeded;
        }
        if (rfNeeded < EnvironmentalConfiguration.MIN_USAGE.get()) {
            rfNeeded = EnvironmentalConfiguration.MIN_USAGE.get();
        }
        return rfNeeded;
    }

    public int getVolume() {
        if (volume == -1) {
            volume = (int) ((radius * radius * Math.PI) * (maxy - miny + 1));
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
        markDirtyClient();
    }

    public int getMiny() {
        return miny;
    }

    public void setMiny(int miny) {
        this.miny = miny;
        volume = -1;
        environmentModules = null;
        markDirtyClient();
    }

    public int getMaxy() {
        return maxy;
    }

    public void setMaxy(int maxy) {
        this.maxy = maxy;
        volume = -1;
        environmentModules = null;
        markDirtyClient();
    }

    @Override
    public void update() {
        if (!getWorld().isRemote) {
            checkStateServer();
        }
    }

    private void checkStateServer() {
        if (powerTimeout > 0) {
            powerTimeout--;
            return;
        }

        long rf = getStoredPower();
        if (!isMachineEnabled()) {
            rf = 0;
        }

        getEnvironmentModules();

        int rfNeeded = getTotalRfPerTick();
        if (rfNeeded > rf || environmentModules.isEmpty()) {
            deactivate();
            powerTimeout = 20;
        } else {
            consumeEnergy(rfNeeded);
            for (EnvironmentModule module : environmentModules) {
                module.activate(true);
                module.tick(getWorld(), getPos(), radius, miny, maxy, this);
            }
            if (!active) {
                active = true;
                markDirtyClient();
            }
        }
    }

    public void deactivate() {
        for (EnvironmentModule module : environmentModules) {
            module.activate(false);
        }
        if (active) {
            active = false;
            markDirtyClient();
        }
    }

    private Object[] setRedstoneMode(String mode) {
        RedstoneMode redstoneMode = RedstoneMode.getMode(mode);
        if (redstoneMode == null) {
            throw new IllegalArgumentException("Not a valid mode");
        }
        setRSMode(redstoneMode);
        return null;
    }

    @Override
    public void setPowerInput(int powered) {
        if (powerLevel != powered) {
            powerTimeout = 0;
        }
        super.setPowerInput(powered);
    }

    // This is called server side.
    public List<EnvironmentModule> getEnvironmentModules() {
        if (environmentModules == null) {
            int volume = getVolume();
            totalRfPerTick = 0;
            environmentModules = new ArrayList<>();
            for (int i = 0; i < inventoryHelper.getCount(); i++) {
                ItemStack itemStack = inventoryHelper.getStackInSlot(i);
                if (!itemStack.isEmpty() && itemStack.getItem() instanceof EnvModuleProvider) {
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
    public int[] getSlotsForFace(Direction side) {
        return CONTAINER_FACTORY.getAccessibleSlots();
    }

    @Override
    public boolean canExtractItem(int index, ItemStack stack, Direction direction) {
        return CONTAINER_FACTORY.isOutputSlot(index);
    }

    @Override
    public boolean canInsertItem(int index, ItemStack itemStackIn, Direction direction) {
        return CONTAINER_FACTORY.isInputSlot(index);
    }

    @Override
    public ItemStack decrStackSize(int index, int amount) {
        environmentModules = null;
        return inventoryHelper.decrStackSize(index, amount);
    }

    @Override
    public void setInventorySlotContents(int index, ItemStack stack) {
        inventoryHelper.setInventorySlotContents(getInventoryStackLimit(), index, stack);
        environmentModules = null;
    }

    @Override
    public int getInventoryStackLimit() {
        return 1;
    }

    @Override
    public boolean isUsableByPlayer(PlayerEntity player) {
        return canPlayerAccess(player);
    }

    @Override
    public InventoryHelper getInventoryHelper() {
        return inventoryHelper;
    }

    @Override
    public boolean isItemValidForSlot(int index, ItemStack stack) {
        return true;
    }

    @Override
    public void readFromNBT(CompoundNBT tagCompound) {
        super.readFromNBT(tagCompound);
        totalRfPerTick = tagCompound.getInteger("rfPerTick");
        active = tagCompound.getBoolean("active");
    }

    @Override
    public void readRestorableFromNBT(CompoundNBT tagCompound) {
        super.readRestorableFromNBT(tagCompound);
        readBufferFromNBT(tagCompound, inventoryHelper);
        radius = tagCompound.getInteger("radius");
        miny = tagCompound.getInteger("miny");
        maxy = tagCompound.getInteger("maxy");
        volume = -1;

        // Compatibility
        if (tagCompound.hasKey("whitelist")) {
            boolean wl = tagCompound.getBoolean("whitelist");
            mode = wl ? EnvironmentalMode.MODE_WHITELIST : EnvironmentalMode.MODE_BLACKLIST;
        } else {
            int m = tagCompound.getInteger("mode");
            mode = EnvironmentalMode.values()[m];
        }

        players.clear();
        NBTTagList playerList = tagCompound.getTagList("players", Constants.NBT.TAG_STRING);
        if (playerList != null) {
            for (int i = 0; i < playerList.tagCount(); i++) {
                String player = playerList.getStringTagAt(i);
                players.add(player);
            }
        }
    }

    @Override
    public CompoundNBT writeToNBT(CompoundNBT tagCompound) {
        super.writeToNBT(tagCompound);
        tagCompound.setInteger("rfPerTick", totalRfPerTick);
        tagCompound.setBoolean("active", active);
        return tagCompound;
    }

    @Override
    public void writeRestorableToNBT(CompoundNBT tagCompound) {
        super.writeRestorableToNBT(tagCompound);
        writeBufferToNBT(tagCompound, inventoryHelper);
        tagCompound.setInteger("radius", radius);
        tagCompound.setInteger("miny", miny);
        tagCompound.setInteger("maxy", maxy);

        tagCompound.setInteger("mode", mode.ordinal());

        NBTTagList playerTagList = new NBTTagList();
        for (String player : players) {
            playerTagList.appendTag(new NBTTagString(player));
        }
        tagCompound.setTag("players", playerTagList);
    }

    @Override
    public boolean execute(EntityPlayerMP playerMP, String command, TypedMap params) {
        boolean rc = super.execute(playerMP, command, params);
        if (rc) {
            return true;
        }
        if (CMD_ADDPLAYER.equals(command)) {
            addPlayer(params.get(PARAM_NAME));
            return true;
        } else if (CMD_DELPLAYER.equals(command)) {
            delPlayer(params.get(PARAM_NAME));
            return true;
        } else if (CMD_RSMODE.equals(command)) {
            setRSMode(RedstoneMode.values()[params.get(ImageChoiceLabel.PARAM_CHOICE_IDX)]);
            return true;
        } else if (CMD_SETRADIUS.equals(command)) {
            setRadius(params.get(ScrollableLabel.PARAM_VALUE));
            return true;
        } else if (CMD_SETMODE.equals(command)) {
            setMode(EnvironmentalMode.values()[params.get(PARAM_MODE)]);
            return true;
        } else if (CMD_SETBOUNDS.equals(command)) {
            setMiny(params.get(PARAM_MIN));
            setMaxy(params.get(PARAM_MAX));
            return true;
        }
        return false;
    }

    @Nonnull
    @Override
    public <T> List<T> executeWithResultList(String command, TypedMap args, Type<T> type) {
        List<T> rc = super.executeWithResultList(command, args, type);
        if (!rc.isEmpty()) {
            return rc;
        }
        if (CMD_GETPLAYERS.equals(command)) {
            return type.convert(getPlayersAsList());
        }
        return Collections.emptyList();
    }

    @Override
    public <T> boolean receiveListFromServer(String command, List<T> list, Type<T> type) {
        boolean rc = super.receiveListFromServer(command, list, type);
        if (rc) {
            return true;
        }
        if (CLIENTCMD_GETPLAYERS.equals(command)) {
            players = new HashSet<>(Type.STRING.convert(list));
            return true;
        }
        return false;
    }

    @Override
    public boolean shouldRenderInPass(int pass) {
        return pass == 1;
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    @Override
    public void onBlockBreak(World world, BlockPos pos, BlockState state) {
        deactivate();
    }

    @Override
    @Optional.Method(modid = "theoneprobe")
    public void addProbeInfo(ProbeMode mode, IProbeInfo probeInfo, PlayerEntity player, World world, BlockState blockState, IProbeHitData data) {
        super.addProbeInfo(mode, probeInfo, player, world, blockState, data);
        int rfPerTick = getTotalRfPerTick();
        int volume = getVolume();
        if (isActive()) {
            probeInfo.text(TextFormatting.GREEN + "Active " + rfPerTick + " RF/tick (" + volume + " blocks)");
        } else {
            probeInfo.text(TextFormatting.GREEN + "Inactive (" + volume + " blocks)");
        }
        int radius = getRadius();
        int miny = getMiny();
        int maxy = getMaxy();
        probeInfo.text(TextFormatting.GREEN + "Area: radius " + radius + " (between " + miny + " and " + maxy + ")");
    }

    @SideOnly(Side.CLIENT)
    @Override
    @Optional.Method(modid = "waila")
    public void addWailaBody(ItemStack itemStack, List<String> currenttip, IWailaDataAccessor accessor, IWailaConfigHandler config) {
        super.addWailaBody(itemStack, currenttip, accessor, config);
        CompoundNBT tagCompound = accessor.getNBTData();
        if (tagCompound != null) {
            int rfPerTick = getTotalRfPerTick();
            int volume = getVolume();
            if (isActive()) {
                currenttip.add(TextFormatting.GREEN + "Active " + rfPerTick + " RF/tick (" + volume + " blocks)");
            } else {
                currenttip.add(TextFormatting.GREEN + "Inactive (" + volume + " blocks)");
            }
            int radius = getRadius();
            int miny = getMiny();
            int maxy = getMaxy();
            currenttip.add(TextFormatting.GREEN + "Area: radius " + radius + " (between " + miny + " and " + maxy + ")");
        }
    }


}
