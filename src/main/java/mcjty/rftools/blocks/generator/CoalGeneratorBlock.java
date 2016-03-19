package mcjty.rftools.blocks.generator;


import mcjty.lib.api.Infusable;
import mcjty.lib.container.GenericGuiContainer;
import mcjty.rftools.RFTools;
import mcjty.rftools.blocks.GenericRFToolsBlock;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Keyboard;

import java.util.List;

public class CoalGeneratorBlock extends GenericRFToolsBlock<CoalGeneratorTileEntity, CoalGeneratorContainer> implements Infusable {

    public static final PropertyBool WORKING = PropertyBool.create("working");

    public CoalGeneratorBlock() {
        super(Material.iron, CoalGeneratorTileEntity.class, CoalGeneratorContainer.class, "coalgenerator", true);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public Class<? extends GenericGuiContainer> getGuiClass() {
        return GuiCoalGenerator.class;
    }

    @Override
    public int getGuiID() {
        return RFTools.GUI_COALGENERATOR;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void addInformation(ItemStack itemStack, EntityPlayer player, List list, boolean whatIsThis) {
        super.addInformation(itemStack, player, list, whatIsThis);

        if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT)) {
            list.add(TextFormatting.WHITE + "This machine produces RF (" + CoalGeneratorConfiguration.rfPerTick + " RF/t)");
            list.add(TextFormatting.WHITE + "from coal or charcoal");
            list.add(TextFormatting.YELLOW + "Infusing bonus: more power generation");
            list.add(TextFormatting.YELLOW + "and lasts longer on a single fuel");
        } else {
            list.add(TextFormatting.WHITE + RFTools.SHIFT_MESSAGE);
        }
    }

//    @SideOnly(Side.CLIENT)
//    @Override
//    public List<String> getWailaBody(ItemStack itemStack, List<String> currenttip, IWailaDataAccessor accessor, IWailaConfigHandler config) {
//        super.getWailaBody(itemStack, currenttip, accessor, config);
//        CoalGeneratorTileEntity te = (CoalGeneratorTileEntity) accessor.getTileEntity();
//        Boolean working = te.isWorking();
//        if (working) {
//            currenttip.add(TextFormatting.GREEN + "Producing " + te.getRfPerTick() + " RF/t");
//        }
//
//        return currenttip;
//    }

    @Override
    public IBlockState getActualState(IBlockState state, IBlockAccess world, BlockPos pos) {
        TileEntity te = world.getTileEntity(pos);
        boolean working = false;
        if (te instanceof CoalGeneratorTileEntity) {
            working = ((CoalGeneratorTileEntity)te).isWorking();
        }
        return state.withProperty(WORKING, working);
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, FACING, WORKING);
    }


}
