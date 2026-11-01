package si.um.feri.manfreda.oop;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
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
import si.um.feri.manfreda.util.debug.DebugCameraController;
import si.um.feri.manfreda.util.debug.MemoryInfo;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.graphics.g2d.ParticleEffect;
import com.badlogic.gdx.graphics.g2d.ParticleEffectPool;
import com.badlogic.gdx.graphics.g2d.ParticleEffectPool.PooledEffect;
import com.badlogic.gdx.utils.ObjectMap;

public class ManfredaGameOOP extends ApplicationAdapter{
    private int enemiesCreated = 0;
    private int seedsCreated = 0;
    private static final float ENEMY_SPAWN_INTERVAL = 1.0f;
    private static final float SEED_SPAWN_INTERVAL = 1.7f;

    private static final float GRID_CELL_SIZE = 100f;
    private boolean debug = false;
    private DebugCameraController debugCameraController;
    private MemoryInfo memoryInfo;

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
    private ParticleEffect birdEffect;

    private ParticleEffect rockEffectTemplate;
    private ParticleEffectPool rockEffectPool;
    private ObjectMap<Enemy, PooledEffect> rockEffects = new ObjectMap<>();
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

        debugCameraController = new DebugCameraController();
        debugCameraController.setStartPosition(Constants.SCREEN_WIDTH/2f, Constants.SCREEN_HEIGHT/ 2f);
        memoryInfo = new MemoryInfo(500);// memory data updates every 500ms

        playerTexture = new Texture("grayBird.png");
        enemyTexture = new Texture("rock.png");
        seedTexture = new Texture("seed.png");
        backgroundTexture = new Texture("sky.jpg");
        enemyHit = Gdx.audio.newSound(Gdx.files.internal("metalthunk.mp3"));
        seedCollect = Gdx.audio.newSound(Gdx.files.internal("CoinPicked.wav"));

        player = new Player(playerTexture);

        birdEffect = new ParticleEffect();
        birdEffect.load(Gdx.files.internal("birdEffect.p"), Gdx.files.internal(""));
        birdEffect.start();

        rockEffectTemplate = new ParticleEffect();
        rockEffectTemplate.load(Gdx.files.internal("rockEffect.p"), Gdx.files.internal(""));
        rockEffectPool = new ParticleEffectPool(rockEffectTemplate, 5, 20);

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

    private void releaseRockEffect(Enemy enemy){
        PooledEffect fx = rockEffects.remove(enemy);
        if (fx != null) fx.free();
    }

    private void resetGame(){
        for (Enemy e : enemies) releaseRockEffect(e);
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
        rockEffects.put(enemy, rockEffectPool.obtain());
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

    private void handleDebugInput(float dt){
        if (Gdx.input.isKeyJustPressed(Input.Keys.F1)) {
            debug = !debug;
        }
        if (debug) {
            debugCameraController.handleDebugInput(dt);
            memoryInfo.update();
        }
    }

    private void update(float dt){
        player.update(dt);

        Rectangle pb = player.getBounds();
        birdEffect.setPosition(pb.x + pb.width/2f,pb.y + pb.height * 0.5f);

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

        for (Enemy e : enemies) {
            PooledEffect fx = rockEffects.get(e);
            if (fx != null) {
                Rectangle r = e.getBounds();
                fx.setPosition(r.x + r.width/2f, r.y + r.height/2f);
            }
        }

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
                if (obj instanceof Enemy) releaseRockEffect((Enemy) obj);
                pool.free(obj); // returns object to pool
                System.out.println("POOL: free " + obj.getClass().getSimpleName() + ", free in pool: " + pool.getFree());
            } else if (obj.isOutOfScreen()) {
                objects.removeIndex(i);
                if (obj instanceof Enemy) releaseRockEffect((Enemy) obj);
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


    private void drawGrid(){
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(1f, 1f, 1f, 1f);

        for (float x = 0; x <= Constants.SCREEN_WIDTH; x += GRID_CELL_SIZE) {
            shapeRenderer.line(x, 0, x, Constants.SCREEN_HEIGHT);
        }
        for (float y = 0; y <= Constants.SCREEN_HEIGHT; y += GRID_CELL_SIZE) {
            shapeRenderer.line(0, y, Constants.SCREEN_WIDTH, y);
        }

        shapeRenderer.end();
    }

    private void drawBounds(){
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(1f, 1f, 0f, 1f);

        Rectangle p = player.getBounds();
        shapeRenderer.rect(p.x, p.y, p.width, p.height);

        for(Enemy enemy : enemies){
            Rectangle r = enemy.getBounds();
            shapeRenderer.rect(r.x, r.y, r.width, r.height);
        }
        for(Seed seed : seeds){
            Rectangle r = seed.getBounds();
            shapeRenderer.rect(r.x, r.y, r.width, r.height);
        }

        shapeRenderer.end();
    }

    @Override
    public void render(){
        float dt = Gdx.graphics.getDeltaTime();
        handleDebugInput(dt);
        update(dt);

        ScreenUtils.clear(0.15f, 0.15f,0.2f,1f);

        if(debug){
            debugCameraController.applyTo(camera);
        }else{
            camera.position.set(Constants.SCREEN_WIDTH / 2f, Constants.SCREEN_HEIGHT / 2f, 0);
            camera.zoom = 1f;
        }

        camera.update();
        batch.setProjectionMatrix(camera.combined);
        shapeRenderer.setProjectionMatrix(camera.combined);

        batch.begin();
        batch.draw(backgroundTexture, 0, 0, Constants.SCREEN_WIDTH, Constants.SCREEN_HEIGHT);
        birdEffect.draw(batch, dt);
        for (PooledEffect fx : rockEffects.values()) fx.draw(batch, dt);
        player.render(batch);
        for(Enemy enemy:enemies) enemy.render(batch);
        for(Seed seed:seeds) seed.render(batch);
        font.draw(batch, "HP:" + (int) player.getHealth(), 20, Constants.SCREEN_HEIGHT-20);
        font.draw(batch, "Score: " + player.getScore(), Constants.SCREEN_WIDTH - 150, Constants.SCREEN_HEIGHT - 20);
        if(debug){
            memoryInfo.render(batch, font);
        }
        batch.end();

        drawHealthBar();

        if(debug){
            drawGrid();
            drawBounds();
        }
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
        birdEffect.dispose();
        rockEffects.clear();
        rockEffectPool.clear();
        rockEffectTemplate.dispose();
    }
}
