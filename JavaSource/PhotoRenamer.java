// spufi.java
//
// This is a sample application that simulates a bare-bones spufi
// in java using jdbc.


import java.io.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Vector;

import com.drew.imaging.jpeg.JpegMetadataReader;
import com.drew.imaging.jpeg.JpegProcessingException;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.exif.ExifDirectory;

public class PhotoRenamer {
	public static final String crlf = System.getProperties().getProperty("line.separator");
	public static final char   eofchar[] = {0x1A};
	public static final String eofstr = new String(eofchar);
	private static SimpleDateFormat dateFormatter  = new SimpleDateFormat ("yyyy-MM-dd-HH.mm.ss.SSS'000'");
	private static List timeTags = null;
	private static List tagText  = null;
	private static String outDirectory  = null;
	
public PhotoRenamer() {
	super();
}

public static void main(String argv[]) throws Exception {
	if (argv.length <2) {
			System.out.println("Please enter at least input folder and hh:mm \n usage: PhotoRenamer inputFolder hh:mm includeMakes times.txt outputDirectory");
			System.exit(12);
	}
	String inDir = argv[0].trim();
	String minutes = argv[1];
	
	String matchMake = "";
	if (argv.length > 2) {
		matchMake = argv[2];
	}
	if (argv.length > 3) {
		String inFile = argv[3];
		readTimeTags(inFile);
	}
	if (argv.length > 4) {
		outDirectory = argv[4];
	}
	PhotoRenameProcessor dirProc = new PhotoRenameProcessor(inDir, minutes, matchMake);
	int numPhotos = dirProc.ProcessDirectory();
	System.out.println("Photo Renaming Completed. Total Number Renamed="+numPhotos);
}
/**
 * @param inFile
 * @throws Exception
 */
private static void readTimeTags(String inFile) throws Exception {
	
	timeTags = null;
	tagText  = null;
	if (inFile == null || inFile.equals(""))
		return;
	
	timeTags = new ArrayList();
	tagText  = new ArrayList();
		
	FileInputStream inStream = new FileInputStream(inFile);

	BufferedReader in = new BufferedReader(new InputStreamReader(inStream));
	String line  = in.readLine(); 
	int numLines = 0;

	while (line != null && !line.equals(eofstr)) {
	 
		String[] subStrings = line.split(" ");
	    String photoDate = subStrings[0].substring(0,10);
	    String photoTime = subStrings[0].substring(11,19);
	    String text  = subStrings[2];

		int year = new Integer(photoDate.substring(0,4)).intValue();
		int month = new Integer(photoDate.substring(5,7)).intValue();
		int day = new Integer(photoDate.substring(8,10)).intValue();
	
		int hour = new Integer(photoTime.substring(0,2)).intValue();
		int minute = new Integer(photoTime.substring(3,5)).intValue();
	
		int second = new Integer(photoTime.substring(6,8)).intValue();
		GregorianCalendar cal = new GregorianCalendar(year,month-1,day,hour,minute,second);
		timeTags.add(cal.getTime());
		tagText.add(text);
		line = in.readLine();
	}
	inStream.close();
	in.close();
}
public static String findTag(Date inputDate) {
	// finds a tag based on the input date
	String text = "";
	if (timeTags == null)
		return text;
		
	int numTags = timeTags.size();
	int textNo = 0;
	boolean found = false;
	for (int i=0;i<numTags;i++) {
		Date tagDate = (Date) timeTags.get(i);
		if (tagDate.after(inputDate)) {
			found = true;
			break;
		}
		textNo = i;
	}
	if (!found) textNo = numTags-1;
	text = "-"+(String) tagText.get(textNo);
	return text;
}
public static String getOutputDirectory(String parentDirectory, String folder) {
	// replace parentDirectory if outputDirectory Exists
	if (outDirectory == null)
		return parentDirectory;
	else
		return outDirectory + "\\" + folder;
}

}