package fromgate.dogtags;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.bukkit.entity.Player;

public class DTRenderHistory {

	HashMap<String, Set<Integer>> rh = new HashMap<String, Set<Integer>>(); 


	public void clearHistory (Player p){
		if (rh.containsKey(p.getName())) rh.remove(p.getName());
	}

	public boolean isRendered(Player p, int map_id){
		String pn = p.getName();
		if (rh.containsKey(pn)){
			if (rh.get(pn).contains(map_id)) return true;
		} else rh.put(pn, new HashSet<Integer>());
		rh.get(pn).add(map_id);
		return false;		
	}


}
