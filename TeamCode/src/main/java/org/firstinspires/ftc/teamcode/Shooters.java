package org.firstinspires.ftc.teamcode;

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
 */
public class Shooters {

    public static final String NECTAR = "nectar_shooter";
    public static final String POLLEN = "pollen_shooter";
    public static final double RUNNING_POWER = 1.0;

    private DcMotor nectarShooter = null;
    private DcMotor pollenShooter = null;

    private boolean nectarRunning = false;
    private boolean pollenRunning = false;

    private final HardwareMap hardwareMap;

    public Shooters(HardwareMap hardwareMap) {
        this.hardwareMap = hardwareMap;
    }

    public void init() {
        nectarShooter = hardwareMap.get(DcMotor.class, NECTAR);
        pollenShooter = hardwareMap.get(DcMotor.class, POLLEN);
        prepare(nectarShooter);
        prepare(pollenShooter);
        setNectarRunning(false);
        setPollenRunning(false);
    }

    public void setNectarRunning(boolean running) {
        nectarRunning = running;
        nectarShooter.setPower(running ? RUNNING_POWER : 0);
    }

    public void setPollenRunning(boolean running) {
        pollenRunning = running;
        pollenShooter.setPower(running ? RUNNING_POWER : 0);
    }

    public boolean isNectarRunning() {
        return nectarRunning;
    }

    public boolean isPollenRunning() {
        return pollenRunning;
    }

    private void prepare(DcMotor motor) {
        motor.setDirection(DcMotor.Direction.FORWARD);
        motor.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        motor.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        motor.setPower(0);
    }
}
