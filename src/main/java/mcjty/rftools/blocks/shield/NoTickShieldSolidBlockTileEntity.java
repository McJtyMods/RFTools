package mcjty.rftools.blocks.shield;

import net.minecraft.tileentity.TileEntityType;

import static mcjty.rftools.blocks.shield.ShieldSetup.TYPE_SHIELD_SOLID_NO_TICK_BLOCK;

public class NoTickShieldSolidBlockTileEntity extends NoTickShieldBlockTileEntity {
    public NoTickShieldSolidBlockTileEntity() {
        super(TYPE_SHIELD_SOLID_NO_TICK_BLOCK);
    }
}
