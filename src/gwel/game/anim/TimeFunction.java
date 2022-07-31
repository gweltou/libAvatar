package gwel.game.anim;


import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.JsonValue;

public abstract class TimeFunction {
    public static final int STOPPED = 0;
    public static final int RUNNING = 1;
    public static final int DELAY = 2;
    public static final int REVERSE = 3;

    protected TFParam<Object>[] params;
    protected float time = 0.f;
    protected float value;
    protected int state = RUNNING;


    public TFParam<Object> getParam(String paramName) {
        for (TFParam<Object> param : params) {
            if (param.name.equals(paramName))
                return param;
        }
        return null;
    }

    public TFParam<Object>[] getParams() { return params; }

    public TFParam<Object>[] getParamsCopy() {
        TFParam<Object>[] paramsCopy = new TFParam[params.length];
        int i=0;
        for (TFParam<Object> param : params) {
            paramsCopy[i++] = param.copy();
        }
        return paramsCopy;
    }

    // Because of ControlP5's limitation this function will only get float payloads
    public void setParam(String name, float value) {
        for (TFParam<Object> param : params) {
            if (param.name.equals(name)) {
                if (param.getValue() instanceof Integer) {
                    // Convert from float to integer value
                    param.setValue(MathUtils.floor(value));
                } else if (param.getValue() instanceof Boolean) {
                    // Convert from float to boolean value
                    param.setValue(value > 0.5f);
                } else {
                    param.setValue(value);
                }
                break;
            }
        }
        reset();
    }

    public void setParam(String name, String value) {
        for (TFParam<Object> param : params) {
            if (param.name.equals(name)) {
                param.setValue(value);
                break;
            }
        }
        reset();
    }

    // setParams should be used only to make copies of the same TimeFunction
    public void setParams(TFParam<Object>[] otherParams) {
        params = otherParams;
        reset();
    }


    public int getState() { return state; }

    public void update(float dtime) {
        time += dtime;
    }

    public void reset() {
        time = 0;
    }

    public float getValue() { return value; };


    public static TimeFunction fromJson(JsonValue json) {
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
            return fn;
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Could not recreate animation function from json file");
            System.err.println(json);
            return null;
        }
    }


    public JsonValue toJson() {
        JsonValue json = new JsonValue(JsonValue.ValueType.object);
        String[] fullFunctionName = getClass().getName().split("[.]");
        json.addChild("function", new JsonValue(fullFunctionName[fullFunctionName.length-1]));
        // TimeTable values
        if (this instanceof TFTimetable) {
            JsonValue table = new JsonValue(JsonValue.ValueType.array);
            for (float value : ((TFTimetable) this).getTable())
                table.addChild(new JsonValue(value));
            json.addChild("table", table);
        }
        // Function parameters
        for (TFParam param : getParams()) {
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
    }


    public String toString() {
        String s = String.format(" [value: %.1f]", value);
        return super.toString() + s;
    }
}
