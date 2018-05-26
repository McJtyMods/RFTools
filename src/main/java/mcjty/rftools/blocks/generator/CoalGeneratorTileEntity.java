package mcjty.rftools.blocks.generator;


import mcjty.lib.McJtyLib;
import mcjty.lib.api.information.IMachineInformation;
import mcjty.lib.compat.RedstoneFluxCompatibility;
import mcjty.lib.container.ContainerFactory;
import mcjty.lib.container.DefaultSidedInventory;
import mcjty.lib.container.InventoryHelper;
import mcjty.lib.tileentity.GenericEnergyStorageTileEntity;
import mcjty.lib.gui.widgets.ImageChoiceLabel;
import mcjty.lib.varia.EnergyTools;
import mcjty.lib.varia.RedstoneMode;
import mcjty.rftools.RFTools;
import mcjty.theoneprobe.api.IProbeHitData;
import mcjty.theoneprobe.api.IProbeInfo;
import mcjty.theoneprobe.api.ProbeMode;
import mcjty.lib.typed.TypedMap;
import mcp.mobius.waila.api.IWailaConfigHandler;
import mcp.mobius.waila.api.IWailaDataAccessor;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.fml.common.Optional;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.List;

public class CoalGeneratorTileEntity extends GenericEnergyStorageTileEntity implements ITickable, DefaultSidedInventory,
        IMachineInformation {

    public static final String CMD_RSMODE = "coalgen.setRsMode";

    public static final PropertyBool WORKING = PropertyBool.create("working");

    public static final int SLOT_COALINPUT = 0;
    public static final int SLOT_CHARGEITEM = 1;

    public static final ContainerFactory CONTAINER_FACTORY = new ContainerFactory(new ResourceLocation(RFTools.MODID, "gui/coalgenerator.gui"));

    private InventoryHelper inventoryHelper = new InventoryHelper(this, CONTAINER_FACTORY, 2);

    private int burning;

    public CoalGeneratorTileEntity() {
        super(CoalGeneratorConfiguration.MAXENERGY, CoalGeneratorConfiguration.SENDPERTICK);
    }

    @Override
    public long getEnergyDiffPerTick() {
        return burning > 0 ? getRfPerTick() : 0;
    }

    @Nullable
    @Override
    public String getEnergyUnitName() {
        return "RF";
    }

    @Override
    public boolean isMachineActive() {
        return isMachineEnabled();
    }

    @Override
    public boolean isMachineRunning() {
        return isMachineEnabled();
    }

    @Nullable
    @Override
    public String getMachineStatus() {
        return burning > 0 ? "generating power" : "idle";
    }

    @Override
    protected boolean needsCustomInvWrapper() {
        return true;
    }

    @Override
    protected boolean needsRedstoneMode() {
        return true;
    }

    @Override
    public void setPowerInput(int powered) {
        boolean changed = powerLevel != powered;
        super.setPowerInput(powered);
        if (changed) {
            markDirtyClient();
        }
    }

    @Override
    public void update() {
        if (!getWorld().isRemote) {
            handleChargingItem();
            handleSendingEnergy();

            if (!isMachineEnabled()) {
                return;
            }

            boolean working = burning > 0;

            if (burning > 0) {
                burning--;
                int rf = getRfPerTick();
                modifyEnergyStored(rf);
                if (burning == 0) {
                    markDirtyClient();
                } else {
                    markDirty();
                }
                return;
            }

            if (inventoryHelper.containsItem(SLOT_COALINPUT)) {
                ItemStack extracted = inventoryHelper.decrStackSize(SLOT_COALINPUT, 1);
                burning = CoalGeneratorConfiguration.ticksPerCoal;
                if (extracted.getItem() == Item.getItemFromBlock(Blocks.COAL_BLOCK)) {
                    burning *= 9;
                }
                burning += (int) (burning * getInfusedFactor() / 2.0f);
                if (working) {
                    markDirty();
                } else {
                    markDirtyClient();
                }
            } else {
                if (working) {
                    markDirtyClient();
                }
            }
        }
    }

    public int getRfPerTick() {
        int rf = CoalGeneratorConfiguration.rfPerTick;
        rf += (int) (rf * getInfusedFactor());
        return rf;
    }

    @Override
    public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity packet) {
        boolean working = isWorking();

        super.onDataPacket(net, packet);

        if (getWorld().isRemote) {
            // If needed send a render update.
            boolean newWorking = isWorking();
            if (newWorking != working) {
                getWorld().markBlockRangeForRenderUpdate(getPos(), getPos());
            }
        }
    }

    public boolean isWorking() {
        return burning > 0 && isMachineEnabled();
    }

    private void handleChargingItem() {
        ItemStack stack = inventoryHelper.getStackInSlot(SLOT_CHARGEITEM);
        if (stack.isEmpty()) {
            return;
        }
        int energyStored = getEnergyStored();
        int rfToGive = Math.min(CoalGeneratorConfiguration.CHARGEITEMPERTICK, energyStored);
        int received = (int)EnergyTools.receiveEnergy(stack, rfToGive);
        storage.extractEnergy(received, false);
    }

    private void handleSendingEnergy() {
        int energyStored = getEnergyStored();

        for (EnumFacing facing : EnumFacing.VALUES) {
            BlockPos pos = getPos().offset(facing);
            TileEntity te = getWorld().getTileEntity(pos);
            EnumFacing opposite = facing.getOpposite();
            if (EnergyTools.isEnergyTE(te, opposite)) {
                int rfToGive = Math.min(CoalGeneratorConfiguration.SENDPERTICK, energyStored);
                int received = (int) EnergyTools.receiveEnergy(te, opposite, rfToGive);
                energyStored -= storage.extractEnergy(received, false);
                if (energyStored <= 0) {
                    break;
                }
            }
        }
    }

    @Override
    public InventoryHelper getInventoryHelper() {
        return inventoryHelper;
    }

    @Override
    public int[] getSlotsForFace(EnumFacing side) {
        return new int[] { SLOT_COALINPUT };
    }

    @Override
    public boolean canInsertItem(int index, ItemStack stack, EnumFacing direction) {
        return isItemValidForSlot(index, stack);
    }

    @Override
    public boolean isItemValidForSlot(int index, ItemStack stack) {
        if (index == SLOT_CHARGEITEM) {
            return EnergyTools.isEnergyItem(stack);
        } else if (index == SLOT_COALINPUT) {
            return stack.getItem() == Items.COAL || stack.getItem() == Item.getItemFromBlock(Blocks.COAL_BLOCK);
        }
        return true;
    }

    @Override
    public boolean canExtractItem(int index, ItemStack stack, EnumFacing direction) {
        return false;
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
    public void readRestorableFromNBT(NBTTagCompound tagCompound) {
        super.readRestorableFromNBT(tagCompound);
        burning = tagCompound.getInteger("burning");
        readBufferFromNBT(tagCompound, inventoryHelper);
    }

    @Override
    public void writeRestorableToNBT(NBTTagCompound tagCompound) {
        super.writeRestorableToNBT(tagCompound);
        tagCompound.setInteger("burning", burning);
        writeBufferToNBT(tagCompound, inventoryHelper);
    }

    @Override
    public boolean execute(EntityPlayerMP playerMP, String command, TypedMap params) {
        boolean rc = super.execute(playerMP, command, params);
        if (rc) {
            return true;
        }
        if (CMD_RSMODE.equals(command)) {
            setRSMode(RedstoneMode.values()[params.get(ImageChoiceLabel.PARAM_CHOICE_IDX)]);
            return true;
        }

        return false;
    }

    @Override
    public IBlockState getActualState(IBlockState state) {
        return state.withProperty(WORKING, isWorking());
    }

    @Override
    @Optional.Method(modid = "theoneprobe")
    public void addProbeInfo(ProbeMode mode, IProbeInfo probeInfo, EntityPlayer player, World world, IBlockState blockState, IProbeHitData data) {
        super.addProbeInfo(mode, probeInfo, player, world, blockState, data);
        Boolean working = isWorking();
        if (working) {
            probeInfo.text(TextFormatting.GREEN + "Producing " + getRfPerTick() + " RF/t");
        }
    }

    @SideOnly(Side.CLIENT)
    @Override
    @Optional.Method(modid = "waila")
    public void addWailaBody(ItemStack itemStack, List<String> currenttip, IWailaDataAccessor accessor, IWailaConfigHandler config) {
        super.addWailaBody(itemStack, currenttip, accessor, config);
        if (isWorking()) {
            currenttip.add(TextFormatting.GREEN + "Producing " + getRfPerTick() + " RF/t");
        }
    }
}
