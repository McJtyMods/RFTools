package mcjty.rftools.blocks.screens;

import cpw.mods.fml.common.Optional;
import dan200.computercraft.api.lua.ILuaContext;
import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.api.peripheral.IComputerAccess;
import dan200.computercraft.api.peripheral.IPeripheral;
import li.cil.oc.api.machine.Arguments;
import li.cil.oc.api.machine.Callback;
import li.cil.oc.api.machine.Context;
import li.cil.oc.api.network.SimpleComponent;
import mcjty.entity.GenericEnergyReceiverTileEntity;
import mcjty.rftools.blocks.screens.modules.ComputerScreenModule;
import mcjty.rftools.network.Argument;
import mcjty.varia.Coordinate;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Optional.InterfaceList({
        @Optional.Interface(iface = "li.cil.oc.api.network.SimpleComponent", modid = "OpenComputers"),
        @Optional.Interface(iface = "dan200.computercraft.api.peripheral.IPeripheral", modid = "ComputerCraft")})
public class ScreenControllerTileEntity extends GenericEnergyReceiverTileEntity implements SimpleComponent, IPeripheral {

    public static final String CMD_SCAN = "scan";
    public static final String CMD_DETACH = "detach";

    public static final String COMPONENT_NAME = "screen_controller";

    private List<Coordinate> connectedScreens = new ArrayList<Coordinate>();
    private int tickCounter = 20;

    public ScreenControllerTileEntity() {
        super(ScreenConfiguration.CONTROLLER_MAXENERGY, ScreenConfiguration.CONTROLLER_RECEIVEPERTICK);
    }

    @Override
    @Optional.Method(modid = "ComputerCraft")
    public String getType() {
        return COMPONENT_NAME;
    }

    @Override
    @Optional.Method(modid = "ComputerCraft")
    public String[] getMethodNames() {
        return new String[] { "getScreenCount", "getScreenIndex", "getScreenCoordinate", "addText", "clearText" };
    }

    @Override
    @Optional.Method(modid = "ComputerCraft")
    public Object[] callMethod(IComputerAccess computer, ILuaContext context, int method, Object[] arguments) throws LuaException, InterruptedException {
        switch (method) {
            case 0: return new Object[] { connectedScreens.size() };
            case 1: return getScreenIndex(new Coordinate(((Double) arguments[0]).intValue(), ((Double) arguments[1]).intValue(), ((Double) arguments[2]).intValue()));
            case 2: Coordinate c = connectedScreens.get(((Double) arguments[0]).intValue()); return new Object[] { c.getX(), c.getY(), c.getZ() };
            case 3: return addText((String) arguments[0], (String) arguments[1], ((Double) arguments[2]).intValue());
            case 4: return clearText((String) arguments[0]);
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

    @Callback
    @Optional.Method(modid = "OpenComputers")
    public Object[] getScreenCount(Context context, Arguments args) throws Exception {
        return new Object[] { connectedScreens.size() };
    }

    @Callback
    @Optional.Method(modid = "OpenComputers")
    public Object[] getScreens(Context context, Arguments args) throws Exception {
        List<Map<String,Integer>> result = new ArrayList<Map<String, Integer>>();
        for (Coordinate screen : connectedScreens) {
            Map<String,Integer> coordinate = new HashMap<String, Integer>();
            coordinate.put("x", screen.getX());
            coordinate.put("y", screen.getY());
            coordinate.put("z", screen.getZ());
            result.add(coordinate);
        }

        return new Object[] { result };
    }

    @Callback
    @Optional.Method(modid = "OpenComputers")
    public Object[] getScreenIndex(Context context, Arguments args) throws Exception {
        Map screen = args.checkTable(0);
        if (!screen.containsKey("x") || !screen.containsKey("y") || !screen.containsKey("z")) {
            throw new IllegalArgumentException("Screen map doesn't contain the right x,y,z coordinate!");
        }
        Coordinate recC = new Coordinate(((Double) screen.get("x")).intValue(), ((Double) screen.get("y")).intValue(), ((Double) screen.get("z")).intValue());
        return getScreenIndex(recC);
    }

    private Object[] getScreenIndex(Coordinate coordinate) {
        int i = 0;
        for (Coordinate connectedScreen : connectedScreens) {
            if (connectedScreen.equals(coordinate)) {
                return new Object[] { i };
            }
            i++;
        }

        return null;
    }

    @Callback
    @Optional.Method(modid = "OpenComputers")
    public Object[] getScreenCoordinate(Context context, Arguments args) throws Exception {
        int index = args.checkInteger(0);
        if (index < 0 || index >= connectedScreens.size()) {
            throw new IllegalArgumentException("Screen index out of range!");
        }
        Coordinate screen = connectedScreens.get(index);
        Map<String,Integer> coordinate = new HashMap<String, Integer>();
        coordinate.put("x", screen.getX());
        coordinate.put("y", screen.getY());
        coordinate.put("z", screen.getZ());

        return new Object[] { coordinate };
    }


    @Callback
    @Optional.Method(modid = "OpenComputers")
    public Object[] addText(Context context, Arguments args) throws Exception {
        String tag = args.checkString(0);
        String text = args.checkString(1);
        int color = args.checkInteger(2);

        return addText(tag, text, color);
    }

    private Object[] addText(String tag, String text, int color) {
        for (Coordinate screen : connectedScreens) {
            TileEntity te = worldObj.getTileEntity(screen.getX(), screen.getY(), screen.getZ());
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

    @Callback
    @Optional.Method(modid = "OpenComputers")
    public Object[] clearText(Context context, Arguments args) throws Exception {
        String tag = args.checkString(0);

        return clearText(tag);
    }

    private Object[] clearText(String tag) {
        for (Coordinate screen : connectedScreens) {
            TileEntity te = worldObj.getTileEntity(screen.getX(), screen.getY(), screen.getZ());
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

    @Callback
    @Optional.Method(modid = "OpenComputers")
    public Object[] getTags(Context context, Arguments args) throws Exception {
        List<String> tags = new ArrayList<String>();
        for (Coordinate screen : connectedScreens) {
            TileEntity te = worldObj.getTileEntity(screen.getX(), screen.getY(), screen.getZ());
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
            connectedScreens.add(new Coordinate(xes[i], yes[i], zes[i]));
        }
    }

    @Override
    public void readRestorableFromNBT(NBTTagCompound tagCompound) {
        super.readRestorableFromNBT(tagCompound);
    }

    @Override
    public void writeToNBT(NBTTagCompound tagCompound) {
        super.writeToNBT(tagCompound);
        int[] xes = new int[connectedScreens.size()];
        int[] yes = new int[connectedScreens.size()];
        int[] zes = new int[connectedScreens.size()];
        for (int i = 0 ; i < connectedScreens.size() ; i++) {
            Coordinate c = connectedScreens.get(i);
            xes[i] = c.getX();
            yes[i] = c.getY();
            zes[i] = c.getZ();
        }
        tagCompound.setIntArray("screensx", xes);
        tagCompound.setIntArray("screensy", yes);
        tagCompound.setIntArray("screensz", zes);
    }

    @Override
    public void writeRestorableToNBT(NBTTagCompound tagCompound) {
        super.writeRestorableToNBT(tagCompound);
    }

    @Override
    protected void checkStateServer() {
        tickCounter--;
        if (tickCounter > 0) {
            return;
        }
        tickCounter = 20;
        int rf = getEnergyStored(ForgeDirection.DOWN);
        int rememberRf = rf;
        boolean fixesAreNeeded = false;
        for (Coordinate c : connectedScreens) {
            TileEntity te = worldObj.getTileEntity(c.getX(), c.getY(), c.getZ());
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
            List<Coordinate> newScreens = new ArrayList<Coordinate>();
            for (Coordinate c : connectedScreens) {
                TileEntity te = worldObj.getTileEntity(c.getX(), c.getY(), c.getZ());
                if (te instanceof ScreenTileEntity) {
                    newScreens.add(c);
                }
            }
            connectedScreens = newScreens;
            markDirty();
            worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
        }
    }

    private void scan() {
        detach();
        int radius = 32 + (int) (getInfusedFactor() * 32);

        for (int y = yCoord - 16 ; y <= yCoord + 16 ; y++) {
            if (y >= 0 && y < 256) {
                for (int x = xCoord - radius; x <= xCoord + radius; x++) {
                    for (int z = zCoord - radius; z <= zCoord + radius; z++) {
                        TileEntity te = worldObj.getTileEntity(x, y, z);
                        if (te instanceof ScreenTileEntity) {
                            if (!((ScreenTileEntity) te).isConnected()) {
                                connectedScreens.add(new Coordinate(x, y, z));
                                ((ScreenTileEntity) te).setConnected(true);
                            }
                        }
                    }
                }
            }
        }
        markDirty();
        worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
    }

    public void detach() {
        for (Coordinate c : connectedScreens) {
            TileEntity te = worldObj.getTileEntity(c.getX(), c.getY(), c.getZ());
            if (te instanceof ScreenTileEntity) {
                ((ScreenTileEntity) te).setPower(false);
                ((ScreenTileEntity) te).setConnected(false);
            }
        }

        connectedScreens.clear();
        markDirty();
        worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
    }

    public List<Coordinate> getConnectedScreens() {
        return connectedScreens;
    }

    @Override
    public boolean execute(String command, Map<String, Argument> args) {
        boolean rc = super.execute(command, args);
        if (rc) {
            return true;
        }
        if (CMD_SCAN.equals(command)) {
            scan();
            return true;
        } else if (CMD_DETACH.equals(command)) {
            detach();
            return true;
        }
        return false;
    }
}
