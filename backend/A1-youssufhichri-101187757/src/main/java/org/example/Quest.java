package org.example;
import java.util.ArrayList;
import java.util.List;

public class Quest {
    private int stages;
    private Player sponsor;
    private List<Stage> stageList;

    public Quest(int stages) {
        this.stages = stages;
        stageList = new ArrayList<>();
    }

    public Player getSponsor() {
        return sponsor;
    }

    public void setSponsor(Player sponsor) {
        this.sponsor = sponsor;
    }

    public int getStages() {
        return stages;
    }

    public void setStages(int stages) {
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