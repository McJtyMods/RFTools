package mcjty.rftools.compat.computers;

public class PowercellDriver {
//    public static class OCDriver extends AbstractOCDriver {
//        public OCDriver() {
//            super("rftools_powercell", PowerCellTileEntity.class);
//        }
//
//        public static class InternalManagedEnvironment extends AbstractOCDriver.InternalManagedEnvironment<PowerCellTileEntity> {
//            public InternalManagedEnvironment(PowerCellTileEntity tile) {
//                super(tile, "rftools_powercell");
//            }
//
//            @Callback(doc = "function():number; Get the currently stored energy")
//            public Object[] getEnergy(Context c, Arguments a) {
//                return new Object[]{tile.getEnergyStored()};
//            }
//
//            @Callback(doc = "function():number; Get the maximum stored energy")
//            public Object[] getMaxEnergy(Context c, Arguments a) {
//                return new Object[]{tile.getMaxEnergyStored()};
//            }
//
//            @Callback(doc="function():number; Get how much energy was extracted in total")
//            public Object[] getTotalExtracted(Context c, Arguments a) {
//                return new Object[]{tile.getTotalExtracted()};
//            }
//
//            @Callback(doc="function():number; Get how much energy was inserted in total")
//            public Object[] getTotalInserted(Context c, Arguments a) {
//                return new Object[]{tile.getTotalInserted()};
//            }
//
//            @Callback(doc="function(); Reset the total extracted energy stat")
//            public Object[] resetTotalExtracted(Context c, Arguments a) {
//                tile.resetTotalExtracted();
//                return new Object[]{};
//            }
//
//            @Callback(doc="function(); Reset the total extracted energy stat")
//            public Object[] resetTotalInserted(Context c, Arguments a) {
//                tile.resetTotalInserted();
//                return new Object[]{};
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
//            return new InternalManagedEnvironment((PowerCellTileEntity) tile);
//        }
//    }
}
