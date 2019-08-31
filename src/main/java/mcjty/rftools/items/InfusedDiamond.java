package mcjty.rftools.items;

import mcjty.rftools.RFTools;
import net.minecraft.item.Item;

public class InfusedDiamond extends Item {

    public InfusedDiamond() {
        super(new Properties().maxStackSize(64).group(RFTools.setup.getTab()));
        setRegistryName("infused_diamond");
    }
}
