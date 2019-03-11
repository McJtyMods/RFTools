package mcjty.rftools.items.storage;

import mcjty.lib.varia.EnergyTools;
import mcjty.lib.varia.IEnergyItem;
import mcjty.lib.varia.ItemCapabilityProvider;
import mcjty.lib.varia.Logging;
import mcjty.lib.varia.WorldTools;
import mcjty.rftools.RFTools;
import mcjty.rftools.blocks.storage.ModularStorageConfiguration;
import mcjty.rftools.blocks.storage.ModularStorageSetup;
import mcjty.rftools.setup.GuiProxy;
import mcjty.rftools.items.GenericRFToolsItem;
import mcjty.rftools.items.screenmodules.StorageControlModuleItem;
import net.minecraft.client.renderer.block.model.ModelBakery;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Keyboard;

import java.util.List;

public class StorageModuleTabletItem extends GenericRFToolsItem implements IEnergyItem {

    private int capacity;
    private int maxReceive;
    private int maxExtract;

    public static final int DAMAGE_EMPTY = 0;
    public static final int DAMAGE_FULL = 1;
    public static final int DAMAGE_SCANNER = 2;

    public static final int META_FOR_SCANNER = 666;

    public StorageModuleTabletItem() {
        super("storage_module_tablet");
        setMaxStackSize(1);

        capacity = ModularStorageConfiguration.TABLET_MAXENERGY;
        maxReceive = ModularStorageConfiguration.TABLET_RECEIVEPERTICK;
        maxExtract = ModularStorageConfiguration.TABLET_CONSUMEPERUSE;
    }

    @Override
    public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
        if (oldStack.isEmpty() != newStack.isEmpty()) {
            return true;
        }
        return oldStack.getItem() != newStack.getItem();
    }

    @Override
    public ICapabilityProvider initCapabilities(ItemStack stack, NBTTagCompound nbt) {
        return new ItemCapabilityProvider(stack, this);
    }


    @Override
    public int getMaxItemUseDuration(ItemStack stack) {
        return 1;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void initModel() {
        ModelBakery.registerItemVariants(this,
                new ModelResourceLocation(getRegistryName() + "_empty", "inventory"),
                new ModelResourceLocation(getRegistryName(), "inventory"));
        ModelLoader.setCustomModelResourceLocation(this, DAMAGE_EMPTY, new ModelResourceLocation(getRegistryName() + "_empty", "inventory"));
        ModelLoader.setCustomModelResourceLocation(this, DAMAGE_FULL, new ModelResourceLocation(getRegistryName(), "inventory"));
        ModelLoader.setCustomModelResourceLocation(this, DAMAGE_SCANNER, new ModelResourceLocation(getRegistryName() + "_scanner", "inventory"));
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
        ItemStack stack = player.getHeldItem(hand);
        // Make sure the tablet only works in main hand to avoid problems later
        if (hand != EnumHand.MAIN_HAND) {
            return new ActionResult<>(EnumActionResult.PASS, stack);
        }
        if (!world.isRemote) {
            NBTTagCompound tagCompound = stack.getTagCompound();
            if (tagCompound == null || !tagCompound.hasKey("childDamage")) {
                Logging.message(player, TextFormatting.YELLOW + "This tablet contains no storage module!");
                return new ActionResult<>(EnumActionResult.FAIL, stack);
            }

            int moduleDamage = tagCompound.getInteger("childDamage");
            int rfNeeded;
            if (moduleDamage == META_FOR_SCANNER) {
                rfNeeded = ModularStorageConfiguration.TABLET_CONSUMEPERUSE_SCANNER;
            } else {
                rfNeeded = ModularStorageConfiguration.TABLET_CONSUMEPERUSE;
                if (moduleDamage != StorageModuleItem.STORAGE_REMOTE) {
                    rfNeeded += ModularStorageConfiguration.TABLET_EXTRACONSUME * (moduleDamage + 1);
                }
            }

            int energy = tagCompound.getInteger("Energy");
            if (energy < rfNeeded) {
                Logging.message(player, TextFormatting.YELLOW + "Not enough energy to open the contents!");
                return new ActionResult<>(EnumActionResult.FAIL, stack);
            }

            energy -= rfNeeded;
            tagCompound.setInteger("Energy", energy);

            if (moduleDamage == META_FOR_SCANNER) {
                if (tagCompound.hasKey("monitorx")) {
                    int monitordim = tagCompound.getInteger("monitordim");
                    int monitorx = tagCompound.getInteger("monitorx");
                    int monitory = tagCompound.getInteger("monitory");
                    int monitorz = tagCompound.getInteger("monitorz");
                    BlockPos pos = new BlockPos(monitorx, monitory, monitorz);
                    WorldServer w = DimensionManager.getWorld(monitordim);
                    if (w == null || !WorldTools.chunkLoaded(w, pos)) {
                        player.sendStatusMessage(new TextComponentString(TextFormatting.RED + "Storage scanner is out of range!"), false);
                    } else {
                        player.openGui(RFTools.instance, GuiProxy.GUI_REMOTE_STORAGESCANNER_ITEM, player.getEntityWorld(), (int) player.posX, (int) player.posY, (int) player.posZ);
                    }
                } else {
                    player.sendStatusMessage(new TextComponentString(TextFormatting.RED + "Storage module is not linked to a storage scanner!"), false);
                }
            } else if (moduleDamage == StorageModuleItem.STORAGE_REMOTE) {
                if (!tagCompound.hasKey("id")) {
                    Logging.message(player, TextFormatting.YELLOW + "This remote storage module is not linked!");
                    return new ActionResult<>(EnumActionResult.FAIL, stack);
                }
                player.openGui(RFTools.instance, GuiProxy.GUI_REMOTE_STORAGE_ITEM, player.getEntityWorld(), (int) player.posX, (int) player.posY, (int) player.posZ);
            } else {
                player.openGui(RFTools.instance, GuiProxy.GUI_MODULAR_STORAGE_ITEM, player.getEntityWorld(), (int) player.posX, (int) player.posY, (int) player.posZ);
            }
            return new ActionResult<>(EnumActionResult.SUCCESS, stack);
        }
        return new ActionResult<>(EnumActionResult.SUCCESS, stack);
    }

    @Override
    public boolean hasContainerItem(ItemStack stack) {
        if (stack.getItemDamage() != DAMAGE_EMPTY) {
            return true;
        }
        return false;
    }

    @Override
    public Item getContainerItem() {
        return ModularStorageSetup.storageModuleTabletItem;
    }

    @Override
    public boolean isDamageable() {
        return super.isDamageable();
    }

//    @Override
//    public boolean getShareTag() {
//        return true;
//    }
//
//    @Nullable
//    @Override
//    public NBTTagCompound getNBTShareTag(ItemStack stack) {
//        if (!stack.hasTagCompound()) {
//            return stack.getTagCompound();
//        }
//        NBTTagCompound tagCompound = new NBTTagCompound();
//        NBTTagCompound tc = stack.getTagCompound();
//        tagCompound.setInteger("Energy", tc.getInteger("Energy"));
//        tagCompound.setInteger("childDamage", tc.getInteger("childDamage"));
//        if (tc.hasKey("grid")) {
//            tagCompound.setTag("grid", tc.getTag("grid"));
//        }
//        return tagCompound;
//    }

    @Override
    public ItemStack getContainerItem(ItemStack stack) {
        if (hasContainerItem(stack) && stack.hasTagCompound()) {
            NBTTagCompound tagCompound = new NBTTagCompound();
            tagCompound.setInteger("Energy", stack.getTagCompound().getInteger("Energy"));
            if (stack.getTagCompound().hasKey("grid")) {
                tagCompound.setTag("grid", stack.getTagCompound().getTag("grid"));
            }
            ItemStack container = new ItemStack(getContainerItem());
            container.setTagCompound(tagCompound);
            return container;
        }
        return ItemStack.EMPTY;
    }

    @Override
    public void addInformation(ItemStack itemStack, World player, List<String> list, ITooltipFlag whatIsThis) {
        super.addInformation(itemStack, player, list, whatIsThis);
        NBTTagCompound tagCompound = itemStack.getTagCompound();
        if (tagCompound != null) {
            list.add(TextFormatting.BLUE + "Energy: " + tagCompound.getInteger("Energy") + " RF");
            if (itemStack.getItemDamage() == DAMAGE_FULL) {
                int max = StorageModuleItem.MAXSIZE[tagCompound.getInteger("childDamage")];
                StorageModuleItem.addModuleInformation(list, max, tagCompound);
            } else if (itemStack.getItemDamage() == DAMAGE_SCANNER) {
                list.add(TextFormatting.YELLOW + "Storage scanner module installed!");
                StorageControlModuleItem.addModuleInformation(list, itemStack);
            } else {
                list.add(TextFormatting.YELLOW + "No storage module installed!");
            }
        }
        if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT)) {
            list.add("This RF/charged tablet can hold a storage module");
            list.add("and allows the wielder to manipulate the contents of");
            list.add("this module (remote or normal).");
            list.add("You can also craft this with a storage control");
            list.add("screen module for remote access to a storage scanner");
        } else {
            list.add(TextFormatting.WHITE + GuiProxy.SHIFT_MESSAGE);
        }
    }

    @Override
    public long receiveEnergyL(ItemStack container, long maxReceive, boolean simulate) {
        if (container.getTagCompound() == null) {
            container.setTagCompound(new NBTTagCompound());
        }
        int energy = container.getTagCompound().getInteger("Energy");
        int energyReceived = Math.min(capacity - energy, Math.min(this.maxReceive, EnergyTools.unsignedClampToInt(maxReceive)));

        if (!simulate) {
            energy += energyReceived;
            container.getTagCompound().setInteger("Energy", energy);
        }
        return energyReceived;
    }

    @Override
    public long extractEnergyL(ItemStack container, long maxExtract, boolean simulate) {
        if (container.getTagCompound() == null || !container.getTagCompound().hasKey("Energy")) {
            return 0;
        }
        int energy = container.getTagCompound().getInteger("Energy");
        int energyExtracted = Math.min(energy, Math.min(this.maxExtract, EnergyTools.unsignedClampToInt(maxExtract)));

        if (!simulate) {
            energy -= energyExtracted;
            container.getTagCompound().setInteger("Energy", energy);
        }
        return energyExtracted;
    }

    @Override
    public long getEnergyStoredL(ItemStack container) {
        if (container.getTagCompound() == null || !container.getTagCompound().hasKey("Energy")) {
            return 0;
        }
        return container.getTagCompound().getInteger("Energy");
    }

    @Override
    public long getMaxEnergyStoredL(ItemStack container) {
        return capacity;
    }
}
