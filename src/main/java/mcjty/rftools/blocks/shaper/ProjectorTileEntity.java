package mcjty.rftools.blocks.shaper;

import mcjty.lib.container.*;
import mcjty.lib.tileentity.GenericEnergyReceiverTileEntity;
import mcjty.lib.typed.Key;
import mcjty.lib.typed.Type;
import mcjty.lib.typed.TypedMap;
import mcjty.lib.varia.OrientationTools;
import mcjty.rftools.blocks.builder.BuilderSetup;
import mcjty.rftools.items.builder.ShapeCardItem;
import mcjty.rftools.network.RFToolsMessages;
import mcjty.rftools.shapes.RenderData;
import mcjty.rftools.shapes.ShapeID;
import mcjty.rftools.shapes.ShapeRenderer;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;

import static net.minecraft.util.EnumFacing.*;

import java.util.Arrays;
import java.util.List;

public class ProjectorTileEntity extends GenericEnergyReceiverTileEntity implements DefaultSidedInventory, ITickable {

    public static final String CMD_RSSETTINGS = "projector.rsSettings";

    public static final Key<String> PARAM_OPON_N = new Key<>("opOn_n", Type.STRING);
    public static final Key<String> PARAM_OPON_S = new Key<>("opOn_s", Type.STRING);
    public static final Key<String> PARAM_OPON_W = new Key<>("opOn_w", Type.STRING);
    public static final Key<String> PARAM_OPON_E = new Key<>("opOn_e", Type.STRING);
    public static final List<Key<String>> PARAM_OPON = Arrays.asList(PARAM_OPON_N, PARAM_OPON_S, PARAM_OPON_W, PARAM_OPON_E);

    public static final Key<String> PARAM_OPOFF_N = new Key<>("opOff_n", Type.STRING);
    public static final Key<String> PARAM_OPOFF_S = new Key<>("opOff_s", Type.STRING);
    public static final Key<String> PARAM_OPOFF_W = new Key<>("opOff_w", Type.STRING);
    public static final Key<String> PARAM_OPOFF_E = new Key<>("opOff_e", Type.STRING);
    public static final List<Key<String>> PARAM_OPOFF = Arrays.asList(PARAM_OPOFF_N, PARAM_OPOFF_S, PARAM_OPOFF_W, PARAM_OPOFF_E);

    public static final Key<Double> PARAM_VALON_N = new Key<>("valOn_n", Type.DOUBLE);
    public static final Key<Double> PARAM_VALON_S = new Key<>("valOn_s", Type.DOUBLE);
    public static final Key<Double> PARAM_VALON_W = new Key<>("valOn_w", Type.DOUBLE);
    public static final Key<Double> PARAM_VALON_E = new Key<>("valOn_e", Type.DOUBLE);
    public static final List<Key<Double>> PARAM_VALON = Arrays.asList(PARAM_VALON_N, PARAM_VALON_S, PARAM_VALON_W, PARAM_VALON_E);

    public static final Key<Double> PARAM_VALOFF_N = new Key<>("valOff_n", Type.DOUBLE);
    public static final Key<Double> PARAM_VALOFF_S = new Key<>("valOff_s", Type.DOUBLE);
    public static final Key<Double> PARAM_VALOFF_W = new Key<>("valOff_w", Type.DOUBLE);
    public static final Key<Double> PARAM_VALOFF_E = new Key<>("valOff_e", Type.DOUBLE);
    public static final List<Key<Double>> PARAM_VALOFF = Arrays.asList(PARAM_VALOFF_N, PARAM_VALOFF_S, PARAM_VALOFF_W, PARAM_VALOFF_E);

    public static final String CMD_SETTINGS = "projector.settings";

    public static final Key<Integer> PARAM_SCALE = new Key<>("scale", Type.INTEGER);
    public static final Key<Integer> PARAM_OFFSET = new Key<>("offset", Type.INTEGER);
    public static final Key<Integer> PARAM_ANGLE = new Key<>("angle", Type.INTEGER);
    public static final Key<Boolean> PARAM_AUTO = new Key<>("auto", Type.BOOLEAN);
    public static final Key<Boolean> PARAM_SCAN = new Key<>("scan", Type.BOOLEAN);
    public static final Key<Boolean> PARAM_SOUND = new Key<>("sound", Type.BOOLEAN);
    public static final Key<Boolean> PARAM_GRAY = new Key<>("gray", Type.BOOLEAN);

    public static final int SLOT_CARD = 0;
    public static final ContainerFactory CONTAINER_FACTORY = new ContainerFactory() {
        @Override
        protected void setup() {
            addSlot(new SlotDefinition(SlotType.SLOT_SPECIFICITEM,
                    new ItemStack(BuilderSetup.shapeCardItem)), ContainerFactory.CONTAINER_CONTAINER, SLOT_CARD, 15, 7);
            layoutPlayerInventorySlots(85, 142);
        }
    };

    private InventoryHelper inventoryHelper = new InventoryHelper(this, CONTAINER_FACTORY, 1);
    private ShapeRenderer shapeRenderer = null;
    private ProjectorOperation operations[] = new ProjectorOperation[4];

    private boolean active = false;

    // The following fields are all needed on client and sent with a custom packet
    private float verticalOffset = .2f;
    private float scale = 0.01f;
    private float angle = 0.0f;
    private boolean autoRotate = false;
    private boolean projecting = false;
    private boolean scanline = true;
    private boolean sound = true;
    private boolean grayscale = false;
    private int counter = 0;    // Counter to detect that we need to do a new 'scan' client-side

    // Needed client side but set on client when 'counter' changes
    private boolean scanNeeded = false;

    // Set on server
    private boolean doNotifyClients = false;  // Set to true to notify clients

    public ProjectorTileEntity() {
        super(ScannerConfiguration.PROJECTOR_MAXENERGY.get(), ScannerConfiguration.PROJECTOR_RECEIVEPERTICK.get());
        for (int i = 0 ; i < operations.length ; i++) {
            operations[i] = new ProjectorOperation();
        }
        for (ProjectorOperation operation : operations) {
            operation.setOpcodeOn(ProjectorOpcode.NONE);
            operation.setValueOn(null);
            operation.setOpcodeOff(ProjectorOpcode.NONE);
            operation.setValueOff(null);
        }
        operations[0].setOpcodeOn(ProjectorOpcode.ON);
        operations[0].setOpcodeOff(ProjectorOpcode.ON);
    }

    @Override
    public void update() {
        if (!getWorld().isRemote) {
            updateOperations(false);

            boolean a = active;
            if (a) {
                if (getStoredPower() < ScannerConfiguration.PROJECTOR_USEPERTICK.get()) {
                    a = false;
                }
            }

            if (a != projecting) {
                projecting = a;
                markForNotification();
            }

            if (projecting) {
                consumeEnergy(ScannerConfiguration.PROJECTOR_USEPERTICK.get());
            }
            if (doNotifyClients) {
                doNotifyClients = false;
                notifyClients();
            }
        } else {
            if (scanNeeded) {
                scanNeeded = false;
                RenderData data = ShapeRenderer.getRenderDataAndCreate(getShapeID());
                data.setWantData(true);
            }
            if (autoRotate) {
                angle += 1;
                if (angle >= 360) {
                    angle = 0;
                }
            }
        }
    }

    @Override
    public void invalidate() {
        super.invalidate();
        if (getWorld().isRemote) {
            stopSounds();
        }
    }

    @SideOnly(Side.CLIENT)
    private void stopSounds() {
        ProjectorSounds.stopSound(getPos());
    }

    public ShapeID getShapeID() {
        int scanId = ShapeCardItem.getScanId(getRenderStack());
        boolean isSolid = ShapeCardItem.isSolid(getRenderStack());
        if (scanId == 0) {
            return new ShapeID(getWorld().provider.getDimension(), getPos(), scanId, isGrayscale(), isSolid);
        } else {
            return new ShapeID(0, null, scanId, isGrayscale(), isSolid);
        }
    }

    @Override
    public void setPowerInput(int powered) {
        boolean changed = powerLevel != powered;
        super.setPowerInput(powered);
        if (changed) {
            updateOperations(true);
        }
    }

    private void updateOperations(boolean pulse) {
        for (EnumFacing facing : EnumFacing.HORIZONTALS) {
            int index = facing.ordinal() - 2;
            ProjectorOperation op = operations[index];
            int pl = index ^ 1;
            if (((powerLevel >> pl) & 1) != 0) {
                handleOpcode(op.getOpcodeOn(), op.getValueOn(), pulse);
            } else {
                handleOpcode(op.getOpcodeOff(), op.getValueOff(), pulse);
            }
        }
    }

    private void handleOpcode(ProjectorOpcode op, @Nullable Double val, boolean pulse) {
        if (op == null) {
            op = ProjectorOpcode.NONE;
        }
        switch (op) {
            case NONE:
                break;
            case ON:
                setActive(true);
                break;
            case OFF:
                setActive(false);
                break;
            case SCAN:
                if (pulse) {
                    counter++;
                    markForNotification();
                }
                break;
            case OFFSET: {
                double o = getOffsetDouble();
                if (val != null && Math.abs(o-val) > .3) {
                    if (o < val) {
                        o++;
                    } else {
                        o--;
                    }
                    setOffsetInt(o);
                    markForNotification();
                }
                break;
            }
            case ROT: {
                int o = getAngleInt();
                if (val != null && o != val) {
                    if (o < val) {
                        o++;
                    } else {
                        o--;
                    }
                    setAngleInt(o);
                    markForNotification();
                }
                break;
            }
            case SCALE: {
                double o = getScaleDouble();
                if (val != null && Math.abs(o-val) > .3) {
                    if (o < val) {
                        o++;
                    } else {
                        o--;
                    }
                    setScaleInt(o);
                    markForNotification();
                }
                break;
            }
            case GRAYON:
                grayscale = true;
                markForNotification();
                break;
            case GRAYOFF:
                grayscale = false;
                markForNotification();
                break;
        }
    }

    private void setActive(boolean a) {
        if (a == active) {
            return;
        }
        active = a;
        markDirtyQuick();
    }

    public ProjectorOperation[] getOperations() {
        return operations;
    }

    public int getCounter() {
        return counter;
    }

    @Override
    public InventoryHelper getInventoryHelper() {
        return inventoryHelper;
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
    public boolean isItemValidForSlot(int index, ItemStack stack) {
        return stack.getItem() == BuilderSetup.shapeCardItem;
    }

    @Override
    public void readRestorableFromNBT(NBTTagCompound tagCompound) {
        super.readRestorableFromNBT(tagCompound);
        readBufferFromNBT(tagCompound, inventoryHelper);
        verticalOffset = tagCompound.hasKey("offs") ? tagCompound.getFloat("offs") : .2f;
        scale = tagCompound.hasKey("scale") ? tagCompound.getFloat("scale") : .01f;
        angle = tagCompound.hasKey("angle") ? tagCompound.getFloat("angle") : .0f;
        autoRotate = tagCompound.getBoolean("rot");
        scanline = tagCompound.getBoolean("scan");
        sound = tagCompound.getBoolean("sound");
        boolean gs = tagCompound.getBoolean("grayscale");
        if (gs != grayscale) {
            grayscale = gs;
            shapeRenderer = null;
            scanNeeded = true;
        }
        projecting = tagCompound.getBoolean("projecting");
        active = tagCompound.getBoolean("active");
        counter = tagCompound.getInteger("counter");
        for (EnumFacing facing : EnumFacing.HORIZONTALS) {
            if (tagCompound.hasKey("op_"+facing.getName())) {
                int index = facing.ordinal() - 2;
                ProjectorOperation op = operations[index];
                NBTTagCompound tc = (NBTTagCompound) tagCompound.getTag("op_" + facing.getName());
                String on = tc.getString("on");
                Double von = null;
                if (tc.hasKey("von")) {
                    von = tc.getDouble("von");
                }
                String off = tc.getString("off");
                Double voff = null;
                if (tc.hasKey("voff")) {
                    voff = tc.getDouble("voff");
                }
                op.setOpcodeOn(ProjectorOpcode.getByCode(on));
                op.setOpcodeOff(ProjectorOpcode.getByCode(off));
                op.setValueOn(von);
                op.setValueOff(voff);
            }
        }
    }

    @Override
    public void writeRestorableToNBT(NBTTagCompound tagCompound) {
        super.writeRestorableToNBT(tagCompound);
        writeBufferToNBT(tagCompound, inventoryHelper);
        tagCompound.setFloat("offs", verticalOffset);
        tagCompound.setFloat("scale", scale);
        tagCompound.setFloat("angle", angle);
        tagCompound.setBoolean("rot", autoRotate);
        tagCompound.setBoolean("scan", scanline);
        tagCompound.setBoolean("sound", sound);
        tagCompound.setBoolean("grayscale", grayscale);
        tagCompound.setBoolean("projecting", projecting);
        tagCompound.setBoolean("active", active);
        tagCompound.setInteger("counter", counter);
        for (EnumFacing facing : EnumFacing.HORIZONTALS) {
            int index = facing.ordinal() - 2;
            ProjectorOperation op = operations[index];
            NBTTagCompound tc = new NBTTagCompound();
            tc.setString("on", op.getOpcodeOn().getCode());
            if (op.getValueOn() != null) {
                tc.setDouble("von", op.getValueOn());
            }
            tc.setString("off", op.getOpcodeOff().getCode());
            if (op.getValueOff() != null) {
                tc.setDouble("voff", op.getValueOff());
            }
            tagCompound.setTag("op_"+facing.getName(), tc);
        }
    }

    public float getVerticalOffset() {
        return verticalOffset;
    }

    public int getOffsetInt() {
        return (int) getOffsetDouble();
    }

    private float getOffsetDouble() {
        return verticalOffset * 20;
    }

    private void setOffsetInt(double o) {
        verticalOffset = (float) (o / 20.0);
    }

    public float getScale() {
        return scale;
    }

    // 0 -> 0.001
    // 100 -> 0.1
    public int getScaleInt() {
        return (int) getScaleDouble();
    }

    private double getScaleDouble() {
        return 20.0 * Math.log((scale - 0.001f) / 0.1f * 147.4131f + 1);
    }

    private void setScaleInt(double s) {
        scale = ((float) Math.exp(s / 20.0) - 1) / 147.4131f * 0.1f + 0.001f;
    }

    public float getAngle() {
        return angle;
    }

    public int getAngleInt() {
        return (int) angle;
    }

    private void setAngleInt(int a) {
        angle = a;
    }

    public boolean isAutoRotate() {
        return autoRotate;
    }

    public boolean isScanline() {
        return scanline;
    }

    public boolean isSound() {
        return sound;
    }

    public boolean isGrayscale() {
        return grayscale;
    }

    public boolean isProjecting() {
        return projecting;
    }

    public ItemStack getRenderStack() {
        return inventoryHelper.getStackInSlot(SLOT_CARD);
    }

    public ShapeRenderer getShapeRenderer() {
        if (shapeRenderer == null) {
            shapeRenderer = new ShapeRenderer(getShapeID());
        }
        return shapeRenderer;
    }

    @SuppressWarnings("NullableProblems")
    @SideOnly(Side.CLIENT)
    @Override
    public AxisAlignedBB getRenderBoundingBox() {
        return new AxisAlignedBB(pos.add(-5, 0, -5), pos.add(6, 5, 6));
    }

    private void markForNotification() {
        markDirtyQuick();
        doNotifyClients = true;
    }

    private void notifyClients() {
        int dimension = getWorld().provider.getDimension();
        double x = getPos().getX();
        double y = getPos().getY();
        double z = getPos().getZ();
        double sqradius = 40 * 40;
        for (EntityPlayerMP player : getWorld().getMinecraftServer().getPlayerList().getPlayers()) {
            if (player.dimension == dimension) {
                double d0 = x - player.posX;
                double d1 = y - player.posY;
                double d2 = z - player.posZ;
                if (d0 * d0 + d1 * d1 + d2 * d2 < sqradius) {
                    RFToolsMessages.INSTANCE.sendTo(new PacketProjectorClientNotification(this), player);
                }
            }
        }
    }

    public void updateFromServer(PacketProjectorClientNotification message) {
        verticalOffset = message.getVerticalOffset();
        scale = message.getScale();
        angle = message.getAngle();
        autoRotate = message.isAutoRotate();
        projecting = message.isProjecting();
        scanline = message.isScanline();
        sound = message.isSound();
        if (grayscale != message.isGrayscale()) {
            grayscale = message.isGrayscale();
            shapeRenderer = null;
            scanNeeded = true;
        }
        if (counter != message.getCounter()) {
            counter = message.getCounter();
            scanNeeded = true;
        }
    }

    @Override
    public boolean execute(EntityPlayerMP playerMP, String command, TypedMap params) {
        boolean rc = super.execute(playerMP, command, params);
        if (rc) {
            return true;
        }
        if (CMD_RSSETTINGS.equals(command)) {
            for (EnumFacing facing : EnumFacing.HORIZONTALS) {
                int idx = facing.ordinal()-2;
                String opOn = params.get(PARAM_OPON.get(idx));
                String opOff = params.get(PARAM_OPOFF.get(idx));
                Double valOn = params.get(PARAM_VALON.get(idx));
                Double valOff = params.get(PARAM_VALOFF.get(idx));
                operations[idx].setOpcodeOn(ProjectorOpcode.getByCode(opOn));
                operations[idx].setOpcodeOff(ProjectorOpcode.getByCode(opOff));
                operations[idx].setValueOn(valOn);
                operations[idx].setValueOff(valOff);
            }
            markDirtyClient();
            updateOperations(false);
            return true;
        } else if (CMD_SETTINGS.equals(command)) {
            int scaleInt = params.get(PARAM_SCALE);
            int offsetInt = params.get(PARAM_OFFSET);
            int angleInt = params.get(PARAM_ANGLE);
            setScaleInt(scaleInt);
            setOffsetInt(offsetInt);
            setAngleInt(angleInt);
            autoRotate = params.get(PARAM_AUTO);
            scanline = params.get(PARAM_SCAN);
            sound = params.get(PARAM_SOUND);
            grayscale = params.get(PARAM_GRAY);
            markDirtyClient();
            return true;
        }
        return false;
    }

    @Override
    public void checkRedstone(World world, BlockPos pos) {
        IBlockState state = world.getBlockState(pos);
        if (state.getBlock() == BuilderSetup.projectorBlock) {
            EnumFacing horiz = OrientationTools.getOrientationHoriz(state);
            EnumFacing north = reorient(EnumFacing.NORTH, horiz);
            EnumFacing south = reorient(EnumFacing.SOUTH, horiz);
            EnumFacing west = reorient(EnumFacing.WEST, horiz);
            EnumFacing east = reorient(EnumFacing.EAST, horiz);

            int powered1 = getInputStrength(world, pos, north) > 0 ? 1 : 0;
            int powered2 = getInputStrength(world, pos, south) > 0 ? 2 : 0;
            int powered3 = getInputStrength(world, pos, west) > 0 ? 4 : 0;
            int powered4 = getInputStrength(world, pos, east) > 0 ? 8 : 0;
            setPowerInput(powered1 + powered2 + powered3 + powered4);
        }
    }

    private static EnumFacing reorient(EnumFacing side, EnumFacing blockDirection) {
        switch (blockDirection) {
            case NORTH:
                if (side == DOWN || side == UP) {
                    return side;
                }
                return side.getOpposite();
            case SOUTH:
                return side;
            case WEST:
                if (side == DOWN || side == UP) {
                    return side;
                } else if (side == WEST) {
                    return NORTH;
                } else if (side == NORTH) {
                    return EAST;
                } else if (side == EAST) {
                    return SOUTH;
                } else {
                    return WEST;
                }
            case EAST:
                if (side == DOWN || side == UP) {
                    return side;
                } else if (side == WEST) {
                    return SOUTH;
                } else if (side == NORTH) {
                    return WEST;
                } else if (side == EAST) {
                    return NORTH;
                } else {
                    return EAST;
                }
            default:
                return side;
        }
    }


    /**
     * Returns the signal strength at one side of the block
     */
    private int getInputStrength(World world, BlockPos pos, EnumFacing side) {
        int power = world.getRedstonePower(pos.offset(side), side);
        if (power == 0) {
            // Check if there is no redstone wire there. If there is a 'bend' in the redstone wire it is
            // not detected with world.getRedstonePower().
            // @todo this is a bit of a hack. Don't know how to do it better right now
            IBlockState blockState = world.getBlockState(pos.offset(side));
            Block b = blockState.getBlock();
            if (b == Blocks.REDSTONE_WIRE) {
                power = world.isBlockPowered(pos.offset(side)) ? 15 : 0;
            }
        }

        return power;
    }

}
