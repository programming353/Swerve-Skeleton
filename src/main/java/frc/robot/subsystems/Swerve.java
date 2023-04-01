// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot.subsystems;

import com.kauailabs.navx.frc.AHRS;

import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.kinematics.ChassisSpeeds;
import edu.wpi.first.math.kinematics.SwerveDriveKinematics;
import edu.wpi.first.math.kinematics.SwerveDriveOdometry;
import edu.wpi.first.math.kinematics.SwerveModulePosition;
import edu.wpi.first.math.kinematics.SwerveModuleState;
import edu.wpi.first.wpilibj.SPI;
import edu.wpi.first.wpilibj.smartdashboard.Field2d;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj2.command.SubsystemBase;
import frc.robot.Constants.SwerveConstants;
import frc.robot.Constants.SwerveConstants.BackLeftModule;
import frc.robot.Constants.SwerveConstants.BackRightModule;
import frc.robot.Constants.SwerveConstants.FrontLeftModule;
import frc.robot.Constants.SwerveConstants.FrontRightModule;

public class Swerve extends SubsystemBase {
  private SwerveDriveKinematics swerveKinematics = new SwerveDriveKinematics(SwerveConstants.wheelLocations);

  private SwerveModule frontLeftModule = new SwerveModule(FrontLeftModule.driveID, FrontLeftModule.turnID);
  private SwerveModule frontRightModule = new SwerveModule(FrontRightModule.driveID, FrontRightModule.turnID);
  private SwerveModule backLeftModule = new SwerveModule(BackLeftModule.driveID, BackLeftModule.turnID);
  private SwerveModule backRightModule = new SwerveModule(BackRightModule.driveID, BackRightModule.turnID);

  private SwerveModuleState[] targetStates = { new SwerveModuleState(), new SwerveModuleState(),
      new SwerveModuleState(), new SwerveModuleState() };

  private AHRS navx = new AHRS(SPI.Port.kMXP);

  private SwerveDriveOdometry swerveOdometry;

  private Field2d field = new Field2d();

  /** Creates a new Swerve. */
  public Swerve() {
    swerveOdometry = new SwerveDriveOdometry(swerveKinematics, navx.getRotation2d(), getModulePositions());

    SmartDashboard.putData("NavX Sensor", navx);
    SmartDashboard.putData("Odometry", field);
  }

  public SwerveModulePosition[] getModulePositions() {
    return new SwerveModulePosition[] { frontLeftModule.getModulePosition(), frontRightModule.getModulePosition(),
        backLeftModule.getModulePosition(), backRightModule.getModulePosition() };
  }

  public SwerveModuleState[] getModuleStates() {
    return new SwerveModuleState[] { frontLeftModule.getModuleState(), frontRightModule.getModuleState(),
        backLeftModule.getModuleState(), backRightModule.getModuleState() };
  }

  public void driveFieldOriented(double x, double y, double turn) {
    ChassisSpeeds chassisSpeeds = ChassisSpeeds.fromFieldRelativeSpeeds(y, x, turn, navx.getRotation2d());
    setChassisSpeeds(chassisSpeeds);
  }

  public void setChassisSpeeds(ChassisSpeeds speeds) {
    setModuleStates(swerveKinematics.toSwerveModuleStates(speeds));
  }

  public void setModuleStates(SwerveModuleState[] states) {
    SwerveDriveKinematics.desaturateWheelSpeeds(states, SwerveConstants.maxModuleSpeed);

    targetStates = states;
  }

  public SwerveDriveKinematics getKinematics() {
    return swerveKinematics;
  }

  public Pose2d getPose() {
    return swerveOdometry.getPoseMeters();
  }

  public void resetOdometry(Pose2d pose) {
    swerveOdometry.resetPosition(navx.getRotation2d(), getModulePositions(), pose);
  }

  @Override
  public void periodic() {
    // This method will be called once per scheduler run
    frontLeftModule.setState(targetStates[0]);
    frontRightModule.setState(targetStates[1]);
    backLeftModule.setState(targetStates[2]);
    backRightModule.setState(targetStates[3]);

    field.setRobotPose(swerveOdometry.update(navx.getRotation2d(), getModulePositions()));
  }
}