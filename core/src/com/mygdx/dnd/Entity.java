package com.mygdx.dnd;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Select;
import com.sun.org.apache.xpath.internal.operations.Or;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * Created by David on 10/2/2016.
 *
 */
public class Entity {
    protected Vector2 position;
    protected Vector2 size;
    protected BitmapFont font;
    protected Color bgColor;
    protected ArrayList<Vector2> wallPositions;
    protected HashMap<Vector2, Boolean> viewedTiles;

    protected boolean[] moving; //used to determine which direction players is moving
    protected boolean[] cameraMoving;
    protected float timer; //used to control movement
    protected float lastMove; //time at which the entity last moved
    public static final float moveInterval = 0.25f; //time in seconds required before next move
    public static final float CMR = 15; //camera movement rate in m/s
    public static float CZS = 1; //speed at which the camera zooms

    protected int moveRadius;
    protected Vector2 moveRadiusCenter;

    //pulse animation stuff
    protected static final float DEFAULT_ALPHA = 0.3f;
    protected static final float MIN_PULSE = 0.3f;
    protected static final float MAX_PULSE = 0.7f;
    protected static final float PULSE_RATE = 0.8f;
    protected static final float SELECT_BOX_RADIUS = 0.1f;
    protected float bgAlpha;
    protected int pulseDirection;
    protected boolean selected;

    //texture stuff
    protected Map<String, Texture> textures;
    protected String textureName;
    protected static final String BG_TEXTURE = "whitebox"; //name of background texture
    protected static final String DEAD_TEXTURE = "dead"; // name of dead overlay texture

    //stats
    protected boolean dead;
    protected String status;
    protected boolean player;
    protected boolean visible;
    protected int viewRange; //number of tiles the player can see


    public Entity(Vector2 position, Vector2 size, String textureName, Map<String, Texture> textures, ArrayList<Vector2> wallPositions) {
        this.position = position;
        this.size = size;
        this.textures = textures;
        this.textureName = textureName;
        this.wallPositions = wallPositions;

        moveRadius = 0;
        font = new BitmapFont();
        randColor();

        moving = new boolean[4];
        moving[Direction.RIGHT] = false;
        moving[Direction.LEFT] = false;
        moving[Direction.UP] = false;
        moving[Direction.DOWN] = false;

        cameraMoving = new boolean[6];
        cameraMoving[Direction.RIGHT] = false;
        cameraMoving[Direction.LEFT] = false;
        cameraMoving[Direction.UP] = false;
        cameraMoving[Direction.DOWN] = false;
        cameraMoving[Direction.IN] = false;
        cameraMoving[Direction.OUT] = false;

        lastMove = 0;

        bgAlpha = DEFAULT_ALPHA;
        pulseDirection = Direction.UP;
        selected = false;

        dead = false;
        status = "";
        player = false;
        viewRange = 0;
        visible = true;
        viewedTiles = new HashMap<Vector2, Boolean>();

        setFOV();
    }

    public void update(SpriteBatch batch, OrthographicCamera camera, OrthographicCamera hudCamera) {
        if (selected) {
            pulse();
        }

        //drawing background box
        batch.setColor(bgColor.r, bgColor.g, bgColor.b, bgAlpha);
        if (selected) {
            batch.draw(textures.get(BG_TEXTURE), position.x - SELECT_BOX_RADIUS, position.y - SELECT_BOX_RADIUS,
                    size.x + SELECT_BOX_RADIUS * 2, size.y + SELECT_BOX_RADIUS * 2);
        } else {
            batch.draw(textures.get(BG_TEXTURE), position.x, position.y, size.x, size.y);
        }

        //drawing sprite
        batch.setColor(bgColor.r, bgColor.g, bgColor.b, 1f);
        batch.draw(textures.get(textureName), position.x, position.y, size.x, size.y);

        if (dead) {
            batch.setColor(Color.WHITE);
            batch.draw(textures.get(DEAD_TEXTURE), position.x, position.y, size.x, size.y);
        }

        if (moveRadius > 0) {
            drawMoveRadius(batch);
        }

        if (!status.equals("")) {
            drawStatus(batch, camera, hudCamera);
        }

        timer += Gdx.graphics.getDeltaTime();
        move();
        moveCamera(camera, hudCamera);
    }

    private void setMoveRadius(int moveRange, int direction, Vector2 pos, boolean wasDiagonal, ArrayList path) {
        if (!path.contains(pos) || pos.epsilonEquals(moveRadiusCenter, 0.9f)) {
            if (moveRange >= 0) {
                path.add(pos);

                if (direction == Direction.UP_RIGHT) {
                    if (wasDiagonal) {
                        setMoveRadius(moveRange - 2, Direction.UP_RIGHT, new Vector2(pos).add(1, 1), false, path);
                    } else {
                        setMoveRadius(moveRange - 1, Direction.UP_RIGHT, new Vector2(pos).add(1, 1), true, path);
                    }
                    setMoveRadius(moveRange - 1, Direction.UP_RIGHT, new Vector2(pos).add(1, 0), wasDiagonal, path);
                    setMoveRadius(moveRange - 1, Direction.UP_RIGHT, new Vector2(pos).add(0, 1), wasDiagonal, path);
                }
                if (direction == Direction.UP_LEFT) {
                    if (wasDiagonal) {
                        setMoveRadius(moveRange - 2, Direction.UP_LEFT, new Vector2(pos).add(-1, 1), false, path);
                    } else {
                        setMoveRadius(moveRange - 1, Direction.UP_LEFT, new Vector2(pos).add(-1, 1), true, path);
                    }
                    setMoveRadius(moveRange - 1, Direction.UP_LEFT, new Vector2(pos).add(-1, 0), wasDiagonal, path);
                    setMoveRadius(moveRange - 1, Direction.UP_LEFT, new Vector2(pos).add(0, 1), wasDiagonal, path);

                }
                if (direction == Direction.DOWN_LEFT) {
                    if (wasDiagonal) {
                        setMoveRadius(moveRange - 2, Direction.DOWN_LEFT, new Vector2(pos).add(-1, -1), false, path);
                    } else {
                        setMoveRadius(moveRange - 1, Direction.DOWN_LEFT, new Vector2(pos).add(-1, -1), true, path);
                    }
                    setMoveRadius(moveRange - 1, Direction.DOWN_LEFT, new Vector2(pos).add(-1, 0), wasDiagonal, path);
                    setMoveRadius(moveRange - 1, Direction.DOWN_LEFT, new Vector2(pos).add(0, -1), wasDiagonal, path);

                }
                if (direction == Direction.DOWN_RIGHT) {
                    if (wasDiagonal) {
                        setMoveRadius(moveRange - 2, Direction.DOWN_RIGHT, new Vector2(pos).add(1, -1), false, path);
                    } else {
                        setMoveRadius(moveRange - 1, Direction.DOWN_RIGHT, new Vector2(pos).add(1, -1), true, path);
                    }
                    setMoveRadius(moveRange - 1, Direction.DOWN_RIGHT, new Vector2(pos).add(1, 0), wasDiagonal, path);
                    setMoveRadius(moveRange - 1, Direction.DOWN_RIGHT, new Vector2(pos).add(0, -1), wasDiagonal, path);
                }
            }
        }
    }

    private void drawMoveRadius(SpriteBatch batch) {
        batch.setColor(bgColor.r, bgColor.g, bgColor.b, 0.7f);

        ArrayList<Vector2> path = new ArrayList<Vector2>();

        setMoveRadius(moveRadius, Direction.UP_RIGHT, moveRadiusCenter, false, path);
        setMoveRadius(moveRadius, Direction.DOWN_RIGHT, moveRadiusCenter, false, path);
        setMoveRadius(moveRadius, Direction.UP_LEFT, moveRadiusCenter, false, path);
        setMoveRadius(moveRadius, Direction.DOWN_LEFT, moveRadiusCenter, false, path);
        
        for (Vector2 pos : path) {
            if (!pos.epsilonEquals(moveRadiusCenter, 0.9f)) {
                batch.draw(textures.get(BG_TEXTURE), pos.x, pos.y, 1, 1);
            }
        }

    }

    private void drawStatus(SpriteBatch batch, OrthographicCamera camera, OrthographicCamera hudCamera) {
        BitmapFont font = new BitmapFont();

        //conversion ratios for hud
        float wc = hudCamera.viewportWidth / camera.viewportWidth;
        float hc = hudCamera.viewportHeight / camera.viewportHeight;

        hudCamera.position.x = camera.position.x * wc;
        hudCamera.position.y = camera.position.y * hc;
        hudCamera.update();

        batch.setProjectionMatrix(hudCamera.combined);
        font.draw(batch, status, position.x * wc, position.y * hc);
        batch.setProjectionMatrix(camera.combined);

        font.dispose();
    }

    public void setMoving(int direction, boolean value) {
        moving[direction] = value;
    }

    public void setCameraMoving(int direction, boolean value) {
        cameraMoving[direction] = value;
    }


    private void move() {
        if (timer > lastMove + moveInterval) {
            if (moving[Direction.LEFT]) {
                position.x -= 1;
                lastMove = timer;
                setFOV();
            } if (moving[Direction.RIGHT]) {
                position.x += 1;
                lastMove = timer;
                setFOV();
            } if (moving[Direction.UP]) {
                position.y += 1;
                lastMove = timer;
                setFOV();
            } if (moving[Direction.DOWN]) {
                position.y -= 1;
                lastMove = timer;
                setFOV();
            }
        }
    }

    private void moveCamera(OrthographicCamera camera, OrthographicCamera hudCamera) {
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
        if (cameraMoving[Direction.IN]) {
            camera.zoom += CZS * Gdx.graphics.getDeltaTime();
            hudCamera.zoom += CZS * Gdx.graphics.getDeltaTime();
        }
        if (cameraMoving[Direction.OUT]) {
            camera.zoom -= CZS * Gdx.graphics.getDeltaTime();
            hudCamera.zoom -= CZS * Gdx.graphics.getDeltaTime();
        }
        camera.update();
    }

    public void changeMoveRadius(int deltaRadius) {
        if (!(deltaRadius < 0 && moveRadius == 0)) {
            if (moveRadius == 0) {
                moveRadiusCenter = new Vector2(position);

            }
            moveRadius += deltaRadius;
            status = Integer.toString(moveRadius * 5) + " ft";
        }
        if (moveRadius == 0){
            status = "";
        }
    }

    public void pulse() {
        if (pulseDirection == Direction.DOWN) {
            if (Math.abs(MIN_PULSE - bgAlpha) > 0.01 && bgAlpha > MIN_PULSE) {
                bgAlpha -= PULSE_RATE * Gdx.graphics.getDeltaTime();
            } else {
                pulseDirection = Direction.UP;
            }
        } else {
            if (Math.abs(MAX_PULSE - bgAlpha) > 0.01 && bgAlpha < MAX_PULSE) {
                bgAlpha += PULSE_RATE * Gdx.graphics.getDeltaTime();
            } else {
                pulseDirection = Direction.DOWN;
            }
        }
    }

    public void setBgColor(Color c) {
        bgColor = c;
    }

    public void setSelected(boolean value) {
        selected = value;

        if (!selected) {
            bgAlpha = DEFAULT_ALPHA;
        }
    }

    public void setDead(boolean deathState) {
        dead = deathState;
    }

    //sets entity to a random color
    public void randColor() {
        Random rand = new Random();
        bgColor = new Color(rand.nextFloat(), rand.nextFloat(), rand.nextFloat(), (float)1);
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setPlayer(boolean status) {
        player = status;
    }

    public void setViewRange(int vr) {
        this.viewRange = vr;
    }

    public void toggleVisibility() {
        visible = !visible;
    }

    public Vector2 getSize() {
        return size;
    }

    public Vector2 getPosition() {
        return position;
    }

    public String getTextureName() {
        return textureName;
    }


    public int getMoveRadius() { return moveRadius; }

    public boolean isPlayer() {
        return player;
    }


    private boolean viewBlocked(Vector2 pos) {
        for (Vector2 wallPos : wallPositions) {
            if (pos.epsilonEquals(wallPos, 0.9f)) {
                return true;
            }
        }

        return false;
    }

    private void checkFOV(Vector2 endPoint) {
        float dx = endPoint.x - position.x;
        float dy = endPoint.y - position.y;

        float steps;
        if (Math.abs(dx) > Math.abs(dy)) {
            steps = Math.abs(dx);
        } else {
            steps = Math.abs(dy);
        }

        float deltaX = dx / steps;
        float deltaY = dy / steps;

        Vector2 checkPoint = new Vector2(position);
        for (int j = 0; j < steps; j++) {
            if (!viewedTiles.containsKey(new Vector2((int)checkPoint.x, (int)checkPoint.y))) {
                viewedTiles.put(new Vector2((int)checkPoint.x, (int)checkPoint.y), true);
            }

            if (viewBlocked(checkPoint)) {
                break;
            }
            checkPoint.add(deltaX, deltaY);
        }
    }


    public void setFOV() {
        viewedTiles.clear();

        /*for (int i = -viewRange; i <= viewRange; i++) {
            Vector2 endPointTop = new Vector2(position).add(i, viewRange);
            Vector2 endPointBottom = new Vector2(position).add(i, -viewRange);
            Vector2 endPointLeft = new Vector2(position).add(-viewRange, i);
            Vector2 endPointRight = new Vector2(position).add(viewRange, i);

            checkFOV(endPointTop);
            checkFOV(endPointBottom);
            checkFOV(endPointLeft);
            checkFOV(endPointRight);
        }*/

        for (int i = -viewRange; i <= viewRange; i++) {
            for (int j = -viewRange; j <= viewRange; j++) {
                Vector2 endPoint = new Vector2(position).add(i, j);
                checkFOV(endPoint);
            }
        }
    }

    public HashMap<Vector2, Boolean> getFOV() {
        if (visible) {
            return viewedTiles;
        } else {
            return null;
        }
    }
}
