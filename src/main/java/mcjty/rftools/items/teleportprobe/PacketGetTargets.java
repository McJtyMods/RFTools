package mcjty.rftools.items.teleportprobe;

import io.netty.buffer.ByteBuf;
import mcjty.lib.varia.GlobalCoordinate;
import mcjty.rftools.blocks.teleporter.TeleportDestination;
import mcjty.rftools.blocks.teleporter.TeleportDestinations;
import mcjty.rftools.network.RFToolsMessages;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumHand;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class PacketGetTargets implements IMessage {
    @Override
    public void fromBytes(ByteBuf buf) {
    }

    @Override
    public void toBytes(ByteBuf buf) {
    }

    public PacketGetTargets() {
    }

    public static class Handler implements IMessageHandler<PacketGetTargets, IMessage> {
        @Override
        public IMessage onMessage(PacketGetTargets message, MessageContext ctx) {
            FMLCommonHandler.instance().getWorldThread(ctx.netHandler).addScheduledTask(() -> handle(message, ctx));
            return null;
        }

        private void handle(PacketGetTargets message, MessageContext ctx) {
            EntityPlayerMP player = ctx.getServerHandler().playerEntity;
            ItemStack heldItem = player.getHeldItem(EnumHand.MAIN_HAND);
            if (heldItem == null) {
                return;
            }
            NBTTagCompound tagCompound = heldItem.getTagCompound();

            int target = -1;
            int targets[] = new int[AdvancedChargedPorterItem.MAXTARGETS];
            String names[] = new String[AdvancedChargedPorterItem.MAXTARGETS];
            TeleportDestinations destinations = TeleportDestinations.getDestinations(player.getEntityWorld());

            if (tagCompound != null) {
                if (tagCompound.hasKey("target")) {
                    target = tagCompound.getInteger("target");
                } else {
                    target = -1;
                }
                for (int i = 0 ; i < AdvancedChargedPorterItem.MAXTARGETS ; i++) {
                    names[i] = "";
                    if (tagCompound.hasKey("target" + i)) {
                        targets[i] = tagCompound.getInteger("target" + i);
                        GlobalCoordinate gc = destinations.getCoordinateForId(targets[i]);
                        if (gc != null) {
                            TeleportDestination destination = destinations.getDestination(gc);
                            if (destination != null) {
                                names[i] = destination.getName() + " (dimension " + destination.getDimension() + ")";
                            }
                        }
                    } else {
                        targets[i] = -1;
                    }
                }
            } else {
                for (int i = 0 ; i < AdvancedChargedPorterItem.MAXTARGETS ; i++) {
                    targets[i] = -1;
                    names[i] = "";
                }
            }

            PacketTargetsReady msg = new PacketTargetsReady(target, targets, names);
            RFToolsMessages.INSTANCE.sendTo(msg, player);
        }
    }
}
