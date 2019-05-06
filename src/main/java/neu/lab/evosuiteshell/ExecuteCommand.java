package neu.lab.evosuiteshell;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.ExecuteException;
import org.apache.commons.exec.ExecuteWatchdog;
import org.apache.commons.exec.PumpStreamHandler;

import neu.lab.conflict.vo.DependencyInfo;

public class ExecuteCommand {
	public static void exeCmd(String mvnCmd) throws ExecuteException, IOException {
		exeCmd(mvnCmd, 0, null);
	}

	public static void exeCmd(String mvnCmd, long timeout, String logPath) throws ExecuteException, IOException {
		CommandLine cmdLine = CommandLine.parse(mvnCmd);
		DefaultExecutor executor = new DefaultExecutor();
		if (timeout != 0) {
			ExecuteWatchdog watchdog = new ExecuteWatchdog(timeout);
			executor.setWatchdog(watchdog);
		}
		if (logPath != null) {
			executor.setStreamHandler(new PumpStreamHandler(new FileOutputStream(logPath)));
		}
		executor.execute(cmdLine);
	}

	public static void main(String[] args) throws ExecuteException, IOException {
		String sensor_dir = "C:\\Users\\Flipped\\eclipse-workspace\\Host\\" + Config.SENSOR_DIR + "\\";
		String targetFile = ReadXML.copyPom(sensor_dir);
		List<DependencyInfo> DependencyInfos = new ArrayList<DependencyInfo>();
		DependencyInfo dependencyInfo = new DependencyInfo();
		dependencyInfo.setArtifactId("B");
		dependencyInfo.setGroupId("neu.lab");
		dependencyInfo.setVersion("1.0");
		DependencyInfos.add(dependencyInfo);
		ReadXML.setCopyDependency(DependencyInfos, targetFile);
		String mvnCmd = Config.getMaven() + Command.MVN_POM + targetFile + Command.MVN_COPY + sensor_dir + "jar\\";
		exeCmd(mvnCmd);
	}
}
