package mcjty.rftools.blocks.storagemonitor;

import io.netty.buffer.ByteBuf;
import mcjty.lib.network.NetworkTools;
import mcjty.lib.varia.Logging;
import mcjty.rftools.network.RFToolsMessages;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import java.util.List;

public class PacketGetInventory implements IMessage {
    private BlockPos pos;
    private BlockPos cpos;

    @Override
    public void fromBytes(ByteBuf buf) {
        pos = NetworkTools.readPos(buf);
        cpos = NetworkTools.readPos(buf);
    }

    @Override
    public void toBytes(ByteBuf buf) {
        NetworkTools.writePos(buf, pos);
        NetworkTools.writePos(buf, cpos);
    }

    public PacketGetInventory() {
    }

    public PacketGetInventory(BlockPos pos, BlockPos cpos) {
        this.pos = pos;
        this.cpos = cpos;
    }

    public static class Handler implements IMessageHandler<PacketGetInventory, IMessage> {
        @Override
        public IMessage onMessage(PacketGetInventory message, MessageContext ctx) {
            FMLCommonHandler.instance().getWorldThread(ctx.netHandler).addScheduledTask(() -> handle(message, ctx));
            return null;
        }

        private void handle(PacketGetInventory message, MessageContext ctx) {
            TileEntity te = ctx.getServerHandler().playerEntity.worldObj.getTileEntity(message.pos);
            if(!(te instanceof StorageScannerTileEntity)) {
                Logging.log("createGetInventoryPacket: TileEntity is not a StorageScannerTileEntity!");
                return;
            }
            StorageScannerTileEntity storageScannerTileEntity = (StorageScannerTileEntity) te;
            List<ItemStack> items = storageScannerTileEntity.getInventoryForBlock(message.cpos);
            RFToolsMessages.INSTANCE.sendTo(new PacketInventoryReady(message.pos, items), ctx.getServerHandler().playerEntity);
        }

    }

}
