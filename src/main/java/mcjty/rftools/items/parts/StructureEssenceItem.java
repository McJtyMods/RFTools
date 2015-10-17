package mcjty.rftools.items.parts;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import mcjty.rftools.RFTools;
import mcjty.rftools.blocks.dimletconstruction.DimletConstructionSetup;
import mcjty.rftools.dimension.world.types.StructureType;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;
import org.lwjgl.input.Keyboard;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StructureEssenceItem extends Item {
    public static final Map<Integer,StructureType> structures = new HashMap<Integer, StructureType>();

    static {
        structures.put(1, StructureType.STRUCTURE_VILLAGE);
        structures.put(2, StructureType.STRUCTURE_STRONGHOLD);
        structures.put(3, StructureType.STRUCTURE_DUNGEON);
        structures.put(4, StructureType.STRUCTURE_FORTRESS);
        structures.put(5, StructureType.STRUCTURE_MINESHAFT);
        structures.put(6, StructureType.STRUCTURE_SCATTERED);
    }

    public StructureEssenceItem() {
        setMaxStackSize(16);
        setHasSubtypes(true);
        setMaxDamage(0);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void addInformation(ItemStack itemStack, EntityPlayer player, List list, boolean whatIsThis) {
        super.addInformation(itemStack, player, list, whatIsThis);
        if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT)) {
            list.add(EnumChatFormatting.WHITE + "This essence item is the main ingredient for");
            list.add(EnumChatFormatting.WHITE + "structure dimlets.");
        } else {
            list.add(EnumChatFormatting.WHITE + RFTools.SHIFT_MESSAGE);
        }
    }

    @Override
    public String getUnlocalizedName(ItemStack itemStack) {
        StructureType structureType = structures.get(itemStack.getItemDamage());
        if (structureType == null) {
            return "unknown";
        }
        return super.getUnlocalizedName(itemStack) + structureType.getName();
    }

    @Override
    public void getSubItems(Item item, CreativeTabs creativeTabs, List list) {
        for (Integer key : structures.keySet()) {
            list.add(new ItemStack(DimletConstructionSetup.structureEssenceItem, 1, key));
        }
    }

}
