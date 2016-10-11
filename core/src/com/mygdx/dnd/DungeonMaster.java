package com.mygdx.dnd;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;

import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

/**
 * Created by David on 10/2/2016.
 * DM can spawn in entities and control all
 * entities on the field
 */
public class DungeonMaster {
    private Entity currentEntity;
    private List<Entity> entities;
    Map<String, Texture> textures;
    private OrthographicCamera camera;
    private boolean cameraMoving[]; //determines whether camera is moving in 4 directions
    private Vector2 lastClicked; // last clicked position by dm
    private DND game;

    private int actionState;

    public static final float CMR = 5; //camera movement rate in m/s

    public DungeonMaster(List<Entity> entities, OrthographicCamera camera, Map<String, Texture> textures, DND game) {
        this.entities = entities;
        this.camera = camera;
        this.textures = textures;
        this.game = game;
        actionState = 0;

        cameraMoving = new boolean[4];
        cameraMoving[Direction.UP] = false;
        cameraMoving[Direction.DOWN] = false;
        cameraMoving[Direction.LEFT] = false;
        cameraMoving[Direction.RIGHT] = false;
    }

    public boolean entityAt(Vector2 p) {
        for (Entity e : entities) {
            for (int i = 0; i < e.size.x; i++) {
                for (int j = 0; j < e.size.y; j++) {
                    if (p.epsilonEquals(e.position.x + i, e.position.y + j, 0.1f)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public Entity getEntity(Vector2 p) {
        for (Entity e : entities) {
            for (int i = 0; i < e.size.x; i++) {
                for (int j = 0; j < e.size.y; j++) {
                    if (p.epsilonEquals(e.position.x + i, e.position.y + j, 0.1f)) {
                        return e;
                    }
                }
            }
        }
        return null;
    }

    public void setCameraMoving(int direction, boolean value) {
        cameraMoving[direction] = value;
    }
    public void setCurrentEntity(Entity e) {
        currentEntity = e;
    }
    public void setLastClicked(Vector2 p) { lastClicked = p; }
    public Entity getCurrentEntity() {
        return currentEntity;
    }


    public void spawnEntity(String command) {
        if (command != "") {
            try {
                StringTokenizer tk = new StringTokenizer(command);
                //defaults to 1x1
                float width = 1;
                float height = 1;
                int numEntities = 1;

                String name = tk.nextToken();


                if (tk.hasMoreTokens()) {
                    numEntities = Integer.parseInt(tk.nextToken());
                }

                if(tk.hasMoreTokens()){
                    width = Float.parseFloat(tk.nextToken());
                }
                if(tk.hasMoreTokens()){
                    height = Float.parseFloat(tk.nextToken());
                }
                if (textures.get(name) == null) {
                    throw new Exception();
                }

                for(int x = 0; x < numEntities; x++){
                    lastClicked.add(1, 0);
                    Entity e = new Entity(new Vector2(lastClicked), new Vector2(width, height), textures.get(name), textures.get("whitebox"));
                    entities.add(e);
                }
                setCurrentEntity(entities.get(entities.size() - 1));
            } catch (Exception e) {
                System.out.println(e);
                System.out.println("invalid command");
            }
        }
    }


    public void update(SpriteBatch batch) {
        if (actionState == States.NORMAL) {
            for (Entity e : entities) {
                e.update(batch);
            }

            //camera movement
            if (cameraMoving[Direction.RIGHT]) { camera.translate(CMR*Gdx.graphics.getDeltaTime(), 0, 0); }
            if (cameraMoving[Direction.LEFT]) { camera.translate(-CMR*Gdx.graphics.getDeltaTime(), 0, 0); }
            if (cameraMoving[Direction.UP]) { camera.translate(0, CMR*Gdx.graphics.getDeltaTime(), 0); }
            if (cameraMoving[Direction.DOWN]) { camera.translate(0, -CMR*Gdx.graphics.getDeltaTime(), 0); }
            camera.update();
        } else if (actionState == States.SPAWN_ENTITY) {
            spawnEntity(game.getPromptScreen().getResponse());
            setActionState(States.NORMAL);
        }
    }

    public void setActionState(int state) {
        actionState = state;
    }

    public DND getGame() {
        return game;
    }

    public Map<String, Texture> getTextures() {
        return textures;
    }

    public void deleteCurrentEntity() {
        entities.remove(entities.indexOf(currentEntity));
    }
}
