package mcjty.rftools.compat.computers;

public class MatterReceiverDriver {
//    public static class OCDriver extends AbstractOCDriver {
//        public OCDriver() {
//            super("rftools_matter_receiver", MatterReceiverTileEntity.class);
//        }
//
//        public static class InternalManagedEnvironment extends AbstractOCDriver.InternalManagedEnvironment<MatterReceiverTileEntity> {
//            public InternalManagedEnvironment(MatterReceiverTileEntity tile) {
//                super(tile, "rftools_matter_receiver");
//            }
//
//            @Callback(doc = "function():number; Get the currently stored energy")
//            public Object[] getEnergy(Context c, Arguments a) {
//                return new Object[]{tile.getStoredPower()};
//            }
//
//            @Callback(doc = "function():number; Get the maximum stored energy")
//            public Object[] getMaxEnergy(Context c, Arguments a) {
//                return new Object[]{tile.getCapacity()};
//            }
//
//            @Callback(doc = "function():string; Get the current name")
//            public Object[] getName(Context c, Arguments a) {
//                return new Object[]{tile.getName()};
//            }
//
//            @Callback(doc = "function(name:string); Set the current name")
//            public Object[] setName(Context c, Arguments a) {
//                String name = a.checkString(0);
//                tile.setName(name);
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
//            return new InternalManagedEnvironment((MatterReceiverTileEntity) tile);
//        }
//    }
}
