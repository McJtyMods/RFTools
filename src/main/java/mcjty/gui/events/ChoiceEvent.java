package mcjty.gui.events;

import mcjty.gui.widgets.Widget;

public interface ChoiceEvent {
    void choiceChanged(Widget parent, String newChoice);
}
