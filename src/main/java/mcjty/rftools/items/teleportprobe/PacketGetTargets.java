package mcjty.rftools.items.teleportprobe;

import io.netty.buffer.ByteBuf;
import mcjty.lib.varia.GlobalCoordinate;
import mcjty.rftools.blocks.teleporter.TeleportDestination;
import mcjty.rftools.blocks.teleporter.TeleportDestinations;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class PacketGetTargets implements IMessage, IMessageHandler<PacketGetTargets, PacketTargetsReady> {
    @Override
    public void fromBytes(ByteBuf buf) {
    }

    @Override
    public void toBytes(ByteBuf buf) {
    }

    public PacketGetTargets() {
    }

    @Override
    public PacketTargetsReady onMessage(PacketGetTargets message, MessageContext ctx) {
        EntityPlayer player = ctx.getServerHandler().playerEntity;
        ItemStack heldItem = player.getHeldItem();
        if (heldItem == null) {
            return null;
        }
        NBTTagCompound tagCompound = heldItem.getTagCompound();

        int target = -1;
        int targets[] = new int[AdvancedChargedPorterItem.MAXTARGETS];
        String names[] = new String[AdvancedChargedPorterItem.MAXTARGETS];
        TeleportDestinations destinations = TeleportDestinations.getDestinations(player.worldObj);

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

        return new PacketTargetsReady(target, targets, names);
    }
}
