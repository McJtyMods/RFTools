package mcjty.rftools.blocks.shaper;

import mcjty.lib.container.ContainerFactory;
import mcjty.lib.container.NoDirectionItemHander;
import mcjty.lib.container.SlotDefinition;
import mcjty.lib.container.SlotType;
import mcjty.lib.tileentity.GenericEnergyStorage;
import mcjty.lib.tileentity.GenericTileEntity;
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
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.fml.server.ServerLifecycleHooks;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.List;

import static mcjty.rftools.blocks.builder.BuilderSetup.TYPE_PROJECTOR;
import static net.minecraft.util.Direction.*;

public class ProjectorTileEntity extends GenericTileEntity implements ITickableTileEntity {

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

//    private InventoryHelper inventoryHelper = new InventoryHelper(this, CONTAINER_FACTORY, 1);
    private LazyOptional<NoDirectionItemHander> itemHandler = LazyOptional.of(this::createItemHandler);
    private LazyOptional<GenericEnergyStorage> energyHandler = LazyOptional.of(() -> new GenericEnergyStorage(this, true, ScannerConfiguration.PROJECTOR_MAXENERGY.get(), ScannerConfiguration.PROJECTOR_RECEIVEPERTICK.get()));


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
        super(TYPE_PROJECTOR);
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
    public void tick() {
        if (!world.isRemote) {
            updateOperations(false);

            energyHandler.ifPresent(h -> {
                boolean a = active;
                if (a) {
                    if (h.getEnergyStored() < ScannerConfiguration.PROJECTOR_USEPERTICK.get()) {
                        a = false;
                    }
                }

                if (a != projecting) {
                    projecting = a;
                    markForNotification();
                }

                if (projecting) {
                    h.consumeEnergy(ScannerConfiguration.PROJECTOR_USEPERTICK.get());
                }
                if (doNotifyClients) {
                    doNotifyClients = false;
                    notifyClients();
                }
            });
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
    public void remove() {
        super.remove();
        if (world.isRemote) {
            stopSounds();
        }
    }

    private void stopSounds() {
        ProjectorSounds.stopSound(getPos());
    }

    public ShapeID getShapeID() {
        int scanId = ShapeCardItem.getScanId(getRenderStack());
        boolean isSolid = ShapeCardItem.isSolid(getRenderStack());
        if (scanId == 0) {
            return new ShapeID(world.getDimension().getType().getId(), getPos(), scanId, isGrayscale(), isSolid);
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
        for (Direction facing : OrientationTools.HORIZONTAL_DIRECTION_VALUES) {
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

    // @todo 1.14 loot tables
    @Override
    public void read(CompoundNBT tagCompound) {
        super.read(tagCompound);
        itemHandler.ifPresent(h -> h.deserializeNBT(tagCompound.getList("Items", Constants.NBT.TAG_COMPOUND)));
        energyHandler.ifPresent(h -> h.setEnergy(tagCompound.getLong("Energy")));
        verticalOffset = tagCompound.contains("offs") ? tagCompound.getFloat("offs") : .2f;
        scale = tagCompound.contains("scale") ? tagCompound.getFloat("scale") : .01f;
        angle = tagCompound.contains("angle") ? tagCompound.getFloat("angle") : .0f;
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
        counter = tagCompound.getInt("counter");
        for (Direction facing : OrientationTools.HORIZONTAL_DIRECTION_VALUES) {
            if (tagCompound.contains("op_"+facing.getName())) {
                int index = facing.ordinal() - 2;
                ProjectorOperation op = operations[index];
                CompoundNBT tc = (CompoundNBT) tagCompound.get("op_" + facing.getName());
                String on = tc.getString("on");
                Double von = null;
                if (tc.contains("von")) {
                    von = tc.getDouble("von");
                }
                String off = tc.getString("off");
                Double voff = null;
                if (tc.contains("voff")) {
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
    public CompoundNBT write(CompoundNBT tagCompound) {
        super.write(tagCompound);
        itemHandler.ifPresent(h -> tagCompound.put("Items", h.serializeNBT()));
        energyHandler.ifPresent(h -> tagCompound.putLong("Energy", h.getEnergy()));
        tagCompound.putFloat("offs", verticalOffset);
        tagCompound.putFloat("scale", scale);
        tagCompound.putFloat("angle", angle);
        tagCompound.putBoolean("rot", autoRotate);
        tagCompound.putBoolean("scan", scanline);
        tagCompound.putBoolean("sound", sound);
        tagCompound.putBoolean("grayscale", grayscale);
        tagCompound.putBoolean("projecting", projecting);
        tagCompound.putBoolean("active", active);
        tagCompound.putInt("counter", counter);
        for (Direction facing : OrientationTools.HORIZONTAL_DIRECTION_VALUES) {
            int index = facing.ordinal() - 2;
            ProjectorOperation op = operations[index];
            CompoundNBT tc = new CompoundNBT();
            tc.putString("on", op.getOpcodeOn().getCode());
            if (op.getValueOn() != null) {
                tc.putDouble("von", op.getValueOn());
            }
            tc.putString("off", op.getOpcodeOff().getCode());
            if (op.getValueOff() != null) {
                tc.putDouble("voff", op.getValueOff());
            }
            tagCompound.put("op_"+facing.getName(), tc);
        }
        return tagCompound;
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
        return itemHandler.map(h -> h.getStackInSlot(SLOT_CARD)).orElse(ItemStack.EMPTY);
    }

    public ShapeRenderer getShapeRenderer() {
        if (shapeRenderer == null) {
            shapeRenderer = new ShapeRenderer(getShapeID());
        }
        return shapeRenderer;
    }

    @SuppressWarnings("NullableProblems")
    @Override
    public AxisAlignedBB getRenderBoundingBox() {
        return new AxisAlignedBB(pos.add(-5, 0, -5), pos.add(6, 5, 6));
    }

    private void markForNotification() {
        markDirtyQuick();
        doNotifyClients = true;
    }

    private void notifyClients() {
        DimensionType dimension = world.getDimension().getType();
        double x = getPos().getX();
        double y = getPos().getY();
        double z = getPos().getZ();
        double sqradius = 40 * 40;
        for (ServerPlayerEntity player : ServerLifecycleHooks.getCurrentServer().getPlayerList().getPlayers()) {
            if (player.dimension.equals(dimension)) {
                double d0 = x - player.posX;
                double d1 = y - player.posY;
                double d2 = z - player.posZ;
                if (d0 * d0 + d1 * d1 + d2 * d2 < sqradius) {
                    RFToolsMessages.INSTANCE.sendTo(new PacketProjectorClientNotification(this), player.connection.netManager, NetworkDirection.PLAY_TO_CLIENT);
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
    public boolean execute(PlayerEntity playerMP, String command, TypedMap params) {
        boolean rc = super.execute(playerMP, command, params);
        if (rc) {
            return true;
        }
        if (CMD_RSSETTINGS.equals(command)) {
            for (Direction facing : OrientationTools.HORIZONTAL_DIRECTION_VALUES) {
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
        BlockState state = world.getBlockState(pos);
        if (state.getBlock() == BuilderSetup.projectorBlock) {
            Direction horiz = OrientationTools.getOrientationHoriz(state);
            Direction north = reorient(Direction.NORTH, horiz);
            Direction south = reorient(Direction.SOUTH, horiz);
            Direction west = reorient(Direction.WEST, horiz);
            Direction east = reorient(Direction.EAST, horiz);

            int powered1 = getInputStrength(world, pos, north) > 0 ? 1 : 0;
            int powered2 = getInputStrength(world, pos, south) > 0 ? 2 : 0;
            int powered3 = getInputStrength(world, pos, west) > 0 ? 4 : 0;
            int powered4 = getInputStrength(world, pos, east) > 0 ? 8 : 0;
            setPowerInput(powered1 + powered2 + powered3 + powered4);
        }
    }

    private static Direction reorient(Direction side, Direction blockDirection) {
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
    private int getInputStrength(World world, BlockPos pos, Direction side) {
        int power = world.getRedstonePower(pos.offset(side), side);
        if (power == 0) {
            // Check if there is no redstone wire there. If there is a 'bend' in the redstone wire it is
            // not detected with world.getRedstonePower().
            // @todo this is a bit of a hack. Don't know how to do it better right now
            BlockState blockState = world.getBlockState(pos.offset(side));
            Block b = blockState.getBlock();
            if (b == Blocks.REDSTONE_WIRE) {
                power = world.isBlockPowered(pos.offset(side)) ? 15 : 0;
            }
        }

        return power;
    }

    private NoDirectionItemHander createItemHandler() {
        return new NoDirectionItemHander(ProjectorTileEntity.this, CONTAINER_FACTORY, 1) {
            @Override
            public boolean isItemValid(int slot, @Nonnull ItemStack stack) {
                return stack.getItem() == BuilderSetup.shapeCardItem;
            }
        };
    }

}
