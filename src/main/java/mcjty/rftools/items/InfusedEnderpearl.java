package mcjty.rftools.items;

import mcjty.rftools.RFTools;
import net.minecraft.item.Item;

public class InfusedEnderpearl extends Item {

    public InfusedEnderpearl() {
        super(new Properties().maxStackSize(16).group(RFTools.setup.getTab()));
        setRegistryName("infused_enderpearl");
    }
}
