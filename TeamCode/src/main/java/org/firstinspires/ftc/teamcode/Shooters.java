package org.firstinspires.ftc.teamcode;

import android.content.Context;
import android.content.SharedPreferences;

import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.HardwareMap;

/*
 * Shared nectar and pollen shooter motors for every OpMode.
 *
 * The Driver Hub configuration assigns the Expansion Hub ports to nectar_shooter
 * and pollen_shooter. This class is the one place those names are written down.
 * The two shooters stay together because they are the same kind of mechanism.
 *
 * If a motor spins the wrong way on the first test, flip FORWARD and REVERSE here.
 *
 * The trimmed power for each shooter is stored in the Control Hub app's shared
 * preferences and loaded again the next time an OpMode starts.
 */
public class Shooters {

    public static final String NECTAR = "nectar_shooter";
    public static final String POLLEN = "pollen_shooter";
    public static final double POWER_CHANGE_PER_SECOND = 0.20;

    private static final String PREFS_NAME = "shooter_trim";
    private static final String KEY_NECTAR = "nectar";
    private static final String KEY_POLLEN = "pollen";
    private static final double INITIAL_POWER = 1.0;

    private DcMotor nectarShooter = null;
    private DcMotor pollenShooter = null;

    private double nectarPower = INITIAL_POWER;
    private double pollenPower = INITIAL_POWER;
    private boolean nectarRunning = false;
    private boolean pollenRunning = false;

    private final HardwareMap hardwareMap;
    private final SharedPreferences preferences;

    public Shooters(HardwareMap hardwareMap) {
        this.hardwareMap = hardwareMap;
        preferences = hardwareMap.appContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public void init() {
        loadSetpoints();
        nectarShooter = hardwareMap.get(DcMotor.class, NECTAR);
        pollenShooter = hardwareMap.get(DcMotor.class, POLLEN);
        prepare(nectarShooter);
        prepare(pollenShooter);
        nectarShooter.setDirection(DcMotor.Direction.REVERSE);
        setNectarRunning(false);
        setPollenRunning(false);
    }

    public void setNectarRunning(boolean running) {
        nectarRunning = running;
        applyNectarPower();
    }

    public void setPollenRunning(boolean running) {
        pollenRunning = running;
        applyPollenPower();
    }

    public void adjustNectarPower(double delta) {
        double updated = clampPower(nectarPower + delta);
        if (updated == nectarPower) {
            return;
        }
        nectarPower = updated;
        if (nectarRunning) {
            applyNectarPower();
        }
        storeSetpoints(false);
    }

    public void adjustPollenPower(double delta) {
        double updated = clampPower(pollenPower + delta);
        if (updated == pollenPower) {
            return;
        }
        pollenPower = updated;
        if (pollenRunning) {
            applyPollenPower();
        }
        storeSetpoints(false);
    }

    public void save() {
        storeSetpoints(true);
    }

    public boolean isNectarRunning() {
        return nectarRunning;
    }

    public boolean isPollenRunning() {
        return pollenRunning;
    }

    public double getNectarPower() {
        return nectarPower;
    }

    public double getPollenPower() {
        return pollenPower;
    }

    private void applyNectarPower() {
        nectarShooter.setPower(nectarRunning ? nectarPower : 0);
    }

    private void applyPollenPower() {
        pollenShooter.setPower(pollenRunning ? pollenPower : 0);
    }

    private double clampPower(double power) {
        return Math.max(0.0, Math.min(1.0, power));
    }

    private void loadSetpoints() {
        nectarPower = clampPower(preferences.getFloat(KEY_NECTAR, (float) INITIAL_POWER));
        pollenPower = clampPower(preferences.getFloat(KEY_POLLEN, (float) INITIAL_POWER));
    }

    private void storeSetpoints(boolean durable) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putFloat(KEY_NECTAR, (float) nectarPower);
        editor.putFloat(KEY_POLLEN, (float) pollenPower);
        if (durable) {
            editor.commit();
        } else {
            editor.apply();
        }
    }

    private void prepare(DcMotor motor) {
        motor.setDirection(DcMotor.Direction.FORWARD);
        motor.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        motor.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        motor.setPower(0);
    }
}
