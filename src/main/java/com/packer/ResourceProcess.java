package com.packer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;


import org.apache.commons.lang3.StringUtils;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

@Mojo(name="cleanJsAndCssCash",defaultPhase=LifecyclePhase.PACKAGE)
public class ResourceProcess extends AbstractMojo{
	
	@Parameter(property="newVersion")
	private String newVersion;
	
	@Parameter(property="suffixs")
	private List<String> suffixs;
	
	private static String[] tagNames = {"link|href","script|src"};
	
	
	public void execute() throws MojoExecutionException, MojoFailureException {
		File root = new File(System.getProperty("user.dir"));
		recursionFile(root);
	}
	
	private void recursionFile(File file) {
		if(file == null || suffixs == null) {
			return;
		}
		if(file.isFile()) {
			String fileName = file.getName();
			if(suffixs.contains(fileName.substring(fileName.lastIndexOf(".")+1))) {
				//读取文件并且修改js与css的版本号
				try {
					replaceVersion(file);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			return;
		}
		if(file.isDirectory()) {
			File[] listFiles = file.listFiles();
			if(listFiles != null) {
				for (File newFile : listFiles) {
					recursionFile(newFile);
				}
			}
		}
	}
	private void replaceVersion(File file) throws IOException {
		Document document = Jsoup.parse(file, "utf-8");
		for (String tagName : tagNames) {
			String[] tags = tagName.split("\\|");
			Elements elements = document.getElementsByTag(tags[0]);
			if(elements != null && !elements.isEmpty()) {
				for (Element element : elements) {

					String oldValue = element.attr(tags[1]);
					String subValue = oldValue;
					if(oldValue.contains("?")) {
						subValue = oldValue.substring(0,oldValue.lastIndexOf("?"));
					}
					if(StringUtils.isBlank(newVersion)) {
						SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
						newVersion = sdf.format(new Date());
					}
					element.attr(tags[1],subValue+"?v="+newVersion);
				}
			}
		}
		FileOutputStream fos = new FileOutputStream(file, false);
		OutputStreamWriter osw = new OutputStreamWriter(fos, "utf-8");
		osw.write(document.html());
		osw.close();
	}
	

}