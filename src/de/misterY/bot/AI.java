package de.misterY.bot;

import java.util.ArrayList;
import java.util.Arrays;

import de.misterY.MeansOfTransportation;
import de.misterY.Path;
import de.misterY.PathFinder;
import de.misterY.Player;
import de.misterY.Station;

public class AI {
	private ArrayList<Station> resolvedPositions = new ArrayList<Station>();
	private ArrayList<Station> predictedPositions = new ArrayList<Station>();
	private ArrayList<MeansOfTransportation> ticketRecordMRY = new ArrayList<MeansOfTransportation>();
	private PositionResolver resolver;
	private PositionPredicter predicter;
	private Station lastMRYStation;
	private Player localPlayer;
	private int moveState;
	private Player mryHandle;
	private boolean isChasing = false;
	private int RESOLVER_PRECISION = 4;
	private int targetID;

	/**
	 * Initializes the AI
	 * 
	 * @param pStation
	 *            Our Starting position
	 * @param pPlayer
	 *            a Handle to the local player
	 * @param pMap
	 *            The map we are playing on
	 */
	public void initialize(Station pStation, Player pPlayer) {
		resolver = new PositionResolver();
		predicter = new PositionPredicter();
		localPlayer = pPlayer;
		lastMRYStation = pStation;
		moveState = 0;
	}

	/**
	 * This method is called after as soon as MRY has made a turn to update our data
	 * on him
	 * 
	 * @param pStation
	 *            The position where MRY was last seen
	 * @param ticket
	 *            The Ticket MRY last used
	 */
	public void updateData(Station pStation, MeansOfTransportation[] tickets) {
		if (resolvedPositions != null && resolvedPositions.size() == 1
				&& localPlayer.getCurrentStation().equals(resolvedPositions.get(0))
				|| localPlayer.getCurrentStation().equals(lastMRYStation)) {
			isChasing = true;
		} else {
			isChasing = false;
		}
		lastMRYStation = pStation;
		ticketRecordMRY.clear();
		ticketRecordMRY.addAll(Arrays.asList(tickets));
		resolver.updateData(tickets[tickets.length - 1], pStation);
		resolvedPositions = resolver.resolve(RESOLVER_PRECISION);
		if (resolvedPositions != null && resolvedPositions.size() == 1) {
			predictedPositions = predicter.getAllPredictions(resolvedPositions.get(0), mryHandle);
		}

	}

	/**
	 * Analyses the Situation and tries to find the best moveState
	 * 
	 */
	public void doAnalysis() {
		// Check everything in order of importance
		if (resolvedPositions == null) {
			moveState = 0;
		} else if (resolvedPositions.size() == 1 && predictedPositions.size() == 1
				&& resolvedPositions.get(0) != localPlayer.getCurrentStation()
				&& predictedPositions.get(0) != localPlayer.getCurrentStation()) {
			moveState = 2;
		} else if (resolvedPositions.size() == 1 && resolvedPositions.get(0) != localPlayer.getCurrentStation()) {
			moveState = 1;
		} else if (isChasing) {
			moveState = 5;
		} else {
			moveState = 0;
		}

	}

	public void moveExecute() {
		switch (moveState) {
		case 0: // Nothing useful to do, just go in a random direction
			targetID = -1;
			break;
		case 1: // Go to Definitive Resolved Position
		case 2: // Go to Definitive Predicted Position
			Path path = PathFinder.findPath(localPlayer.getCurrentStation(),
					(moveState == 1 ? resolvedPositions : predictedPositions).get(0));
			Station targetStation = path.getLastStation();
			while (targetStation != null && PathFinder.getPossibleMeansOfTransportation(localPlayer.getCurrentStation(),
					targetStation).length == 0) {
				targetStation = path.getPreviousStation(targetStation);
			}
			targetID = targetStation == null ? -1 : targetStation.getId();
			break;
		case 5: // We are chasing MRY & are one turn behind him, pick a link that matches his
				// ticket to maybe get him
			targetID = -5;
			break;
		}
	}

	public int getTarget() {
		return targetID;
	}

	public void setMryHandle(Player mryHandle) {
		this.mryHandle = mryHandle;
	}

	public Player getLocalPlayer() {
		return localPlayer;
	}
}
