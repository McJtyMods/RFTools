package mcjty.rftools.items.screenmodules;

import mcjty.lib.McJtyLib;
import mcjty.lib.varia.BlockPosTools;
import mcjty.rftools.RFTools;
import mcjty.rftools.api.screens.IModuleGuiBuilder;
import mcjty.rftools.api.screens.IModuleProvider;
import mcjty.rftools.blocks.screens.ScreenConfiguration;
import mcjty.rftools.blocks.screens.modules.DumpScreenModule;
import mcjty.rftools.blocks.screens.modulesclient.DumpClientScreenModule;
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

import java.util.List;

public class DumpModuleItem extends Item implements IModuleProvider {

    public DumpModuleItem() {
        super(new Item.Properties().defaultMaxDamage(1).group(RFTools.setup.getTab()));
        setRegistryName("dump_module");
    }

//    @Override
//    public int getMaxItemUseDuration(ItemStack stack) {
//        return 1;
//    }

    @Override
    public Class<DumpScreenModule> getServerScreenModule() {
        return DumpScreenModule.class;
    }

    @Override
    public Class<DumpClientScreenModule> getClientScreenModule() {
        return DumpClientScreenModule.class;
    }

    @Override
    public String getModuleName() {
        return "Dump";
    }

    @Override
    public void createGui(IModuleGuiBuilder guiBuilder) {
        int index = 0;
        for (int y = 0 ; y < DumpScreenModule.ROWS ; y++) {
            for (int x = 0 ; x < DumpScreenModule.COLS ; x++) {
                guiBuilder.ghostStack("stack" + index);
                index++;
            }
            guiBuilder.nl();
        }
        guiBuilder
                .label("Label:").text("text", "Label text").color("color", "Label color").nl()
                .toggle("oredict", "Ore Dict", "If enabled use ore dictionary", "to match items");
    }

    @Override
    public void addInformation(ItemStack itemStack, World world, List<ITextComponent> list, ITooltipFlag flag) {
        super.addInformation(itemStack, world, list, flag);
        list.add(new StringTextComponent(TextFormatting.GREEN + "Uses " + ScreenConfiguration.DUMP_RFPERTICK.get() + " RF/tick"));
        boolean hasTarget = false;
        CompoundNBT tagCompound = itemStack.getTag();
        if (tagCompound != null) {
            hasTarget = addModuleInformation(list, itemStack);
        }
        if (!hasTarget) {
            list.add(new StringTextComponent(TextFormatting.YELLOW + "Sneak right-click on a storage scanner to set the"));
            list.add(new StringTextComponent(TextFormatting.YELLOW + "target for this dump module"));
        }
        if (McJtyLib.proxy.isShiftKeyDown()) {
            list.add(new StringTextComponent(TextFormatting.WHITE + "This screen module allows you to dump"));
            list.add(new StringTextComponent(TextFormatting.WHITE + "a lot of items through a storage scanner"));
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
}