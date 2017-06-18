package mcjty.rftools.blocks.booster;

import mcjty.lib.api.IModuleSupport;
import mcjty.lib.api.Infusable;
import mcjty.lib.container.GenericGuiContainer;
import mcjty.lib.varia.ModuleSupport;
import mcjty.rftools.RFTools;
import mcjty.rftools.blocks.GenericRFToolsBlock;
import mcjty.rftools.blocks.environmental.EnvModuleProvider;
import mcp.mobius.waila.api.IWailaConfigHandler;
import mcp.mobius.waila.api.IWailaDataAccessor;
import net.minecraft.block.material.Material;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Keyboard;

import java.util.List;

public class BoosterBlock extends GenericRFToolsBlock<BoosterTileEntity, BoosterContainer> implements Infusable {

    public BoosterBlock() {
        super(Material.IRON, BoosterTileEntity.class, BoosterContainer.class, "booster", false);
    }

    @Override
    public boolean hasNoRotation() {
        return true;
    }

    @Override
    public boolean needsRedstoneCheck() {
        return true;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public Class<? extends GenericGuiContainer> getGuiClass() {
        return GuiBooster.class;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void addInformation(ItemStack itemStack, World player, List<String> list, ITooltipFlag whatIsThis) {
        super.addInformation(itemStack, player, list, whatIsThis);
        if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT)) {
            list.add(TextFormatting.WHITE + "This blocks gives entities directly on top of this");
            list.add(TextFormatting.WHITE + "block a temporary effect");
            list.add(TextFormatting.YELLOW + "Infusing bonus: reduced power consumption");
        } else {
            list.add(TextFormatting.WHITE + RFTools.SHIFT_MESSAGE);
        }
    }

    @SideOnly(Side.CLIENT)
    @Override
    public List<String> getWailaBody(ItemStack itemStack, List<String> currenttip, IWailaDataAccessor accessor, IWailaConfigHandler config) {
        super.getWailaBody(itemStack, currenttip, accessor, config);
        return currenttip;
    }

    @Override
    protected IModuleSupport getModuleSupport() {
        return new ModuleSupport(BoosterContainer.SLOT_MODULE) {
            @Override
            public boolean isModule(ItemStack itemStack) {
                return itemStack.getItem() instanceof EnvModuleProvider;
            }
        };
    }

    @Override
    public int getGuiID() {
        return RFTools.GUI_BOOSTER;
    }
}
