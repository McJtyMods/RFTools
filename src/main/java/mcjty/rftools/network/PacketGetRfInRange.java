package mcjty.rftools.network;

import io.netty.buffer.ByteBuf;
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
            World world = ctx.getServerHandler().playerEntity.worldObj;
            Map<BlockPos, EnergyTools.EnergyLevel> result = new HashMap<>();
            int range = 12;
            for (int x = -range; x <= range; x++) {
                for (int y = -range; y <= range; y++) {
                    for (int z = -range; z <= range; z++) {
                        BlockPos p = message.pos.add(x, y, z);
                        TileEntity te = world.getTileEntity(p);
                        if (EnergyTools.isEnergyTE(te)) {
                            EnergyTools.EnergyLevel level = EnergyTools.getEnergyLevel(te);
                            result.put(p, level);
                        }
                    }
                }
            }

            RFToolsMessages.INSTANCE.sendTo(new PacketReturnRfInRange(result), ctx.getServerHandler().playerEntity);
        }

    }

}