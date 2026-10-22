package si.um.feri.manfreda.oop;

import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Texture;

public class Seed extends FallingObject{
    private final Sound collectSound;

    public Seed(Texture texture, Sound collectSound){
        super(texture, 70f, 300f, 90f);
        this.collectSound = collectSound;
    }

    @Override
    public void onHit(Player player){
        collectSound.play();
        player.addScore(10);
    }
}
