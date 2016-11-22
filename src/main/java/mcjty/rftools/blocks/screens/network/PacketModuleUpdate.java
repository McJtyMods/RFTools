package mcjty.rftools.blocks.screens.network;

import io.netty.buffer.ByteBuf;
import mcjty.lib.network.NetworkTools;
import mcjty.lib.tools.PacketBufferTools;
import mcjty.lib.varia.Logging;
import mcjty.rftools.blocks.screens.ScreenTileEntity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import java.io.IOException;

public class PacketModuleUpdate implements IMessage {
    private BlockPos pos;

    private int slotIndex;
    private NBTTagCompound tagCompound;

    @Override
    public void fromBytes(ByteBuf buf) {
        pos = NetworkTools.readPos(buf);
        slotIndex = buf.readInt();
        PacketBuffer buffer = new PacketBuffer(buf);
        try {
            tagCompound = PacketBufferTools.readCompoundTag(buffer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void toBytes(ByteBuf buf) {
        NetworkTools.writePos(buf, pos);
        buf.writeInt(slotIndex);
        PacketBuffer buffer = new PacketBuffer(buf);
        PacketBufferTools.writeCompoundTag(buffer, tagCompound);
    }

    public PacketModuleUpdate() {
    }

    public PacketModuleUpdate(BlockPos pos, int slotIndex, NBTTagCompound tagCompound) {
        this.pos = pos;
        this.slotIndex = slotIndex;
        this.tagCompound = tagCompound;
    }

    public static class Handler implements IMessageHandler<PacketModuleUpdate, IMessage> {
        @Override
        public IMessage onMessage(PacketModuleUpdate message, MessageContext ctx) {
            FMLCommonHandler.instance().getWorldThread(ctx.netHandler).addScheduledTask(() -> handle(message, ctx));
            return null;
        }

        private void handle(PacketModuleUpdate message, MessageContext ctx) {
            TileEntity te = ctx.getServerHandler().playerEntity.getEntityWorld().getTileEntity(message.pos);
            if(!(te instanceof ScreenTileEntity)) {
                Logging.logError("PacketModuleUpdate: TileEntity is not a SimpleScreenTileEntity!");
                return;
            }
            ((ScreenTileEntity) te).updateModuleData(message.slotIndex, message.tagCompound);
        }

    }
}
