package org.example.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
public class Quest {
    private int stages;
    private Player sponsor;
    private List<Stage> stageList = new ArrayList<>();

    public Quest(int stages) {
        this.stages = stages;
    }

    public Stage getStage(int index) {
        return stageList.get(index);
    }

    public void addStage(Stage stage) {
        if (stageList.size() < stages) {
            stageList.add(stage);
        } else {
            throw new IllegalStateException("Cannot add more stages to this quest");
        }
    }

    public boolean isValid() {
        if (stageList.size() != stages) {
            return false;
        }
        for (int i = 1; i < stageList.size(); i++) {
            if (stageList.get(i).getValue() <= stageList.get(i - 1).getValue()) {
                return false;
            }
        }
        return true;
    }
}