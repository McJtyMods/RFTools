package mcjty.rftools.blocks.infuser;

import mcjty.lib.blocks.GenericBlock;
import mcjty.lib.container.GenericContainer;
import mcjty.rftools.RFTools;
import mcjty.rftools.blocks.ModBlocks;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class MachineInfuserSetup {
    public static GenericBlock<MachineInfuserTileEntity, GenericContainer> machineInfuserBlock;

    public static void init() {
        machineInfuserBlock = ModBlocks.builderFactory.<MachineInfuserTileEntity> builder("machine_infuser")
                .tileEntityClass(MachineInfuserTileEntity.class)
                .container(MachineInfuserTileEntity.CONTAINER_FACTORY)
                .guiId(RFTools.GUI_MACHINE_INFUSER)
                .info("message.rftools.shiftmessage")
                .infoExtended("message.rftools.infuser")
                .build();
    }

    @SideOnly(Side.CLIENT)
    public static void initClient() {
        machineInfuserBlock.initModel();
        machineInfuserBlock.setGuiFactory(GuiMachineInfuser::new);
    }
}
