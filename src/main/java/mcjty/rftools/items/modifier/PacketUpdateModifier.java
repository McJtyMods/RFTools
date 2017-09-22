package mcjty.rftools.items.modifier;

import io.netty.buffer.ByteBuf;
import mcjty.lib.network.NetworkTools;
import mcjty.lib.tools.ItemStackTools;
import mcjty.rftools.items.ModItems;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class PacketUpdateModifier implements IMessage {
    private ItemStack stack;

    @Override
    public void fromBytes(ByteBuf buf) {
        stack = NetworkTools.readItemStack(buf);
    }

    @Override
    public void toBytes(ByteBuf buf) {
        NetworkTools.writeItemStack(buf, stack);
    }

    public PacketUpdateModifier() {
    }

    public PacketUpdateModifier(ItemStack stack) {
        this.stack = stack;
    }

    public static class Handler implements IMessageHandler<PacketUpdateModifier, IMessage> {
        @Override
        public IMessage onMessage(PacketUpdateModifier message, MessageContext ctx) {
            FMLCommonHandler.instance().getWorldThread(ctx.netHandler).addScheduledTask(() -> handle(message, ctx));
            return null;
        }

        private void handle(PacketUpdateModifier message, MessageContext ctx) {
            EntityPlayer player = ctx.getServerHandler().player;
            ItemStack heldItem = player.getHeldItem(EnumHand.MAIN_HAND);
            if (ItemStackTools.isValid(heldItem) && heldItem.getItem() == ModItems.modifierItem) {
                player.setHeldItem(EnumHand.MAIN_HAND, message.stack);
            }
        }
    }
}
