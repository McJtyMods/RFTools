package mcjty.rftools.items.builder;

import mcjty.lib.tools.ChatTools;
import mcjty.lib.tools.ItemStackTools;
import mcjty.lib.varia.BlockPosTools;
import mcjty.lib.varia.GlobalCoordinate;
import mcjty.lib.varia.Logging;
import mcjty.rftools.RFTools;
import mcjty.rftools.blocks.builder.BuilderConfiguration;
import mcjty.rftools.blocks.builder.BuilderTileEntity;
import mcjty.rftools.blocks.shaper.ScannerTileEntity;
import mcjty.rftools.items.GenericRFToolsItem;
import mcjty.rftools.shapes.*;
import mcjty.rftools.varia.RLE;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.oredict.OreDictionary;
import org.apache.commons.lang3.StringUtils;
import org.lwjgl.input.Keyboard;

import java.io.*;
import java.util.*;

public class ShapeCardItem extends GenericRFToolsItem {

    public static final int CARD_UNKNOWN = -2;          // Not known yet
    public static final int CARD_SPACE = -1;            // Not a shape card but a space card instead
    public static final int CARD_SHAPE = 0;
    public static final int CARD_VOID = 1;
    public static final int CARD_QUARRY = 2;
    public static final int CARD_QUARRY_SILK = 3;
    public static final int CARD_QUARRY_FORTUNE = 4;
    public static final int CARD_QUARRY_CLEAR = 5;
    public static final int CARD_QUARRY_CLEAR_SILK = 6;
    public static final int CARD_QUARRY_CLEAR_FORTUNE = 7;
    public static final int CARD_PUMP = 8;
    public static final int CARD_PUMP_CLEAR = 9;
    public static final int CARD_PUMP_LIQUID = 10;

    public static final int MAXIMUM_COUNT = 50000000;

    public static final int MODE_NONE = 0;
    public static final int MODE_CORNER1 = 1;
    public static final int MODE_CORNER2 = 2;

    public ShapeCardItem() {
        super("shape_card");
        setMaxStackSize(1);
        setHasSubtypes(true);
        setMaxDamage(0);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void initModel() {
        ModelLoader.setCustomModelResourceLocation(this, CARD_SHAPE, new ModelResourceLocation(RFTools.MODID + ":shape_card_def", "inventory"));
        ModelLoader.setCustomModelResourceLocation(this, CARD_VOID, new ModelResourceLocation(RFTools.MODID + ":shape_card_void", "inventory"));
        ModelLoader.setCustomModelResourceLocation(this, CARD_QUARRY, new ModelResourceLocation(RFTools.MODID + ":shape_card_quarry", "inventory"));
        ModelLoader.setCustomModelResourceLocation(this, CARD_QUARRY_SILK, new ModelResourceLocation(RFTools.MODID + ":shape_card_quarry_silk", "inventory"));
        ModelLoader.setCustomModelResourceLocation(this, CARD_QUARRY_FORTUNE, new ModelResourceLocation(RFTools.MODID + ":shape_card_quarry_fortune", "inventory"));
        ModelLoader.setCustomModelResourceLocation(this, CARD_QUARRY_CLEAR, new ModelResourceLocation(RFTools.MODID + ":shape_card_quarry_clear", "inventory"));
        ModelLoader.setCustomModelResourceLocation(this, CARD_QUARRY_CLEAR_SILK, new ModelResourceLocation(RFTools.MODID + ":shape_card_quarry_clear_silk", "inventory"));
        ModelLoader.setCustomModelResourceLocation(this, CARD_QUARRY_CLEAR_FORTUNE, new ModelResourceLocation(RFTools.MODID + ":shape_card_quarry_clear_fortune", "inventory"));
        ModelLoader.setCustomModelResourceLocation(this, CARD_PUMP, new ModelResourceLocation(RFTools.MODID + ":shape_card_pump", "inventory"));
        ModelLoader.setCustomModelResourceLocation(this, CARD_PUMP_CLEAR, new ModelResourceLocation(RFTools.MODID + ":shape_card_pump_clear", "inventory"));
        ModelLoader.setCustomModelResourceLocation(this, CARD_PUMP_LIQUID, new ModelResourceLocation(RFTools.MODID + ":shape_card_liquid", "inventory"));
    }

    @Override
    public int getMaxItemUseDuration(ItemStack stack) {
        return 1;
    }

    @Override
    protected EnumActionResult clOnItemUse(EntityPlayer player, World world, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        ItemStack stack = player.getHeldItem(hand);
        if (!world.isRemote) {
            int mode = getMode(stack);
            if (mode == MODE_NONE) {
                if (player.isSneaking()) {
                    if (world.getTileEntity(pos) instanceof BuilderTileEntity) {
                        setCurrentBlock(stack, new GlobalCoordinate(pos, world.provider.getDimension()));
                        Logging.message(player, TextFormatting.GREEN + "Now select the first corner");
                        setMode(stack, MODE_CORNER1);
                        setCorner1(stack, null);
                    } else {
                        Logging.message(player, TextFormatting.RED + "You can only do this on a builder!");
                    }
                } else {
                    return EnumActionResult.SUCCESS;
                }
            } else if (mode == MODE_CORNER1) {
                GlobalCoordinate currentBlock = getCurrentBlock(stack);
                if (currentBlock.getDimension() != world.provider.getDimension()) {
                    Logging.message(player, TextFormatting.RED + "The Builder is in another dimension!");
                } else if (currentBlock.getCoordinate().equals(pos)) {
                    Logging.message(player, TextFormatting.RED + "Cleared area selection mode!");
                    setMode(stack, MODE_NONE);
                } else {
                    Logging.message(player, TextFormatting.GREEN + "Now select the second corner");
                    setMode(stack, MODE_CORNER2);
                    setCorner1(stack, pos);
                }
            } else {
                GlobalCoordinate currentBlock = getCurrentBlock(stack);
                if (currentBlock.getDimension() != world.provider.getDimension()) {
                    Logging.message(player, TextFormatting.RED + "The Builder is in another dimension!");
                } else if (currentBlock.getCoordinate().equals(pos)) {
                    Logging.message(player, TextFormatting.RED + "Cleared area selection mode!");
                    setMode(stack, MODE_NONE);
                } else {
                    NBTTagCompound tag = getCompound(stack);
                    BlockPos c1 = getCorner1(stack);
                    if (c1 == null) {
                        Logging.message(player, TextFormatting.RED + "Cleared area selection mode!");
                        setMode(stack, MODE_NONE);
                    } else {
                        Logging.message(player, TextFormatting.GREEN + "New settings copied to the shape card!");
                        BlockPos center = new BlockPos((int) Math.ceil((c1.getX() + pos.getX()) / 2.0f), (int) Math.ceil((c1.getY() + pos.getY()) / 2.0f), (int) Math.ceil((c1.getZ() + pos.getZ()) / 2.0f));
                        setDimension(stack, Math.abs(c1.getX() - pos.getX()) + 1, Math.abs(c1.getY() - pos.getY()) + 1, Math.abs(c1.getZ() - pos.getZ()) + 1);
                        setOffset(stack, center.getX() - currentBlock.getCoordinate().getX(), center.getY() - currentBlock.getCoordinate().getY(), center.getZ() - currentBlock.getCoordinate().getZ());

                        setMode(stack, MODE_NONE);
                        setCorner1(stack, null);
                        setShape(stack, Shape.SHAPE_BOX, true);
                    }
                }
            }
        }
        return EnumActionResult.SUCCESS;
    }

    public static GlobalCoordinate getData(NBTTagCompound tagCompound) {
        if (!tagCompound.hasKey("datadim")) {
            return null;
        }
        int dim = tagCompound.getInteger("datadim");
        int x = tagCompound.getInteger("datax");
        int y = tagCompound.getInteger("datay");
        int z = tagCompound.getInteger("dataz");
        return new GlobalCoordinate(new BlockPos(x, y, z), dim);
    }

    public static void setData(NBTTagCompound tagCompound, int dimension, BlockPos pos) {
        tagCompound.setInteger("datadim", dimension);
        tagCompound.setInteger("datax", pos.getX());
        tagCompound.setInteger("datay", pos.getY());
        tagCompound.setInteger("dataz", pos.getZ());
        dirty(tagCompound);
    }

    public static void setModifier(NBTTagCompound tag, ShapeModifier modifier) {
        tag.setString("mod_op", modifier.getOperation().getCode());
        tag.setBoolean("mod_flipy", modifier.isFlipY());
        tag.setString("mod_rot", modifier.getRotation().getCode());
    }

    public static void setGhostMaterial(NBTTagCompound tag, ItemStack materialGhost) {
        if (ItemStackTools.isEmpty(materialGhost)) {
            tag.removeTag("ghost_block");
            tag.removeTag("ghost_meta");
        } else {
            Block block = Block.getBlockFromItem(materialGhost.getItem());
            if (block == null) {
                tag.removeTag("ghost_block");
                tag.removeTag("ghost_meta");
            } else {
                tag.setString("ghost_block", block.getRegistryName().toString());
                tag.setInteger("ghost_meta", materialGhost.getMetadata());
            }
        }
    }

    public static void setChildren(ItemStack itemStack, NBTTagList list) {
        NBTTagCompound tagCompound = getCompound(itemStack);
        NBTTagList listThis = tagCompound.getTagList("children", Constants.NBT.TAG_COMPOUND);
        boolean same = true;
        for (int i = 0 ; i < list.tagCount() ; i++) {
            NBTTagCompound child1 = listThis.getCompoundTagAt(i);
            int c1 = getCheck(child1);
            NBTTagCompound child2 = list.getCompoundTagAt(i);
            int c2 = getCheck(child1);
            if (c1 != c2) {
                same = false;
                break;
            }
            if (!(child1.getString("mod_op").equals(child2.getString("mod_op")) &&
                    child1.getString("mod_rot").equals(child2.getString("mod_rot")) &&
                    child1.getBoolean("mod_flipy") == child2.getBoolean("mod_flipy") &&
                    child1.getString("ghost_block").equals(child2.getString("ghost_block")) &&
                    child1.getInteger("ghost_meta") == child2.getInteger("ghost_meta"))) {
                same = false;
            }
        }
        tagCompound.setTag("children", list);
        if (!same) {
            dirty(tagCompound);
        }
    }

    public static void setDimension(ItemStack itemStack, int x, int y, int z) {
        NBTTagCompound tagCompound = getCompound(itemStack);
        if (tagCompound.getInteger("dimX") == x && tagCompound.getInteger("dimY") == y && tagCompound.getInteger("dimZ") == z) {
            return;
        }
        tagCompound.setInteger("dimX", x);
        tagCompound.setInteger("dimY", y);
        tagCompound.setInteger("dimZ", z);
        dirty(tagCompound);
    }


    public static void setOffset(ItemStack itemStack, int x, int y, int z) {
        NBTTagCompound tagCompound = getCompound(itemStack);
        if (tagCompound.getInteger("offsetX") == x && tagCompound.getInteger("offsetY") == y && tagCompound.getInteger("offsetZ") == z) {
            return;
        }
        tagCompound.setInteger("offsetX", x);
        tagCompound.setInteger("offsetY", y);
        tagCompound.setInteger("offsetZ", z);
        dirty(tagCompound);
    }

    private static NBTTagCompound getCompound(ItemStack itemStack) {
        NBTTagCompound tagCompound = itemStack.getTagCompound();
        if (tagCompound == null) {
            tagCompound = new NBTTagCompound();
            itemStack.setTagCompound(tagCompound);
        }
        return tagCompound;
    }

    public static void setCorner1(ItemStack itemStack, BlockPos corner) {
        NBTTagCompound tagCompound = getCompound(itemStack);
        if (corner == null) {
            tagCompound.removeTag("corner1x");
            tagCompound.removeTag("corner1y");
            tagCompound.removeTag("corner1z");
        } else {
            tagCompound.setInteger("corner1x", corner.getX());
            tagCompound.setInteger("corner1y", corner.getY());
            tagCompound.setInteger("corner1z", corner.getZ());
        }
    }

    public static BlockPos getCorner1(ItemStack stack1) {
        NBTTagCompound tagCompound = stack1.getTagCompound();
        if (tagCompound == null) {
            return null;
        }
        if (!tagCompound.hasKey("corner1x")) {
            return null;
        }
        return new BlockPos(tagCompound.getInteger("corner1x"), tagCompound.getInteger("corner1y"), tagCompound.getInteger("corner1z"));
    }

    public static int getMode(ItemStack itemStack) {
        NBTTagCompound tagCompound = itemStack.getTagCompound();
        if (tagCompound != null) {
            return tagCompound.getInteger("mode");
        } else {
            return MODE_NONE;
        }
    }

    public static void setMode(ItemStack itemStack, int mode) {
        NBTTagCompound tagCompound = getCompound(itemStack);
        if (tagCompound.getInteger("mode") == mode) {
            return;
        }
        tagCompound.setInteger("mode", mode);
        dirty(tagCompound);
    }

    public static void setCurrentBlock(ItemStack itemStack, GlobalCoordinate c) {
        NBTTagCompound tagCompound = getCompound(itemStack);

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


    @SideOnly(Side.CLIENT)
    @Override
    public void addInformation(ItemStack itemStack, EntityPlayer player, List<String> list, boolean whatIsThis) {
        super.addInformation(itemStack, player, list, whatIsThis);

        int type = itemStack.getItemDamage();
        if (!BuilderConfiguration.shapeCardAllowed) {
            list.add(TextFormatting.RED + "Disabled in config!");
        } else if (type != CARD_SHAPE) {
            if (!BuilderConfiguration.quarryAllowed) {
                list.add(TextFormatting.RED + "Disabled in config!");
            } else if (isClearingQuarry(type)) {
                if (!BuilderConfiguration.clearingQuarryAllowed) {
                    list.add(TextFormatting.RED + "Disabled in config!");
                }
            }
        }

        Shape shape = getShape(itemStack);
        boolean issolid = isSolid(itemStack);
        list.add(TextFormatting.GREEN + "Shape " + shape.getDescription() + " (" + (issolid ? "Solid" : "Hollow") + ")");
        list.add(TextFormatting.GREEN + "Dimension " + BlockPosTools.toString(getDimension(itemStack)));
        list.add(TextFormatting.GREEN + "Offset " + BlockPosTools.toString(getOffset(itemStack)));

        if (shape.isComposition()) {
            NBTTagCompound card = itemStack.getTagCompound();
            NBTTagList children = card.getTagList("children", Constants.NBT.TAG_COMPOUND);
            list.add(TextFormatting.DARK_GREEN + "Formulas: " + children.tagCount());
        }

        if (shape.isScan()) {
            NBTTagCompound card = itemStack.getTagCompound();
            int dim = card.getInteger("datadim");
            int x = card.getInteger("datax");
            int y = card.getInteger("datay");
            int z = card.getInteger("dataz");
            list.add(TextFormatting.DARK_GREEN + "Scanner at: " + x + "," + y + "," + z + " (dim " + dim + ")");
        }

        if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT)) {
            list.add(TextFormatting.YELLOW + "Sneak right click on builder to start mark mode");
            list.add(TextFormatting.YELLOW + "Then right click to mark two corners of wanted area");
            switch (type) {
                case CARD_PUMP_LIQUID:
                    list.add(TextFormatting.WHITE + "This item will cause the builder to place");
                    list.add(TextFormatting.WHITE + "liquids from an tank on top/bottom into the world.");
                    list.add(TextFormatting.GREEN + "Max area: " + BuilderConfiguration.maxBuilderDimension + "x" + Math.min(256, BuilderConfiguration.maxBuilderDimension) + "x" + BuilderConfiguration.maxBuilderDimension);
                    list.add(TextFormatting.GREEN + "Base cost: " + BuilderConfiguration.builderRfPerLiquid + " RF/t per block");
                    list.add(TextFormatting.GREEN + "(final cost depends on infusion level and block hardness)");
                    break;
                case CARD_PUMP:
                    list.add(TextFormatting.WHITE + "This item will cause the builder to collect");
                    list.add(TextFormatting.WHITE + "all liquids in the configured space.");
                    list.add(TextFormatting.WHITE + "The liquid will be replaced with " + getDirtOrCobbleName() + ".");
                    list.add(TextFormatting.GREEN + "Max area: " + BuilderConfiguration.maxBuilderDimension + "x" + Math.min(256, BuilderConfiguration.maxBuilderDimension) + "x" + BuilderConfiguration.maxBuilderDimension);
                    list.add(TextFormatting.GREEN + "Base cost: " + BuilderConfiguration.builderRfPerLiquid + " RF/t per block");
                    list.add(TextFormatting.GREEN + "(final cost depends on infusion level and block hardness)");
                    break;
                case CARD_PUMP_CLEAR:
                    list.add(TextFormatting.WHITE + "This item will cause the builder to collect");
                    list.add(TextFormatting.WHITE + "all liquids in the configured space.");
                    list.add(TextFormatting.WHITE + "The liquid will be removed from the world");
                    list.add(TextFormatting.GREEN + "Max area: " + BuilderConfiguration.maxBuilderDimension + "x" + Math.min(256, BuilderConfiguration.maxBuilderDimension) + "x" + BuilderConfiguration.maxBuilderDimension);
                    list.add(TextFormatting.GREEN + "Base cost: " + BuilderConfiguration.builderRfPerLiquid + " RF/t per block");
                    list.add(TextFormatting.GREEN + "(final cost depends on infusion level and block hardness)");
                    break;
                case CARD_VOID:
                    list.add(TextFormatting.WHITE + "This item will cause the builder to void");
                    list.add(TextFormatting.WHITE + "all blocks in the configured space.");
                    list.add(TextFormatting.GREEN + "Max area: " + BuilderConfiguration.maxBuilderDimension + "x" + Math.min(256, BuilderConfiguration.maxBuilderDimension) + "x" + BuilderConfiguration.maxBuilderDimension);
                    list.add(TextFormatting.GREEN + "Base cost: " + (int)(BuilderConfiguration.builderRfPerQuarry * BuilderConfiguration.voidShapeCardFactor) + " RF/t per block");
                    list.add(TextFormatting.GREEN + "(final cost depends on infusion level and block hardness)");
                    break;
                case CARD_SHAPE:
                    list.add(TextFormatting.WHITE + "This item can be configured as a shape. You");
                    list.add(TextFormatting.WHITE + "can then use it in the shield projector to make");
                    list.add(TextFormatting.WHITE + "a shield of that shape or in the builder to");
                    list.add(TextFormatting.WHITE + "actually build the shape");
                    list.add(TextFormatting.GREEN + "Max area: " + BuilderConfiguration.maxBuilderDimension + "x" + Math.min(256, BuilderConfiguration.maxBuilderDimension) + "x" + BuilderConfiguration.maxBuilderDimension);
                    list.add(TextFormatting.GREEN + "Base cost: " + BuilderConfiguration.builderRfPerOperation + " RF/t per block");
                    list.add(TextFormatting.GREEN + "(final cost depends on infusion level)");
                    break;
                case CARD_QUARRY_SILK:
                    list.add(TextFormatting.WHITE + "This item will cause the builder to quarry");
                    list.add(TextFormatting.WHITE + "all blocks in the configured space and replace");
                    list.add(TextFormatting.WHITE + "them with " + getDirtOrCobbleName() + ".");
                    list.add(TextFormatting.WHITE + "Blocks are harvested with silk touch");
                    list.add(TextFormatting.GREEN + "Max area: " + BuilderConfiguration.maxBuilderDimension + "x" + Math.min(256, BuilderConfiguration.maxBuilderDimension) + "x" + BuilderConfiguration.maxBuilderDimension);
                    list.add(TextFormatting.GREEN + "Base cost: " + (int)(BuilderConfiguration.builderRfPerQuarry * BuilderConfiguration.silkquarryShapeCardFactor) + " RF/t per block");
                    list.add(TextFormatting.GREEN + "(final cost depends on infusion level and block hardness)");
                    break;
                case CARD_QUARRY_CLEAR_SILK:
                    list.add(TextFormatting.WHITE + "This item will cause the builder to quarry");
                    list.add(TextFormatting.WHITE + "all blocks in the configured space.");
                    list.add(TextFormatting.WHITE + "Blocks are harvested with silk touch");
                    list.add(TextFormatting.GREEN + "Max area: " + BuilderConfiguration.maxBuilderDimension + "x" + Math.min(256, BuilderConfiguration.maxBuilderDimension) + "x" + BuilderConfiguration.maxBuilderDimension);
                    list.add(TextFormatting.GREEN + "Base cost: " + (int)(BuilderConfiguration.builderRfPerQuarry * BuilderConfiguration.silkquarryShapeCardFactor) + " RF/t per block");
                    list.add(TextFormatting.GREEN + "(final cost depends on infusion level and block hardness)");
                    break;
                case CARD_QUARRY_FORTUNE:
                    list.add(TextFormatting.WHITE + "This item will cause the builder to quarry");
                    list.add(TextFormatting.WHITE + "all blocks in the configured space and replace");
                    list.add(TextFormatting.WHITE + "them with " + getDirtOrCobbleName() + ".");
                    list.add(TextFormatting.WHITE + "Blocks are harvested with fortune");
                    list.add(TextFormatting.GREEN + "Max area: " + BuilderConfiguration.maxBuilderDimension + "x" + Math.min(256, BuilderConfiguration.maxBuilderDimension) + "x" + BuilderConfiguration.maxBuilderDimension);
                    list.add(TextFormatting.GREEN + "Base cost: " + (int)(BuilderConfiguration.builderRfPerQuarry * BuilderConfiguration.fortunequarryShapeCardFactor) + " RF/t per block");
                    list.add(TextFormatting.GREEN + "(final cost depends on infusion level and block hardness)");
                    break;
                case CARD_QUARRY_CLEAR_FORTUNE:
                    list.add(TextFormatting.WHITE + "This item will cause the builder to quarry");
                    list.add(TextFormatting.WHITE + "all blocks in the configured space.");
                    list.add(TextFormatting.WHITE + "Blocks are harvested with fortune");
                    list.add(TextFormatting.GREEN + "Max area: " + BuilderConfiguration.maxBuilderDimension + "x" + Math.min(256, BuilderConfiguration.maxBuilderDimension) + "x" + BuilderConfiguration.maxBuilderDimension);
                    list.add(TextFormatting.GREEN + "Base cost: " + (int)(BuilderConfiguration.builderRfPerQuarry * BuilderConfiguration.fortunequarryShapeCardFactor) + " RF/t per block");
                    list.add(TextFormatting.GREEN + "(final cost depends on infusion level and block hardness)");
                    break;
                case CARD_QUARRY:
                    list.add(TextFormatting.WHITE + "This item will cause the builder to quarry");
                    list.add(TextFormatting.WHITE + "all blocks in the configured space and replace");
                    list.add(TextFormatting.WHITE + "them with " + getDirtOrCobbleName() + ".");
                    list.add(TextFormatting.GREEN + "Max area: " + BuilderConfiguration.maxBuilderDimension + "x" + Math.min(256, BuilderConfiguration.maxBuilderDimension) + "x" + BuilderConfiguration.maxBuilderDimension);
                    list.add(TextFormatting.GREEN + "Base cost: " + BuilderConfiguration.builderRfPerQuarry + " RF/t per block");
                    list.add(TextFormatting.GREEN + "(final cost depends on infusion level and block hardness)");
                    break;
                case CARD_QUARRY_CLEAR:
                    list.add(TextFormatting.WHITE + "This item will cause the builder to quarry");
                    list.add(TextFormatting.WHITE + "all blocks in the configured space");
                    list.add(TextFormatting.GREEN + "Max area: " + BuilderConfiguration.maxBuilderDimension + "x" + Math.min(256, BuilderConfiguration.maxBuilderDimension) + "x" + BuilderConfiguration.maxBuilderDimension);
                    list.add(TextFormatting.GREEN + "Base cost: " + BuilderConfiguration.builderRfPerQuarry + " RF/t per block");
                    list.add(TextFormatting.GREEN + "(final cost depends on infusion level and block hardness)");
                    break;
            }
        } else {
            list.add(TextFormatting.WHITE + RFTools.SHIFT_MESSAGE);
        }
    }

    private String getDirtOrCobbleName() {
        return BuilderConfiguration.getQuarryReplace().getLocalizedName();
    }

    /**
     * Return true if the card is a normal card (not a quarry or void card)
     * @param stack
     * @return
     */
    public static boolean isNormalShapeCard(ItemStack stack) {
        return stack.getItemDamage() == CARD_SHAPE;
    }

    public static boolean isClearingQuarry(int type) {
        return type == CARD_QUARRY_CLEAR || type == CARD_QUARRY_CLEAR_FORTUNE || type == CARD_QUARRY_CLEAR_SILK;
    }

    public static boolean isQuarry(int type) {
        return type == CARD_QUARRY_CLEAR || type == CARD_QUARRY_CLEAR_FORTUNE || type == CARD_QUARRY_CLEAR_SILK ||
                type == CARD_QUARRY || type == CARD_QUARRY_FORTUNE || type == CARD_QUARRY_SILK;
    }

    private static void addBlocks(Set<Block> blocks, Block block, boolean oredict) {
        blocks.add(block);
        if (oredict) {
            int[] iDs = OreDictionary.getOreIDs(new ItemStack(block));
            for (int id : iDs) {
                String oreName = OreDictionary.getOreName(id);
                List<ItemStack> ores = ItemStackTools.getOres(oreName);
                for (ItemStack ore : ores) {
                    if (ore.getItem() instanceof ItemBlock) {
                        blocks.add(((ItemBlock)ore.getItem()).getBlock());
                    }
                }
            }
        }
    }

    public static Set<Block> getVoidedBlocks(ItemStack stack) {
        Set<Block> blocks = new HashSet<Block>();
        boolean oredict = isOreDictionary(stack);
        if (isVoiding(stack, "stone")) {
            addBlocks(blocks, Blocks.STONE, oredict);
        }
        if (isVoiding(stack, "cobble")) {
            addBlocks(blocks, Blocks.COBBLESTONE, oredict);
        }
        if (isVoiding(stack, "dirt")) {
            addBlocks(blocks, Blocks.DIRT, oredict);
            addBlocks(blocks, Blocks.GRASS, oredict);
        }
        if (isVoiding(stack, "sand")) {
            addBlocks(blocks, Blocks.SAND, oredict);
        }
        if (isVoiding(stack, "gravel")) {
            addBlocks(blocks, Blocks.GRAVEL, oredict);
        }
        if (isVoiding(stack, "netherrack")) {
            addBlocks(blocks, Blocks.NETHERRACK, oredict);
        }
        return blocks;
    }

    public static boolean isOreDictionary(ItemStack stack) {
        NBTTagCompound tagCompound = stack.getTagCompound();
        if (tagCompound == null) {
            return false;
        }
        return tagCompound.getBoolean("oredict");
    }

    public static boolean isVoiding(ItemStack stack, String material) {
        NBTTagCompound tagCompound = stack.getTagCompound();
        if (tagCompound == null) {
            return false;
        }
        return tagCompound.getBoolean("void" + material);
    }

    public static Shape getShape(ItemStack stack) {
        NBTTagCompound tagCompound = stack.getTagCompound();
        return getShape(tagCompound);
    }

    public static Shape getShape(NBTTagCompound tagCompound) {
        if (tagCompound == null) {
            return Shape.SHAPE_BOX;
        }
        if (!tagCompound.hasKey("shape") && !tagCompound.hasKey("shapenew")) {
            return Shape.SHAPE_BOX;
        }
        Shape shape;
        if (tagCompound.hasKey("shapenew")) {
            String sn = tagCompound.getString("shapenew");
            shape = Shape.getShape(sn);
        } else {
            int shapedeprecated = tagCompound.getInteger("shape");
            ShapeDeprecated sd = ShapeDeprecated.getShape(shapedeprecated);
            shape = sd.getNewshape();
        }

        if (shape == null) {
            return Shape.SHAPE_BOX;
        }
        return shape;
    }

    public static boolean isSolid(ItemStack stack) {
        NBTTagCompound tagCompound = stack.getTagCompound();
        return isSolid(tagCompound);
    }

    public static boolean isSolid(NBTTagCompound tagCompound) {
        if (tagCompound == null) {
            return true;
        }
        if (!tagCompound.hasKey("shape") && !tagCompound.hasKey("shapenew")) {
            return true;
        }
        if (tagCompound.hasKey("shapenew")) {
            return tagCompound.getBoolean("solid");
        } else {
            int shapedeprecated = tagCompound.getInteger("shape");
            ShapeDeprecated sd = ShapeDeprecated.getShape(shapedeprecated);
            return sd.isSolid();
        }
    }

    public static IFormula createCorrectFormula(NBTTagCompound tagCompound) {
        Shape shape = getShape(tagCompound);
        boolean solid = isSolid(tagCompound);
        IFormula formula = shape.getFormulaFactory().createFormula();
        return formula.correctFormula(solid);
    }

    public static int getCheck(ItemStack stack) {
        NBTTagCompound tagCompound = getCompound(stack);
        return getCheck(tagCompound);
    }

    public static int getCheck(NBTTagCompound tagCompound) {
        int check = tagCompound.getInteger("check");
        NBTTagList children = tagCompound.getTagList("children", Constants.NBT.TAG_COMPOUND);
        for (int i = 0 ; i < children.tagCount() ; i++) {
            NBTTagCompound child = children.getCompoundTagAt(i);
            check += getCheck(child) << 6;
        }
        return check;
    }

    public static void dirty(NBTTagCompound tag) {
        tag.setInteger("check", tag.getInteger("check") + 1);
    }

    public static void setShape(ItemStack stack, Shape shape, boolean solid) {
        NBTTagCompound tagCompound = getCompound(stack);
        if (isSolid(tagCompound) == solid && getShape(tagCompound).equals(shape)) {
            // Nothing happens
            return;
        }
        tagCompound.setString("shapenew", shape.getDescription());
        tagCompound.setBoolean("solid", solid);
        dirty(tagCompound);
    }

    public static BlockPos getDimension(ItemStack stack) {
        NBTTagCompound tagCompound = stack.getTagCompound();
        return getDimension(tagCompound);
    }

    public static BlockPos getDimension(NBTTagCompound tagCompound) {
        if (tagCompound == null) {
            return new BlockPos(5, 5, 5);
        }
        if (!tagCompound.hasKey("dimX")) {
            return new BlockPos(5, 5, 5);
        }
        int dimX = tagCompound.getInteger("dimX");
        int dimY = tagCompound.getInteger("dimY");
        int dimZ = tagCompound.getInteger("dimZ");
        return new BlockPos(dimX, clampDimension(dimY, 256), dimZ);
    }

    public static BlockPos getClampedDimension(ItemStack stack, int maximum) {
        NBTTagCompound tagCompound = stack.getTagCompound();
        return getClampedDimension(tagCompound, maximum);
    }

    public static BlockPos getClampedDimension(NBTTagCompound tagCompound, int maximum) {
        if (tagCompound == null) {
            return new BlockPos(5, 5, 5);
        }
        int dimX = tagCompound.getInteger("dimX");
        int dimY = tagCompound.getInteger("dimY");
        int dimZ = tagCompound.getInteger("dimZ");
        return new BlockPos(clampDimension(dimX, maximum), clampDimension(dimY, maximum), clampDimension(dimZ, maximum));
    }

    private static int clampDimension(int o, int maximum) {
        if (o > maximum) {
            o = maximum;
        } else if (o < 0) {
            o = 0;
        }
        return o;
    }

    public static BlockPos getOffset(ItemStack stack) {
        NBTTagCompound tagCompound = stack.getTagCompound();
        if (tagCompound == null) {
            return new BlockPos(0, 0, 0);
        }
        int offsetX = tagCompound.getInteger("offsetX");
        int offsetY = tagCompound.getInteger("offsetY");
        int offsetZ = tagCompound.getInteger("offsetZ");
        return new BlockPos(offsetX, offsetY, offsetZ);
    }

    public static BlockPos getClampedOffset(ItemStack stack, int maximum) {
        NBTTagCompound tagCompound = stack.getTagCompound();
        return getClampedOffset(tagCompound, maximum);
    }

    public static BlockPos getClampedOffset(NBTTagCompound tagCompound, int maximum) {
        if (tagCompound == null) {
            return new BlockPos(0, 0, 0);
        }
        int offsetX = tagCompound.getInteger("offsetX");
        int offsetY = tagCompound.getInteger("offsetY");
        int offsetZ = tagCompound.getInteger("offsetZ");
        return new BlockPos(clampOffset(offsetX, maximum), clampOffset(offsetY, maximum), clampOffset(offsetZ, maximum));
    }

    private static int clampOffset(int o, int maximum) {
        if (o < -maximum) {
            o = -maximum;
        } else if (o > maximum) {
            o = maximum;
        }
        return o;
    }

    @Override
    protected ActionResult<ItemStack> clOnItemRightClick(World world, EntityPlayer player, EnumHand hand) {
        ItemStack stack = player.getHeldItem(hand);
        if (world.isRemote) {
            player.openGui(RFTools.instance, RFTools.GUI_SHAPECARD, player.getEntityWorld(), (int) player.posX, (int) player.posY, (int) player.posZ);
            return new ActionResult<>(EnumActionResult.SUCCESS, stack);
        }
        return new ActionResult<>(EnumActionResult.SUCCESS, stack);
    }

    public static BlockPos getMinCorner(BlockPos thisCoord, BlockPos dimension, BlockPos offset) {
        int xCoord = thisCoord.getX();
        int yCoord = thisCoord.getY();
        int zCoord = thisCoord.getZ();
        int dx = dimension.getX();
        int dy = dimension.getY();
        int dz = dimension.getZ();
        return new BlockPos(xCoord - dx/2 + offset.getX(), yCoord - dy/2 + offset.getY(), zCoord - dz/2 + offset.getZ());
    }

    public static BlockPos getMaxCorner(BlockPos thisCoord, BlockPos dimension, BlockPos offset) {
        int dx = dimension.getX();
        int dy = dimension.getY();
        int dz = dimension.getZ();
        BlockPos minCorner = getMinCorner(thisCoord, dimension, offset);
        return new BlockPos(minCorner.getX() + dx, minCorner.getY() + dy, minCorner.getZ() + dz);
    }

    public static int countBlocks(ItemStack shapeCard, Shape shape, boolean solid, BlockPos dimension) {
        final int[] cnt = {0};
        BlockPos offset = new BlockPos(0, 128, 0);
        BlockPos clamped = new BlockPos(Math.min(dimension.getX(), 512), Math.min(dimension.getY(), 256), Math.min(dimension.getZ(), 512));
        composeFormula(shapeCard, shape.getFormulaFactory().createFormula(), null, new BlockPos(0, 0, 0), clamped, offset, new AbstractMap<BlockPos, IBlockState>() {
            @Override
            public IBlockState put(BlockPos key, IBlockState value) {
                cnt[0]++;
                return value;
            }

            @Override
            public Set<Entry<BlockPos, IBlockState>> entrySet() {
                return Collections.emptySet();
            }

            @Override
            public int size() {
                return 0;
            }
        }, MAXIMUM_COUNT+1, solid, false, null);
        return cnt[0];
    }

    public static boolean xInChunk(int x, ChunkPos chunk) {
        if (chunk == null) {
            return true;
        } else {
            return chunk.chunkXPos == (x>>4);
        }
    }

    public static boolean zInChunk(int z, ChunkPos chunk) {
        if (chunk == null) {
            return true;
        } else {
            return chunk.chunkZPos == (z>>4);
        }
    }

    private static void placeBlockIfPossible(World worldObj, Map<BlockPos, IBlockState> blocks, int maxSize, int x, int y, int z, IBlockState state, boolean forquarry) {
        BlockPos c = new BlockPos(x, y, z);
        if (worldObj == null) {
            blocks.put(c, state);
            return;
        }
        if (forquarry) {
            if (worldObj.isAirBlock(c)) {
                return;
            }
            blocks.put(c, state);
        } else {
            if (BuilderTileEntity.isEmptyOrReplacable(worldObj, c) && blocks.size() < maxSize - 1) {
                blocks.put(c, state);
            }
        }
    }

    public static int getRenderPositions(ItemStack stack, Shape shape, boolean solid, RLE positions, StatePalette statePalette) {
        BlockPos dimension = ShapeCardItem.getDimension(stack);
        BlockPos clamped = new BlockPos(Math.min(dimension.getX(), 512), Math.min(dimension.getY(), 256), Math.min(dimension.getZ(), 512));

        IFormula formula = shape.getFormulaFactory().createFormula();
        int dx = clamped.getX();
        int dy = clamped.getY();
        int dz = clamped.getZ();

        formula = formula.correctFormula(solid);
        formula.setup(new BlockPos(0, 0, 0), clamped, new BlockPos(0, 0, 0), stack != null ? stack.getTagCompound() : null);

        int cnt = 0;
        for (int oy = 0; oy < dy; oy++) {
            int y = oy - dy/2;
            for (int ox = 0; ox < dx; ox++) {
                int x = ox - dx/2;
                for (int oz = 0; oz < dz; oz++) {
                    int z = oz - dz/2;
                    int v = 255;
                    if (formula.isInside(x, y, z)) {
                        cnt++;
                        IBlockState lastState = formula.getLastState();
                        if (solid) {
                            if (ox == 0 || ox == dx - 1 || oy == 0 || oy == dy - 1 || oz == 0 || oz == dz - 1) {
                                v = statePalette.alloc(lastState, -1) + 1;
                            } else if (!formula.isInside(x - 1, y, z) || !formula.isInside(x + 1, y, z) || !formula.isInside(x, y - 1, z) || !formula.isInside(x, y + 1, z) || !formula.isInside(x, y, z - 1) || !formula.isInside(x, y, z + 1)) {
                                v = statePalette.alloc(lastState, -1) + 1;
                            }
                        } else {
                            v = statePalette.alloc(lastState, -1) + 1;
                        }
                    }
                    positions.add(v);
                }
            }
        }
        return cnt;
    }

    // Used for saving
    public static int getDataPositions(ItemStack stack, Shape shape, boolean solid, RLE positions, StatePalette statePalette) {
        BlockPos dimension = ShapeCardItem.getDimension(stack);
        BlockPos clamped = new BlockPos(Math.min(dimension.getX(), 512), Math.min(dimension.getY(), 256), Math.min(dimension.getZ(), 512));

        IFormula formula = shape.getFormulaFactory().createFormula();
        int dx = clamped.getX();
        int dy = clamped.getY();
        int dz = clamped.getZ();

        formula = formula.correctFormula(solid);
        formula.setup(new BlockPos(0, 0, 0), clamped, new BlockPos(0, 0, 0), stack != null ? stack.getTagCompound() : null);

        int cnt = 0;
        for (int oy = 0; oy < dy; oy++) {
            int y = oy - dy/2;
            for (int ox = 0; ox < dx; ox++) {
                int x = ox - dx/2;
                for (int oz = 0; oz < dz; oz++) {
                    int z = oz - dz/2;
                    int v = 255;
                    if (formula.isInside(x, y, z)) {
                        cnt++;
                        IBlockState lastState = formula.getLastState();
                        if (lastState == null) {
                            lastState = Blocks.STONE.getDefaultState();
                        }
                        v = statePalette.alloc(lastState, 0) + 1;
                    }
                    System.out.println("v = " + v);
                    positions.add(v);
                }
            }
        }
        return cnt;
    }



    public static void composeFormula(ItemStack shapeCard, IFormula formula, World worldObj, BlockPos thisCoord, BlockPos dimension, BlockPos offset, Map<BlockPos, IBlockState> blocks, int maxSize, boolean solid, boolean forquarry, ChunkPos chunk) {
        int xCoord = thisCoord.getX();
        int yCoord = thisCoord.getY();
        int zCoord = thisCoord.getZ();
        int dx = dimension.getX();
        int dy = dimension.getY();
        int dz = dimension.getZ();
        BlockPos tl = new BlockPos(xCoord - dx/2 + offset.getX(), yCoord - dy/2 + offset.getY(), zCoord - dz/2 + offset.getZ());

        formula = formula.correctFormula(solid);
        formula.setup(thisCoord, dimension, offset, shapeCard != null ? shapeCard.getTagCompound() : null);

        for (int ox = 0 ; ox < dx ; ox++) {
            int x = tl.getX() + ox;
            if (xInChunk(x, chunk)) {
                for (int oz = 0 ; oz < dz ; oz++) {
                    int z = tl.getZ() + oz;
                    if (zInChunk(z, chunk)) {
                        for (int oy = 0; oy < dy; oy++) {
                            int y = tl.getY() + oy;
//                            if (y >= yCoord-dy/2 && y < yCoord+dy/2) {    @todo!!!
                                if (formula.isInside(x, y, z)) {
                                    placeBlockIfPossible(worldObj, blocks, maxSize, x, y, z, formula.getLastState(), forquarry);
                                }
//                            }
                        }
                    }
                }
            }
        }
    }

    @Override
    public String getUnlocalizedName(ItemStack itemStack) {
        if (itemStack.getItemDamage() == 0) {
            return super.getUnlocalizedName(itemStack);
        } else {
            return super.getUnlocalizedName(itemStack) + itemStack.getItemDamage();
        }
    }

    @Override
    protected void clGetSubItems(Item itemIn, CreativeTabs tab, List<ItemStack> subItems) {
        for (int i = 0 ; i <= 10 ; i++) {
            subItems.add(new ItemStack(this, 1, i));
        }
    }

    public static void save(EntityPlayer player, ItemStack card, String filename) {
        Shape shape = ShapeCardItem.getShape(card);
        boolean solid = ShapeCardItem.isSolid(card);
        BlockPos offset = ShapeCardItem.getOffset(card);
        BlockPos dimension = ShapeCardItem.getDimension(card);

        RLE positions = new RLE();
        StatePalette statePalette = new StatePalette();
        int cnt = getDataPositions(card, shape, solid, positions, statePalette);

        byte[] data = positions.getData();

        File file = new File(filename);
        FileOutputStream stream;
        try {
            stream = new FileOutputStream(file);
        } catch (FileNotFoundException e) {
            ChatTools.addChatMessage(player, new TextComponentString(TextFormatting.RED + "Cannot write to file '" + filename + "'!"));
            return;
        }
        PrintWriter writer = new PrintWriter(stream);
        writer.println("SHAPE");
        writer.println("DIM:" + dimension.getX() + "," + dimension.getY() + "," + dimension.getZ());
        writer.println("OFF:" + offset.getX() + "," + offset.getY() + "," + offset.getZ());
        for (IBlockState state : statePalette.getPalette()) {
            String r = state.getBlock().getRegistryName().toString();
            writer.println(r + "@" + state.getBlock().getMetaFromState(state));
        }
        writer.println("DATA");

        byte[] encoded = Base64.getEncoder().encode(data);
        writer.write(new String(encoded));
        writer.close();
    }

    public static void load(EntityPlayer player, ItemStack card, String filename) {
        Shape shape = ShapeCardItem.getShape(card);

        if (shape != Shape.SHAPE_SCAN) {
            ChatTools.addChatMessage(player, new TextComponentString(TextFormatting.RED + "To load a file into this card you need a linked 'scan' type card!"));
            return;
        }

        NBTTagCompound compound = ShapeCardItem.getCompound(card);
        GlobalCoordinate scannerPos = ShapeCardItem.getData(compound);
        if (scannerPos == null) {
            ChatTools.addChatMessage(player, new TextComponentString(TextFormatting.RED + "This card is not linked to a scanner!"));
            return;
        }

        World world = DimensionManager.getWorld(scannerPos.getDimension());
        if (world == null || !world.isBlockLoaded(scannerPos.getCoordinate())) {
            ChatTools.addChatMessage(player, new TextComponentString(TextFormatting.RED + "The scanner is out of reach or not chunkloaded!"));
            return;
        }

        TileEntity te = world.getTileEntity(scannerPos.getCoordinate());
        if (!(te instanceof ScannerTileEntity)) {
            ChatTools.addChatMessage(player, new TextComponentString(TextFormatting.RED + "Not a valid scanner!"));
            return;
        }

        ScannerTileEntity scanner = (ScannerTileEntity) te;

        File file = new File(filename);
        FileInputStream stream;

        try {
            stream = new FileInputStream(file);
            BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
            String s = reader.readLine();
            if (!"SHAPE".equals(s)) {
                ChatTools.addChatMessage(player, new TextComponentString(TextFormatting.RED + "This does not appear to be a valid shapecard file!"));
                return;
            }
            s = reader.readLine();
            if (!s.startsWith("DIM:")) {
                ChatTools.addChatMessage(player, new TextComponentString(TextFormatting.RED + "This does not appear to be a valid shapecard file!"));
                return;
            }
            BlockPos dim = parse(s.substring(4));
            s = reader.readLine();
            if (!s.startsWith("OFF:")) {
                ChatTools.addChatMessage(player, new TextComponentString(TextFormatting.RED + "This does not appear to be a valid shapecard file!"));
                return;
            }
            BlockPos off = parse(s.substring(4));
            s = reader.readLine();
            StatePalette statePalette = new StatePalette();
            while (!"DATA".equals(s)) {
                String[] split = StringUtils.split(s, '@');
                Block block = ForgeRegistries.BLOCKS.getValue(new ResourceLocation(split[0]));
                int meta = Integer.parseInt(split[1]);
                if (block == null) {
                    ChatTools.addChatMessage(player, new TextComponentString(TextFormatting.YELLOW + "Could not find block '" + split[0] + "'!"));
                    block = Blocks.STONE;
                    meta = 0;
                }
                statePalette.add(block.getStateFromMeta(meta));
                s = reader.readLine();
            }
            s = reader.readLine();
            byte[] decoded = Base64.getDecoder().decode(s.getBytes());
            for (byte b : decoded) {
                System.out.println("b = " + b);
            }

            scanner.setDataFromFile(card, dim, off, decoded, statePalette);
        } catch (FileNotFoundException e) {
            ChatTools.addChatMessage(player, new TextComponentString(TextFormatting.RED + "Cannot read from file '" + filename + "'!"));
            return;
        } catch (IOException e) {
            ChatTools.addChatMessage(player, new TextComponentString(TextFormatting.RED + "Cannot read from file '" + filename + "'!"));
            return;
        } catch (NullPointerException e) {
            ChatTools.addChatMessage(player, new TextComponentString(TextFormatting.RED + "File '" + filename + "' is too short!"));
            return;
        } catch (ArrayIndexOutOfBoundsException e) {
            ChatTools.addChatMessage(player, new TextComponentString(TextFormatting.RED + "File '" + filename + "' contains invalid entries!"));
            return;
        }

    }

    private static BlockPos parse(String s) {
        String[] split = StringUtils.split(s, ',');
        return new BlockPos(Integer.parseInt(split[0]), Integer.parseInt(split[1]), Integer.parseInt(split[2]));
    }
}