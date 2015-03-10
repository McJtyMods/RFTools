package com.mcjty.rftools.blocks.storagemonitor;

import com.mcjty.rftools.RFTools;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;

import java.util.List;

public class PacketGetInventory implements IMessage, IMessageHandler<PacketGetInventory, PacketInventoryReady> {
    private int x;
    private int y;
    private int z;

    private int cx;
    private int cy;
    private int cz;

    @Override
    public void fromBytes(ByteBuf buf) {
        x = buf.readInt();
        y = buf.readInt();
        z = buf.readInt();
        cx = buf.readInt();
        cy = buf.readInt();
        cz = buf.readInt();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(x);
        buf.writeInt(y);
        buf.writeInt(z);
        buf.writeInt(cx);
        buf.writeInt(cy);
        buf.writeInt(cz);
    }

    public PacketGetInventory() {
    }

    public PacketGetInventory(int x, int y, int z, int cx, int cy, int cz) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.cx = cx;
        this.cy = cy;
        this.cz = cz;
    }

    @Override
    public PacketInventoryReady onMessage(PacketGetInventory message, MessageContext ctx) {
        TileEntity te = ctx.getServerHandler().playerEntity.worldObj.getTileEntity(message.x, message.y, message.z);
        if(!(te instanceof StorageScannerTileEntity)) {
            RFTools.log("createGetInventoryPacket: TileEntity is not a StorageScannerTileEntity!");
            return null;
        }
        StorageScannerTileEntity storageScannerTileEntity = (StorageScannerTileEntity) te;
        List<ItemStack> items = storageScannerTileEntity.getInventoryForBlock(message.cx, message.cy, message.cz);
        return new PacketInventoryReady(message.x, message.y, message.z, items);
    }

}
