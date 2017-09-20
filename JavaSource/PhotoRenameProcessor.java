// spufi.java
//
// This is a sample application that simulates a bare-bones spufi
// in java using jdbc.


import java.io.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Vector;

import com.drew.imaging.jpeg.JpegMetadataReader;
import com.drew.imaging.jpeg.JpegProcessingException;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.exif.ExifDirectory;

/**
 * @author richard
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class PhotoRenameProcessor {
	public static final String crlf = System.getProperties().getProperty("line.separator");
	public static final char   eofchar[] = {0x1A};
	public static final String eofstr = new String(eofchar);
	private static SimpleDateFormat dateFormatter  = new SimpleDateFormat ("yyyy-MM-dd-HH.mm.ss");
	private static String lastModel = "";
	private static String lastMake  = "";
	private String inDir;

	private String offsetTime;
	private String hours;
	private String minutes;
	private String matchMake;
	
public PhotoRenameProcessor(String dir, String offsetTime, String match) {
	super();
	inDir = dir;
	this.offsetTime = offsetTime;
	String[] times = offsetTime.split(":");
	hours = times[0];
	if (times.length > 1) {
		minutes = times[1];
	} else {
		minutes = "0";
	}
	matchMake = match;
}

/**
 * @param inDir
 * @param hours
 * @throws Exception
 */
public int ProcessDirectory()
	throws Exception {
	int offsetHours   = new Integer(hours).intValue();
	int offsetMinutes = new Integer(minutes).intValue();
	File fileDir = new File(inDir);
	File[] strFilesDirs = fileDir.listFiles ( );
	int numPhotos = 0;
	for ( int i = 0 ; i < strFilesDirs.length ; i ++ ) {
		if ( strFilesDirs[i].isDirectory ( ) ) {
			System.out.println ( "Directory: " + strFilesDirs[i] ) ;
			PhotoRenameProcessor dirProc = new PhotoRenameProcessor(strFilesDirs[i].getAbsolutePath(), offsetTime, matchMake);
			numPhotos  += dirProc.ProcessDirectory();
		}
		else if ( strFilesDirs[i].isFile ( ) ) {
			String fileName = strFilesDirs[i].getName();
			if (fileName.toUpperCase().endsWith(".JPG")) {
				//System.out.println ( "File: " + strFilesDirs[i] + " (" + strFilesDirs[i].length ( ) + ")" ) ;
				File jpegFile = strFilesDirs[i];
				try {
					numPhotos += renameFile(offsetHours, offsetMinutes, jpegFile, matchMake);
				} catch (Exception e) {
					System.out.println("Failed to rename Photo: "+jpegFile.getName()+ " Error: "+e.getMessage());
				}
			}
			else {
				// not a jpeg file - rename using file date/time
				File normalFile = strFilesDirs[i];
				try {
					numPhotos += renameNormalFile(offsetHours, offsetMinutes, normalFile);
				} catch (Exception e) {
					System.out.println("Failed to rename Normal File: "+normalFile.getName()+ " Error: "+e.getMessage());
				}
			}
		} 
	}
	return numPhotos;
}

/**
 * @param offsetHours
 * @param jpegFile
 * @throws JpegProcessingException
 */
private int renameFile(int offsetHours, int offsetMinutes,File jpegFile, String matchMake)
	 throws Exception {
	String fileName     = jpegFile.getName();
	String basefilename = fileName.substring(0,fileName.lastIndexOf("."));
	
	Metadata metadata = JpegMetadataReader.readMetadata(jpegFile);
	Directory exifDirectory = metadata.getDirectory(ExifDirectory.class);
	String cameraMake = exifDirectory.getString(ExifDirectory.TAG_MAKE);
	String cameraModel = exifDirectory.getString(ExifDirectory.TAG_MODEL);
	String jpegDateOrig = exifDirectory.getString(ExifDirectory.TAG_DATETIME_ORIGINAL);
	String jpegDate = exifDirectory.getString(ExifDirectory.TAG_DATETIME_ORIGINAL);
	
	String makes[] = null;
	if (cameraMake != null)
		makes = cameraMake.split(" ");
	String models[] = null;
	if (cameraModel != null)
		models = cameraModel.split(" ");
	String make = lastMake;
	if (makes != null) 
		make = makes[0];
	
	// only rename files from cameras which match the matchMake input
	if (matchMake.trim().equals("") || matchMake.trim().length() <= 1) ; // make doesn't matter, do them all
	else {
		// if make doesn't match, exit
		if (!make.toUpperCase().trim().equals(matchMake.toUpperCase().trim()))
			return 0;
	}
		 
	String model = lastModel;
	if (models != null)
		model = models[models.length-1];
	
	lastModel = model;
	lastMake  = make;
	
	//  example: 
	// cameraMake= "CASIO COMPUTER CO.,LTD "
	// cameraModel= "EX-Z750"
	// jpegDateOrig= "2007:08:08 14:07:31"
	// jpegDate= "2007:08:08 14:07:31"
	
	
	String parentPath   = jpegFile.getParent();
	String photoDate = jpegDate.substring(0,10);
	String photoTime = jpegDate.substring(11,19);
	
	int year = new Integer(photoDate.substring(0,4)).intValue();
	int month = new Integer(photoDate.substring(5,7)).intValue();
	int day = new Integer(photoDate.substring(8,10)).intValue();
	
	int hour = new Integer(photoTime.substring(0,2)).intValue();
	int minute = new Integer(photoTime.substring(3,5)).intValue();
	
	int second = new Integer(photoTime.substring(6,8)).intValue();
	GregorianCalendar cal = new GregorianCalendar(year,month-1,day,hour,minute,second);
	cal.add(Calendar.HOUR, offsetHours);
	cal.add(Calendar.MINUTE,offsetMinutes);
	String newDate = dateFormatter.format(cal.getTime());
	//String newDate = DateFormat.getDateTimeInstance().format(cal.getTime()).replace(',',' ').replace(':','_').replace(' ','_');
	String appendText = PhotoRenamer.findTag(cal.getTime());
	if (!appendText.trim().equals("")) {
		String[] breakdownAppendText = appendText.split("-");
		String newFolderName = newDate.substring(0,10)+"-"+appendText.split("-")[1];
		// change output directory if requested
		parentPath = PhotoRenamer.getOutputDirectory(parentPath, newFolderName);
	}
	String newFileName = parentPath+"\\"+newDate+appendText+"-"+make+"-"+model+"_"+basefilename+".JPG";
	PhotoStampProcessor.createDir(newFileName);
	File newFile = new File(newFileName);
	jpegFile.renameTo(newFile);
	
	System.out.println(fileName + " renamed to ===> "+newFileName);
	return +1;
	
}
/**
 * @param offsetHours
 * @param jpegFile
 * @throws JpegProcessingException
 */
private int renameNormalFile(int offsetHours, int offsetMinutes,File normalFile)
	 throws Exception {
	String fileName     = normalFile.getName();

	String parentPath   = normalFile.getParent();
	String lastDir = parentPath.substring(parentPath.lastIndexOf("\\")+1);
	String filextension = fileName.substring(fileName.lastIndexOf(".") + 1);
	String basefilename = fileName.substring(0,fileName.lastIndexOf("."));
	
	Date lastmodified =  new Date(normalFile.lastModified());
	GregorianCalendar cal = new GregorianCalendar();
	cal.setTime(lastmodified);
	cal.add(Calendar.HOUR, offsetHours);
	cal.add(Calendar.MINUTE,offsetMinutes);
	String newDate = dateFormatter.format(cal.getTime());
	//String newDate = DateFormat.getDateTimeInstance().format(cal.getTime()).replace(',',' ').replace(':','_').replace(' ','_');
	String appendText = PhotoRenamer.findTag(cal.getTime());
	if (!appendText.trim().equals("")) {
		String[] breakdownAppendText = appendText.split("-");
		String newFolderName = newDate.substring(0,10)+"-"+appendText.split("-")[1];
		// change output directory if requested
		parentPath = PhotoRenamer.getOutputDirectory(parentPath, newFolderName);
	}
	String newFileName = parentPath+"\\"+newDate+appendText+"_"+lastDir+"_"+basefilename+"."+filextension;
	PhotoStampProcessor.createDir(newFileName);
	File newFile = new File(newFileName);
	normalFile.renameTo(newFile);
	
	System.out.println(fileName + " renamed to ===> "+newFileName);
	return +1;
	
}
}