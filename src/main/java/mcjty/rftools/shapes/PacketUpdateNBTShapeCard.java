package mcjty.rftools.shapes;

import io.netty.buffer.ByteBuf;
import mcjty.lib.network.AbstractServerCommand;
import mcjty.lib.network.Argument;
import mcjty.rftools.items.builder.ShapeCardItem;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumHand;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import java.util.HashMap;
import java.util.Map;

/**
 * This is a packet that can be used to update the NBT on the held item of a player.
 */
public class PacketUpdateNBTShapeCard implements IMessage {
    private Map<String,Argument> args;

    @Override
    public void fromBytes(ByteBuf buf) {
        args = AbstractServerCommand.readArguments(buf);
    }

    @Override
    public void toBytes(ByteBuf buf) {
        AbstractServerCommand.writeArguments(buf, args);
    }

    public PacketUpdateNBTShapeCard() {
    }

    public PacketUpdateNBTShapeCard(Argument... arguments) {
        if (arguments == null) {
            this.args = null;
        } else {
            args = new HashMap<String, Argument>(arguments.length);
            for (Argument arg : arguments) {
                args.put(arg.getName(), arg);
            }
        }
    }

    public static class Handler implements IMessageHandler<PacketUpdateNBTShapeCard, IMessage> {
        @Override
        public IMessage onMessage(PacketUpdateNBTShapeCard message, MessageContext ctx) {
            FMLCommonHandler.instance().getWorldThread(ctx.netHandler).addScheduledTask(() -> handle(message, ctx));
            return null;
        }

        private void handle(PacketUpdateNBTShapeCard message, MessageContext ctx) {
            EntityPlayerMP playerEntity = ctx.getServerHandler().player;
            ItemStack heldItem = playerEntity.getHeldItem(EnumHand.MAIN_HAND);
            if (heldItem.isEmpty()) {
                return;
            }
            NBTTagCompound tagCompound = heldItem.getTagCompound();
            if (tagCompound == null) {
                tagCompound = new NBTTagCompound();
                heldItem.setTagCompound(tagCompound);
            }
            for (Map.Entry<String, Argument> entry : message.args.entrySet()) {
                String key = entry.getKey();
                switch (entry.getValue().getType()) {
                    case TYPE_STRING:
                        tagCompound.setString(key, entry.getValue().getString());
                        break;
                    case TYPE_INTEGER:
                        tagCompound.setInteger(key, entry.getValue().getInteger());
                        break;
                    case TYPE_BLOCKPOS:
                        throw new RuntimeException("BlockPos not supported for PacketUpdateNBTItem!");
                    case TYPE_BOOLEAN:
                        tagCompound.setBoolean(key, entry.getValue().getBoolean());
                        break;
                    case TYPE_DOUBLE:
                        tagCompound.setDouble(key, entry.getValue().getDouble());
                        break;
                }
            }
        }

    }

}