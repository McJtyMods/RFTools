package mcjty.rftools.blocks.infuser;

import mcjty.lib.api.Infusable;
import mcjty.rftools.RFTools;
import mcjty.rftools.blocks.GenericRFToolsBlock;
import net.minecraft.block.material.Material;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Keyboard;

import java.util.List;

public class MachineInfuserBlock extends GenericRFToolsBlock<MachineInfuserTileEntity, MachineInfuserContainer> implements Infusable {

    public MachineInfuserBlock() {
        super(Material.IRON, MachineInfuserTileEntity.class, MachineInfuserContainer.class, "machine_infuser", true);
    }

    @Override
    public int getGuiID() {
        return RFTools.GUI_MACHINE_INFUSER;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public Class<GuiMachineInfuser> getGuiClass() {
        return GuiMachineInfuser.class;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void addInformation(ItemStack itemStack, World player, List<String> list, ITooltipFlag whatIsThis) {
        super.addInformation(itemStack, player, list, whatIsThis);

        if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT)) {
            list.add(TextFormatting.WHITE + "With this machine you can improve most other machines");
            list.add(TextFormatting.WHITE + "in RFTools in various ways. This needs dimensional");
            list.add(TextFormatting.WHITE + "shards.");
            list.add(TextFormatting.YELLOW + "Infusing bonus: reduced power consumption.");
        } else {
            list.add(TextFormatting.WHITE + RFTools.SHIFT_MESSAGE);
        }
    }
}
