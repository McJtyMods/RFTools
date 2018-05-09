package mcjty.rftools.shapes;

import io.netty.buffer.ByteBuf;
import mcjty.lib.network.AbstractServerCommandTyped;
import mcjty.lib.typed.Key;
import mcjty.lib.typed.Type;
import mcjty.lib.typed.TypedMap;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumHand;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

/**
 * This is a packet that can be used to update the NBT on the held item of a player.
 */
public class PacketUpdateNBTShapeCard implements IMessage {
    private TypedMap args;

    @Override
    public void fromBytes(ByteBuf buf) {
        args = AbstractServerCommandTyped.readArguments(buf);
    }

    @Override
    public void toBytes(ByteBuf buf) {
        AbstractServerCommandTyped.writeArguments(buf, args);
    }

    public PacketUpdateNBTShapeCard() {
    }

    public PacketUpdateNBTShapeCard(TypedMap arguments) {
        this.args = arguments;
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
            for (Key<?> akey : message.args.getKeys()) {
                String key = akey.getName();
                if (Type.STRING.equals(akey.getType())) {
                    tagCompound.setString(key, (String) message.args.get(akey));
                } else if (Type.INTEGER.equals(akey.getType())) {
                    tagCompound.setInteger(key, (Integer) message.args.get(akey));
                } else if (Type.DOUBLE.equals(akey.getType())) {
                    tagCompound.setDouble(key, (Double) message.args.get(akey));
                } else if (Type.BOOLEAN.equals(akey.getType())) {
                    tagCompound.setBoolean(key, (Boolean) message.args.get(akey));
                } else if (Type.BLOCKPOS.equals(akey.getType())) {
                    throw new RuntimeException("BlockPos not supported for PacketUpdateNBTItem!");
                } else if (Type.ITEMSTACK.equals(akey.getType())) {
                    throw new RuntimeException("ItemStack not supported for PacketUpdateNBTItem!");
                }
            }
        }

    }

}