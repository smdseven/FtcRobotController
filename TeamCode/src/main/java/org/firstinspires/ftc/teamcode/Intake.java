package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.HardwareMap;

/*
 * Shared intake motor for every OpMode.
 *
 * The Driver Hub configuration assigns the Expansion Hub port to intake_motor.
 * This class is the one place that name is written down.
 *
 * If the motor spins the wrong way on the first test, flip FORWARD and REVERSE here.
 */
public class Intake {

    public static final String MOTOR_NAME = "intake_motor";
    public static final double RUNNING_POWER = 1.0;

    private DcMotor motor = null;
    private boolean running = false;

    private final HardwareMap hardwareMap;

    public Intake(HardwareMap hardwareMap) {
        this.hardwareMap = hardwareMap;
    }

    public void init() {
        motor = hardwareMap.get(DcMotor.class, MOTOR_NAME);
        motor.setDirection(DcMotor.Direction.FORWARD);
        motor.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        motor.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        setRunning(false);
    }

    public void setRunning(boolean running) {
        this.running = running;
        motor.setPower(running ? RUNNING_POWER : 0);
    }

    public boolean isRunning() {
        return running;
    }
}
