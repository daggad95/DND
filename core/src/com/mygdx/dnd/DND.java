package com.mygdx.dnd;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Screen;

/**
 * Created by David on 10/9/2016.
 */
public class DND extends Game {
    GameScreen gameScreen;
    PromptScreen promptScreen;

    public void create() {
        gameScreen = new GameScreen(this);
        promptScreen = new PromptScreen(this);

        setScreen(gameScreen);
    }

    public GameScreen getGameScreen() {
        return gameScreen;
    }

    public PromptScreen getPromptScreen() {
        return promptScreen;
    }
}
