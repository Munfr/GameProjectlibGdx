package si.um.feri.manfreda.oop;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Texture;

public class Player extends DynamicGameObject{
    private static final float SPD = 500f;
    private static final float SIZE = 90f;
    public static final float MAX_HEALTH = 100f;

    private float health = MAX_HEALTH;

    public Player(Texture texture){
        super(texture, 0, 10f, SIZE, SIZE);
        reset();
    }

    @Override
    public void update(float deltaTime){
        velocity.x = 0;
        if(Gdx.input.isKeyPressed(Input.Keys.A) || Gdx.input.isKeyPressed(Input.Keys.LEFT)){
            velocity.x -= SPD;
        }else if(Gdx.input.isKeyPressed(Input.Keys.D) || Gdx.input.isKeyPressed(Input.Keys.RIGHT)){
            velocity.x += SPD;
        }

        super.update(deltaTime);

        float maxX = Constants.SCREEN_WIDTH - bounds.width;
        if(bounds.x<0f) bounds.x = 0f;
        else if (bounds.x > maxX) bounds.x = maxX;
    }

    public void takeDamage(float amount) { health -=amount;}
    public boolean isDead(){ return health<=0f;}
    public float getHealth(){ return health; }

    public void reset(){
        health = MAX_HEALTH;
        bounds.x = Constants.SCREEN_WIDTH/2f - bounds.width /2f;
        bounds.y = 10f;
    }
}
