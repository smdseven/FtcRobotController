package org.firstinspires.ftc.teamcode;

import android.content.Context;
import android.content.SharedPreferences;

import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.PwmControl;
import com.qualcomm.robotcore.hardware.Servo;

import java.util.Locale;

/*
 * Shared nectar and pollen shooters for every OpMode.
 *
 * The Driver Hub configuration assigns the Expansion Hub ports to nectar_shooter,
 * pollen_shooter, nectar_gate, and pollen_gate. This class is the one place those
 * names are written down. Each gate is a goBILDA 2000-0025-0002 left in its factory
 * positional mode. The PWM range is widened here to that servo's 500-2500 microsecond
 * travel, so position 0.0 and 1.0 are the ends of its 300 degrees.
 *
 * If a motor or a gate moves the wrong way on the first test, flip that direction below.
 *
 * An activation runs one timed cycle: spin the wheel up with the gate blocking, open
 * the gate, close the gate while the wheel is still spinning, then stop the wheel.
 * Call update() once per loop. Autonomous code uses the same start, cancel, and update
 * methods and waits until the shooter is no longer busy.
 *
 * Speed, both gate positions, and the three wait times are stored in the Control Hub
 * app's shared preferences. init() reads them before a motor or servo is commanded,
 * and a change is committed immediately so the next run, match, or reboot keeps it.
 */
public class Shooters {

    public enum Part {
        SPEED,
        BLOCK,
        OPEN,
        SPIN_UP,
        RELEASE,
        CLOSE
    }

    public static final String NECTAR = "nectar_shooter";
    public static final String POLLEN = "pollen_shooter";
    public static final String NECTAR_GATE = "nectar_gate";
    public static final String POLLEN_GATE = "pollen_gate";

    public static final double POWER_CHANGE_PER_SECOND = 0.20;
    public static final double POSITION_CHANGE_PER_SECOND = 0.20;
    public static final double TIME_CHANGE_PER_SECOND = 0.50;

    private static final String PREFS_NAME = "shooter_trim";
    private static final double INITIAL_POWER = 1.0;
    private static final double INITIAL_BLOCK = 0.15;
    private static final double INITIAL_OPEN = 0.55;
    private static final double INITIAL_SPIN_UP_SECONDS = 0.75;
    private static final double INITIAL_RELEASE_SECONDS = 1.00;
    private static final double INITIAL_CLOSE_SECONDS = 0.40;
    private static final double MIN_SECONDS = 0.05;
    private static final double MAX_SECONDS = 5.0;
    private static final double GATE_PULSE_LOWER_US = 500;
    private static final double GATE_PULSE_UPPER_US = 2500;

    private final HardwareMap hardwareMap;
    private final SharedPreferences preferences;
    private final Lane nectar;
    private final Lane pollen;

    public Shooters(HardwareMap hardwareMap) {
        this.hardwareMap = hardwareMap;
        preferences = hardwareMap.appContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        nectar = new Lane(
                NECTAR,
                NECTAR_GATE,
                DcMotor.Direction.REVERSE,
                Servo.Direction.FORWARD,
                "nectar",
                "nectar_block",
                "nectar_open",
                "nectar_spin_up",
                "nectar_release",
                "nectar_close");
        pollen = new Lane(
                POLLEN,
                POLLEN_GATE,
                DcMotor.Direction.FORWARD,
                Servo.Direction.FORWARD,
                "pollen",
                "pollen_block",
                "pollen_open",
                "pollen_spin_up",
                "pollen_release",
                "pollen_close");
    }

    public void init() {
        nectar.load();
        pollen.load();
        nectar.bind();
        pollen.bind();
    }

    public void update(double seconds) {
        nectar.update(seconds);
        pollen.update(seconds);
    }

    public void startNectar() {
        nectar.start();
    }

    public void startPollen() {
        pollen.start();
    }

    public void cancelNectar() {
        nectar.cancel();
    }

    public void cancelPollen() {
        pollen.cancel();
    }

    public void stop() {
        nectar.previewOpen = false;
        pollen.previewOpen = false;
        nectar.cancel();
        pollen.cancel();
    }

    public boolean isNectarBusy() {
        return nectar.isBusy();
    }

    public boolean isPollenBusy() {
        return pollen.isBusy();
    }

    public String nectarStatus() {
        return nectar.status();
    }

    public String pollenStatus() {
        return pollen.status();
    }

    public double getNectarPower() {
        return nectar.power;
    }

    public double getPollenPower() {
        return pollen.power;
    }

    /**
     * While trim is showing one gate's open position, that idle gate moves there so it can be aimed.
     * The other gate is left alone. Any other choice leaves an idle gate blocking.
     */
    public void setGatePreview(boolean nectarOpen, boolean pollenOpen) {
        nectar.previewOpen = nectarOpen;
        pollen.previewOpen = pollenOpen;
    }

    public void adjustNectar(Part part, double delta) {
        if (!nectar.adjust(part, delta)) {
            return;
        }
        store();
        nectar.applyOutputs();
    }

    public void adjustPollen(Part part, double delta) {
        if (!pollen.adjust(part, delta)) {
            return;
        }
        store();
        pollen.applyOutputs();
    }

    public String formatNectar(Part part) {
        return format(part, nectar.value(part));
    }

    public String formatPollen(Part part) {
        return format(part, pollen.value(part));
    }

    private static String format(Part part, double value) {
        if (part == Part.SPEED) {
            return String.format(Locale.US, "%d%%", Math.round(value * 100));
        }
        if (part == Part.BLOCK || part == Part.OPEN) {
            return String.format(Locale.US, "%.2f", value);
        }
        return String.format(Locale.US, "%.2f s", value);
    }

    public void save() {
        store();
    }

    private void store() {
        SharedPreferences.Editor editor = preferences.edit();
        nectar.write(editor);
        pollen.write(editor);
        editor.commit();
    }

    private final class Lane {
        private final String motorName;
        private final String gateName;
        private final DcMotor.Direction motorDirection;
        private final Servo.Direction gateDirection;
        private final String powerKey;
        private final String blockKey;
        private final String openKey;
        private final String spinUpKey;
        private final String releaseKey;
        private final String closeKey;

        private DcMotor motor = null;
        private Servo gate = null;
        private boolean previewOpen = false;
        private Phase phase = Phase.IDLE;
        private double phaseSeconds = 0;
        private double power = INITIAL_POWER;
        private double block = INITIAL_BLOCK;
        private double open = INITIAL_OPEN;
        private double spinUpSeconds = INITIAL_SPIN_UP_SECONDS;
        private double releaseSeconds = INITIAL_RELEASE_SECONDS;
        private double closeSeconds = INITIAL_CLOSE_SECONDS;

        private Lane(
                String motorName,
                String gateName,
                DcMotor.Direction motorDirection,
                Servo.Direction gateDirection,
                String powerKey,
                String blockKey,
                String openKey,
                String spinUpKey,
                String releaseKey,
                String closeKey) {
            this.motorName = motorName;
            this.gateName = gateName;
            this.motorDirection = motorDirection;
            this.gateDirection = gateDirection;
            this.powerKey = powerKey;
            this.blockKey = blockKey;
            this.openKey = openKey;
            this.spinUpKey = spinUpKey;
            this.releaseKey = releaseKey;
            this.closeKey = closeKey;
        }

        private void load() {
            power = clamp(preferences.getFloat(powerKey, (float) INITIAL_POWER), 0, 1);
            block = clamp(preferences.getFloat(blockKey, (float) INITIAL_BLOCK), 0, 1);
            open = clamp(preferences.getFloat(openKey, (float) INITIAL_OPEN), 0, 1);
            spinUpSeconds = clamp(
                    preferences.getFloat(spinUpKey, (float) INITIAL_SPIN_UP_SECONDS), MIN_SECONDS, MAX_SECONDS);
            releaseSeconds = clamp(
                    preferences.getFloat(releaseKey, (float) INITIAL_RELEASE_SECONDS), MIN_SECONDS, MAX_SECONDS);
            closeSeconds = clamp(
                    preferences.getFloat(closeKey, (float) INITIAL_CLOSE_SECONDS), MIN_SECONDS, MAX_SECONDS);
        }

        private void bind() {
            motor = hardwareMap.get(DcMotor.class, motorName);
            motor.setDirection(motorDirection);
            motor.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
            motor.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);

            gate = hardwareMap.get(Servo.class, gateName);
            gate.setDirection(gateDirection);
            if (gate instanceof PwmControl) {
                ((PwmControl) gate).setPwmRange(
                        new PwmControl.PwmRange(GATE_PULSE_LOWER_US, GATE_PULSE_UPPER_US));
            }
            phase = Phase.IDLE;
            phaseSeconds = 0;
            applyOutputs();
        }

        private void start() {
            if (phase != Phase.IDLE) {
                return;
            }
            phase = Phase.SPIN_UP;
            phaseSeconds = 0;
            applyOutputs();
        }

        private void cancel() {
            phase = Phase.IDLE;
            phaseSeconds = 0;
            applyOutputs();
        }

        private void update(double seconds) {
            if (phase != Phase.IDLE) {
                phaseSeconds += seconds;
                while (phase != Phase.IDLE) {
                    double step = duration();
                    if (step <= 0 || phaseSeconds < step) {
                        break;
                    }
                    phaseSeconds -= step;
                    advance();
                }
            }
            applyOutputs();
        }

        private boolean adjust(Part part, double delta) {
            if (delta == 0) {
                return false;
            }
            if (part == Part.SPEED) {
                return assignPower(power + delta);
            }
            if (part == Part.BLOCK) {
                return assignBlock(block + delta);
            }
            if (part == Part.OPEN) {
                return assignOpen(open + delta);
            }
            if (part == Part.SPIN_UP) {
                return assignSpinUp(spinUpSeconds + delta);
            }
            if (part == Part.RELEASE) {
                return assignRelease(releaseSeconds + delta);
            }
            return assignClose(closeSeconds + delta);
        }

        private boolean assignPower(double updated) {
            updated = clamp(updated, 0, 1);
            if (updated == power) {
                return false;
            }
            power = updated;
            return true;
        }

        private boolean assignBlock(double updated) {
            updated = clamp(updated, 0, 1);
            if (updated == block) {
                return false;
            }
            block = updated;
            return true;
        }

        private boolean assignOpen(double updated) {
            updated = clamp(updated, 0, 1);
            if (updated == open) {
                return false;
            }
            open = updated;
            return true;
        }

        private boolean assignSpinUp(double updated) {
            updated = clamp(updated, MIN_SECONDS, MAX_SECONDS);
            if (updated == spinUpSeconds) {
                return false;
            }
            spinUpSeconds = updated;
            return true;
        }

        private boolean assignRelease(double updated) {
            updated = clamp(updated, MIN_SECONDS, MAX_SECONDS);
            if (updated == releaseSeconds) {
                return false;
            }
            releaseSeconds = updated;
            return true;
        }

        private boolean assignClose(double updated) {
            updated = clamp(updated, MIN_SECONDS, MAX_SECONDS);
            if (updated == closeSeconds) {
                return false;
            }
            closeSeconds = updated;
            return true;
        }

        private void advance() {
            if (phase == Phase.SPIN_UP) {
                phase = Phase.RELEASE;
            } else if (phase == Phase.RELEASE) {
                phase = Phase.CLOSE;
            } else {
                phase = Phase.IDLE;
            }
            phaseSeconds = 0;
        }

        private double duration() {
            if (phase == Phase.SPIN_UP) {
                return spinUpSeconds;
            }
            if (phase == Phase.RELEASE) {
                return releaseSeconds;
            }
            if (phase == Phase.CLOSE) {
                return closeSeconds;
            }
            return Double.POSITIVE_INFINITY;
        }

        private void applyOutputs() {
            motor.setPower(phase == Phase.IDLE ? 0 : power);
            if (phase == Phase.RELEASE || (phase == Phase.IDLE && previewOpen)) {
                gate.setPosition(open);
            } else {
                gate.setPosition(block);
            }
        }

        private void write(SharedPreferences.Editor editor) {
            editor.putFloat(powerKey, (float) power);
            editor.putFloat(blockKey, (float) block);
            editor.putFloat(openKey, (float) open);
            editor.putFloat(spinUpKey, (float) spinUpSeconds);
            editor.putFloat(releaseKey, (float) releaseSeconds);
            editor.putFloat(closeKey, (float) closeSeconds);
        }

        private double value(Part part) {
            if (part == Part.SPEED) {
                return power;
            }
            if (part == Part.BLOCK) {
                return block;
            }
            if (part == Part.OPEN) {
                return open;
            }
            if (part == Part.SPIN_UP) {
                return spinUpSeconds;
            }
            if (part == Part.RELEASE) {
                return releaseSeconds;
            }
            return closeSeconds;
        }

        private boolean isBusy() {
            return phase != Phase.IDLE;
        }

        private String status() {
            if (phase == Phase.SPIN_UP) {
                return "spin-up";
            }
            if (phase == Phase.RELEASE) {
                return "release";
            }
            if (phase == Phase.CLOSE) {
                return "close";
            }
            return "idle";
        }
    }

    private enum Phase {
        IDLE,
        SPIN_UP,
        RELEASE,
        CLOSE
    }

    private static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }
}
