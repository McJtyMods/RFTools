package mcjty.rftools.items.teleportprobe;

import mcjty.lib.crafting.INBTPreservingIngredient;
import mcjty.lib.varia.*;
import mcjty.rftools.ForgeEventHandlers;
import mcjty.rftools.blocks.teleporter.*;
import mcjty.rftools.blocks.teleporter.TeleportationTools;
import mcjty.rftools.setup.GuiProxy;
import net.minecraft.client.renderer.block.model.ModelBakery;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.common.capabilities.ICapabilityProvider;


import org.lwjgl.input.Keyboard;

import java.util.List;

public class ChargedPorterItem extends GenericRFToolsItem implements IEnergyItem, INBTPreservingIngredient {

    private int capacity;
    private int maxReceive;
    private int maxExtract;

    public ChargedPorterItem() {
        this("charged_porter");
    }

    protected ChargedPorterItem(String name) {
        this(name, TeleportConfiguration.CHARGEDPORTER_MAXENERGY.get());
    }

    protected ChargedPorterItem(String name, int capacity) {
        super(name);
        this.capacity = capacity;
        setMaxStackSize(1);

        maxReceive = TeleportConfiguration.CHARGEDPORTER_RECEIVEPERTICK.get();
        maxExtract = 0;
    }

    @Override
    public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
        if (oldStack.isEmpty() != newStack.isEmpty()) {
            return true;
        }
        return oldStack.getItem() != newStack.getItem();
    }

    @Override
    public ICapabilityProvider initCapabilities(ItemStack stack, CompoundNBT nbt) {
        return new ItemCapabilityProvider(stack, this);
    }

    @Override
    public void onUpdate(ItemStack stack, World worldIn, Entity entityIn, int itemSlot, boolean isSelected) {
        if (!worldIn.isRemote) {
            CompoundNBT tagCompound = stack.getTag();
            if (tagCompound == null) {
                return;
            }
            if (!tagCompound.hasKey("tpTimer")) {
                return;
            }
            if (!(entityIn instanceof PlayerEntity)) {
                return;
            }
            PlayerEntity player = (PlayerEntity) entityIn;
            int timer = tagCompound.getInteger("tpTimer");
            timer--;
            if (timer <= 0) {
                tagCompound.removeTag("tpTimer");
                TeleportDestinations destinations = TeleportDestinations.getDestinations(worldIn);
                int target = tagCompound.getInteger("target");
                GlobalCoordinate coordinate = destinations.getCoordinateForId(target);
                if (coordinate == null) {
                    Logging.message(player, TextFormatting.RED + "Something went wrong! The target has disappeared!");
                    TeleportationTools.applyEffectForSeverity(player, 3, false);
                    return;
                }
                TeleportDestination destination = destinations.getDestination(coordinate);
                ForgeEventHandlers.addPlayerToTeleportHere(destination, player);
//                    TeleportationTools.performTeleport(player, destination, 0, 10, false);
            } else {
                tagCompound.setInteger("tpTimer", timer);
            }
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void initModel() {
        for (int i = 0 ; i <= 8 ; i++) {
            String domain = getRegistryName().getResourceDomain();
            String path = getRegistryName().getResourcePath();
            ModelBakery.registerItemVariants(this, new ModelResourceLocation(new ResourceLocation(domain, path + i), "inventory"));
        }

        ModelLoader.setCustomMeshDefinition(this, stack -> {
            CompoundNBT tagCompound = stack.getTag();
            int energy = 0;
            if (tagCompound != null) {
                energy = tagCompound.getInteger("Energy");
            }
            int level = (9 * energy) / capacity;
            if (level < 0) {
                level = 0;
            } else if (level > 8) {
                level = 8;
            }
            String domain = getRegistryName().getResourceDomain();
            String path = getRegistryName().getResourcePath();
            return new ModelResourceLocation(new ResourceLocation(domain, path + (8 - level)), "inventory");
        });
    }


    protected int getSpeedBonus() {
        return 1;
    }

    @Override
    public int getMaxItemUseDuration(ItemStack stack) {
        return 1;
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, PlayerEntity player, Hand hand) {
        ItemStack stack = player.getHeldItem(hand);
        if (!player.isSneaking()) {
            startTeleport(stack, player, world);
        } else {
            selectReceiver(stack, world, player);
        }
        return super.onItemRightClick(world, player, hand);
    }

    protected void selectReceiver(ItemStack stack, World world, PlayerEntity player) {
    }

    @Override
    public ActionResultType onItemUse(PlayerEntity player, World world, BlockPos pos, Hand hand, Direction facing, float hitX, float hitY, float hitZ) {
        ItemStack stack = player.getHeldItem(hand);
        if (player.isSneaking()) {
            TileEntity te = world.getTileEntity(pos);
            setTarget(stack, player, world, te);
        } else {
            startTeleport(stack, player, world);
        }
        return ActionResultType.SUCCESS;
    }

    private void startTeleport(ItemStack stack, PlayerEntity player, World world) {
        CompoundNBT tagCompound = stack.getTag();
        if (tagCompound == null || (!tagCompound.hasKey("target")) || tagCompound.getInteger("target") == -1) {
            if (world.isRemote) {
                Logging.message(player, TextFormatting.RED + "The charged porter has no target.");
            }
            return;
        }

        if (!world.isRemote) {
            if (tagCompound.hasKey("tpTimer")) {
                Logging.message(player, TextFormatting.RED + "Already teleporting!");
                return;
            }

//            PorterProperties porterProperties = PlayerExtendedProperties.getPorterProperties(player);
//            if (porterProperties != null) {
//                if (porterProperties.isTeleporting()) {
//                    Logging.message(player, TextFormatting.RED + "Already teleporting!");
//                    return;
//                }
//
//            }

            int target = tagCompound.getInteger("target");

            TeleportDestinations destinations = TeleportDestinations.getDestinations(world);
            GlobalCoordinate coordinate = destinations.getCoordinateForId(target);
            if (coordinate == null) {
                Logging.message(player, TextFormatting.RED + "Something went wrong! The target has disappeared!");
                TeleportationTools.applyEffectForSeverity(player, 3, false);
                return;
            }
            TeleportDestination destination = destinations.getDestination(coordinate);

            if (!TeleportationTools.checkValidTeleport(player, world.provider.getDimension(), destination.getDimension())) {
                return;
            }

            BlockPos playerCoordinate = new BlockPos((int) player.posX, (int) player.posY, (int) player.posZ);
            int cost = TeleportationTools.calculateRFCost(world, playerCoordinate, destination);
            cost *= 1.5f;
            long energy = getEnergyStoredL(stack);
            if (cost > energy) {
                Logging.message(player, TextFormatting.RED + "Not enough energy to start the teleportation!");
                return;
            }
            extractEnergyNoMax(stack, cost, false);

            int ticks = TeleportationTools.calculateTime(world, playerCoordinate, destination);
            ticks /= getSpeedBonus();
//            if (porterProperties != null) {
//                porterProperties.startTeleport(target, ticks);
//            }
            tagCompound.setInteger("tpTimer", ticks);
            Logging.message(player, TextFormatting.YELLOW + "Start teleportation!");
        }
    }

    private void setTarget(ItemStack stack, PlayerEntity player, World world, TileEntity te) {
        CompoundNBT tagCompound = stack.getTag();

        if (tagCompound == null) {
            tagCompound = new CompoundNBT();
        }
        int id = -1;
        if (te instanceof MatterReceiverTileEntity) {
            MatterReceiverTileEntity matterReceiverTileEntity = (MatterReceiverTileEntity) te;
            if (!matterReceiverTileEntity.checkAccess(player.getName())) {
                Logging.message(player, TextFormatting.RED + "You have no access to target this receiver!");
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

    protected void selectOnReceiver(PlayerEntity player, World world, CompoundNBT tagCompound, int id) {
        if (world.isRemote) {
            Logging.message(player, "Charged porter target is set to " + id + ".");
        }
        tagCompound.setInteger("target", id);
    }

    protected void selectOnThinAir(PlayerEntity player, World world, CompoundNBT tagCompound, ItemStack stack) {
        if (world.isRemote) {
            Logging.message(player, "Charged porter is cleared.");
        }
        tagCompound.removeTag("target");
    }

    @Override
    public void addInformation(ItemStack itemStack, World player, List<String> list, ITooltipFlag whatIsThis) {
        super.addInformation(itemStack, player, list, whatIsThis);
        CompoundNBT tagCompound = itemStack.getTag();
        if (tagCompound != null) {
            list.add(TextFormatting.BLUE + "Energy: " + tagCompound.getInteger("Energy") + " RF");
            if (tagCompound.hasKey("target")) {
                list.add(TextFormatting.BLUE + "Target: " + tagCompound.getInteger("target"));
            } else {
                list.add(TextFormatting.RED + "No target set! Sneak-Right click on receiver to set.");
            }
        }
        if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT)) {
            list.add("This RF/charged item allows you to teleport to a");
            list.add("previously set matter receiver. Sneak-right click");
            list.add("on a receiver to set the destination.");
            list.add("Right click to perform the teleport.");
        } else {
            list.add(TextFormatting.WHITE + GuiProxy.SHIFT_MESSAGE);
        }
    }

    @Override
    public long receiveEnergyL(ItemStack container, long maxReceive, boolean simulate) {
        if (container.getTag() == null) {
            container.setTagCompound(new CompoundNBT());
        }
        int energy = container.getTag().getInteger("Energy");
        int energyReceived = Math.min(capacity - energy, Math.min(this.maxReceive, EnergyTools.unsignedClampToInt(maxReceive)));

        if (!simulate) {
            energy += energyReceived;
            container.getTag().setInteger("Energy", energy);
        }
        return energyReceived;
    }

    @Override
    public long extractEnergyL(ItemStack container, long maxExtract, boolean simulate) {
        if (container.getTag() == null || !container.getTag().hasKey("Energy")) {
            return 0;
        }
        int energy = container.getTag().getInteger("Energy");
        int energyExtracted = Math.min(energy, Math.min(this.maxExtract, EnergyTools.unsignedClampToInt(maxExtract)));

        if (!simulate) {
            energy -= energyExtracted;
            container.getTag().setInteger("Energy", energy);
        }
        return energyExtracted;
    }

    public int extractEnergyNoMax(ItemStack container, int maxExtract, boolean simulate) {
        if (container.getTag() == null || !container.getTag().hasKey("Energy")) {
            return 0;
        }
        int energy = container.getTag().getInteger("Energy");
        int energyExtracted = Math.min(energy, maxExtract);

        if (!simulate) {
            energy -= energyExtracted;
            container.getTag().setInteger("Energy", energy);
        }
        return energyExtracted;
    }

    @Override
    public long getEnergyStoredL(ItemStack container) {
        if (container.getTag() == null || !container.getTag().hasKey("Energy")) {
            return 0;
        }
        return container.getTag().getInteger("Energy");
    }

    @Override
    public long getMaxEnergyStoredL(ItemStack container) {
        return capacity;
    }
}
