package si.um.feri.manfreda.oop;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Pool;

public abstract class FallingObject extends DynamicGameObject implements Pool.Poolable{

    public FallingObject(Texture texture){
        super(texture, 0f,0f,1f,1f);
    }

    // se klice ob vsakem obtain, nastavi velikost, hitrost in vrtenje
    protected void init(float size, float spd, float rotationSpeed){
        bounds.width = size;
        bounds.height = size;
        bounds.x = MathUtils.random(0f, Constants.SCREEN_WIDTH-size);
        bounds.y = Constants.SCREEN_HEIGHT;
        velocity.set(0f, -spd);
        this.rotationSpeed = rotationSpeed;
        this.rotation = 0;
    }

    public boolean isOutOfScreen(){
        return bounds.y + bounds.height <0;
    }

    public abstract void onHit(Player player);

    // pool poklice reset ob free, objekt se vrne v zacetno stanje
    @Override
    public void reset(){
        bounds.set(0f,0f,1f,1f);
        velocity.setZero();
        rotationSpeed = 0f;
        rotation = 0f;
    }
}
