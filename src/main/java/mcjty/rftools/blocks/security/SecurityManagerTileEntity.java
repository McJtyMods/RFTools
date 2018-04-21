package mcjty.rftools.blocks.security;

import mcjty.lib.container.*;
import mcjty.lib.entity.GenericTileEntity;
import mcjty.lib.network.Argument;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import java.util.Map;

public class SecurityManagerTileEntity extends GenericTileEntity implements DefaultSidedInventory {

    public static final String CMD_SETCHANNELNAME = "setChannelName";
    public static final String CMD_SETMODE = "setMode";
    public static final String CMD_ADDPLAYER = "addPlayer";
    public static final String CMD_DELPLAYER = "delPlayer";

    public static final int SLOT_CARD = 0;
    public static final int SLOT_LINKER = 1;
    public static final int SLOT_BUFFER = 2;
    public static final ContainerFactory CONTAINER_FACTORY = new ContainerFactory() {
        @Override
        protected void setup() {
            addSlotBox(new SlotDefinition(SlotType.SLOT_SPECIFICITEM, new ItemStack(SecuritySetup.securityCardItem)), ContainerFactory.CONTAINER_CONTAINER, SLOT_CARD, 10, 7, 1, 18, 1, 18);
            addSlotBox(new SlotDefinition(SlotType.SLOT_SPECIFICITEM, new ItemStack(SecuritySetup.securityCardItem)), ContainerFactory.CONTAINER_CONTAINER, SLOT_LINKER, 42, 7, 1, 18, 1, 18);
            addSlotBox(new SlotDefinition(SlotType.SLOT_SPECIFICITEM, new ItemStack(SecuritySetup.securityCardItem)), ContainerFactory.CONTAINER_CONTAINER, SLOT_BUFFER, 10, 124, 3, 18, 4, 18);
            layoutPlayerInventorySlots(74, 124);
        }
    };

    public static final int BUFFER_SIZE = (3*4);
    public static final int SLOT_PLAYERINV = SLOT_CARD + BUFFER_SIZE + 2;
    private InventoryHelper inventoryHelper = new InventoryHelper(this, CONTAINER_FACTORY, BUFFER_SIZE + 2);

    @Override
    protected boolean needsCustomInvWrapper() {
        return true;
    }

    private NBTTagCompound getOrCreateNBT(ItemStack cardStack) {
        NBTTagCompound tagCompound = cardStack.getTagCompound();
        if (tagCompound == null) {
            tagCompound = new NBTTagCompound();
            cardStack.setTagCompound(tagCompound);
        }
        return tagCompound;
    }

    private void updateCard(ItemStack cardStack) {
        if (getWorld().isRemote) {
            return;
        }
        if (cardStack.isEmpty()) {
            return;
        }
        NBTTagCompound tagCompound = getOrCreateNBT(cardStack);
        if (!tagCompound.hasKey("channel")) {
            SecurityChannels securityChannels = SecurityChannels.getChannels(getWorld());
            int id = securityChannels.newChannel();
            tagCompound.setInteger("channel", id);
            securityChannels.save(getWorld());
            markDirtyClient();
        }
    }

    private void updateLinkedCard() {
        if (getWorld().isRemote) {
            return;
        }
        ItemStack masterCard = inventoryHelper.getStackInSlot(SLOT_CARD);
        if (masterCard.isEmpty()) {
            return;
        }
        ItemStack linkerCard = inventoryHelper.getStackInSlot(SLOT_LINKER);
        if (linkerCard.isEmpty()) {
            return;
        }

        NBTTagCompound masterNBT = masterCard.getTagCompound();
        if (masterNBT == null) {
            return;
        }
        NBTTagCompound linkerNBT = getOrCreateNBT(linkerCard);
        linkerNBT.setInteger("channel", masterNBT.getInteger("channel"));
        markDirtyClient();
    }

    private void addPlayer(String player) {
        NBTTagCompound tagCompound = getCardInfo();
        if (tagCompound == null) {
            return;
        }
        if (tagCompound.hasKey("channel")) {
            SecurityChannels securityChannels = SecurityChannels.getChannels(getWorld());
            int id = tagCompound.getInteger("channel");
            SecurityChannels.SecurityChannel channel = securityChannels.getOrCreateChannel(id);
            channel.addPlayer(player);
            securityChannels.save(getWorld());
            markDirtyClient();
        }
    }

    private void delPlayer(String player) {
        NBTTagCompound tagCompound = getCardInfo();
        if (tagCompound == null) {
            return;
        }
        if (tagCompound.hasKey("channel")) {
            SecurityChannels securityChannels = SecurityChannels.getChannels(getWorld());
            int id = tagCompound.getInteger("channel");
            SecurityChannels.SecurityChannel channel = securityChannels.getOrCreateChannel(id);
            channel.delPlayer(player);
            securityChannels.save(getWorld());
            markDirtyClient();
        }
    }

    private void setWhiteListMode(boolean whitelist) {
        NBTTagCompound tagCompound = getCardInfo();
        if (tagCompound == null) {
            return;
        }
        if (tagCompound.hasKey("channel")) {
            SecurityChannels securityChannels = SecurityChannels.getChannels(getWorld());
            int id = tagCompound.getInteger("channel");
            SecurityChannels.SecurityChannel channel = securityChannels.getOrCreateChannel(id);
            channel.setWhitelist(whitelist);
            securityChannels.save(getWorld());
            markDirtyClient();
        }
    }

    private void setChannelName(String name) {
        NBTTagCompound tagCompound = getCardInfo();
        if (tagCompound == null) {
            return;
        }
        if (tagCompound.hasKey("channel")) {
            SecurityChannels securityChannels = SecurityChannels.getChannels(getWorld());
            int id = tagCompound.getInteger("channel");
            SecurityChannels.SecurityChannel channel = securityChannels.getOrCreateChannel(id);
            channel.setName(name);
            securityChannels.save(getWorld());
            markDirtyClient();
        }
    }

    private NBTTagCompound getCardInfo() {
        ItemStack cardStack = inventoryHelper.getStackInSlot(SLOT_CARD);
        if (cardStack.isEmpty()) {
            return null;
        }
        return getOrCreateNBT(cardStack);
    }

    @Override
    public InventoryHelper getInventoryHelper() {
        return inventoryHelper;
    }

    @Override
    public void setInventorySlotContents(int index, ItemStack stack) {
        inventoryHelper.setInventorySlotContents(getInventoryStackLimit(), index, stack);
        if (index == SLOT_CARD) {
            updateCard(stack);
            updateLinkedCard();
        } else if (index == SLOT_LINKER) {
            updateLinkedCard();
        }
    }

    @Override
    public int getInventoryStackLimit() {
        return 1;
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    @Override
    public boolean isUsableByPlayer(EntityPlayer player) {
        return canPlayerAccess(player);
    }

    @Override
    public boolean isItemValidForSlot(int index, ItemStack stack) {
        return SecuritySetup.securityCardItem.equals(stack.getItem());
    }

    @Override
    public void readRestorableFromNBT(NBTTagCompound tagCompound) {
        super.readRestorableFromNBT(tagCompound);
        readBufferFromNBT(tagCompound, inventoryHelper);
    }

    @Override
    public void writeRestorableToNBT(NBTTagCompound tagCompound) {
        super.writeRestorableToNBT(tagCompound);
        writeBufferToNBT(tagCompound, inventoryHelper);
    }

    @Override
    public boolean execute(EntityPlayerMP playerMP, String command, Map<String, Argument> args) {
        boolean rc = super.execute(playerMP, command, args);
        if (rc) {
            return true;
        }
        if (CMD_SETCHANNELNAME.equals(command)) {
            setChannelName(args.get("name").getString());
            return true;
        } else if (CMD_SETMODE.equals(command)) {
            setWhiteListMode(args.get("whitelist").getBoolean());
            return true;
        } else if (CMD_ADDPLAYER.equals(command)) {
            addPlayer(args.get("player").getString());
            return true;
        } else if (CMD_DELPLAYER.equals(command)) {
            delPlayer(args.get("player").getString());
            return true;
        }
        return false;
    }
}
