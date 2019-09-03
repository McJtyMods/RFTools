package mcjty.rftools.compat.wheelsupport;

public class RemoveBlockAction {} /* @todo 1.14 implements IWheelAction {

    public static final String ACTION_REMOVEBLOCK = "rftools.removeblock";

    @Override
    public String getId() {
        return ACTION_REMOVEBLOCK;
    }

    @Override
    public WheelActionElement createElement() {
        return new WheelActionElement(ACTION_REMOVEBLOCK).description("Remove a block", null).texture("rftools:textures/gui/wheel_actions.png", 0, 0, 0, 0+32, 128, 128);
    }

    @Override
    public boolean performClient(PlayerEntity player, World world, @Nullable BlockPos pos, boolean extended) {
        return true;
    }

    @Override
    public void performServer(PlayerEntity player, World world, @Nullable BlockPos pos, boolean extended) {
        if (pos != null) {
            Block block = world.getBlockState(pos).getBlock();
            if (block instanceof BaseBlock) {
                block.harvestBlock(world, player, pos, world.getBlockState(pos), world.getTileEntity(pos), ItemStack.EMPTY);
            }
        }
    }
}
*/