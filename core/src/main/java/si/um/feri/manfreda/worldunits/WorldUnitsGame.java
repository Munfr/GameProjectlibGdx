package si.um.feri.manfreda.worldunits;

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
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

// svet meri 16x12 enot in okno 800x600px
public class WorldUnitsGame extends ApplicationAdapter {

    private static final float WORLD_WIDTH = 16f;
    private static final float WORLD_HEIGHT = 12f;

    private static final float PLAYER_SPD = 10f;
    private static final float ENEMY_SPD = 8f;
    private static final float SEED_SPD = 6f;

    private static final float PLAYER_WIDTH = 1.8f;
    private static final float ENEMY_WIDTH = 2.2f;
    private static final float SEED_WIDTH = 1.4f;

    private static final float SPAWN_INTERVAL = 1.0f;
    private static final float SEED_SPAWN_INTERVAL = 1.7f;

    private Viewport viewport;
    private OrthographicCamera camera;

    private SpriteBatch batch;
    private ShapeRenderer healthBar;
    private BitmapFont font;

    private int score = 0;
    private Texture playerImg;
    private Texture enemyImg;
    private Texture seedImg;
    private Texture background;

    private Sound seedCollect;
    private Sound enemyHit;

    private Rectangle player;
    private Array<Rectangle> enemies;
    private Array<Rectangle> seeds;

    private float playerHeight;
    private float enemyHeight;
    private float seedHeight;

    private float playerHealth = 100f;
    private float enemySpawnTimer;
    private float seedSpawnTimer;

    private void spawnSeed() {
        Rectangle seed = new Rectangle();
        seed.width = SEED_WIDTH;
        seed.height = seedHeight;
        seed.y = WORLD_HEIGHT;

        boolean overlaps;
        int safetyCounter = 0;
        do {
            overlaps = false;
            seed.x = MathUtils.random(0f, WORLD_WIDTH - seed.width);

            for (Rectangle existingEnemy : enemies) {
                if (seed.overlaps(existingEnemy)) {
                    overlaps = true;
                    break;
                }
            }
            safetyCounter++;
        } while (overlaps && safetyCounter < 10);

        seeds.add(seed);
    }

    private void spawnEnemy() {
        Rectangle enemy = new Rectangle();
        enemy.width = ENEMY_WIDTH;
        enemy.height = enemyHeight;
        enemy.x = MathUtils.random(0f, WORLD_WIDTH - enemy.width);
        enemy.y = WORLD_HEIGHT;
        enemies.add(enemy);
    }

    private void resetGame() {
        enemies.clear();
        seeds.clear();

        score = 0;
        enemySpawnTimer = 0f;
        seedSpawnTimer = 0f;
        playerHealth = 100f;

        player.x = WORLD_WIDTH / 2f - player.width / 2f;
        player.y = 0.2f;
    }

    private void drawHealthBar() {
        healthBar.begin(ShapeRenderer.ShapeType.Filled);

        float barX = 0.4f;
        float barY = WORLD_HEIGHT - 1.4f;
        float barWidth = 4f;
        float barHeight = 0.4f;

        healthBar.setColor(1, 0, 0, 1);
        healthBar.rect(barX, barY, barWidth, barHeight);

        float currentHealthWidth = barWidth * (playerHealth / 100f);
        if (currentHealthWidth > 0) {
            healthBar.setColor(0, 1, 0, 1);
            healthBar.rect(barX, barY, currentHealthWidth, barHeight);
        }

        healthBar.end();
    }

    @Override
    public void create() {
        camera = new OrthographicCamera();
        viewport = new FitViewport(WORLD_WIDTH, WORLD_HEIGHT, camera);

        batch = new SpriteBatch();
        healthBar = new ShapeRenderer();

        playerImg = new Texture("grayBird.png");
        enemyImg = new Texture("rock.png");
        seedImg = new Texture("seed.png");
        background = new Texture("sky.jpg");

        enemyHit = Gdx.audio.newSound(Gdx.files.internal("metalthunk.mp3"));
        seedCollect = Gdx.audio.newSound(Gdx.files.internal("CoinPicked.wav"));

        // maintain image ratio
        playerHeight = PLAYER_WIDTH * ((float) playerImg.getHeight() / playerImg.getWidth());
        enemyHeight = ENEMY_WIDTH * ((float) enemyImg.getHeight() / enemyImg.getWidth());
        seedHeight = SEED_WIDTH * ((float) seedImg.getHeight() / seedImg.getWidth());

        font = new BitmapFont();
        font.setUseIntegerPositions(false);// num of tet position isnt rounded
        font.getData().setScale(0.04f);

        enemies = new Array<Rectangle>();
        seeds = new Array<Rectangle>();

        player = new Rectangle();
        player.width = PLAYER_WIDTH;
        player.height = playerHeight;

        resetGame();
    }

    @Override
    public void render() {

        float deltaTime = Gdx.graphics.getDeltaTime();
        float maxX = WORLD_WIDTH - player.width;

        if (Gdx.input.isKeyPressed(Input.Keys.A) || Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
            player.x -= PLAYER_SPD * deltaTime;
        } else if (Gdx.input.isKeyPressed(Input.Keys.D) || Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
            player.x += PLAYER_SPD * deltaTime;
        }

        if (player.x < 0f) {
            player.x = 0f;
        } else if (player.x > maxX) {
            player.x = maxX;
        }

        enemySpawnTimer += deltaTime;
        if (enemySpawnTimer > SPAWN_INTERVAL) {
            spawnEnemy();
            enemySpawnTimer = 0f;
        }

        seedSpawnTimer += deltaTime;
        if (seedSpawnTimer > SEED_SPAWN_INTERVAL) {
            spawnSeed();
            seedSpawnTimer = 0f;
        }

        for (int i = enemies.size - 1; i >= 0; i--) {
            Rectangle enemy = enemies.get(i);
            enemy.y -= ENEMY_SPD * deltaTime;

            if (playerHealth <= 0f) {
                resetGame();
                break;
            }

            if (enemy.overlaps(player)) {
                enemyHit.play();
                playerHealth -= 25f;
                enemies.removeIndex(i);
                break;
            }

            if (enemy.y + enemy.height < 0) {
                enemies.removeIndex(i);
            }
        }

        for (int i = seeds.size - 1; i >= 0; i--) {
            Rectangle seed = seeds.get(i);
            seed.y -= SEED_SPD * deltaTime;

            if (seed.overlaps(player)) {
                score+=10;
                seedCollect.play();
                seeds.removeIndex(i);
                continue;
            }

            if (seed.y + seed.height < 0) {
                seeds.removeIndex(i);
            }
        }

        ScreenUtils.clear(0.15f, 0.15f, 0.2f, 1f); // clears screen and sets background

        viewport.apply();
        camera.update();
        batch.setProjectionMatrix(camera.combined);
        healthBar.setProjectionMatrix(camera.combined);

        batch.begin();
        batch.draw(background, 0, 0, WORLD_WIDTH, WORLD_HEIGHT);
        batch.draw(playerImg, player.x, player.y, player.width, player.height);
        for (Rectangle enemy : enemies) {
            batch.draw(enemyImg, enemy.x, enemy.y, enemy.width, enemy.height);
        }
        for (Rectangle seed : seeds) {
            batch.draw(seedImg, seed.x, seed.y, seed.width, seed.height);
        }
        font.draw(batch, "HP:" + (int) playerHealth, 0.4f, WORLD_HEIGHT - 0.4f);
        font.draw(batch, "Score: " + score, WORLD_WIDTH - 3.5f, WORLD_HEIGHT - 0.4f);
        batch.end();

        drawHealthBar();
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
    }

    @Override
    public void dispose() {
        batch.dispose();
        healthBar.dispose();
        font.dispose();
        background.dispose();
        playerImg.dispose();
        enemyImg.dispose();
        seedImg.dispose();
        enemyHit.dispose();
        seedCollect.dispose();
    }
}
