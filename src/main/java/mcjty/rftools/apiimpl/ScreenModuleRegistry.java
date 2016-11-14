package mcjty.rftools.apiimpl;

import mcjty.rftools.api.screens.IScreenModuleRegistry;
import mcjty.rftools.api.screens.data.IModuleDataFactory;
import mcjty.rftools.blocks.screens.data.ModuleDataBoolean;
import mcjty.rftools.blocks.screens.data.ModuleDataInteger;
import mcjty.rftools.blocks.screens.data.ModuleDataString;
import mcjty.rftools.blocks.screens.modules.ElevatorButtonScreenModule;
import mcjty.rftools.blocks.screens.modules.ItemStackScreenModule;
import mcjty.rftools.blocks.screens.modules.ScreenModuleHelper;
import mcjty.rftools.blocks.screens.modules.StorageControlScreenModule;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

public class ScreenModuleRegistry implements IScreenModuleRegistry {

    private Map<String, IModuleDataFactory> dataFactoryMap = new HashMap<>();
    private Map<String, Integer> idToIntMap = null;
    private Map<Integer, String> inttoIdMap = null;


    public void registerBuiltins() {
        dataFactoryMap.put(ModuleDataBoolean.ID, ModuleDataBoolean::new);
        dataFactoryMap.put(ModuleDataInteger.ID, ModuleDataInteger::new);
        dataFactoryMap.put(ModuleDataString.ID, ModuleDataString::new);
        dataFactoryMap.put(ScreenModuleHelper.ModuleDataContents.ID, ScreenModuleHelper.ModuleDataContents::new);
        dataFactoryMap.put(ItemStackScreenModule.ModuleDataStacks.ID, ItemStackScreenModule.ModuleDataStacks::new);
        dataFactoryMap.put(StorageControlScreenModule.ModuleDataStacks.ID, StorageControlScreenModule.ModuleDataStacks::new);
        dataFactoryMap.put(ElevatorButtonScreenModule.ModuleElevatorInfo.ID, ElevatorButtonScreenModule.ModuleElevatorInfo::new);
    }

    @Override
    public void registerModuleDataFactory(String id, IModuleDataFactory dataFactory) {
        dataFactoryMap.put(id, dataFactory);
    }

    @Override
    public IModuleDataFactory getModuleDataFactory(String id) {
        return dataFactoryMap.get(id);
    }

    public String getNormalId(int i) {
        createIdMap();
        return inttoIdMap.get(i);
    }

    public int getShortId(String id) {
        createIdMap();
        return idToIntMap.get(id);
    }

    private void createIdMap() {
        if (idToIntMap == null) {
            idToIntMap = new HashMap<>();
            inttoIdMap = new HashMap<>();
            ArrayList<String> strings = new ArrayList<>(dataFactoryMap.keySet());
            strings.sort(Comparator.<String>naturalOrder());
            int idx = 0;
            for (String s : strings) {
                idToIntMap.put(s, idx);
                inttoIdMap.put(idx, s);
                idx++;
            }
        }
    }
}
