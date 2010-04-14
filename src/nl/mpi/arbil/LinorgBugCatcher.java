package nl.mpi.arbil;

import java.awt.Robot;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileWriter;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.imageio.ImageIO;
//import org.apache.log4j.Logger;

/**
 * Document   : LinorgBugCatcher
 * Created on : Dec 17, 2008, 10:35:56 AM
 * @author Peter.Withers@mpi.nl
 */
public class LinorgBugCatcher {

//    static Logger log = Logger.getLogger(ImdiIcons.class.getName());
//            log.debug("debug message.");
//            log.info("info message.");
//            log.warn("warn message.");
//            log.error("error message.");
//            log.fatal("fatal message.");

    public LinorgBugCatcher() {
        // remove all previous error logs for this version other than the one for this build number
        File errorLogFile = new File(LinorgSessionStorage.getSingleInstance().storageDirectory, "linorgerror.log");
        if (errorLogFile.exists()) {
            errorLogFile.delete();
        }
        // look for previous error logs for this version only
        LinorgVersion linorgVersion = new LinorgVersion();
        String currentApplicationVersionMatch = "error-" + linorgVersion.currentMajor + "-" + linorgVersion.currentMinor + "-";
        String currentLogFileMatch = "error-" + linorgVersion.currentMajor + "-" + linorgVersion.currentMinor + "-" + linorgVersion.currentRevision + ".log";
        for (String currentFile : LinorgSessionStorage.getSingleInstance().storageDirectory.list()) {
            if (currentFile.startsWith(currentApplicationVersionMatch)) {
                if (currentFile.startsWith(currentLogFileMatch)) {
                    // keeping this builds log file
                    System.out.println("currentLogFileMatch: " + currentFile);
                } else {
                    System.out.println("deleting old log file: " + currentFile);
                    new File(LinorgSessionStorage.getSingleInstance().storageDirectory, currentFile).delete();
                }
            }
        }
    }

    private int captureCount = 0;

    public File getLogFile() {
        LinorgVersion linorgVersion = new LinorgVersion();
        return new File(LinorgSessionStorage.getSingleInstance().storageDirectory, "error-" + linorgVersion.currentMajor + "-" + linorgVersion.currentMinor + "-" + linorgVersion.currentRevision + ".log");
    }

    public void grabApplicationShot() {
        try {
            Robot robot = new Robot();
            //BufferedImage screenShot = robot.createScreenCapture(new Rectangle(Toolkit.getDefaultToolkit().getScreenSize()));
            BufferedImage screenShot = robot.createScreenCapture(LinorgWindowManager.getSingleInstance().linorgFrame.getBounds());
            DecimalFormat myFormat = new DecimalFormat("000");
            SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
            String formattedDate = formatter.format(new Date());
            String formattedCount = myFormat.format(new Integer(captureCount));
            ImageIO.write(screenShot, "JPG", new File(LinorgSessionStorage.getSingleInstance().storageDirectory, "screenshots" + File.separatorChar + formattedDate + "-" + formattedCount + ".jpg"));
            captureCount++;
        } catch (Exception e) {
        }
    }

//    public void logMessage(String messageString) {
//        try {
//            FileWriter errorLogFile = new FileWriter(GuiHelper.linorgSessionStorage.storageDirectory, "linorgerror.log", true);
//            System.out.println("logCatch: " + messageString);
//            errorLogFile.append(messageString + System.getProperty("line.separator"));
//            errorLogFile.append("Message Date: " + new Date().toString() + System.getProperty("line.separator"));
//            errorLogFile.append("Compile Date: " + new LinorgVersion().compileDate + System.getProperty("line.separator"));
//            errorLogFile.append("Current Revision: " + new LinorgVersion().currentRevision + System.getProperty("line.separator"));
//            errorLogFile.append("======================================================================" + System.getProperty("line.separator"));
//            errorLogFile.close();
//        } catch (Exception ex) {
//            System.err.println("failed to write to the error log: " + ex.getMessage());
//        }
//    }
    
    public void logError(Exception exception) {
        logError("", exception);
    }

    public void logError(String messageString, Exception exception) {
        try {
            LinorgVersion linorgVersion = new LinorgVersion();
            System.err.println("exception: " + exception.getMessage());
            System.err.println(messageString);
            exception.printStackTrace();
            FileWriter errorLogFile = new FileWriter(getLogFile(), true);
//            System.out.println("logCatch: " + messageString);
            errorLogFile.append(messageString + System.getProperty("line.separator"));
            errorLogFile.append("Error Date: " + new Date().toString() + System.getProperty("line.separator"));
            errorLogFile.append("Compile Date: " + linorgVersion.compileDate + System.getProperty("line.separator"));
            errorLogFile.append("Current Revision: " + linorgVersion.currentMajor + "-" + linorgVersion.currentMinor + "-" + linorgVersion.currentRevision + System.getProperty("line.separator"));
            errorLogFile.append("Exception Message: " + exception.getMessage() + System.getProperty("line.separator"));
            StackTraceElement[] stackTraceElements = exception.getStackTrace();
            for (StackTraceElement element : stackTraceElements) {
                errorLogFile.append(element.toString() + System.getProperty("line.separator"));
            }
            errorLogFile.append("======================================================================" + System.getProperty("line.separator"));
            errorLogFile.close();
        } catch (Exception ex) {
            System.err.println("failed to write to the error log: " + ex.getMessage());
        }
    }
}
