package si.um.feri.manfreda.oop;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Vector2;

public class DynamicGameObject extends GameObject{
    protected final Vector2 velocity = new Vector2();
    protected float rotationSpeed = 0f;

    public DynamicGameObject(Texture texture, float x, float y, float width, float height){
        super(texture, x, y, width, height);
    }

    @Override
    public void update(float deltaTime){
        bounds.x += velocity.x * deltaTime;
        bounds.y += velocity.y * deltaTime;
        rotation += rotationSpeed *deltaTime;
    }
}
