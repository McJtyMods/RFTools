package mcjty.rftools.integration.computers;

import li.cil.oc.api.machine.Arguments;
import li.cil.oc.api.machine.Callback;
import li.cil.oc.api.machine.Context;
import li.cil.oc.api.network.ManagedEnvironment;
import mcjty.lib.integration.computers.AbstractOCDriver;
import mcjty.rftools.blocks.teleporter.*;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class DialingDeviceDriver {
    public static class OCDriver extends AbstractOCDriver {
        public OCDriver() {
            super("rftools_dialing_device", DialingDeviceTileEntity.class);
        }

        public static class InternalManagedEnvironment extends AbstractOCDriver.InternalManagedEnvironment<DialingDeviceTileEntity> {
            public InternalManagedEnvironment(DialingDeviceTileEntity tile) {
                super(tile, "rftools_dialing_device");
            }

            @Callback(doc = "function():number; Get the currently stored energy")
            public Object[] getEnergy(Context c, Arguments a) {
                return new Object[]{tile.getEnergyStored()};
            }

            @Callback(doc = "function():number; Get the maximum stored energy")
            public Object[] getMaxEnergy(Context c, Arguments a) {
                return new Object[]{tile.getMaxEnergyStored()};
            }

            @Callback(doc="function():table; Get a list of nearby matter transmitters")
            public Object[] getTransmitters(Context c, Arguments a) {
                return new Object[]{tile.searchTransmitters().stream().map((TransmitterInfo info) -> {
                    BlockPos pos = info.getCoordinate();
                    TileEntity transmitterTE = tile.getWorld().getTileEntity(pos);

                    Map<String, Object> transmitterInfo = new HashMap<>();
                    transmitterInfo.put("name", info.getName());
                    transmitterInfo.put("position", getCoordinateMap(pos));
                    transmitterInfo.put("dialed", transmitterTE instanceof MatterTransmitterTileEntity && ((MatterTransmitterTileEntity)transmitterTE).isDialed());
                    return transmitterInfo;
                }).collect(Collectors.toList())};
            }

            @Callback(doc="function():table; Get a list of valid matter receivers")
            public Object[] getReceivers(Context c, Arguments a) {
                TeleportDestinations receivers = TeleportDestinations.getDestinations(tile.getWorld());
                List<Map<String, Object>> ret = receivers.getValidDestinations(tile.getWorld(), null)
                        .stream().map((TeleportDestinationClientInfo destination) -> {
                            String name = destination.getName();
                            Map<String, Integer> pos = getCoordinateMap(destination.getCoordinate());
                            int dimension = destination.getDimension();
                            String dimName = destination.getDimensionName();

                            Map<String, Object> receiverInfo = new HashMap<>();
                            receiverInfo.put("name", name);
                            receiverInfo.put("position", pos);
                            receiverInfo.put("dimension", dimension);
                            receiverInfo.put("dimensionName", dimName);
                            return receiverInfo;
                        }).collect(Collectors.toList());
                return new Object[]{ret};
            }

            @Callback(doc="function(transmitter:table, receiver:table, targetDim:int, once:bool):true or nil, string; Dial the transmitter to the receiver. The table arguments are their respective positions as returned by getReceivers and getTransmitters")
            public Object[] dial(Context c, Arguments a) {
                Map transmitterSPos = a.checkTable(0);
                Map receiverSPos = a.checkTable(1);
                int targetDim = a.checkInteger(2);
                boolean once = a.checkBoolean(3);

                TileEntity transmitterTE = tile.getWorld().getTileEntity(fromCoordinateMap(transmitterSPos));
                TileEntity receiverTE = TeleportationTools.getWorldForDimension(tile.getWorld(), targetDim).getTileEntity(fromCoordinateMap(receiverSPos));

                if (!(transmitterTE instanceof MatterTransmitterTileEntity)) {
                    return new Object[]{null, "Invalid matter transmitter"};
                }

                if (!(receiverTE instanceof MatterReceiverTileEntity)) {
                    return new Object[]{null, "Invalid matter receiver"};
                }

                MatterTransmitterTileEntity transmitter = (MatterTransmitterTileEntity)transmitterTE;
                MatterReceiverTileEntity receiver = (MatterReceiverTileEntity)receiverTE;

                BlockPos transmitterPos = transmitter.getPos();
                BlockPos receiverPos = receiver.getPos();

                TeleportationTools.dial(tile.getWorld(), tile, null, transmitterPos, tile.getWorld().provider.getDimension(), receiverPos, targetDim, once);

                return new Object[]{true};
            }

            @Callback(doc="function(transmitter:table):true or nil, string; Interrupt the current beam")
            public Object[] interrupt(Context c, Arguments a) {
                Map transmitterSPos = a.checkTable(0);
                TileEntity transmitterTE = tile.getWorld().getTileEntity(fromCoordinateMap(transmitterSPos));

                if (!(transmitterTE instanceof MatterTransmitterTileEntity)) {
                    return new Object[]{null, "Invalid matter transmitter"};
                }

                MatterTransmitterTileEntity transmitter = (MatterTransmitterTileEntity)transmitterTE;
                BlockPos transmitterPos = transmitter.getPos();

                TeleportationTools.dial(tile.getWorld(), tile, null, transmitterPos, tile.getWorld().provider.getDimension(), null, 0, false);

                return new Object[]{true};
            }

            @Callback(doc="function():bool; Returns whether a matter booster is available or not")
            public Object[] isMatterBoosterAvailable(Context c, Arguments a) {
                boolean ret = DialingDeviceTileEntity.isMatterBoosterAvailable(tile.getWorld(), tile.getPos());
                return new Object[]{ret};
            }

            @Callback(doc="function():bool; Returns whether a destination analyzer is available or not")
            public Object[] isDestinationAnalyzerAvailable(Context c, Arguments a) {
                boolean ret = DialingDeviceTileEntity.isDestinationAnalyzerAvailable(tile.getWorld(), tile.getPos());
                return new Object[]{ret};
            }

            @Nonnull
            private static Map<String, Integer> getCoordinateMap(BlockPos pos) {
                Map<String, Integer> ret = new HashMap<>();
                ret.put("x", pos.getX());
                ret.put("y", pos.getY());
                ret.put("z", pos.getZ());
                return ret;
            }

            private static BlockPos fromCoordinateMap(Map<String, Double> map) {
                return new BlockPos(map.get("x"), map.get("y"), map.get("z"));
            }

            @Override
            public int priority() {
                return 4;
            }
        }

        @Override
        public ManagedEnvironment createEnvironment(World world, BlockPos pos, EnumFacing side, TileEntity tile) {
            return new InternalManagedEnvironment((DialingDeviceTileEntity) tile);
        }
    }
}
