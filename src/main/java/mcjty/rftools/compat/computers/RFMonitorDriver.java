package mcjty.rftools.compat.computers;

public class RFMonitorDriver {
//    public static class OCDriver extends AbstractOCDriver {
//        public OCDriver() {
//            super("rftools_rf_monitor", RFMonitorBlockTileEntity.class);
//        }
//
//        public static class InternalManagedEnvironment extends AbstractOCDriver.InternalManagedEnvironment<RFMonitorBlockTileEntity> {
//            public InternalManagedEnvironment(RFMonitorBlockTileEntity tile) {
//                super(tile, "rftools_rf_monitor");
//            }
//
//            @Callback(doc = "function():number or nil, string; Get the currently stored energy")
//            public Object[] getEnergy(Context c, Arguments a) {
//                if (tile.getMonitor() == null) {
//                    return new Object[]{null, "No monitored block set"};
//                }
//
//                TileEntity monitor = tile.getWorld().getTileEntity(tile.getMonitor());
//                if (!EnergyTools.isEnergyTE(monitor, null)) {
//                    return new Object[] {null, "Invalid monitored block"};
//                }
//
//                return new Object[]{EnergyTools.getEnergyLevelMulti(monitor, null).getEnergy()};
//            }
//
//            @Callback(doc = "function():number; Get the maximum stored energy")
//            public Object[] getMaxEnergy(Context c, Arguments a) {
//                if (tile.getMonitor() == null) {
//                    return new Object[]{null, "No monitored block set"};
//                }
//
//                TileEntity monitor = tile.getWorld().getTileEntity(tile.getMonitor());
//                if (!EnergyTools.isEnergyTE(monitor, null)) {
//                    return new Object[] {null, "Invalid monitored block"};
//                }
//
//                return new Object[]{EnergyTools.getEnergyLevelMulti(monitor, null).getMaxEnergy()};
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
//
//            @Override
//            public int priority() {
//                return 4;
//            }
//        }
//
//        @Override
//        public AbstractManagedEnvironment createEnvironment(World world, BlockPos pos, Direction side, TileEntity tile) {
//            return new InternalManagedEnvironment((RFMonitorBlockTileEntity) tile);
//        }
//    }
}
