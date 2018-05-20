package mcjty.rftools.blocks.storage;

import mcjty.lib.api.IModuleSupport;
import mcjty.lib.blocks.LogicSlabBlock;
import mcjty.lib.gui.GenericGuiContainer;
import mcjty.lib.varia.ModuleSupport;
import mcjty.rftools.RFTools;
import mcjty.rftools.blocks.screens.ScreenSetup;
import mcjty.theoneprobe.api.ElementAlignment;
import mcjty.theoneprobe.api.IProbeHitData;
import mcjty.theoneprobe.api.IProbeInfo;
import mcjty.theoneprobe.api.ProbeMode;
import mcp.mobius.waila.api.IWailaConfigHandler;
import mcp.mobius.waila.api.IWailaDataAccessor;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.Optional;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Keyboard;

import java.util.List;
import java.util.function.BiFunction;

public class LevelEmitterBlock extends LogicSlabBlock<LevelEmitterTileEntity, LevelEmitterContainer> {

    public static final PropertyBool MODULE = PropertyBool.create("module");

    public LevelEmitterBlock() {
        super(RFTools.instance, Material.IRON, LevelEmitterTileEntity.class, LevelEmitterContainer::new, "level_emitter", true);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public BiFunction<LevelEmitterTileEntity, LevelEmitterContainer, GenericGuiContainer<? super LevelEmitterTileEntity>> getGuiFactory() {
        return GuiLevelEmitter::new;
    }

    @Override
    protected IModuleSupport getModuleSupport() {
        return new ModuleSupport(LevelEmitterContainer.SLOT_MODULE) {
            @Override
            public boolean isModule(ItemStack itemStack) {
                return itemStack.getItem() == ScreenSetup.storageControlModuleItem;
            }
        };
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void addInformation(ItemStack itemStack, World player, List<String> list, ITooltipFlag whatIsThis) {
        super.addInformation(itemStack, player, list, whatIsThis);

        if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT)) {
            list.add(TextFormatting.WHITE + "This block can be retrofitted with a");
            list.add(TextFormatting.WHITE + "Storage Control Screen Module so that");
            list.add(TextFormatting.WHITE + "you can count items in your storage");
        } else {
            list.add(TextFormatting.WHITE + RFTools.SHIFT_MESSAGE);
        }
    }

    @Override
    @Optional.Method(modid = "theoneprobe")
    public void addProbeInfo(ProbeMode mode, IProbeInfo probeInfo, EntityPlayer player, World world, IBlockState blockState, IProbeHitData data) {
        super.addProbeInfo(mode, probeInfo, player, world, blockState, data);
        ItemStack module = getModule(world.getTileEntity(data.getPos()));
        if (module.isEmpty()) {
            probeInfo.text(TextFormatting.GREEN + "Install storage control screen module first");
        } else {
            TileEntity te = world.getTileEntity(data.getPos());
            if (te instanceof LevelEmitterTileEntity) {
                LevelEmitterTileEntity emitterTileEntity = (LevelEmitterTileEntity) te;
                int count = emitterTileEntity.getCurrentCount();
                ItemStack toCount = emitterTileEntity.getInventoryHelper().getStackInSlot(LevelEmitterContainer.SLOT_ITEMMATCH);
                if (!toCount.isEmpty()) {
                    probeInfo.horizontal(probeInfo.defaultLayoutStyle().alignment(ElementAlignment.ALIGN_CENTER))
                            .item(toCount)
                            .text(TextFormatting.BLUE + "Count: " + TextFormatting.WHITE + count);
                }
            }
        }
    }

    @SideOnly(Side.CLIENT)
    @Override
    @Optional.Method(modid = "waila")
    public List<String> getWailaBody(ItemStack itemStack, List<String> currenttip, IWailaDataAccessor accessor, IWailaConfigHandler config) {
        super.getWailaBody(itemStack, currenttip, accessor, config);
        ItemStack module = getModule(accessor.getTileEntity());
        if (module.isEmpty()) {
            currenttip.add(TextFormatting.GREEN + "Install storage control screen module first");
        }
        return currenttip;
    }


    @Override
    public boolean needsRedstoneCheck() {
        return false;
    }

    @Override
    public int getGuiID() {
        return RFTools.GUI_STORAGE_TERMINAL;
    }

    private static ItemStack getModule(TileEntity tileEntity) {
        if (tileEntity instanceof LevelEmitterTileEntity) {
            LevelEmitterTileEntity emitterTileEntity = (LevelEmitterTileEntity) tileEntity;
            return emitterTileEntity.getStackInSlot(LevelEmitterContainer.SLOT_MODULE);
        }
        return ItemStack.EMPTY;
    }

    @Override
    public IBlockState getActualState(IBlockState state, IBlockAccess world, BlockPos pos) {
        ItemStack module = getModule(world.getTileEntity(pos));
        return super.getActualState(state, world, pos).withProperty(MODULE, !module.isEmpty());
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, LOGIC_FACING, META_INTERMEDIATE, MODULE);
    }
}
