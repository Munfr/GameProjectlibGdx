package si.um.feri.manfreda.oop;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;

public abstract class GameObject {
    protected final Rectangle bounds;
    protected final TextureRegion region;
    protected float rotation = 0f;

    public GameObject(Texture texture, float x, float y, float width, float height){
        this.region = new TextureRegion(texture);
        this.bounds = new Rectangle(x,y,width,height);
    }

    public abstract void update(float deltaTime);

    public void render(SpriteBatch batch){
        batch.draw(region,
            bounds.x, bounds.y,
            bounds.width/2f, bounds.height/2f, // sredisce vrtenja
            bounds.width, bounds.height,
            1f, 1f,
            rotation
            );
    }

    public boolean overlaps(GameObject other){
        return bounds.overlaps(other.bounds);
    }

    public Rectangle getBounds(){ return bounds;}
    public void setX(float x ){ bounds.x = x; }
}
