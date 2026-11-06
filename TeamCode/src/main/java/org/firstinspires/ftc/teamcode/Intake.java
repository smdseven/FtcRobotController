package org.firstinspires.ftc.teamcode;

import android.content.Context;
import android.content.SharedPreferences;

import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.util.ElapsedTime;

/*
 * Shared intake motor for every OpMode.
 *
 * The Driver Hub configuration assigns the Expansion Hub port to intake_motor.
 * This class is the one place that name is written down.
 *
 * Forward uses the saved speed. Reverse uses half of that speed. The signs below
 * keep the mechanism direction. A switch between those two directions holds the
 * motor at zero for a short pause so it is not commanded straight from one
 * direction to the other. Call update() once per loop.
 *
 * If the motor spins the wrong way on the first test, flip FORWARD_SIGN and
 * REVERSE_SIGN here.
 *
 * The speed is stored in the Control Hub app's shared preferences. init() reads
 * it before the motor is commanded, and a change is committed immediately.
 */
public class Intake {

    public enum Direction {
        FORWARD,
        REVERSE,
        STOPPED
    }

    public static final String MOTOR_NAME = "intake_motor";
    public static final double REVERSAL_PAUSE_SECONDS = 0.1;

    private static final String PREFS_NAME = "intake_trim";
    private static final String KEY_SPEED = "speed";
    private static final double INITIAL_SPEED = 1.0;
    private static final double FORWARD_SIGN = -1.0;
    private static final double REVERSE_SIGN = 1.0;
    private static final double REVERSE_FRACTION = 0.5;

    private DcMotor motor = null;
    private Direction applied = Direction.STOPPED;
    private Direction pending = null;
    private double speed = INITIAL_SPEED;
    private final ElapsedTime pauseTimer = new ElapsedTime();

    private final HardwareMap hardwareMap;
    private final SharedPreferences preferences;

    public Intake(HardwareMap hardwareMap) {
        this.hardwareMap = hardwareMap;
        preferences = hardwareMap.appContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public void init() {
        speed = clamp(preferences.getFloat(KEY_SPEED, (float) INITIAL_SPEED));
        motor = hardwareMap.get(DcMotor.class, MOTOR_NAME);
        motor.setDirection(DcMotor.Direction.FORWARD);
        motor.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        motor.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        apply(Direction.STOPPED);
    }

    public void adjustSpeed(double delta) {
        if (delta == 0) {
            return;
        }
        double updated = clamp(speed + delta);
        if (updated == speed) {
            return;
        }
        speed = updated;
        save();
    }

    public double getSpeed() {
        return speed;
    }

    public void save() {
        preferences.edit().putFloat(KEY_SPEED, (float) speed).commit();
    }

    public void request(Direction direction) {
        if (direction == Direction.STOPPED) {
            pending = null;
            apply(Direction.STOPPED);
            return;
        }

        if (pending != null && direction == applied) {
            pending = null;
            apply(direction);
            return;
        }

        if (pending != null && direction == pending) {
            return;
        }

        if (direction == applied) {
            return;
        }

        if (applied == Direction.STOPPED) {
            apply(direction);
            return;
        }

        pending = direction;
        motor.setPower(0);
        pauseTimer.reset();
    }

    public void update() {
        if (pending != null) {
            if (pauseTimer.seconds() >= REVERSAL_PAUSE_SECONDS) {
                Direction next = pending;
                pending = null;
                apply(next);
            }
            return;
        }
        if (applied != Direction.STOPPED) {
            apply(applied);
        }
    }

    public String status() {
        if (pending != null) {
            return "switching";
        }
        if (applied == Direction.FORWARD) {
            return "forward";
        }
        if (applied == Direction.REVERSE) {
            return "reverse";
        }
        return "off";
    }

    private void apply(Direction direction) {
        applied = direction;
        if (direction == Direction.FORWARD) {
            motor.setPower(FORWARD_SIGN * speed);
        } else if (direction == Direction.REVERSE) {
            motor.setPower(REVERSE_SIGN * speed * REVERSE_FRACTION);
        } else {
            motor.setPower(0);
        }
    }

    private static double clamp(double value) {
        return Math.max(0.0, Math.min(1.0, value));
    }
}
