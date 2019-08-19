package mcjty.rftools.items.creativeonly;

import io.netty.buffer.ByteBuf;
import mcjty.lib.network.NetworkTools;
import mcjty.lib.thirteen.Context;
import mcjty.rftools.network.RFToolsMessages;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public class PacketGetDelightingInfo implements IMessage {
    private BlockPos pos;

    @Override
    public void fromBytes(ByteBuf buf) {
        pos = NetworkTools.readPos(buf);
    }

    @Override
    public void toBytes(ByteBuf buf) {
        NetworkTools.writePos(buf, pos);
    }

    public PacketGetDelightingInfo() {
    }

    public PacketGetDelightingInfo(ByteBuf buf) {
        fromBytes(buf);
    }

    public PacketGetDelightingInfo(BlockPos pos) {
        this.pos = pos;
    }

    public void handle(Supplier<Context> supplier) {
        Context ctx = supplier.get();
        ctx.enqueueWork(() -> {
            PlayerEntity player = ctx.getSender();
            World world = player.getEntityWorld();

            List<String> blockClasses = new ArrayList<>();
            List<String> teClasses = new ArrayList<>();
            Map<String,DelightingInfoHelper.NBTDescription> nbtData = new HashMap<>();

            int metadata = DelightingInfoHelper.fillDelightingData(pos.getX(), pos.getY(), pos.getZ(), world, blockClasses, teClasses, nbtData);
            RFToolsMessages.INSTANCE.sendTo(new PacketDelightingInfoReady(blockClasses, teClasses, nbtData, metadata), ctx.getSender());
        });
        ctx.setPacketHandled(true);
    }
}