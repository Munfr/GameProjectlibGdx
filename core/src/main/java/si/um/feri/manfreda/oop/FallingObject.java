package si.um.feri.manfreda.oop;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.MathUtils;

public abstract class FallingObject extends DynamicGameObject {

    public FallingObject(Texture texture, float size, float spd, float rotationSpeed){
        super(texture,
            MathUtils.random(0f, Constants.SCREEN_WIDTH-size),
            Constants.SCREEN_HEIGHT,
            size, size);
        this.velocity.y =-spd;
        this.rotationSpeed = rotationSpeed;
    }

    public boolean isOutOfScreen(){
        return bounds.y + bounds.height <0;
    }

    public abstract void onHit(Player player);
}
