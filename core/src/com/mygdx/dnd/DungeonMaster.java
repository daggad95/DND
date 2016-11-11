package com.mygdx.dnd;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.controllers.Controllers;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.FPSLogger;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.math.Vector2;

import java.io.File;
import java.util.*;

/**
 * Created by David on 10/2/2016.
 * DM can spawn in entities and control all
 * entities on the field
 */
public class DungeonMaster {
    private ArrayList<Vector2> wallPositions;
    private TiledMap map;
    private Entity currentEntity;
    private List<Entity> entities;
    List<PlayerController> controllers;
    Map<String, Texture> textures;
    private OrthographicCamera camera;
    private OrthographicCamera hudCamera;
    private boolean cameraMoving[]; //determines whether camera is moving in 4 directions
    private Vector2 lastClicked; // last clicked position by dm
    private DND game;
    private boolean fow; //determines if fow is being drawn
    FPSLogger logger;

    protected static final String FOG_TEXTURE = "whitebox"; //name of background texture

    private int actionState;

    public static final float CMR = 15; //camera movement rate in m/s
    public static float CZS = 3; //speed at which the camera zooms

    public DungeonMaster(List<Entity> entities, OrthographicCamera camera, OrthographicCamera hudCamera, Map<String, Texture> textures, DND game, TiledMap map) {
        this.entities = entities;
        this.camera = camera;
        this.hudCamera = hudCamera;
        this.textures = textures;
        this.game = game;
        this.map = map;
        logger = new FPSLogger();
        actionState = 0;
        controllers = new ArrayList<PlayerController>();

        cameraMoving = new boolean[4];
        cameraMoving[Direction.UP] = false;
        cameraMoving[Direction.DOWN] = false;
        cameraMoving[Direction.LEFT] = false;
        cameraMoving[Direction.RIGHT] = false;

        //loading position of walls in map to be used for line of sight
        TiledMapTileLayer wallLayer = (TiledMapTileLayer) map.getLayers().get("Walls");
        wallPositions = new ArrayList<Vector2>();
        if (wallLayer != null) {
            for (int i = 0; i < wallLayer.getWidth(); i++) {
                for (int j = 0; j < wallLayer.getWidth(); j++) {
                    if (wallLayer.getCell(i, j) != null) {
                        wallPositions.add(new Vector2(i, j));
                    }
                }
            }
        }

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
        if (currentEntity != null) {
            currentEntity.setSelected(false);
        }
        currentEntity = e;
        currentEntity.setSelected(true);
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
                    Entity e = new Entity(new Vector2(lastClicked), new Vector2(width, height), name, textures, wallPositions);
                    entities.add(e);
                }
                setCurrentEntity(entities.get(entities.size() - 1));
            } catch (Exception e) {
                System.out.println(e);
                System.out.println("invalid command");
            }
        }
    }

    private void handleCommand(String command) {
        if (command != "") {
            try {
                StringTokenizer tk = new StringTokenizer(command);

                String mainCommand = tk.nextToken();

                if (mainCommand.equals("setplayer")) {
                    if (tk.hasMoreTokens()) {
                        int playerNum = Integer.parseInt(tk.nextToken());
                        setPlayer(playerNum);
                    }
                } else if (mainCommand.equals("unsetplayer")) {
                    unsetPlayer();
                } else if (mainCommand.equals("kill")) {
                    setCEDeathState(true);
                } else if (mainCommand.equals("unkill")) {
                    setCEDeathState(false);
                } else if (mainCommand.equals("recolor")) {
                    recolorCE();
                } else if (mainCommand.equals("status")) {
                    if (tk.hasMoreTokens()) {
                        String status = tk.nextToken();
                        setStatus(status);
                    } else {
                        setStatus("");
                    }
                } else if (mainCommand.equals("save")) {
                    if (tk.hasMoreTokens()) {
                        String fileName = tk.nextToken();
                        save(fileName);
                    }
                } else if (mainCommand.equals("load")) {
                    if (tk.hasMoreTokens()) {
                        String fileName = tk.nextToken();
                        load(fileName);
                    }
                } else if (mainCommand.equals("viewrange")) {
                    if (tk.hasMoreTokens()) {
                        int range = Integer.parseInt(tk.nextToken());
                        setViewRange(range);
                    }
                }


            } catch (Exception e) {
                System.out.println(e);
            }
        }
    }

    //sets view range of CE and updates CE's field of view
    private void setViewRange(int range) {
        currentEntity.setViewRange(range);
        currentEntity.setFOV();
    }

    //Turns the current entity into a
    //controllable player;
    private void setPlayer(int playerNum) {
        if (Controllers.getControllers().size >= playerNum) {
            controllers.add(new PlayerController(Controllers.getControllers().get(playerNum - 1), currentEntity));
            currentEntity.setBgColor(Color.WHITE);
            currentEntity.setPlayer(true);
        }
    }

    private void unsetPlayer() {
        for (PlayerController pc : controllers) {
            if (pc.getEntity() == currentEntity) {
                pc.dispose();
                controllers.remove(controllers.indexOf(pc));
                currentEntity.randColor();
                currentEntity.setPlayer(false);
                return;
            }
        }
    }
    private void setCEDeathState(boolean state) {
        currentEntity.setDead(state);
    }

    private void recolorCE() {
        currentEntity.randColor();
    }

    private void setStatus(String status) { currentEntity.setStatus(status); }

    private void save(String fileName) {
        FileHandle fh = Gdx.files.local("saves/" + fileName + ".save");

        //clearing file
        fh.writeString("", false);

        for (Entity e : entities) {
            String line = e.getTextureName() + ";" + e.getPosition().x + ";"
                    + e.getPosition().y + ";" + e.getSize().x + ";" + e.getSize().y + "\n";
            fh.writeString(line, true);
        }
    }

    private void load(String fileName) {
        FileHandle fh = Gdx.files.local("saves/" + fileName + ".save");

        StringTokenizer lineTokenizer = new StringTokenizer(fh.readString(), "\n");

        while(lineTokenizer.hasMoreTokens()) {
            String line = lineTokenizer.nextToken();
            StringTokenizer entityTokenizer = new StringTokenizer(line, ";");

            String texture = entityTokenizer.nextToken();
            float x = Float.parseFloat(entityTokenizer.nextToken());
            float y = Float.parseFloat(entityTokenizer.nextToken());
            float w = Float.parseFloat(entityTokenizer.nextToken());
            float h = Float.parseFloat(entityTokenizer.nextToken());

            entities.add(new Entity(new Vector2(x, y), new Vector2(w, h), texture, textures, wallPositions));
        }
    }

    public void toggleFOW() {
        fow = !fow;
    }

    public void update(SpriteBatch batch) {
        logger.log();

        if (actionState == States.NORMAL) {
            for (Entity e : entities) {
                e.update(batch, camera, hudCamera);
            }

            if (fow) {
                drawFOW(batch);
            }

            //conversion ratios for hud
            float wc = hudCamera.viewportWidth / camera.viewportWidth;
            float hc = hudCamera.viewportHeight / camera.viewportHeight;

            //camera movement
            if (cameraMoving[Direction.RIGHT]) {
                camera.translate(CMR*Gdx.graphics.getDeltaTime() * camera.zoom, 0, 0);
            }
            if (cameraMoving[Direction.LEFT]) {
                camera.translate(-CMR*Gdx.graphics.getDeltaTime() * camera.zoom, 0, 0);
            }
            if (cameraMoving[Direction.UP]) {
                camera.translate(0, CMR*Gdx.graphics.getDeltaTime() * camera.zoom, 0);
            }
            if (cameraMoving[Direction.DOWN]) {
                camera.translate(0, -CMR*Gdx.graphics.getDeltaTime() * camera.zoom, 0);
            }
            camera.update();
        } else if (actionState == States.SPAWN_ENTITY) {
            spawnEntity(game.getPromptScreen().getResponse());
            setActionState(States.NORMAL);
        } else if (actionState == States.GET_COMMAND) {
            handleCommand(game.getPromptScreen().getResponse());
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

    public void getCommand() {
        game.getPromptScreen().prompt("Enter Command");
        actionState = States.GET_COMMAND;
    }

    public void zoomCamera(int amount) {
        camera.zoom += CZS * Gdx.graphics.getDeltaTime() * amount;
        hudCamera.zoom += CZS * Gdx.graphics.getDeltaTime() * amount;
        camera.update();
    }

    //compiles a map of all players field of views
    private HashMap<Vector2, Boolean>  getPlayersFOV() {
        HashMap<Vector2, Boolean> viewedTiles = new HashMap<Vector2, Boolean>();

        for (Entity e : entities) {
            //if (e.isPlayer()) {
                if (e.getFOV() != null) {
                    for (Vector2 tilePos : e.getFOV().keySet()) {
                        viewedTiles.put(tilePos, e.getFOV().get(tilePos));
                    }
                }
            //}
        }
        return viewedTiles;
    }

    //draws fog of war based on players line of sight
    private void drawFOW(SpriteBatch batch) {
        HashMap<Vector2, Boolean> viewedTiles = getPlayersFOV();

        float width = camera.viewportWidth*camera.zoom;
        float height = camera.viewportHeight*camera.zoom;
        float x = camera.position.x;
        float y = camera.position.y;

        batch.setColor(Color.BLACK);

        for (int i = (int)(x - width / 1.5); i < (int)(x + width / 1.5); i++) {
            for (int j = (int)(y- height / 1.5); j < (int)(y + height / 1.5); j++) {
                if (!viewedTiles.containsKey(new Vector2(i, j))) {
                    batch.draw(textures.get(FOG_TEXTURE), i, j, 1, 1);
                }
            }
        }
    }
}
