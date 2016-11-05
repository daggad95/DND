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
    private ControllerAdapter adapter;
    private Entity entity;

    public PlayerController(Controller c, Entity e) {
        controller = c;
        entity = e;

        adapter = new ControllerAdapter() {

            //Left && right joystick controls
            public boolean axisMoved(Controller controller, int axisCode, float value) {
                //entity movement
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
               
               //camera movement
               if (controller.getAxis(Xbox360Pad.AXIS_RIGHT_X) < -0.5) { //LEFT
                    entity.setCameraMoving(Direction.LEFT, true);
                } if (controller.getAxis(Xbox360Pad.AXIS_RIGHT_X) > 0.5) { //RIGHT
                    entity.setCameraMoving(Direction.RIGHT, true);
                } else if (controller.getAxis(Xbox360Pad.AXIS_RIGHT_Y) > 0.5) { //DOWN
                    entity.setCameraMoving(Direction.DOWN, true);
                } else if (controller.getAxis(Xbox360Pad.AXIS_RIGHT_Y) < -0.5) { //UP
                    entity.setCameraMoving(Direction.UP, true);
                } else if(Math.abs(controller.getAxis(Xbox360Pad.AXIS_RIGHT_X)) < 0.3 &&
                        Math.abs(controller.getAxis(Xbox360Pad.AXIS_RIGHT_Y)) < 0.3) {
                    entity.setCameraMoving(Direction.LEFT, false);
                    entity.setCameraMoving(Direction.RIGHT, false);
                    entity.setCameraMoving(Direction.UP, false);
                    entity.setCameraMoving(Direction.DOWN, false);
                }

               return true;
            }

            public boolean buttonDown(Controller controller, int buttonCode) {
                if (buttonCode == Xbox360Pad.BUTTON_A) {
                    entity.changeMoveRadius(1);
                } else if (buttonCode == Xbox360Pad.BUTTON_X) {
                    entity.changeMoveRadius(-1);
                }
                return true;
            }

        };

        controller.addListener(adapter);
    }

    public Entity getEntity() {
        return entity;
    }

    public void dispose() {
        controller.removeListener(adapter);
    }
}
