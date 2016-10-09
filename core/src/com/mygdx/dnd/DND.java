package com.mygdx.dnd;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DND extends ApplicationAdapter {
	SpriteBatch batch;
	Map<String, Texture> textures;
	Texture img;
	OrthographicCamera camera;

	Entity player1;
	Entity player2;
	Entity player3;
	Entity player4;

	PlayerController controller1;
	PlayerController controller2;
	PlayerController controller3;
	PlayerController controller4;

	DungeonMaster dm;
	DMController dmc;

	MapRenderer mapRenderer;
	TiledMap map;

	public void create () {
		batch = new SpriteBatch();

		loadTextures();

		player1 = new Entity(new Vector2(0, 0), new Vector2(1, 1), textures.get("chase"));
		player2 = new Entity(new Vector2(1, 0), new Vector2(1, 1), textures.get("jono"));
		player3 = new Entity(new Vector2(2, 0), new Vector2(1, 1), textures.get("evan"));
		player4 = new Entity(new Vector2(3, 0), new Vector2(1, 1), textures.get("david"));
		controller1 = new PlayerController(Controllers.getControllers().first(), player1);
		controller2 = new PlayerController(Controllers.getControllers().get(1), player2);
		controller3 = new PlayerController(Controllers.getControllers().get(2), player3);
		controller4 = new PlayerController(Controllers.getControllers().get(3), player4);



		float w = Gdx.graphics.getWidth();
		float h = Gdx.graphics.getHeight();
		camera = new OrthographicCamera(40, 40 * (h/w));

		map = new TmxMapLoader().load("grass40.tmx");
		mapRenderer = new OrthogonalTiledMapRenderer(map, 1 / 32f);
		mapRenderer.setView(camera);

		List<Entity> entities = new ArrayList<Entity>();
		entities.add(player1);
		entities.add(player2);
		entities.add(player3);
		entities.add(player4);
		dm = new DungeonMaster(entities, camera, textures);
		dmc = new DMController(dm, camera);
	}

	public void loadTextures() {
		File folder = new File("textures");
		textures = new HashMap<String, Texture>();

		for (File f : folder.listFiles()) {
			textures.put(f.getName().split("\\.")[0], new Texture(f.getPath()));
		}
	}

	public void render () {
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

		camera.update();
		batch.setProjectionMatrix(camera.combined);

		mapRenderer.setView(camera);
		mapRenderer.render();


		batch.begin();
		dm.update(batch);
		batch.end();
	}

	public void dispose () {
		batch.dispose();
	}
}
