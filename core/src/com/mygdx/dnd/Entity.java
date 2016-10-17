package com.mygdx.dnd;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Select;

import java.awt.*;
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

    protected boolean[] moving; //used to determine which direction players is moving
    protected float timer; //used to control movement
    protected float lastMove; //time at which the entity last moved
    public static final float moveInterval = 0.25f; //time in seconds required before next move

    protected int moveRadius;

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


    public Entity(Vector2 position, Vector2 size, String textureName, Map<String, Texture> textures) {
        this.position = position;
        this.size = size;
        this.textures = textures;
        this.textureName = textureName;

        moveRadius = 0;
        font = new BitmapFont();
        randColor();

        moving = new boolean[4];
        moving[Direction.RIGHT] = false;
        moving[Direction.LEFT] = false;
        moving[Direction.UP] = false;
        moving[Direction.DOWN] = false;

        lastMove = 0;

        bgAlpha = DEFAULT_ALPHA;
        pulseDirection = Direction.UP;
        selected = false;

        dead = false;
        status = "";
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
    }

    private void drawMoveRadius(SpriteBatch batch) {
        batch.setColor(bgColor.r, bgColor.g, bgColor.b, 0.7f);

        for (int i = -moveRadius; i < moveRadius + 1; i++) {
            for (int j = -moveRadius; j < moveRadius + 1; j++) {
                if (Math.abs(i) + Math.abs(j) <= moveRadius && Math.abs(i) + Math.abs(j) != 0) {
                    batch.draw(textures.get(BG_TEXTURE), position.x + i, position.y + j, 1, 1);
                }
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

    private void move() {
        if (timer > lastMove + moveInterval) {
            if (moving[Direction.LEFT]) {
                position.x -= 1;
                lastMove = timer;

                if (moveRadius > 0) {
                    moveRadius--;
                }
            } else if (moving[Direction.RIGHT]) {
                position.x += 1;
                lastMove = timer;

                if (moveRadius > 0) {
                    moveRadius--;
                }
            } else if (moving[Direction.UP]) {
                position.y += 1;
                lastMove = timer;

                if (moveRadius > 0) {
                    moveRadius--;
                }
            } else if (moving[Direction.DOWN]) {
                position.y -= 1;
                lastMove = timer;

                if (moveRadius > 0) {
                    moveRadius--;
                }
            }

        }
    }

    public void changeMoveRadius(int deltaRadius) {
        if (!(deltaRadius < 0 && moveRadius == 0)) {
            moveRadius += deltaRadius;
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

    public Vector2 getSize() {
        return size;
    }

    public Vector2 getPosition() {
        return position;
    }

    public String getTextureName() {
        return textureName;
    }
}
