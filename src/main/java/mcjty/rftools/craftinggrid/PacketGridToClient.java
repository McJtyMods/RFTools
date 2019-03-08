package mcjty.rftools.craftinggrid;

import io.netty.buffer.ByteBuf;
import mcjty.lib.thirteen.Context;
import mcjty.rftools.RFTools;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

import java.util.function.Supplier;

public class PacketGridToClient extends PacketGridSync implements IMessage {

    @Override
    public void fromBytes(ByteBuf buf) {
        convertFromBytes(buf);
    }

    @Override
    public void toBytes(ByteBuf buf) {
        convertToBytes(buf);
    }

    public PacketGridToClient() {
    }

    public PacketGridToClient(ByteBuf buf) {
        fromBytes(buf);
    }

    public PacketGridToClient(BlockPos pos, CraftingGrid grid) {
        init(pos, grid);
    }

    public void handle(Supplier<Context> supplier) {
        Context ctx = supplier.get();
        ctx.enqueueWork(() -> {
            World world = RFTools.proxy.getClientWorld();
            EntityPlayer player = RFTools.proxy.getClientPlayer();
            handleMessage(world, player);
        });
        ctx.setPacketHandled(true);
    }
}
