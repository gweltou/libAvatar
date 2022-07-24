package gwel.game.anim;

import com.badlogic.gdx.math.Affine2;
import gwel.game.graphics.ComplexShape2;

import java.util.ArrayList;
import java.util.Arrays;


public class PostureTree {
    private final PostureTree parent;
    private final ComplexShape2 shape;
    private final ArrayList<PostureTree> children = new ArrayList<>();
    private final ArrayList<Animation2> animations = new ArrayList<>();
    private final Affine2 transform = new Affine2();
    private final float[] colorMod = new float[4];;


    public PostureTree(PostureTree parent, ComplexShape2 shape) {
        this.parent = parent;
        this.shape = shape;
    }


    public static PostureTree buildTree(ComplexShape2 shape) {
        PostureTree pt = new PostureTree(null, shape);
        for (ComplexShape2 child : shape.getChildren()) {
            pt.children.add(new PostureTree(pt, child));
        }
        return pt;
    }


    public PostureTree getParent() { return parent; }


    public ArrayList<Animation2> getAnimations() { return animations; }


    public Affine2 getTransform() { return transform; }

    /**
     * Updates each transform matrix in the tree to their corresponding local transform matrices
     */
    public void updateTransform() {
        transform.idt();
        Arrays.fill(colorMod, 0f);
        for (Animation2 anim : animations) {
            if (anim.getAxe() < 6) {
                transform.preMul(anim.getTransform());
            } else {
                float[] animColorMod = anim.getColorMod();
                colorMod[0] += animColorMod[0];
                colorMod[1] += animColorMod[1];
                colorMod[2] += animColorMod[2];
                colorMod[3] *= animColorMod[3];
            }
        }

        for (PostureTree child : children)
            child.updateTransform();
    }


    public PostureTree findByShape(ComplexShape2 shape) {
        if (shape.equals(this.shape))
            return this;
        for (PostureTree child : children) {
            PostureTree match = child.findByShape(shape);
            if (match != null)
                return match;
        }
        return null;
    }


    public PostureTree findById(String id) {
        if (id.equals(shape.getId()))
            return this;
        for (PostureTree child : children) {
            PostureTree match = child.findById(id);
            if (match != null)
                return match;
        }
        return null;
    }


    public TimeFunction[] getUniqueTimeFunctions() {
        ArrayList<TimeFunction> functions = new ArrayList<>();
        for (Animation2 anim : animations) {
            if (!functions.contains(anim.getFunction()))
                functions.add(anim.getFunction());
        }
        for (PostureTree child : children) {
            for (TimeFunction fn : child.getUniqueTimeFunctions()) {
                if (!functions.contains(fn))
                    functions.add(fn);
            }
        }
        return functions.toArray(new TimeFunction[0]);
    }
}