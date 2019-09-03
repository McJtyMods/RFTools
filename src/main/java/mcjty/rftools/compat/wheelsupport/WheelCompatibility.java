package mcjty.rftools.compat.wheelsupport;

public class WheelCompatibility {

    private static boolean registered;

    public static void register() {
        if (registered)
            return;
        registered = true;
        // @todo 1.14
//        FMLInterModComms.sendFunctionMessage("intwheel", "getTheWheel", "mcjty.rftools.compat.wheelsupport.WheelCompatibility$GetTheWheel");
    }


//    public static class GetTheWheel implements com.google.common.base.Function<IInteractionWheel, Void> {
//
//        public static IInteractionWheel wheel;
//
//        @Nullable
//        @Override
//        public Void apply(IInteractionWheel theWheel) {
//            wheel = theWheel;
//            Logging.getLogger().log(Level.INFO, "Enabled support for The Interaction Wheel");
//            wheel.registerProvider(new IWheelActionProvider() {
//                @Override
//                public String getID() {
//                    return RFTools.MODID + ".wheel";
//                }
//
//                @Override
//                public void updateWheelActions(@Nonnull Set<String> actions, @Nonnull PlayerEntity player, World world, @Nullable BlockPos pos) {
//                    if (pos != null) {
//                        Block block = world.getBlockState(pos).getBlock();
//                        actions.add(FindBlockAction.ACTION_FINDBLOCK);
//                        if (block instanceof BaseBlock) {
//                            actions.add(RemoveBlockAction.ACTION_REMOVEBLOCK);
//                        }
//                        if (block instanceof ScreenBlock || block instanceof ScreenHitBlock) {
//                            actions.add(ResizeScreenAction.ACTION_RESIZESCREEN);
//                            actions.add(ToggleScreenTransparentAction.ACTION_TOGGLESCREENTRANSPARENT);
//                        }
//                    }
//                }
//            });
//            wheel.getRegistry().register(new RemoveBlockAction());
//            wheel.getRegistry().register(new ResizeScreenAction());
//            wheel.getRegistry().register(new ToggleScreenTransparentAction());
//            wheel.getRegistry().register(new FindBlockAction());
//            return null;
//        }
//    }
}
