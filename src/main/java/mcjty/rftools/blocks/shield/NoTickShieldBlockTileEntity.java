package mcjty.rftools.blocks.shield;

public class NoTickShieldBlockTileEntity extends ShieldBlockTileEntity {
    @Override
    public boolean canUpdate() {
        return false;
    }
}
