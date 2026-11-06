package org.firstinspires.ftc.teamcode;

import android.content.Context;
import android.content.SharedPreferences;

import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.HardwareMap;

/*
 * Shared mecanum drive for every OpMode.
 *
 * The Driver Hub configuration assigns each motor port to a name. This class is the
 * one place those names are written down. Teleop and autonomous both call init(), so
 * they use the same ports, the same directions, and the same wheel math.
 *
 * When a sensor is added, give it a name in the Driver Hub configuration and look it
 * up here. Do not repeat hardwareMap.get in each OpMode.
 *
 * After the wheel powers are mixed, one saved percentage scales all four motors.
 * drive() uses the max-speed percentage. The slow overload uses the slow-speed
 * percentage by itself. The two percentages are stored in the Control Hub app's
 * shared preferences, read in init(), and committed as soon as they change.
 */
public class MecanumDrive {

    public static final String FRONT_LEFT = "front_left_drive";
    public static final String BACK_LEFT = "back_left_drive";
    public static final String FRONT_RIGHT = "front_right_drive";
    public static final String BACK_RIGHT = "back_right_drive";

    private static final String PREFS_NAME = "drive_trim";
    private static final String KEY_MAX = "max";
    private static final String KEY_SLOW = "slow";
    private static final double INITIAL_MAX = 1.0;
    private static final double INITIAL_SLOW = 0.30;

    private DcMotor frontLeftDrive = null;
    private DcMotor backLeftDrive = null;
    private DcMotor frontRightDrive = null;
    private DcMotor backRightDrive = null;
    private double maxScale = INITIAL_MAX;
    private double slowScale = INITIAL_SLOW;

    private final HardwareMap hardwareMap;
    private final SharedPreferences preferences;

    public MecanumDrive(HardwareMap hardwareMap) {
        this.hardwareMap = hardwareMap;
        preferences = hardwareMap.appContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public void init() {
        maxScale = clamp(preferences.getFloat(KEY_MAX, (float) INITIAL_MAX));
        slowScale = clamp(preferences.getFloat(KEY_SLOW, (float) INITIAL_SLOW));
        frontLeftDrive = hardwareMap.get(DcMotor.class, FRONT_LEFT);
        backLeftDrive = hardwareMap.get(DcMotor.class, BACK_LEFT);
        frontRightDrive = hardwareMap.get(DcMotor.class, FRONT_RIGHT);
        backRightDrive = hardwareMap.get(DcMotor.class, BACK_RIGHT);

        // Left motors are reversed so a positive power drives the robot forward.
        // If a wheel spins the wrong way on the first test, flip that motor here.
        frontLeftDrive.setDirection(DcMotor.Direction.REVERSE);
        backLeftDrive.setDirection(DcMotor.Direction.REVERSE);
        frontRightDrive.setDirection(DcMotor.Direction.FORWARD);
        backRightDrive.setDirection(DcMotor.Direction.FORWARD);

        frontLeftDrive.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        backLeftDrive.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        frontRightDrive.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        backRightDrive.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);

        frontLeftDrive.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        backLeftDrive.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        frontRightDrive.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        backRightDrive.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);

        stop();
    }

    /**
     * Drive using three requests, each from -1.0 to 1.0, then the max-speed percentage.
     * axial: forward is positive. lateral: right is positive. yaw: clockwise is positive.
     */
    public void drive(double axial, double lateral, double yaw) {
        drive(axial, lateral, yaw, false);
    }

    /**
     * Same wheel mix as {@link #drive(double, double, double)}. When slow is true, the
     * slow-speed percentage is the only scale. The max-speed percentage is not applied.
     */
    public void drive(double axial, double lateral, double yaw, boolean slow) {
        double frontLeftPower = axial + lateral + yaw;
        double frontRightPower = axial - lateral - yaw;
        double backLeftPower = axial - lateral + yaw;
        double backRightPower = axial + lateral - yaw;

        double max = Math.max(Math.abs(frontLeftPower), Math.abs(frontRightPower));
        max = Math.max(max, Math.abs(backLeftPower));
        max = Math.max(max, Math.abs(backRightPower));
        if (max > 1.0) {
            frontLeftPower /= max;
            frontRightPower /= max;
            backLeftPower /= max;
            backRightPower /= max;
        }

        double scale = slow ? slowScale : maxScale;
        frontLeftDrive.setPower(frontLeftPower * scale);
        frontRightDrive.setPower(frontRightPower * scale);
        backLeftDrive.setPower(backLeftPower * scale);
        backRightDrive.setPower(backRightPower * scale);
    }

    public void adjustMax(double delta) {
        double updated = change(maxScale, delta);
        if (updated == maxScale) {
            return;
        }
        maxScale = updated;
        save();
    }

    public void adjustSlow(double delta) {
        double updated = change(slowScale, delta);
        if (updated == slowScale) {
            return;
        }
        slowScale = updated;
        save();
    }

    public double getMaxScale() {
        return maxScale;
    }

    public double getSlowScale() {
        return slowScale;
    }

    public void save() {
        preferences.edit()
                .putFloat(KEY_MAX, (float) maxScale)
                .putFloat(KEY_SLOW, (float) slowScale)
                .commit();
    }

    public void stop() {
        drive(0, 0, 0);
    }

    private static double change(double current, double delta) {
        if (delta == 0) {
            return current;
        }
        return clamp(current + delta);
    }

    private static double clamp(double value) {
        return Math.max(0.0, Math.min(1.0, value));
    }
}
