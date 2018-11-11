package mcjty.rftools.blocks.screens.network;

import io.netty.buffer.ByteBuf;
import mcjty.lib.network.NetworkTools;
import mcjty.lib.varia.Logging;
import mcjty.rftools.blocks.screens.ScreenBlock;
import mcjty.rftools.blocks.screens.ScreenTileEntity;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
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
            tagCompound = buffer.readCompoundTag();
        } catch (IOException e) {
            Logging.logError("Error updating module", e);
        }
    }

    @Override
    public void toBytes(ByteBuf buf) {
        NetworkTools.writePos(buf, pos);
        buf.writeInt(slotIndex);
        PacketBuffer buffer = new PacketBuffer(buf);
        buffer.writeCompoundTag(tagCompound);
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
            FMLCommonHandler.instance().getWorldThread(ctx.netHandler).addScheduledTask(() -> {
                EntityPlayerMP player = ctx.getServerHandler().player;
                World world = player.getEntityWorld();
                Block block = world.getBlockState(message.pos).getBlock();
                if(!(block instanceof ScreenBlock)) {
                    Logging.logError("PacketModuleUpdate: Block is not a ScreenBlock!");
                    return;
                }
                TileEntity te = world.getTileEntity(message.pos);
                if(((ScreenBlock)block).checkAccess(world, player, te)) {
                    return;
                }
                if(!(te instanceof ScreenTileEntity)) {
                    Logging.logError("PacketModuleUpdate: TileEntity is not a SimpleScreenTileEntity!");
                    return;
                }
                ((ScreenTileEntity) te).updateModuleData(message.slotIndex, message.tagCompound);
            });
            return null;
        }

    }
}
