package mcjty.rftools.blocks.infuser;

import mcjty.lib.container.GenericBlock;
import mcjty.lib.container.GenericContainer;
import mcjty.rftools.RFTools;
import mcjty.rftools.blocks.ModBlocks;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class MachineInfuserSetup {
    public static GenericBlock<MachineInfuserTileEntity, GenericContainer> machineInfuserBlock;

    public static void init() {
        machineInfuserBlock = ModBlocks.builderFactory.<MachineInfuserTileEntity, GenericContainer> builder("machine_infuser")
                .tileEntityClass(MachineInfuserTileEntity.class)
                .container(GenericContainer.class, MachineInfuserTileEntity.CONTAINER_FACTORY)
                .guiId(RFTools.GUI_MACHINE_INFUSER)
                .information("message.rftools.shiftmessage")
                .informationShift("message.rftools.infuser")
                .build();
    }

    @SideOnly(Side.CLIENT)
    public static void initClient() {
        machineInfuserBlock.initModel();
        machineInfuserBlock.setGuiClass(GuiMachineInfuser.class);
    }
}
