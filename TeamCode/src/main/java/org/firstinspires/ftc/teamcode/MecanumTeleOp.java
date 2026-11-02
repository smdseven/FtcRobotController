/* Copyright (c) 2021 FIRST. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted (subject to the limitations in the disclaimer below) provided that
 * the following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice, this list
 * of conditions and the following disclaimer.
 *
 * Redistributions in binary form must reproduce the above copyright notice, this
 * list of conditions and the following disclaimer in the documentation and/or
 * other materials provided with the distribution.
 *
 * Neither the name of FIRST nor the names of its contributors may be used to endorse or
 * promote products derived from this software without specific prior written permission.
 *
 * NO EXPRESS OR IMPLIED LICENSES TO ANY PARTY'S PATENT RIGHTS ARE GRANTED BY THIS
 * LICENSE. THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.util.ElapsedTime;

/*
 * This file contains an example of a Linear "OpMode".
 * An OpMode is a 'program' that runs in either the autonomous or the teleop period of an FTC match.
 * The names of OpModes appear on the menu of the FTC Driver Station.
 * When a selection is made from the menu, the corresponding OpMode is executed.
 *
 * This particular OpMode illustrates driving a 4-motor Omni-Directional (or Holonomic) robot.
 * This code will work with either a Mecanum-Drive or an X-Drive train.
 * Both of these drives are illustrated at https://gm0.org/en/latest/docs/robot-design/drivetrains/holonomic.html
 * Note that a Mecanum drive must display an X roller-pattern when viewed from above.
 *
 * Also note that it is critical to set the correct rotation direction for each motor.  See details below.
 *
 * Holonomic drives provide the ability for the robot to move in three axes (directions) simultaneously.
 * Each motion axis is controlled by one Joystick axis.
 *
 * 1) Axial:    Driving forward and backward               Left-joystick Forward/Backward
 * 2) Lateral:  Strafing right and left                     Left-joystick Right and Left
 * 3) Yaw:      Rotating Clockwise and counter clockwise    Right-joystick Right and Left
 *
 * Motor names and directions live in MecanumDrive. If the robot drives backward on the first test,
 * flip FORWARD and REVERSE on the motors in that class.
 *
 * Gamepad 2 runs the intake through Intake and the shooters through Shooters.
 * Circle turns the intake on and Cross turns it off.
 * L2 turns the nectar shooter on and L1 turns it off.
 * R2 turns the pollen shooter on and R1 turns it off.
 * Each motor stays in that state until the other button is pressed.
 */

@TeleOp(name="Mecanum Drive", group="Drive")
public class MecanumTeleOp extends LinearOpMode {

    private static final float TRIGGER_PRESSED = 0.5f;

    private ElapsedTime runtime = new ElapsedTime();

    @Override
    public void runOpMode() {
        MecanumDrive drive = new MecanumDrive(hardwareMap);
        Intake intake = new Intake(hardwareMap);
        Shooters shooters = new Shooters(hardwareMap);
        drive.init();
        intake.init();
        shooters.init();

        telemetry.addData("Status", "Initialized");
        telemetry.update();

        waitForStart();
        runtime.reset();

        while (opModeIsActive()) {
            // Left stick forward is negative, so flip it. Positive axial means drive forward.
            double axial = -gamepad1.left_stick_y;
            double lateral = gamepad1.left_stick_x;
            double yaw = gamepad1.right_stick_x;

            drive.drive(axial, lateral, yaw);
            readIntakeControls(intake);
            readShooterControls(shooters);

            telemetry.addData("Status", "Run Time: " + runtime.toString());
            telemetry.addData("Forward / Strafe / Turn", "%4.2f, %4.2f, %4.2f", axial, lateral, yaw);
            telemetry.addData("Intake", intake.isRunning() ? "on" : "off");
            telemetry.addData("Nectar / Pollen", "%s / %s",
                    shooters.isNectarRunning() ? "on" : "off",
                    shooters.isPollenRunning() ? "on" : "off");
            telemetry.update();
        }
    }

    private void readIntakeControls(Intake intake) {
        // Off wins when both buttons are pressed, so a held button cannot force the motor to stay on.
        if (gamepad2.cross) {
            intake.setRunning(false);
        } else if (gamepad2.circle) {
            intake.setRunning(true);
        }
    }

    private void readShooterControls(Shooters shooters) {
        if (gamepad2.left_bumper) {
            shooters.setNectarRunning(false);
        } else if (gamepad2.left_trigger > TRIGGER_PRESSED) {
            shooters.setNectarRunning(true);
        }

        if (gamepad2.right_bumper) {
            shooters.setPollenRunning(false);
        } else if (gamepad2.right_trigger > TRIGGER_PRESSED) {
            shooters.setPollenRunning(true);
        }
    }
}
