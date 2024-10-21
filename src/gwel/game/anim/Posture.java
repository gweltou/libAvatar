package gwel.game.anim;

import gwel.game.graphics.ComplexShape;


public class Posture {
    private String name;
    private float duration;
    private TimeFunction[] timeFunctions;
    private PostureTree postureTree;



    public void setName(String name) { this.name = name; }

    public String getName() { return name; }

    public void setDuration(float duration) { this.duration = duration; }

    public float getDuration() { return duration; }

    public void setPostureTree(PostureTree tree) {
        postureTree = tree;
        timeFunctions = postureTree.getUniqueTimeFunctions();
    }

    public PostureTree getPostureTree() { return postureTree; }


    /**
     * Updates the TimeFunctions used by this posture and
     * Returns an array of ComplexShape instances and their corresponding local animation matrices
     *
     * @param dtime
     */
    public void update(float dtime) {
        for (TimeFunction fn : timeFunctions)
            fn.update(dtime);
        postureTree.updateTransform();
     }


     public void reset() {
         for (TimeFunction fn : timeFunctions)
             fn.reset();
         postureTree.updateTransform();
     }


    /**
     * Used by SGAnimator
     */
    public Animation getAnimation(String partId, int idx) {
        PostureTree pt = postureTree.findById(partId);
        if (pt == null)
            return null;
        return pt.getAnimations().get(idx);
    }


    /**
     * Used by SGAnimator
     */
    public void setAnimation(String partId, int idx, Animation anim) {
        PostureTree pt = postureTree.findById(partId);
        assert (pt != null) : String.format("Part \"%s\" not found in PostureTree", partId);
        pt.getAnimations().set(idx, anim);
        timeFunctions = postureTree.getUniqueTimeFunctions();
    }


    /**
     * Used by SGAnimator
     */
    public void addAnimation(String partId, Animation anim) {
        PostureTree pt = postureTree.findById(partId);
        assert (pt != null) : String.format("Part \"%s\" not found in PostureTree", partId);
        pt.getAnimations().add(anim);
        timeFunctions = postureTree.getUniqueTimeFunctions();
    }


    /**
     * Used by SGAnimator
     *
     * @param shape
     * @param idx
     */
    public void removeAnimation(ComplexShape shape, int idx) {
        PostureTree pt = postureTree.findByShape(shape);
        assert (pt != null) : String.format("Shape %s not found in PostureTree", shape);
        pt.getAnimations().remove(idx);
        timeFunctions = postureTree.getUniqueTimeFunctions();
    }


    public String toString() {
        String s = String.format(" [name: %s duration: %.1f]", name, duration);
        return super.toString() + s;
    }
}
