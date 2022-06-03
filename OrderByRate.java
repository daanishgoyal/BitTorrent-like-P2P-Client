

import java.util.Comparator;
public class OrderByRate implements Comparator<PeerStateInfo> {
	private boolean toggleOrder;

	public OrderByRate() {
		this.toggleOrder = true;
	}

	/**
	 
	 * @param toggleOrder
	 */
	public OrderByRate(boolean toggleOrder) {
		this.toggleOrder = toggleOrder;
	}


	public int compare(PeerStateInfo state1, PeerStateInfo state2) {
		if (state1 == null && state2 == null)
			return 0;

		if (state1 == null)
			return 1;

		if (state2 == null)
			return -1;

		if (state1 instanceof Comparable) {
			if (toggleOrder)
				return state1.compareTo(state2);
			else
				return state2.compareTo(state1);
		} 
		else {
			if (toggleOrder)
				return state1.toString().compareTo(state2.toString());
			else
				return state2.toString().compareTo(state1.toString());
		}
	}
}
