package mcjty.rftools.blocks.screens.network;

import io.netty.buffer.ByteBuf;
import mcjty.lib.network.NetworkTools;
import mcjty.lib.thirteen.Context;
import mcjty.lib.varia.Logging;
import mcjty.rftools.blocks.screens.ScreenBlock;
import mcjty.rftools.blocks.screens.ScreenTileEntity;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

import java.io.IOException;
import java.util.function.Supplier;

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

    public PacketModuleUpdate(ByteBuf buf) {
        fromBytes(buf);
    }

    public PacketModuleUpdate(BlockPos pos, int slotIndex, NBTTagCompound tagCompound) {
        this.pos = pos;
        this.slotIndex = slotIndex;
        this.tagCompound = tagCompound;
    }

    public void handle(Supplier<Context> supplier) {
        Context ctx = supplier.get();
        ctx.enqueueWork(() -> {
            EntityPlayerMP player = ctx.getSender();
            World world = player.getEntityWorld();
            Block block = world.getBlockState(pos).getBlock();
            // adapted from NetHandlerPlayServer.processTryUseItemOnBlock
            double dist = player.getEntityAttribute(EntityPlayer.REACH_DISTANCE).getAttributeValue() + 3;
            if(player.getDistanceSq(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D) >= dist * dist) {
                return;
            }
            if(!(block instanceof ScreenBlock)) {
                Logging.logError("PacketModuleUpdate: Block is not a ScreenBlock!");
                return;
            }
            TileEntity te = world.getTileEntity(pos);
            if(((ScreenBlock)block).checkAccess(world, player, te)) {
                return;
            }
            if(!(te instanceof ScreenTileEntity)) {
                Logging.logError("PacketModuleUpdate: TileEntity is not a SimpleScreenTileEntity!");
                return;
            }
            ((ScreenTileEntity) te).updateModuleData(slotIndex, tagCompound);
        });
        ctx.setPacketHandled(true);
    }
}
