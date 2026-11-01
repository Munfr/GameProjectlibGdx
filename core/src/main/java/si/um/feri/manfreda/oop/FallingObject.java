package si.um.feri.manfreda.oop;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Pool;

public abstract class FallingObject extends DynamicGameObject implements Pool.Poolable{

    private final Texture fallingTexture;

    public FallingObject(Texture texture){
        super(texture, 0f,0f,1f,1f);
        this.fallingTexture = texture;
    }

    // is called on every obtain, sets size, spd and rotation
    protected void init(float width, float spd, float rotationSpeed){
        bounds.width = width;
        bounds.height = width * ((float) fallingTexture.getHeight() / fallingTexture.getWidth());
        bounds.x = MathUtils.random(0f, Constants.SCREEN_WIDTH-width);
        bounds.y = Constants.SCREEN_HEIGHT;
        velocity.set(0f, -spd); // 0f spd on x, -spd spd on y
        this.rotationSpeed = rotationSpeed;
        this.rotation = 0;
    }

    public boolean isOutOfScreen(){
        return bounds.y + bounds.height <0;
    }

    public abstract void onHit(Player player);

    // pool calls reset on free object returns to starting state
    @Override
    public void reset(){
        bounds.set(0f,0f,1f,1f);
        velocity.setZero();
        rotationSpeed = 0f;
        rotation = 0f;
    }
}
