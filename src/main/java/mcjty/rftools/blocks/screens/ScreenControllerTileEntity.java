package mcjty.rftools.blocks.screens;

import li.cil.oc.api.machine.Arguments;
import li.cil.oc.api.machine.Callback;
import li.cil.oc.api.machine.Context;
import li.cil.oc.api.network.SimpleComponent;
import mcjty.lib.entity.DefaultAction;
import mcjty.lib.entity.GenericEnergyReceiverTileEntity;
import mcjty.lib.entity.IAction;
import mcjty.rftools.blocks.screens.modules.ComputerScreenModule;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.Optional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Optional.InterfaceList({
        @Optional.Interface(iface = "li.cil.oc.api.network.SimpleComponent", modid = "opencomputers"),
//        @Optional.Interface(iface = "dan200.computercraft.api.peripheral.IPeripheral", modid = "ComputerCraft"),
})
public class ScreenControllerTileEntity extends GenericEnergyReceiverTileEntity implements ITickable, SimpleComponent { // implements IPeripheral {

    public static final String ACTION_SCAN = "scan";
    public static final String ACTION_DETACH = "detach";

    @Override
    public IAction[] getActions() {
        return new IAction[] {
                new DefaultAction<>(ACTION_SCAN, ScreenControllerTileEntity::scan),
                new DefaultAction<>(ACTION_DETACH, ScreenControllerTileEntity::detach),
        };
    }

    public static final String COMPONENT_NAME = "screen_controller";

    private List<BlockPos> connectedScreens = new ArrayList<>();
    private int tickCounter = 20;

    public ScreenControllerTileEntity() {
        super(ScreenConfiguration.CONTROLLER_MAXENERGY, ScreenConfiguration.CONTROLLER_RECEIVEPERTICK);
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
//        return new String[] { "getScreenCount", "getScreenIndex", "getScreenCoordinate", "addText", "setText", "clearText" };
//    }
//
//    @Override
//    @Optional.Method(modid = "ComputerCraft")
//    public Object[] callMethod(IComputerAccess computer, ILuaContext context, int method, Object[] arguments) throws LuaException, InterruptedException {
//        switch (method) {
//            case 0: return new Object[] { connectedScreens.size() };
//            case 1: return getScreenIndex(new Coordinate(((Double) arguments[0]).intValue(), ((Double) arguments[1]).intValue(), ((Double) arguments[2]).intValue()));
//            case 2: Coordinate c = connectedScreens.get(((Double) arguments[0]).intValue()); return new Object[] { c.getX(), c.getY(), c.getZ() };
//            case 3: return addText((String) arguments[0], (String) arguments[1], ((Double) arguments[2]).intValue());
//            case 4: return setText((String) arguments[0], (String) arguments[1], ((Double) arguments[2]).intValue());
//            case 5: return clearText((String) arguments[0]);
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

    @Callback(doc = "Get the amount of screens controlled by this controller", getter = true)
    @Optional.Method(modid = "opencomputers")
    public Object[] getScreenCount(Context context, Arguments args) {
        return new Object[] { connectedScreens.size() };
    }

    @Callback(doc = "Get a table with coordinates (every coordinate is a table indexed with 'x', 'y', and 'z') for all connected screens", getter = true)
    @Optional.Method(modid = "opencomputers")
    public Object[] getScreens(Context context, Arguments args) {
        List<Map<String,Integer>> result = new ArrayList<>();
        for (BlockPos screen : connectedScreens) {
            Map<String,Integer> coordinate = new HashMap<>();
            coordinate.put("x", screen.getX());
            coordinate.put("y", screen.getY());
            coordinate.put("z", screen.getZ());
            result.add(coordinate);
        }

        return new Object[] { result };
    }

    @Callback(doc = "Given a screen coordinate (table indexed by 'x', 'y', and 'z') return the index of that screen", getter = true)
    @Optional.Method(modid = "opencomputers")
    public Object[] getScreenIndex(Context context, Arguments args) throws Exception {
        Map screen = args.checkTable(0);
        if (!screen.containsKey("x") || !screen.containsKey("y") || !screen.containsKey("z")) {
            throw new IllegalArgumentException("Screen map doesn't contain the right x,y,z coordinate!");
        }
        BlockPos recC = new BlockPos(((Double) screen.get("x")).intValue(), ((Double) screen.get("y")).intValue(), ((Double) screen.get("z")).intValue());
        return getScreenIndex(recC);
    }

    private Object[] getScreenIndex(BlockPos coordinate) {
        int i = 0;
        for (BlockPos connectedScreen : connectedScreens) {
            if (connectedScreen.equals(coordinate)) {
                return new Object[] { i };
            }
            i++;
        }

        return null;
    }

    @Callback(doc = "Given a screen index return the coordinate (table indexed by 'x', 'y', and 'z') of that screen", getter = true)
    @Optional.Method(modid = "opencomputers")
    public Object[] getScreenCoordinate(Context context, Arguments args) throws Exception {
        int index = args.checkInteger(0);
        if (index < 0 || index >= connectedScreens.size()) {
            throw new IllegalArgumentException("Screen index out of range!");
        }
        BlockPos screen = connectedScreens.get(index);
        Map<String,Integer> coordinate = new HashMap<>();
        coordinate.put("x", screen.getX());
        coordinate.put("y", screen.getY());
        coordinate.put("z", screen.getZ());

        return new Object[] { coordinate };
    }


    @Callback(doc = "Add text to all screens listening to the given 'tag'. Parameters are: 'tag', 'text' and 'color' (RGB value)")
    @Optional.Method(modid = "opencomputers")
    public Object[] addText(Context context, Arguments args) {
        String tag = args.checkString(0);
        String text = args.checkString(1);
        int color = args.checkInteger(2);

        return addText(tag, text, color);
    }

    @Callback(doc = "Set text to all screens listening to the given 'tag'. Parameters are: 'tag', 'text' and 'color' (RGB value)")
    @Optional.Method(modid = "opencomputers")
    public Object[] setText(Context context, Arguments args) {
        String tag = args.checkString(0);
        String text = args.checkString(1);
        int color = args.checkInteger(2);

        clearText(tag);
        return addText(tag, text, color);
    }

    private Object[] setText(String tag, String text, int color) {
        clearText(tag);
        return addText(tag, text, color);
    }

    private Object[] addText(String tag, String text, int color) {
        for (BlockPos screen : connectedScreens) {
            TileEntity te = getWorld().getTileEntity(screen);
            if (te instanceof ScreenTileEntity) {
                ScreenTileEntity screenTileEntity = (ScreenTileEntity) te;
                List<ComputerScreenModule> computerScreenModules = screenTileEntity.getComputerModules(tag);
                if (computerScreenModules != null) {
                    for (ComputerScreenModule screenModule : computerScreenModules) {
                        screenModule.addText(text, color);
                    }
                }
            }
        }
        return null;
    }

    @Callback(doc = "Clear text to all screens listening to the given 'tag'. The 'tag' is the only parameter")
    @Optional.Method(modid = "opencomputers")
    public Object[] clearText(Context context, Arguments args) {
        String tag = args.checkString(0);

        return clearText(tag);
    }

    private Object[] clearText(String tag) {
        for (BlockPos screen : connectedScreens) {
            TileEntity te = getWorld().getTileEntity(screen);
            if (te instanceof ScreenTileEntity) {
                ScreenTileEntity screenTileEntity = (ScreenTileEntity) te;
                List<ComputerScreenModule> computerScreenModules = screenTileEntity.getComputerModules(tag);
                if (computerScreenModules != null) {
                    for (ComputerScreenModule screenModule : computerScreenModules) {
                        screenModule.clearText();
                    }
                }
            }
        }
        return null;
    }

    @Callback(doc = "Get a table of all tags supported by all connected screens", getter = true)
    @Optional.Method(modid = "opencomputers")
    public Object[] getTags(Context context, Arguments args) {
        List<String> tags = new ArrayList<>();
        for (BlockPos screen : connectedScreens) {
            TileEntity te = getWorld().getTileEntity(screen);
            if (te instanceof ScreenTileEntity) {
                ScreenTileEntity screenTileEntity = (ScreenTileEntity) te;
                tags.addAll(screenTileEntity.getTags());
            }
        }
        return new Object[] { tags };
    }


    @Override
    public void readFromNBT(NBTTagCompound tagCompound) {
        super.readFromNBT(tagCompound);
        int[] xes = tagCompound.getIntArray("screensx");
        int[] yes = tagCompound.getIntArray("screensy");
        int[] zes = tagCompound.getIntArray("screensz");
        connectedScreens.clear();
        for (int i = 0 ; i < xes.length ; i++) {
            connectedScreens.add(new BlockPos(xes[i], yes[i], zes[i]));
        }
    }

    @Override
    public void readRestorableFromNBT(NBTTagCompound tagCompound) {
        super.readRestorableFromNBT(tagCompound);
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound tagCompound) {
        super.writeToNBT(tagCompound);
        int[] xes = new int[connectedScreens.size()];
        int[] yes = new int[connectedScreens.size()];
        int[] zes = new int[connectedScreens.size()];
        for (int i = 0 ; i < connectedScreens.size() ; i++) {
            BlockPos c = connectedScreens.get(i);
            xes[i] = c.getX();
            yes[i] = c.getY();
            zes[i] = c.getZ();
        }
        tagCompound.setIntArray("screensx", xes);
        tagCompound.setIntArray("screensy", yes);
        tagCompound.setIntArray("screensz", zes);
        return tagCompound;
    }

    @Override
    public void writeRestorableToNBT(NBTTagCompound tagCompound) {
        super.writeRestorableToNBT(tagCompound);
    }

    @Override
    public void update() {
        if (!getWorld().isRemote) {
            checkStateServer();
        }
    }

    private void checkStateServer() {
        tickCounter--;
        if (tickCounter > 0) {
            return;
        }
        tickCounter = 20;
        int rf = getEnergyStored();
        int rememberRf = rf;
        boolean fixesAreNeeded = false;
        for (BlockPos c : connectedScreens) {
            TileEntity te = getWorld().getTileEntity(c);
            if (te instanceof ScreenTileEntity) {
                ScreenTileEntity screenTileEntity = (ScreenTileEntity) te;
                int rfModule = screenTileEntity.getTotalRfPerTick() * 20;

                if (rfModule > rf) {
                    screenTileEntity.setPower(false);
                } else {
                    rf -= rfModule;
                    screenTileEntity.setPower(true);
                }
            } else {
                // This coordinate is no longer a valid screen. We need to update.
                fixesAreNeeded = true;
            }
        }
        if (rf < rememberRf) {
            consumeEnergy(rememberRf - rf);
        }

        if (fixesAreNeeded) {
            List<BlockPos> newScreens = new ArrayList<>();
            for (BlockPos c : connectedScreens) {
                TileEntity te = getWorld().getTileEntity(c);
                if (te instanceof ScreenTileEntity) {
                    newScreens.add(c);
                }
            }
            connectedScreens = newScreens;
            markDirtyClient();
        }
    }

    private void scan() {
        detach();
        int radius = 32 + (int) (getInfusedFactor() * 32);

        int xCoord = getPos().getX();
        int yCoord = getPos().getY();
        int zCoord = getPos().getZ();
        for (int y = yCoord - radius ; y <= yCoord + radius ; y++) {
            if (y >= 0 && y < 256) {
                for (int x = xCoord - radius; x <= xCoord + radius; x++) {
                    for (int z = zCoord - radius; z <= zCoord + radius; z++) {
                        BlockPos spos = new BlockPos(x, y, z);
                        if (getWorld().getBlockState(spos).getBlock() instanceof ScreenBlock) {
                            TileEntity te = getWorld().getTileEntity(spos);
                            if (te instanceof ScreenTileEntity) {
                                ScreenTileEntity ste = (ScreenTileEntity)te;
                                if (!ste.isConnected() && ste.isControllerNeeded()) {
                                    connectedScreens.add(spos);
                                    ste.setConnected(true);
                                }
                            }
                        }
                    }
                }
            }
        }
        markDirtyClient();
    }

    public void detach() {
        for (BlockPos c : connectedScreens) {
            TileEntity te = getWorld().getTileEntity(c);
            if (te instanceof ScreenTileEntity) {
                ((ScreenTileEntity) te).setPower(false);
                ((ScreenTileEntity) te).setConnected(false);
            }
        }

        connectedScreens.clear();
        markDirtyClient();
    }

    public List<BlockPos> getConnectedScreens() {
        return connectedScreens;
    }
}
