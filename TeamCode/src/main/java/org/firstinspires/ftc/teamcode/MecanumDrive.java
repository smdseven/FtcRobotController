package org.firstinspires.ftc.teamcode;

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
 */
public class MecanumDrive {

    public static final String FRONT_LEFT = "front_left_drive";
    public static final String BACK_LEFT = "back_left_drive";
    public static final String FRONT_RIGHT = "front_right_drive";
    public static final String BACK_RIGHT = "back_right_drive";

    private DcMotor frontLeftDrive = null;
    private DcMotor backLeftDrive = null;
    private DcMotor frontRightDrive = null;
    private DcMotor backRightDrive = null;

    private final HardwareMap hardwareMap;

    public MecanumDrive(HardwareMap hardwareMap) {
        this.hardwareMap = hardwareMap;
    }

    public void init() {
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
     * Drive using three requests, each from -1.0 to 1.0.
     * axial: forward is positive. lateral: right is positive. yaw: clockwise is positive.
     */
    public void drive(double axial, double lateral, double yaw) {
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

        frontLeftDrive.setPower(frontLeftPower);
        frontRightDrive.setPower(frontRightPower);
        backLeftDrive.setPower(backLeftPower);
        backRightDrive.setPower(backRightPower);
    }

    public void stop() {
        drive(0, 0, 0);
    }
}
