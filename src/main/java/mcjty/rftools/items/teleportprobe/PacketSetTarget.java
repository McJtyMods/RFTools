package mcjty.rftools.items.teleportprobe;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

public class PacketSetTarget implements IMessage, IMessageHandler<PacketSetTarget, IMessage> {
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

    @Override
    public IMessage onMessage(PacketSetTarget message, MessageContext ctx) {
        EntityPlayer player = ctx.getServerHandler().playerEntity;
        ItemStack heldItem = player.getHeldItem();
        if (heldItem == null) {
            return null;
        }
        NBTTagCompound tagCompound = heldItem.getTagCompound();
        if (tagCompound == null) {
            return null;
        }
        tagCompound.setInteger("target", message.target);
        return null;
    }

}
