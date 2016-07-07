package mcjty.rftools.items.screenmodules;

import mcjty.lib.varia.BlockPosTools;
import mcjty.lib.varia.Logging;
import mcjty.rftools.BlockInfo;
import mcjty.rftools.RFTools;
import mcjty.rftools.api.screens.IClientScreenModule;
import mcjty.rftools.api.screens.IModuleProvider;
import mcjty.rftools.api.screens.IScreenModule;
import mcjty.rftools.blocks.screens.ScreenConfiguration;
import mcjty.rftools.blocks.screens.modules.DumpScreenModule;
import mcjty.rftools.blocks.screens.modulesclient.DumpClientScreenModule;
import mcjty.rftools.blocks.storagemonitor.StorageScannerTileEntity;
import mcjty.rftools.items.GenericRFToolsItem;
import mcjty.rftools.varia.RFToolsTools;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
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
    public Class<? extends IScreenModule> getServerScreenModule() {
        return DumpScreenModule.class;
    }

    @Override
    public Class<? extends IClientScreenModule> getClientScreenModule() {
        return DumpClientScreenModule.class;
    }

    @Override
    public String getName() {
        return "Dump";
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void addInformation(ItemStack itemStack, EntityPlayer player, List<String> list, boolean whatIsThis) {
        super.addInformation(itemStack, player, list, whatIsThis);
        list.add(TextFormatting.GREEN + "Uses " + ScreenConfiguration.DUMP_RFPERTICK + " RF/tick");
        boolean hasTarget = false;
        NBTTagCompound tagCompound = itemStack.getTagCompound();
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
            list.add(TextFormatting.WHITE + RFTools.SHIFT_MESSAGE);
        }
    }

    public static boolean addModuleInformation(List<String> list, ItemStack stack) {
        if (!stack.hasTagCompound()) {
            return false;
        }
        list.add(TextFormatting.YELLOW + "Label: " + stack.getTagCompound().getString("text"));

        if (RFToolsTools.hasModuleTarget(stack)) {
            BlockPos pos = RFToolsTools.getPositionFromModule(stack);
            String monitorname = stack.getTagCompound().getString("monitorname");
            list.add(TextFormatting.YELLOW + "Monitoring: " + monitorname + " (at " + BlockPosTools.toString(pos) + ")");
            return true;
        }
        return false;
    }

    @Override
    public EnumActionResult onItemUse(ItemStack stack, EntityPlayer player, World world, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        TileEntity te = world.getTileEntity(pos);
        if (te instanceof StorageScannerTileEntity) {
            IBlockState state = world.getBlockState(pos);
            Block block = state.getBlock();
            String name = "<invalid>";
            if (block != null && !block.isAir(state, world, pos)) {
                name = BlockInfo.getReadableName(world.getBlockState(pos));
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
        return EnumActionResult.SUCCESS;
    }
}