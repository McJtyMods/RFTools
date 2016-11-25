package mcjty.rftools.items.teleportprobe;

import cofh.api.energy.IEnergyContainerItem;
import mcjty.lib.varia.GlobalCoordinate;
import mcjty.lib.varia.Logging;
import mcjty.rftools.ForgeEventHandlers;
import mcjty.rftools.RFTools;
import mcjty.rftools.blocks.teleporter.*;
import mcjty.rftools.items.GenericRFToolsItem;
import mcjty.rftools.items.ItemCapabilityProvider;
import net.minecraft.client.renderer.ItemMeshDefinition;
import net.minecraft.client.renderer.block.model.ModelBakery;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Keyboard;

import java.util.List;

public class ChargedPorterItem extends GenericRFToolsItem implements IEnergyContainerItem {

    private int capacity;
    private int maxReceive;
    private int maxExtract;

    public ChargedPorterItem() {
        this("charged_porter");
    }

    protected ChargedPorterItem(String name) {
        this(name, TeleportConfiguration.CHARGEDPORTER_MAXENERGY);
    }

    protected ChargedPorterItem(String name, int capacity) {
        super(name);
        setMaxStackSize(1);

        maxReceive = TeleportConfiguration.CHARGEDPORTER_RECEIVEPERTICK;
        maxExtract = 0;
    }

    @Override
    public ICapabilityProvider initCapabilities(ItemStack stack, NBTTagCompound nbt) {
        return new ItemCapabilityProvider(stack, this);
    }

    @Override
    public void onUpdate(ItemStack stack, World worldIn, Entity entityIn, int itemSlot, boolean isSelected) {
        if (!worldIn.isRemote) {
            NBTTagCompound tagCompound = stack.getTagCompound();
            if (tagCompound == null) {
                return;
            }
            if (!tagCompound.hasKey("tpTimer")) {
                return;
            }
            if (!(entityIn instanceof EntityPlayer)) {
                return;
            }
            EntityPlayer player = (EntityPlayer) entityIn;
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

        ModelLoader.setCustomMeshDefinition(this, new ItemMeshDefinition() {
            @Override
            public ModelResourceLocation getModelLocation(ItemStack stack) {
                NBTTagCompound tagCompound = stack.getTagCompound();
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
            }
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
    protected ActionResult<ItemStack> clOnItemRightClick(World world, EntityPlayer player, EnumHand hand) {
        ItemStack stack = player.getHeldItem(hand);
        if (!player.isSneaking()) {
            startTeleport(stack, player, world);
        } else {
            selectReceiver(stack, world, player);
        }
        return super.clOnItemRightClick(world, player, hand);
    }

    protected void selectReceiver(ItemStack stack, World world, EntityPlayer player) {
    }

    @Override
    protected EnumActionResult clOnItemUse(EntityPlayer player, World world, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        ItemStack stack = player.getHeldItem(hand);
        if (player.isSneaking()) {
            TileEntity te = world.getTileEntity(pos);
            setTarget(stack, player, world, te);
        } else {
            startTeleport(stack, player, world);
        }
        return EnumActionResult.SUCCESS;
    }

    private void startTeleport(ItemStack stack, EntityPlayer player, World world) {
        NBTTagCompound tagCompound = stack.getTagCompound();
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
            int energy = getEnergyStored(stack);
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

    private void setTarget(ItemStack stack, EntityPlayer player, World world, TileEntity te) {
        NBTTagCompound tagCompound = stack.getTagCompound();

        if (tagCompound == null) {
            tagCompound = new NBTTagCompound();
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
    public void addInformation(ItemStack itemStack, EntityPlayer player, List<String> list, boolean whatIsThis) {
        super.addInformation(itemStack, player, list, whatIsThis);
        NBTTagCompound tagCompound = itemStack.getTagCompound();
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
            list.add(TextFormatting.WHITE + RFTools.SHIFT_MESSAGE);
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
