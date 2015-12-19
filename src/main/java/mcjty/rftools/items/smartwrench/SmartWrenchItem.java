package mcjty.rftools.items.smartwrench;

import cofh.api.item.IToolHammer;
import mcjty.lib.varia.Coordinate;
import mcjty.lib.varia.GlobalCoordinate;
import mcjty.lib.varia.Logging;
import mcjty.rftools.RFTools;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.common.Optional;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.List;

@Optional.InterfaceList({
        @Optional.Interface(iface = "cofh.api.item.IToolHammer", modid = "CoFHLib")})
public class SmartWrenchItem extends Item implements IToolHammer, SmartWrench {
    public SmartWrenchItem() {
        setUnlocalizedName("smartwrench");
        setCreativeTab(RFTools.tabRfTools);
        setMaxStackSize(1);
        GameRegistry.registerItem(this, "smartwrench");
    }

    @SideOnly(Side.CLIENT)
    public void initModel() {
        ModelLoader.setCustomModelResourceLocation(this, 0, new ModelResourceLocation(RFTools.MODID + ":" + getUnlocalizedName().substring(5), "inventory"));
    }

    @Override
    @Optional.Method(modid = "CoFHLib")
    public boolean isUsable(ItemStack item, EntityLivingBase user, int x, int y, int z) {
        SmartWrenchMode mode = getCurrentMode(item);
        return mode == SmartWrenchMode.MODE_WRENCH;
    }

    @Override
    @Optional.Method(modid = "CoFHLib")
    public void toolUsed(ItemStack item, EntityLivingBase user, int x, int y, int z) {
    }

    @Override
    public ItemStack onItemRightClick(ItemStack stack, World world, EntityPlayer player) {
        if (!world.isRemote) {
            SmartWrenchMode mode = getCurrentMode(stack);
            if (mode == SmartWrenchMode.MODE_WRENCH) {
                mode = SmartWrenchMode.MODE_SELECT;
            } else {
                mode = SmartWrenchMode.MODE_WRENCH;
            }
            NBTTagCompound tagCompound = stack.getTagCompound();
            if (tagCompound == null) {
                tagCompound = new NBTTagCompound();
                stack.setTagCompound(tagCompound);
            }
            tagCompound.setString("mode", mode.getCode());
            Logging.message(player, EnumChatFormatting.YELLOW + "Smart wrench is now in " + mode.getName() + " mode.");
        }
        return super.onItemRightClick(stack, world, player);
    }

    @Override
    public boolean onItemUse(ItemStack stack, EntityPlayer player, World world, BlockPos pos, EnumFacing side, float hitX, float hitY, float hitZ) {
        if (!world.isRemote) {
            SmartWrenchMode mode = getCurrentMode(stack);
            if (mode == SmartWrenchMode.MODE_SELECT) {
                GlobalCoordinate b = getCurrentBlock(stack);
                if (b != null) {
                    if (b.getDimension() != world.provider.getDimensionId()) {
                        Logging.message(player, EnumChatFormatting.RED + "The selected block is in another dimension!");
                        return true;
                    }
                    TileEntity te = world.getTileEntity(b.getCoordinate());
                    if (te instanceof SmartWrenchSelector) {
                        SmartWrenchSelector smartWrenchSelector = (SmartWrenchSelector) te;
                        smartWrenchSelector.selectBlock(player, pos);
                    }
                }
            }
        }
        return true;
    }

    @Override
    public boolean doesSneakBypassUse(World world, BlockPos pos, EntityPlayer player) {
        return true;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void addInformation(ItemStack itemStack, EntityPlayer player, List list, boolean whatIsThis) {
        super.addInformation(itemStack, player, list, whatIsThis);
        GlobalCoordinate b = getCurrentBlock(itemStack);
        if (b != null) {
            list.add(EnumChatFormatting.GREEN + "Block: " + b.getCoordinate().toString() + " at dimension " + b.getDimension());
        }
        SmartWrenchMode mode = getCurrentMode(itemStack);
        list.add(EnumChatFormatting.WHITE + "Right-click on air to change mode.");
        list.add(EnumChatFormatting.GREEN + "Mode: " + mode.getName());
        if (mode == SmartWrenchMode.MODE_WRENCH) {
            list.add(EnumChatFormatting.WHITE + "Use as a normal wrench:");
            list.add(EnumChatFormatting.WHITE + "    Sneak-right-click to pick up machines.");
            list.add(EnumChatFormatting.WHITE + "    Right-click to rotate machines.");
        } else if (mode == SmartWrenchMode.MODE_SELECT) {
            list.add(EnumChatFormatting.WHITE + "Use as a block selector:");
            list.add(EnumChatFormatting.WHITE + "    Sneak-right-click select master block.");
            list.add(EnumChatFormatting.WHITE + "    Right-click to associate blocks with master.");
        }
    }

    @Override
    public SmartWrenchMode getMode(ItemStack itemStack) {
        SmartWrenchMode mode = SmartWrenchMode.MODE_WRENCH;
        NBTTagCompound tagCompound = itemStack.getTagCompound();
        if (tagCompound != null) {
            String modeString = tagCompound.getString("mode");
            if (modeString != null && !modeString.isEmpty()) {
                mode = SmartWrenchMode.getMode(modeString);
            }
        }
        return mode;
    }

    public static SmartWrenchMode getCurrentMode(ItemStack itemStack) {
        SmartWrenchMode mode = SmartWrenchMode.MODE_WRENCH;
        NBTTagCompound tagCompound = itemStack.getTagCompound();
        if (tagCompound != null) {
            String modeString = tagCompound.getString("mode");
            if (modeString != null && !modeString.isEmpty()) {
                mode = SmartWrenchMode.getMode(modeString);
            }
        }
        return mode;
    }

    public static void setCurrentBlock(ItemStack itemStack, GlobalCoordinate c) {
        NBTTagCompound tagCompound = itemStack.getTagCompound();
        if (tagCompound == null) {
            tagCompound = new NBTTagCompound();
            itemStack.setTagCompound(tagCompound);
        }

        if (c == null) {
            tagCompound.removeTag("selectedX");
            tagCompound.removeTag("selectedY");
            tagCompound.removeTag("selectedZ");
            tagCompound.removeTag("selectedDim");
        } else {
            tagCompound.setInteger("selectedX", c.getCoordinate().getX());
            tagCompound.setInteger("selectedY", c.getCoordinate().getY());
            tagCompound.setInteger("selectedZ", c.getCoordinate().getZ());
            tagCompound.setInteger("selectedDim", c.getDimension());
        }
    }

    public static GlobalCoordinate getCurrentBlock(ItemStack itemStack) {
        NBTTagCompound tagCompound = itemStack.getTagCompound();
        if (tagCompound != null && tagCompound.hasKey("selectedX")) {
            int x = tagCompound.getInteger("selectedX");
            int y = tagCompound.getInteger("selectedY");
            int z = tagCompound.getInteger("selectedZ");
            int dim = tagCompound.getInteger("selectedDim");
            return new GlobalCoordinate(new Coordinate(x, y, z), dim);
        }
        return null;
    }

    @Override
    public int getMaxItemUseDuration(ItemStack stack) {
        return 1;
    }
}