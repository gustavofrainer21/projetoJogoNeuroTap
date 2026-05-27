package br.mackenzie;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;

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
    
    // Retângulos dos botões do Menu Principal e de Pausa
    Rectangle botaoIniciarRect;

    int score = 0;
    BitmapFont font;
    BitmapFont fontTitulo; 

    ArduinoInput arduinoInput;

    Texture coracaoTexture;
    int vidas = 3;

    private static final float WORLD_WIDTH = 800;
    private static final float WORLD_HEIGHT = 480;

    private enum GameState { MENU, JOGANDO, PAUSADO }
    private GameState currentState = GameState.MENU;

    // Variáveis do menu
    private int menuSelection = 0; // alternar entre opções do menu
    private float dificuldadeInicial = 1.7f; 

    @Override
    public void create() {
        dropTexture = new Texture("meteoro2.png");
        backgroundTexture = new Texture("background2.png");
        avatarTexture = new Texture("Avatar.png");
        coracaoTexture = new Texture("Coração.png");

        dropSprites = new Array<>();

        // Config avatar
        avatarSprite = new Sprite(avatarTexture);
        avatarSprite.setSize(100, 100);

        shapeRenderer = new ShapeRenderer();
        spriteBatch = new SpriteBatch();
        camera = new OrthographicCamera();
        
        viewport = new FitViewport(WORLD_WIDTH, WORLD_HEIGHT, camera);

        float espessura = 5f;
        linhaRect = new Rectangle(0, WORLD_HEIGHT / 2 - espessura / 2, WORLD_WIDTH, espessura);

        // Define a área de clique do botão Iniciar no Menu
        botaoIniciarRect = new Rectangle(WORLD_WIDTH / 2 - 100, WORLD_HEIGHT / 2 - 50, 200, 50);

        font = new BitmapFont();
        font.setColor(Color.RED);

        fontTitulo = new BitmapFont(); 
        fontTitulo.setColor(Color.CYAN);
        fontTitulo.getData().setScale(3.0f); 

        music = Gdx.audio.newMusic(Gdx.files.internal("music.mp3"));
        music.setLooping(true);
        music.setVolume(.2f);
        music.play();

        arduinoInput = new ArduinoInput();
        arduinoInput.conectar();
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
        float dropWidth = 70;
        float dropHeight = 70;

        Sprite dropSprite = new Sprite(dropTexture);
        dropSprite.setSize(dropWidth, dropHeight);
        dropSprite.setX(MathUtils.random(70f, WORLD_WIDTH - dropWidth));
        dropSprite.setY(WORLD_HEIGHT);
        dropSprites.add(dropSprite);
    }

    private void logic() {
        if (vidas <= 0) {
            if (Gdx.input.isKeyJustPressed(com.badlogic.gdx.Input.Keys.ENTER)) {
                reiniciarJogo();
                currentState = GameState.JOGANDO;
            }
            return;
        }

        // Menu principal
        if (currentState == GameState.MENU) {
            if (Gdx.input.isKeyJustPressed(com.badlogic.gdx.Input.Keys.ENTER)) {
                reiniciarJogo();
                currentState = GameState.JOGANDO;
            }
            
            if (Gdx.input.justTouched()) {
                Vector3 cliqueCoordenadas = new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0);
                viewport.unproject(cliqueCoordenadas);

                if (botaoIniciarRect.contains(cliqueCoordenadas.x, cliqueCoordenadas.y)) {
                    reiniciarJogo();
                    currentState = GameState.JOGANDO;
                }
            }
            return;
        }
        
        // Alternar Pausa
        if (Gdx.input.isKeyJustPressed(com.badlogic.gdx.Input.Keys.ESCAPE)) {
            if (currentState == GameState.JOGANDO) {
                currentState = GameState.PAUSADO;
            } else if (currentState == GameState.PAUSADO) {
                currentState = GameState.JOGANDO;
            }
        }
        
        // Jogo rodando
        if (currentState == GameState.JOGANDO) {
            float delta = Gdx.graphics.getDeltaTime();
            float yMeio = WORLD_HEIGHT / 2;
            float zonaInicio = yMeio - 100f;

            dropTimer += delta;
            if (dropTimer > dificuldadeInicial) {
                createDroplet();
                dropTimer = 0;
            }

            for (int i = dropSprites.size - 1; i >= 0; i--) {
                Sprite drop = dropSprites.get(i);
                drop.translateY(-200f * delta);

                if (drop.getY() + drop.getHeight() < 0) {
                    dropSprites.removeIndex(i);
                    continue;
                }

                if (drop.getY() < zonaInicio){
                    dropSprites.removeIndex(i);
                    vidas -= 1;
                }
            }

            if (Gdx.input.isKeyJustPressed(com.badlogic.gdx.Input.Keys.SPACE)
                || arduinoInput.consumirBotao()) {
                atirar();
            }
        }

        // Menu de pausa
        else if (currentState == GameState.PAUSADO) {
            if (Gdx.input.isKeyJustPressed(com.badlogic.gdx.Input.Keys.UP)) {
                menuSelection--;
                if (menuSelection < 0) menuSelection = 1; 
            }
            
            if (Gdx.input.isKeyJustPressed(com.badlogic.gdx.Input.Keys.DOWN)) {
                menuSelection++;
                if (menuSelection > 1) menuSelection = 0; 
            }

            if (Gdx.input.isKeyJustPressed(com.badlogic.gdx.Input.Keys.ENTER)) {
                if (menuSelection == 0) {
                    currentState = GameState.JOGANDO;
                } else if (menuSelection == 1) {
                    dificuldadeInicial = Math.max(0.3f, dificuldadeInicial - 0.3f); 
                    currentState = GameState.JOGANDO; 
                }
            }
        }
    }

    private void reiniciarJogo() {
        score = 0;
        vidas = 3;
        dropSprites.clear();
        dropTimer = 0;
        dificuldadeInicial = 1.7f;
        createDroplet();
    }

    private void atirar() {
        float yMeio = WORLD_HEIGHT / 2;
        float zonaInicio = yMeio - 30f; 
        float zonaFim = yMeio + 20f;    
        boolean acertouAlgum = false;

        for (int i = dropSprites.size - 1; i >= 0; i--) {
            Sprite drop = dropSprites.get(i);
            float dropCentroY = drop.getY() + drop.getHeight() / 2;

            if (dropCentroY >= zonaInicio && dropCentroY <= zonaFim) {
                dropSprites.removeIndex(i);
                score += 1;
                acertouAlgum = true;
                break; 
            }
        }
        if (!acertouAlgum) {
            vidas -= 1;
        }
    }

    private void draw() {
        float yMeio = WORLD_HEIGHT / 2;

        // Game Over
        if (vidas <= 0) {
            spriteBatch.begin();
            font.setColor(Color.RED);
            font.draw(spriteBatch, "GAME OVER", WORLD_WIDTH / 2 - 50, WORLD_HEIGHT / 2 + 30);
            font.draw(spriteBatch, "Pontuacao Final: " + score, WORLD_WIDTH / 2 - 65, WORLD_HEIGHT / 2 + 10);
            font.setColor(Color.WHITE);
            font.draw(spriteBatch, "Pressione ENTER para reiniciar", WORLD_WIDTH / 2 - 110, WORLD_HEIGHT / 2 - 10);
            spriteBatch.end();
            return;
        }

        // Menu Principal
        if (currentState == GameState.MENU) {
            spriteBatch.begin();
            spriteBatch.draw(backgroundTexture, 0, 0, WORLD_WIDTH, WORLD_HEIGHT);
            fontTitulo.draw(spriteBatch, "NeuroTap", WORLD_WIDTH / 2 - 90, WORLD_HEIGHT / 2 + 110);
            spriteBatch.end();

            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
            shapeRenderer.setColor(Color.CYAN);
            shapeRenderer.rect(botaoIniciarRect.x, botaoIniciarRect.y, botaoIniciarRect.width, botaoIniciarRect.height);
            shapeRenderer.end();

            // Texto do Botão
            spriteBatch.begin();
            font.setColor(Color.BLACK);
            font.draw(spriteBatch, "INICIAR", WORLD_WIDTH / 2 - 28, WORLD_HEIGHT / 2 - 20);
            spriteBatch.end();
            return;
        }

        // Jogando
        spriteBatch.begin();
        
        spriteBatch.draw(backgroundTexture, 0, 0, WORLD_WIDTH, WORLD_HEIGHT);

        for (Sprite dropSprite : dropSprites) {
            dropSprite.draw(spriteBatch);
        }

        avatarSprite.setSize(100, 100);
        avatarSprite.setX(-23);
        avatarSprite.setY(yMeio - avatarSprite.getHeight() / 2);
        avatarSprite.draw(spriteBatch);
        
        font.setColor(Color.RED);
        font.draw(spriteBatch, "Score: " + score, 700, 450);

        // Vidas
        float tamanhoCoracao = 32f;
        float espacamento = 10f;
        float xInicial = 20f; 
        float yPosicao = WORLD_HEIGHT - tamanhoCoracao - 20f; 

        for (int i = 0; i < vidas; i++) {
            float xPosicao = xInicial + i * (tamanhoCoracao + espacamento);
            spriteBatch.draw(coracaoTexture, xPosicao, yPosicao, tamanhoCoracao, tamanhoCoracao);
        }
        spriteBatch.end();

        // Linha de Acerto
        Gdx.gl.glLineWidth(3f);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(1, 0, 0, 1);
        shapeRenderer.line(50, yMeio, WORLD_WIDTH, yMeio);
        shapeRenderer.end();

        // Pausa
        if (currentState == GameState.PAUSADO) {
            Gdx.gl.glEnable(GL20.GL_BLEND); 
            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
            shapeRenderer.setColor(0, 0, 0, 0.6f); 
            shapeRenderer.rect(0, 0, WORLD_WIDTH, WORLD_HEIGHT);
            shapeRenderer.end();
            Gdx.gl.glDisable(GL20.GL_BLEND);

            spriteBatch.begin();
            font.setColor(Color.WHITE);
            font.draw(spriteBatch, "--- JOGO PAUSADO ---", WORLD_WIDTH / 2 - 75, WORLD_HEIGHT / 2 + 80);

            if (menuSelection == 0) {
                font.setColor(Color.YELLOW);
                font.draw(spriteBatch, "> RESUMIR <", WORLD_WIDTH / 2 - 45, WORLD_HEIGHT / 2 + 10);
            } else {
                font.setColor(Color.WHITE);
                font.draw(spriteBatch, "  RESUMIR  ", WORLD_WIDTH / 2 - 45, WORLD_HEIGHT / 2 + 10);
            }

            if (menuSelection == 1) {
                font.setColor(Color.YELLOW);
                font.draw(spriteBatch, "> AUMENTAR DIFICULDADE <", WORLD_WIDTH / 2 - 95, WORLD_HEIGHT / 2 - 30);
            } else {
                font.setColor(Color.WHITE);
                font.draw(spriteBatch, "  AUMENTAR DIFICULDADE  ", WORLD_WIDTH / 2 - 95, WORLD_HEIGHT / 2 - 30);
            }
            spriteBatch.end();
        }
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
        coracaoTexture.dispose();
        font.dispose();
        fontTitulo.dispose();
        arduinoInput.desconectar();
    }
}