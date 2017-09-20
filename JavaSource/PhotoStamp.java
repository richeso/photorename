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

/*
 * Created on Sep 21, 2007
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */

/**
 * @author richard
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class PhotoStamp {

	public PhotoStamp() {
		super();
	}

	public static void main(String argv[]) throws Exception {
		if (argv.length != 2) {
			System.out.println("Please enter input and output folder\n usage: Photostamp inputFolder outputFolder");
			System.exit(12);
		}
		String inputDir    = argv[0];
		String destFolder  = argv[1];
		PhotoStampProcessor photoProc = new PhotoStampProcessor(inputDir, destFolder);
		photoProc.ProcessDirectory();
	}
}
