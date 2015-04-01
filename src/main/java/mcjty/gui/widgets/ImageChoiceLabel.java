package mcjty.gui.widgets;

import mcjty.gui.Window;
import mcjty.gui.events.ChoiceEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.util.ResourceLocation;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ImageChoiceLabel extends ImageLabel<ImageChoiceLabel> {
    private List<String> choiceList = new ArrayList<String>();
    private List<List<String>> tooltipList = new ArrayList<List<String>>();
    private List<ResourceLocation> imageList = new ArrayList<ResourceLocation>();
    private List<Integer> uList = new ArrayList<Integer>();
    private List<Integer> vList = new ArrayList<Integer>();

    private int currentChoice = -1;
    private List<ChoiceEvent> choiceEvents = null;

    public ImageChoiceLabel(Minecraft mc, Gui gui) {
        super(mc, gui);
    }

    @Override
    public List<String> getTooltips() {
        if (currentChoice == -1) {
            return super.getTooltips();
        } else {
            return tooltipList.get(currentChoice);
        }
    }

    public ImageChoiceLabel addChoice(String choice, String tooltips, ResourceLocation image, int u, int v) {
        choiceList.add(choice);
        tooltipList.add(Arrays.asList(StringUtils.split(tooltips, "\n")));
        imageList.add(image);
        uList.add(u);
        vList.add(v);
        if (currentChoice == -1) {
            currentChoice = 0;
            setCurrentChoice(currentChoice);
        }
        return this;
    }

    @Override
    public Widget mouseClick(Window window, int x, int y, int button) {
        if (isEnabledAndVisible()) {
            currentChoice++;
            if (currentChoice >= choiceList.size()) {
                currentChoice = 0;
            }
            setCurrentChoice(currentChoice);
            fireChoiceEvents(choiceList.get(currentChoice));
        }
        return null;
    }


    public void setCurrentChoice(int currentChoice) {
        this.currentChoice = currentChoice;
        setImage(imageList.get(currentChoice), uList.get(currentChoice), vList.get(currentChoice));
    }

    public int getCurrentChoice() {
        return currentChoice;
    }

    public ImageChoiceLabel addChoiceEvent(ChoiceEvent event) {
        if (choiceEvents == null) {
            choiceEvents = new ArrayList<ChoiceEvent>();
        }
        choiceEvents.add(event);
        return this;
    }

    public void removeChoiceEvent(ChoiceEvent event) {
        if (choiceEvents != null) {
            choiceEvents.remove(event);
        }
    }

    private void fireChoiceEvents(String choice) {
        if (choiceEvents != null) {
            for (ChoiceEvent event : choiceEvents) {
                event.choiceChanged(this, choice);
            }
        }
    }

}
