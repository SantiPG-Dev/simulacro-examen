package com.examenes.util;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Circle;

public class ThemeToggle extends HBox {

    private final Circle thumb;
    private final StackPane switchPane;
    private boolean darkMode;
    private Runnable onToggle;

    public ThemeToggle() {
        Label sun = new Label("\u2600");
        sun.getStyleClass().add("theme-icon");

        Region track = new Region();
        track.getStyleClass().add("theme-track");
        track.setPrefSize(44, 22);
        track.setMinSize(44, 22);
        track.setMaxSize(44, 22);

        thumb = new Circle(8);
        thumb.getStyleClass().add("theme-thumb");

        switchPane = new StackPane(track, thumb);
        switchPane.setPrefSize(44, 22);
        switchPane.setMinSize(44, 22);
        switchPane.setMaxSize(44, 22);
        switchPane.getStyleClass().addAll("theme-switch-pane", "theme-switch-light");
        switchPane.setOnMouseClicked(this::handleClick);

        StackPane.setAlignment(thumb, Pos.CENTER_LEFT);
        StackPane.setMargin(thumb, new Insets(0, 0, 0, 3));

        Label moon = new Label("\u263E");
        moon.getStyleClass().add("theme-icon");

        setAlignment(Pos.CENTER);
        setSpacing(6);
        getChildren().addAll(sun, switchPane, moon);
        getStyleClass().add("theme-toggle-container");

        updateVisual();
    }

    private void handleClick(MouseEvent e) {
        darkMode = !darkMode;
        updateVisual();
        if (onToggle != null) onToggle.run();
    }

    private void updateVisual() {
        if (darkMode) {
            StackPane.setAlignment(thumb, Pos.CENTER_RIGHT);
            StackPane.setMargin(thumb, new Insets(0, 3, 0, 0));
            switchPane.getStyleClass().remove("theme-switch-light");
            switchPane.getStyleClass().add("theme-switch-dark");
        } else {
            StackPane.setAlignment(thumb, Pos.CENTER_LEFT);
            StackPane.setMargin(thumb, new Insets(0, 0, 0, 3));
            switchPane.getStyleClass().remove("theme-switch-dark");
            switchPane.getStyleClass().add("theme-switch-light");
        }
    }

    public void setDarkMode(boolean dark) {
        this.darkMode = dark;
        updateVisual();
    }

    public boolean isDarkMode() {
        return darkMode;
    }

    public void setOnToggle(Runnable r) {
        this.onToggle = r;
    }
}
