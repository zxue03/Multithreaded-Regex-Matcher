import java.util.*;
public class Match {
	public static void main(String[] args) {
		if (args.length != 2) {
			System.out.println("Usage: java Match [regex] [string]");
			System.out.println("       Put regex in quotes to avoid shell parsing weirdness");
			return;
		}

		Parser p = new Parser(args[0]);
		Regex r = p.parse();
		NFA nfa = new NFA(r);
//		System.out.println(nfa.states());
//		System.out.println(nfa.transitions);
//		System.out.println(nfa.next_state_count);
//		System.out.println(nfa.final_states());
//		List<Map.Entry<Character, Object>> l = nfa.transition(0);
//		for (Map.Entry<Character, Object> i : l) {
//			System.out.println(i.getKey() + " " + i.getValue());
//		}
		 nfa = new NFA(new Parser("(((a|b|c|d|e|f|g)*z)*)*").parse());
	        System.out.println(nfa.match("aadz",1));
	        System.out.println(nfa.match("ab",12));
	        System.out.println(nfa.match("z",12));
	        System.out.println(nfa.match("abcdzfz",12));
	        

//	        assertFalse(nfa.match("abcdzf",12));
//	        assertFalse(nfa.match("abcdefgabcdefgabcdefgabcdefgabcdefgabcdefgabcdefgabcdefgabcdefgabcdefgabcdefgabcdefgabcdefgabcdefgabcdefgabcdefgabcdefgabcdefgabcdefgabcdefgabcdefg",1));
//	        assertFalse(nfa.match("aaaaaaaaaaaaabaaaaaaaaaaaaaa",1));
//		if (nfa.match(args[1], 12)) {
//			System.out.println("yes");
//		} else {
//			System.out.println("no");
//		}
	}
}
