package com.developpez.florentgarin.android;

import java.util.ArrayList;
import java.util.List;


import org.jivesoftware.smack.*;
import org.jivesoftware.smack.filter.*;
import org.jivesoftware.smack.packet.*;
import org.jivesoftware.smack.util.StringUtils;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.*;

public class ClientJabberActivity extends Activity {
	
	private final static String SERVER_HOST = "192.168.2.105";
	private final static int SERVER_PORT = 5222;
	private final static String SERVICE_NAME = "servicio.ubuntu";	
	private final static String LOGIN = "virtual";
	private final static String PASSWORD = "Virtual01";


	private List<String> m_discussionThread;
	private ArrayAdapter<String> m_discussionThreadAdapter;
	private XMPPConnection m_connection;
	private Handler m_handler;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        m_handler = new Handler();
		try {
			initConnection();
		} catch (XMPPException e) {
			e.printStackTrace();
		}
		
		final EditText recipient = (EditText) this.findViewById(R.id.recipient);
		final EditText message = (EditText) this.findViewById(R.id.message);		
		ListView list = (ListView) this.findViewById(R.id.thread);
		
		m_discussionThread = new ArrayList<String>();
		m_discussionThreadAdapter = new ArrayAdapter<String>(this,
				R.layout.multi_line_list_item, m_discussionThread);
		list.setAdapter(m_discussionThreadAdapter);
		
		Button send = (Button) this.findViewById(R.id.send);
		send.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				String to = recipient.getText().toString();
				String text = message.getText().toString();
		
				Message msg = new Message(to, Message.Type.chat);
				msg.setBody(text + " cgb");
				m_connection.sendPacket(msg);
				m_discussionThread.add("yo :");
				m_discussionThread.add(text);
				m_discussionThreadAdapter.notifyDataSetChanged();
			}
		});
	}


	private void initConnection() throws XMPPException {
		//Initialisation de la connexion
        ConnectionConfiguration config =
                new ConnectionConfiguration(SERVER_HOST, SERVER_PORT, SERVICE_NAME);
        m_connection = new XMPPConnection(config);
        m_connection.connect();
        m_connection.login(LOGIN, PASSWORD);
        Presence presence = new Presence(Presence.Type.available);
        m_connection.sendPacket(presence);
       
        //enregistrement de l'�couteur de messages
		PacketFilter filter = new MessageTypeFilter(Message.Type.chat);
		m_connection.addPacketListener(new PacketListener() {
				public void processPacket(Packet packet) {
					Message message = (Message) packet;
					if (message.getBody() != null) {
						String fromName = StringUtils.parseBareAddress(message
								.getFrom());
						m_discussionThread.add(fromName + ":");
						m_discussionThread.add(message.getBody());
						
						m_handler.post(new Runnable() {
							public void run() {
								m_discussionThreadAdapter.notifyDataSetChanged();
							}
						});
					}
				}
			}, filter);
	}

}