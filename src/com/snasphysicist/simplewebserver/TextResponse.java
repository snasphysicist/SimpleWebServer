
package com.snasphysicist.simplewebserver;

public class TextResponse 
	extends Response 
{
	private String body;
	
	public TextResponse(
		Protocol protocol,
		int status,
		String title,
		String body
	) {
		super(
			protocol,
			status,
			title
		);
		this.body = body; 
	}
	
	@Override
	protected void generateContent()
	{
		generateTopMatter();
		content += "\r\n" + body;
	}
}