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
import com.badlogic.gdx.math.Rectangle;

public class Main implements ApplicationListener {
    ShapeRenderer shapeRenderer;
    SpriteBatch spriteBatch;
    FitViewport viewport;
    OrthographicCamera camera;

    Texture backgroundTexture;
    Texture dropTexture;
    Texture avatarTexture;
    
    Array<Sprite> dropSprites;
    Sprite avatarSprite;
    
    float dropTimer;

    Music music;

    Rectangle linhaRect;

    private static final float WORLD_WIDTH = 800;
    private static final float WORLD_HEIGHT = 480;

    @Override
    public void create() {
        dropTexture = new Texture("meteoro.png");
        backgroundTexture = new Texture("background.jpg");
        avatarTexture = new Texture("Avatar.png");
        dropSprites = new Array<>();

        // Config avatar
        avatarSprite = new Sprite(avatarTexture);
        avatarSprite.setSize(60, 60);
        avatarSprite.setPosition(WORLD_WIDTH / 2 - 30, 20);

        shapeRenderer = new ShapeRenderer();
        spriteBatch = new SpriteBatch();
        camera = new OrthographicCamera();
        
        viewport = new FitViewport(WORLD_WIDTH, WORLD_HEIGHT, camera);
        createDroplet();

        music = Gdx.audio.newMusic(Gdx.files.internal("music.mp3"));
        music.setLooping(true);
        music.setVolume(.1f);
        //music.play();

        float espessura = 5f;
        linhaRect = new Rectangle(0, WORLD_HEIGHT / 2 - espessura / 2, WORLD_WIDTH, espessura);
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
        if (Gdx.input.isKeyJustPressed(com.badlogic.gdx.Input.Keys.SPACE)) {
            atirar();
        }
    }

    private void atirar() {
        float yMeio = WORLD_HEIGHT / 2;
        float zonaInicio = yMeio - 40f; // margem abaixo da linha
        float zonaFim = yMeio + 40f;    // margem acima da linha

        for (int i = dropSprites.size - 1; i >= 0; i--) {
            Sprite drop = dropSprites.get(i);
            float dropCentroY = drop.getY() + drop.getHeight() / 2;

            if (dropCentroY >= zonaInicio && dropCentroY <= zonaFim) {
                dropSprites.removeIndex(i);
            }
        }
    }

    private void draw() {
        float yMeio = WORLD_HEIGHT / 2;
        spriteBatch.begin();
        
        // Fundo
        spriteBatch.draw(backgroundTexture, 0, 0, WORLD_WIDTH, WORLD_HEIGHT);
        
        // Meteoros
        for(Sprite dropSprite : dropSprites){
            dropSprite.draw(spriteBatch);
        }
        
        // Avatar
        
        avatarSprite.setSize(100, 100);
        avatarSprite.setX(-23);
        avatarSprite.setY(yMeio - avatarSprite.getHeight() / 2);
        avatarSprite.draw(spriteBatch);
        
        spriteBatch.end();
        Gdx.gl.glLineWidth(3f);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(1, 0, 0, 1);
        
        shapeRenderer.line(50, yMeio, WORLD_WIDTH, yMeio);
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
        avatarTexture.dispose();
        music.dispose();
    }
}
