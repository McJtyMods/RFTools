package mcjty.rftools.blocks.shaper;

import mcjty.lib.container.DefaultSidedInventory;
import mcjty.lib.container.InventoryHelper;
import mcjty.lib.entity.GenericEnergyReceiverTileEntity;
import mcjty.lib.network.Argument;
import mcjty.rftools.blocks.builder.BuilderSetup;
import mcjty.rftools.items.builder.ShapeCardItem;
import mcjty.rftools.network.RFToolsMessages;
import mcjty.rftools.shapes.RenderData;
import mcjty.rftools.shapes.ShapeID;
import mcjty.rftools.shapes.ShapeRenderer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.Map;

public class ProjectorTileEntity extends GenericEnergyReceiverTileEntity implements DefaultSidedInventory, ITickable {

    public static final String CMD_SETTINGS = "settings";
    public static final String CMD_RSSETTINGS = "rsSettings";

    private InventoryHelper inventoryHelper = new InventoryHelper(this, ProjectorContainer.factory, 1);
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
    private int counter = 0;    // Counter to detect that we need to do a new 'scan' client-side

    // Needed client side but set on client when 'counter' changes
    private boolean scanNeeded = false;

    // Set on server
    private boolean doNotifyClients = false;  // Set to true to notify clients

    public ProjectorTileEntity() {
        super(ScannerConfiguration.PROJECTOR_MAXENERGY, ScannerConfiguration.PROJECTOR_RECEIVEPERTICK);
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
                if (getEnergyStored() < ScannerConfiguration.PROJECTOR_USEPERTICK) {
                    a = false;
                }
            }

            if (a != projecting) {
                projecting = a;
                markForNotification();
            }

            if (projecting) {
                consumeEnergy(ScannerConfiguration.PROJECTOR_USEPERTICK);
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


    public ShapeID getShapeID() {
        int scanId = ShapeCardItem.getScanId(getRenderStack());
        if (scanId == 0) {
            return new ShapeID(getWorld().provider.getDimension(), getPos(), scanId);
        } else {
            return new ShapeID(0, null, scanId);
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

    public boolean isProjecting() {
        return projecting;
    }

    public ItemStack getRenderStack() {
        return inventoryHelper.getStackInSlot(ProjectorContainer.SLOT_CARD);
    }

    public ShapeRenderer getShapeRenderer() {
        ItemStack renderStack = getRenderStack();
        if (shapeRenderer == null) {
            int scanId = ShapeCardItem.getScanId(renderStack);
            if (scanId == 0) {
                shapeRenderer = new ShapeRenderer(new ShapeID(getWorld().provider.getDimension(), getPos(), scanId));
            } else {
                shapeRenderer = new ShapeRenderer(new ShapeID(0, null, scanId));
            }
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
        if (counter != message.getCounter()) {
            counter = message.getCounter();
            scanNeeded = true;
        }
    }

    @Override
    public boolean execute(EntityPlayerMP playerMP, String command, Map<String, Argument> args) {
        boolean rc = super.execute(playerMP, command, args);
        if (rc) {
            return true;
        }
        if (CMD_RSSETTINGS.equals(command)) {
            for (EnumFacing facing : EnumFacing.HORIZONTALS) {
                int idx = facing.ordinal()-2;
                String opOn = args.get("opOn"+idx).getString();
                String opOff = args.get("opOff"+idx).getString();
                Double valOn = null;
                Double valOff = null;
                if (args.containsKey("valOn"+idx)) {
                    valOn = args.get("valOn"+idx).getDouble();
                }
                if (args.containsKey("valOff"+idx)) {
                    valOff = args.get("valOff"+idx).getDouble();
                }
                operations[idx].setOpcodeOn(ProjectorOpcode.getByCode(opOn));
                operations[idx].setOpcodeOff(ProjectorOpcode.getByCode(opOff));
                operations[idx].setValueOn(valOn);
                operations[idx].setValueOff(valOff);
            }
            markDirtyClient();
            updateOperations(false);
            return true;
        } else if (CMD_SETTINGS.equals(command)) {
            int scaleInt = args.get("scale").getInteger();
            int offsetInt = args.get("offset").getInteger();
            int angleInt = args.get("angle").getInteger();
            setScaleInt(scaleInt);
            setOffsetInt(offsetInt);
            setAngleInt(angleInt);
            autoRotate = args.get("auto").getBoolean();
            scanline = args.get("scan").getBoolean();
            sound = args.get("sound").getBoolean();
            markDirtyClient();
            return true;
        }
        return false;
    }
}
