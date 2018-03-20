package de.misterY.server;

import java.util.ArrayList;

public class Sessions {
	private ArrayList<Session> sessions;
	private Session fillingSession;
	private int lastId = 0;

	public Sessions() {
		sessions = new ArrayList<Session>();
		Session s = new Session(0);
		sessions.add(s);
		fillingSession = sessions.get(0);
	}

	public void addSession(Session s) {
		sessions.add(s);
	}

	public void removeSession(Session s) {
		sessions.remove(s);
	}

	public void placeUserInSession(User u) {
		if (!fillingSession.isFull() && !fillingSession.isActive()) {
			fillingSession.addUser(u);
		} else {
			sessions.add(fillingSession);
			fillingSession = new Session(++lastId);
			fillingSession.addUser(u);
		}
	}

	/**
	 * Returns the session containing the given user or null if no session contains
	 * the user.
	 * 
	 * @param user
	 *            The user
	 * @return The session containing the given user or null if no session contains
	 *         the user.
	 */
	public Session getSessionByUser(User user) {
		if (fillingSession.doesContain(user)) {
			return fillingSession;
		}
		for (Session session : sessions) {
			if (session.doesContain(user)) {
				return session;
			}
		}
		return null;
	}

	public int getSessionCount() {
		return sessions.size();
	}

	public ArrayList<Session> getSessionList() {
		return sessions;
	}
}
