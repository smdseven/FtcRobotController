package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.util.ElapsedTime;

/*
 * Shared intake motor for every OpMode.
 *
 * The Driver Hub configuration assigns the Expansion Hub port to intake_motor.
 * This class is the one place that name is written down.
 *
 * Forward is full power. Reverse is half power. A switch between those two
 * directions holds the motor at zero for a short pause so it is not commanded
 * straight from one direction to the other. Call update() once per loop.
 *
 * If the motor spins the wrong way on the first test, flip FORWARD and REVERSE here.
 */
public class Intake {

    public enum Direction {
        FORWARD,
        REVERSE,
        STOPPED
    }

    public static final String MOTOR_NAME = "intake_motor";
    public static final double FORWARD_POWER = 1.0;
    public static final double REVERSE_POWER = -0.5;
    public static final double REVERSAL_PAUSE_SECONDS = 0.1;

    private DcMotor motor = null;
    private Direction applied = Direction.STOPPED;
    private Direction pending = null;
    private final ElapsedTime pauseTimer = new ElapsedTime();

    private final HardwareMap hardwareMap;

    public Intake(HardwareMap hardwareMap) {
        this.hardwareMap = hardwareMap;
    }

    public void init() {
        motor = hardwareMap.get(DcMotor.class, MOTOR_NAME);
        motor.setDirection(DcMotor.Direction.FORWARD);
        motor.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        motor.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        apply(Direction.STOPPED);
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
        if (pending != null && pauseTimer.seconds() >= REVERSAL_PAUSE_SECONDS) {
            Direction next = pending;
            pending = null;
            apply(next);
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
            motor.setPower(FORWARD_POWER);
        } else if (direction == Direction.REVERSE) {
            motor.setPower(REVERSE_POWER);
        } else {
            motor.setPower(0);
        }
    }
}
