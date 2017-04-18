package mcjty.rftools.xnet;

import mcjty.lib.varia.WorldTools;
import mcjty.rftools.RFTools;
import mcjty.rftools.blocks.storagemonitor.StorageScannerTileEntity;
import mcjty.xnet.api.channels.IChannelSettings;
import mcjty.xnet.api.channels.IConnectorSettings;
import mcjty.xnet.api.channels.IControllerContext;
import mcjty.xnet.api.gui.IEditorGui;
import mcjty.xnet.api.gui.IndicatorIcon;
import mcjty.xnet.api.helper.DefaultChannelSettings;
import mcjty.xnet.api.keys.SidedConsumer;
import net.minecraft.inventory.IInventory;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.items.CapabilityItemHandler;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

public class StorageChannelSettings extends DefaultChannelSettings implements IChannelSettings {

    public static final ResourceLocation iconGuiElements = new ResourceLocation(RFTools.MODID, "textures/gui/guielements.png");

    private List<Pair<SidedConsumer, StorageConnectorSettings>> storageControllers = null;
    private List<Pair<SidedConsumer, StorageConnectorSettings>> inputOnly = null;
    private List<Pair<SidedConsumer, StorageConnectorSettings>> outputOnly = null;
    private List<Pair<SidedConsumer, StorageConnectorSettings>> inputAndOutput = null;

    @Override
    public void readFromNBT(NBTTagCompound tag) {

    }

    @Override
    public void writeToNBT(NBTTagCompound tag) {

    }

    @Override
    public void tick(int channel, IControllerContext context) {
        if (updateCache(channel, context)) {
            World world = context.getControllerWorld();
            for (Pair<SidedConsumer, StorageConnectorSettings> entry : storageControllers) {
                BlockPos extractorPos = context.findConsumerPosition(entry.getKey().getConsumerId());
                if (extractorPos != null) {
                    EnumFacing side = entry.getKey().getSide();
                    BlockPos pos = extractorPos.offset(side);
                    if (!WorldTools.chunkLoaded(world, pos)) {
                        continue;
                    }

                    TileEntity te = world.getTileEntity(pos);
                    if (te instanceof StorageScannerTileEntity) {
                        StorageScannerTileEntity scanner = (StorageScannerTileEntity) te;
                        List<BlockPos> in = inputOnly.stream().map(isInventory(context)).filter(Objects::nonNull).collect(Collectors.toList());
                        List<BlockPos> out = outputOnly.stream().map(isInventory(context)).filter(Objects::nonNull).collect(Collectors.toList());
                        List<BlockPos> inOut = inputAndOutput.stream().map(isInventory(context)).filter(Objects::nonNull).collect(Collectors.toList());
                        scanner.register(in, out, inOut);
                    }
                }
            }
        }
    }

    private Function<Pair<SidedConsumer, StorageConnectorSettings>, BlockPos> isInventory(IControllerContext context) {
        return pair -> {
            BlockPos invPos = context.findConsumerPosition(pair.getKey().getConsumerId());
            if (invPos != null) {
                TileEntity te = context.getControllerWorld().getTileEntity(invPos);
                if (te != null && (te instanceof IInventory || te.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null))) {
                    return invPos;
                }
            }
            return null;
        };
    }

    private boolean updateCache(int channel, IControllerContext context) {
        if (storageControllers == null) {
            storageControllers = new ArrayList<>();
            inputOnly = new ArrayList<>();
            outputOnly = new ArrayList<>();
            inputAndOutput = new ArrayList<>();

            Map<SidedConsumer, IConnectorSettings> connectors = context.getConnectors(channel);
            for (Map.Entry<SidedConsumer, IConnectorSettings> entry : connectors.entrySet()) {
                StorageConnectorSettings con = (StorageConnectorSettings) entry.getValue();
                switch (con.getMode()) {
                    case DUAL:
                        inputAndOutput.add(Pair.of(entry.getKey(), con));
                        break;
                    case INS:
                        inputOnly.add(Pair.of(entry.getKey(), con));
                        break;
                    case EXT:
                        outputOnly.add(Pair.of(entry.getKey(), con));
                        break;
                    case STORAGE:
                        storageControllers.add(Pair.of(entry.getKey(), con));
                        break;
                }
            }
            connectors = context.getRoutedConnectors(channel);
            for (Map.Entry<SidedConsumer, IConnectorSettings> entry : connectors.entrySet()) {
                StorageConnectorSettings con = (StorageConnectorSettings) entry.getValue();
                switch (con.getMode()) {
                    case DUAL:
                        inputAndOutput.add(Pair.of(entry.getKey(), con));
                        break;
                    case INS:
                        inputOnly.add(Pair.of(entry.getKey(), con));
                        break;
                    case EXT:
                        outputOnly.add(Pair.of(entry.getKey(), con));
                        break;
                    case STORAGE:
                        storageControllers.add(Pair.of(entry.getKey(), con));
                        break;
                }
            }
            return true;
        }
        return false;
    }


    @Override
    public void cleanCache() {
        storageControllers = null;
        inputOnly = null;
        outputOnly = null;
        inputAndOutput = null;
    }

    @Override
    public int getColors() {
        return 0;
    }

    @Nullable
    @Override
    public IndicatorIcon getIndicatorIcon() {
        return new IndicatorIcon(iconGuiElements, 0, 57, 11, 10);
    }

    @Nullable
    @Override
    public String getIndicator() {
        return null;
    }

    @Override
    public boolean isEnabled(String tag) {
        return true;
    }

    @Override
    public void createGui(IEditorGui gui) {

    }

    @Override
    public void update(Map<String, Object> data) {

    }
}
