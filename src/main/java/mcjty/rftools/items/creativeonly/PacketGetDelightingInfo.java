package mcjty.rftools.items.creativeonly;

import mcjty.lib.network.NetworkTools;
import mcjty.rftools.network.RFToolsMessages;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public class PacketGetDelightingInfo {
    private BlockPos pos;

    public void toBytes(PacketBuffer buf) {
        NetworkTools.writePos(buf, pos);
    }

    public PacketGetDelightingInfo() {
    }

    public PacketGetDelightingInfo(PacketBuffer buf) {
        pos = NetworkTools.readPos(buf);
    }

    public PacketGetDelightingInfo(BlockPos pos) {
        this.pos = pos;
    }

    public void handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context ctx = supplier.get();
        ctx.enqueueWork(() -> {
            PlayerEntity player = ctx.getSender();
            World world = player.getEntityWorld();

            List<String> blockClasses = new ArrayList<>();
            List<String> teClasses = new ArrayList<>();
            Map<String,DelightingInfoHelper.NBTDescription> nbtData = new HashMap<>();

            DelightingInfoHelper.fillDelightingData(pos.getX(), pos.getY(), pos.getZ(), world, blockClasses, teClasses, nbtData);
            RFToolsMessages.INSTANCE.sendTo(new PacketDelightingInfoReady(blockClasses, teClasses, nbtData),
                    ctx.getSender().connection.netManager, NetworkDirection.PLAY_TO_CLIENT);
        });
        ctx.setPacketHandled(true);
    }
}