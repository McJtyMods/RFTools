package mcjty.rftools.items.teleportprobe;

import io.netty.buffer.ByteBuf;
import mcjty.lib.varia.GlobalCoordinate;
import mcjty.rftools.blocks.teleporter.TeleportDestination;
import mcjty.rftools.blocks.teleporter.TeleportDestinations;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class PacketCycleDestination implements IMessage {

    private boolean next;

    @Override
    public void fromBytes(ByteBuf buf) {
        next = buf.readBoolean();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeBoolean(next);
    }

    public PacketCycleDestination() {
    }

    public PacketCycleDestination(boolean next) {
        this.next = next;
    }

    public static class Handler implements IMessageHandler<PacketCycleDestination, IMessage> {
        @Override
        public IMessage onMessage(PacketCycleDestination message, MessageContext ctx) {
            FMLCommonHandler.instance().getWorldThread(ctx.netHandler).addScheduledTask(() -> handle(message, ctx));
            return null;
        }

        private void handle(PacketCycleDestination message, MessageContext ctx) {
            EntityPlayerMP playerEntity = ctx.getServerHandler().playerEntity;
            ItemStack stack = playerEntity.getHeldItemMainhand();
            if (stack != null && stack.getItem() instanceof AdvancedChargedPorterItem) {
                NBTTagCompound tagCompound = stack.getTagCompound();
                if (tagCompound == null) {
                    return;
                }
                TeleportDestinations destinations = TeleportDestinations.getDestinations(playerEntity.getEntityWorld());

                int curtarget = tagCompound.getInteger("target");

                int donext = 0;
                // To wrap around we cycle through the list twice
                for (int i = 0; i < AdvancedChargedPorterItem.MAXTARGETS * 2; i++) {
                    int tgt;
                    if (message.next) {
                        tgt = i % AdvancedChargedPorterItem.MAXTARGETS;
                    } else {
                        tgt = (AdvancedChargedPorterItem.MAXTARGETS * 2 - i) % AdvancedChargedPorterItem.MAXTARGETS;
                    }
                    donext = checkTarget(playerEntity, tagCompound, destinations, curtarget, donext, tgt);
                    if (donext == 2) {
                        break;
                    }
                }
            }
        }

        private int checkTarget(EntityPlayerMP playerEntity, NBTTagCompound tagCompound, TeleportDestinations destinations, int curtarget, int donext, int tgt) {
            if (tagCompound.hasKey("target" + tgt)) {
                int target = tagCompound.getInteger("target" + tgt);
                GlobalCoordinate gc = destinations.getCoordinateForId(target);
                if (gc != null) {
                    TeleportDestination destination = destinations.getDestination(gc);
                    if (destination != null) {
                        if (donext == 1) {
                            String name = destination.getName() + " (dimension " + destination.getDimension() + ")";
                            tagCompound.setInteger("target", target);
                            playerEntity.addChatComponentMessage(new TextComponentString(TextFormatting.GREEN + "Target: "+
                            TextFormatting.WHITE + name));
                            donext = 2;
                        } else if (target == curtarget) {
                            donext = 1;
                        }
                    }
                }
            }
            return donext;
        }
    }
}
