package mcjty.rftools.blocks.shaper;

import mcjty.lib.container.DefaultSidedInventory;
import mcjty.lib.container.InventoryHelper;
import mcjty.lib.entity.GenericEnergyReceiverTileEntity;
import mcjty.lib.network.Argument;
import mcjty.lib.varia.BlockPosTools;
import mcjty.lib.varia.RedstoneMode;
import mcjty.rftools.blocks.builder.BuilderConfiguration;
import mcjty.rftools.blocks.builder.BuilderSetup;
import mcjty.rftools.items.builder.ShapeCardItem;
import mcjty.rftools.shapes.ShapeID;
import mcjty.rftools.shapes.ShapeRenderer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.Map;

public class ProjectorTileEntity extends GenericEnergyReceiverTileEntity implements DefaultSidedInventory, ITickable {

    public static final String CMD_SETTINGS = "settings";
    public static final String CMD_MODE = "setMode";

    private InventoryHelper inventoryHelper = new InventoryHelper(this, ProjectorContainer.factory, 1);
    private ShapeRenderer shapeRenderer = null;

    private float verticalOffset = .2f;
    private float scale = 0.01f;
    private float angle = 0.0f;
    private boolean autoRotate = false;
    private boolean active = false;

    public ProjectorTileEntity() {
        super(BuilderConfiguration.PROJECTOR_MAXENERGY, BuilderConfiguration.PROJECTOR_RECEIVEPERTICK);
    }

    @Override
    public void update() {
        if (!getWorld().isRemote) {
            boolean a = isMachineEnabled();
            if (a) {
                if (getEnergyStored() < BuilderConfiguration.PROJECTOR_USEPERTICK) {
                    a = false;
                }
            }

            if (a != active) {
                active = a;
                markDirtyClient();
            }

            if (active) {
                consumeEnergy(BuilderConfiguration.PROJECTOR_USEPERTICK);
            }
        } else {
            if (autoRotate) {
                angle += 1;
                if (angle >= 360) {
                    angle = 0;
                }
            }
        }
    }

    @Override
    public InventoryHelper getInventoryHelper() {
        return inventoryHelper;
    }

    @Override
    protected boolean needsRedstoneMode() {
        return true;
    }


    @Override
    public boolean isUsable(EntityPlayer player) {
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
        active = tagCompound.getBoolean("active");
    }

    @Override
    public void writeRestorableToNBT(NBTTagCompound tagCompound) {
        super.writeRestorableToNBT(tagCompound);
        writeBufferToNBT(tagCompound, inventoryHelper);
        tagCompound.setFloat("offs", verticalOffset);
        tagCompound.setFloat("scale", scale);
        tagCompound.setFloat("angle", angle);
        tagCompound.setBoolean("rot", autoRotate);
        tagCompound.setBoolean("active", active);
    }

    public float getVerticalOffset() {
        return verticalOffset;
    }

    public int getOffsetInt() {
        return (int) (verticalOffset * 20);
    }

    private void setOffsetInt(int o) {
        verticalOffset = o / 20.0f;
    }

    public float getScale() {
        return scale;
    }

    // 0 -> 0.001
    // 100 -> 0.1
    public int getScaleInt() {
        return (int) (20.0 * Math.log((scale - 0.001f) / 0.1f * 147.4131f + 1));
    }

    private void setScaleInt(int s) {
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

    public boolean isActive() {
        return active;
    }

    public ItemStack getRenderStack() {
        return inventoryHelper.getStackInSlot(ProjectorContainer.SLOT_CARD);
    }

    public ShapeRenderer getShapeRenderer() {
        if (shapeRenderer == null) {
            shapeRenderer = new ShapeRenderer(new ShapeID(getWorld().provider.getDimension(), getPos(), ShapeCardItem.getCheck(getRenderStack())));
        }
        return shapeRenderer;
    }

    @SuppressWarnings("NullableProblems")
    @SideOnly(Side.CLIENT)
    @Override
    public AxisAlignedBB getRenderBoundingBox() {
        return new AxisAlignedBB(pos.add(-5, 0, -5), pos.add(6, 5, 6));
    }

    @Override
    public boolean execute(EntityPlayerMP playerMP, String command, Map<String, Argument> args) {
        boolean rc = super.execute(playerMP, command, args);
        if (rc) {
            return true;
        }
        if (CMD_MODE.equals(command)) {
            String m = args.get("rs").getString();
            setRSMode(RedstoneMode.getMode(m));
            return true;
        } else if (CMD_SETTINGS.equals(command)) {
            int scaleInt = args.get("scale").getInteger();
            int offsetInt = args.get("offset").getInteger();
            int angleInt = args.get("angle").getInteger();
            setScaleInt(scaleInt);
            setOffsetInt(offsetInt);
            setAngleInt(angleInt);
            autoRotate = args.get("auto").getBoolean();
            markDirtyClient();
            return true;
        }
        return false;
    }
}
