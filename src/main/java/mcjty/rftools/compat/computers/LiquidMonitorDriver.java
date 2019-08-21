package mcjty.rftools.compat.computers;

public class LiquidMonitorDriver {
//    public static class OCDriver extends AbstractOCDriver {
//        public OCDriver() {
//            super("rftools_liquid_monitor", LiquidMonitorBlockTileEntity.class);
//        }
//
//        public static class InternalManagedEnvironment extends AbstractOCDriver.InternalManagedEnvironment<LiquidMonitorBlockTileEntity> {
//            public InternalManagedEnvironment(LiquidMonitorBlockTileEntity tile) {
//                super(tile, "rftools_liquid_monitor");
//            }
//
//            @Callback(doc="function():number; Get the max amount of liquid")
//            public Object[] getMaxLiquid(Context c, Arguments a) {
//                if (tile.getMonitor() == null) {
//                    return new Object[]{null, "No monitored block set"};
//                }
//
//                TileEntity monitor = tile.getWorld().getTileEntity(tile.getMonitor());
//                IFluidHandler fluidHandler = getFluidHandler(monitor);
//                if (fluidHandler != null) {
//                    IFluidTankProperties[] properties = fluidHandler.getTankProperties();
//                    if (properties != null && properties.length > 0) {
//                        return new Object[]{properties[0].getCapacity()};
//                    }
//                }
//                return new Object[] {null, "Invalid monitored block"};
//            }
//
//            @Callback(doc="function():string, number; Get the current type and amount of liquid")
//            public Object[] getLiquid(Context c, Arguments a) {
//                if (tile.getMonitor() == null) {
//                    return new Object[]{null, "No monitored block set"};
//                }
//
//                TileEntity monitor = tile.getWorld().getTileEntity(tile.getMonitor());
//                IFluidHandler fluidHandler = getFluidHandler(monitor);
//                if (fluidHandler != null) {
//                    IFluidTankProperties[] properties = fluidHandler.getTankProperties();
//                    if (properties != null && properties.length > 0) {
//                        FluidStack contents = properties[0].getContents();
//                        if (contents == null) {
//                            return new Object[] {"null", 0};
//                        }
//                        return new Object[] {contents.getFluid().getModuleName(), contents.amount};
//                    }
//                }
//                return new Object[] {null, "Invalid monitored block"};
//            }
//
//            @Callback(doc="function(level:int):true or nil, string; Set the alarm level")
//            public Object[] setLevel(Context c, Arguments a) {
//                int level = a.checkInteger(0);
//
//                if (level < 0 || level > 100) {
//                    return new Object[]{null, "Invalid level. Must be between 0 and 100"};
//                }
//
//                tile.setAlarm(tile.getAlarmMode(), level);
//
//                return new Object[]{};
//            }
//
//            @Callback(doc="function():int; Get the current alarm level")
//            public Object[] getLevel(Context c, Arguments a) {
//                return new Object[]{tile.getAlarmLevel()};
//            }
//
//            @Callback(doc="function(mode:string):true or nil, string; Set the alarm mode. Most be one of: \"Off\", \"Less\", \"More\"")
//            public Object[] setMode(Context c, Arguments a) {
//                String newMode = a.checkString(0);
//                switch (newMode) {
//                    case "Off":
//                    case "Less":
//                    case "More":
//                        break;
//                    default:
//                        return new Object[]{null, "Invalid mode. Most be one of: \"Off\", \"Less\", \"More\""};
//                }
//                RFMonitorMode mode = RFMonitorMode.getModeFromDescription(newMode);
//
//                tile.setAlarm(mode, tile.getAlarmLevel());
//                return new Object[]{};
//            }
//
//            @Callback(doc="function():string; Get the current alarm mode. Will be one of: \"Off\", \"Less\", \"More\"")
//            public Object[] getMode(Context c, Arguments a) {
//                return new Object[]{tile.getAlarmMode().getDescription()};
//            }
//            @Override
//            public int priority() {
//                return 4;
//            }
//        }
//
//        @Override
//        public AbstractManagedEnvironment createEnvironment(World world, BlockPos pos, Direction side, TileEntity tile) {
//            return new InternalManagedEnvironment((LiquidMonitorBlockTileEntity) tile);
//        }
//    }
//
//    private static IFluidHandler getFluidHandler(TileEntity tile) {
//        if (tile != null) {
//            IFluidHandler cap = null;
//            if  (tile.hasCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, null)) {
//                return tile.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, null);
//            } else if (tile instanceof IFluidHandler) {
//                return (IFluidHandler) tile;
//            }
//        }
//        return null;
//    }
}
