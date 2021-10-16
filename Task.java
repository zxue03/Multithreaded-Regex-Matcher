import java.util.*;
import java.util.concurrent.*;

public class Task implements Runnable {
	private Queue<Integer[]> queue;
	private String s;
	private NFA nfa;
	private List<Boolean> indicator;
	private Integer[] curr;
	private int maxsize;
	private Set<Integer[]> visited_e;
	public static int completed;

	Task(Integer[] curr, Queue<Integer[]> queue, int maxsize, String s, NFA nfa, List<Boolean> indicator,
			Set<Integer[]> visited_e) {
			this.queue = queue;
			this.s = s;
			this.nfa = nfa;
			this.indicator = indicator;
			this.curr = curr;
			this.maxsize = maxsize;
			this.visited_e = visited_e;
	}

	public void run() {
		try {
			Integer curr_state = curr[0];
			Integer curr_index = curr[1];
			while (true) {
				
				if (curr_index == (s.length() - 1) && nfa.final_states.contains(curr_state)) {
//					System.out.println("Found");
					synchronized (Task.class) {
						indicator.add(true);
					}
					break;
				}
				List<Map.Entry<Character, Object>> possible_transitions = nfa.transition(curr_state);
				List<Map.Entry<Character, Object>> relevant_transitions = new ArrayList<>();
				for (Map.Entry<Character, Object> transition : possible_transitions) {
					if (transition.getKey().equals('#')) {
						relevant_transitions.add(transition);
					} else if (curr_index < s.length() - 1 && transition.getKey().equals(s.charAt(curr_index + 1))) {
						relevant_transitions.add(transition);
					}
				}
				if (relevant_transitions.size() == 0) {
					break;
				} else if (relevant_transitions.size() == 1) {
					if (relevant_transitions.get(0).getKey().equals('#')) {
						synchronized (Task.class) {
							Integer[] e1 = { (Integer) relevant_transitions.get(0).getValue(), curr_index };
							Integer[] e2 = { curr_state, curr_index };
							boolean repeated = false;
							for (Integer[] e : visited_e) {
								if (e[0].equals(e1[0]) && e[1].equals(e1[1])) {
									repeated = true;
								}
							}
							if (repeated) {
								break;
							}
							visited_e.add(e1);
							visited_e.add(e2);
						}
						curr_state = (Integer) relevant_transitions.get(0).getValue();
						continue;
					}
					if (curr_index == s.length() - 1) {
						break;

					}
					if (relevant_transitions.get(0).getKey().equals(s.charAt(curr_index + 1))) {
//						System.out.println("char equals");
						curr_state = (Integer) relevant_transitions.get(0).getValue();
						curr_index++;
						continue;
					}

				} else {

					for (Map.Entry<Character, Object> transition : relevant_transitions) {

						if (transition.getKey().equals('#')) {

							Integer next_state = (Integer) transition.getValue();

							Integer[] next = { next_state, curr_index };

							synchronized (Task.class) {
								boolean repeated = false;
								for (Integer[] e : visited_e) {
									if (e[0].equals(next[0]) && e[1].equals(next[1])) {
										repeated = true;
									}
								}
								if (repeated) {
									continue;
								}
//									Task.class.notify();
								Integer[] current = { curr_state, curr_index };
								visited_e.add(current);
								visited_e.add(next);
								queue.add(next);
							}

							System.out.println("#");
							System.out.println("89");
							System.out.flush();

						} else if (curr_index == s.length() - 1) {
							continue;

						} else if (transition.getKey().equals(s.charAt(curr_index + 1))) {
							Integer next_state = (Integer) transition.getValue();
							Integer[] next = { next_state, curr_index + 1 };
							synchronized (Task.class) {

//									Task.class.notify();

								queue.add(next);
							}
						}

					}
					break;
				}

			}
			synchronized (Task.class) {
				completed++;
				Task.class.notify();
				return;
			}

		} catch (Exception e) {
			// Throwing an exception
			System.out.println("Exception is caught");
		}
	}
}
