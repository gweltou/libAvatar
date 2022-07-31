package gwel.game.graphics;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Affine2;
import com.badlogic.gdx.math.Bezier;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.JsonValue;
import gwel.game.anim.PostureTree;
import gwel.game.utils.BoundingBox;
import gwel.game.utils.DummyPShape;
import processing.core.PMatrix3D;
import processing.core.PShape;
import processing.core.PVector;

import java.util.ArrayList;

import static gwel.game.graphics.Utils.pColorToGDXColor;
import static processing.core.PConstants.*;


public class ComplexShape2 implements Shape {
    private final String id;
    protected ComplexShape2 parent = null;
    private final ArrayList<ComplexShape2> children = new ArrayList<>();
    private final ArrayList<Shape> shapes = new ArrayList<>(); // ComplexShapes and simple shapes
    private final Vector2 localOrigin = new Vector2();  // Pivot point
    protected final Affine2 preTransform = new Affine2();
    private final float[] tint = new float[] {0f, 0f, 0f, 1f};
    private final BoundingBox boundingBox = new BoundingBox();
    private boolean boundingBox_dirty;


    public ComplexShape2(String id) {
        this.id = id;
    }

    private ComplexShape2 getRootShape() {
        if (parent == null)
            return this;
        return parent.getRootShape();
    }

    public void addShape(Shape shape) {
        shapes.add(shape);
        if (shape instanceof ComplexShape2) {
            children.add((ComplexShape2) shape);
            ((ComplexShape2) shape).parent = this;
        }
        boundingBox_dirty = true;
    }

    /**
     * Get the list of all children in this ComplexShape
     *
     * @return list of all children (simple and composed)
     */
    public ArrayList<Shape> getShapes() {
        return shapes;
    }

    /**
     * Get the list of ComplexShape children (only) of this shape
     *
     * @return list of ComplexShape children
     */
    public ArrayList<ComplexShape2> getChildren() {
        return children;
    }


    public Vector2 getLocalOrigin() { return localOrigin.cpy(); }

    public void setLocalOrigin(Vector2 pos) { localOrigin.set(pos); }


    public BoundingBox getBoundingBox() {
        if (boundingBox_dirty) {
            boundingBox.reset();
            Affine2 globalTransform = getAbsoluteTransform();
            for (Shape shape : shapes) {
                if (shape instanceof ComplexShape2) {
                    boundingBox.include( ((ComplexShape2) shape).getBoundingBox() );
                } else if (shape instanceof DrawablePolygon) {
                    BoundingBox bb = new BoundingBox();
                    float[] vertices = ((DrawablePolygon) shape).getVertices();
                    for (int i = 0; i < vertices.length; i += 2) {
                        Vector2 p = new Vector2(vertices[i], vertices[i+1]);
                        globalTransform.applyTo(p);
                        bb.include(p);
                    }
                    boundingBox.include(bb);
                }
            }
            boundingBox_dirty = false;
        }
        return boundingBox;
    }

    public void invalidateBoundingBox() {
        boundingBox_dirty = true;
        for (ComplexShape2 child : children)
            child.invalidateBoundingBox();
    }

    public boolean contains(Vector2 position) {
        return contains(position.x, position.y);
    }

    public boolean contains(float x, float y) {
        Vector2 transformedPoint = new Vector2(x, y);
        Affine2 globalTransform = getAbsoluteTransform();
        if (globalTransform.m00 == 0f || globalTransform.m11 == 0f)
            return false;
        globalTransform.inv().applyTo(transformedPoint);
        if (getBoundingBox().contains(x, y)) {
            for (Shape shape : shapes) {
                if (shape instanceof ComplexShape2) {
                    if (shape.contains(x, y)) return true;
                } else {
                    // Simple shapes are not aware of their transformation
                    if (shape.contains(transformedPoint))
                        return true;
                }
            }
        }
        return false;
    }

    public boolean contains(float x, float y, PostureTree pt) {
        Affine2 globalTransform = getAbsoluteTransform(pt);
        if (globalTransform.m00 == 0f || globalTransform.m11 == 0f) {
            return false;
        }
        if (getBoundingBox().contains(x, y)) {
            Vector2 transformedPoint = new Vector2(x, y);
            globalTransform.inv().applyTo(transformedPoint);
            for (Shape shape : shapes) {
                if (shape instanceof ComplexShape2) {
                    if (((ComplexShape2) shape).contains(x, y, pt)) return true;
                } else {
                    // Simple shapes are not aware of their transformation
                    if (shape.contains(transformedPoint))
                        return true;
                }
            }
        }
        return false;
    }


    /**
     * Get the transform state of this shape
     * That includes parents transforms and soft transform
     *
     * @return this shape's transform affine matrix
     */
    public Affine2 getAbsoluteTransform() {
        Affine2 combined = new Affine2();
        combined.mul(preTransform);
        if (parent == null) {
            return combined;
        } else {
            return parent.getAbsoluteTransform().mul(combined);
        }
    }


    /**
     * V 2.0
     * Get the transform state of this shape at this particular animated moment
     * That includes parents transform and soft transform and animations transform
     *
     * @return this shape's transform affine matrix
     */
    public Affine2 getAbsoluteTransform(PostureTree pt) {
        Affine2 combined = new Affine2();
        if (pt != null) {
            combined.mul(pt.getTransform());
        }
        combined.mul(preTransform);
        if (parent == null) {
            return combined;
        } else {
            assert pt != null;
            return parent.getAbsoluteTransform(pt.getParent()).mul(combined);
        }
    }


    /**
     * Apply a non-destructive pre-transformation to the shapes
     */
    public void softTransform(Affine2 transform) {
        Affine2 currentTransform = getAbsoluteTransform();
        Affine2 invTransform = (new Affine2(currentTransform)).inv();
        preTransform.mul(invTransform);
        preTransform.mul(transform);
        preTransform.mul(currentTransform);
        getRootShape().invalidateBoundingBox();
    }

    /**
     * Sets the pre-transform matrix manually
     *
     * @param transform matrix
     */
    public void setTransform(Affine2 transform) {
        preTransform.set(transform);
    }

    public Affine2 getTransform() { return new Affine2(preTransform); }

    /**
     * Used by SGAnimator
     */
    public void resetTransform() {
        preTransform.idt();
        getRootShape().invalidateBoundingBox();
    }

    /**
     * Modify geometry in place
     */
    public void hardTransform(Affine2 transform) {
        transform.applyTo(localOrigin);
        for (Shape shape : shapes)
            shape.hardTransform(transform);
        getRootShape().invalidateBoundingBox();
    }

    public void hardTranslate(float x, float y) {
        hardTransform(new Affine2().setToTranslation(x, y));
    }

    public void hardScale(float sx, float sy) {
        hardTransform(new Affine2().setToScaling(sx, sy));
    }


    public String getId() { return id; }


    public ComplexShape2 getById(String label) {
        if (id.equals(label))
            return this;
        for (ComplexShape2 child : children) {
            if (child.getById(label) != null)
                return child.getById(label);
        }
        return null;
    }


    public ArrayList<String> getIdList() {
        ArrayList<String> list = new ArrayList<>();
        list.add(id);
        for (ComplexShape2 child : children)
            list.addAll(child.getIdList());
        return list;
    }


    /**
     * Used by SGAnimator
     *
     * @param pre prefix to add to the names of parts
     * @return a list of part names prefixed with the given string, according to their hierarchy
     */
    public ArrayList<String> getIdListPre(String pre) {
        ArrayList<String> list = new ArrayList<>();
        list.add(pre + id);
        for (ComplexShape2 child : children) {
            for (String childId : child.getIdListPre("  ")) list.add(pre + childId);
        }
        return list;
    }


    public ArrayList<ComplexShape2> getPartsList() {
        ArrayList<ComplexShape2> list = new ArrayList<>();
        list.add(this);
        for (ComplexShape2 child : children) {
            list.addAll(child.getPartsList());
        }
        return list;
    }


    /*
    public Animation[] getAnimationList() {
        return animations;
    }

    public void setAnimationList(Animation[] animList) {
        animations = animList;
    }

    public void clearAnimationList() {
        animTransform.idt();
        animations = new Animation[0];
        resetColorMod();
    }*/


    /*
    public void update(float dtime) {
        // dtime is in seconds
        Vector2 pivotPoint = getLocalOrigin();
        if (!preTransform.isIdt()) {
            preTransform.applyTo(pivotPoint);
        }

        if (transitioning) {
            transitionTime += dtime;
            float t = transitionTime / transitionDuration;
            if (transitionTime >= transitionDuration) {
                transitioning = false;
                t = 1.0f;
            }
            animTransform.setToTranslation(pivotPoint);
            animTransform.mul(Animation.lerpAffine(oldTransform, nextTransform, t));
            animTransform.translate(-pivotPoint.x, -pivotPoint.y);
        }
        else if (animations.length > 0) {
            animTransform.setToTranslation(pivotPoint);
            System.arraycopy(tint, 0, colorMod, 0, tint.length);
            for (int i = animations.length - 1; i >= 0; i--) {
                animations[i].update(dtime);
                if (animations[i].getAxe() < 6) {
                    animTransform.mul(animations[i].getTransform());
                } else {
                    float[] animColorMod = animations[i].getColorMod();
                    colorMod[0] += animColorMod[0];
                    colorMod[1] += animColorMod[1];
                    colorMod[2] += animColorMod[2];
                    colorMod[3] *= animColorMod[3];
                }
            }
            animTransform.translate(-pivotPoint.x, -pivotPoint.y);
        }

        for (ComplexShape2 child : children)
            child.update(dtime);
    }*/


    // Maybe could put a "fixtransform" parameter here
    /*public void transitionAnimation(Animation[] nextAnims, float duration) {
        // duration is in seconds
        if (animations.length > 0) {
            transitioning = true;
            transitionDuration = duration;
            transitionTime = 0;
            oldTransform.idt();
            for (Animation anim : animations) {
                oldTransform.preMul(anim.getTransform());
            }
            nextTransform.idt();
            for (Animation anim : nextAnims) {
                nextTransform.preMul(anim.getTransform());
            }
        }
        animations = nextAnims;
    }*/


    /**
     *  Apply a constant tint to the shape
     */
    public void setTint(Color color) {
        setTint(color.r, color.g, color.b, color.a);
    }

    /**
     *  Apply a constant tint to the shape
     */
    public void setTint(float r, float g, float b, float a) {
        tint[0] = r;
        tint[1] = g;
        tint[2] = b;
        tint[3] = a;
    }

    public float[] getTint() { return tint; }


    public void draw(Renderer renderer) {
        // preTransform is applied BEFORE animTransform
        //renderer.pushMatrix(animTransform);
        renderer.pushMatrix(preTransform);
        //renderer.pushColorMod(colorMod);
        for (Shape shape : shapes)
            shape.draw(renderer);
        //renderer.popColorMod();
        renderer.popMatrix();
        //renderer.popMatrix();
    }


    public void draw(Renderer renderer, PostureTree pt) {
        renderer.pushColorMod(pt.getColorMod());
        renderer.pushMatrix(pt.getTransform());
        renderer.pushMatrix(preTransform);
        int i = 0;
        for (Shape shape : shapes) {
            if (shape instanceof ComplexShape2)
                ((ComplexShape2) shape).draw(renderer, pt.getChildren().get(i++));
            else
                shape.draw(renderer);
        }
        renderer.popMatrix();
        renderer.popMatrix();
        renderer.popColorMod();
    }


    // Used by processing animation editor
    public void drawSelected(PRenderer renderer) {
        //renderer.pushMatrix(pt.getTransform());
        renderer.pushMatrix(preTransform);
        for (Shape shape : shapes)
            shape.drawSelected(renderer);
        renderer.popMatrix();
        //renderer.popMatrix();
    }

    // Used for processing animation editor
    /*public void drawSelectedOnly(PRenderer renderer) {
        if (renderer.getSelected() == this) {
            // Highlight this shape
            drawSelected(renderer);
        } else {
            renderer.pushMatrix(animTransform);
            renderer.pushMatrix(preTransform);
            for (ComplexShape2 shape : children)
                shape.drawSelectedOnly(renderer);
            renderer.popMatrix();
            renderer.popMatrix();
        }
    }*/


    /**
     * V 2.0
     * Used by SGAnimator
     * Draw the pivot point of this shape while animated
     */
    public void drawPivot(Renderer renderer, PostureTree pt) {
        Vector2 point = getLocalOrigin();
        getAbsoluteTransform(pt).applyTo(point);
        //renderer.getTransform().applyTo(point);
        renderer.color.set(0f, 0f, 1f, 1f);
        renderer.circle(point.x, point.y, 4/renderer.getTransform().m00);

    }


    public short getNumIndices() {
        short n = 0;
        for (Shape shape : shapes) {
            n += shape.getNumIndices();
        }
        return n;
    }

    public int getNumVerts() {
        int n = 0;
        for (Shape shape : shapes) {
            n += shape.getNumVerts();
        }
        return n;
    }


    public ComplexShape2 copy() {
        // XXX: should choose an id
        ComplexShape2 copy = new ComplexShape2("");
        for (Shape shape : shapes)
            copy.addShape(shape.copy());
        copy.setLocalOrigin(localOrigin);
        return copy;
    }


    // Used by processing animation editor
    public static ComplexShape2 fromPShape(PShape svgShape) {
        Shape shape = fromPShape(svgShape, new PMatrix3D(), 0);
        if (!(shape instanceof ComplexShape2)) {
            ComplexShape2 cs = new ComplexShape2("root");
            cs.addShape(shape);
            return cs;
        }
        ((ComplexShape2) shape).hardScale(0.1f, 0.1f);
        return (ComplexShape2) shape;
    }

    // Used by processing animation editor
    public static Shape fromPShape(PShape svgShape, PMatrix3D matrix, int depth) {
        //StringBuilder prefix = new StringBuilder();

        //for (int i=0; i<depth; i++)
        //    prefix.append('-');

        Shape shape = null;
        int family = svgShape.getFamily();
        int kind = svgShape.getKind();
        int childCount = svgShape.getChildCount();
        PMatrix3D mat = (PMatrix3D) (new DummyPShape(svgShape)).getMatrix();
        if (mat != null)
            matrix.apply(mat);

        if (childCount > 0) {
            ComplexShape2 cs = new ComplexShape2(svgShape.getName());
            for (PShape child : svgShape.getChildren()) {
                Shape childShape = fromPShape(child, matrix.get(), depth+1);
                if (childShape != null)
                    cs.addShape(childShape);
            }
            cs.localOrigin.set(cs.getBoundingBox().getCenter());

            shape = cs;
        }
        else if (family == PShape.PATH) {
            int vertexCount = svgShape.getVertexCount();
            int[] vertexCodes = svgShape.getVertexCodes();
            ArrayList<Float> verts = new ArrayList<>(); // vertex buffer for polygon

            if (svgShape.getVertexCodeCount() == 0) {  // each point is a simple vertex
                for (int i = 0; i < vertexCount; i++) {
                    PVector vertex = new PVector(svgShape.getVertexX(i), svgShape.getVertexY(i));
                    vertex = matrix.mult(vertex, null);
                    verts.add(vertex.x);
                    verts.add(vertex.y);
                }
            } else {  // coded set of vertices
                int codeIdx = 0;
                Vector2 prev, ctrl, ctrl2, end, point;
                PVector vertex;
                PVector ppoint;
                Bezier<Vector2> b;
                for (int j = 0; j < svgShape.getVertexCodeCount(); j++) {
                    switch (vertexCodes[j]) {
                        case VERTEX:
                            vertex = new PVector(svgShape.getVertexX(codeIdx), svgShape.getVertexY(codeIdx));
                            vertex = matrix.mult(vertex, null);
                            verts.add(vertex.x);
                            verts.add(vertex.y);
                            codeIdx++;
                            break;

                        case QUADRATIC_VERTEX:
                            prev = new Vector2(svgShape.getVertexX(codeIdx-1), svgShape.getVertexY(codeIdx-1));
                            ctrl = new Vector2(svgShape.getVertexX(codeIdx), svgShape.getVertexY(codeIdx));
                            end = new Vector2(svgShape.getVertexX(codeIdx+1), svgShape.getVertexY(codeIdx+1));
                            b = new Bezier<>(prev, ctrl, end);
                            // Approximate quadratic Bezier curve (8 steps)
                            point = new Vector2();
                            for (int i=0; i<=6; i++) {
                                b.valueAt(point, (float) i/6);
                                ppoint = matrix.mult(new PVector(point.x, point.y), null);
                                verts.add(ppoint.x);
                                verts.add(ppoint.y);
                            }
                            codeIdx += 2;
                            break;

                        case BEZIER_VERTEX:
                            prev = new Vector2(svgShape.getVertexX(codeIdx-1), svgShape.getVertexY(codeIdx-1));
                            ctrl = new Vector2(svgShape.getVertexX(codeIdx), svgShape.getVertexY(codeIdx));
                            ctrl2 = new Vector2(svgShape.getVertexX(codeIdx+1), svgShape.getVertexY(codeIdx+1));
                            end = new Vector2(svgShape.getVertexX(codeIdx+2), svgShape.getVertexY(codeIdx+2));
                            b = new Bezier(prev, ctrl, ctrl2, end);
                            // Approximate Bezier curve (8 steps)
                            point = new Vector2();
                            for (int i=0; i<=8; i++) {
                                b.valueAt(point, (float) i/8);
                                ppoint = matrix.mult(new PVector(point.x, point.y), null);
                                verts.add(ppoint.x);
                                verts.add(ppoint.y);
                            }
                            codeIdx += 3;
                            break;

                        case BREAK:
                            System.out.println("BREAK");
                            /*
                            if (insideContour) {
                                g.endContour();
                            }
                            g.beginContour();
                            insideContour = true;
                         */
                    }
                }
            }
            /*
            if (insideContour) {
                g.endContour();
            }*/

            float[] verticesArray = new float[verts.size()];
            for (int i=0; i<verticesArray.length; i++)
                verticesArray[i] = verts.get(i);
            DrawablePolygon poly = new DrawablePolygon(verticesArray);
            try { poly.setColor(pColorToGDXColor(svgShape.getFill(999))); }
            catch (Exception e) { poly.setColor(0.f, 0.f, 0.f, 1.f); e.printStackTrace(); }
            shape = poly;
        }
        else if (family == PShape.PRIMITIVE ) {
            float[] params = svgShape.getParams();
            if (kind == PShape.ELLIPSE) {
                float r = params[2];
                // params[0], params[1] is top-left coordinate
                PVector center = matrix.mult(new PVector(params[0]+r/2, params[1]+r/2), null);
                PVector radiusPoint = matrix.mult(new PVector(params[0]+ r, 0), null);
                DrawableCircle c = new DrawableCircle(center.x, center.y, radiusPoint.x-center.x);
                try { c.setColor(pColorToGDXColor(svgShape.getFill(0))); }
                catch (Exception e) { c.setColor(0.f, 0.f, 0.f, 1.f); e.printStackTrace(); }
                shape = c;
            }
            else if (kind == PShape.RECT) {
                float x = params[0];
                float y = params[1];
                float width = params[2];
                float height = params[3];
                PVector p0 = matrix.mult(new PVector(x, y), null);
                PVector p1 = matrix.mult(new PVector(x+width, y), null);
                PVector p2 = matrix.mult(new PVector(x+width, y+height), null);
                PVector p3 = matrix.mult(new PVector(x, y+height), null);
                float[] verts = new float[] {p0.x, p0.y, p1.x, p1.y, p2.x, p2.y, p3.x, p3.y};
                DrawablePolygon poly = new DrawablePolygon(verts);
                try { poly.setColor(pColorToGDXColor(svgShape.getFill(999))); }
                catch (Exception e) { poly.setColor(0.f, 0.f, 0.f, 1.f); e.printStackTrace(); }
                shape = poly;
            }
        }

        return shape;
    }


    public static ComplexShape2 fromJson(JsonValue json) {
        ComplexShape2 cs = new ComplexShape2(json.getString("id"));
        if (json.has("shapes")) {
            for (JsonValue shape : json.get("shapes")) {
                if (shape.has("type")) {  // Treat as simple shape
                    String type = shape.getString("type");
                    if (type.equals("polygon")) {
                        DrawablePolygon p = new DrawablePolygon();
                        p.setVertices(shape.get("vertices").asFloatArray());
                        p.setIndices(shape.get("triangles").asShortArray());
                        float[] c = shape.get("color").asFloatArray();
                        p.setColor(c[0], c[1], c[2], c[3]);

                        cs.addShape(p);
                    } else if (type.equals("circle")) {
                        float[] params = shape.get("params").asFloatArray();
                        DrawableCircle c = new DrawableCircle(params[0], params[1], params[2]);
                        float[] co = shape.get("color").asFloatArray();
                        c.setColor(co[0], co[1], co[2], co[3]);
                        cs.addShape(c);
                    }
                } else if (shape.has("id")) {  // Treat as ComplexShape
                    cs.addShape(fromJson(shape));
                }
            }
        }

        if (json.has("origin")) {
            float[] coord = json.get("origin").asFloatArray();
            cs.setLocalOrigin(new Vector2(coord[0], coord[1]));
        }

        if (json.has("transform")) {
            float[] transform = json.get("transform").asFloatArray();
            cs.preTransform.m00 = transform[0];
            cs.preTransform.m01 = transform[1];
            cs.preTransform.m02 = transform[2];
            cs.preTransform.m10 = transform[3];
            cs.preTransform.m11 = transform[4];
            cs.preTransform.m12 = transform[5];
        }

        return cs;
    }


    private float roundPrec(float val, float prec) {
        return Math.round(val * prec) / prec;
    }

    public JsonValue toJson() {
        return toJson(8);
    }

    public JsonValue toJson(int precision) {
        float prec = (float) Math.pow(2, precision);

        JsonValue json = new JsonValue(JsonValue.ValueType.object);
        json.addChild("id", new JsonValue(id));

        JsonValue origin = new JsonValue(JsonValue.ValueType.array);
        origin.addChild(new JsonValue(roundPrec(localOrigin.x, prec)));
        origin.addChild(new JsonValue(roundPrec(localOrigin.y, prec)));
        json.addChild("origin", origin);

        if (!preTransform.isIdt()) {
            JsonValue transform = new JsonValue(JsonValue.ValueType.array);
            transform.addChild(new JsonValue(preTransform.m00));
            transform.addChild(new JsonValue(preTransform.m01));
            transform.addChild(new JsonValue(preTransform.m02));
            transform.addChild(new JsonValue(preTransform.m10));
            transform.addChild(new JsonValue(preTransform.m11));
            transform.addChild(new JsonValue(preTransform.m12));
            json.addChild("transform", transform);
        }

        JsonValue shapes = new JsonValue(JsonValue.ValueType.array);
        for (Shape shape : getShapes()) {
            if (shape instanceof ComplexShape2) {
                shapes.addChild(((ComplexShape2) shape).toJson());
            } else if (shape instanceof DrawableCircle) {
                DrawableCircle c = (DrawableCircle) shape;
                JsonValue s = new JsonValue(JsonValue.ValueType.object);
                s.addChild("type", new JsonValue("circle"));

                JsonValue colorArray = new JsonValue(JsonValue.ValueType.array);
                colorArray.addChild(new JsonValue(roundPrec(c.getColor().r, 256f)));
                colorArray.addChild(new JsonValue(roundPrec(c.getColor().g, 256f)));
                colorArray.addChild(new JsonValue(roundPrec(c.getColor().b, 256f)));
                colorArray.addChild(new JsonValue(roundPrec(c.getColor().a, 256f)));
                s.addChild("color", colorArray);

                JsonValue paramsArray = new JsonValue(JsonValue.ValueType.array);
                paramsArray.addChild(new JsonValue(roundPrec(c.getCenter().x, prec)));
                paramsArray.addChild(new JsonValue(roundPrec(c.getCenter().y, prec)));
                paramsArray.addChild(new JsonValue(roundPrec(c.getRadius(), prec)));
                paramsArray.addChild(new JsonValue(c.getSegments()));
                s.addChild("params", paramsArray);

                shapes.addChild(s);
            } else if (shape instanceof DrawablePolygon) {
                DrawablePolygon p = (DrawablePolygon) shape;
                JsonValue s = new JsonValue(JsonValue.ValueType.object);
                s.addChild("type", new JsonValue("polygon"));

                JsonValue colorArray = new JsonValue(JsonValue.ValueType.array);
                colorArray.addChild(new JsonValue(roundPrec(p.getColor().r, 256f)));
                colorArray.addChild(new JsonValue(roundPrec(p.getColor().g, 256f)));
                colorArray.addChild(new JsonValue(roundPrec(p.getColor().b, 256f)));
                colorArray.addChild(new JsonValue(roundPrec(p.getColor().a, 256f)));
                s.addChild("color", colorArray);

                JsonValue verticesArray = new JsonValue(JsonValue.ValueType.array);
                for (float vert : p.getVertices()) {
                    // No use trying to round values here...
                    verticesArray.addChild(new JsonValue(roundPrec(vert, prec)));
                }
                s.addChild("vertices", verticesArray);

                JsonValue trianglesArray = new JsonValue(JsonValue.ValueType.array);
                for (short triangle : p.getIndices()) {
                    trianglesArray.addChild(new JsonValue(triangle));
                }
                s.addChild("triangles", trianglesArray);

                shapes.addChild(s);
            }
        }
        json.addChild("shapes", shapes);
        return json;
    }


    public String toString() {
        String s = String.format(" [id: %s origin: %.1f %.1f  children: %d]",
                id, localOrigin.x, localOrigin.y, children.size());
        return super.toString() + s;
    }
}
