package fromgate.dogtags;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

public class DTRgDisable {
	static Set<String> drgs = new HashSet<String>();
	static WorldGuardPlugin worldguard; 
	static boolean wg_enable = false;
	private static JavaPlugin plugin;

	public static void init(JavaPlugin plg){
		plugin = plg;
		wg_enable = connectWorldGuard();
		load(plugin);
	}

	public static boolean connectWorldGuard(){
		Plugin worldGuard = Bukkit.getServer().getPluginManager().getPlugin("WorldGuard");
		if ((worldGuard != null)&&(worldGuard instanceof WorldGuardPlugin)) {
			worldguard = (WorldGuardPlugin)worldGuard;
			return true;
		}
		return false;
	}


	public static boolean isWgRegion(String rg){
		if (!wg_enable) return false;
		for (World w : Bukkit.getWorlds()){
			ProtectedRegion region = worldguard.getRegionManager(w).getRegion(rg);
			if (region != null) return true;
		}
		return false;
	}

	public static boolean isDogatgsDisabledRegion(Location loc){
		if (!wg_enable) return false;
		ApplicableRegionSet rset = worldguard.getRegionManager(loc.getWorld()).getApplicableRegions(loc);
		if ((rset == null)||(rset.size() == 0)) return false;
		for (ProtectedRegion rg : rset )
			if (drgs.contains(rg.getId())) return true;
		return false;
	}

	
	public static boolean toogleRegions(String rg){
		if (!drgs.contains(rg)) return add(rg);
		else drgs.remove(rg);
		return false;
	}
	
	public static boolean isDisReg(String rg){
		return drgs.contains(rg);
	}
	
		
	public static boolean add(String rg){
		if (!isWgRegion(rg)) return false;
		drgs.add(rg);
		save(plugin);
		return true;
	}

	public static int add(List<String> rgs){
		int count = 0;
		for (String rg : rgs)
			if (add (rg)) count++;
		save(plugin);
		return count;
	}

	public static boolean remove(String rg){
		if (!drgs.contains(rg)) return false;
		drgs.remove(rg);
		save(plugin);
		return true; 
	}
	
	public static void load(JavaPlugin plg){
		plg.reloadConfig();
		List<String> lst = plg.getConfig().getStringList("disable-regions");
		drgs.clear();
		drgs.addAll(lst);
	}
	
	public static void save(JavaPlugin plg){
		plg.getConfig().set("disable-regions", null);
		List<String> lst = new ArrayList<String>();
		lst.addAll(drgs);
		plg.getConfig().set("disable-regions", lst);
		plg.saveConfig();
	}
	
	public static String getRegions(){
		if (drgs.isEmpty()) return "";
		StringBuilder buff = new StringBuilder();
		int i=0;
		for (String rg : drgs) {
			buff.append(rg);
			i++;
			if (i<drgs.size()) buff.append(", ");
		}
		return buff.toString();
	}
	
	public static boolean isEmpty(){
		return (drgs.size()>0);
	}
	

}
