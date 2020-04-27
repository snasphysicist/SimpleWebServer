
package com.snasphysicist.simplewebserver;

import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

public class StaticTextResponse 
	extends Response 
{

	private final static Logger LOG = Logger.getLogger(
		Logger.class.getName()
	);
	
	URL filePath;
	
	public StaticTextResponse(
		Protocol protocol, 
		int status, 
		String title, 
		URL filePath
	) {
		super(
			protocol,
			status, 
			title
		);
		this.filePath = filePath;
	}

	public static ContentType guessContentType(
		URL filePath
	) {
		
		String path = filePath.getPath();
		int i = path.length();
		while(i > 0) {
			if(path.substring(i - 1, i).equals(".")) {
				break;
			}
			i--;
		}
		
		switch(path.substring(i).toLowerCase()) {
			case "html": {
				return ContentType.TEXTHTML;
			}
			case "css": {
				return ContentType.TEXTCSS;
			}
			case "js": {
				return ContentType.APPLICATIONJAVASCRIPT;
			}
			case "bmp": {
				return ContentType.IMAGEBMP;
			}
			case "jpg":
			case "jpeg": {
				return ContentType.IMAGEJPEG;
			}
			case "png": {
				return ContentType.IMAGEPNG;
			}
			default: {
				return ContentType.APPLICATIONOCTETSTREAM;
			}
		}
	}
	
	public static Header generateContentTypeHeader(
		ContentType contentType
	) {
		String contentTypeString;
		switch(contentType) {
			case TEXTHTML: {
				contentTypeString = "text/html";
				break;
			}
			case TEXTCSS: {
				contentTypeString = "text/css";
				break;
			}
			case APPLICATIONJAVASCRIPT: {
				contentTypeString = "application/javascript";
				break;
			}
			case IMAGEBMP: {
				contentTypeString = "image/bmp";
				break;
			}
			case IMAGEJPEG: {
				contentTypeString = "image/jpeg";
				break;
			}
			case IMAGEPNG: {
				contentTypeString = "image/png";
				break;
			}
			case APPLICATIONJSON: {
				contentTypeString = "application/json";
				break;
			} 
			case APPLICATIONOCTETSTREAM: 
			default: {
				contentTypeString = "application/octet-stream";
			}
		}
		
		return new Header(
			"content-type", 
			contentTypeString
		);
	}
	
	private byte[] byteArrayListToArray(
		ArrayList<Byte> arrayList
	) {
		byte[] array = new byte[arrayList.size()];
		for(int i = 0; i < arrayList.size(); i++) {
			array[i] = arrayList.get(i);
		}
		return array;
	}
	
	private void loadBody() {
		content += "\n" ;
		try {
			String body = "";
			ArrayList<Byte> bytes = new ArrayList<Byte>();
			InputStream fileStream = filePath.openStream();
			Byte nextByte = (byte) fileStream.read();
			while(nextByte != -1) {
				bytes.add(
					nextByte
				);
				nextByte = (byte) fileStream.read();
			}
			fileStream.close();
			body = new String(
				byteArrayListToArray(
					bytes
				), 
				StandardCharsets.UTF_8
			);
			content += body;
			LOG.log(
				Level.FINE, 
				String.format(
					"Read body of length %d", 
					body.length()
				)
			);
		}
		catch(Exception e) {
			LOG.log(
				Level.WARNING, 
				String.format(
					"Could not load file %s",
					filePath
				)
			);
			content += "<html><body><p>500: Experienced an Internal Server Error</p></body></html>" ;
		}
	}
	
	protected void generateContent() {
		addHeader(
			generateContentTypeHeader(
				guessContentType(
					filePath
				)
			)
		);
		generateTopMatter();
		loadBody();
		System.out.println(
			content
		);
	}

}
