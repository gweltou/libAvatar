package gwel.game.anim;

import com.badlogic.gdx.math.Interpolation;


public class TFTimetable extends TimeFunction {
    private  int[] easingTable;
    private float[] valueTable;
    private Interpolation interpolation;
    private float timePerStep;
    private int prevStep, nextStep;
    private boolean loop;


    public TFTimetable() {
        this(2f, true, true, "linear");
    }

    public TFTimetable(float duration, boolean loop, boolean smoothend, String easing) {
        params = new TFParam[]{
                new TFParam<>("duration", TFParam.SLIDER, "s", 0.f, 10.f, duration),
                new TFParam<>("loop", TFParam.CHECKBOX, "", 0f, 1f, loop),
                new TFParam<>("smoothend", TFParam.CHECKBOX, "", 0, 1, smoothend),
                new TFParam<>("easing", TFParam.EASING, "", 0, 0, easing),
        };
        valueTable = new float[] {0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f};
        interpolation = Animation.getInterpolation(easing);
        reset();
    }


    public void setTable(float[] table) {
        valueTable = table;
        reset();
    }

    public void setTableValue(int idx, float value) { valueTable[idx] = value; }

    public float[] getTable() {
        return valueTable;
    }


    /*public void setEasing(String easingName) {
        interp = Animation.getInterpolation(easingName);
    }*/

    public void setEasingTable(int [] table) { easingTable = table; }

    public int[] getEasingTable() { return easingTable; }

    public void setEasingValue(int idx, int value) { easingTable[idx] = value; }


    public int[] getActiveStep() {
        return new int[]{prevStep, nextStep};
    }


    @Override
    public void update(float dtime) {
        if (state == TimeFunction.RUNNING) {
            super.update(dtime);
            if (time > timePerStep) {
                time -= timePerStep;
                if (prevStep == valueTable.length-1 && !((boolean) params[1].getValue())) {
                    // Smoothend is active but doesn't loop
                    state = STOPPED;
                    return;
                }
                prevStep = nextStep;
                nextStep++;
                if (nextStep >= valueTable.length) {
                    nextStep = 0;
                    if (!((boolean) params[2].getValue())) { // No smoothend
                        if ((boolean) params[1].getValue()) { // Loop ?
                            prevStep = 0;
                            nextStep = 1;
                        } else {
                            state = STOPPED;
                            return;
                        }
                    }
                }
            }
            value = interpolation.apply(valueTable[prevStep], valueTable[nextStep], time/timePerStep);
        }
    }


    @Override
    public void reset() {
        super.reset();
        prevStep = 0;
        nextStep = 1;
        state = TimeFunction.RUNNING;
        // Add an extra step if smoothend parameter is enabled
        int totalSteps = valueTable.length-1 + ((boolean) params[2].getValue() ? 1 : 0);
        timePerStep = (float) params[0].getValue() / totalSteps;
        interpolation = Animation.getInterpolation((String) params[3].getValue());
    }
}