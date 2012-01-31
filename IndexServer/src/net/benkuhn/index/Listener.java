package net.benkuhn.index;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.SortedSet;
import java.util.Map.Entry;


public class Listener extends Thread {
	//TODO figure out a better way to get debug data in
	//TODO logging
	
	public static void main(String[] args) {
		Index idx = new Index(61, 4);
		new Listener(16180, idx).start();
	}
	
	boolean listening = true;
	int port;
	Index idx;
	
	public Listener(int port, Index idx) {
		super();
		this.port = port;
		this.idx = idx;
	}

	@Override
	public void run() {
		ServerSocket sock;
		try {
			sock = new ServerSocket(port);
		}
		catch (IOException e) {
			System.out.println("Creating socket failed: " + e.getLocalizedMessage());
			return;
		}
		System.out.println("listening on port " + port);
		while(listening) {
			Socket client;
			try {
				System.out.println("waiting...");
				client = sock.accept();
				System.out.println("got a client");
			}
			catch (IOException e) {
				System.out.println("Listening for client failed: "
						+ e.getLocalizedMessage());
				continue;
			}
			new ClientHandler(client).start();
		}
		try {
			sock.close();
		}
		catch (IOException e) {
			System.out.println("Couldn't close socket: " + e.getLocalizedMessage());
			return;
		}
	}
	
	public static int[] parseArray(String text) {
		int i = 0;
		ArrayList<Integer> ints = new ArrayList<Integer>();
		while (i >= 0 && i < text.length()) {
			if (Character.isDigit(text.charAt(i))) {
				int end = text.indexOf(' ', i);
				if (end == -1) end = text.length();
				int val = Integer.parseInt(text.substring(i, end));
				ints.add(val);
				i = end;
			}
			else {
				i += 1;
			}
		}
		int[] ret = new int[ints.size()];
		for (int j = 0; j < ints.size(); j++) {
			ret[j] = ints.get(j);
		}
		return ret;
	}
	public class ClientHandler extends Thread {
		Socket client;
		public ClientHandler(Socket client) {
			super();
			this.client = client;
		}
		@Override
		public void run() {
			try {
				BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));
				PrintStream out = new PrintStream(client.getOutputStream());
				boolean go = true;
				while (go) {
					out.print("cmd>");
					String cmd = in.readLine();
					if (cmd.equals("add")) {
						out.print("id>");
						String id = in.readLine();
						out.print("notes>");
						String text = in.readLine();
						int[] notes = parseArray(text);
						idx.add(id, notes);
					}
					else if (cmd.equals("del")) {
						out.print("id>");
						String id = in.readLine();
						idx.del(id);
					}
					else if (cmd.equals("search")) {
						out.print("notes>");
						String text = in.readLine();
						int[] notes = parseArray(text);
						idx.search(notes, out);
					}
					else if (cmd.equals("align")) {
						out.print("id>");
						String id = in.readLine();
						out.print("notes>");
						String text = in.readLine();
						int[] notes = parseArray(text);
						Match m = idx.getMatch(id, notes);
						for (Entry<Integer, SortedSet<Integer>[]> e : m.hits.entrySet()) {
							out.println(e.getKey() + ":");
							for (int i = 0; i < e.getValue().length; i++) {
								out.print("  " + i + ": ");
								if (e.getValue()[i] == null) {
									out.print("nothing");
									continue;
								}
								for (int j : e.getValue()[i]) {
									out.print(j + ", ");
								}
								out.println();
							}
						}
						//TODO handle null ID
						Alignment align = idx.align(m.computeOffset(), m);
						out.print(align);
					}
					else if (cmd.equals("bye")) {
						go = false;
					}
					else {
						out.print("unrecognized command, please try"
								+ " `add', `del', `search', or `bye'\n");
					}
				}
				in.close();
				out.close();
				client.close();
				System.out.println("client done");
			}
			catch (IOException e) {
				System.out.println("IO exception" + e.getLocalizedMessage());
			}
		}
	}
}
