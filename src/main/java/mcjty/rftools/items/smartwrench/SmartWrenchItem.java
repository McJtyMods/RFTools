package mcjty.rftools.items.smartwrench;

import cofh.api.item.IToolHammer;
import mcjty.lib.api.smartwrench.SmartWrench;
import mcjty.lib.api.smartwrench.SmartWrenchMode;
import mcjty.lib.api.smartwrench.SmartWrenchSelector;
import mcjty.lib.compat.CompatBlock;
import mcjty.lib.compat.CompatItem;
import mcjty.lib.container.GenericBlock;
import mcjty.lib.varia.BlockPosTools;
import mcjty.lib.varia.GlobalCoordinate;
import mcjty.lib.varia.Logging;
import mcjty.rftools.RFTools;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.ItemMeshDefinition;
import net.minecraft.client.renderer.block.model.ModelBakery;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.List;

public class SmartWrenchItem extends CompatItem implements IToolHammer, SmartWrench {

    public SmartWrenchItem() {
        setUnlocalizedName("smartwrench");
        setRegistryName("smartwrench");
        setCreativeTab(RFTools.tabRfTools);
        setMaxStackSize(1);
        GameRegistry.register(this);
    }

    @SideOnly(Side.CLIENT)
    public void initModel() {
        ModelResourceLocation selectedModel = new ModelResourceLocation(getRegistryName() + "_select", "inventory");
        ModelResourceLocation normalModel = new ModelResourceLocation(getRegistryName(), "inventory");

        ModelBakery.registerItemVariants(this, selectedModel, normalModel);

        ModelLoader.setCustomMeshDefinition(this, new ItemMeshDefinition() {
            @Override
            public ModelResourceLocation getModelLocation(ItemStack stack) {
                SmartWrenchMode mode = getCurrentMode(stack);
                if (mode == SmartWrenchMode.MODE_SELECT) {
                    return selectedModel;
                } else {
                    return normalModel;
                }
            }
        });
    }

    @Override
    public boolean isUsable(ItemStack item, EntityLivingBase user, BlockPos pos) {
        SmartWrenchMode mode = getCurrentMode(item);
        return mode == SmartWrenchMode.MODE_WRENCH;
    }

    @Override
    public boolean isUsable(ItemStack item, EntityLivingBase user, Entity entity) {
        SmartWrenchMode mode = getCurrentMode(item);
        return mode == SmartWrenchMode.MODE_WRENCH;
    }

    @Override
    public void toolUsed(ItemStack item, EntityLivingBase user, BlockPos pos) {

    }

    @Override
    public void toolUsed(ItemStack item, EntityLivingBase user, Entity entity) {

    }

    @Override
    protected ActionResult<ItemStack> clOnItemRightClick(World world, EntityPlayer player, EnumHand hand) {
        ItemStack stack = player.getHeldItem(hand);
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
            Logging.message(player, TextFormatting.YELLOW + "Smart wrench is now in " + mode.getName() + " mode.");
        }
        return super.clOnItemRightClick(world, player, hand);
    }

    @Override
    protected EnumActionResult clOnItemUse(EntityPlayer player, World world, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        ItemStack stack = player.getHeldItem(hand);
        if (!world.isRemote) {
            if (player.isSneaking()) {
                // Make sure the block get activated if it is a GenericBlock
                IBlockState state = world.getBlockState(pos);
                Block block = state.getBlock();
                if (block instanceof GenericBlock) {
                    if (CompatBlock.activateBlock(block, world, pos, state, player, hand, facing, hitX, hitY, hitZ)) {
                        return EnumActionResult.SUCCESS;
                    }
                }
            }
            SmartWrenchMode mode = getCurrentMode(stack);
            if (mode == SmartWrenchMode.MODE_SELECT) {
                GlobalCoordinate b = getCurrentBlock(stack);
                if (b != null) {
                    if (b.getDimension() != world.provider.getDimension()) {
                        Logging.message(player, TextFormatting.RED + "The selected block is in another dimension!");
                        return EnumActionResult.FAIL;
                    }
                    TileEntity te = world.getTileEntity(b.getCoordinate());
                    if (te instanceof SmartWrenchSelector) {
                        SmartWrenchSelector smartWrenchSelector = (SmartWrenchSelector) te;
                        smartWrenchSelector.selectBlock(player, pos);
                    }
                }
            }
        }
        return EnumActionResult.SUCCESS;
    }

//    @Override
//    public boolean doesSneakBypassUse(ItemStack stack, IBlockAccess world, BlockPos pos, EntityPlayer player) {
//        return true;
//    }
//
    @SideOnly(Side.CLIENT)
    @Override
    public void addInformation(ItemStack itemStack, World player, List<String> list, ITooltipFlag whatIsThis) {
        super.addInformation(itemStack, player, list, whatIsThis);
        GlobalCoordinate b = getCurrentBlock(itemStack);
        if (b != null) {
            list.add(TextFormatting.GREEN + "Block: " + BlockPosTools.toString(b.getCoordinate()) + " at dimension " + b.getDimension());
        }
        SmartWrenchMode mode = getCurrentMode(itemStack);
        list.add(TextFormatting.WHITE + "Right-click on air to change mode.");
        list.add(TextFormatting.GREEN + "Mode: " + mode.getName());
        if (mode == SmartWrenchMode.MODE_WRENCH) {
            list.add(TextFormatting.WHITE + "Use as a normal wrench:");
            list.add(TextFormatting.WHITE + "    Sneak-right-click to pick up machines.");
            list.add(TextFormatting.WHITE + "    Right-click to rotate machines.");
        } else if (mode == SmartWrenchMode.MODE_SELECT) {
            list.add(TextFormatting.WHITE + "Use as a block selector:");
            list.add(TextFormatting.WHITE + "    Sneak-right-click select master block.");
            list.add(TextFormatting.WHITE + "    Right-click to associate blocks with master.");
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
            return new GlobalCoordinate(new BlockPos(x, y, z), dim);
        }
        return null;
    }

    @Override
    public int getMaxItemUseDuration(ItemStack stack) {
        return 1;
    }
}