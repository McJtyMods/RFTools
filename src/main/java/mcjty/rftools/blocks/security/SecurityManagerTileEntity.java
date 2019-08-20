package mcjty.rftools.blocks.security;

import mcjty.lib.container.ContainerFactory;
import mcjty.lib.container.InventoryHelper;
import mcjty.lib.container.SlotDefinition;
import mcjty.lib.container.SlotType;
import mcjty.lib.tileentity.GenericTileEntity;
import mcjty.lib.typed.Key;
import mcjty.lib.typed.Type;
import mcjty.lib.typed.TypedMap;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;

public class SecurityManagerTileEntity extends GenericTileEntity implements DefaultSidedInventory {

    public static final String CMD_SETCHANNELNAME = "security.setChannelName";
    public static final Key<String> PARAM_NAME = new Key<>("name", Type.STRING);

    public static final String CMD_SETMODE = "security.setMode";
    public static final Key<Boolean> PARAM_WHITELIST = new Key<>("whitelist", Type.BOOLEAN);

    public static final String CMD_ADDPLAYER = "security.addPlayer";
    public static final String CMD_DELPLAYER = "security.delPlayer";
    public static final Key<String> PARAM_PLAYER = new Key<>("player", Type.STRING);

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

    private CompoundNBT getOrCreateNBT(ItemStack cardStack) {
        CompoundNBT tagCompound = cardStack.getTag();
        if (tagCompound == null) {
            tagCompound = new CompoundNBT();
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
        CompoundNBT tagCompound = getOrCreateNBT(cardStack);
        if (!tagCompound.hasKey("channel")) {
            SecurityChannels securityChannels = SecurityChannels.getChannels(getWorld());
            int id = securityChannels.newChannel();
            tagCompound.setInteger("channel", id);
            securityChannels.save();
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

        CompoundNBT masterNBT = masterCard.getTag();
        if (masterNBT == null) {
            return;
        }
        CompoundNBT linkerNBT = getOrCreateNBT(linkerCard);
        linkerNBT.setInteger("channel", masterNBT.getInteger("channel"));
        markDirtyClient();
    }

    private void addPlayer(String player) {
        CompoundNBT tagCompound = getCardInfo();
        if (tagCompound == null) {
            return;
        }
        if (tagCompound.hasKey("channel")) {
            SecurityChannels securityChannels = SecurityChannels.getChannels(getWorld());
            int id = tagCompound.getInteger("channel");
            SecurityChannels.SecurityChannel channel = securityChannels.getOrCreateChannel(id);
            channel.addPlayer(player);
            securityChannels.save();
            markDirtyClient();
        }
    }

    private void delPlayer(String player) {
        CompoundNBT tagCompound = getCardInfo();
        if (tagCompound == null) {
            return;
        }
        if (tagCompound.hasKey("channel")) {
            SecurityChannels securityChannels = SecurityChannels.getChannels(getWorld());
            int id = tagCompound.getInteger("channel");
            SecurityChannels.SecurityChannel channel = securityChannels.getOrCreateChannel(id);
            channel.delPlayer(player);
            securityChannels.save();
            markDirtyClient();
        }
    }

    private void setWhiteListMode(boolean whitelist) {
        CompoundNBT tagCompound = getCardInfo();
        if (tagCompound == null) {
            return;
        }
        if (tagCompound.hasKey("channel")) {
            SecurityChannels securityChannels = SecurityChannels.getChannels(getWorld());
            int id = tagCompound.getInteger("channel");
            SecurityChannels.SecurityChannel channel = securityChannels.getOrCreateChannel(id);
            channel.setWhitelist(whitelist);
            securityChannels.save();
            markDirtyClient();
        }
    }

    private void setChannelName(String name) {
        CompoundNBT tagCompound = getCardInfo();
        if (tagCompound == null) {
            return;
        }
        if (tagCompound.hasKey("channel")) {
            SecurityChannels securityChannels = SecurityChannels.getChannels(getWorld());
            int id = tagCompound.getInteger("channel");
            SecurityChannels.SecurityChannel channel = securityChannels.getOrCreateChannel(id);
            channel.setName(name);
            securityChannels.save();
            markDirtyClient();
        }
    }

    private CompoundNBT getCardInfo() {
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
    public boolean isUsableByPlayer(PlayerEntity player) {
        return canPlayerAccess(player);
    }

    @Override
    public boolean isItemValidForSlot(int index, ItemStack stack) {
        return SecuritySetup.securityCardItem.equals(stack.getItem());
    }

    @Override
    public void readRestorableFromNBT(CompoundNBT tagCompound) {
        super.readRestorableFromNBT(tagCompound);
        readBufferFromNBT(tagCompound, inventoryHelper);
    }

    @Override
    public void writeRestorableToNBT(CompoundNBT tagCompound) {
        super.writeRestorableToNBT(tagCompound);
        writeBufferToNBT(tagCompound, inventoryHelper);
    }

    @Override
    public boolean execute(ServerPlayerEntity playerMP, String command, TypedMap params) {
        boolean rc = super.execute(playerMP, command, params);
        if (rc) {
            return true;
        }
        if (CMD_SETCHANNELNAME.equals(command)) {
            setChannelName(params.get(PARAM_NAME));
            return true;
        } else if (CMD_SETMODE.equals(command)) {
            setWhiteListMode(params.get(PARAM_WHITELIST));
            return true;
        } else if (CMD_ADDPLAYER.equals(command)) {
            addPlayer(params.get(PARAM_PLAYER));
            return true;
        } else if (CMD_DELPLAYER.equals(command)) {
            delPlayer(params.get(PARAM_PLAYER));
            return true;
        }
        return false;
    }
}
