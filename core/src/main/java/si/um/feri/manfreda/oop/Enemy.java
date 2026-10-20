package si.um.feri.manfreda.oop;

import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.MathUtils;

public class Enemy extends FallingObject{
    private static final float DAMAGE = 25f;
    private final Sound hitSound;

    public Enemy(Texture texture, Sound hitSound){
        super(texture,
            MathUtils.random(90f, 130f),
            MathUtils.random(300f, 500f),
            MathUtils.random(-240f, 240f));
        this.hitSound = hitSound;
    }

    @Override
    public void onHit(Player player){
        hitSound.play();
        player.takeDamage(DAMAGE);
    }
}
