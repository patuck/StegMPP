package stegmpp.xmpp;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.ConnectException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.logging.Level;
import java.util.logging.Logger;
import stegmpp.ui.Style;
import stegmpp.ui.UI;

/**
 * The connection class handles connecting and disconnecting from an XMPP Server.
 * @author reshad
 */
public class Connection
{
	
	private Socket socket;
	private static UI ui = UI.getUI();
	
	
	
	/**
	 * The connect method is used to connect to the XMPP server.
	 */
	public PrintWriter connect()
	{
		
		ui.print("[System] ",Style.SYSTEM);
		ui.println("Connecting to XMPP Server");
		
		try
		{
			socket = new Socket(Session.server, Session.port);
			// Create and start a new Receiver thread.
			Receiver receiver = new Receiver(socket.getInputStream());
			Thread inThread = new Thread(receiver);
			inThread.start();
			
			PrintWriter pw=new PrintWriter(socket.getOutputStream());
			Session.pw = pw;
			
			// Create connection.
			pw.println("<?xml version='1.0' ?>");
			pw.flush();
			pw.println("<stream:stream to='" + Session.server + "' xmlns='jabber:client' xmlns:stream='http://etherx.jabber.org/streams' version='1.0'>");
			pw.flush();
			
			// Authentication.
			ui.print("[System] ",Style.SYSTEM);
			Authentication.setPrintWriter(pw);
			if(Authentication.sendAuth("PLAIN", Session.username, Session.username))
			{
				ui.println("Authentication: Successful");
			}
			else
			{
				ui.println("Authentication failed. please check connection setting and try again");
				return null;
			}
			
			
			pw.println("<stream:stream to='" + Session.server + "' xmlns='jabber:client' xmlns:stream='http://etherx.jabber.org/streams' version='1.0'>");
			pw.flush();
			
			// Send IQ requests.
			IQ.setPrintWriter(pw);
			IQ.sendIQ("set", null, "<bind xmlns='urn:ietf:params:xml:ns:xmpp-bind'/>");
			IQ.sendIQ("set", null, "<session xmlns='urn:ietf:params:xml:ns:xmpp-session'/>");
			IQ.sendIQ("get", Session.server, "<query xmlns='http://jabber.org/protocol/disco#info'/>");
			IQ.sendIQ("get", null, "<vCard xmlns='vcard-temp'/>");
			IQ.sendIQ("get", null, "<query xmlns='jabber:iq:roster'/>");
			IQ.sendIQ("get", Session.server, "<query xmlns='http://jabber.org/protocol/disco#items' node='http://jabber.org/protocol/commands'/>");
		//	IQ.sendIQ("get", null, "<blocklist xmlns='urn:xmpp:blocking'/>");
		//	IQ.sendIQ("get", "proxy.eu.jabber.org", "<query xmlns='http://jabber.org/protocol/bytestreams'/>");
		//	IQ.sendIQ("get", "conference." + Session.server, "<query xmlns='http://jabber.org/protocol/disco#info'/>");
		//	IQ.sendIQ("get", "irc." + server, "<query xmlns='http://jabber.org/protocol/disco#info'/>");
		//	IQ.sendIQ("get", "pubsub." + Session.server, "<query xmlns='http://jabber.org/protocol/disco#info'/>");
		//	IQ.sendIQ("get", "vjud." + server, "<query xmlns='http://jabber.org/protocol/disco#info'/>");

			// Send presence.
			pw.println("<presence><priority>1</priority><show>chat</show></presence>");
			pw.flush();
			
			// Enable messaging.
			ui.enableMessaging();
			return pw;
			
		}
		catch (UnknownHostException | ConnectException ex)
		{
			ui.print("[System] ",Style.SYSTEM);
			ui.println("Error: wrong hostname, please check the server.");
		}
		catch (IOException ex)
		{
			Logger.getLogger(Connection.class.getName()).log(Level.SEVERE, null, ex);
		}
		return null;
		
	}
	
	/**
	 * The disconnect method is used to end the XML stream and disconnect form the server.
	 */
	public void disconnect()
	{
		Session.pw.println("</stream:stream>");
		Session.pw.flush();
		ui.print("[System] ",Style.SYSTEM);
		ui.println("Disconnected.");
	}
}
