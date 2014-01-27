package excelUtils;

import java.io.File;
import java.io.IOException;
/*
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
*/
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JFileChooser;

//import org.apache.tika.Tika;

/* * * * * * * * * * * * * * * * * * * * * * * * * * * *
 * To do:
 * 		[ ] Implement file detection
 * 
 * 
 * * * * * * * * * * * * * * * * * * * * * * * * * * * */


public class FileUtils {
	public static String getPWD() {
		return System.getProperty("user.dir") + File.separator;
	}
	/* Excel extension tools */
	public static String getExt(File file) {
		return getExt(file.getName());
	}
	public static String getExt(String filename) {
		int i;
		String buf = "";
		boolean noExtFound = true;
		for (i = 0; i < filename.length(); i++) {
			if (filename.charAt(i) == '.') {
				buf = "";
				noExtFound = false;
			}
			buf += filename.charAt(i);
		}
		if (noExtFound) {
			return null;
		}
		return buf;
	}
	public static String parseExt(File file) {
		return parseExt(file.getName());
	}

	public static String parseExt(String filename) {
		int i;
		String buf = "", parsedFilename = "";
		boolean noExtFound = true;
		for (i = 0; i < filename.length(); i++) {
			if (filename.charAt(i) == '.' && i > 0) {
				parsedFilename += buf;
				buf ="";
				noExtFound = false;
			}
			buf += filename.charAt(i);
		}
		if (noExtFound) {
			parsedFilename += buf;
		}
		return parsedFilename;
	}
	public static boolean hasProperExt(String filename) {
		String ext = getExt(filename);
		if (ext == null) {
			return false;
		}
		if (ext.equals(".xls") || ext.equals(".xlsx") ) {
			return true;
		}
		return false;
	}
	/* File Location Methods */
	public static File locateAndOpenFile(String filename) throws Exception {
		return locateAndOpenFile(filename,getPWD(),0,false,true,false,null);
	}
	public static File locateAndOpenFile(String filename,String path,String msg) throws Exception {
		return locateAndOpenFile(filename,path,0,false,true,false,msg);
	}
	public static File locateAndOpenFile(String filename,String path,boolean matchPartial,String msg) throws Exception {
		return locateAndOpenFile(filename,path,0,matchPartial,true,false,msg);
	}
	public static File locateAndOpenFile(String filename,String path,boolean matchPartial,boolean recursive,String msg) throws Exception {
		return locateAndOpenFile(filename,path,0,matchPartial,recursive,false,msg);
	}
	public static File locateAndOpenFile(String filename,String path,boolean matchPartial,boolean recursive,boolean makedirs,String msg) throws Exception {
		return locateAndOpenFile(filename,path,0,matchPartial,recursive,makedirs,msg);
	}
	public static File locateAndOpenFile(String filename, String path, int level,boolean matchPartial,boolean recursive,boolean makedirs, String msg) throws Exception {
		File locatedFile = null;
		String curFileName;
		File dir = new File(path);
		if (!dir.exists()) {return null;}
		ArrayList<File> subDirs = new ArrayList<File>(); 
		dir.mkdirs();
		if (level == 0) {
			filename = parseExt(filename);
		}
		for (File curFile: dir.listFiles()) {
			if (curFile.exists()) {
				if (recursive && curFile.isDirectory()) {
					subDirs.add(curFile);
				}
				if (curFile.isFile()) {
					curFileName = parseExt(curFile.getName());
					if (matchPartial) {
						if (curFileName.toLowerCase().contains(filename.toLowerCase()) || 
								filename.toLowerCase().contains(curFileName.toLowerCase())) {
							locatedFile = curFile;
							break;
						}
					} else {
						if (curFileName.equals(filename)) {
							locatedFile = curFile;
							break;
						}
					}
				}
			}
		}
		if (locatedFile == null && level == 0) {
			/* Manual Location required */
			locatedFile = manualLocate("Locate " + filename + "...");
		} else if (locatedFile == null) {
			for (File curFile: subDirs) { 
				locatedFile = locateAndOpenFile(filename,curFile.getAbsolutePath(),level+1,matchPartial,recursive,makedirs,msg);
				if (locatedFile != null) {
					break;
				}
			}
		}
		return locatedFile;
	}
	/* File location by REGEX */
	public static File locateAndOpenFileRegex(String regex) {
		return locateAndOpenFileRegex(regex,getPWD(),true,"Locate file matching pattern: " + regex);
	}
	public static File locateAndOpenFileRegex(String regex,boolean recursive) {
		return locateAndOpenFileRegex(regex,getPWD(),recursive,"Locate file matching pattern: " + regex);
	}
	public static File locateAndOpenFileRegex(String regex,boolean recursive, String msg) {
		return locateAndOpenFileRegex(regex,getPWD(),recursive,msg);
	}
	public static File locateAndOpenFileRegex(String regex, String startingDirectory) {
		return locateAndOpenFileRegex(regex,startingDirectory,true,"Locate file matching pattern: " + regex);
	}
	public static File locateAndOpenFileRegex(String regex, String startingDirectory, String msg) {
		return locateAndOpenFileRegex(regex,startingDirectory,true,msg);
	}
	public static File locateAndOpenFileRegex(String regex, String startingDirectory, boolean recursive) {
		return locateAndOpenFileRegex(regex,startingDirectory,recursive,"Locate file matching pattern: " + regex);
	}
	public static File locateAndOpenFileRegex(String regex, String startingDirectory, boolean recursive, String msg) {
		Pattern pattern = Pattern.compile(regex);
		return locateAndOpenFileRegex(pattern,startingDirectory,recursive,msg,0);
	}
	public static File locateAndOpenFileRegex(Pattern pattern, String startingDirectory, boolean recursive, String msg,int level) {
		File locatedFile = null;
		String curFileName;
		File dir = new File(startingDirectory);
		if (!dir.exists()) {return null;}
		ArrayList<File> subDirs = new ArrayList<File>(); 
		dir.mkdirs();
		Matcher m;
		for (File curFile: dir.listFiles()) {
			if (curFile.exists()) {
				if (curFile.isFile()) {
					curFileName = curFile.getName();
					m = pattern.matcher(curFileName);
					if (m.find()) {
						locatedFile = curFile;
						break;
					}
				}
				if (recursive && curFile.isDirectory()) {
					subDirs.add(curFile);
				}
			}
		}
		if (locatedFile == null && level == 0) {
			/* Manual Location required */
			locatedFile = manualLocate(msg);
		} else if (locatedFile == null) {
			for (File curFile: subDirs) {
				locatedFile = locateAndOpenFileRegex(pattern,curFile.getAbsolutePath(),recursive,msg,level+1);
				if (locatedFile != null) {
					break;
				}
			}
		}
		return locatedFile;
	}
	// Manual Location
	public static File manualLocate(String msg) {
		return manualLocate(msg, false);
	}
	public static File manualLocate(String msg, boolean isDir) {
		return manualLocate(System.getProperty("user.dir"),msg,isDir);
	}
	public static File manualLocate(String dir, String msg, boolean isDir) {
		JFileChooser locator = new JFileChooser(dir);
		if (isDir) {
			locator.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		}
		locator.setDialogTitle(msg);
		int returnVal = locator.showOpenDialog(null);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			return locator.getSelectedFile();
		} else {
			return null;
		}
	}
	/* Locate folder */
	public static File locateFolder(String folderName) {
		return locateFolder(folderName,getPWD(),true,false,true);
	}
	public static File locateFolder(String folderName, String startingDir) {
		return locateFolder(folderName,startingDir,true,false,true);
	}
	public static File locateFolder(String folderName, File startingDir) {
		return locateFolder(folderName,startingDir.getName(),true,false,true);
	}
	public static File locateFolder(String folderName, String startingDir, boolean recursive, boolean matchPartial, boolean ignoreCase) {
		File itr = new File(startingDir);
		if (!itr.exists()) {
			return null;
		}
		return locateFolder(folderName,itr,recursive,matchPartial,ignoreCase);
	}
	public static File locateFolder(String folderName, File startingDir, boolean recursive, boolean matchPartial, boolean ignoreCase) {
		ArrayList<File> dirsToSearch = new ArrayList<File>();
		for (File curDir : startingDir.listFiles()) {
			if (curDir.isDirectory()) {
				if (ignoreCase ?
						matchPartial ? curDir.getName().toLowerCase().contains(folderName.toLowerCase()) : curDir.getName().equalsIgnoreCase(folderName)
								:
									matchPartial ? curDir.getName().contains(folderName) : curDir.getName().equals(folderName)) {
					return curDir;
				}
				if (recursive) {
					dirsToSearch.add(curDir);
				}
			}
		}
		File folder;
		for (File itr : dirsToSearch) { 
			if ((folder = locateFolder(folderName,itr,recursive,matchPartial,ignoreCase)) != null) {
				return folder;
			}
		}
		return null;
	}
	
	/* File Type detection */
	public static String detectFileType(File file) throws IOException {
		return detectFileType(file.getAbsolutePath());
	}
	public static String detectFileType(String path) throws IOException {
		/*
		Path p = FileSystems.getDefault().getPath(path);
		String mimeType = Files.probeContentType(p);
		*/
		if (path.endsWith(".xls") || path.endsWith(".xlsx")){
			return "application/vnd.ms-excel";
		} else if (path.endsWith(".csv")){
			return "text/csv";
		}  else if (path.endsWith(".txt")){
			return "text/plain";
		}
		return "unknown";
		
	}
	
	
	/* Ensured File deletion */
	public static void deleteFile(File file) throws Exception {
		if (file != null && file.exists()) {
			if (!file.delete()) {
				org.apache.commons.io.FileUtils.deleteQuietly(file);
			}
		}
		return;
	}

	/* File path manipulation */
	public static String joinPath(String path1, String path2) {
		return path1 + (path1.endsWith(File.separator) ? "" : File.separator) + (path2.startsWith(File.separator) ? path2.substring(1) : path2);
	}
	public static String shortenPath(String path) {
		return shortenPath(path,1);
	}
	public static String shortenPath(String path, int distance) {
		int i = 0, L = path.length(), index = L, count = 0;
		for (i = L-1; i >= 0; i--) {
			if (path.charAt(i) == File.separatorChar) {
				count++;
				index = i+1;
			}
			if (count >= distance) {
				break;
			}
		}
		return path.substring(index);
	}

	/* Main Method for tests */
	public static void main(String args[]) {
		File pwd = new File(getPWD());
		for (File file : pwd.listFiles()) {
			try {
				System.out.println(file.getName() + "\n\t" + detectFileType(file));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
