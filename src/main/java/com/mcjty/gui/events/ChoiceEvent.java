package com.mcjty.gui.events;

import com.mcjty.gui.widgets.Widget;

public interface ChoiceEvent {
    void choiceChanged(Widget parent, String newChoice);
}
