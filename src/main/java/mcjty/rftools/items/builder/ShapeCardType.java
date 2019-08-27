package mcjty.rftools.items.builder;

import mcjty.lib.container.InventoryHelper;
import mcjty.rftools.RFTools;
import mcjty.rftools.blocks.builder.BuilderConfiguration;
import mcjty.rftools.blocks.builder.BuilderTileEntity;
import mcjty.rftools.shapes.Shape;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.renderer.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public enum ShapeCardType {
    CARD_UNKNOWN(-2),          // Not known yet
    CARD_SPACE(-1),            // Not a shape card but a space card instead

    CARD_SHAPE(0, "def", false, false, false, BuilderTileEntity::buildBlock, null,
            () -> BuilderConfiguration.builderRfPerOperation.get(),
            "This item can be configured as a shape. You",
            "can then use it in the shield projector to make",
            "a shield of that shape or in the builder to",
            "actually build the shape") {
        @Override
        public void addHudLog(List<String> list, InventoryHelper inventoryHelper) {
            list.add("    Shape card");
            ItemStack shapeCard = inventoryHelper.getStackInSlot(BuilderTileEntity.SLOT_TAB);
            if (!shapeCard.isEmpty()) {
                Shape shape = ShapeCardItem.getShape(shapeCard);
                if (shape != null) {
                    list.add("    " + shape.getDescription());
                }
            }
        }
    },

    CARD_VOID(1, "void", false, false, false, BuilderTileEntity::voidBlock, "    Void mode",
            () -> (BuilderConfiguration.builderRfPerQuarry.get() * (int) (double) BuilderConfiguration.voidShapeCardFactor.get()),
            "This item will cause the builder to void",
            "all blocks in the configured space."),

    CARD_QUARRY(2, "quarry", true, false, false, BuilderTileEntity::quarryBlock, "    Normal quarry",
            () -> BuilderConfiguration.builderRfPerQuarry.get(),
            "This item will cause the builder to quarry",
            "all blocks in the configured space and replace",
            "them with " + getDirtOrCobbleName() + "."),

    CARD_QUARRY_SILK(3, "quarry_silk", true, false, false, BuilderTileEntity::silkQuarryBlock, "    Silktouch quarry",
            () -> (int) (BuilderConfiguration.builderRfPerQuarry.get() * BuilderConfiguration.silkquarryShapeCardFactor.get()),
            "This item will cause the builder to quarry",
            "all blocks in the configured space and replace",
            "them with " + getDirtOrCobbleName() + ".",
            "Blocks are harvested with silk touch"),

    CARD_QUARRY_FORTUNE(4, "quarry_fortune", true, false, true, BuilderTileEntity::quarryBlock, "    Fortune quarry",
            () -> (int) (BuilderConfiguration.builderRfPerQuarry.get() * BuilderConfiguration.fortunequarryShapeCardFactor.get()),
            "This item will cause the builder to quarry",
            "all blocks in the configured space and replace",
            "them with " + getDirtOrCobbleName() + ".",
            "Blocks are harvested with fortune"),

    CARD_QUARRY_CLEAR(5, "quarry_clear", true, true, false, BuilderTileEntity::quarryBlock, "    Normal quarry",
            () -> BuilderConfiguration.builderRfPerQuarry.get(),
            "This item will cause the builder to quarry",
            "all blocks in the configured space"),

    CARD_QUARRY_CLEAR_SILK(6, "quarry_clear_silk", true, true, false, BuilderTileEntity::silkQuarryBlock, "    Silktouch quarry",
            () -> (int) (BuilderConfiguration.builderRfPerQuarry.get() * BuilderConfiguration.silkquarryShapeCardFactor.get()),
            "This item will cause the builder to quarry",
            "all blocks in the configured space.",
            "Blocks are harvested with silk touch"),

    CARD_QUARRY_CLEAR_FORTUNE(7, "quarry_clear_fortune", true, true, true, BuilderTileEntity::quarryBlock, "    Fortune quarry",
            () -> (int) (BuilderConfiguration.builderRfPerQuarry.get() * BuilderConfiguration.fortunequarryShapeCardFactor.get()),
            "This item will cause the builder to quarry",
            "all blocks in the configured space.",
            "Blocks are harvested with fortune"),

    CARD_PUMP(8, "pump", false, false, false, BuilderTileEntity::pumpBlock, "    Pump",
            () -> BuilderConfiguration.builderRfPerLiquid.get(),
            "This item will cause the builder to collect",
            "all liquids in the configured space.",
            "The liquid will be replaced with " + getDirtOrCobbleName() + "."),

    CARD_PUMP_CLEAR(9, "pump_clear", false, true, false, BuilderTileEntity::pumpBlock, "    Pump",
            () -> BuilderConfiguration.builderRfPerLiquid.get(),
            "This item will cause the builder to collect",
            "all liquids in the configured space.",
            "The liquid will be removed from the world"),

    CARD_PUMP_LIQUID(10, "liquid", false, false, false, BuilderTileEntity::placeLiquidBlock, "    Place liquids",
            () -> BuilderConfiguration.builderRfPerLiquid.get(),
            "This item will cause the builder to place",
            "liquids from an tank on top/bottom into the world.");

    private static String getDirtOrCobbleName() {
        // @todo 1.14, re-evaluate
        BlockState state = BuilderConfiguration.getQuarryReplace();
        Block block = state.getBlock();
        Item item = Item.getItemFromBlock(block);
        if(item == Items.AIR) {
            return block.getTranslationKey();
        } else {
            return new ItemStack(item, 1).getDisplayName().getFormattedText(); // TODO see if this can be made less fragile
        }
    }

    private final int damage;
    private final Supplier<Integer> rfNeeded;
    private final ModelResourceLocation modelResourceLocation;
    private final ShapeCardType.SingleBlockHandler singleBlockHandler;
    private final String hudLogEntry;
    private final boolean quarry, clearing, fortune;
    private final List<ITextComponent> information;

    private static final Map<Integer, ShapeCardType> SHAPE_TYPE_MAP;

    static {
        SHAPE_TYPE_MAP = new HashMap<>(ShapeCardType.values().length);
        for (ShapeCardType type : ShapeCardType.values()) {
            SHAPE_TYPE_MAP.put(type.damage, type);
        }
    }

    public static ShapeCardType fromDamage(int damage) {
        return SHAPE_TYPE_MAP.get(damage);
    }

    private ShapeCardType(int damage) {
        this(damage, null, false, false, false, BuilderTileEntity::suspend, null, () -> 0);
    }

    private ShapeCardType(int damage, String resourceSuffix, boolean quarry, boolean clearing, boolean fortune, ShapeCardType.SingleBlockHandler singleBlockHandler, String hudLogEntry, Supplier<Integer> rfNeeded, String... information) {
        this.damage = damage;
        this.modelResourceLocation = resourceSuffix == null ? null : new ModelResourceLocation(RFTools.MODID + ":shape_card_" + resourceSuffix, "inventory");
        this.quarry = quarry;
        this.clearing = clearing;
        this.fortune = fortune;
        this.rfNeeded = rfNeeded;
        this.singleBlockHandler = singleBlockHandler;
        this.hudLogEntry = hudLogEntry;
        this.information = Arrays.stream(information).map(a -> new StringTextComponent(TextFormatting.WHITE.toString() + a)).collect(Collectors.toList());
    }

    public int getDamage() {
        return damage;
    }

    public boolean isQuarry() {
        return quarry;
    }

    public boolean isClearing() {
        return clearing;
    }

    public boolean isFortune() {
        return fortune;
    }

    public int getRfNeeded() {
        return rfNeeded.get();
    }

    public ModelResourceLocation getModelResourceLocation() {
        return modelResourceLocation;
    }

    public void addHudLog(List<String> list, InventoryHelper inventoryHelper) {
        if(hudLogEntry != null) {
            list.add(hudLogEntry);
        }
        if(isClearing()) {
            list.add("    (clearing)");
        }
    }

    public void addInformation(List<ITextComponent> list) {
        list.addAll(information);
        list.add(new StringTextComponent(TextFormatting.GREEN + "Max area: " + BuilderConfiguration.maxBuilderDimension.get() + "x" + Math.min(256, BuilderConfiguration.maxBuilderDimension.get()) + "x" + BuilderConfiguration.maxBuilderDimension.get()));
        list.add(new StringTextComponent(TextFormatting.GREEN + "Base cost: " + rfNeeded.get() + " RF/t per block"));
        list.add(new StringTextComponent(TextFormatting.GREEN + (this == CARD_SHAPE ? "(final cost depends on infusion level)" : "(final cost depends on infusion level and block hardness)")));
    }

    public boolean handleSingleBlock(BuilderTileEntity te, int rfNeeded, BlockPos srcPos, BlockState srcState, BlockState pickState) {
        return singleBlockHandler.handleSingleBlock(te, rfNeeded, srcPos, srcState, pickState);
    }

    @FunctionalInterface
    public interface SingleBlockHandler {
        public boolean handleSingleBlock(BuilderTileEntity te, int rfNeeded, BlockPos srcPos, BlockState srcState, BlockState pickState);
    }
}
