package gwel.game.anim;

import com.badlogic.gdx.math.Affine2;
import com.badlogic.gdx.math.Vector2;
import gwel.game.graphics.ComplexShape;

import java.util.ArrayList;


public class PostureTree {
    private final PostureTree parent;
    private final ComplexShape shape;
    private final ArrayList<PostureTree> children = new ArrayList<>();
    private final ArrayList<Animation> animations = new ArrayList<>();
    private final Affine2 transform = new Affine2();
    private final float[] colorMod = new float[] {0f, 0f, 0f, 1f};


    public PostureTree(ComplexShape shape, PostureTree parent) {
        this.parent = parent;
        this.shape = shape;
    }


    public PostureTree getParent() { return parent; }

    public ArrayList<PostureTree> getChildren() { return children; }

    public ArrayList<Animation> getAnimations() { return animations; }

    public Affine2 getTransform() { return transform; }

    public float[] getColorMod() { return colorMod; }


    public static PostureTree buildTree(ComplexShape shape) { return buildTree(shape, null); }

    public static PostureTree buildTree(ComplexShape shape, PostureTree parent) {
        PostureTree pt = new PostureTree(shape, parent);
        for (ComplexShape child : shape.getChildren()) {
            pt.children.add(PostureTree.buildTree(child, pt));
        }
        return pt;
    }


    /**
     * Updates each transform matrix in the tree to their corresponding local transform matrices
     */
    public void updateTransform() {
        if (!animations.isEmpty()) {
            System.arraycopy(shape.getTint(), 0, colorMod, 0, 4);

            Vector2 pivotPoint = shape.getLocalOrigin();
            if (!shape.getTransform().isIdt()) {
                shape.getTransform().applyTo(pivotPoint);
            }
            transform.setToTranslation(-pivotPoint.x, -pivotPoint.y);
            for (Animation anim : animations) {
                anim.update();
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
            transform.preTranslate(pivotPoint);
        }

        for (PostureTree child : children)
            child.updateTransform();
    }


    public PostureTree findByShape(ComplexShape shape) {
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
        for (Animation anim : animations) {
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