package br.mackenzie;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.math.MathUtils;

public class Main implements ApplicationListener {
    ShapeRenderer shapeRenderer;
    SpriteBatch spriteBatch;
    FitViewport viewport;
    OrthographicCamera camera;

    Texture backgroundTexture;
    Texture dropTexture;
    Texture spaceshipTexture;
    
    Array<Sprite> dropSprites;
    Sprite spaceshipSprite; // Adicionado: Sprite da nave
    
    float dropTimer;

    Music music;

    private static final float WORLD_WIDTH = 800;
    private static final float WORLD_HEIGHT = 480;

    @Override
    public void create() {
        dropTexture = new Texture("meteoro.png");
        backgroundTexture = new Texture("background.jpg");
        spaceshipTexture = new Texture("nave_L.png");
        dropSprites = new Array<>();

        // Configuração da Nave
        spaceshipSprite = new Sprite(spaceshipTexture);
        spaceshipSprite.setSize(60, 60);
        spaceshipSprite.setPosition(WORLD_WIDTH / 2 - 30, 20);

        shapeRenderer = new ShapeRenderer();
        spriteBatch = new SpriteBatch();
        camera = new OrthographicCamera();
        
        viewport = new FitViewport(WORLD_WIDTH, WORLD_HEIGHT, camera);
        createDroplet();

        music = Gdx.audio.newMusic(Gdx.files.internal("music.mp3"));
        music.setLooping(true);
        music.setVolume(.1f);
        music.play();
    }

    @Override
    public void render() {
        logic();
        
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        spriteBatch.setProjectionMatrix(camera.combined);
        shapeRenderer.setProjectionMatrix(camera.combined);

        draw();
    }

    private void createDroplet(){
        float dropWidth = 40;
        float dropHeight = 40;

        Sprite dropSprite = new Sprite(dropTexture);
        dropSprite.setSize(dropWidth, dropHeight);
        dropSprite.setX(MathUtils.random(0f, WORLD_WIDTH - dropWidth));
        dropSprite.setY(WORLD_HEIGHT);
        dropSprites.add(dropSprite);
    }

    private void logic() {
        float delta = Gdx.graphics.getDeltaTime(); 

        dropTimer += delta;
        if (dropTimer > 1.7f) {
            createDroplet();
            dropTimer = 0;
        }

        for (int i = 0; i < dropSprites.size; i++) {
            Sprite drop = dropSprites.get(i);
            drop.translateY(-200f * delta);

            if (drop.getY() + drop.getHeight() < 0) {
                dropSprites.removeValue(drop, true);
            }
        }
    }

    private void draw() {
        spriteBatch.begin();
        
        // Fundo
        spriteBatch.draw(backgroundTexture, 0, 0, WORLD_WIDTH, WORLD_HEIGHT);
        
        // Meteoros
        for(Sprite dropSprite : dropSprites){
            dropSprite.draw(spriteBatch);
        }
        
        // Nave (Desenhada por último para ficar na frente)
        spaceshipSprite.draw(spriteBatch);
        
        spriteBatch.end();

        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(1, 0, 0, 1);
        float yMeio = WORLD_HEIGHT / 2;
        shapeRenderer.line(0, yMeio, WORLD_WIDTH, yMeio);
        shapeRenderer.end();
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
    }

    @Override
    public void pause() { }

    @Override
    public void resume() { }

    @Override
    public void dispose() {
        shapeRenderer.dispose();
        backgroundTexture.dispose();
        spriteBatch.dispose();
        dropTexture.dispose();
        spaceshipTexture.dispose(); // Adicionado: Limpeza da textura da nave
        music.dispose();
    }
}