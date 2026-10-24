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
import com.badlogic.gdx.utils.Pool;

public class ManfredaGameOOP extends ApplicationAdapter{
    private int enemiesCreated = 0;
    private int seedsCreated = 0;
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
    private Pool<Enemy> enemyPool;
    private Pool<Seed> seedPool;

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

        enemyPool = new Pool<Enemy>() {
            @Override
            protected Enemy newObject() {
                enemiesCreated++;
                System.out.println("POOL new enemy created (all enemies created: "  + enemiesCreated + ")");
                return new Enemy(enemyTexture, enemyHit);
            }
        };

        seedPool = new Pool<Seed>() {
            @Override
            protected Seed newObject() {
                seedsCreated++;
                System.out.println("POOL new seed created (all seeds created: "  + seedsCreated + ")");
                return new Seed(seedTexture, seedCollect);
            }
        };
    }

    @Override
    public void resize(int width, int height){
        viewport.update(width, height, true);
    }

    private void resetGame(){
        enemyPool.freeAll(enemies);
        seedPool.freeAll(seeds);
        enemies.clear();
        seeds.clear();
        enemySpawnTimer = 0f;
        seedSpawnTimer = 0f;
        player.reset();
    }

    private void spawnEnemy(){
        Enemy enemy = enemyPool.obtain();
        System.out.println("POOL: obtain Enemy, enemies free in pool: " + enemyPool.getFree() + " active: " + (enemies.size +1));
        enemy.init();
        enemies.add(enemy);
    }

    private void spawnSeed() {
        Seed seed = seedPool.obtain();
        seed.init();

        for (int attempt = 0; attempt < 10; attempt++) {
            boolean overlaps = false;
            for (Enemy enemy : enemies) {
                if (seed.overlaps(enemy)) { overlaps = true; break; }
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

        updateFalling(enemies, enemyPool, dt);
        updateFalling(seeds, seedPool, dt);

        if(player.isDead()){
            resetGame();
        }
    }

    private <T extends FallingObject> void updateFalling(Array<T> objects, Pool<T> pool, float dt){
        for (int i = objects.size - 1; i >= 0; i--) {
            T obj = objects.get(i);
            obj.update(dt);

            if (obj.overlaps(player)) {
                obj.onHit(player);
                objects.removeIndex(i);
                pool.free(obj);
                System.out.println("POOL: free " + obj.getClass().getSimpleName() + ", free in pool: " + pool.getFree());
            } else if (obj.isOutOfScreen()) {
                objects.removeIndex(i);
                pool.free(obj);
                System.out.println("POOL: free " + obj.getClass().getSimpleName() + ", free in pool: " + pool.getFree());
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
        font.draw(batch, "Score: " + player.getScore(), Constants.SCREEN_WIDTH - 150, Constants.SCREEN_HEIGHT - 20);
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
