package mcjty.rftools.blocks.storagemonitor;

import io.netty.buffer.ByteBuf;
import mcjty.lib.network.NetworkTools;
import mcjty.rftools.varia.RFToolsTools;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class PacketRequestItem implements IMessage {

    private int dimensionId;
    private BlockPos pos;
    private BlockPos inventoryPos;
    private ItemStack item;
    private int amount;


    @Override
    public void fromBytes(ByteBuf buf) {
        dimensionId = buf.readInt();
        pos = NetworkTools.readPos(buf);
        inventoryPos = NetworkTools.readPos(buf);
        amount = buf.readInt();
        item = NetworkTools.readItemStack(buf);
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(dimensionId);
        NetworkTools.writePos(buf, pos);
        NetworkTools.writePos(buf, inventoryPos);
        buf.writeInt(amount);
        NetworkTools.writeItemStack(buf, item);
    }

    public PacketRequestItem() {
    }

    public PacketRequestItem(int dimensionId, BlockPos pos, BlockPos inventoryPos, ItemStack item, int amount) {
        this.dimensionId = dimensionId;
        this.pos = pos;
        this.inventoryPos = inventoryPos;
        this.item = item;
        this.amount = amount;
    }

    public static class Handler implements IMessageHandler<PacketRequestItem, IMessage> {
        @Override
        public IMessage onMessage(PacketRequestItem message, MessageContext ctx) {
            FMLCommonHandler.instance().getWorldThread(ctx.netHandler).addScheduledTask(() -> handle(message, ctx));
            return null;
        }

        public void handle(PacketRequestItem message, MessageContext ctx) {
            WorldServer world = DimensionManager.getWorld(message.dimensionId);
            if (world == null) {
                return;
            }
            if (!RFToolsTools.chunkLoaded(world, message.pos)) {
                return;
            }
            TileEntity te = world.getTileEntity(message.pos);
            if (te instanceof StorageScannerTileEntity) {
                StorageScannerTileEntity tileEntity = (StorageScannerTileEntity) te;
                tileEntity.requestStack(message.inventoryPos, message.item, message.amount);
            }
        }

    }

}
