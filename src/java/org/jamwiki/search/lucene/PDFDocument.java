/**
 *
 */
package org.jamwiki.search.lucene;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStreamWriter;
import org.pdfbox.encryption.DecryptDocument;
import org.pdfbox.exceptions.CryptographyException;
import org.pdfbox.exceptions.InvalidPasswordException;
import org.pdfbox.pdfparser.PDFParser;
import org.pdfbox.pdmodel.PDDocument;
import org.pdfbox.util.PDFTextStripper;

/**
 * Get the content of a PDF file
 */
public class PDFDocument {

	/**
	 * Actually get the content of a PDF file
	 *
	 * @param attachmentFileName
	 *			String with the filename of the file to read
	 * @param attachmentFile
	 *			File handler of the file to read
	 * @return StringBuffer containing the (textual) content of the PDF file
	 * @throws FileNotFoundException
	 *			 If the PDF file cannot be found
	 * @throws IOException
	 *			 If the PDF file cannot be read
	 */
	public static StringBuffer getContentOfPDFFile(
		String attachmentFileName,
		File attachmentFile)
		throws FileNotFoundException, IOException {
		StringBuffer contents = new StringBuffer();
		FileInputStream input = null;
		try {
			input = new FileInputStream(attachmentFile);
			PDDocument pdfDocument = null;
			try {
				PDFParser parser = new PDFParser(input);
				parser.parse();

				pdfDocument = parser.getPDDocument();

				if (pdfDocument.isEncrypted()) {
					DecryptDocument decryptor =
						new DecryptDocument(pdfDocument);
					//Just try using the default password and move on
					decryptor.decryptDocument("");
				}

				//create a tmp output stream with the size of the content.
				ByteArrayOutputStream out = new ByteArrayOutputStream();
				OutputStreamWriter writer = new OutputStreamWriter(out);
				PDFTextStripper stripper = new PDFTextStripper();
				stripper.writeText(pdfDocument.getDocument(), writer);
				writer.close();

				contents.append(" ");
				contents.append(out.toString());
			} catch (CryptographyException e) {
				throw new IOException(
					"Error decrypting document("
						+ attachmentFileName
						+ "): "
						+ e);
			} catch (InvalidPasswordException e) {
				//they didn't suppply a password and the default of "" was
				// wrong.
				throw new IOException(
					"Error: The document("
						+ attachmentFileName
						+ ") is encrypted and will not be indexed.");
			} finally {
				if (pdfDocument != null) {
					pdfDocument.close();
				}
			}
		} finally {
			if (input != null) {
				input.close();
			}
		}
		return contents;
	}
}
