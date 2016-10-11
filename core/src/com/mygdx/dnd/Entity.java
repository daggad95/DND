package com.mygdx.dnd;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;

/**
 * Created by David on 10/2/2016.
 *
 */
public class Entity {
    protected Vector2 position;
    protected Vector2 size;
    protected Texture texture;
    protected Texture background;
    BitmapFont font;

    protected boolean[] moving; //used to determine which direction players is moving
    protected float timer; //used to control movement
    protected float lastMove; //time at which the entity last moved
    public static final float moveInterval = 0.25f; //time in seconds required before next move

    protected int moveRadius;

    public Entity(Vector2 position, Vector2 size, Texture texture, Texture background) {
        this.position = position;
        this.size = size;
        this.texture = texture;
        this.background = background;

        moveRadius = 0;
        font = new BitmapFont();

        moving = new boolean[4];
        moving[Direction.RIGHT] = false;
        moving[Direction.LEFT] = false;
        moving[Direction.UP] = false;
        moving[Direction.DOWN] = false;

        lastMove = 0;
    }

    public void update(SpriteBatch batch) {
        Color c = batch.getColor();

        batch.setColor(c.r, c.g, c.b, 0.3f);
        batch.draw(background, position.x, position.y, size.x, size.y);

        batch.setColor(c.r, c.g, c.b, 1f);
        batch.draw(texture, position.x, position.y, size.x, size.y);

        if (moveRadius > 0) {
            drawMoveRadius(batch);
        }

        timer += Gdx.graphics.getDeltaTime();
        move();
    }

    private void drawMoveRadius(SpriteBatch batch) {
        Color c = batch.getColor();
        batch.setColor(c.r, c.g, c.b, 0.7f);

        for (int i = -moveRadius; i < moveRadius + 1; i++) {
            for (int j = -moveRadius; j < moveRadius + 1; j++) {
                if (Math.abs(i) + Math.abs(j) <= moveRadius && Math.abs(i) + Math.abs(j) != 0) {
                    batch.draw(background, position.x + i, position.y + j, 1, 1);
                }
            }
        }
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
}
