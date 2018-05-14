package mcjty.rftools.blocks.screens;

import mcjty.lib.container.DefaultSidedInventory;
import mcjty.lib.container.InventoryHelper;
import mcjty.lib.bindings.DefaultValue;
import mcjty.lib.tileentity.GenericTileEntity;
import mcjty.lib.bindings.IValue;
import mcjty.lib.network.PacketServerCommandTyped;
import mcjty.lib.typed.Key;
import mcjty.lib.typed.Type;
import mcjty.lib.typed.TypedMap;
import mcjty.lib.varia.GlobalCoordinate;
import mcjty.lib.varia.Logging;
import mcjty.rftools.api.screens.*;
import mcjty.rftools.api.screens.data.*;
import mcjty.rftools.blocks.screens.data.ModuleDataBoolean;
import mcjty.rftools.blocks.screens.data.ModuleDataInteger;
import mcjty.rftools.blocks.screens.data.ModuleDataString;
import mcjty.rftools.blocks.screens.modules.ComputerScreenModule;
import mcjty.rftools.blocks.screens.modules.ScreenModuleHelper;
import mcjty.rftools.blocks.screens.modulesclient.TextClientScreenModule;
import mcjty.rftools.network.RFToolsMessages;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import java.util.*;

public class ScreenTileEntity extends GenericTileEntity implements ITickable, DefaultSidedInventory {

    public static final String CMD_SCREEN_INFO = "getScreenInfo";
    public static final String CLIENTCMD_SCREEN_INFO = "getScreenInfo";
    public static final Key<List<String>> PARAM_INFO = new Key<>("info", Type.STRING_LIST);

    // Client side data for CMD_SCREEN_INFO
    public static List<String> infoReceived = Collections.emptyList();

    public static final String CMD_CLICK = "screen.click";
    public static final String CMD_HOVER = "screen.hover";
    public static final String CMD_SETTRUETYPE = "screen.setTruetype";

    public static final Key<Integer> PARAM_X = new Key<>("x", Type.INTEGER);
    public static final Key<Integer> PARAM_Y = new Key<>("y", Type.INTEGER);
    public static final Key<Integer> PARAM_MODULE = new Key<>("module", Type.INTEGER);
    public static final Key<Integer> PARAM_TRUETYPE = new Key<>("truetype", Type.INTEGER);

    public static Key<Boolean> VALUE_BRIGHT = new Key<>("bright", Type.BOOLEAN);

    @Override
    public IValue[] getValues() {
        return new IValue[] {
                new DefaultValue<>(VALUE_BRIGHT, ScreenTileEntity::isBright, ScreenTileEntity::setBright),
        };
    }

    private InventoryHelper inventoryHelper = new InventoryHelper(this, ScreenContainer.factory, ScreenContainer.SCREEN_MODULES);

    // This is a map that contains a map from the coordinate of the screen to a map of screen data from the server indexed by slot number,
    public static Map<GlobalCoordinate, Map<Integer, IModuleData>> screenData = new HashMap<>();

    // Cached client screen modules
    private List<IClientScreenModule> clientScreenModules = null;

    // A list of tags linked to computer modules.
    private final Map<String,List<ComputerScreenModule>> computerModules = new HashMap<>();

    private boolean needsServerData = false;
    private boolean showHelp = true;
    private boolean powerOn = false;        // True if screen is powered.
    private boolean connected = false;      // True if screen is connected to a controller.
    private int size = 0;                   // Size of screen (0 is normal, 1 is large, 2 is huge)
    private boolean transparent = false;    // Transparent screen.
    private int color = 0;                  // Color of the screen.
    private boolean bright = false;         // True if the screen contents is full bright

    private int trueTypeMode = 0;           // 0 is default, -1 is disabled, 1 is truetype

    // Sever side, the module we are hovering over
    private int hoveringModule = -1;
    private int hoveringX = -1;
    private int hoveringY = -1;


    public static final int SIZE_NORMAL = 0;
    public static final int SIZE_LARGE = 1;
    public static final int SIZE_HUGE = 2;

    // Cached server screen modules
    private List<IScreenModule> screenModules = null;
    private List<ActivatedModule> clickedModules = new ArrayList<>();

    private static class ActivatedModule {
        private int module;
        private int ticks;
        private int x;
        private int y;

        public ActivatedModule(int module, int ticks, int x, int y) {
            this.module = module;
            this.ticks = ticks;
            this.x = x;
            this.y = y;
        }
    }

    private int totalRfPerTick = 0;     // The total rf per tick for all modules.
    private boolean controllerNeededInCreative = false; // If any of this screen's modules use the screen controller, thus requiring one even for creative screens.

    public long lastTime = 0;

    public ScreenTileEntity() {
    }

    @Override
    protected boolean needsCustomInvWrapper() {
        return true;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public AxisAlignedBB getRenderBoundingBox() {
        int xCoord = getPos().getX();
        int yCoord = getPos().getY();
        int zCoord = getPos().getZ();
        return new AxisAlignedBB(xCoord - size - 1, yCoord - size - 1, zCoord - size - 1, xCoord + size + 1, yCoord + size + 1, zCoord + size + 1); // TODO see if we can shrink this
    }

    @Override
    public void update() {
        if (getWorld().isRemote) {
            checkStateClient();
        } else {
            checkStateServer();
        }
    }

    private void checkStateClient() {
        if (clickedModules.isEmpty()) {
            return;
        }
        List<ActivatedModule> newClickedModules = new ArrayList<>();
        for (ActivatedModule cm : clickedModules) {
            cm.ticks--;
            if (cm.ticks > 0) {
                newClickedModules.add(cm);
            } else {
                List<IClientScreenModule> modules = getClientScreenModules();
                if (cm.module < modules.size()) {
                    modules.get(cm.module).mouseClick(getWorld(), cm.x, cm.y, false);
                }
            }
        }
        clickedModules = newClickedModules;
    }

    private void checkStateServer() {
        if (clickedModules.isEmpty()) {
            return;
        }
        List<ActivatedModule> newClickedModules = new ArrayList<>();
        for (ActivatedModule cm : clickedModules) {
            cm.ticks--;
            if (cm.ticks > 0) {
                newClickedModules.add(cm);
            } else {
                List<IScreenModule> modules = getScreenModules();
                if (cm.module < modules.size()) {
                    ItemStack itemStack = inventoryHelper.getStackInSlot(cm.module);
                    IScreenModule module = modules.get(cm.module);
                    module.mouseClick(getWorld(), cm.x, cm.y, false, null);
                    if (module instanceof IScreenModuleUpdater) {
                        NBTTagCompound newCompound = ((IScreenModuleUpdater) module).update(itemStack.getTagCompound(), getWorld(), null);
                        if (newCompound != null) {
                            itemStack.setTagCompound(newCompound);
                            markDirtyClient();
                        }
                    }
                }
            }
        }
        clickedModules = newClickedModules;
    }

    @Override
    public int[] getSlotsForFace(EnumFacing side) {
        return ScreenContainer.factory.getAccessibleSlots();
    }

    @Override
    public boolean canExtractItem(int index, ItemStack stack, EnumFacing direction) {
        return ScreenContainer.factory.isOutputSlot(index);
    }

    @Override
    public boolean canInsertItem(int index, ItemStack itemStackIn, EnumFacing direction) {
        return ScreenContainer.factory.isInputSlot(index);
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
        resetModules();
        return inventoryHelper.decrStackSize(index, amount);
    }

    private void resetModules() {
        clientScreenModules = null;
        screenModules = null;
        clickedModules.clear();
        showHelp = true;
        computerModules.clear();
    }

    public static class ModuleRaytraceResult {
        private final int x;
        private final int y;
        private final int currenty;
        private final int moduleIndex;

        public ModuleRaytraceResult(int moduleIndex, int x, int y, int currenty) {
            this.moduleIndex = moduleIndex;
            this.x = x;
            this.y = y;
            this.currenty = currenty;
        }

        public int getModuleIndex() {
            return moduleIndex;
        }

        public int getX() {
            return x;
        }

        public int getY() {
            return y;
        }

        public int getCurrenty() {
            return currenty;
        }
    }

    private boolean isActivated(int index) {
        for (ActivatedModule module : clickedModules) {
            if (module.module == index) {
                return true;
            }
        }
        return false;
    }

    public void focusModuleClient(double hitX, double hitY, double hitZ, EnumFacing side, EnumFacing horizontalFacing) {
        ModuleRaytraceResult result = getHitModule(hitX, hitY, hitZ, side, horizontalFacing);
        if (result == null) {
            RFToolsMessages.INSTANCE.sendToServer(new PacketServerCommandTyped(getPos(), CMD_HOVER,
                    TypedMap.builder()
                            .put(PARAM_X, -1)
                            .put(PARAM_Y, -1)
                            .put(PARAM_MODULE, -1)
                            .build()));
            return;
        }

        RFToolsMessages.INSTANCE.sendToServer(new PacketServerCommandTyped(getPos(), CMD_HOVER,
                TypedMap.builder()
                        .put(PARAM_X, result.getX())
                        .put(PARAM_Y, result.getY() - result.getCurrenty())
                        .put(PARAM_MODULE, result.getModuleIndex())
                        .build()));
    }

    public void hitScreenClient(double hitX, double hitY, double hitZ, EnumFacing side, EnumFacing horizontalFacing) {
        ModuleRaytraceResult result = getHitModule(hitX, hitY, hitZ, side, horizontalFacing);
        if (result == null) {
            return;
        }

        List<IClientScreenModule> modules = getClientScreenModules();
        int module = result.getModuleIndex();
        if (isActivated(module)) {
            // We are getting a hit twice. Module is already activated. Do nothing
            return;
        }
        modules.get(module).mouseClick(getWorld(), result.getX(), result.getY() - result.getCurrenty(), true);
        clickedModules.add(new ActivatedModule(module, 3, result.getX(), result.getY()));

        RFToolsMessages.INSTANCE.sendToServer(new PacketServerCommandTyped(getPos(), CMD_CLICK,
                TypedMap.builder()
                        .put(PARAM_X, result.getX())
                        .put(PARAM_Y, result.getY() - result.getCurrenty())
                        .put(PARAM_MODULE, module)
                        .build()));
    }

    public ModuleRaytraceResult getHitModule(double hitX, double hitY, double hitZ, EnumFacing side, EnumFacing horizontalFacing) {
        ModuleRaytraceResult result;
        float factor = size+1.0f;
        float dx = 0, dy = 0;
        switch (side) {
            case NORTH:
                dx = (float) ((1.0-hitX) / factor);
                dy = (float) ((1.0-hitY) / factor);
                break;
            case SOUTH:
                dx = (float) (hitX / factor);
                dy = (float) ((1.0-hitY) / factor);
                break;
            case WEST:
                dx = (float) (hitZ / factor);
                dy = (float) ((1.0-hitY) / factor);
                break;
            case EAST:
                dx = (float) ((1.0-hitZ) / factor);
                dy = (float) ((1.0-hitY) / factor);
                break;
            case UP:
                switch(horizontalFacing) {
                    case NORTH:
                        dx = (float) ((1.0-hitX) / factor);
                        dy = (float) ((1.0-hitZ) / factor);
                        break;
                    case SOUTH:
                        dx = (float) (hitX / factor);
                        dy = (float) (hitZ / factor);
                        break;
                    case WEST:
                        dx = (float) (hitZ / factor);
                        dy = (float) ((1.0-hitX) / factor);
                        break;
                    case EAST:
                        dx = (float) ((1.0-hitZ) / factor);
                        dy = (float) (hitX / factor);
                }
                break;
            case DOWN:
                switch(horizontalFacing) {
                    case NORTH:
                        dx = (float) ((1.0-hitX) / factor);
                        dy = (float) (hitZ / factor);
                        break;
                    case SOUTH:
                        dx = (float) (hitX / factor);
                        dy = (float) ((1.0-hitZ) / factor);
                        break;
                    case WEST:
                        dx = (float) (hitZ / factor);
                        dy = (float) (hitX / factor);
                        break;
                    case EAST:
                        dx = (float) ((1.0-hitZ) / factor);
                        dy = (float) ((1.0-hitX) / factor);
                }
                break;
            default:
                return null;
        }
        int x = (int) (dx * 128);
        int y = (int) (dy * 128);
        int currenty = 7;

        int moduleIndex = 0;
        List<IClientScreenModule> clientScreenModules = getClientScreenModules();
        for (IClientScreenModule module : clientScreenModules) {
            if (module != null) {
                int height = module.getHeight();
                // Check if this module has enough room
                if (currenty + height <= 124) {
                    if (currenty <= y && y < (currenty + height)) {
                        break;
                    }
                    currenty += height;
                }
            }
            moduleIndex++;
        }
        if (moduleIndex >= clientScreenModules.size()) {
            return null;
        }
        result = new ModuleRaytraceResult(moduleIndex, x, y, currenty);
        return result;
    }

    private void hitScreenServer(EntityPlayer player, int x, int y, int module) {
        List<IScreenModule> screenModules = getScreenModules();
        IScreenModule screenModule = screenModules.get(module);
        if (screenModule != null) {
            ItemStack itemStack = inventoryHelper.getStackInSlot(module);
            screenModule.mouseClick(getWorld(), x, y, true, player);
            if (screenModule instanceof IScreenModuleUpdater) {
                NBTTagCompound newCompound = ((IScreenModuleUpdater) screenModule).update(itemStack.getTagCompound(), getWorld(), player);
                if (newCompound != null) {
                    itemStack.setTagCompound(newCompound);
                    markDirtyClient();
                }
            }
            clickedModules.add(new ActivatedModule(module, 5, x, y));
        }
    }

    @Override
    public InventoryHelper getInventoryHelper() {
        return inventoryHelper;
    }

    @Override
    public void setInventorySlotContents(int index, ItemStack stack) {
        inventoryHelper.setInventorySlotContents(getInventoryStackLimit(), index, stack);
        resetModules();
    }

    @Override
    public int getInventoryStackLimit() {
        return 1;
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    @Override
    public boolean isUsableByPlayer(EntityPlayer player) {
        return canPlayerAccess(player);
    }

    @Override
    public void readFromNBT(NBTTagCompound tagCompound) {
        super.readFromNBT(tagCompound);
        powerOn = tagCompound.getBoolean("powerOn");
        connected = tagCompound.getBoolean("connected");
        totalRfPerTick = tagCompound.getInteger("rfPerTick");
        controllerNeededInCreative = tagCompound.getBoolean("controllerNeededInCreative");
    }

    @Override
    public void readRestorableFromNBT(NBTTagCompound tagCompound) {
        super.readRestorableFromNBT(tagCompound);
        readBufferFromNBT(tagCompound, inventoryHelper);
        resetModules();
        if (tagCompound.hasKey("large")) {
            size = tagCompound.getBoolean("large") ? 1 : 0;
        } else {
            size = tagCompound.getInteger("size");
        }
        transparent = tagCompound.getBoolean("transparent");
        color = tagCompound.getInteger("color");
        bright = tagCompound.getBoolean("bright");
        trueTypeMode = tagCompound.getInteger("truetype");
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound tagCompound) {
        super.writeToNBT(tagCompound);
        tagCompound.setBoolean("powerOn", powerOn);
        tagCompound.setBoolean("connected", connected);
        tagCompound.setInteger("rfPerTick", totalRfPerTick);
        tagCompound.setBoolean("controllerNeededInCreative", controllerNeededInCreative);
        return tagCompound;
    }

    @Override
    public void writeRestorableToNBT(NBTTagCompound tagCompound) {
        super.writeRestorableToNBT(tagCompound);
        writeBufferToNBT(tagCompound, inventoryHelper);
        tagCompound.setInteger("size", size);
        tagCompound.setBoolean("transparent", transparent);
        tagCompound.setInteger("color", color);
        tagCompound.setBoolean("bright", bright);
        tagCompound.setInteger("truetype", trueTypeMode);
    }

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
        markDirtyClient();
    }

    public void setSize(int size) {
        this.size = size;
        markDirtyClient();
    }

    public boolean isBright() {
        return bright;
    }

    public void setBright(boolean bright) {
        this.bright = bright;
        markDirtyClient();
    }

    public int getTrueTypeMode() {
        return trueTypeMode;
    }

    public void setTrueTypeMode(int trueTypeMode) {
        this.trueTypeMode = trueTypeMode;
        markDirtyClient();
    }

    public void setTransparent(boolean transparent) {
        this.transparent = transparent;
        markDirtyClient();
    }

    public int getSize() {
        return size;
    }

    public boolean isTransparent() {
        return transparent;
    }

    public void setPower(boolean power) {
        if (powerOn == power) {
            return;
        }
        powerOn = power;
        markDirtyClient();
    }

    public boolean isPowerOn() {
        return powerOn;
    }

    public boolean isRenderable() {
        if (powerOn) {
            return true;
        }
        if (isShowHelp()) {
            return true;    // True because then we give help
        }
        return isCreative();
    }

    protected boolean isCreative() {
        return false;
    }

    public void setConnected(boolean c) {
        if (connected == c) {
            return;
        }
        connected = c;
        markDirtyClient();
    }

    public boolean isConnected() {
        return connected;
    }

    public void updateModuleData(int slot, NBTTagCompound tagCompound) {
        ItemStack stack = inventoryHelper.getStackInSlot(slot);
        stack.setTagCompound(tagCompound);
        screenModules = null;
        clientScreenModules = null;
        computerModules.clear();
        markDirty();
    }

    private static List<IClientScreenModule> helpingScreenModules = null;

    public static List<IClientScreenModule> getHelpingScreenModules() {
        if (helpingScreenModules == null) {
            helpingScreenModules = new ArrayList<>();
            addLine("Read me", 0x7799ff, true);
            addLine("", 0xffffff, false);
            addLine("Sneak-right click for", 0xffffff, false);
            addLine("GUI and insertion of", 0xffffff, false);
            addLine("modules", 0xffffff, false);
            addLine("", 0xffffff, false);
            addLine("Use Screen Controller", 0xffffff, false);
            addLine("to power screens", 0xffffff, false);
            addLine("remotely", 0xffffff, false);
        }
        return helpingScreenModules;
    }

    private static void addLine(String s, int color, boolean large) {
        TextClientScreenModule t1 = new TextClientScreenModule();
        t1.setLine(s);
        t1.setColor(color);
        t1.setLarge(large);
        helpingScreenModules.add(t1);
    }


    // This is called client side.
    public List<IClientScreenModule> getClientScreenModules() {
        if (clientScreenModules == null) {
            needsServerData = false;
            showHelp = true;
            clientScreenModules = new ArrayList<>();
            for (int i = 0 ; i < inventoryHelper.getCount() ; i++) {
                ItemStack itemStack = inventoryHelper.getStackInSlot(i);
                if (!itemStack.isEmpty() && ScreenBlock.hasModuleProvider(itemStack)) {
                    IModuleProvider moduleProvider = ScreenBlock.getModuleProvider(itemStack);
                    IClientScreenModule clientScreenModule;
                    try {
                        clientScreenModule = moduleProvider.getClientScreenModule().newInstance();
                    } catch (InstantiationException e) {
                        Logging.logError("Internal error with screen modules!", e);
                        continue;
                    } catch (IllegalAccessException e) {
                        Logging.logError("Internal error with screen modules!", e);
                        continue;
                    }
                    clientScreenModule.setupFromNBT(itemStack.getTagCompound(), getWorld().provider.getDimension(), getPos());
                    clientScreenModules.add(clientScreenModule);
                    if (clientScreenModule.needsServerData()) {
                        needsServerData = true;
                    }
                    showHelp = false;
                } else {
                    clientScreenModules.add(null);        // To keep the indexing correct so that the modules correspond with there slot number.
                }
            }
        }
        return clientScreenModules;
    }

    // Called client side only
    public boolean isShowHelp() {
        return showHelp;
    }

    public boolean isNeedsServerData() {
        return needsServerData;
    }

    public int getTotalRfPerTick() {
        if (isCreative()) return 0;
        if (screenModules == null) {
            getScreenModules();
        }
        return totalRfPerTick;
    }

    public boolean isControllerNeeded() {
        if (!isCreative()) return true;
        if (screenModules == null) {
            getScreenModules();
        }
        return controllerNeededInCreative;
    }

    // This is called server side.
    public List<IScreenModule> getScreenModules() {
        if (screenModules == null) {
            totalRfPerTick = 0;
            controllerNeededInCreative = false;
            screenModules = new ArrayList<>();
            for (int i = 0 ; i < inventoryHelper.getCount() ; i++) {
                ItemStack itemStack = inventoryHelper.getStackInSlot(i);
                if (!itemStack.isEmpty() && ScreenBlock.hasModuleProvider(itemStack)) {
                    IModuleProvider moduleProvider = ScreenBlock.getModuleProvider(itemStack);
                    IScreenModule screenModule;
                    try {
                        screenModule = moduleProvider.getServerScreenModule().newInstance();
                    } catch (InstantiationException e) {
                        Logging.logError("Internal error with screen modules!", e);
                        continue;
                    } catch (IllegalAccessException e) {
                        Logging.logError("Internal error with screen modules!", e);
                        continue;
                    }
                    screenModule.setupFromNBT(itemStack.getTagCompound(), getWorld().provider.getDimension(), getPos());
                    screenModules.add(screenModule);
                    totalRfPerTick += screenModule.getRfPerTick();
                    if(screenModule.needsController()) controllerNeededInCreative = true;

                    if (screenModule instanceof ComputerScreenModule) {
                        ComputerScreenModule computerScreenModule = (ComputerScreenModule) screenModule;
                        String tag = computerScreenModule.getTag();
                        if (!computerModules.containsKey(tag)) {
                            computerModules.put(tag, new ArrayList<ComputerScreenModule>());
                        }
                        computerModules.get(tag).add(computerScreenModule);
                    }
                } else {
                    screenModules.add(null);        // To keep the indexing correct so that the modules correspond with there slot number.
                }
            }

        }
        return screenModules;
    }

    public List<ComputerScreenModule> getComputerModules(String tag) {
        return computerModules.get(tag);
    }

    public Set<String> getTags() {
        return computerModules.keySet();
    }

    private IScreenDataHelper screenDataHelper = new IScreenDataHelper() {
        @Override
        public IModuleDataInteger createInteger(int i) {
            return new ModuleDataInteger(i);
        }

        @Override
        public IModuleDataBoolean createBoolean(boolean b) {
            return new ModuleDataBoolean(b);
        }

        @Override
        public IModuleDataString createString(String b) {
            return new ModuleDataString(b);
        }

        @Override
        public IModuleDataContents createContents(long contents, long maxContents, long lastPerTick) {
            return new ScreenModuleHelper.ModuleDataContents(contents, maxContents, lastPerTick);
        }
    };

    // This is called server side.
    public Map<Integer, IModuleData> getScreenData(long millis) {
        Map<Integer, IModuleData> map = new HashMap<>();
        List<IScreenModule> screenModules = getScreenModules();
        int moduleIndex = 0;
        for (IScreenModule module : screenModules) {
            if (module != null) {
                IModuleData data = module.getData(screenDataHelper, getWorld(), millis);
                if (data != null) {
                    map.put(moduleIndex, data);
                }
            }
            moduleIndex++;
        }
        return map;
    }

    public IScreenModule getHoveringModule() {
        if (hoveringModule == -1) {
            return null;
        }
        getScreenModules();
        if (hoveringModule >= 0 && hoveringModule < screenModules.size()) {
            return screenModules.get(hoveringModule);
        }
        return null;
    }

    public int getHoveringX() {
        return hoveringX;
    }

    public int getHoveringY() {
        return hoveringY;
    }

    @Override
    public boolean execute(EntityPlayerMP playerMP, String command, TypedMap params) {
        boolean rc = super.execute(playerMP, command, params);
        if (rc) {
            return true;
        }
        if (CMD_CLICK.equals(command)) {
            int x = params.get(PARAM_X);
            int y = params.get(PARAM_Y);
            int module = params.get(PARAM_MODULE);
            hitScreenServer(playerMP, x, y, module);
            return true;
        } else if (CMD_HOVER.equals(command)) {
            hoveringX = params.get(PARAM_X);
            hoveringY = params.get(PARAM_Y);
            hoveringModule = params.get(PARAM_MODULE);
            return true;
        } else if (CMD_SETTRUETYPE.equals(command)) {
            int b = params.get(PARAM_TRUETYPE);
            setTrueTypeMode(b);
            return true;
        }
        return false;
    }

    @Override
    public TypedMap executeWithResult(String command, TypedMap args) {
        TypedMap rc = super.executeWithResult(command, args);
        if (rc != null) {
            return rc;
        }
        if (CMD_SCREEN_INFO.equals(command)) {
            IScreenModule module = getHoveringModule();
            List<String> info = Collections.emptyList();
            if (module instanceof ITooltipInfo) {
                info = ((ITooltipInfo) module).getInfo(world, getHoveringX(), getHoveringY());
            }
            return TypedMap.builder()
                    .put(PARAM_INFO, info)
                    .build();
        }
        return null;
    }

    @Override
    public boolean receiveDataFromServer(String command, @Nonnull TypedMap result) {
        boolean rc = super.receiveDataFromServer(command, result);
        if (rc) {
            return rc;
        }
        if (CLIENTCMD_SCREEN_INFO.equals(command)) {
            infoReceived = result.get(PARAM_INFO);
            return true;
        }
        return false;
    }
}
