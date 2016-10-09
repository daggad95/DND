package com.mygdx.dnd;

import com.badlogic.gdx.controllers.Controller;
import com.badlogic.gdx.controllers.ControllerAdapter;
import com.badlogic.gdx.controllers.PovDirection;
import com.badlogic.gdx.controllers.mappings.Xbox;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;

/**
 * Created by David on 10/2/2016.
 */
public class PlayerController {
    private Controller controller;
    private Entity entity;

    public PlayerController(Controller c, Entity e) {
        controller = c;
        entity = e;

        controller.addListener(new ControllerAdapter() {

            //Left joystick controls
            public boolean axisMoved(Controller controller, int axisCode, float value) {
               if (controller.getAxis(Xbox360Pad.AXIS_LEFT_X) < -0.5) { //LEFT
                   entity.setMoving(Direction.LEFT, true);
               } if (controller.getAxis(Xbox360Pad.AXIS_LEFT_X) > 0.5) { //RIGHT
                   entity.setMoving(Direction.RIGHT, true);
               } else if (controller.getAxis(Xbox360Pad.AXIS_LEFT_Y) > 0.5) { //DOWN
                   entity.setMoving(Direction.DOWN, true);
               } else if (controller.getAxis(Xbox360Pad.AXIS_LEFT_Y) < -0.5) { //UP
                   entity.setMoving(Direction.UP, true);
               } else if(Math.abs(controller.getAxis(Xbox360Pad.AXIS_LEFT_X)) < 0.3 &&
                       Math.abs(controller.getAxis(Xbox360Pad.AXIS_LEFT_Y)) < 0.3) {
                   entity.setMoving(Direction.LEFT, false);
                   entity.setMoving(Direction.RIGHT, false);
                   entity.setMoving(Direction.UP, false);
                   entity.setMoving(Direction.DOWN, false);
               }

               return true;
            }

        });
    }
}
