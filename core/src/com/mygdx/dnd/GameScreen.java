package com.mygdx.dnd;

import com.badlogic.gdx.*;
import com.badlogic.gdx.controllers.Controllers;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.maps.MapRenderer;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Vector2;

import java.io.File;
import java.util.*;

public class GameScreen extends ScreenAdapter {
	SpriteBatch batch;
	Map<String, Texture> textures;
	Texture img;
	OrthographicCamera camera;

	List<Entity> players;

	DungeonMaster dm;
	DMController dmc;

	MapRenderer mapRenderer;
	TiledMap map;

    DND game;

    int loadStage; //current stage of loading
    int loadedPlayers; //number of players currently loaded

	public GameScreen(DND game) {
        loadStage = 0;
        loadedPlayers = 0;
        this.game = game;
		batch = new SpriteBatch();

		loadTextures();

        players = new LinkedList<Entity>();

		float w = Gdx.graphics.getWidth();
		float h = Gdx.graphics.getHeight();
		camera = new OrthographicCamera(40, 40 * (h/w));

		List<Entity> entities = new ArrayList<Entity>();

	}

	public void loadTextures() {
		File folder = new File("textures");
		textures = new HashMap<String, Texture>();

		for (File f : folder.listFiles()) {
			textures.put(f.getName().split("\\.")[0], new Texture(f.getPath()));
		}
	}

	public void render (float delta) {
        if (loadStage == 0) { //loading map
            String response = game.getPromptScreen().getResponse();

            if (response != "") {
                try {
                    map = new TmxMapLoader().load("maps/" + response + ".tmx");
                    mapRenderer = new OrthogonalTiledMapRenderer(map, 1 / 32f);
                    mapRenderer.setView(camera);
                    loadStage++;
                } catch (Exception e) {
                    System.out.println(e);
                    game.getPromptScreen().prompt("Invalid map name, please try again");
                }
            } else {
                game.getPromptScreen().prompt("Enter Map");
            }
        } else if (loadStage == 1) { //Loading players
                loadStage++;
                dm = new DungeonMaster(players, camera, textures, game);
                dmc = new DMController(dm, camera);
                Gdx.input.setInputProcessor(dmc);
        } else { //Normal Game loop
            Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

            camera.update();
            batch.setProjectionMatrix(camera.combined);

            mapRenderer.setView(camera);
            mapRenderer.render();


            batch.begin();
            dm.update(batch);
            batch.end();
        }
	}

	public void dispose () {
		batch.dispose();
	}

	public void show() {
        Gdx.input.setInputProcessor(dmc);
    }

}
