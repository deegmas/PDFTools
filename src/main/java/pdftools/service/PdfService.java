package pdftools.service;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.imageio.ImageIO;

import org.apache.pdfbox.multipdf.PDFMergerUtility;
import org.apache.pdfbox.multipdf.Splitter;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.encryption.AccessPermission;
import org.apache.pdfbox.pdmodel.encryption.InvalidPasswordException;
import org.apache.pdfbox.pdmodel.encryption.StandardProtectionPolicy;
import org.apache.pdfbox.rendering.PDFRenderer;

import pdftools.uploadingfiles.storage.StorageProperties;

/**
 * Class to processing PDF files
 * @author Deegmas
 *
 */
public class PdfService {
	
	private StorageProperties storageProperties = new StorageProperties();
	
	/**
	 * Convert a PDF to Image
	 * @param file (filename of the PDF)
	 * @return filename of the image
	 * @throws InvalidPasswordException
	 * @throws IOException
	 */
	public String pdfToImage(String file) throws InvalidPasswordException, IOException {
		PDDocument pd = PDDocument.load (new File(storageProperties.getLocation() + "/" + file));
	    PDFRenderer pr = new PDFRenderer (pd);
	    BufferedImage bi = pr.renderImageWithDPI (0, 300);
	    String filename = file.substring(0, file.lastIndexOf(".")) + ".jpeg";
	    String newFile = storageProperties.getLocation() + "/" + filename;
	    ImageIO.write (bi, "JPEG", new File (newFile));
	    return filename;
	}
	
	/**
	 * Merge multiple PDF to one
	 * @param String[] files (array of filename)
	 * @return filename of the mergedFile
	 * @throws InvalidPasswordException
	 * @throws IOException
	 */
	public String mergePdf(String[] files) throws InvalidPasswordException, IOException {
		
		File file[] = new File[files.length];
		PDDocument[] document = new PDDocument[files.length];
		
		for (int i = 0; i < files.length; i++) {
			file[i] = new File(storageProperties.getLocation() + "/" + files[i]);
			document[i] = PDDocument.load(file[i]);
		}

		PDFMergerUtility PDFmerger = new PDFMergerUtility();
		String filename = UUID.randomUUID().toString() + ".pdf";
		PDFmerger.setDestinationFileName(storageProperties.getLocation() + "/" + filename);  
		
		for (int i = 0; i < files.length; i++) {
			PDFmerger.addSource(file[i]);
		}
		
		PDFmerger.mergeDocuments(null);
		for (int i = 0; i < files.length; i++) {
			document[i].close();
		}
		
		return filename;
	}
	
	/**
	 * Split all pages in a zip file
	 * @param String pdfFile (name of the file)
	 * @return the name of the zip file
	 * @throws IOException
	 */
	public String splitPdf(String pdfFile) throws IOException {
		
		File file = new File(storageProperties.getLocation() + "/" + pdfFile);
		PDDocument document = PDDocument.load(file);

		Splitter splitter = new Splitter();
		List<PDDocument> Pages = splitter.split(document);
		Iterator<PDDocument> iterator = Pages.listIterator();

		List<String> srcFiles = new ArrayList<String>();
		int nbFiles = 1;
		String filename = pdfFile.substring(0, pdfFile.lastIndexOf("."));
		while (iterator.hasNext()) {
			PDDocument pd = iterator.next();
			pd.save(storageProperties.getLocation() + "/" + filename + nbFiles++ + ".pdf");
			srcFiles.add(storageProperties.getLocation() + "/" + filename + (nbFiles-1) + ".pdf");
		}
		document.close();
		
		String zipName = filename + ".zip";
		FileOutputStream fos = new FileOutputStream(storageProperties.getLocation() + "/" + zipName);
        ZipOutputStream zipOut = new ZipOutputStream(fos);
        for (String srcFile : srcFiles) {
            File fileToZip = new File(srcFile);
            FileInputStream fis = new FileInputStream(fileToZip);
            ZipEntry zipEntry = new ZipEntry(fileToZip.getName());
            zipOut.putNextEntry(zipEntry);

            byte[] bytes = new byte[1024];
            int length;
            while((length = fis.read(bytes)) >= 0) {
                zipOut.write(bytes, 0, length);
            }
            fis.close();
        }
        zipOut.close();
        fos.close();
		
		return zipName;
	}
	
	/**
	 * Encrypt PDF file with a password defined by user
	 * @param filename (PDF filename to encrypt)
	 * @param password (password defined by the user)
	 * @return the filename of the encrypted file
	 * @throws InvalidPasswordException
	 * @throws IOException
	 */
	public String encryptedPdf(String filename, 
							   String password) 
							   throws InvalidPasswordException, IOException {

		StorageProperties storageProperties = new StorageProperties();
		
		File file = new File(storageProperties.getLocation() + "/" + filename);
		PDDocument document = PDDocument.load(file);
		AccessPermission ap = new AccessPermission();
		
		StandardProtectionPolicy spp = new StandardProtectionPolicy(password, password, ap);
		spp.setEncryptionKeyLength(128);
		spp.setPermissions(ap);
		document.protect(spp);
		
		String filenameEncrypted = filename.substring(0,filename.lastIndexOf(".")) + ".pdf";
		document.save(storageProperties.getLocation() + "/" + filenameEncrypted);
		document.close();
		
		return filenameEncrypted;
	}
}