package mcjty.rftools.items.storage;

import mcjty.lib.McJtyRegister;
import mcjty.rftools.RFTools;
import mcjty.rftools.blocks.storage.sorters.*;
import mcjty.rftools.setup.GuiProxy;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Keyboard;

import java.util.ArrayList;
import java.util.List;

public class GenericTypeItem extends StorageTypeItem {
    private List<ItemSorter> sorters = null;

    public GenericTypeItem() {
        setMaxStackSize(16);
        setUnlocalizedName("generic_module");
        setRegistryName("generic_module");
        setCreativeTab(RFTools.setup.getTab());
        McJtyRegister.registerLater(this, RFTools.instance);
    }

    @Override
    public List<ItemSorter> getSorters() {
        if (sorters == null) {
            sorters = new ArrayList<>();
            sorters.add(new NameItemSorter());
            sorters.add(new CountItemSorter());
            sorters.add(new GenericItemSorter());
            sorters.add(new ModItemSorter());
        }
        return sorters;
    }

    @SideOnly(Side.CLIENT)
    public void initModel() {
        ModelLoader.setCustomModelResourceLocation(this, 0, new ModelResourceLocation(RFTools.MODID + ":" + getUnlocalizedName().substring(5), "inventory"));
    }


    @Override
    public String getLongLabel(ItemStack stack) {
        return stack.getDisplayName();
    }

    @Override
    public String getShortLabel(ItemStack stack) {
        return stack.getDisplayName();
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void addInformation(ItemStack itemStack, World player, List<String> list, ITooltipFlag whatIsThis) {
        super.addInformation(itemStack, player, list, whatIsThis);
        if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT)) {
            list.add(TextFormatting.WHITE + "This module extends the Modular Storage block");
            list.add(TextFormatting.WHITE + "with support for sorting items in general");
        } else {
            list.add(TextFormatting.WHITE + GuiProxy.SHIFT_MESSAGE);
        }
    }
}
