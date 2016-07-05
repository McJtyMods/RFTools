package mcjty.rftools.blocks.screens;

import mcjty.lib.container.DefaultSidedInventory;
import mcjty.lib.container.InventoryHelper;
import mcjty.lib.entity.GenericTileEntity;
import mcjty.lib.network.Argument;
import mcjty.lib.network.PacketServerCommand;
import mcjty.lib.varia.GlobalCoordinate;
import mcjty.rftools.api.screens.*;
import mcjty.rftools.api.screens.data.*;
import mcjty.rftools.blocks.screens.data.ModuleDataBoolean;
import mcjty.rftools.blocks.screens.data.ModuleDataInteger;
import mcjty.rftools.blocks.screens.data.ModuleDataString;
import mcjty.rftools.blocks.screens.modules.ScreenModuleHelper;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ScreenTileEntity extends GenericTileEntity implements ITickable, DefaultSidedInventory {

    public static final String CMD_CLICK = "click";
    public static final String CMD_HOVER = "hover";

    private InventoryHelper inventoryHelper = new InventoryHelper(this, ScreenContainer.factory, ScreenContainer.SCREEN_MODULES);

    // This is a map that contains a map from the coordinate of the screen to a map of screen data from the server indexed by slot number,
    public static Map<GlobalCoordinate, Map<Integer, IModuleData>> screenData = new HashMap<>();

    // Cached client screen modules
    private List<IClientScreenModule> clientScreenModules = null;

    // A list of tags linked to computer modules.
//    private final Map<String,List<ComputerScreenModule>> computerModules = new HashMap<String, List<ComputerScreenModule>>();

    private boolean needsServerData = false;
    private boolean powerOn = false;        // True if screen is powered.
    private boolean connected = false;      // True if screen is connected to a controller.
    private int size = 0;                   // Size of screen (0 is normal, 1 is large, 2 is huge)
    private boolean transparent = false;    // Transparent screen.
    private int color = 0;                  // Color of the screen.

    // Sever side, the module we are hovering over
    private int hoveringModule = -1;
    private int hoveringX = -1;
    private int hoveringY = -1;


    public static final int SIZE_NORMAL = 0;
    public static final int SIZE_LARGE = 1;
    public static final int SIZE_HUGE = 2;

    // Cached server screen modules
    private List<IScreenModule> screenModules = null;
    private List<ActivatedModule> clickedModules = new ArrayList<ActivatedModule>();

    private static class ActivatedModule {
        int module;
        int ticks;
        int x;
        int y;

        public ActivatedModule(int module, int ticks, int x, int y) {
            this.module = module;
            this.ticks = ticks;
            this.x = x;
            this.y = y;
        }
    }

    private int totalRfPerTick = 0;     // The total rf per tick for all modules.

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
        return new AxisAlignedBB(xCoord - 1, yCoord - 1, zCoord - 1, xCoord + size + 1, yCoord + size + 1, zCoord + size + 1);
    }

    @Override
    public void update() {
        if (worldObj.isRemote) {
            checkStateClient();
        } else {
            checkStateServer();
        }
    }

    private void checkStateClient() {
        if (clickedModules.isEmpty()) {
            return;
        }
        List<ActivatedModule> newClickedModules = new ArrayList<ActivatedModule>();
        for (ActivatedModule cm : clickedModules) {
            cm.ticks--;
            if (cm.ticks > 0) {
                newClickedModules.add(cm);
            } else {
                List<IClientScreenModule> modules = getClientScreenModules();
                if (cm.module < modules.size()) {
                    modules.get(cm.module).mouseClick(worldObj, cm.x, cm.y, false);
                }
            }
        }
        clickedModules = newClickedModules;
    }

    private void checkStateServer() {
        if (clickedModules.isEmpty()) {
            return;
        }
        List<ActivatedModule> newClickedModules = new ArrayList<ActivatedModule>();
        for (ActivatedModule cm : clickedModules) {
            cm.ticks--;
            if (cm.ticks > 0) {
                newClickedModules.add(cm);
            } else {
                List<IScreenModule> modules = getScreenModules();
                if (cm.module < modules.size()) {
                    ItemStack itemStack = inventoryHelper.getStackInSlot(cm.module);
                    IScreenModule module = modules.get(cm.module);
                    module.mouseClick(worldObj, cm.x, cm.y, false, null);
                    if (module instanceof IScreenModuleUpdater) {
                        NBTTagCompound newCompound = ((IScreenModuleUpdater) module).update(itemStack.getTagCompound(), worldObj, null);
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
//        computerModules.clear();
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

    public void focusModuleClient(double hitX, double hitY, double hitZ, EnumFacing side) {
        ModuleRaytraceResult result = getHitModule(hitX, hitY, hitZ, side);
        if (result == null) {
            RFToolsMessages.INSTANCE.sendToServer(new PacketServerCommand(getPos(), CMD_HOVER,
                    new Argument("x", -1),
                    new Argument("y", -1),
                    new Argument("module", -1)));
            return;
        }

        RFToolsMessages.INSTANCE.sendToServer(new PacketServerCommand(getPos(), CMD_HOVER,
                new Argument("x", result.getX()),
                new Argument("y", result.getY() - result.getCurrenty()),
                new Argument("module", result.getModuleIndex())));
    }

    public void hitScreenClient(double hitX, double hitY, double hitZ, EnumFacing side) {
        ModuleRaytraceResult result = getHitModule(hitX, hitY, hitZ, side);
        if (result == null) {
            return;
        }

        List<IClientScreenModule> modules = getClientScreenModules();
        int module = result.getModuleIndex();
        if (isActivated(module)) {
            // We are getting a hit twice. Module is already activated. Do nothing
            return;
        }
        modules.get(module).mouseClick(worldObj, result.getX(), result.getY() - result.getCurrenty(), true);
        clickedModules.add(new ActivatedModule(module, 3, result.getX(), result.getY()));

        RFToolsMessages.INSTANCE.sendToServer(new PacketServerCommand(getPos(), CMD_CLICK,
                new Argument("x", result.getX()),
                new Argument("y", result.getY() - result.getCurrenty()),
                new Argument("module", module)));
    }

    public ModuleRaytraceResult getHitModule(double hitX, double hitY, double hitZ, EnumFacing side) {
        ModuleRaytraceResult result;
        float factor = size+1.0f;
        float dx = 0;
        float dy = (float) ((-hitY + 1.0) / factor);
        switch (side) {
            case NORTH:
                dx = (float) ((1.0-hitX) / factor);
                break;
            case SOUTH:
                dx = (float) (hitX / factor);
                break;
            case WEST:
                dx = (float) (hitZ / factor);
                break;
            case EAST:
                dx = (float) ((1.0 - hitZ) / factor);
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
            screenModule.mouseClick(worldObj, x, y, true, player);
            if (screenModule instanceof IScreenModuleUpdater) {
                NBTTagCompound newCompound = ((IScreenModuleUpdater) screenModule).update(itemStack.getTagCompound(), worldObj, player);
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
    public boolean isUseableByPlayer(EntityPlayer player) {
        return canPlayerAccess(player);
    }

    @Override
    public void readFromNBT(NBTTagCompound tagCompound) {
        super.readFromNBT(tagCompound);
        powerOn = tagCompound.getBoolean("powerOn");
        connected = tagCompound.getBoolean("connected");
        totalRfPerTick = tagCompound.getInteger("rfPerTick");
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
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound tagCompound) {
        super.writeToNBT(tagCompound);
        tagCompound.setBoolean("powerOn", powerOn);
        tagCompound.setBoolean("connected", connected);
        tagCompound.setInteger("rfPerTick", totalRfPerTick);
        return tagCompound;
    }

    @Override
    public void writeRestorableToNBT(NBTTagCompound tagCompound) {
        super.writeRestorableToNBT(tagCompound);
        writeBufferToNBT(tagCompound, inventoryHelper);
        tagCompound.setInteger("size", size);
        tagCompound.setBoolean("transparent", transparent);
        tagCompound.setInteger("color", color);
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
//        computerModules.clear();
        markDirty();
    }

    // This is called client side.
    public List<IClientScreenModule> getClientScreenModules() {
        if (clientScreenModules == null) {
            needsServerData = false;
            clientScreenModules = new ArrayList<>();
            for (int i = 0 ; i < inventoryHelper.getCount() ; i++) {
                ItemStack itemStack = inventoryHelper.getStackInSlot(i);
                if (itemStack != null && itemStack.getItem() instanceof IModuleProvider) {
                    IModuleProvider moduleProvider = (IModuleProvider) itemStack.getItem();
                    IClientScreenModule clientScreenModule;
                    try {
                        clientScreenModule = moduleProvider.getClientScreenModule().newInstance();
                    } catch (InstantiationException e) {
                        e.printStackTrace();
                        continue;
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                        continue;
                    }
                    clientScreenModule.setupFromNBT(itemStack.getTagCompound(), worldObj.provider.getDimension(), getPos());
                    clientScreenModules.add(clientScreenModule);
                    if (clientScreenModule.needsServerData()) {
                        needsServerData = true;
                    }
                } else {
                    clientScreenModules.add(null);        // To keep the indexing correct so that the modules correspond with there slot number.
                }
            }

        }
        return clientScreenModules;
    }

    public boolean isNeedsServerData() {
        return needsServerData;
    }

    public int getTotalRfPerTick() {
        if (screenModules == null) {
            getScreenModules();
        }
        return totalRfPerTick;
    }

    // This is called server side.
    public List<IScreenModule> getScreenModules() {
        if (screenModules == null) {
            totalRfPerTick = 0;
            screenModules = new ArrayList<IScreenModule>();
            for (int i = 0 ; i < inventoryHelper.getCount() ; i++) {
                ItemStack itemStack = inventoryHelper.getStackInSlot(i);
                if (itemStack != null && itemStack.getItem() instanceof IModuleProvider) {
                    IModuleProvider moduleProvider = (IModuleProvider) itemStack.getItem();
                    IScreenModule screenModule;
                    try {
                        screenModule = moduleProvider.getServerScreenModule().newInstance();
                    } catch (InstantiationException e) {
                        e.printStackTrace();
                        continue;
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                        continue;
                    }
                    screenModule.setupFromNBT(itemStack.getTagCompound(), worldObj.provider.getDimension(), getPos());
                    screenModules.add(screenModule);
                    totalRfPerTick += screenModule.getRfPerTick();

//                    if (screenModule instanceof ComputerScreenModule) {
//                        ComputerScreenModule computerScreenModule = (ComputerScreenModule) screenModule;
//                        String tag = computerScreenModule.getTag();
//                        if (!computerModules.containsKey(tag)) {
//                            computerModules.put(tag, new ArrayList<ComputerScreenModule>());
//                        }
//                        computerModules.get(tag).add(computerScreenModule);
//                    }
                } else {
                    screenModules.add(null);        // To keep the indexing correct so that the modules correspond with there slot number.
                }
            }

        }
        return screenModules;
    }

//    public List<ComputerScreenModule> getComputerModules(String tag) {
//        return computerModules.get(tag);
//    }
//
//    public Set<String> getTags() {
//        return computerModules.keySet();
//    }

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
                IModuleData data = module.getData(screenDataHelper, worldObj, millis);
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
    public boolean execute(EntityPlayerMP playerMP, String command, Map<String, Argument> args) {
        boolean rc = super.execute(playerMP, command, args);
        if (rc) {
            return true;
        }
        if (CMD_CLICK.equals(command)) {
            int x = args.get("x").getInteger();
            int y = args.get("y").getInteger();
            int module = args.get("module").getInteger();
            hitScreenServer(playerMP, x, y, module);
            return true;
        } else if (CMD_HOVER.equals(command)) {
            hoveringX = args.get("x").getInteger();
            hoveringY = args.get("y").getInteger();
            hoveringModule = args.get("module").getInteger();
            return true;
        }
        return false;
    }
}
