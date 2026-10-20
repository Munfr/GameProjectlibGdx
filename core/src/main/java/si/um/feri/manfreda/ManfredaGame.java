package si.um.feri.manfreda;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;// health bar import
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.badlogic.gdx.audio.Sound;


/** {@link com.badlogic.gdx.ApplicationListener} implementation shared by all platforms. */
public class ManfredaGame extends ApplicationAdapter {
    private Viewport viewport;
    private OrthographicCamera camera;
    private final float SCREEN_WIDTH = 800f;
    private final float SCREEN_HEIGHT = 600f;
    private SpriteBatch batch;
    private Texture image;
    private Texture playerImg;
    private Texture enemyImg;
    private Texture seedImg;
    private Texture background;
    private BitmapFont font;
    private ShapeRenderer healthBar;
    private Sound seedCollect;
    private Sound enemyHit;

    private Rectangle player;
    private Array<Rectangle> enemies;
    private Array<Rectangle> seeds;

    private float playerHealth=100.0f;

    private float enemySpawnTimer;
    private float seedSpawnTimer;
    private final float PLAYER_SPD=500.0f;
    private final float SPAWN_INTERVAL=1.0f;
    private final float SEED_SPAWN_INTERVAL = 1.7f;
    private final float ENEMY_SPD=400.0f;
    private final float SEED_SPD=300.0f;

    private void spawnSeed(){
        Rectangle seed = new Rectangle();
        seed.width = 70f;
        seed.height = 70f;
        seed.y = SCREEN_HEIGHT;

        boolean overlaps;
        int safetyCounter = 0; // safety if an overlaping random number is selected more then 10 times in a row
        do{
            overlaps = false;
            seed.x = MathUtils.random(0f,SCREEN_WIDTH-seed.width);

            for(Rectangle existingEnemy: enemies){
                if(seed.overlaps(existingEnemy)){
                    overlaps=true;
                    break;
                }
            }
            safetyCounter++;
        }while(overlaps&& safetyCounter<10);

        seeds.add(seed);
    }
    private void spawnEnemy(){
        Rectangle enemy = new Rectangle();
        enemy.width = 110f;
        enemy.height = 110f;

        enemy.x = MathUtils.random(0f, SCREEN_WIDTH-enemy.width);

        enemy.y = SCREEN_HEIGHT;

        enemies.add(enemy);
    }

    private void resetGame(){
        enemies.clear();
        seeds.clear();

        enemySpawnTimer = 0;
        playerHealth = 100f;

        player.x = SCREEN_WIDTH / 2f - playerImg.getWidth() / 2f;
        player.y = 10f;
    }

    private void drawHealthBar(){
        healthBar.begin(ShapeRenderer.ShapeType.Filled);

        float barX = 20f;
        float barY = SCREEN_HEIGHT-70f;
        float barWidth = 200f;
        float barHeight = 20f;

        healthBar.setColor(1,0,0,1); // red background
        healthBar.rect(barX,barY,barWidth,barHeight);

        float currentHealthWidth = barWidth *(playerHealth/100f);

        if(currentHealthWidth>0){
            healthBar.setColor(0,1,0,1);
            healthBar.rect(barX,barY, currentHealthWidth, barHeight);
        }

        healthBar.end();
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
    }

    @Override
    public void create() {
        camera = new OrthographicCamera();
        viewport = new FitViewport(SCREEN_WIDTH, SCREEN_HEIGHT, camera);

        batch = new SpriteBatch();
        image = new Texture("libgdx.png");
        playerImg = new Texture("grayBird.png");
        enemyImg = new Texture("rock.png");
        seedImg = new Texture("seed.png");
        background = new Texture("sky.jpg");
        enemyHit = Gdx.audio.newSound(Gdx.files.internal("metalthunk.mp3"));
        seedCollect = Gdx.audio.newSound(Gdx.files.internal("CoinPicked.wav"));
        enemies = new Array<Rectangle>();
        seeds = new Array<Rectangle>();
        font = new BitmapFont();
        font.getData().setScale(2.0f);
        healthBar = new ShapeRenderer();

        player = new Rectangle();
        player.width = 90f;
        player.height = 90f;

        resetGame();
    }

    @Override
    public void render() {
        camera.update();
        batch.setProjectionMatrix(camera.combined);
        healthBar.setProjectionMatrix(camera.combined);

        float deltaTime = Gdx.graphics.getDeltaTime();
        float maxX= SCREEN_WIDTH-player.width;

        if(Gdx.input.isKeyPressed(Input.Keys.A)||Gdx.input.isKeyPressed(Input.Keys.LEFT)){
            player.x -= PLAYER_SPD* deltaTime;
        }else if(Gdx.input.isKeyPressed(Input.Keys.D)||Gdx.input.isKeyPressed(Input.Keys.RIGHT)){
            player.x += PLAYER_SPD*deltaTime;
        }

        // checks if the player is out of bounds
        if(player.x < 0f){
            player.x=0f;
        }else if(player.x>maxX){
            player.x = maxX;
        }

        enemySpawnTimer +=deltaTime;
        if(enemySpawnTimer>SPAWN_INTERVAL){
            spawnEnemy();
            enemySpawnTimer = 0f;
        }

        seedSpawnTimer += deltaTime;
        if(seedSpawnTimer>SEED_SPAWN_INTERVAL){
            spawnSeed();
            seedSpawnTimer = 0f;
        }

        // logic for moving the enemy from top to bottom and removing it from the array
        for(int i = enemies.size-1; i>=0; i--){
            Rectangle enemy = enemies.get(i);
            enemy.y -= ENEMY_SPD*deltaTime;

            if(playerHealth<=0f){
                resetGame();
                break;
            }

            if(enemy.overlaps(player)){
                enemyHit.play();
                playerHealth -= 25.0f;
                enemies.removeIndex(i);
                break;
            }

            if(enemy.y+enemy.height<0){
                enemies.removeIndex(i);
            }
        }

        for(int i =  seeds.size-1; i>=0;i--){
            Rectangle seed = seeds.get(i);
            seed.y -= SEED_SPD*deltaTime;

            if(seed.overlaps(player)){
                seedCollect.play();
                seeds.removeIndex(i);
                continue;
            }

            if (seed.y + seed.height < 0) {
                seeds.removeIndex(i);
            }
        }

        ScreenUtils.clear(0.15f, 0.15f, 0.2f, 1f);
        batch.begin();
        batch.draw(background,0,0,SCREEN_WIDTH, SCREEN_HEIGHT);
        batch.draw(playerImg, player.x, player.y, player.width, player.height);
        for(Rectangle enemy:enemies){
            batch.draw(enemyImg, enemy.x, enemy.y,enemy.width,enemy.height);
        }
        for(Rectangle seed:seeds){
            batch.draw(seedImg, seed.x, seed.y, seed.width, seed.height);
        }
        font.draw(batch, "HP:"+ (int)playerHealth,20,SCREEN_HEIGHT-20);

        batch.end();
        drawHealthBar();
    }

    @Override
    public void dispose() {
        batch.dispose();
        image.dispose();
        background.dispose();
        playerImg.dispose();
        enemyImg.dispose();
        font.dispose();
        healthBar.dispose();
        enemyHit.dispose();
        seedCollect.dispose();
    }
}
