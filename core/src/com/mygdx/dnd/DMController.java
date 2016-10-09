package com.mygdx.dnd;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;

/**
 * Created by David on 10/2/2016.
 */
public class DMController {
    private Input.TextInputListener textListener;
    private DungeonMaster dungeonMaster;
    private OrthographicCamera camera;
    public static float CZS = 3; //speed at which the camera zooms

    public DMController(DungeonMaster dm, OrthographicCamera c) {
        this.dungeonMaster = dm;
        this.camera = c;

        //Keyboard input
        Gdx.input.setInputProcessor(new InputAdapter() {


            public boolean touchDown (int x, int y, int pointer, int button) {
                //changing mouse coordinates to game coordinates
                camera.update();
                Vector3 mousePosition = new Vector3(x, y, 0);
                camera.unproject(mousePosition);
                x = (int) Math.floor(mousePosition.x);
                y = (int) Math.floor(mousePosition.y);

                //sets entity controlled by dm to clicked on entity
                Vector2 pos = new Vector2((float)x, (float)y);
                if (dungeonMaster.entityAt(pos)) {
                    dungeonMaster.setCurrentEntity(dungeonMaster.getEntity(pos));
                } else {
                    dungeonMaster.setLastClicked(pos);
                    Gdx.input.getTextInput(textListener, "Enter Command", "", "");
                }
                return true;
            }

            public boolean keyDown(int keycode) {
                //camera movement
                if (keycode == Input.Keys.W) { dungeonMaster.setCameraMoving(Direction.UP, true); }
                if (keycode == Input.Keys.S) { dungeonMaster.setCameraMoving(Direction.DOWN, true); }
                if (keycode == Input.Keys.A) { dungeonMaster.setCameraMoving(Direction.LEFT, true); }
                if (keycode == Input.Keys.D) { dungeonMaster.setCameraMoving(Direction.RIGHT, true); }


                //entity movement
                if (dungeonMaster.getCurrentEntity() != null) {
                    if (keycode == Input.Keys.UP) { //move current entity up
                        dungeonMaster.getCurrentEntity().setMoving(Direction.UP, true);
                    } else if (keycode == Input.Keys.DOWN) {//move current entity down
                        dungeonMaster.getCurrentEntity().setMoving(Direction.DOWN, true);
                    } else if (keycode == Input.Keys.RIGHT) { //move current entity right
                        dungeonMaster.getCurrentEntity().setMoving(Direction.RIGHT, true);
                    } else if (keycode == Input.Keys.LEFT) { //move current entity left
                        dungeonMaster.getCurrentEntity().setMoving(Direction.LEFT, true);
                    }
                }
                return true;
            }

            public boolean keyUp(int keycode) {
                //camera movement
                if (keycode == Input.Keys.W) { dungeonMaster.setCameraMoving(Direction.UP, false); }
                if (keycode == Input.Keys.S) { dungeonMaster.setCameraMoving(Direction.DOWN, false); }
                if (keycode == Input.Keys.A) { dungeonMaster.setCameraMoving(Direction.LEFT, false); }
                if (keycode == Input.Keys.D) { dungeonMaster.setCameraMoving(Direction.RIGHT, false); }

                //stops entity movement if key is released
                if (dungeonMaster.getCurrentEntity() != null) {
                    if (keycode == Input.Keys.UP) {
                        dungeonMaster.getCurrentEntity().setMoving(Direction.UP, false);
                    } else if (keycode == Input.Keys.DOWN) {
                        dungeonMaster.getCurrentEntity().setMoving(Direction.DOWN, false);
                    } else if (keycode == Input.Keys.RIGHT) {
                        dungeonMaster.getCurrentEntity().setMoving(Direction.RIGHT, false);
                    } else if (keycode == Input.Keys.LEFT) {
                        dungeonMaster.getCurrentEntity().setMoving(Direction.LEFT, false);
                    }
                }

                return true;
            }

            //zoom camera on scroll
            public boolean scrolled(int amount) {
                camera.zoom += CZS * Gdx.graphics.getDeltaTime() * amount;
                camera.update();
                return true;
            }
        });

        //Command text input
        textListener = new Input.TextInputListener() {
            public void input(String text) {
                dungeonMaster.handleCommand(text);
            }

            public void canceled() {}
        };
    }
}