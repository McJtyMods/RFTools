package mcjty.rftools.items.teleportprobe;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.EnumHand;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class PacketSetTarget implements IMessage {
    private int target;

    @Override
    public void fromBytes(ByteBuf buf) {
        target = buf.readInt();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(target);
    }

    public PacketSetTarget() {
    }

    public PacketSetTarget(int target) {
        this.target = target;
    }

    public static class Handler implements IMessageHandler<PacketSetTarget, IMessage> {
        @Override
        public IMessage onMessage(PacketSetTarget message, MessageContext ctx) {
            FMLCommonHandler.instance().getWorldThread(ctx.netHandler).addScheduledTask(() -> handle(message, ctx));
            return null;
        }

        private void handle(PacketSetTarget message, MessageContext ctx) {
            EntityPlayer player = ctx.getServerHandler().playerEntity;
            ItemStack heldItem = player.getHeldItem(EnumHand.MAIN_HAND);
            if (heldItem == null) {
                return;
            }
            NBTTagCompound tagCompound = heldItem.getTagCompound();
            if (tagCompound == null) {
                return;
            }
            tagCompound.setInteger("target", message.target);
        }

    }

}
