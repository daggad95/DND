package com.mygdx.dnd;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
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

    protected boolean[] moving; //used to determine which direction players is moving
    protected float timer; //used to control movement
    protected float lastMove; //time at which the entity last moved
    public static final float moveInterval = 0.25f; //time in seconds required before next move

    public Entity(Vector2 position, Vector2 size, Texture texture) {
        this.position = position;
        this.size = size;
        this.texture = texture;

        moving = new boolean[4];
        moving[Direction.RIGHT] = false;
        moving[Direction.LEFT] = false;
        moving[Direction.UP] = false;
        moving[Direction.DOWN] = false;

        lastMove = 0;
    }

    public void update(SpriteBatch batch) {
        batch.draw(texture, position.x, position.y, size.x, size.y);
        timer += Gdx.graphics.getDeltaTime();
        move();
    }

    public void setMoving(int direction, boolean value) {
        moving[direction] = value;
    }

    private void move() {
        if (timer > lastMove + moveInterval) {
            if (moving[Direction.LEFT]) {
                position.x -= 1;
                lastMove = timer;
            } else if (moving[Direction.RIGHT]) {
                position.x += 1;
                lastMove = timer;
            } else if (moving[Direction.UP]) {
                position.y += 1;
                lastMove = timer;
            } else if (moving[Direction.DOWN]) {
                position.y -= 1;
                lastMove = timer;
            }

        }
    }
}
