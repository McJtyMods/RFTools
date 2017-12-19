package mcjty.rftools.shapes;

import mcjty.lib.network.Arguments;
import mcjty.lib.network.PacketSendServerCommand;
import mcjty.rftools.CommandHandler;
import mcjty.rftools.RFTools;
import mcjty.rftools.network.RFToolsMessages;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;

public class ScanDataManagerClient {

    private static ScanDataManagerClient instance = null;

    private final Map<Integer, Scan> scans = new HashMap<>();
    private final Map<Integer, ScanExtraData> scanDataClient = new HashMap<>();

    public static ScanDataManagerClient getScansClient() {
        if (instance == null) {
            instance = new ScanDataManagerClient();
        }
        return instance;
    }

    @Nonnull
    public Scan getOrCreateScan(int id) {
        Scan scan = scans.get(id);
        if (scan == null) {
            scan = new Scan();
        }
        return scan;
    }


    public ScanExtraData getExtraDataClient(int id) {
        ScanExtraData data = scanDataClient.get(id);
        if (data == null) {
            data = new ScanExtraData();
            scanDataClient.put(id, data);
        } else {
//            // @todo configurable and dependend on locator speed
//            if (data.getBirthTime() + 3000 < System.currentTimeMillis()) {
//                data = new ScanExtraData(System.currentTimeMillis());
//                scanDataClient.put(id, data);
//            }
        }
        return data;
    }

    // Client side only
    public void requestExtraDataClient(int id) {
        RFToolsMessages.INSTANCE.sendToServer(new PacketSendServerCommand(RFTools.MODID, CommandHandler.CMD_REQUESTSHAPEDATA,
                Arguments.builder().value(id).build()));
    }

    // Client side only
    public void registerExtraDataFromServer(int id, ScanExtraData extraData) {
        scanDataClient.put(id, extraData);
    }

    public int getScanDirtyCounterClient(int id) {
        Scan scan;
        if (!scans.containsKey(id)) {
            scan = new Scan();
            scans.put(id, scan);
        } else {
            scan = scans.get(id);
        }
        scan.dirtyRequestTimeout--;
        if (scan.dirtyRequestTimeout <= 0) {
            RFToolsMessages.INSTANCE.sendToServer(new PacketSendServerCommand(RFTools.MODID, CommandHandler.CMD_REQUESTSCANDIRTY,
                    Arguments.builder().value(id).build()));
            scan.dirtyRequestTimeout = 20;
        }
        return scan.getDirtyCounter();
    }

}
