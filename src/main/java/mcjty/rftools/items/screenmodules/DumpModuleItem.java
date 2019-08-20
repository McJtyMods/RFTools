package mcjty.rftools.items.screenmodules;

import mcjty.lib.varia.BlockPosTools;
import mcjty.lib.varia.BlockTools;
import mcjty.lib.varia.Logging;
import mcjty.rftools.api.screens.IModuleGuiBuilder;
import mcjty.rftools.api.screens.IModuleProvider;
import mcjty.rftools.blocks.screens.ScreenConfiguration;
import mcjty.rftools.blocks.screens.modules.DumpScreenModule;
import mcjty.rftools.blocks.screens.modulesclient.DumpClientScreenModule;
import mcjty.rftools.blocks.storagemonitor.StorageScannerTileEntity;
import mcjty.rftools.setup.GuiProxy;
import mcjty.rftools.varia.RFToolsTools;
import net.minecraft.block.Block;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Keyboard;

import java.util.List;

public class DumpModuleItem extends GenericRFToolsItem implements IModuleProvider {

    public DumpModuleItem() {
        super("dump_module");
    }

    @Override
    public int getMaxItemUseDuration(ItemStack stack) {
        return 1;
    }

    @Override
    public Class<DumpScreenModule> getServerScreenModule() {
        return DumpScreenModule.class;
    }

    @Override
    public Class<DumpClientScreenModule> getClientScreenModule() {
        return DumpClientScreenModule.class;
    }

    @Override
    public String getName() {
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

    @SideOnly(Side.CLIENT)
    @Override
    public void addInformation(ItemStack itemStack, World player, List<String> list, ITooltipFlag whatIsThis) {
        super.addInformation(itemStack, player, list, whatIsThis);
        list.add(TextFormatting.GREEN + "Uses " + ScreenConfiguration.DUMP_RFPERTICK.get() + " RF/tick");
        boolean hasTarget = false;
        CompoundNBT tagCompound = itemStack.getTag();
        if (tagCompound != null) {
            hasTarget = addModuleInformation(list, itemStack);
        }
        if (!hasTarget) {
            list.add(TextFormatting.YELLOW + "Sneak right-click on a storage scanner to set the");
            list.add(TextFormatting.YELLOW + "target for this dump module");
        }
        if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT)) {
            list.add(TextFormatting.WHITE + "This screen module allows you to dump");
            list.add(TextFormatting.WHITE + "a lot of items through a storage scanner");
        } else {
            list.add(TextFormatting.WHITE + GuiProxy.SHIFT_MESSAGE);
        }
    }

    public static boolean addModuleInformation(List<String> list, ItemStack stack) {
        if (!stack.hasTagCompound()) {
            return false;
        }
        list.add(TextFormatting.YELLOW + "Label: " + stack.getTag().getString("text"));

        if (RFToolsTools.hasModuleTarget(stack)) {
            BlockPos pos = RFToolsTools.getPositionFromModule(stack);
            String monitorname = stack.getTag().getString("monitorname");
            list.add(TextFormatting.YELLOW + "Monitoring: " + monitorname + " (at " + BlockPosTools.toString(pos) + ")");
            return true;
        }
        return false;
    }

    @Override
    public ActionResultType onItemUse(PlayerEntity player, World world, BlockPos pos, Hand hand, Direction facing, float hitX, float hitY, float hitZ) {
        ItemStack stack = player.getHeldItem(hand);
        TileEntity te = world.getTileEntity(pos);
        if (te instanceof StorageScannerTileEntity) {
            BlockState state = world.getBlockState(pos);
            Block block = state.getBlock();
            String name = "<invalid>";
            if (block != null && !block.isAir(state, world, pos)) {
                name = BlockTools.getReadableName(world, pos);
            }
            RFToolsTools.setPositionInModule(stack, world.provider.getDimension(), pos, name);
            if (world.isRemote) {
                Logging.message(player, "Storage module is set to block '" + name + "'");
            }
        } else {
            RFToolsTools.clearPositionInModule(stack);
            if (world.isRemote) {
                Logging.message(player, "Storage module is cleared");
            }
        }
        return ActionResultType.SUCCESS;
    }
}