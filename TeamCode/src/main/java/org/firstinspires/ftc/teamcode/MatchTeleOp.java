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
 * Circle runs the intake forward, Square runs it in reverse at half power, and Cross stops it.
 * A direction change pauses briefly at zero power before the motor reverses.
 * A fresh L2 press runs one nectar shot. A fresh R2 press runs one pollen shot.
 * The cycle spins the wheel up, opens the gate, closes the gate, then stops the wheel.
 * L1 cancels nectar and R1 cancels pollen. The gate then blocks and that wheel stops.
 * Holding the driver's R2 uses the saved slow-speed percentage alone. Releasing it
 * returns to the saved max-speed percentage.
 * Hold Triangle for 3 seconds to enter trim. A later press leaves trim.
 * D-pad up and down choose one saved value. The right stick raises or lowers it.
 */

@TeleOp(name = "Match Teleop", group = "Drive")
public class MatchTeleOp extends LinearOpMode {

    private static final float STICK_DEADZONE = 0.05f;
    private static final double TRIM_ENTER_HOLD_SECONDS = 3.0;

    private ElapsedTime runtime = new ElapsedTime();
    private ElapsedTime loopTimer = new ElapsedTime();
    private boolean speedTrimEnabled = false;
    private double triangleHoldSeconds = 0;
    private boolean ignoreTriangleUntilRelease = false;
    private TrimTarget trimTarget = TrimTarget.NECTAR_SPEED;
    private Shooters shooters;

    @Override
    public void runOpMode() {
        MecanumDrive drive = new MecanumDrive(hardwareMap);
        Intake intake = new Intake(hardwareMap);
        shooters = new Shooters(hardwareMap);
        drive.init();
        intake.init();
        shooters.init();

        telemetry.addData("Status", "Initialized");
        telemetry.update();

        waitForStart();
        runtime.reset();
        loopTimer.reset();

        while (opModeIsActive()) {
            double loopSeconds = loopTimer.seconds();
            loopTimer.reset();

            // Left stick forward is negative, so flip it. Positive axial means drive forward.
            double axial = -gamepad1.left_stick_y;
            double lateral = gamepad1.left_stick_x;
            double yaw = gamepad1.right_stick_x;

            boolean slowDrive = gamepad1.right_trigger_pressed;
            drive.drive(axial, lateral, yaw, slowDrive);
            readIntakeControls(intake);
            readOperatorControls(drive, intake, shooters, loopSeconds);

            telemetry.addData("Status", "Run Time: " + runtime.toString());
            telemetry.addData("Forward / Strafe / Turn", "%4.2f, %4.2f, %4.2f", axial, lateral, yaw);
            telemetry.addData("Drive", slowDrive ? "slow %d%%" : "max %d%%",
                    Math.round((slowDrive ? drive.getSlowScale() : drive.getMaxScale()) * 100));
            telemetry.addData("Intake", intake.status());
            telemetry.addData("Nectar / Pollen", "%s / %s",
                    shooters.nectarStatus(),
                    shooters.pollenStatus());
            if (speedTrimEnabled) {
                telemetry.addData("Trim", trimTarget.label());
                telemetry.addData("Value", formatTrim(trimTarget, intake, drive));
            } else {
                telemetry.addData("Trim", "off");
                telemetry.addData("Nectar / Pollen speed", "%d%% / %d%%",
                        Math.round(shooters.getNectarPower() * 100),
                        Math.round(shooters.getPollenPower() * 100));
            }
            telemetry.update();
        }

        shooters.stop();
        shooters.save();
        intake.save();
        drive.save();
    }

    private void readIntakeControls(Intake intake) {
        // Cross wins over Circle or Square. Circle and Square together are ignored so they cannot fight.
        if (gamepad2.cross) {
            intake.request(Intake.Direction.STOPPED);
        } else if (gamepad2.circle && !gamepad2.square) {
            intake.request(Intake.Direction.FORWARD);
        } else if (gamepad2.square && !gamepad2.circle) {
            intake.request(Intake.Direction.REVERSE);
        }
        intake.update();
    }

    private void readOperatorControls(MecanumDrive drive, Intake intake, Shooters shooters, double loopSeconds) {
        updateSpeedTrim(drive, intake, loopSeconds);

        if (speedTrimEnabled) {
            if (gamepad2.dpadUpWasPressed()) {
                trimTarget = adjacentTarget(-1);
            } else if (gamepad2.dpadDownWasPressed()) {
                trimTarget = adjacentTarget(1);
            }
            adjustTrim(
                    trimTarget,
                    stickDelta(gamepad2.right_stick_y, loopSeconds, trimTarget.changePerSecond()),
                    drive,
                    intake);
        }

        // An idle gate follows its open position only while that gate's open value is selected.
        shooters.setGatePreview(
                speedTrimEnabled && trimTarget == TrimTarget.NECTAR_OPEN,
                speedTrimEnabled && trimTarget == TrimTarget.POLLEN_OPEN);

        // The bumper wins when it is held with its trigger. Consume the trigger edge
        // so releasing the bumper does not count as a new shot.
        if (gamepad2.left_bumper) {
            shooters.cancelNectar();
            gamepad2.leftTriggerWasPressed();
        } else if (gamepad2.leftTriggerWasPressed()) {
            shooters.startNectar();
        }

        if (gamepad2.right_bumper) {
            shooters.cancelPollen();
            gamepad2.rightTriggerWasPressed();
        } else if (gamepad2.rightTriggerWasPressed()) {
            shooters.startPollen();
        }

        shooters.update(loopSeconds);
    }

    private TrimTarget adjacentTarget(int direction) {
        TrimTarget[] targets = TrimTarget.values();
        int index = trimTarget.ordinal() + direction;
        if (index < 0) {
            index = targets.length - 1;
        } else if (index >= targets.length) {
            index = 0;
        }
        return targets[index];
    }

    private void adjustTrim(TrimTarget target, double delta, MecanumDrive drive, Intake intake) {
        switch (target) {
            case NECTAR_SPEED:
                shooters.adjustNectar(Shooters.Part.SPEED, delta);
                break;
            case POLLEN_SPEED:
                shooters.adjustPollen(Shooters.Part.SPEED, delta);
                break;
            case INTAKE_SPEED:
                intake.adjustSpeed(delta);
                break;
            case DRIVE_MAX:
                drive.adjustMax(delta);
                break;
            case DRIVE_SLOW:
                drive.adjustSlow(delta);
                break;
            case NECTAR_BLOCK:
                shooters.adjustNectar(Shooters.Part.BLOCK, delta);
                break;
            case NECTAR_OPEN:
                shooters.adjustNectar(Shooters.Part.OPEN, delta);
                break;
            case POLLEN_BLOCK:
                shooters.adjustPollen(Shooters.Part.BLOCK, delta);
                break;
            case POLLEN_OPEN:
                shooters.adjustPollen(Shooters.Part.OPEN, delta);
                break;
            case NECTAR_SPIN_UP:
                shooters.adjustNectar(Shooters.Part.SPIN_UP, delta);
                break;
            case NECTAR_RELEASE:
                shooters.adjustNectar(Shooters.Part.RELEASE, delta);
                break;
            case NECTAR_CLOSE:
                shooters.adjustNectar(Shooters.Part.CLOSE, delta);
                break;
            case POLLEN_SPIN_UP:
                shooters.adjustPollen(Shooters.Part.SPIN_UP, delta);
                break;
            case POLLEN_RELEASE:
                shooters.adjustPollen(Shooters.Part.RELEASE, delta);
                break;
            case POLLEN_CLOSE:
                shooters.adjustPollen(Shooters.Part.CLOSE, delta);
                break;
            default:
                break;
        }
    }

    private String formatTrim(TrimTarget target, Intake intake, MecanumDrive drive) {
        switch (target) {
            case NECTAR_SPEED:
                return shooters.formatNectar(Shooters.Part.SPEED);
            case POLLEN_SPEED:
                return shooters.formatPollen(Shooters.Part.SPEED);
            case INTAKE_SPEED:
                return percent(intake.getSpeed());
            case DRIVE_MAX:
                return percent(drive.getMaxScale());
            case DRIVE_SLOW:
                return percent(drive.getSlowScale());
            case NECTAR_BLOCK:
                return shooters.formatNectar(Shooters.Part.BLOCK);
            case NECTAR_OPEN:
                return shooters.formatNectar(Shooters.Part.OPEN);
            case POLLEN_BLOCK:
                return shooters.formatPollen(Shooters.Part.BLOCK);
            case POLLEN_OPEN:
                return shooters.formatPollen(Shooters.Part.OPEN);
            case NECTAR_SPIN_UP:
                return shooters.formatNectar(Shooters.Part.SPIN_UP);
            case NECTAR_RELEASE:
                return shooters.formatNectar(Shooters.Part.RELEASE);
            case NECTAR_CLOSE:
                return shooters.formatNectar(Shooters.Part.CLOSE);
            case POLLEN_SPIN_UP:
                return shooters.formatPollen(Shooters.Part.SPIN_UP);
            case POLLEN_RELEASE:
                return shooters.formatPollen(Shooters.Part.RELEASE);
            case POLLEN_CLOSE:
                return shooters.formatPollen(Shooters.Part.CLOSE);
            default:
                return "";
        }
    }

    private static String percent(double fraction) {
        return Math.round(fraction * 100) + "%";
    }

    private void updateSpeedTrim(MecanumDrive drive, Intake intake, double loopSeconds) {
        if (!speedTrimEnabled) {
            if (gamepad2.triangle) {
                triangleHoldSeconds += loopSeconds;
                if (triangleHoldSeconds >= TRIM_ENTER_HOLD_SECONDS) {
                    speedTrimEnabled = true;
                    ignoreTriangleUntilRelease = true;
                    triangleHoldSeconds = 0;
                    // The press that started this hold stays stored until read.
                    // Clear it so releasing Triangle is not treated as the press that leaves trim.
                    gamepad2.triangleWasPressed();
                    // A D-pad press from before trim must not change the setting on entry.
                    gamepad2.dpadUpWasPressed();
                    gamepad2.dpadDownWasPressed();
                }
            } else {
                triangleHoldSeconds = 0;
            }
            return;
        }

        if (ignoreTriangleUntilRelease) {
            if (!gamepad2.triangle) {
                ignoreTriangleUntilRelease = false;
            }
            return;
        }

        if (gamepad2.triangleWasPressed()) {
            speedTrimEnabled = false;
            shooters.save();
            intake.save();
            drive.save();
        }
    }

    private double stickDelta(float stickY, double loopSeconds, double changePerSecond) {
        if (Math.abs(stickY) < STICK_DEADZONE) {
            return 0;
        }
        // Stick up is negative. Negating it makes up increase the setting and down decrease it.
        return -stickY * changePerSecond * loopSeconds;
    }

    private enum TrimTarget {
        NECTAR_SPEED("nectar speed"),
        POLLEN_SPEED("pollen speed"),
        INTAKE_SPEED("intake speed"),
        DRIVE_MAX("drive max"),
        DRIVE_SLOW("drive slow"),
        NECTAR_BLOCK("nectar block"),
        NECTAR_OPEN("nectar open"),
        POLLEN_BLOCK("pollen block"),
        POLLEN_OPEN("pollen open"),
        NECTAR_SPIN_UP("nectar spin-up"),
        NECTAR_RELEASE("nectar release"),
        NECTAR_CLOSE("nectar close"),
        POLLEN_SPIN_UP("pollen spin-up"),
        POLLEN_RELEASE("pollen release"),
        POLLEN_CLOSE("pollen close");

        private final String label;

        TrimTarget(String label) {
            this.label = label;
        }

        private String label() {
            return label;
        }

        private double changePerSecond() {
            switch (this) {
                case NECTAR_SPIN_UP:
                case NECTAR_RELEASE:
                case NECTAR_CLOSE:
                case POLLEN_SPIN_UP:
                case POLLEN_RELEASE:
                case POLLEN_CLOSE:
                    return Shooters.TIME_CHANGE_PER_SECOND;
                case NECTAR_BLOCK:
                case NECTAR_OPEN:
                case POLLEN_BLOCK:
                case POLLEN_OPEN:
                    return Shooters.POSITION_CHANGE_PER_SECOND;
                default:
                    return Shooters.POWER_CHANGE_PER_SECOND;
            }
        }
    }
}
