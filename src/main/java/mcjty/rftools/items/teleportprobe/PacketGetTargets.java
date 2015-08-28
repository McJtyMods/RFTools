package mcjty.rftools.items.teleportprobe;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import mcjty.rftools.blocks.teleporter.TeleportDestination;
import mcjty.rftools.blocks.teleporter.TeleportDestinations;
import mcjty.varia.GlobalCoordinate;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

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
        String name = "";
        int targets[] = new int[AdvancedChargedPorterItem.MAXTARGETS];
        String names[] = new String[AdvancedChargedPorterItem.MAXTARGETS];
        TeleportDestinations destinations = TeleportDestinations.getDestinations(player.worldObj);

        if (tagCompound != null) {
            if (tagCompound.hasKey("target")) {
                target = tagCompound.getInteger("target");
                GlobalCoordinate gc = destinations.getCoordinateForId(target);
                if (gc != null) {
                    TeleportDestination destination = destinations.getDestination(gc);
                    if (destination != null) {
                        name = destination.getName() + " (dimension " + destination.getDimension() + ")";
                    }
                }
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

        return new PacketTargetsReady(target, name, targets, names);
    }
}
