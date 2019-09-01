package mcjty.rftools.items.screenmodules;

import mcjty.lib.McJtyLib;
import mcjty.lib.crafting.INBTPreservingIngredient;
import mcjty.lib.varia.BlockPosTools;
import mcjty.rftools.RFTools;
import mcjty.rftools.api.screens.IModuleGuiBuilder;
import mcjty.rftools.api.screens.IModuleProvider;
import mcjty.rftools.blocks.screens.ScreenConfiguration;
import mcjty.rftools.blocks.screens.modules.StorageControlScreenModule;
import mcjty.rftools.blocks.screens.modulesclient.StorageControlClientScreenModule;
import mcjty.rftools.setup.GuiProxy;
import mcjty.rftools.varia.RFToolsTools;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;

import java.util.Collection;
import java.util.List;

public class StorageControlModuleItem extends Item implements IModuleProvider, INBTPreservingIngredient {

    public StorageControlModuleItem() {
        super(new Item.Properties().maxStackSize(1).defaultMaxDamage(1).group(RFTools.setup.getTab()));
        setRegistryName("storage_control_module");
    }

    @Override
    public void addInformation(ItemStack itemStack, World world, List<ITextComponent> list, ITooltipFlag flag) {
        super.addInformation(itemStack, world, list, flag);
        list.add(new StringTextComponent(TextFormatting.GREEN + "Uses " + ScreenConfiguration.STORAGE_CONTROL_RFPERTICK.get() + " RF/tick"));
        boolean hasTarget = false;
        CompoundNBT tagCompound = itemStack.getTag();
        if (tagCompound != null) {
            hasTarget = addModuleInformation(list, itemStack);
        }
        if (!hasTarget) {
            list.add(new StringTextComponent(TextFormatting.YELLOW + "Sneak right-click on a storage scanner to set the"));
            list.add(new StringTextComponent(TextFormatting.YELLOW + "target for this storage module"));
        }
        if (McJtyLib.proxy.isShiftKeyDown()) {
            list.add(new StringTextComponent(TextFormatting.WHITE + "This screen module allows you to monitor 9 different"));
            list.add(new StringTextComponent(TextFormatting.WHITE + "items through a storage scanner."));
            list.add(new StringTextComponent(TextFormatting.WHITE + "This module can also be combined with a tablet"));
            list.add(new StringTextComponent(TextFormatting.WHITE + "for remote access to a storage scanner controlled"));
            list.add(new StringTextComponent(TextFormatting.WHITE + "system"));
        } else {
            list.add(new StringTextComponent(TextFormatting.WHITE + GuiProxy.SHIFT_MESSAGE));
        }
    }

    public static boolean addModuleInformation(List<ITextComponent> list, ItemStack stack) {
        if (!stack.hasTag()) {
            return false;
        }
        list.add(new StringTextComponent(TextFormatting.YELLOW + "Label: " + stack.getTag().getString("text")));

        if (RFToolsTools.hasModuleTarget(stack)) {
            BlockPos pos = RFToolsTools.getPositionFromModule(stack);
            String monitorname = stack.getTag().getString("monitorname");
            list.add(new StringTextComponent(TextFormatting.YELLOW + "Monitoring: " + monitorname + " (at " + BlockPosTools.toString(pos) + ")"));
            return true;
        }
        return false;
    }

    @Override
    public ActionResultType onItemUse(ItemUseContext context) {
        ItemStack stack = context.getItem();
        World world = context.getWorld();
        BlockPos pos = context.getPos();
        TileEntity te = world.getTileEntity(pos);
        // @todo 1.14
//        if (te instanceof StorageScannerTileEntity) {
//            BlockState state = world.getBlockState(pos);
//            Block block = state.getBlock();
//            String name = "<invalid>";
//            if (block != null && !block.isAir(state, world, pos)) {
//                name = BlockTools.getReadableName(world, pos);
//            }
//            RFToolsTools.setPositionInModule(stack, world.getDimension().getType().getId(), pos, name);
//            if (world.isRemote) {
//                Logging.message(player, "Storage module is set to block '" + name + "'");
//            }
//        } else {
//            RFToolsTools.clearPositionInModule(stack);
//            if (world.isRemote) {
//                Logging.message(player, "Storage module is cleared");
//            }
//        }
        return ActionResultType.SUCCESS;
    }

    @Override
    public Class<StorageControlScreenModule> getServerScreenModule() {
        return StorageControlScreenModule.class;
    }

    @Override
    public Class<StorageControlClientScreenModule> getClientScreenModule() {
        return StorageControlClientScreenModule.class;
    }

    @Override
    public String getModuleName() {
        return "Stor";
    }

    @Override
    public void createGui(IModuleGuiBuilder guiBuilder) {
        guiBuilder
                .ghostStack("stack0").ghostStack("stack1").ghostStack("stack2").nl()
                .ghostStack("stack3").ghostStack("stack4").ghostStack("stack5").nl()
                .ghostStack("stack6").ghostStack("stack7").ghostStack("stack8").nl()
                .toggle("starred", "Starred", "If enabled only count items", "in 'starred' inventories", "(mark inventories in storage scanner)")
                .toggle("oredict", "Ore Dict", "If enabled use ore dictionary", "to match items").nl()
                .block("monitor").nl();
    }

    // @todo 1.14 implement!
    @Override
    public Collection<String> getTagsToPreserve() {
        return null;
    }
}