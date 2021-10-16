import java.util.*;
import java.util.concurrent.*;

public class NFA {

	public int next_state_count = 0;
	public Integer start_state;
	public List<Integer> final_states = new ArrayList<>();
	public List<Integer> states = new ArrayList<>();
	public Map<Integer, Map<Character, List<Integer>>> transitions = new HashMap<>();

	NFA() {
		start_state = (Integer) newState();
	}

	Object newState() {
		Integer new_state = Integer.valueOf(next_state_count);
		states.add(new_state);
		next_state_count++;
		return new_state;
	}

	void newTransition(Object start, char c, Object end) {
		if (!states.contains(start)) {
			throw new UnsupportedOperationException();
		}
		Map<Character, List<Integer>> start_transitions = transitions.get(start);
		List<Integer> c_states;
		if (start_transitions != null) {
			c_states = start_transitions.get(c);
			if (c_states != null) {
				c_states.add((Integer) end);
				return;
			}
			c_states = new ArrayList<>();
			c_states.add((Integer) end);

			start_transitions.put(c, c_states);
			return;
		}
		c_states = new ArrayList<>();
		c_states.add((Integer) end);
		start_transitions = new HashMap<>();
		start_transitions.put(c, c_states);
		transitions.put((Integer) start, start_transitions);
	}

	void makeFinal(Object s) {
		if (!states.contains(s)) {
			throw new UnsupportedOperationException();
		}
		final_states.add((Integer) s);
	}

	NFA(Regex re) {
		NFA built_result = NFABuilder(re);
		this.next_state_count = built_result.next_state_count;
		this.start_state = built_result.start_state;
		this.final_states = built_result.final_states;
		this.states = built_result.states;
		this.transitions = built_result.transitions;

	}

	public static NFA NFABuilder(Regex re) {
		if (re.getClass() == RChar.class) {
			return character(((RChar) re).c);
		} else if (re.getClass() == RSeq.class) {
			NFA E = NFABuilder(((RSeq) re).left);
			NFA F = NFABuilder(((RSeq) re).right);
			return sequence(E, F);
		} else if (re.getClass() == ROr.class) {
			NFA E = NFABuilder(((ROr) re).left);
			NFA F = NFABuilder(((ROr) re).right);
			return disjunction(E, F);
		} else if (re.getClass() == RStar.class) {
			NFA E = NFABuilder(((RStar) re).re);
			return star(E);
		}

		return null;
	}

	public static NFA character(char c) {
		NFA result = new NFA();
		Integer final_state = (Integer) result.newState();
		result.makeFinal(final_state);
		result.newTransition(result.start_state, c, final_state);
		return result;
	}

	public static NFA sequence(NFA E, NFA F) {
		updateStates(F, E.next_state_count);
		List<Integer> states_from = new ArrayList<>(E.final_states);
		E.final_states = F.final_states;
		E.states.addAll(F.states);
		E.next_state_count = F.next_state_count;
		E.transitions.putAll(F.transitions);
		for (Integer state_from : states_from) {
			E.newTransition(state_from, '#', F.start_state);
		}
		return E;
	}

	public static NFA disjunction(NFA E, NFA F) {
		NFA nfa = new NFA();
		updateStates(E, nfa.next_state_count);
		nfa.final_states.addAll(E.final_states);
		nfa.states.addAll(E.states);
		nfa.next_state_count = E.next_state_count;
		nfa.transitions.putAll(E.transitions);
		nfa.newTransition(nfa.start_state, '#', E.start_state);
		updateStates(F, nfa.next_state_count);
		nfa.final_states.addAll(F.final_states);
		nfa.states.addAll(F.states);
		nfa.next_state_count = F.next_state_count;
		nfa.transitions.putAll(F.transitions);
		nfa.newTransition(nfa.start_state, '#', F.start_state);
		return nfa;
	}

	public static NFA star(NFA E) {
		NFA nfa = new NFA();
		updateStates(E, nfa.next_state_count);
		nfa.states.addAll(E.states);
		nfa.next_state_count = E.next_state_count;
		nfa.transitions.putAll(E.transitions);
		nfa.newTransition(nfa.start_state, '#', E.start_state);
		nfa.makeFinal(nfa.start_state);

		for (Integer state_from : E.final_states) {
			nfa.newTransition(state_from, '#', nfa.start_state);
		}
		return nfa;
	}

	public static void updateStates(NFA nfa, Integer offset) {
		nfa.start_state += offset;
		nfa.next_state_count += offset;
		for (int i = 0; i < nfa.final_states.size(); i++) {
			nfa.final_states.set(i, nfa.final_states.get(i) + offset);
		}
		for (int j = 0; j < nfa.states.size(); j++) {
			nfa.states.set(j, nfa.states.get(j) + offset);
		}

		List<Integer> state_keys = new ArrayList<>(nfa.transitions.keySet());
		state_keys.sort(Collections.reverseOrder());
		for (Integer state_key : state_keys) {
			Map<Character, List<Integer>> state_transitions = nfa.transitions.get(state_key);
			for (Map.Entry<Character, List<Integer>> c_transitions : state_transitions.entrySet()) {
				for (int k = 0; k < c_transitions.getValue().size(); k++) {
					c_transitions.getValue().set(k, c_transitions.getValue().get(k) + offset);
				}
			}
			nfa.transitions.remove(state_key);
			nfa.transitions.put(state_key + offset, state_transitions);
		}

	}

	public List<Object> states() {
		return new ArrayList<Object>(states);
	}

	public Object start_state() {
		return start_state;
	}

	public List<Object> final_states() {
		return new ArrayList<Object>(final_states);
	}

	public List<Map.Entry<Character, Object>> transition(Object state) {
		if (!states.contains(state)) {
			throw new UnsupportedOperationException();
		}
		Map<Character, List<Integer>> state_transitions = transitions.get(state);
		if (state_transitions == null) {
			return new ArrayList<>();
		}
		List<Map.Entry<Character, Object>> result = new ArrayList<>();
		for (Map.Entry<Character, List<Integer>> c_transitions : state_transitions.entrySet()) {
			for (Integer c_state : c_transitions.getValue()) {
				Map.Entry<Character, Object> transition = new AbstractMap.SimpleEntry<>(c_transitions.getKey(),
						c_state);
				result.add(transition);
			}
		}
		return result;
	}

	boolean match(String s) {
//		Set<Integer> visited = new HashSet<>();
		LinkedList<Integer[]> queue = new LinkedList<>();
		Set<Integer[]> visited_e = new HashSet<>();
//		visited.add(start_state);
		Integer[] start = { start_state, -1 };
		queue.add(start);
		while (queue.size() != 0) {
			Integer[] curr = queue.poll();
			Integer curr_state = curr[0];
			Integer curr_index = curr[1];
			if (curr_index == (s.length() - 1) && final_states.contains(curr_state)) {
				return true;
			}
			List<Map.Entry<Character, Object>> possible_transitions = transition(curr_state);
			for (Map.Entry<Character, Object> transition : possible_transitions) {
				if (transition.getKey().equals('#')) {
					
					Integer next_state = (Integer) transition.getValue();
					Integer[] next = { next_state, curr_index };
					boolean repeated = false;
					for(Integer[] e : visited_e) {
						if(e[0].equals(next[0]) && e[1].equals(next[1])) {
							repeated = true;
						}
					}
					if(repeated) {
						continue;
					}
//						Task.class.notify();
					visited_e.add(curr);
					visited_e.add(next);
					queue.add(next);
				} else if (curr_index == s.length() - 1) {
					continue;
				} else if (transition.getKey().equals(s.charAt(curr_index + 1))) {
					Integer next_state = (Integer) transition.getValue();
					Integer[] next = { next_state, curr_index + 1 };
					queue.add(next);
				}

			}
		}
		return false;
	}

//	
	boolean match(String s, int nthreads) {
//		System.out.println(s);
		if (nthreads == 1) {
			return match(s);
		}

		int poolsize = nthreads - 1;
//		if(nthreads > 1) {
//			poolsize = nthreads - 1;
//		}
		ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(poolsize);
//		BlockingQueue<Integer[]> queue = new LinkedBlockingQueue<>(poolsize);
		Queue<Integer[]> queue = new LinkedList<>();
		Set<Integer[]> visited_e = new HashSet<>();
//		Set<Integer> visited = new HashSet<>();
//		visited.add(start_state);
		Integer[] start = { start_state, -1 };

		try {
			queue.add(start);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		List<Boolean> indicator = new ArrayList<>();
		int called = 0;
		while (true) {
			synchronized (Task.class) {

				if (!indicator.isEmpty()) {
					executor.shutdown();
//					try {
//						executor.awaitTermination(10, TimeUnit.MICROSECONDS);
//					} catch (InterruptedException e) {
//						// TODO Auto-generated catch block
//						e.printStackTrace();
//					}
					return true;
				}
				if (Task.completed == called && queue.isEmpty() && indicator.isEmpty()) {
					executor.shutdown();
//					try {
//						executor.awaitTermination(10, TimeUnit.MICROSECONDS);
//					} catch (InterruptedException e) {
//						// TODO Auto-generated catch block
//						e.printStackTrace();
//					}
					break;
				}
				int size = queue.size();
				for (int i = 0; i < size; i++) {
					try {
						called++;
						Integer[] candidate = queue.poll();
						Task new_task = new Task(candidate, queue, poolsize, s, this, indicator, visited_e);
						executor.execute(new_task);
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

				}
//				try {
//					Thread.sleep(2000);
//				} catch (InterruptedException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}

				try {
					Task.class.wait();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

		}

		return false;
	}
}
