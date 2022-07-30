package gwel.game.graphics;

import com.badlogic.gdx.math.Affine2;
import com.badlogic.gdx.math.Vector2;


public interface Shape {
    void draw(Renderer renderer);
    void drawSelected(PRenderer renderer);
    void hardTransform(Affine2 transform);
    boolean contains(Vector2 p);
    boolean contains(float x, float y);
    short getNumIndices();
    int getNumVerts();
    Shape copy();
}
