package mcjty.rftools.items.teleportprobe;

import cofh.api.energy.IEnergyContainerItem;
import mcjty.lib.varia.Coordinate;
import mcjty.lib.varia.GlobalCoordinate;
import mcjty.lib.varia.Logging;
import mcjty.rftools.RFTools;
import mcjty.rftools.blocks.teleporter.*;
import mcjty.rftools.playerprops.PlayerExtendedProperties;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import net.minecraftforge.common.IExtendedEntityProperties;
import org.lwjgl.input.Keyboard;

import java.util.List;

public class ChargedPorterItem extends Item implements IEnergyContainerItem {

    protected int capacity;
    private int maxReceive;
    private int maxExtract;

    public ChargedPorterItem() {
        setMaxStackSize(1);

        capacity = TeleportConfiguration.CHARGEDPORTER_MAXENERGY;
        maxReceive = TeleportConfiguration.CHARGEDPORTER_RECEIVEPERTICK;
        maxExtract = 0;
    }

    protected String getIconName() {
        return "chargedPorterItemL";
    }

    protected int getSpeedBonus() {
        return 1;
    }

    @Override
    public int getMaxItemUseDuration(ItemStack stack) {
        return 1;
    }

    @Override
    public ItemStack onItemRightClick(ItemStack stack, World world, EntityPlayer player) {
        if (!player.isSneaking()) {
            startTeleport(stack, player, world);
        } else {
            selectReceiver(stack, world, player);
        }
        return super.onItemRightClick(stack, world, player);
    }

    protected void selectReceiver(ItemStack stack, World world, EntityPlayer player) {
    }

    @Override
    public boolean onItemUse(ItemStack stack, EntityPlayer player, World world, BlockPos pos, EnumFacing side, float hitX, float hitY, float hitZ) {
        if (player.isSneaking()) {
            TileEntity te = world.getTileEntity(pos);
            setTarget(stack, player, world, te);
        } else {
            startTeleport(stack, player, world);
        }
        return true;
    }

    private void startTeleport(ItemStack stack, EntityPlayer player, World world) {
        NBTTagCompound tagCompound = stack.getTagCompound();
        if (tagCompound == null || (!tagCompound.hasKey("target")) || tagCompound.getInteger("target") == -1) {
            if (world.isRemote) {
                Logging.message(player, EnumChatFormatting.RED + "The charged porter has no target.");
            }
            return;
        }

        if (!world.isRemote) {
            IExtendedEntityProperties properties = player.getExtendedProperties(PlayerExtendedProperties.ID);
            PlayerExtendedProperties playerExtendedProperties = (PlayerExtendedProperties) properties;
            if (playerExtendedProperties.getPorterProperties().isTeleporting()) {
                Logging.message(player, EnumChatFormatting.RED + "Already teleporting!");
                return;
            }

            int target = tagCompound.getInteger("target");

            TeleportDestinations destinations = TeleportDestinations.getDestinations(world);
            GlobalCoordinate coordinate = destinations.getCoordinateForId(target);
            if (coordinate == null) {
                Logging.message(player, EnumChatFormatting.RED + "Something went wrong! The target has disappeared!");
                TeleportationTools.applyEffectForSeverity(player, 3, false);
                return;
            }
            TeleportDestination destination = destinations.getDestination(coordinate);

            if (!TeleportationTools.checkValidTeleport(player, world.provider.getDimensionId(), destination.getDimension())) {
                return;
            }

            Coordinate playerCoordinate = new Coordinate((int) player.posX, (int) player.posY, (int) player.posZ);
            int cost = TeleportationTools.calculateRFCost(world, playerCoordinate, destination);
            cost *= 1.5f;
            int energy = getEnergyStored(stack);
            if (cost > energy) {
                Logging.message(player, EnumChatFormatting.RED + "Not enough energy to start the teleportation!");
                return;
            }
            extractEnergyNoMax(stack, cost, false);

            int ticks = TeleportationTools.calculateTime(world, playerCoordinate, destination);
            ticks /= getSpeedBonus();
            playerExtendedProperties.getPorterProperties().startTeleport(target, ticks);
            Logging.message(player, EnumChatFormatting.YELLOW + "Start teleportation!");
        }
    }

    private void setTarget(ItemStack stack, EntityPlayer player, World world, TileEntity te) {
        NBTTagCompound tagCompound = stack.getTagCompound();

        if (tagCompound == null) {
            tagCompound = new NBTTagCompound();
        }
        int id = -1;
        if (te instanceof MatterReceiverTileEntity) {
            MatterReceiverTileEntity matterReceiverTileEntity = (MatterReceiverTileEntity) te;
            if (!matterReceiverTileEntity.checkAccess(player.getDisplayNameString())) {
                Logging.message(player, EnumChatFormatting.RED + "You have no access to target this receiver!");
                return;
            }
            id = matterReceiverTileEntity.getId();
        }

        if (id != -1) {
            selectOnReceiver(player, world, tagCompound, id);
        } else {
            selectOnThinAir(player, world, tagCompound, stack);
        }
        stack.setTagCompound(tagCompound);
    }

    protected void selectOnReceiver(EntityPlayer player, World world, NBTTagCompound tagCompound, int id) {
        if (world.isRemote) {
            Logging.message(player, "Charged porter target is set to " + id + ".");
        }
        tagCompound.setInteger("target", id);
    }

    protected void selectOnThinAir(EntityPlayer player, World world, NBTTagCompound tagCompound, ItemStack stack) {
        if (world.isRemote) {
            Logging.message(player, "Charged porter is cleared.");
        }
        tagCompound.removeTag("target");
    }

    @Override
    public void addInformation(ItemStack itemStack, EntityPlayer player, List list, boolean whatIsThis) {
        super.addInformation(itemStack, player, list, whatIsThis);
        NBTTagCompound tagCompound = itemStack.getTagCompound();
        if (tagCompound != null) {
            list.add(EnumChatFormatting.BLUE + "Energy: " + tagCompound.getInteger("Energy") + " RF");
            if (tagCompound.hasKey("target")) {
                list.add(EnumChatFormatting.BLUE + "Target: " + tagCompound.getInteger("target"));
            } else {
                list.add(EnumChatFormatting.RED + "No target set! Sneak-Right click on receiver to set.");
            }
        }
        if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT)) {
            list.add("This RF/charged item allows you to teleport to a");
            list.add("previously set matter receiver. Sneak-right click");
            list.add("on a receiver to set the destination.");
            list.add("Right click to perform the teleport.");
        } else {
            list.add(EnumChatFormatting.WHITE + RFTools.SHIFT_MESSAGE);
        }
    }


    @Override
    public int receiveEnergy(ItemStack container, int maxReceive, boolean simulate) {
        if (container.getTagCompound() == null) {
            container.setTagCompound(new NBTTagCompound());
        }
        int energy = container.getTagCompound().getInteger("Energy");
        int energyReceived = Math.min(capacity - energy, Math.min(this.maxReceive, maxReceive));

        if (!simulate) {
            energy += energyReceived;
            container.getTagCompound().setInteger("Energy", energy);
        }
        return energyReceived;
    }

    @Override
    public int extractEnergy(ItemStack container, int maxExtract, boolean simulate) {
        if (container.getTagCompound() == null || !container.getTagCompound().hasKey("Energy")) {
            return 0;
        }
        int energy = container.getTagCompound().getInteger("Energy");
        int energyExtracted = Math.min(energy, Math.min(this.maxExtract, maxExtract));

        if (!simulate) {
            energy -= energyExtracted;
            container.getTagCompound().setInteger("Energy", energy);
        }
        return energyExtracted;
    }

    public int extractEnergyNoMax(ItemStack container, int maxExtract, boolean simulate) {
        if (container.getTagCompound() == null || !container.getTagCompound().hasKey("Energy")) {
            return 0;
        }
        int energy = container.getTagCompound().getInteger("Energy");
        int energyExtracted = Math.min(energy, maxExtract);

        if (!simulate) {
            energy -= energyExtracted;
            container.getTagCompound().setInteger("Energy", energy);
        }
        return energyExtracted;
    }

    @Override
    public int getEnergyStored(ItemStack container) {
        if (container.getTagCompound() == null || !container.getTagCompound().hasKey("Energy")) {
            return 0;
        }
        return container.getTagCompound().getInteger("Energy");
    }

    @Override
    public int getMaxEnergyStored(ItemStack container) {
        return capacity;
    }
}
