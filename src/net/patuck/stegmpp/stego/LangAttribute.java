/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.patuck.stegmpp.stego;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.Namespace;

/**
 *
 * @author reshad
 */
public class LangAttribute implements StegMethod
{

	/**
	 * The send method encodes one bit of data in the xml:lang tag in the body of a message.
	 * If the bit to be encoded in 0 there is no xml:lang tag
	 * @param tag
	 */
	@Override
	public void send(Document tag)
	{
		Element body = tag.getRootElement().getChild("body");
		if(body != null && Stego.hasNextBit())
		{
			if(Stego.getNextBit())
			{
				// Encode 1 as xml:lang=en
				body.setAttribute("lang", "en", Namespace.XML_NAMESPACE);
			}
			else
			{
				// Encode 0 as no xml:lang attribute
			}
		}
	}

	/**
	 * The receive method receives one bit of data from the presence or absence of the xml:lang tag in the body of a message.
	 * @param tag the message tag.
	 */
	@Override
	public void recieve(Document tag)
	{
		Element body = tag.getRootElement().getChild("body");
		if(body != null)
		{
			if(body.getAttributeValue("lang", Namespace.XML_NAMESPACE) == null)
			{
				Stego.setNextBit(false);
			}
			else
			{
				Stego.setNextBit(true);
			}
		}
	}
	
}
