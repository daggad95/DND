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
public class DMController extends InputAdapter {
    private DungeonMaster dungeonMaster;
    private OrthographicCamera camera;
    private boolean control; //whether or not control is being held

    public DMController(DungeonMaster dm, OrthographicCamera c) {
        this.dungeonMaster = dm;
        this.camera = c;
        control = false;
    }

    public boolean touchDown (int x, int y, int pointer, int button) {
        //changing mouse coordinates to game coordinates
        camera.update();
        Vector3 mousePosition = new Vector3(x, y, 0);
        camera.unproject(mousePosition);
        x = (int) Math.floor(mousePosition.x);
        y = (int) Math.floor(mousePosition.y);

        //sets entity controlled by dm to clicked on entity
        //or links entity to current entity if control is held
        Vector2 pos = new Vector2((float)x, (float)y);

        if (dungeonMaster.entityAt(pos)) {
            if (control) {
                dungeonMaster.getCurrentEntity().togglePartyMember(dungeonMaster.getEntity(pos));
            } else {
                dungeonMaster.setCurrentEntity(dungeonMaster.getEntity(pos));
            }
        } else {
            dungeonMaster.setCurrentTile(pos);
        }
        return true;
    }

    public boolean keyDown(int keycode) {
        //control key usage
        if (keycode == Input.Keys.CONTROL_LEFT) {
            control = true;
        }

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

        //deletion
        if (keycode == Input.Keys.BACKSPACE) {
            dungeonMaster.deleteCurrentEntity();
        }
        return true;
    }

    public boolean keyUp(int keycode) {
        //control key usage
        if (keycode == Input.Keys.CONTROL_LEFT) {
            control = false;
        }

        //camera movement
        if (keycode == Input.Keys.W) { dungeonMaster.setCameraMoving(Direction.UP, false); }
        if (keycode == Input.Keys.S) { dungeonMaster.setCameraMoving(Direction.DOWN, false); }
        if (keycode == Input.Keys.A) { dungeonMaster.setCameraMoving(Direction.LEFT, false); }
        if (keycode == Input.Keys.D) { dungeonMaster.setCameraMoving(Direction.RIGHT, false); }

        //toggle fog of war
        if (keycode == Input.Keys.F) { dungeonMaster.toggleFOW(); }

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

    public boolean keyTyped(char character) {
        //setting current entity movement radius
        if (dungeonMaster.getCurrentEntity() != null) {
            if (character == '-') {
                dungeonMaster.getCurrentEntity().changeMoveRadius(-1);
            } else if (character == '=') {
                dungeonMaster.getCurrentEntity().changeMoveRadius(1);
            }
        }

        if (character == ' ') {
            dungeonMaster.getCommand();
        }
        return true;
    }

    //zoom camera on scroll
    public boolean scrolled(int amount) {
        dungeonMaster.zoomCamera(amount);
        return true;
    }
}
