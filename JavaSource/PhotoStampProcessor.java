// spufi.java
//
// This is a sample application that simulates a bare-bones spufi
// in java using jdbc.


import java.io.*;
import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.font.GlyphVector;
import java.awt.image.BufferedImage;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;
import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.imageio.ImageIO;

import com.sun.image.codec.jpeg.JPEGCodec;
import com.sun.image.codec.jpeg.JPEGEncodeParam;
import com.sun.image.codec.jpeg.JPEGImageEncoder;

/**
 * @author richard
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class PhotoStampProcessor {
	public static final String crlf = System.getProperties().getProperty("line.separator");
	public static final char   eofchar[] = {0x1A};
	public static final String eofstr = new String(eofchar);
	private static SimpleDateFormat dateFormatter  = new SimpleDateFormat ("yyyy-MM-dd-HH.mm.ss");
	private String inDir;
	private String outDir; 
	
public PhotoStampProcessor(String dir1, String dir2) {
	super();
	inDir = dir1;
	outDir = dir2;
	
}

/**
 * @param inDir
 * @param hours
 * @throws Exception
 */
public int ProcessDirectory()
	throws Exception {
	
	File fileDir = new File(inDir);
	File[] strFilesDirs = fileDir.listFiles ( );
	int numPhotos = 0;
	for ( int i = 0 ; i < strFilesDirs.length ; i ++ ) {
		if ( strFilesDirs[i].isDirectory ( ) ) {
			System.out.println ( "Directory: " + strFilesDirs[i] ) ;
			PhotoStampProcessor dirProc = new PhotoStampProcessor(strFilesDirs[i].getAbsolutePath(), outDir);
			numPhotos  += dirProc.ProcessDirectory();
		}
		else if ( strFilesDirs[i].isFile ( ) ) {
			String fileName = strFilesDirs[i].getName();
			if (fileName.toUpperCase().endsWith(".JPG")) {
				//System.out.println ( "File: " + strFilesDirs[i] + " (" + strFilesDirs[i].length ( ) + ")" ) ;
				int lastDot = fileName.lastIndexOf(".");
				File aFile = strFilesDirs[i];
				String imprintText = fileName.substring(0,lastDot);
				try {
					numPhotos+= addImprintText(aFile,outDir,imprintText);
				} catch (Exception e) {
					System.out.println("Failed to imprint Photo: "+fileName+ " Error: "+e.getMessage());
				}
			}
		}
	}
	return numPhotos;
}
/**
 * @param inputFile
 * @param imprintText
 * @throws Exception
 */
public static int addImprintText(File inputFile,  String destFolder, String imprintText) throws Exception {
	String inputFilename = inputFile.getName();
	if (imprintText.equals("")) {
		int lastDot = inputFilename.lastIndexOf(".");
		imprintText = inputFilename.substring(0,lastDot);
	}
	System.out.println("Starting with '" + inputFilename + "'");
	BufferedImage input = ImageIO.read(inputFile);
	Graphics2D graphics = (Graphics2D) input.createGraphics();
	// set fontsize of the text imprint	
	int fontSize = (int) (0.4 * input.getHeight() / 18.0);
	Font font = new Font("Arial", Font.BOLD, fontSize);
	graphics.setFont(font);
	int height = graphics.getFontMetrics().getHeight();
	int width = graphics.getFontMetrics().stringWidth(imprintText);

	BufferedImage dateImage = new BufferedImage(2 * fontSize + width, 2
		* fontSize + height, BufferedImage.TYPE_INT_ARGB);
	Graphics2D dateGraphics = (Graphics2D) dateImage.getGraphics().create();
	dateGraphics.setColor(new Color(0, 0, 0, 0));
	dateGraphics.setComposite(AlphaComposite.Src);
	dateGraphics.fillRect(0, 0, width, height);

	dateGraphics.setComposite(AlphaComposite.SrcOver);
	dateGraphics.setFont(font);
	dateGraphics.setColor(Color.black);
	dateGraphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
		RenderingHints.VALUE_ANTIALIAS_ON);
	GlyphVector gv = font.createGlyphVector(dateGraphics
		.getFontRenderContext(), imprintText);
	// dateGraphics.drawString(dateStamp, fontSize, fontSize);
	dateGraphics.translate(fontSize, 2 * fontSize);
	for (int i = 0; i < gv.getNumGlyphs(); i++) {
	  Shape glyph = gv.getGlyphOutline(i);
	  dateGraphics.setStroke(new BasicStroke(fontSize / 10));
	  dateGraphics.draw(glyph);
	}
	float[] kernel = new float[25];
	for (int i = 0; i < kernel.length; i++)
	  kernel[i] = 1.0f / kernel.length;
	ConvolveOp cOp = new ConvolveOp(new Kernel(5, 5, kernel));
	BufferedImage blurred = cOp.filter(dateImage, null);
	dateGraphics.dispose();

	// set location of the imprint text
	int iWidth  = input.getWidth();
	int iHeight = input.getHeight();
	
	int cHeight = iWidth * 80/100;
	int dHeight = (iHeight - cHeight) * 30/100;

	int y = (iHeight - dHeight) - blurred.getHeight();
	int x = iWidth - blurred.getWidth();
	 
	if (iHeight > iWidth) { // rotated photo
			int temp = y;
			x = x * 95/100;
			y = y * 100/82;
	}
	 
	graphics.drawImage(blurred, x - fontSize, y - 2 * fontSize, null);
	graphics.setFont(font);
	graphics.setColor(Color.white);
	graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
		RenderingHints.VALUE_ANTIALIAS_ON);
	graphics.drawString(imprintText, x, y);
	graphics.dispose();

	String outputFilename = destFolder + "\\" + inputFile.getName();
	createDir(outputFilename);
	FileOutputStream out = new FileOutputStream(outputFilename);
	/* encodes image as a JPEG data stream */
	JPEGImageEncoder encoder = JPEGCodec.createJPEGEncoder(out);
	JPEGEncodeParam param = encoder.getDefaultJPEGEncodeParam(input);
	param.setQuality(1f, true);
	encoder.setJPEGEncodeParam(param);
	encoder.encode(input);
	out.flush();
	out.close();
	return +1;	 
  }/**
  * @param inputFile
  * @param imprintText
  * @throws Exception
  */
 public static int addImprintText_original(File inputFile,  String destFolder, String imprintText) throws Exception {
	 String inputFilename = inputFile.getName();
	 if (imprintText.equals("")) {
		 int lastDot = inputFilename.lastIndexOf(".");
		 imprintText = inputFilename.substring(0,lastDot);
	 }
	 System.out.println("Starting with '" + inputFilename + "'");
	 BufferedImage input = ImageIO.read(inputFile);
	 Graphics2D graphics = (Graphics2D) input.createGraphics();
		
	 int fontSize = (int) (0.4 * input.getHeight() / 10.0);
	 Font font = new Font("Arial", Font.BOLD, fontSize);
	 graphics.setFont(font);
	 int height = graphics.getFontMetrics().getHeight();
	 int width = graphics.getFontMetrics().stringWidth(imprintText);

	 BufferedImage dateImage = new BufferedImage(2 * fontSize + width, 2
		 * fontSize + height, BufferedImage.TYPE_INT_ARGB);
	 Graphics2D dateGraphics = (Graphics2D) dateImage.getGraphics().create();
	 dateGraphics.setColor(new Color(0, 0, 0, 0));
	 dateGraphics.setComposite(AlphaComposite.Src);
	 dateGraphics.fillRect(0, 0, width, height);

	 dateGraphics.setComposite(AlphaComposite.SrcOver);
	 dateGraphics.setFont(font);
	 dateGraphics.setColor(Color.black);
	 dateGraphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
		 RenderingHints.VALUE_ANTIALIAS_ON);
	 GlyphVector gv = font.createGlyphVector(dateGraphics
		 .getFontRenderContext(), imprintText);
	 // dateGraphics.drawString(dateStamp, fontSize, fontSize);
	 dateGraphics.translate(fontSize, 2 * fontSize);
	 for (int i = 0; i < gv.getNumGlyphs(); i++) {
	   Shape glyph = gv.getGlyphOutline(i);
	   dateGraphics.setStroke(new BasicStroke(fontSize / 10));
	   dateGraphics.draw(glyph);
	 }
	 float[] kernel = new float[25];
	 for (int i = 0; i < kernel.length; i++)
	   kernel[i] = 1.0f / kernel.length;
	 ConvolveOp cOp = new ConvolveOp(new Kernel(5, 5, kernel));
	 BufferedImage blurred = cOp.filter(dateImage, null);
	 dateGraphics.dispose();

	 int iWidth = input.getWidth();
	 int iHeight = input.getHeight();
	 int cHeight = iWidth * 2 / 3;
	 int dHeight = (iHeight - cHeight) / 2;

	 int y = (iHeight - dHeight) - blurred.getHeight();
	 int x = iWidth - blurred.getWidth();

	 graphics.drawImage(blurred, x - fontSize, y - 2 * fontSize, null);
	 graphics.setFont(font);
	 graphics.setColor(Color.white);
	 graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
		 RenderingHints.VALUE_ANTIALIAS_ON);
	 graphics.drawString(imprintText, x, y);
	 graphics.dispose();

	 String outputFilename = destFolder + "\\" + inputFile.getName();
	 createDir(outputFilename);
	 FileOutputStream out = new FileOutputStream(outputFilename);
	 /* encodes image as a JPEG data stream */
	 JPEGImageEncoder encoder = JPEGCodec.createJPEGEncoder(out);
	 JPEGEncodeParam param = encoder.getDefaultJPEGEncodeParam(input);
	 param.setQuality(0.9f, true);
	 encoder.setJPEGEncodeParam(param);
	 encoder.encode(input);
	 out.flush();
	 out.close();
	 return +1;	 
   }
//create directory for parent if it doesn't exist
  public static void createDir (String aFile)
  {
	  File inFile = new File(aFile);
	  File inDir = new File(inFile.getParent());
	  if (!inDir.isDirectory())
		 inDir.mkdirs();
  }

}