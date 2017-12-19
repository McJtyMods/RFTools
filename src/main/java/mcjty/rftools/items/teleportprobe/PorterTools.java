package mcjty.rftools.items.teleportprobe;

import mcjty.lib.varia.GlobalCoordinate;
import mcjty.rftools.blocks.teleporter.TeleportDestination;
import mcjty.rftools.blocks.teleporter.TeleportDestinations;
import mcjty.rftools.network.PacketReturnDestinationInfo;
import mcjty.rftools.network.RFToolsMessages;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;

public class PorterTools {

    public static void clearTarget(EntityPlayer player, int index) {
        ItemStack heldItem = player.getHeldItem(EnumHand.MAIN_HAND);
        if (heldItem.isEmpty()) {
            return;
        }
        NBTTagCompound tagCompound = heldItem.getTagCompound();
        if (tagCompound == null) {
            return;
        }
        if (tagCompound.hasKey("target"+ index)) {
            int id = tagCompound.getInteger("target"+ index);
            if (tagCompound.hasKey("target") && tagCompound.getInteger("target") == id) {
                tagCompound.removeTag("target");
            }
            tagCompound.removeTag("target"+ index);
        }
    }

    public static void forceTeleport(EntityPlayer player, int dimension, BlockPos pos) {
        boolean probeInMainHand = !player.getHeldItemMainhand().isEmpty() && player.getHeldItemMainhand().getItem() instanceof TeleportProbeItem;
        boolean probeInOffHand = !player.getHeldItemOffhand().isEmpty() && player.getHeldItemOffhand().getItem() instanceof TeleportProbeItem;
        if ((!probeInMainHand) && (!probeInOffHand)) {
            return;
        }

        int x = pos.getX();
        int y = pos.getY();
        int z = pos.getZ();
        int currentId = player.getEntityWorld().provider.getDimension();
        if (currentId != dimension) {
            mcjty.lib.varia.TeleportationTools.teleportToDimension(player, dimension, x + .5, y + 1, z + .5);
        } else {
            player.setPositionAndUpdate(x+.5, y + 1.5, z+.5);
        }
    }

    public static void cycleDestination(EntityPlayer player, boolean next) {
        ItemStack stack = player.getHeldItemMainhand();
        if (!stack.isEmpty() && stack.getItem() instanceof AdvancedChargedPorterItem) {
            NBTTagCompound tagCompound = stack.getTagCompound();
            if (tagCompound == null) {
                return;
            }
            TeleportDestinations destinations = TeleportDestinations.getDestinations(player.getEntityWorld());

            int curtarget = tagCompound.getInteger("target");

            int donext = 0;
            // To wrap around we cycle through the list twice
            for (int i = 0; i < AdvancedChargedPorterItem.MAXTARGETS * 2; i++) {
                int tgt;
                if (next) {
                    tgt = i % AdvancedChargedPorterItem.MAXTARGETS;
                } else {
                    tgt = (AdvancedChargedPorterItem.MAXTARGETS * 2 - i) % AdvancedChargedPorterItem.MAXTARGETS;
                }
                donext = checkTarget(player, tagCompound, destinations, curtarget, donext, tgt);
                if (donext == 2) {
                    break;
                }
            }
        }
    }

    private static int checkTarget(EntityPlayer playerEntity, NBTTagCompound tagCompound, TeleportDestinations destinations, int curtarget, int donext, int tgt) {
        if (tagCompound.hasKey("target" + tgt)) {
            int target = tagCompound.getInteger("target" + tgt);
            GlobalCoordinate gc = destinations.getCoordinateForId(target);
            if (gc != null) {
                TeleportDestination destination = destinations.getDestination(gc);
                if (destination != null) {
                    if (donext == 1) {
                        String name = destination.getName() + " (dimension " + destination.getDimension() + ")";
                        tagCompound.setInteger("target", target);
                        ITextComponent component = new TextComponentString(TextFormatting.GREEN + "Target: "+
                        TextFormatting.WHITE + name);
                        if (playerEntity instanceof EntityPlayer) {
                            playerEntity.sendStatusMessage(component, false);
                        } else {
                            playerEntity.sendMessage(component);
                        }
                        donext = 2;
                    } else if (target == curtarget) {
                        donext = 1;
                    }
                }
            }
        }
        return donext;
    }

    public static void returnDestinationInfo(EntityPlayer player, int receiverId) {
        World world = player.getEntityWorld();
        TeleportDestinations destinations = TeleportDestinations.getDestinations(world);
        String name = TeleportDestinations.getDestinationName(destinations, receiverId);
        RFToolsMessages.INSTANCE.sendTo(new PacketReturnDestinationInfo(receiverId, name), (EntityPlayerMP) player);
    }

    public static void setTarget(EntityPlayer player, int target) {
        ItemStack heldItem = player.getHeldItem(EnumHand.MAIN_HAND);
        if (heldItem.isEmpty()) {
            return;
        }
        NBTTagCompound tagCompound = heldItem.getTagCompound();
        if (tagCompound == null) {
            return;
        }
        tagCompound.setInteger("target", target);
    }

    public static void returnTargets(EntityPlayer player) {
        ItemStack heldItem = player.getHeldItem(EnumHand.MAIN_HAND);
        if (heldItem.isEmpty()) {
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
        RFToolsMessages.INSTANCE.sendTo(msg, (EntityPlayerMP) player);
    }
}
