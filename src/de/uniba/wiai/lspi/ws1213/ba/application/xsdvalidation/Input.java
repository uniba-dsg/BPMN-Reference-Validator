package de.uniba.wiai.lspi.ws1213.ba.application.xsdvalidation;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;

import org.w3c.dom.ls.LSInput;
import org.w3c.dom.ls.LSResourceResolver;

/**
 * Customized LSInput in order to provide access to an resolved resource.
 * 
 * Needed for <xs:include>-resolution when generating a {@link javax.xml.validation.Schema} when using
 * getClass().getResourceAsStream("path/to/file.xsd")
 * 
 * @author Matthias Geiger
 * @version 1.0
 * @see LSInput
 * @see LSResourceResolver
 * @see ResourceResolver
 */
public class Input implements LSInput {

	private String publicId;

	private String systemId;

	private BufferedInputStream inputStream;

	/**
	 * Constructor to generate a customized input source Input using an InputStream
	 * @param publicId
	 * @param sysId
	 * @param input
	 */
	public Input(String publicId, String sysId, InputStream input) {
		this.publicId = publicId;
		this.systemId = sysId;
		this.inputStream = new BufferedInputStream(input);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.w3c.dom.ls.LSInput#getPublicId()
	 */
	@Override
	public String getPublicId() {
		return publicId;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.w3c.dom.ls.LSInput#setPublicId(java.lang.String)
	 */
	@Override
	public void setPublicId(String publicId) {
		this.publicId = publicId;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.w3c.dom.ls.LSInput#getBaseURI()
	 */
	@Override
	public String getBaseURI() {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.w3c.dom.ls.LSInput#getByteStream()
	 */
	@Override
	public InputStream getByteStream() {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.w3c.dom.ls.LSInput#getCertifiedText()
	 */
	@Override
	public boolean getCertifiedText() {
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.w3c.dom.ls.LSInput#getCharacterStream()
	 */
	@Override
	public Reader getCharacterStream() {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.w3c.dom.ls.LSInput#getEncoding()
	 */
	@Override
	public String getEncoding() {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.w3c.dom.ls.LSInput#getStringData()
	 */
	@Override
	public String getStringData() {
		synchronized (inputStream) {
			try {
				byte[] input = new byte[inputStream.available()];
				inputStream.read(input);
				return new String(input);
			} catch (IOException e) {
				e.printStackTrace();
				System.out.println("Exception " + e);
				return null;
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.w3c.dom.ls.LSInput#setBaseURI(java.lang.String)
	 */
	@Override
	public void setBaseURI(String baseURI) {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.w3c.dom.ls.LSInput#setByteStream(java.io.InputStream)
	 */
	@Override
	public void setByteStream(InputStream byteStream) {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.w3c.dom.ls.LSInput#setCertifiedText(boolean)
	 */
	@Override
	public void setCertifiedText(boolean certifiedText) {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.w3c.dom.ls.LSInput#setCharacterStream(java.io.Reader)
	 */
	public void setCharacterStream(Reader characterStream) {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.w3c.dom.ls.LSInput#setEncoding(java.lang.String)
	 */
	@Override
	public void setEncoding(String encoding) {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.w3c.dom.ls.LSInput#setStringData(java.lang.String)
	 */
	@Override
	public void setStringData(String stringData) {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.w3c.dom.ls.LSInput#getSystemId()
	 */
	@Override
	public String getSystemId() {
		return systemId;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.w3c.dom.ls.LSInput#setSystemId(java.lang.String)
	 */
	@Override
	public void setSystemId(String systemId) {
		this.systemId = systemId;
	}


	public BufferedInputStream getInputStream() {
		return inputStream;
	}


	public void setInputStream(BufferedInputStream inputStream) {
		this.inputStream = inputStream;
	}

}
