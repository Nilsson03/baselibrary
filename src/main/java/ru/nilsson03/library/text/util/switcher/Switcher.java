package ru.nilsson03.library.text.util.switcher;

import ru.nilsson03.library.text.api.UniversalTextApi;

import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class Switcher {

    private final List<? extends Switchable> variables;
    private int currentPos;
    private final Consumer<Switchable> onSwitch;

    public Switcher(List<? extends Switchable> variables,
                    Switchable initVariable,
                    Consumer<Switchable> onSwitch) {
        this.variables = variables;
        this.onSwitch = onSwitch;
        this.currentPos = findInitialIndex(initVariable);
    }

    private int findInitialIndex(Switchable switchable) {
        String switchDisplayText = switchable.displayText();
        for (int i = 0; i < variables.size(); i++) {
            String displayText = variables.get(i).displayText();
            if (displayText.equalsIgnoreCase(switchDisplayText)) {
                return i;
            }
        }
        return 0;
    }

    public void next() {
        currentPos = (currentPos + 1) % variables.size();
        onSwitch.accept(variables.get(currentPos));
    }

    public void previous() {
        currentPos = (currentPos - 1 + variables.size()) % variables.size();
        onSwitch.accept(variables.get(currentPos));
    }

    public Switchable getCurrent() {
        return variables.get(currentPos);
    }

    public int getCurrentPos() {
        return currentPos;
    }

    public List<String> getStringWithSelection() {

        List<String> variables = this.variables.stream()
                .map(Switchable::displayText)
                .collect(Collectors.toList());

        return UniversalTextApi.getColoredSelectLore(variables, currentPos);
    }
}
