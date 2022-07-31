package gwel.game.anim;


import com.badlogic.gdx.math.Affine2;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.JsonValue;

import java.util.Arrays;


public class Animation2 {
    public static final int AXE_X = 0;
    public static final int AXE_Y = 1;
    public static final int AXE_ROT = 2;
    public static final int AXE_SX = 3;
    public static final int AXE_SY = 4;
    public static final int AXE_Z = 5;
    public static final int AXE_ALPHA = 6;
    public static final int AXE_RGB = 7;
    public static final int AXE_R = 8;
    public static final int AXE_G = 9;
    public static final int AXE_B = 10;
    // Must be the same order as the enums
    public static final String[] axeNames = {"x", "y", "rotation", "scale x", "scale y", "zoom", "alpha", "rgb", "r", "g", "b"};
    public static final Class[] timeFunctions = {
            //TFConstant.class,
            TFEaseFromTo.class,
            TFTimetable.class,
            TFSin.class,
            TFSpin.class,
            TFRandomEase.class,
            TFRandomBlink.class,
    };
    public static final String[] interpolationNames = new String[]{
            "linear",
            "smooth", "smooth2", "smoother",
            "fade",
            "slowFast", "fastSlow",
            "pow2", "pow2In", "pow2Out", "pow2InInverse", "pow2OutInverse",
            "pow3", "pow3In", "pow3Out", "pow3InInverse", "pow3OutInverse",
            "pow4", "pow4In", "pow4Out",
            "pow5", "pow5In", "pow5Out",
            "sine", "sineIn", "sineOut",
            "exp5",  "exp5In", "exp5Out",
            "exp10", "exp10In", "exp10Out",
            "circle", "circleIn", "circleOut",
            "elastic", "elasticIn", "elasticOut",
            "swing", "swingIn", "swingOut",
            "bounce", "bounceIn", "bounceOut"
    };
    public static final String[] interpolationNamesSimp = new String[]{
            "linear",
            "smooth",
            "slowFast", "fastSlow",
            "pow2", "pow2InInverse", "pow2OutInverse",
            "pow3", "pow3In", "pow3Out", "pow3InInverse", "pow3OutInverse",
            "pow4", "pow4In", "pow4Out",
            "pow5", "pow5In", "pow5Out",
            "exp10", "exp10In", "exp10Out",
            "circle", "circleIn", "circleOut",
            "elastic", "elasticIn", "elasticOut",
            "swing", "swingIn", "swingOut",
            "bounce", "bounceIn", "bounceOut"
    };
    private TimeFunction fn;
    private int axe = AXE_X;
    private final Affine2 transform = new Affine2();
    private final float[] colorMod = new float[] {0f, 0f, 0f, 1f};
    private static final float[] noColor = new float[] {0f, 0f, 0f, 1f};
    private float amp = 1.0f;
    private boolean inv = false;
    private float mult = 1.0f;
    private final boolean isActive = true;
    private float value;


    public Animation2(TimeFunction fn) {
        this.fn = fn;
    }

    public Animation2(TimeFunction fn, int axe) {
        this(fn);
        setAxe(axe);
    }

    /*public Animation2(TimeFunction fn, int axe, float amp) {
        this(fn, axe);
        setAmp(amp);
    }*/


    public static Interpolation getInterpolation(int n) {
        try {
            return (Interpolation) Interpolation.class.getField(interpolationNamesSimp[n]).get(null);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Interpolation getInterpolation(String name) {
        try {
            return (Interpolation) Interpolation.class.getField(name).get(null);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    public void setFunction(TimeFunction fn) {
        this.fn = fn;
    }

    public TimeFunction getFunction() {
        return fn;
    }


    public void setParam(String paramName, float paramValue) { fn.setParam(paramName, paramValue); }


    public void setAxe(int axe) {
        this.axe = axe;
        if (axe == AXE_ROT) {
            amp = 360;
        } else if (axe == AXE_SX || axe == AXE_SY || axe == AXE_Z || axe >= AXE_ALPHA) {
            amp = 1;
        }
        mult = amp * (inv ? -1 : 1);
    }

    public int getAxe() { return this.axe; }


    public void setAmp(float amp) {
        this.amp = amp;
        mult = amp * (inv ? -1 : 1);
    }

    public float getAmp() { return amp; }


    public void setInv(boolean inv) {
        this.inv = inv;
        mult = amp * (inv ? -1 : 1);
    }

    public boolean getInv() { return inv; }


    public void scale(float scale) {
        if (axe == AXE_X || axe == AXE_Y) {
            if (scale < 0) {
                scale *= -1;
                inv = !inv;
            }
            amp *= scale;
            mult = amp * (inv ? -1 : 1);
        }
    }

    public Vector2 getScale() {
        float sx = axe == AXE_SX || axe == AXE_Z ? value : 1.0f;
        float sy = axe == AXE_SY || axe == AXE_Z ? value : 1.0f;
        return new Vector2(sx, sy);
    }


    public Vector2 getTranslation() {
        float tx = axe == AXE_X ? value : 0.0f;
        // Reverse y axis
        float ty = axe == AXE_Y ? -value : 0.0f;
        return new Vector2(tx, ty);
    }


    public float getRotation() {
        return axe == AXE_ROT ? value : 0.0f;
    }


    //public float getValue() { return fn.getValue(); }

    public void update() { value = mult * fn.getValue(); }


    public Affine2 getTransform() {
        if (!isActive)
            return transform.idt();

        if (axe < AXE_ALPHA) {
            transform.setToTranslation(getTranslation());
            transform.scale(getScale());
            transform.rotate(getRotation());
        } else {
            colorMod[0] = axe == AXE_R || axe == AXE_RGB ? value : 0f;
            colorMod[1] = axe == AXE_G || axe == AXE_RGB ? value : 0f;
            colorMod[2] = axe == AXE_B || axe == AXE_RGB ? value : 0f;
            colorMod[3] = axe == AXE_ALPHA ? value : 1f;
        }
        return transform;
    }


    public float[] getColorMod() {
        if (isActive && axe >= AXE_ALPHA) {
            colorMod[0] = axe == AXE_R || axe == AXE_RGB ? value : 0f;
            colorMod[1] = axe == AXE_G || axe == AXE_RGB ? value : 0f;
            colorMod[2] = axe == AXE_B || axe == AXE_RGB ? value : 0f;
            colorMod[3] = axe == AXE_ALPHA ? value : 1f;
            return colorMod;
        }
        return noColor;
    }


    /*public void reset() {
        fn.reset();
        transform.idt();
    }*/


    //public boolean isStopped() { return fn.state == TimeFunction.STOPPED; }

    //public boolean isRunning() { return fn.state == TimeFunction.RUNNING; }


    /*
    public Animation2 copy() {
        try {
            TimeFunction fnCopy = fn.getClass().newInstance();
            fnCopy.setParams(fn.getParamsCopy());
            if (fnCopy instanceof TFTimetable)
                ((TFTimetable) fnCopy).setTable(((TFTimetable) fn).getTable().clone());
            Animation2 newAnimation = new Animation2(fnCopy, axe);
            newAnimation.setInv(inv);
            newAnimation.setAmp(amp);
            return newAnimation;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }*/


    public static Affine2 lerpAffine(Affine2 from, Affine2 to, float t) {
        Affine2 result = new Affine2();
        result.m00 = MathUtils.lerp(from.m00, to.m00, t);
        result.m01 = MathUtils.lerp(from.m01, to.m01, t);
        result.m02 = MathUtils.lerp(from.m02, to.m02, t);
        result.m10 = MathUtils.lerp(from.m10, to.m10, t);
        result.m11 = MathUtils.lerp(from.m11, to.m11, t);
        result.m12 = MathUtils.lerp(from.m12, to.m12, t);
        return result;
    }

    /*
    public static Animation2 fromJson(JsonValue json) {
        Animation2 anim = null;
        int axe = Arrays.asList(Animation2.axeNames).indexOf(json.getString("axe"));
        boolean inv = json.getBoolean("inv", false);
        float amp = json.getFloat("amp", 1.0f);

        try {
            String fullFunctionName = json.getString("function");
            // Dumb hack to read fully qualified function names
            String[] parts = fullFunctionName.split("[.]");
            Class<?> c = Class.forName("gwel.game.anim." + parts[parts.length-1]);
            TimeFunction fn = (TimeFunction) c.getDeclaredConstructor().newInstance();
            if (fn instanceof TFTimetable) {
                float[] table = json.get("table").asFloatArray();
                ((TFTimetable) fn).setTable(table);
            }
            for (TFParam param : fn.getParams()) {
                // Sets to default values if name is not found in json
                if (param.getValue() instanceof Float) {
                    param.setValue(json.getFloat(param.name, (float) param.getValue()));
                } else if (param.getValue() instanceof Boolean) {
                    param.setValue(json.getBoolean(param.name, (boolean) param.getValue()));
                } else if (param.getValue() instanceof Integer) {
                    param.setValue(json.getInt(param.name, (int) param.getValue()));
                } else if (param.getValue() instanceof String) {
                    param.setValue(json.getString(param.name, (String) param.getValue()));
                }
            }
            fn.reset();
            anim = new Animation2(fn, axe);
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Could not recreate animation function from json file");
            System.err.println(json);
        }
        anim.setAmp(amp);
        anim.setInv(inv);
        return anim;
    }*/


    /*public JsonValue toJson() {
        JsonValue json = new JsonValue(JsonValue.ValueType.object);
        String[] fullFunctionName = fn.getClass().getName().split("[.]");
        json.addChild("function", new JsonValue(fullFunctionName[fullFunctionName.length-1]));
        json.addChild("axe", new JsonValue(Animation2.axeNames[axe]));
        json.addChild("inv", new JsonValue(getInv()));
        json.addChild("amp", new JsonValue(getAmp()));

        if (fn instanceof TFTimetable) {
            JsonValue table = new JsonValue(JsonValue.ValueType.array);
            for (float value : ((TFTimetable) fn).getTable())
                table.addChild(new JsonValue(value));
            json.addChild("table", table);
        }
        // Function parameters
        for (TFParam param : fn.getParams()) {
            if (param.getValue() instanceof Float) {
                json.addChild(param.name, new JsonValue((float) param.getValue()));
            } else if (param.getValue() instanceof Boolean) {
                json.addChild(param.name, new JsonValue((boolean) param.getValue()));
            } else if (param.getValue() instanceof Integer) {
                json.addChild(param.name, new JsonValue((int) param.getValue()));
            } else if (param.getValue() instanceof String) {
                json.addChild(param.name, new JsonValue((String) param.getValue()));
            }
        }
        return json;
    }*/

    public String toString() {
        String s = String.format(" [fn: %s axe: %s amp: %.1f inv: %s]",
                fn, axeNames[axe], amp, inv);
        return super.toString() + s;
    }
}