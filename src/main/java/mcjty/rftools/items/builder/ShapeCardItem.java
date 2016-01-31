package mcjty.rftools.items.builder;

import mcjty.lib.varia.GlobalCoordinate;
import mcjty.lib.varia.Logging;
import mcjty.rftools.RFTools;
import mcjty.rftools.blocks.builder.BuilderConfiguration;
import mcjty.rftools.blocks.builder.BuilderTileEntity;
import mcjty.rftools.items.GenericRFToolsItem;
import net.minecraft.block.Block;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.ChunkCoordIntPair;
import net.minecraft.world.World;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.oredict.OreDictionary;
import org.lwjgl.input.Keyboard;

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

    public static final int MAXIMUM_COUNT = 50000000;
    
    public enum Shape {
        SHAPE_BOX(0, "Box"),
        SHAPE_TOPDOME(1, "Top Dome"),
        SHAPE_BOTTOMDOME(2, "Bottom Dome"),
        SHAPE_SPHERE(3, "Sphere"),
        SHAPE_CYLINDER(4, "Cylinder"),
        SHAPE_CAPPEDCYLINDER(5, "Capped Cylinder"),
        SHAPE_PRISM(6, "Prism"),
        SHAPE_TORUS(7, "Torus"),
        SHAPE_SOLIDBOX(100, "Solid Box"),
        SHAPE_SOLIDSPHERE(103, "Solid Sphere"),
        SHAPE_SOLIDCYLINDER(104, "Solid Cylinder"),
        SHAPE_SOLIDTORUS(107, "Solid Torus");


        private final int index;
        private final String description;

        private static Map<Integer,Shape> shapes;
        private static Map<String,Shape> shapesByDescription;

        static {
            shapesByDescription = new HashMap<>();
            shapes = new HashMap<>();
            for (Shape shape : values()) {
                shapes.put(shape.getIndex(), shape);
                shapesByDescription.put(shape.getDescription(), shape);
            }
        }

        // Return the hollow version of the shape.
        public Shape makeHollow() {
            switch (this) {
                case SHAPE_SOLIDBOX:
                    return SHAPE_BOX;
                case SHAPE_SOLIDSPHERE:
                    return SHAPE_SPHERE;
                case SHAPE_SOLIDCYLINDER:
                    return SHAPE_CAPPEDCYLINDER;
                case SHAPE_SOLIDTORUS:
                    return SHAPE_TORUS;
            }
            return this;
        }

        Shape(int index, String description) {
            this.index = index;
            this.description = description;
        }

        public int getIndex() {
            return index;
        }

        public String getDescription() {
            return description;
        }

        public static Shape getShape(int index) {
            return shapes.get(index);
        }

        public static Shape getShape(String description) {
            return shapesByDescription.get(description);
        }
    }

    public static final int MODE_NONE = 0;
    public static final int MODE_CORNER1 = 1;
    public static final int MODE_CORNER2 = 2;

    public ShapeCardItem() {
        super("shape_card");
        setMaxStackSize(1);
        setHasSubtypes(true);
        setMaxDamage(0);
    }

//    @Override
//    public void registerIcons(IIconRegister iconRegister) {
//        icons[CARD_SHAPE] = iconRegister.registerIcon(RFTools.MODID + ":shapeCardItem");
//        icons[CARD_VOID] = iconRegister.registerIcon(RFTools.MODID + ":shapeCardVoidItem");
//        icons[CARD_QUARRY] = iconRegister.registerIcon(RFTools.MODID + ":shapeCardQuarryItem");
//        icons[CARD_QUARRY_SILK] = iconRegister.registerIcon(RFTools.MODID + ":shapeCardSilkItem");
//        icons[CARD_QUARRY_FORTUNE] = iconRegister.registerIcon(RFTools.MODID + ":shapeCardFortuneItem");
//        icons[CARD_QUARRY_CLEAR] = iconRegister.registerIcon(RFTools.MODID + ":shapeCardCQuarryItem");
//        icons[CARD_QUARRY_CLEAR_SILK] = iconRegister.registerIcon(RFTools.MODID + ":shapeCardCSilkItem");
//        icons[CARD_QUARRY_CLEAR_FORTUNE] = iconRegister.registerIcon(RFTools.MODID + ":shapeCardCFortuneItem");
//    }


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
    }

    @Override
    public int getMaxItemUseDuration(ItemStack stack) {
        return 1;
    }

    @Override
    public boolean onItemUse(ItemStack stack, EntityPlayer player, World world, BlockPos pos, EnumFacing side, float hitX, float hitY, float hitZ) {
        if (!world.isRemote) {
            int mode = getMode(stack);
            if (mode == MODE_NONE) {
                if (player.isSneaking()) {
                    if (world.getTileEntity(pos) instanceof BuilderTileEntity) {
                        setCurrentBlock(stack, new GlobalCoordinate(pos, world.provider.getDimensionId()));
                        Logging.message(player, EnumChatFormatting.GREEN + "Now select the first corner");
                        setMode(stack, MODE_CORNER1);
                        setCorner1(stack, null);
                    } else {
                        Logging.message(player, EnumChatFormatting.RED + "You can only do this on a builder!");
                    }
                } else {
                    return false;
                }
            } else if (mode == MODE_CORNER1) {
                GlobalCoordinate currentBlock = getCurrentBlock(stack);
                if (currentBlock.getDimension() != world.provider.getDimensionId()) {
                    Logging.message(player, EnumChatFormatting.RED + "The Builder is in another dimension!");
                } else if (currentBlock.getCoordinate().equals(pos)) {
                    Logging.message(player, EnumChatFormatting.RED + "Cleared area selection mode!");
                    setMode(stack, MODE_NONE);
                } else {
                    Logging.message(player, EnumChatFormatting.GREEN + "Now select the second corner");
                    setMode(stack, MODE_CORNER2);
                    setCorner1(stack, pos);
                }
            } else {
                GlobalCoordinate currentBlock = getCurrentBlock(stack);
                if (currentBlock.getDimension() != world.provider.getDimensionId()) {
                    Logging.message(player, EnumChatFormatting.RED + "The Builder is in another dimension!");
                } else if (currentBlock.getCoordinate().equals(pos)) {
                    Logging.message(player, EnumChatFormatting.RED + "Cleared area selection mode!");
                    setMode(stack, MODE_NONE);
                } else {
                    NBTTagCompound tag = stack.getTagCompound();
                    if (tag == null) {
                        tag = new NBTTagCompound();
                        stack.setTagCompound(tag);
                    }
                    BlockPos c1 = getCorner1(stack);
                    if (c1 == null) {
                        Logging.message(player, EnumChatFormatting.RED + "Cleared area selection mode!");
                        setMode(stack, MODE_NONE);
                    } else {
                        Logging.message(player, EnumChatFormatting.GREEN + "New settings copied to the shape card!");
                        System.out.println("currentBlock = " + currentBlock.getCoordinate());
                        System.out.println("corner1 = " + c1);
                        System.out.println("corner2 = " + pos);
//                        BlockPos center = new BlockPos((int) ((c1.getX() + x) / 2.0f + .55f), (int) ((c1.getY() + y) / 2.0f + .55f), (int) ((c1.getZ() + z) / 2.0f + .55f));
                        BlockPos center = new BlockPos((int) Math.ceil((c1.getX() + pos.getX()) / 2.0f), (int) Math.ceil((c1.getY() + pos.getY()) / 2.0f), (int) Math.ceil((c1.getZ() + pos.getZ()) / 2.0f));
                        System.out.println("center = " + center);
                        tag.setInteger("dimX", Math.abs(c1.getX() - pos.getX()) + 1);
                        tag.setInteger("dimY", Math.abs(c1.getY() - pos.getY()) + 1);
                        tag.setInteger("dimZ", Math.abs(c1.getZ() - pos.getZ()) + 1);
                        tag.setInteger("offsetX", center.getX() - currentBlock.getCoordinate().getX());
                        tag.setInteger("offsetY", center.getY() - currentBlock.getCoordinate().getY());
                        tag.setInteger("offsetZ", center.getZ() - currentBlock.getCoordinate().getZ());

                        setMode(stack, MODE_NONE);
                        setCorner1(stack, null);
                    }
                }
            }
        }
        return true;
    }

    public static void setCorner1(ItemStack itemStack, BlockPos corner) {
        NBTTagCompound tagCompound = itemStack.getTagCompound();
        if (tagCompound == null) {
            tagCompound = new NBTTagCompound();
            itemStack.setTagCompound(tagCompound);
        }
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
        NBTTagCompound tagCompound = itemStack.getTagCompound();
        if (tagCompound == null) {
            tagCompound = new NBTTagCompound();
            itemStack.setTagCompound(tagCompound);
        }
        tagCompound.setInteger("mode", mode);
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


    @SideOnly(Side.CLIENT)
    @Override
    public void addInformation(ItemStack itemStack, EntityPlayer player, List<String> list, boolean whatIsThis) {
        super.addInformation(itemStack, player, list, whatIsThis);

        int type = itemStack.getItemDamage();
        if (!BuilderConfiguration.shapeCardAllowed) {
            list.add(EnumChatFormatting.RED + "Disabled in config!");
        } else if (type != CARD_SHAPE) {
            if (!BuilderConfiguration.quarryAllowed) {
                list.add(EnumChatFormatting.RED + "Disabled in config!");
            } else if (isClearingQuarry(type)) {
                if (!BuilderConfiguration.clearingQuarryAllowed) {
                    list.add(EnumChatFormatting.RED + "Disabled in config!");
                }
            }
        }

        Shape shape = getShape(itemStack);
        list.add(EnumChatFormatting.GREEN + "Shape " + shape.getDescription());
        list.add(EnumChatFormatting.GREEN + "Dimension " + getDimension(itemStack));
        list.add(EnumChatFormatting.GREEN + "Offset " + getOffset(itemStack));

        if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT)) {
            list.add(EnumChatFormatting.YELLOW + "Sneak right click on builder to start mark mode");
            list.add(EnumChatFormatting.YELLOW + "Then right click to mark two corners of wanted area");
            switch (type) {
                case CARD_VOID:
                    list.add(EnumChatFormatting.WHITE + "This item will cause the builder to void");
                    list.add(EnumChatFormatting.WHITE + "all blocks in the configured space.");
                    list.add(EnumChatFormatting.GREEN + "Max area: " + BuilderConfiguration.maxBuilderDimension + "x" + Math.min(256, BuilderConfiguration.maxBuilderDimension) + "x" + BuilderConfiguration.maxBuilderDimension);
                    list.add(EnumChatFormatting.GREEN + "Base cost: " + (int)(BuilderConfiguration.builderRfPerQuarry * BuilderConfiguration.voidShapeCardFactor) + " RF/t per block");
                    list.add(EnumChatFormatting.GREEN + "(final cost depends on infusion level and block hardness)");
                    break;
                case CARD_SHAPE:
                    list.add(EnumChatFormatting.WHITE + "This item can be configured as a shape. You");
                    list.add(EnumChatFormatting.WHITE + "can then use it in the shield projector to make");
                    list.add(EnumChatFormatting.WHITE + "a shield of that shape or in the builder to");
                    list.add(EnumChatFormatting.WHITE + "actually build the shape");
                    list.add(EnumChatFormatting.GREEN + "Max area: " + BuilderConfiguration.maxBuilderDimension + "x" + Math.min(256, BuilderConfiguration.maxBuilderDimension) + "x" + BuilderConfiguration.maxBuilderDimension);
                    list.add(EnumChatFormatting.GREEN + "Base cost: " + BuilderConfiguration.builderRfPerOperation + " RF/t per block");
                    list.add(EnumChatFormatting.GREEN + "(final cost depends on infusion level)");
                    break;
                case CARD_QUARRY_SILK:
                    list.add(EnumChatFormatting.WHITE + "This item will cause the builder to quarry");
                    list.add(EnumChatFormatting.WHITE + "all blocks in the configured space and replace");
                    list.add(EnumChatFormatting.WHITE + "them with dirt.");
                    list.add(EnumChatFormatting.WHITE + "Blocks are harvested with silk touch");
                    list.add(EnumChatFormatting.GREEN + "Max area: " + BuilderConfiguration.maxBuilderDimension + "x" + Math.min(256, BuilderConfiguration.maxBuilderDimension) + "x" + BuilderConfiguration.maxBuilderDimension);
                    list.add(EnumChatFormatting.GREEN + "Base cost: " + (int)(BuilderConfiguration.builderRfPerQuarry * BuilderConfiguration.silkquarryShapeCardFactor) + " RF/t per block");
                    list.add(EnumChatFormatting.GREEN + "(final cost depends on infusion level and block hardness)");
                    break;
                case CARD_QUARRY_CLEAR_SILK:
                    list.add(EnumChatFormatting.WHITE + "This item will cause the builder to quarry");
                    list.add(EnumChatFormatting.WHITE + "all blocks in the configured space.");
                    list.add(EnumChatFormatting.WHITE + "Blocks are harvested with silk touch");
                    list.add(EnumChatFormatting.GREEN + "Max area: " + BuilderConfiguration.maxBuilderDimension + "x" + Math.min(256, BuilderConfiguration.maxBuilderDimension) + "x" + BuilderConfiguration.maxBuilderDimension);
                    list.add(EnumChatFormatting.GREEN + "Base cost: " + (int)(BuilderConfiguration.builderRfPerQuarry * BuilderConfiguration.silkquarryShapeCardFactor) + " RF/t per block");
                    list.add(EnumChatFormatting.GREEN + "(final cost depends on infusion level and block hardness)");
                    break;
                case CARD_QUARRY_FORTUNE:
                    list.add(EnumChatFormatting.WHITE + "This item will cause the builder to quarry");
                    list.add(EnumChatFormatting.WHITE + "all blocks in the configured space and replace");
                    list.add(EnumChatFormatting.WHITE + "them with dirt.");
                    list.add(EnumChatFormatting.WHITE + "Blocks are harvested with fortune");
                    list.add(EnumChatFormatting.GREEN + "Max area: " + BuilderConfiguration.maxBuilderDimension + "x" + Math.min(256, BuilderConfiguration.maxBuilderDimension) + "x" + BuilderConfiguration.maxBuilderDimension);
                    list.add(EnumChatFormatting.GREEN + "Base cost: " + (int)(BuilderConfiguration.builderRfPerQuarry * BuilderConfiguration.fortunequarryShapeCardFactor) + " RF/t per block");
                    list.add(EnumChatFormatting.GREEN + "(final cost depends on infusion level and block hardness)");
                    break;
                case CARD_QUARRY_CLEAR_FORTUNE:
                    list.add(EnumChatFormatting.WHITE + "This item will cause the builder to quarry");
                    list.add(EnumChatFormatting.WHITE + "all blocks in the configured space.");
                    list.add(EnumChatFormatting.WHITE + "Blocks are harvested with fortune");
                    list.add(EnumChatFormatting.GREEN + "Max area: " + BuilderConfiguration.maxBuilderDimension + "x" + Math.min(256, BuilderConfiguration.maxBuilderDimension) + "x" + BuilderConfiguration.maxBuilderDimension);
                    list.add(EnumChatFormatting.GREEN + "Base cost: " + (int)(BuilderConfiguration.builderRfPerQuarry * BuilderConfiguration.fortunequarryShapeCardFactor) + " RF/t per block");
                    list.add(EnumChatFormatting.GREEN + "(final cost depends on infusion level and block hardness)");
                    break;
                case CARD_QUARRY:
                    list.add(EnumChatFormatting.WHITE + "This item will cause the builder to quarry");
                    list.add(EnumChatFormatting.WHITE + "all blocks in the configured space and replace");
                    list.add(EnumChatFormatting.WHITE + "them with dirt.");
                    list.add(EnumChatFormatting.GREEN + "Max area: " + BuilderConfiguration.maxBuilderDimension + "x" + Math.min(256, BuilderConfiguration.maxBuilderDimension) + "x" + BuilderConfiguration.maxBuilderDimension);
                    list.add(EnumChatFormatting.GREEN + "Base cost: " + BuilderConfiguration.builderRfPerQuarry + " RF/t per block");
                    list.add(EnumChatFormatting.GREEN + "(final cost depends on infusion level and block hardness)");
                    break;
                case CARD_QUARRY_CLEAR:
                    list.add(EnumChatFormatting.WHITE + "This item will cause the builder to quarry");
                    list.add(EnumChatFormatting.WHITE + "all blocks in the configured space");
                    list.add(EnumChatFormatting.GREEN + "Max area: " + BuilderConfiguration.maxBuilderDimension + "x" + Math.min(256, BuilderConfiguration.maxBuilderDimension) + "x" + BuilderConfiguration.maxBuilderDimension);
                    list.add(EnumChatFormatting.GREEN + "Base cost: " + BuilderConfiguration.builderRfPerQuarry + " RF/t per block");
                    list.add(EnumChatFormatting.GREEN + "(final cost depends on infusion level and block hardness)");
                    break;
            }
        } else {
            list.add(EnumChatFormatting.WHITE + RFTools.SHIFT_MESSAGE);
        }
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
        if (oredict) {
            int[] iDs = OreDictionary.getOreIDs(new ItemStack(block));
            for (int id : iDs) {
                String oreName = OreDictionary.getOreName(id);
                List<ItemStack> ores = OreDictionary.getOres(oreName);
                for (ItemStack ore : ores) {
                    if (ore.getItem() instanceof ItemBlock) {
                        blocks.add(((ItemBlock)ore.getItem()).getBlock());
                    }
                }
            }
        } else {
            blocks.add(block);
        }
    }

    public static Set<Block> getVoidedBlocks(ItemStack stack) {
        Set<Block> blocks = new HashSet<Block>();
        boolean oredict = isOreDictionary(stack);
        if (isVoiding(stack, "stone")) {
            addBlocks(blocks, Blocks.stone, oredict);
        }
        if (isVoiding(stack, "cobble")) {
            addBlocks(blocks, Blocks.cobblestone, oredict);
        }
        if (isVoiding(stack, "dirt")) {
            addBlocks(blocks, Blocks.dirt, oredict);
        }
        if (isVoiding(stack, "sand")) {
            addBlocks(blocks, Blocks.sand, oredict);
        }
        if (isVoiding(stack, "gravel")) {
            addBlocks(blocks, Blocks.gravel, oredict);
        }
        if (isVoiding(stack, "netherrack")) {
            addBlocks(blocks, Blocks.netherrack, oredict);
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
        if (tagCompound == null) {
            return Shape.SHAPE_BOX;
        }
        int shape = tagCompound.getInteger("shape");
        Shape s = Shape.getShape(shape);
        if (s == null) {
            return Shape.SHAPE_BOX;
        }
        return s;
    }

    public static void setShape(ItemStack stack, Shape shape) {
        NBTTagCompound tagCompound = stack.getTagCompound();
        if (tagCompound == null) {
            tagCompound = new NBTTagCompound();
            stack.setTagCompound(tagCompound);
        }
        tagCompound.setInteger("shape", shape.getIndex());
    }

    public static BlockPos getDimension(ItemStack stack) {
        NBTTagCompound tagCompound = stack.getTagCompound();
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
    public ItemStack onItemRightClick(ItemStack stack, World world, EntityPlayer player) {
        if (world.isRemote) {
            player.openGui(RFTools.instance, RFTools.GUI_SHAPECARD, player.worldObj, (int) player.posX, (int) player.posY, (int) player.posZ);
            return stack;
        }
        return stack;
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

    public static int countBlocks(Shape shape, BlockPos dimension) {
        final int[] cnt = {0};
        BlockPos offset = new BlockPos(0, 128, 0);
        BlockPos clamped = new BlockPos(Math.min(dimension.getX(), 512), Math.min(dimension.getY(), 256), Math.min(dimension.getZ(), 512));
        composeShape(shape, null, new BlockPos(0, 0, 0), clamped, offset, new AbstractCollection<BlockPos>() {
            @Override
            public Iterator<BlockPos> iterator() {
                return null;
            }

            @Override
            public boolean add(BlockPos coordinate) {
                cnt[0]++;
                return true;
            }

            @Override
            public int size() {
                return 0;
            }
        }, MAXIMUM_COUNT+1, false, null);
        return cnt[0];
    }

    public static boolean xInChunk(int x, ChunkCoordIntPair chunk) {
        if (chunk == null) {
            return true;
        } else {
            return chunk.chunkXPos == (x>>4);
        }
    }

    public static boolean zInChunk(int z, ChunkCoordIntPair chunk) {
        if (chunk == null) {
            return true;
        } else {
            return chunk.chunkZPos == (z>>4);
        }
    }

    public static void composeShape(Shape shape, World worldObj, BlockPos thisCoord, BlockPos dimension, BlockPos offset, Collection<BlockPos> blocks, int maxSize, boolean forquarry,
                                    ChunkCoordIntPair chunk) {
        switch (shape) {
            case SHAPE_BOX:
                composeBox(worldObj, thisCoord, dimension, offset, blocks, maxSize, false, forquarry, chunk);
                break;
            case SHAPE_SOLIDBOX:
                composeBox(worldObj, thisCoord, dimension, offset, blocks, maxSize, true, forquarry, chunk);
                break;
            case SHAPE_TOPDOME:
                composeSphere(worldObj, thisCoord, dimension, offset, blocks, maxSize, 1, false, forquarry, chunk);
                break;
            case SHAPE_BOTTOMDOME:
                composeSphere(worldObj, thisCoord, dimension, offset, blocks, maxSize, -1, false, forquarry, chunk);
                break;
            case SHAPE_SPHERE:
                composeSphere(worldObj, thisCoord, dimension, offset, blocks, maxSize, 0, false, forquarry, chunk);
                break;
            case SHAPE_SOLIDSPHERE:
                composeSphere(worldObj, thisCoord, dimension, offset, blocks, maxSize, 0, true, forquarry, chunk);
                break;
            case SHAPE_CYLINDER:
                composeCylinder(worldObj, thisCoord, dimension, offset, blocks, maxSize, false, false, forquarry, chunk);
                break;
            case SHAPE_SOLIDCYLINDER:
                composeCylinder(worldObj, thisCoord, dimension, offset, blocks, maxSize, true, true, forquarry, chunk);
                break;
            case SHAPE_CAPPEDCYLINDER:
                composeCylinder(worldObj, thisCoord, dimension, offset, blocks, maxSize, true, false, forquarry, chunk);
                break;
            case SHAPE_PRISM:
                composePrism(worldObj, thisCoord, dimension, offset, blocks, maxSize, forquarry, chunk);
                break;
            case SHAPE_TORUS:
                composeTorus(worldObj, thisCoord, dimension, offset, blocks, maxSize, false, forquarry, chunk);
                break;
            case SHAPE_SOLIDTORUS:
                composeTorus(worldObj, thisCoord, dimension, offset, blocks, maxSize, true, forquarry, chunk);
                break;
        }
    }

    private static void placeBlockIfPossible(World worldObj, Collection<BlockPos> blocks, int maxSize, int x, int y, int z, boolean forquarry) {
        BlockPos c = new BlockPos(x, y, z);
        if (worldObj == null) {
            blocks.add(c);
            return;
        }
        if (forquarry) {
            if (worldObj.isAirBlock(c)) {
                return;
            }
            blocks.add(c);
        } else {
            //@todo
//            if (BuilderTileEntity.isEmptyOrReplacable(worldObj, x, y, z) && blocks.size() < maxSize - 1) {
//                blocks.add(new BlockPos(x, y, z));
//            }
        }
    }

    private static void composeSphere(World worldObj, BlockPos thisCoord, BlockPos dimension, BlockPos offset, Collection<BlockPos> blocks, int maxSize, int side, boolean solid, boolean forquarry, ChunkCoordIntPair chunk) {
        int xCoord = thisCoord.getX();
        int yCoord = thisCoord.getY();
        int zCoord = thisCoord.getZ();
        int dx = dimension.getX();
        int dy = dimension.getY();
        int dz = dimension.getZ();

        float centerx;
        float centery;
        float centerz;
        centerx = xCoord + offset.getX() + ((dx % 2 != 0) ? 0.0f : -.5f);
        centery = yCoord + offset.getY() + ((dy % 2 != 0) ? 0.0f : -.5f);
        centerz = zCoord + offset.getZ() + ((dz % 2 != 0) ? 0.0f : -.5f);
        BlockPos tl = new BlockPos(xCoord - dx/2 + offset.getX(), yCoord - dy/2 + offset.getY(), zCoord - dz/2 + offset.getZ());

        float dx2;
        float dy2;
        float dz2;
        int davg;

//            float factor = 2.0f;
        float factor = 1.8f;
        dx2 = dx == 0 ? .5f : ((dx + factor) * (dx + factor)) / 4.0f;
        dy2 = dy == 0 ? .5f : ((dy + factor) * (dy + factor)) / 4.0f;
        dz2 = dz == 0 ? .5f : ((dz + factor) * (dz + factor)) / 4.0f;
        davg = (int) ((dx + dy + dz + factor * 3) / 3);

        for (int ox = 0 ; ox < dx ; ox++) {
            int x = tl.getX() + ox;
            if (xInChunk(x, chunk)) {
                for (int oz = 0 ; oz < dz ; oz++) {
                    int z = tl.getZ() + oz;
                    if (zInChunk(z, chunk)) {
                        for (int oy = 0; oy < dy; oy++) {
                            int y = tl.getY() + oy;
                            if (y >= 0 && y < 255) {
                                if (side == 0 || (side == 1 && y >= yCoord + offset.getY()) || (side == -1 && y <= yCoord + offset.getY())) {
                                    if (isInside3D(centerx, centery, centerz, x, y, z, dx2, dy2, dz2, davg) == 1) {
                                        int cnt;
                                        if (solid) {
                                            cnt = 0;
                                        } else {
                                            cnt = isInside3D(centerx, centery, centerz, x - 1, y, z, dx2, dy2, dz2, davg);
                                            cnt += isInside3D(centerx, centery, centerz, x + 1, y, z, dx2, dy2, dz2, davg);
                                            cnt += isInside3D(centerx, centery, centerz, x, y - 1, z, dx2, dy2, dz2, davg);
                                            cnt += isInside3D(centerx, centery, centerz, x, y + 1, z, dx2, dy2, dz2, davg);
                                            cnt += isInside3D(centerx, centery, centerz, x, y, z - 1, dx2, dy2, dz2, davg);
                                            cnt += isInside3D(centerx, centery, centerz, x, y, z + 1, dx2, dy2, dz2, davg);
                                        }
                                        if (cnt != 6) {
                                            placeBlockIfPossible(worldObj, blocks, maxSize, x, y, z, forquarry);
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private static float squaredDistance3D(float cx, float cy, float cz, float x1, float y1, float z1, float dx2, float dy2, float dz2) {
        return (x1-cx) * (x1-cx) / dx2 + (y1-cy) * (y1-cy) / dy2 + (z1-cz) * (z1-cz) / dz2;
    }

    private static float squaredDistance2D(float cx, float cz, float x1, float z1, float dx2, float dz2) {
        return (x1-cx) * (x1-cx) / dx2 + (z1-cz) * (z1-cz) / dz2;
    }

    private static int isInside2D(float centerx, float centerz, int x, int z, float dx2, float dz2, int davg) {
        double distance = Math.sqrt(squaredDistance2D(centerx, centerz, x, z, dx2, dz2));
        return ((int) (distance * (davg / 2 + 1))) <= (davg / 2 - 1) ? 1 : 0;
    }

    private static int isInside3D(float centerx, float centery, float centerz, int x, int y, int z, float dx2, float dy2, float dz2, int davg) {
        double distance = Math.sqrt(squaredDistance3D(centerx, centery, centerz, x, y, z, dx2, dy2, dz2));
        return ((int) (distance * (davg / 2 + 1))) <= (davg / 2 - 1) ? 1 : 0;
    }

    private static void composeCylinder(World worldObj, BlockPos thisCoord, BlockPos dimension, BlockPos offset, Collection<BlockPos> blocks, int maxSize, boolean capped, boolean solid, boolean forquarry, ChunkCoordIntPair chunk) {
        int xCoord = thisCoord.getX();
        int yCoord = thisCoord.getY();
        int zCoord = thisCoord.getZ();
        int dx = dimension.getX();
        int dy = dimension.getY();
        int dz = dimension.getZ();
        float centerx;
        float centerz;

        centerx = xCoord + offset.getX() + ((dx % 2 != 0) ? 0.0f : -.5f);
        centerz = zCoord + offset.getZ() + ((dz % 2 != 0) ? 0.0f : -.5f);

        BlockPos tl = new BlockPos(xCoord - dx/2 + offset.getX(), yCoord - dy/2 + offset.getY(), zCoord - dz/2 + offset.getZ());

        float dx2;
        float dz2;
        int davg;

        float factor = 1.7f;
        dx2 = dx == 0 ? .5f : ((dx + factor) * (dx + factor)) / 4.0f;
        dz2 = dz == 0 ? .5f : ((dz + factor) * (dz + factor)) / 4.0f;
        davg = (int) ((dx + dz + factor * 2) / 2);

        for (int ox = 0 ; ox < dx ; ox++) {
            int x = tl.getX() + ox;
            if (xInChunk(x, chunk)) {
                for (int oz = 0; oz < dz; oz++) {
                    int z = tl.getZ() + oz;
                    if (zInChunk(z, chunk)) {
                        for (int oy = 0; oy < dy; oy++) {
                            int y = tl.getY() + oy;
                            if (y >= 0 && y < 255) {
                                if (isInside2D(centerx, centerz, x, z, dx2, dz2, davg) == 1) {
                                    int cnt;
                                    if (solid) {
                                        cnt = 0;
                                    } else {
                                        cnt = isInside2D(centerx, centerz, x - 1, z, dx2, dz2, davg);
                                        cnt += isInside2D(centerx, centerz, x + 1, z, dx2, dz2, davg);
                                        cnt += isInside2D(centerx, centerz, x, z - 1, dx2, dz2, davg);
                                        cnt += isInside2D(centerx, centerz, x, z + 1, dx2, dz2, davg);
                                    }
                                    if (cnt != 4 || (capped && (oy == 0 || oy == dy - 1))) {
                                        placeBlockIfPossible(worldObj, blocks, maxSize, x, y, z, forquarry);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private static void composeBox(World worldObj, BlockPos thisCoord, BlockPos dimension, BlockPos offset, Collection<BlockPos> blocks, int maxSize, boolean solid, boolean forquarry, ChunkCoordIntPair chunk) {
        int xCoord = thisCoord.getX();
        int yCoord = thisCoord.getY();
        int zCoord = thisCoord.getZ();
        int dx = dimension.getX();
        int dy = dimension.getY();
        int dz = dimension.getZ();
        BlockPos tl = new BlockPos(xCoord - dx/2 + offset.getX(), yCoord - dy/2 + offset.getY(), zCoord - dz/2 + offset.getZ());

        for (int ox = 0 ; ox < dx ; ox++) {
            int x = tl.getX() + ox;
            if (xInChunk(x, chunk)) {
                for (int oz = 0 ; oz < dz ; oz++) {
                    int z = tl.getZ() + oz;
                    if (zInChunk(z, chunk)) {
                        for (int oy = 0; oy < dy; oy++) {
                            int y = tl.getY() + oy;
                            if (y >= 0 && y < 255) {
                                if (solid || ox == 0 || oy == 0 || oz == 0 || ox == (dx - 1) || oy == (dy - 1) || oz == (dz - 1)) {
                                    placeBlockIfPossible(worldObj, blocks, maxSize, x, y, z, forquarry);
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private static void composePrism(World worldObj, BlockPos thisCoord, BlockPos dimension, BlockPos offset, Collection<BlockPos> blocks, int maxSize, boolean forquarry, ChunkCoordIntPair chunk) {
        int xCoord = thisCoord.getX();
        int yCoord = thisCoord.getY();
        int zCoord = thisCoord.getZ();
        int dx = dimension.getX();
        int dy = dimension.getY();
        int dz = dimension.getZ();
        BlockPos tl = new BlockPos(xCoord - dx/2 + offset.getX(), yCoord - dy/2 + offset.getY(), zCoord - dz/2 + offset.getZ());

        for (int oy = 0 ; oy < dy ; oy++) {
            int y = tl.getY() + oy;
            if (y >= 0 && y < 255) {
                int yoffset = oy;
                for (int ox = 0 ; ox < dx ; ox++) {
                    if (ox >= yoffset && ox < dx-yoffset) {
                        int x = tl.getX() + ox;
                        if (xInChunk(x, chunk)) {
                            for (int oz = yoffset; oz < dz - yoffset; oz++) {
                                int z = tl.getZ() + oz;
                                if (zInChunk(z, chunk)) {
                                    if (ox == yoffset || oy == 0 || oz == yoffset || ox == (dx - yoffset - 1) || oz == (dz - yoffset - 1)) {
                                        placeBlockIfPossible(worldObj, blocks, maxSize, x, y, z, forquarry);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private static int isInsideTorus(float centerx, float centery, float centerz, int x, int y, int z, float bigRadius, float smallRadius) {
        double rr = bigRadius - Math.sqrt((x-centerx)*(x-centerx) + (z-centerz)*(z-centerz));
        double f = rr*rr + (y-centery) * (y-centery) - smallRadius * smallRadius;
        if (f < 0) {
            return 1;
        } else {
            return 0;
        }
    }

    private static void composeTorus(World worldObj, BlockPos thisCoord, BlockPos dimension, BlockPos offset, Collection<BlockPos> blocks, int maxSize, boolean solid, boolean forquarry, ChunkCoordIntPair chunk) {
        int xCoord = thisCoord.getX();
        int yCoord = thisCoord.getY();
        int zCoord = thisCoord.getZ();
        int dx = dimension.getX();
        int dy = dimension.getY();
        int dz = dimension.getZ();
        float centerx = xCoord + offset.getX();
        float centery = yCoord + offset.getY();
        float centerz = zCoord + offset.getZ();
        BlockPos tl = new BlockPos(xCoord - dx/2 + offset.getX(), yCoord - dy/2 + offset.getY(), zCoord - dz/2 + offset.getZ());

        float smallRadius = (dy-2)/2.0f;
        float bigRadius = (dx-2)/2.0f - smallRadius;

        for (int ox = 0 ; ox < dx ; ox++) {
            int x = tl.getX() + ox;
            if (xInChunk(x, chunk)) {
                for (int oz = 0 ; oz < dz ; oz++) {
                    int z = tl.getZ() + oz;
                    if (zInChunk(z, chunk)) {
                        for (int oy = 0; oy < dy; oy++) {
                            int y = tl.getY() + oy;
                            if (y >= 0 && y < 255) {
                                if (isInsideTorus(centerx, centery, centerz, x, y, z, bigRadius, smallRadius) == 1) {
                                    int cnt;
                                    if (solid) {
                                        cnt = 0;
                                    } else {
                                        cnt = isInsideTorus(centerx, centery, centerz, x - 1, y, z, bigRadius, smallRadius);
                                        cnt += isInsideTorus(centerx, centery, centerz, x + 1, y, z, bigRadius, smallRadius);
                                        cnt += isInsideTorus(centerx, centery, centerz, x, y, z - 1, bigRadius, smallRadius);
                                        cnt += isInsideTorus(centerx, centery, centerz, x, y, z + 1, bigRadius, smallRadius);
                                        cnt += isInsideTorus(centerx, centery, centerz, x, y - 1, z, bigRadius, smallRadius);
                                        cnt += isInsideTorus(centerx, centery, centerz, x, y + 1, z, bigRadius, smallRadius);
                                    }
                                    if (cnt != 6) {
                                        placeBlockIfPossible(worldObj, blocks, maxSize, x, y, z, forquarry);
                                    }
                                }
                            }
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
    public void getSubItems(Item item, CreativeTabs creativeTabs, List<ItemStack> list) {
        for (int i = 0 ; i <= 7 ; i++) {
            list.add(new ItemStack(this, 1, i));
        }
    }

}