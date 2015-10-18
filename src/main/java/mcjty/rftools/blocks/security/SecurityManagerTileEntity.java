package mcjty.rftools.blocks.security;

import mcjty.lib.container.InventoryHelper;
import mcjty.lib.entity.GenericTileEntity;
import mcjty.lib.network.Argument;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.common.util.Constants;

import java.util.Map;

public class SecurityManagerTileEntity extends GenericTileEntity implements IInventory {

    public static final String CMD_SETCHANNELNAME = "setChannelName";
    public static final String CMD_SETMODE = "setMode";
    public static final String CMD_ADDPLAYER = "addPlayer";
    public static final String CMD_DELPLAYER = "delPlayer";

    private InventoryHelper inventoryHelper = new InventoryHelper(this, SecurityManagerContainer.factory, SecurityManagerContainer.BUFFER_SIZE + 2);

    @Override
    public boolean canUpdate() {
        return false;
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
        if (worldObj.isRemote) {
            return;
        }
        if (cardStack == null) {
            return;
        }
        NBTTagCompound tagCompound = getOrCreateNBT(cardStack);
        if (!tagCompound.hasKey("channel")) {
            SecurityChannels securityChannels = SecurityChannels.getChannels(worldObj);
            int id = securityChannels.newChannel();
            tagCompound.setInteger("channel", id);
            securityChannels.save(worldObj);
            worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
        }
    }

    private void updateLinkedCard() {
        if (worldObj.isRemote) {
            return;
        }
        ItemStack masterCard = inventoryHelper.getStackInSlot(SecurityManagerContainer.SLOT_CARD);
        if (masterCard == null) {
            return;
        }
        ItemStack linkerCard = inventoryHelper.getStackInSlot(SecurityManagerContainer.SLOT_LINKER);
        if (linkerCard == null) {
            return;
        }

        NBTTagCompound masterNBT = masterCard.getTagCompound();
        if (masterNBT == null) {
            return;
        }
        NBTTagCompound linkerNBT = getOrCreateNBT(linkerCard);
        linkerNBT.setInteger("channel", masterNBT.getInteger("channel"));
        worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
    }

    private void addPlayer(String player) {
        NBTTagCompound tagCompound = getCardInfo();
        if (tagCompound == null) return;
        if (tagCompound.hasKey("channel")) {
            SecurityChannels securityChannels = SecurityChannels.getChannels(worldObj);
            int id = tagCompound.getInteger("channel");
            SecurityChannels.SecurityChannel channel = securityChannels.getOrCreateChannel(id);
            channel.addPlayer(player);
            securityChannels.save(worldObj);
            worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
        }
    }

    private void delPlayer(String player) {
        NBTTagCompound tagCompound = getCardInfo();
        if (tagCompound == null) return;
        if (tagCompound.hasKey("channel")) {
            SecurityChannels securityChannels = SecurityChannels.getChannels(worldObj);
            int id = tagCompound.getInteger("channel");
            SecurityChannels.SecurityChannel channel = securityChannels.getOrCreateChannel(id);
            channel.delPlayer(player);
            securityChannels.save(worldObj);
            worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
        }
    }

    private void setWhiteListMode(boolean whitelist) {
        NBTTagCompound tagCompound = getCardInfo();
        if (tagCompound == null) return;
        if (tagCompound.hasKey("channel")) {
            SecurityChannels securityChannels = SecurityChannels.getChannels(worldObj);
            int id = tagCompound.getInteger("channel");
            SecurityChannels.SecurityChannel channel = securityChannels.getOrCreateChannel(id);
            channel.setWhitelist(whitelist);
            securityChannels.save(worldObj);
            worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
        }
    }

    private void setChannelName(String name) {
        NBTTagCompound tagCompound = getCardInfo();
        if (tagCompound == null) return;
        if (tagCompound.hasKey("channel")) {
            SecurityChannels securityChannels = SecurityChannels.getChannels(worldObj);
            int id = tagCompound.getInteger("channel");
            SecurityChannels.SecurityChannel channel = securityChannels.getOrCreateChannel(id);
            channel.setName(name);
            securityChannels.save(worldObj);
            worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
        }
    }

    private NBTTagCompound getCardInfo() {
        ItemStack cardStack = inventoryHelper.getStackInSlot(SecurityManagerContainer.SLOT_CARD);
        if (cardStack == null) {
            return null;
        }
        return getOrCreateNBT(cardStack);
    }

    @Override
    public int getSizeInventory() {
        return inventoryHelper.getCount();
    }

    @Override
    public ItemStack getStackInSlot(int index) {
        return inventoryHelper.getStackInSlot(index);
    }

    @Override
    public ItemStack decrStackSize(int index, int amount) {
        return inventoryHelper.decrStackSize(index, amount);
    }

    @Override
    public ItemStack getStackInSlotOnClosing(int index) {
        return null;
    }

    @Override
    public void setInventorySlotContents(int index, ItemStack stack) {
        inventoryHelper.setInventorySlotContents(getInventoryStackLimit(), index, stack);
        if (index == SecurityManagerContainer.SLOT_CARD) {
            updateCard(stack);
            updateLinkedCard();
        } else if (index == SecurityManagerContainer.SLOT_LINKER) {
            updateLinkedCard();
        }
    }

    @Override
    public String getInventoryName() {
        return "Security Manager Inventory";
    }

    @Override
    public boolean hasCustomInventoryName() {
        return false;
    }

    @Override
    public int getInventoryStackLimit() {
        return 1;
    }

    @Override
    public boolean isUseableByPlayer(EntityPlayer player) {
        return true;
    }

    @Override
    public void openInventory() {

    }

    @Override
    public void closeInventory() {

    }

    @Override
    public boolean isItemValidForSlot(int index, ItemStack stack) {
        return SecuritySetup.securityCardItem.equals(stack.getItem());
    }


    @Override
    public void readFromNBT(NBTTagCompound tagCompound) {
        super.readFromNBT(tagCompound);
    }

    @Override
    public void readRestorableFromNBT(NBTTagCompound tagCompound) {
        super.readRestorableFromNBT(tagCompound);
        readBufferFromNBT(tagCompound);
    }

    private void readBufferFromNBT(NBTTagCompound tagCompound) {
        NBTTagList bufferTagList = tagCompound.getTagList("Items", Constants.NBT.TAG_COMPOUND);
        for (int i = 0 ; i < bufferTagList.tagCount() ; i++) {
            NBTTagCompound nbtTagCompound = bufferTagList.getCompoundTagAt(i);
            inventoryHelper.setStackInSlot(i+SecurityManagerContainer.SLOT_CARD, ItemStack.loadItemStackFromNBT(nbtTagCompound));
        }
    }

    @Override
    public void writeToNBT(NBTTagCompound tagCompound) {
        super.writeToNBT(tagCompound);
    }

    @Override
    public void writeRestorableToNBT(NBTTagCompound tagCompound) {
        super.writeRestorableToNBT(tagCompound);
        writeBufferToNBT(tagCompound);
    }

    private void writeBufferToNBT(NBTTagCompound tagCompound) {
        NBTTagList bufferTagList = new NBTTagList();
        for (int i = 0 ; i < inventoryHelper.getCount() ; i++) {
            ItemStack stack = inventoryHelper.getStackInSlot(i);
            NBTTagCompound nbtTagCompound = new NBTTagCompound();
            if (stack != null) {
                stack.writeToNBT(nbtTagCompound);
            }
            bufferTagList.appendTag(nbtTagCompound);
        }
        tagCompound.setTag("Items", bufferTagList);
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
