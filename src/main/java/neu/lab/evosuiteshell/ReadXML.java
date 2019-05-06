package neu.lab.evosuiteshell;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import org.dom4j.*;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;

import com.google.common.io.Files;

import neu.lab.conflict.util.MavenUtil;
import neu.lab.conflict.vo.DependencyInfo;

public class ReadXML {
	private static String XMLFilePath = System.getProperty("user.dir")
			+ "/src/main/resources/copyConflictDependency.xml";

	/**
	 * add dependency to new empty
	 * 
	 * @param DependencyInfos
	 */
	public static void setCopyDependency(List<DependencyInfo> DependencyInfos, String xmlFilePath) {
		SAXReader reader = new SAXReader();
		try {
			Document document = reader.read(xmlFilePath);
			Element rootElement = document.getRootElement();
			Element dependencies = rootElement.element("dependencies");
			Element dependency = dependencies.addElement("dependency");
			for (DependencyInfo dependencyInfo : DependencyInfos) {
				dependencyInfo.addDependencyElement(dependency);
			}
			OutputFormat outputFormat = OutputFormat.createPrettyPrint();
			outputFormat.setEncoding("UTF-8");
			XMLWriter writer = new XMLWriter(new FileWriter(xmlFilePath), outputFormat);
			writer.write(document);
			writer.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * copy empty dependency xml to target path
	 * 
	 * @param targetPath
	 * @return copyConflictDependency.xml path
	 */
	public static String copyPom(String targetPath) {
//		MavenUtil.i().getLog().info("copy dependency.xml to " + targetPath);
		File dir = new File(targetPath);
		if (!dir.exists())
			dir.mkdirs();
		String target = targetPath + "copyConflictDependency.xml";
		try {
			Files.copy(new File(XMLFilePath), new File(target));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return target;
	}

//test
	public static void main(String[] args) {
		System.out.println(copyPom("C:\\Users\\Flipped\\eclipse-workspace\\Host\\"));
		/*
		 * DependencyInfo d = new DependencyInfo(); d.setArtifactId("a");
		 * d.setGroupId("ffff"); d.setVersion("123"); String filePath =
		 * System.getProperty("user.dir") +
		 * "/src/main/resources/copyConflictDependency.xml"; //
		 * ExecuteCommand().getClass().getClassLoader().getResource(
		 * "copyConflictDependency.xml").getPath(); SAXReader reader = new SAXReader();
		 * try { Document document = reader.read(filePath); Element rootElement =
		 * document.getRootElement(); Element dependencies =
		 * rootElement.element("dependencies"); Element dependency =
		 * dependencies.addElement("dependency"); d.addDependencyElement(dependency);
		 * OutputFormat outputFormat = OutputFormat.createPrettyPrint();
		 * outputFormat.setEncoding("UTF-8"); XMLWriter writer = new XMLWriter(new
		 * FileWriter(filePath), outputFormat); writer.write(document); writer.close();
		 * } catch (Exception e) { // TODO Auto-generated catch block
		 * e.printStackTrace(); }
		 */
	}
}
