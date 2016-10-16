package com.mygdx.dnd;

import com.badlogic.gdx.*;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;

/**
 * Created by David on 10/9/2016.
 */
public class PromptScreen extends ScreenAdapter {
    private DND game;
    private Stage stage;
    private Table table;
    private Label label;
    private TextField textField;
    private String response;

    public PromptScreen(DND g) {
        this.game = g;
        response = "";

        stage = new Stage();

        BitmapFont font = new BitmapFont();

        Label.LabelStyle ls = new Label.LabelStyle();
        ls.font = font;
        label = new Label("Test Label", ls);

        TextField.TextFieldStyle tfs = new TextField.TextFieldStyle();
        tfs.font = font;
        tfs.fontColor = Color.BLACK;
        tfs.background = new TextureRegionDrawable(new TextureRegion(new Texture("ui/textfield.png")));
        textField = new TextField("", tfs);
        textField.addListener(new InputListener() {
            public boolean keyDown(InputEvent event, int keycode) {
                if (keycode == Input.Keys.ENTER) {
                    response = textField.getText();
                    textField.setText("");
                    game.setScreen(game.getGameScreen());
                }
                return true;
            }
        });

        table = new Table();
        table.add(label);
        table.row();
        table.add(textField);
        table.setFillParent(true);
        stage.addActor(table);
    }

    public void render(float delta) {
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        stage.act(Gdx.graphics.getDeltaTime());
        stage.draw();
    }

    public void dispose() {
        stage.dispose();
    }

    public void show() {
        stage.getViewport().update(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), false);
        Gdx.input.setInputProcessor(stage);
    }

    public void prompt(String promptText) {
        stage.setKeyboardFocus(textField);
        response = "";
        label.setText(promptText);
        game.setScreen(this);
    }

    public String getResponse() {
        String temp = response;
        response = "";
        return temp;
    }

    public void resize (int width, int height) {
        stage.getViewport().update(width, height, false);
    }
}
