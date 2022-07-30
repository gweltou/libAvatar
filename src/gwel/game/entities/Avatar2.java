package gwel.game.entities;

import com.badlogic.gdx.math.Affine2;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.JsonWriter;
import gwel.game.anim.*;
import gwel.game.graphics.*;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Set;


public class Avatar2 {
    private ComplexShape2 shape;
    public boolean paused = false;
    private final Vector2 position = new Vector2();
    private final Affine2 transform = new Affine2();
    private ComplexShape2[] partsList;
    public ArrayList<Shape> physicsShapes = new ArrayList<>();

    private boolean flipX = false;
    private boolean flipY = false;
    private float scaleX = 1;
    private float scaleY = 1;
    private float angle = 0;
    private float timeFactor = 1f;

    // V 2.0
    public BufferedRenderer bufferedRenderer;
    public final ArrayList<TimeFunction> timeFunctions = new ArrayList<>();
    private final ArrayList<Posture2> postures = new ArrayList<>();
    public Posture2 currentPosture;
    public boolean clonable = false;



    public void setPosition(float x, float y) { position.set(x, y); }

    public Vector2 getPosition() { return position.cpy(); }


    public void setAngle(float a) { angle = a; }

    public float getAngle() { return angle; }

    public void scale(float s) {
        scale(s, s);
    }

    public void scale(float sx, float sy) {
        scaleX *= sx;
        scaleY *= sy;
    }

    public void setScale(float sx, float sy) {
        scaleX = sx;
        scaleY = sy;
    }

    //public float getScaleX() { return scaleX; }
    //public float getScaleY() { return scaleY; }

    public void setFlipX(boolean flip) { flipX = flip; }
    public void setFlipY(boolean flip) { flipY = flip; }


    public void scalePhysics(float s) {
        Affine2 scaleTransform = new Affine2().setToScaling(s, s);
        for (Shape shape : physicsShapes) {
            if (shape.getClass() == DrawablePolygon.class) {
                DrawablePolygon polygon = (DrawablePolygon) shape;
                polygon.hardTransform(scaleTransform);
            } else if (shape.getClass() == DrawableCircle.class) {
                DrawableCircle circle = (DrawableCircle) shape;
                circle.hardTransform(scaleTransform);
            }
        }
    }


    public void timeScale(float s) { timeFactor = s; }


    /**
     * Check if a world point is inside the avatar's physic shape
     * A first draw call must have been made to initialise the transform matrix
     *
     * @param x
     * @param y
     * @return
     */
    public boolean contains(float x, float y) {
        Vector2 point = new Vector2(x, y);
        transform.inv().applyTo(point);
        for (Shape shape : physicsShapes) {
            if (shape.contains(point.x, point.y))
                return true;
        }
        return false;
    }


    public void setShape(ComplexShape2 root) {
        shape = root;
        partsList = root.getPartsList().toArray(new ComplexShape2[0]);
        bufferedRenderer = new BufferedRenderer();
        bufferedRenderer.setBufferSize(shape.getNumVerts(), shape.getNumIndices());
    }

    public ComplexShape2 getShape() { return shape; }


    public ComplexShape2[] getShapesList() { return partsList; }


    public String[] getPartsName() {
        return shape.getIdList().toArray(new String[0]);
    }

    /**
     * Used by SGAnimator
     */
    public String[] getPartsNamePre() {
        return shape.getIdListPre("").toArray(new String[0]);
    }


    // Should be used by animation editor only (slow)
    public void setPosture(HashMap<String, Animation[]> posture, float transition) {
        Set<String> ids = posture.keySet();
        for (ComplexShape2 part : partsList) {
            if (ids.contains(part.getId())) {
                Animation[] animList;
                animList = posture.get(part.getId());
                //part.transitionAnimation(animList, transition);
            } else {
                //part.transitionAnimation(new Animation[0], transition);
            }
        }
    }

    /*
    // Should be used by animation editor only (slow)
    public void setPosture(HashMap<String, Animation[]> posture) {
        setPosture(posture, 0.2f);
    }


    // Activate a posture from the collection
    public void loadPosture(int idx) {
        loadPosture(postures.getPosture(idx));
    }

    // Activate a posture from the collection
    public void loadPosture(String postureName) {
        loadPosture(postures.getPosture(postureName));
    }

    public void loadPosture(Posture posture) {
        for (int i = 0; i < partsList.length; i++) {
            Animation[] animList = posture.groups[i];
            if (animList != null) {
                //partsList[i].setAnimationList(animList);
            } else {
                //partsList[i].clearAnimationList();
            }
        }
    }


    // Play every animation from the animationCollection sequentially
    public void playSequentially() {

    }*/


    public void update(float dtime) {
        if (!paused && currentPosture != null)
            currentPosture.update(timeFactor * dtime);
    }

    public void resetAnimation() {
        currentPosture.reset();
    }

    /*public void clearAnimation() {
        for (ComplexShape part : partsList)
            part.clearAnimationList();
    }*/


    public void draw(Renderer renderer) {
        transform.setToTranslation(position.x, position.y);
        transform.scale(flipX ? -scaleX : scaleX, flipY ? -scaleY : scaleY);
        transform.rotateRad(angle);
        renderer.pushMatrix(transform);
        if (clonable) {
            bufferedRenderer.reset();
            shape.draw(bufferedRenderer, currentPosture.getPostureTree());
            ((PRenderer) renderer).coloredTriangles(bufferedRenderer.getVertices(), bufferedRenderer.getIndices());
        } else {
            shape.draw(renderer, currentPosture.getPostureTree());
        }
        renderer.popMatrix();
    }


    /**
     * Delete if outside Processing editor
     */
    /*public void drawSelectedOnly(PRenderer renderer) {
        shape.drawSelectedOnly(renderer);
    }*/


    /**
     * Make a copy of this Avatar, with same geometry but no strings attached
     *
     * @return a new Avatar
     */
    public Avatar2 copy() {
        Avatar2 newAvatar = new Avatar2();
        newAvatar.shape = shape.copy();
        for (Shape shape : physicsShapes)
            newAvatar.physicsShapes.add(shape.copy());
        newAvatar.scale(scaleX, scaleY);
        return newAvatar;
    }


    /**
     * Make a clone of this Avatar
     * The geometry and animations will be shared between the clones and their parent
     *
     * @return a cloned Avatar
     */
    public Avatar2 clone() {
        return null;
    }


    public static Avatar2 fromFile(File file) {
        return fromFile(file, true);
    }

    public static Avatar2 fromFile(File file, boolean loadAnim) {
        JsonValue json;
        try {
            InputStream in = new FileInputStream(file);
            json = new JsonReader().parse(in);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        } catch (com.badlogic.gdx.utils.SerializationException e) {
            System.out.println("Corrupted file");
            //e.printStackTrace();
            return null;
        }

        if (json.has("fmt_ver")) {
            String versionString = json.getString("fmt_ver");
            if (versionString.equals("1.0"))
                return load_v1(json, loadAnim);
            else if (versionString.equals("2"))
                return load_v2(json, loadAnim);
            else
                return load_v1(json, loadAnim);
        } else {
            return load_v1(json, loadAnim);
        }
    }

    private static Avatar2 load_v1(JsonValue json, boolean loadAnim) {
        System.out.println("SGA file version 1");
        Avatar2 avatar = new Avatar2();

        // Load shape first
        if (json.has("geometry")) {
            JsonValue jsonGeometry = json.get("geometry");
            avatar.setShape(ComplexShape2.fromJson(jsonGeometry));
        } else {
            System.out.println("No geometry data found !");
            return null;
        }

        if (loadAnim && json.has("animation")) {
            for (JsonValue jsonPosture : json.get("animation")) {
                Posture2 posture = new Posture2();
                posture.setName(jsonPosture.getString("name"));
                posture.setDuration(jsonPosture.getFloat("duration", 0f));
                posture.setPostureTree(PostureTree.buildTree(avatar.getShape(), null));
                for (JsonValue jsonGroup : jsonPosture.get("groups")) {
                    String partId = jsonGroup.getString("id");
                    for (JsonValue jsonAnimFunc : jsonGroup.get("functions")) {
                        Animation2 anim = new Animation2(TimeFunction.fromJson(jsonAnimFunc));
                        int axe = Arrays.asList(Animation2.axeNames).indexOf(jsonAnimFunc.getString("axe"));
                        anim.setAxe(axe);
                        anim.setAmp(jsonAnimFunc.getFloat("amp"));
                        anim.setInv(jsonAnimFunc.getBoolean("inv"));
                        posture.addAnimation(partId, anim);
                    }
                }
                avatar.postures.add(posture);
            }
            avatar.currentPosture = avatar.postures.get(0);
        }

        if (json.has("box2d")) {
            for (JsonValue jsonShape : json.get("box2d")) {
                if (jsonShape.getString("type").equals("circle")) {
                    DrawableCircle circle = new DrawableCircle(0, 0, 0);
                    circle.setCenter(jsonShape.getFloat("x"), jsonShape.getFloat("y"));
                    circle.setRadius(jsonShape.getFloat("radius"));
                    avatar.physicsShapes.add(circle);
                } else if (jsonShape.getString("type").equals("polygon")) {
                    DrawablePolygon polygon = new DrawablePolygon();
                    polygon.setVertices(jsonShape.get("vertices").asFloatArray());
                    avatar.physicsShapes.add(polygon);
                }
            }
        }
        return avatar;
    }

    private static Avatar2 load_v2(JsonValue json, boolean loadAnim) {
        System.out.println("SGA file version 2");
        Avatar2 avatar = new Avatar2();

        // Load shape first
        if (json.has("geometry")) {
            JsonValue jsonGeometry = json.get("geometry");
            avatar.setShape(ComplexShape2.fromJson(jsonGeometry));
        } else {
            System.out.println("No geometry data found !");
            return null;
        }

        if (loadAnim && json.has("animation")) {
            JsonValue jsonAnimation = json.get("animation");

            if (jsonAnimation.has("functions")) {
                for (JsonValue fn : jsonAnimation.get("functions")) {
                    avatar.timeFunctions.add(TimeFunction.fromJson(fn));
                }
            }
            if (jsonAnimation.has("postures")) {
                for (JsonValue jsonPosture : jsonAnimation.get("postures")) {
                    Posture2 posture = new Posture2();
                    posture.setName(jsonPosture.getString("name"));
                    posture.setDuration(jsonPosture.getFloat("duration"));
                    posture.setPostureTree(PostureTree.buildTree(avatar.getShape(), null));
                    for (JsonValue jsonPart : jsonPosture.get("parts")) {
                        String partId = jsonPart.getString("part_id");
                        for (JsonValue jsonAnim : jsonPart.get("animations")) {
                            int fnIdx = jsonAnim.getInt("function_id");
                            Animation2 anim = new Animation2(avatar.timeFunctions.get(fnIdx));
                            int axe = Arrays.asList(Animation2.axeNames).indexOf(jsonAnim.getString("axe"));
                            anim.setAxe(axe);
                            anim.setAmp(jsonAnim.getFloat("amp"));
                            anim.setInv(jsonAnim.getBoolean("inv"));
                            posture.addAnimation(partId, anim);
                        }
                    }
                    avatar.postures.add(posture);
                }
                avatar.currentPosture = avatar.postures.get(0);
            }
        }

        if (json.has("box2d")) {
            for (JsonValue jsonShape : json.get("box2d")) {
                if (jsonShape.getString("type").equals("circle")) {
                    DrawableCircle circle = new DrawableCircle(0, 0, 0);
                    circle.setCenter(jsonShape.getFloat("x"), jsonShape.getFloat("y"));
                    circle.setRadius(jsonShape.getFloat("radius"));
                    avatar.physicsShapes.add(circle);
                } else if (jsonShape.getString("type").equals("polygon")) {
                    DrawablePolygon polygon = new DrawablePolygon();
                    polygon.setVertices(jsonShape.get("vertices").asFloatArray());
                    avatar.physicsShapes.add(polygon);
                }
            }
        }
        return avatar;
    }

    public void saveFile(String filename) { saveFile_v2(filename); }

    public void saveFile_v1(String filename) {
        JsonValue json = new JsonValue(JsonValue.ValueType.object);

        json.addChild("library version", new JsonValue(PRenderer.version()));
        json.addChild("format version", new JsonValue("1.0"));

        /*
        if (postures != null)
            json.addChild("animation", postures.toJson(getPartsName()));

         */

        // Box2D shapes
        JsonValue jsonPhysicsShapes = new JsonValue(JsonValue.ValueType.array);
        for (Shape shape : physicsShapes) {
            JsonValue jsonShape = new JsonValue(JsonValue.ValueType.object);
            if (shape.getClass() == DrawablePolygon.class) {
                jsonShape.addChild("type", new JsonValue("polygon"));
                JsonValue jsonVertices = new JsonValue(JsonValue.ValueType.array);
                float[] vertices = ((DrawablePolygon) shape).getVertices();
                for (float vert : vertices)
                    jsonVertices.addChild(new JsonValue(vert));
                jsonShape.addChild("vertices", jsonVertices);
            } else if (shape.getClass() == DrawableCircle.class) {
                DrawableCircle circle = (DrawableCircle) shape;
                jsonShape.addChild("type", new JsonValue("circle"));
                jsonShape.addChild("x", new JsonValue(circle.getCenter().x));
                jsonShape.addChild("y", new JsonValue(circle.getCenter().y));
                jsonShape.addChild("radius", new JsonValue(circle.getRadius()));
            }
            jsonPhysicsShapes.addChild(jsonShape);
        }
        json.addChild("box2d", jsonPhysicsShapes);
        json.addChild("geometry", shape.toJson());

        try {
            FileWriter writer = new FileWriter(filename);
            writer.write(json.prettyPrint(JsonWriter.OutputType.json, 80));
            writer.close();
            System.out.println("Avatar data saved to " + filename);
        } catch (IOException e) {
            System.out.println("An error occurred while writing to " + filename);
            e.printStackTrace();
        }
    }

    public void saveFile_v2(String filename) {
        JsonValue json = new JsonValue(JsonValue.ValueType.object);
        json.addChild("lib_ver", new JsonValue(PRenderer.version()));
        json.addChild("fmt_ver", new JsonValue("2.0"));

        /*
        if (postures != null) {
            json.addChild("animation", postures.toJson(getPartsName()));
        }*/

        // Box2D shapes
        JsonValue jsonPhysicsShapes = new JsonValue(JsonValue.ValueType.array);
        for (Shape shape : physicsShapes) {
            JsonValue jsonShape = new JsonValue(JsonValue.ValueType.object);
            if (shape.getClass() == DrawablePolygon.class) {
                jsonShape.addChild("type", new JsonValue("polygon"));
                JsonValue jsonVertices = new JsonValue(JsonValue.ValueType.array);
                float[] vertices = ((DrawablePolygon) shape).getVertices();
                for (float vert : vertices)
                    jsonVertices.addChild(new JsonValue(vert));
                jsonShape.addChild("vertices", jsonVertices);
            } else if (shape.getClass() == DrawableCircle.class) {
                DrawableCircle circle = (DrawableCircle) shape;
                jsonShape.addChild("type", new JsonValue("circle"));
                jsonShape.addChild("x", new JsonValue(circle.getCenter().x));
                jsonShape.addChild("y", new JsonValue(circle.getCenter().y));
                jsonShape.addChild("radius", new JsonValue(circle.getRadius()));
            }
            jsonPhysicsShapes.addChild(jsonShape);
        }

        JsonValue jsonAvatar = new JsonValue(JsonValue.ValueType.object);
        jsonAvatar.addChild("box2d", jsonPhysicsShapes);
        jsonAvatar.addChild("geometry", shape.toJson());

        JsonValue jsonAvatars = new JsonValue(JsonValue.ValueType.array);
        jsonAvatars.addChild(jsonAvatar);

        json.addChild("avatars", jsonAvatars);

        try {
            FileWriter writer = new FileWriter(filename);
            writer.write(json.prettyPrint(JsonWriter.OutputType.json, 80));
            writer.close();
            System.out.println("Avatar data saved to " + filename);
        } catch (IOException e) {
            System.out.println("An error occurred while writing to " + filename);
            e.printStackTrace();
        }
    }


    public String toString() {
        String s = String.format(" []");
        return super.toString() + s;
    }
}
