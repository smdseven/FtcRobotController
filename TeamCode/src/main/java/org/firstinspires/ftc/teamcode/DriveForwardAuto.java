package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.util.ElapsedTime;

/*
 * Drives the mecanum robot straight forward for three seconds, then stops.
 * Motor names and directions come from MecanumDrive, the same class teleop uses.
 */
@Autonomous(name = "Drive Forward", group = "Auto")
public class DriveForwardAuto extends LinearOpMode {

    static final double FORWARD_POWER = 0.6;
    static final double DRIVE_SECONDS = 3.0;

    @Override
    public void runOpMode() {
        MecanumDrive drive = new MecanumDrive(hardwareMap);
        drive.init();

        telemetry.addData("Status", "Ready to drive forward");
        telemetry.update();

        waitForStart();

        ElapsedTime runtime = new ElapsedTime();
        drive.drive(FORWARD_POWER, 0, 0);
        while (opModeIsActive() && runtime.seconds() < DRIVE_SECONDS) {
            telemetry.addData("Path", "Forward: %4.1f s", runtime.seconds());
            telemetry.update();
        }

        drive.stop();
        telemetry.addData("Path", "Complete");
        telemetry.update();
    }
}
