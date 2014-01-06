package excelUtils;

import java.io.File;
import java.util.ArrayList;

public class FileUtils {
	public static String getPWD() {
		return System.getProperty("user.dir") + File.separator;
	}
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
			/*
				if (msg != null) {
					JOptionPane.showMessageDialog(frame, msg);
				} else {
					JOptionPane.showMessageDialog(frame, "Please locate " + filename);
				}
				JFileChooser manLocate = new JFileChooser(dir);
				manLocate.setDialogTitle("Locate " + filename + "...");
				int returnVal = manLocate.showOpenDialog(frame);
				if(returnVal == JFileChooser.APPROVE_OPTION) {
					locatedFile = manLocate.getSelectedFile();
				}
			 */
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
	public static void deleteFile(File file) throws Exception {
		if (file != null && file.exists()) {
			if (!file.delete()) {
				org.apache.commons.io.FileUtils.deleteQuietly(file);
			}
		}
		return;
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
	public static String joinPath(String path1, String path2) {
		return path1 + (path1.endsWith(File.separator) ? "" : File.separator) + (path2.startsWith(File.separator) ? path2.substring(1) : path2);
	}
	public static void main(String args[]) {
		String p1 = "/this/is/my/path", p2 = "this/is/my/second/path", f = "filename.file", s;
		System.out.println((s = joinPath(p1,p2)));
		System.out.println(joinPath(s,f));
	}
}
