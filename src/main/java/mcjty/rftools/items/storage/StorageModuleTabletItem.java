package mcjty.rftools.items.storage;

import cofh.api.energy.IEnergyContainerItem;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import mcjty.rftools.RFTools;
import mcjty.rftools.blocks.dimlets.DimletConfiguration;
import mcjty.rftools.blocks.storage.ModularStorageConfiguration;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IIcon;

import java.util.List;

public class StorageModuleTabletItem extends Item implements IEnergyContainerItem {

    private int capacity;
    private int maxReceive;
    private int maxExtract;

    private IIcon iconFull;
    private IIcon iconEmpty;

    public StorageModuleTabletItem() {
        setMaxStackSize(1);

        capacity = ModularStorageConfiguration.TABLET_MAXENERGY;
        maxReceive = ModularStorageConfiguration.TABLET_RECEIVEPERTICK;
        maxExtract = ModularStorageConfiguration.TABLET_CONSUMEPERUSE;
    }

    @Override
    public int getMaxItemUseDuration(ItemStack stack) {
        return 1;
    }

    @Override
    public void registerIcons(IIconRegister iconRegister) {
        iconFull = iconRegister.registerIcon(RFTools.MODID + ":storage/storageModuleTablet");
        iconEmpty = iconRegister.registerIcon(RFTools.MODID + ":storage/storageModuleTabletEmpty");
    }

    @SideOnly(Side.CLIENT)
    @Override
    public IIcon getIconIndex(ItemStack stack) {
//        NBTTagCompound tagCompound = stack.getTagCompound();
        return iconFull;
    }

    @Override
    public void addInformation(ItemStack itemStack, EntityPlayer player, List list, boolean whatIsThis) {
        super.addInformation(itemStack, player, list, whatIsThis);
        NBTTagCompound tagCompound = itemStack.getTagCompound();
        if (tagCompound != null) {
            list.add(EnumChatFormatting.BLUE + "Energy: " + tagCompound.getInteger("Energy") + " RF");
        }
        list.add("This RF/charged module can hold a storage module");
        list.add("and allows the wielder to manipulate the contents of");
        list.add("this module (remote or normal).");
    }

    @Override
    public int receiveEnergy(ItemStack container, int maxReceive, boolean simulate) {
        if (container.stackTagCompound == null) {
            container.stackTagCompound = new NBTTagCompound();
        }
        int energy = container.stackTagCompound.getInteger("Energy");
        int energyReceived = Math.min(capacity - energy, Math.min(this.maxReceive, maxReceive));

        if (!simulate) {
            energy += energyReceived;
            container.stackTagCompound.setInteger("Energy", energy);
        }
        return energyReceived;
    }

    @Override
    public int extractEnergy(ItemStack container, int maxExtract, boolean simulate) {
        if (container.stackTagCompound == null || !container.stackTagCompound.hasKey("Energy")) {
            return 0;
        }
        int energy = container.stackTagCompound.getInteger("Energy");
        int energyExtracted = Math.min(energy, Math.min(this.maxExtract, maxExtract));

        if (!simulate) {
            energy -= energyExtracted;
            container.stackTagCompound.setInteger("Energy", energy);
        }
        return energyExtracted;
    }

    @Override
    public int getEnergyStored(ItemStack container) {
        if (container.stackTagCompound == null || !container.stackTagCompound.hasKey("Energy")) {
            return 0;
        }
        return container.stackTagCompound.getInteger("Energy");
    }

    @Override
    public int getMaxEnergyStored(ItemStack container) {
        return capacity;
    }
}
