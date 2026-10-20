package si.um.feri.manfreda.oop;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

public class ManfredaGameOOP extends ApplicationAdapter{
    private static final float ENEMY_SPAWN_INTERVAL = 1.0f;
    private static final float SEED_SPAWN_INTERVAL = 1.7f;

    private Viewport viewport;
    private OrthographicCamera camera;
    private SpriteBatch batch;
    private ShapeRenderer shapeRenderer;
    private BitmapFont font;

    private Texture playerTexture, enemyTexture, seedTexture, backgroundTexture;
    private Sound seedCollect, enemyHit;

    private Player player;
    private Array<Enemy> enemies;
    private Array<Seed> seeds;

    private float enemySpawnTimer;
    private float seedSpawnTimer;

    @Override
    public void create(){
        camera = new OrthographicCamera();
        viewport = new FitViewport(Constants.SCREEN_WIDTH, Constants.SCREEN_HEIGHT, camera);

        batch = new SpriteBatch();
        shapeRenderer = new ShapeRenderer();
        font = new BitmapFont();
        font.getData().setScale(2.0f);

        playerTexture = new Texture("grayBird.png");
        enemyTexture = new Texture("rock.png");
        seedTexture = new Texture("seed.png");
        backgroundTexture = new Texture("sky.jpg");
        enemyHit = Gdx.audio.newSound(Gdx.files.internal("metalthunk.mp3"));
        seedCollect = Gdx.audio.newSound(Gdx.files.internal("CoinPicked.wav"));

        player = new Player(playerTexture);
        enemies = new Array<>();
        seeds = new Array<>();
    }

    @Override
    public void resize(int width, int height){
        viewport.update(width, height, true);
    }

    private void resetGame(){
        enemies.clear();
        seeds.clear();
        enemySpawnTimer = 0f;
        seedSpawnTimer = 0f;
        player.reset();
    }

    private void spawnEnemy(){
        enemies.add(new Enemy(enemyTexture, enemyHit));
    }

    private void spawnSeed() {
        Seed seed = new Seed(seedTexture, seedCollect);

        for (int attempt = 0; attempt < 10; attempt++) {
            boolean overlaps = false;
            for (Enemy enemy : enemies) {
                if (seed.overlaps(enemy)) {
                    overlaps = true;
                    break;
                }
            }
            if (!overlaps) break;
            seed.setX(MathUtils.random(0f, Constants.SCREEN_WIDTH - seed.getBounds().width));
        }
        seeds.add(seed);
    }

    private void update(float dt){
        player.update(dt);

        enemySpawnTimer +=dt;
        if(enemySpawnTimer > ENEMY_SPAWN_INTERVAL){
            spawnEnemy();
            enemySpawnTimer = 0f;
        }

        seedSpawnTimer +=dt;
        if(seedSpawnTimer > SEED_SPAWN_INTERVAL){
            spawnSeed();
            seedSpawnTimer = 0f;
        }

        updateFalling(enemies, dt);
        updateFalling(seeds, dt);

        if(player.isDead()){
            resetGame();
        }
    }

    private <T extends FallingObject> void updateFalling(Array<T> objects, float dt){
        for(int i=objects.size-1; i>=0; i--){
            T obj = objects.get(i);
            obj.update(dt);

            if(obj.overlaps(player)){
                obj.onHit(player);
                objects.removeIndex(i);
            }else if(obj.isOutOfScreen()){
                objects.removeIndex(i);
            }
        }
    }

    private void drawHealthBar(){
        float barX = 20f, barY = Constants.SCREEN_HEIGHT-70f;
        float barWidth = 200f, barHeight =20f;

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(1,0,0,1);
        shapeRenderer.rect(barX,barY,barWidth,barHeight);

        float current = barWidth*(player.getHealth()/Player.MAX_HEALTH);
        if(current>0){
            shapeRenderer.setColor(0,1,0,1);
            shapeRenderer.rect(barX,barY,current,barHeight);
        }
        shapeRenderer.end();
    }

    @Override
    public void render(){
        update(Gdx.graphics.getDeltaTime());

        ScreenUtils.clear(0.15f, 0.15f,0.2f,1f);
        camera.update();
        batch.setProjectionMatrix(camera.combined);
        shapeRenderer.setProjectionMatrix(camera.combined);

        batch.begin();
        batch.draw(backgroundTexture, 0, 0, Constants.SCREEN_WIDTH, Constants.SCREEN_HEIGHT);
        player.render(batch);
        for(Enemy enemy:enemies) enemy.render(batch);
        for(Seed seed:seeds) seed.render(batch);
        font.draw(batch, "HP:" + (int) player.getHealth(), 20, Constants.SCREEN_HEIGHT-20);
        batch.end();

        drawHealthBar();
    }

    @Override
    public void dispose(){
        batch.dispose();
        shapeRenderer.dispose();
        font.dispose();
        playerTexture.dispose();
        enemyTexture.dispose();
        seedTexture.dispose();
        backgroundTexture.dispose();
        enemyHit.dispose();
        seedCollect.dispose();
    }
}
