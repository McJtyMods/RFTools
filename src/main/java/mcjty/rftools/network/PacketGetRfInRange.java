package mcjty.rftools.network;

import io.netty.buffer.ByteBuf;
import mcjty.lib.api.information.IMachineInformation;
import mcjty.lib.network.NetworkTools;
import mcjty.lib.varia.EnergyTools;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import java.util.HashMap;
import java.util.Map;

public class PacketGetRfInRange implements IMessage {

    public static class MachineInfo {
        private final int energy;
        private final int maxEnergy;
        private final Integer energyPerTick;

        public MachineInfo(int energy, int maxEnergy, Integer energyPerTick) {
            this.energy = energy;
            this.maxEnergy = maxEnergy;
            this.energyPerTick = energyPerTick;
        }

        public int getEnergy() {
            return energy;
        }

        public int getMaxEnergy() {
            return maxEnergy;
        }

        public Integer getEnergyPerTick() {
            return energyPerTick;
        }
    }


    private BlockPos pos;

    @Override
    public void fromBytes(ByteBuf buf) {
        pos = NetworkTools.readPos(buf);
    }

    @Override
    public void toBytes(ByteBuf buf) {
        NetworkTools.writePos(buf, pos);
    }

    public PacketGetRfInRange() {
    }

    public PacketGetRfInRange(BlockPos pos) {
        this.pos = pos;
    }

    public static class Handler implements IMessageHandler<PacketGetRfInRange, IMessage> {
        @Override
        public IMessage onMessage(PacketGetRfInRange message, MessageContext ctx) {
            FMLCommonHandler.instance().getWorldThread(ctx.netHandler).addScheduledTask(() -> handle(message, ctx));
            return null;
        }

        private void handle(PacketGetRfInRange message, MessageContext ctx) {
            World world = ctx.getServerHandler().player.getEntityWorld();
            Map<BlockPos, MachineInfo> result = new HashMap<>();
            int range = 12;
            for (int x = -range; x <= range; x++) {
                for (int y = -range; y <= range; y++) {
                    for (int z = -range; z <= range; z++) {
                        BlockPos p = message.pos.add(x, y, z);
                        TileEntity te = world.getTileEntity(p);
                        if (EnergyTools.isEnergyTE(te)) {
                            EnergyTools.EnergyLevel level = EnergyTools.getEnergyLevel(te);
                            Integer usage = null;
                            if (te instanceof IMachineInformation) {
                                usage = ((IMachineInformation) te).getEnergyDiffPerTick();
                            }
                            result.put(p, new MachineInfo(level.getEnergy(), level.getMaxEnergy(), usage));
                        }
                    }
                }
            }

            RFToolsMessages.INSTANCE.sendTo(new PacketReturnRfInRange(result), ctx.getServerHandler().player);
        }

    }

}