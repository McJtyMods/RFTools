package mcjty.rftools.blocks.storagemonitor;

import io.netty.buffer.ByteBuf;
import mcjty.lib.network.NetworkTools;
import mcjty.lib.thirteen.Context;
import mcjty.lib.varia.WorldTools;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

import java.util.function.Supplier;

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

    public PacketRequestItem(ByteBuf buf) {
        fromBytes(buf);
    }

    public PacketRequestItem(int dimensionId, BlockPos pos, BlockPos inventoryPos, ItemStack item, int amount) {
        this.dimensionId = dimensionId;
        this.pos = pos;
        this.inventoryPos = inventoryPos;
        this.item = item;
        this.amount = amount;
    }

    public void handle(Supplier<Context> supplier) {
        Context ctx = supplier.get();
        ctx.enqueueWork(() -> {
            WorldServer world = DimensionManager.getWorld(dimensionId);
            if (world == null) {
                return;
            }
            if (!WorldTools.chunkLoaded(world, pos)) {
                return;
            }
            TileEntity te = world.getTileEntity(pos);
            if (te instanceof StorageScannerTileEntity) {
                StorageScannerTileEntity tileEntity = (StorageScannerTileEntity) te;
                tileEntity.requestStack(inventoryPos, item, amount, ctx.getSender());
            }
        });
        ctx.setPacketHandled(true);
    }
}
