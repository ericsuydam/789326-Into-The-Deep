package org.firstinspires.ftc.team6220_2020;

import com.qualcomm.robotcore.hardware.DcMotor;

import org.firstinspires.ftc.team6220_2020.ResourceClasses.Button;
import org.firstinspires.ftc.team6220_2020.ResourceClasses.PIDFilter;

//todo add is op mode active breakers
public abstract class MasterAutonomous extends MasterOpMode
{
    // Initialize booleans used in runSetup()--------------------------------------------------
    // Determines whether or not we park on the line at the end of autonomous.
    boolean parkOnLine = true;
    // Determines what team we are on.
    boolean isRedAlliance = true;
    // Determines whether or not we move the wobble goal
    boolean moveWobbleGoal = false;
    //-----------------------------------------------------------------------------------------

    // Variables used during setup and running ----------------------------------------------
    // The number of the rings at start up
    int numRings = 0;

    //Start Position Variables. The various start positions are stored in the array start positions are chosen in runSetup.
    int matchStartPosition = 0;
    int[][] startPositions = {/*Position 1: X,Y */{4,6},/*Position 2: X,Y */{4,6}};
    int numStartPositions = startPositions.length - 1;

    //Position values to use in navigation
    double xPos = 0;
    double yPos = 0;

    //PID filters for navigation
    //PIDFilter translationPID;
    //-----------------------------------------------------------------------------------------

    // Allows the 1st driver to decide which autonomous routine should be run using gamepad input
    void runSetup()
    {
        // Creates the telemetry log
        telemetry.log().add("Red / Blue = B / X");
        telemetry.log().add("Increase / Decrease Delay = DPad Up / Down");
        telemetry.log().add("Score / Not Score Wobble Goal = Toggle Y");
        telemetry.log().add("Toggle Start Position = Toggle X");
        telemetry.log().add("Press Start to exit setup.");

        boolean settingUp = true;

        while(settingUp && opModeIsActive())
        {
            driver1.update();
            driver2.update();
            // Select alliance
            if (driver1.isButtonPressed(Button.B))
                isRedAlliance = true;
            else if (driver1.isButtonPressed(Button.X))
                isRedAlliance = false;

            //Toggles through moving wobble goal
            if(driver1.isButtonJustReleased(Button.Y))
                moveWobbleGoal = !moveWobbleGoal;

            //Toggles through start positions
            if(driver1.isButtonJustPressed(Button.X)){
                matchStartPosition++;
                if(matchStartPosition > numStartPositions){
                    matchStartPosition = 0;
                }
            }

            // If the driver presses start, we exit setup.
            if (driver1.isButtonJustPressed(Button.A) || opModeIsActive())
                settingUp = false;

            // Display the current setup
            telemetry.addData("a just pressed", driver1.getLeftStickX());
            telemetry.addData("a presses", driver1.isButtonPressed(Button.A));
            telemetry.addData("Is on red alliance: ", isRedAlliance);
            telemetry.addData("Is scoring wobble goal: ", moveWobbleGoal);
            telemetry.addData("Start position: ", matchStartPosition);
            telemetry.update();
            idle();

        }

        telemetry.clearAll();
        telemetry.log().clear();
        telemetry.addData("State: ", "waitForStart()");
        telemetry.update();
        //Sets the match start position
        xPos = startPositions[matchStartPosition][0];
        yPos = startPositions[matchStartPosition][1];
    }

    public void driveInches(double targetDistance, double degDriveAngle) {

        motorFrontLeft.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        motorFrontRight.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        motorBackLeft.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        motorBackRight.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);

        motorFrontLeft.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        motorFrontRight.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        motorBackLeft.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        motorBackRight.setMode(DcMotor.RunMode.RUN_USING_ENCODER);

        boolean targetReached = false;

        double xPosition = 0;
        double yPosition = 0;

        double radDriveAngle = Math.toRadians(degDriveAngle);
        double distanceLeft;

        PIDFilter translationPID;
        translationPID = new PIDFilter(Constants.TRANSLATION_P, Constants.TRANSLATION_I, Constants.TRANSLATION_D);

        while (!targetReached && opModeIsActive()) {
            // This calculates the distance traveled in inches
            double distanceTraveled = Math.sqrt(Math.pow((xPosition - 0), 2) + Math.pow((yPosition - 0), 2));

            // This adds a value to the PID loop so it can update
            distanceLeft = targetDistance - distanceTraveled;
            translationPID.roll(distanceLeft);

            // We drive the mecanum wheels with the PID value
            driveMecanum(radDriveAngle, Math.max(translationPID.getFilteredValue(), Constants.MINIMUM_DRIVE_POWER), 0.0);

            // Update positions using last distance measured by encoders
            xPosition = (Constants.IN_PER_ANDYMARK_TICK * (-motorFrontLeft.getCurrentPosition() +
                    motorBackLeft.getCurrentPosition() - motorFrontRight.getCurrentPosition() + motorBackRight.getCurrentPosition()) / 4);

            yPosition = (Constants.IN_PER_ANDYMARK_TICK * (-motorFrontLeft.getCurrentPosition() -
                    motorBackLeft.getCurrentPosition() + motorFrontRight.getCurrentPosition() + motorBackRight.getCurrentPosition()) / 4);

            telemetry.addData("Distance Traveled: ", distanceTraveled);
            telemetry.update();

            if (distanceTraveled > targetDistance) {
                driveMecanum(radDriveAngle, 0.0, 0.0);
                targetReached = true;
            }
        }
    }
}