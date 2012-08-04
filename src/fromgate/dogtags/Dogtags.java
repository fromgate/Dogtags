/*  
 *  Dogtags, Minecraft bukkit plugin
 *  (c)2012, fromgate, fromgate@gmail.com
 *  http://dev.bukkit.org/server-mods/dogtags/
 *    
 *  This file is part of Dogtags.
 *  
 *  Dogtags is free software: you can redistribute it and/or modify
 *	it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Dogtags is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with WeatherMan.  If not, see <http://www.gnu.org/licenses/>.
 * 
 */


package fromgate.dogtags;

import java.awt.Image;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Random;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.EntityEffect;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.map.MapRenderer;
import org.bukkit.map.MapView;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;



public class Dogtags extends JavaPlugin {

	/* Пермишены:
	 * dogtags.claim 		- Дает возможность срезать жетоны. Шанс обычный (минимальный)
	 *                        + возможность на шанс критического удара обычным оружием 
	 * dogtags.knife 		- Дает возможность использовать "кинжалЪ". Шанс повышенный
	 *                        + возможность на шанс критического удара обычным оружием
	 * dogtags.id 			- Дает возможность уточнить id своего dogtag'а
	 * dogtags.id.players	- Дает возможность уточнить id жетона любого другого игрока.
	 * dogtags.config
	 * dogtags.backstab     - Дает возможность наносить удар сзади
	 * dogtags.harakiri		- Дает возможность делать харакири
	 * 
	 */

	//конфигурация
	HashMap<String,Short> dtags = new HashMap<String,Short>();
	String knife = "359";
	int knife_chance = 100;
	int knife_crit_chance = 50;
	int knife_crit_dmg = 10;

	String weapon ="267,268,272,276";
	int weapon_chance = 5;
	int weapon_crit_chance = 10;
	int weapon_crit_dmg = 3;

	boolean backstab_crit_only = true;
	boolean backstab_sneak_only = true;
	int backstab_angle=45;

	boolean harakiri = true;
	int harakiri_dogtag_chance = 50;
	String harakiri_weapon = "359";
	int harakiri_cooldown = 30;  // кулдаун в секундах
	int harakiri_try_delay = 1000; // время между попытками
	int harakiri_clicks = 4;


	HashMap<String,Long> harakiri_time = new HashMap<String,Long>();
	//HashMap<String,Long> harakiri_try = new HashMap<String,Long>();

	HashMap<String,Try> harakiri_tries = new HashMap<String,Try>();
	class Try{
		Long time;
		int tries;
		Try(Long time){
			this.time = time; //System.currentTimeMillis();
			this.tries = 1;
		}
	}

	String language = "english";
	boolean vcheck = true;
	boolean savelng = false;

	Image dtimg; //подложка
	DTUtil u;	


	//разные переменные
	Logger log = Logger.getLogger("Minecraft");
	PluginDescriptionFile des;
	private DTCmd cmd;
	private DTListener l;
	String px = ChatColor.AQUA+"[dxp] "+ChatColor.WHITE;
	Random random = new Random ();

	public boolean allowHarakiri(Player p){
		String name = p.getName();
		boolean ah = (p.hasPermission("dogtags.harakiri")&&
				p.isSneaking()&&
				(!p.isSprinting())&&
				(p.getItemInHand() !=null)&&
				(u.isIdInList(p.getItemInHand().getTypeId(), harakiri_weapon))&&
				((!harakiri_time.containsKey(name)) || 
						(harakiri_time.containsKey(name)&&((System.currentTimeMillis()-harakiri_time.get(name))>harakiri_cooldown*1000))));

		if (ah) {
			p.playEffect(EntityEffect.HURT);
			p.addPotionEffect(new PotionEffect(PotionEffectType.CONFUSION,150,2));
		}
		return ah;
	}


	public int tryHarakiri (Player p){
		String pn = p.getName();
		Long time = System.currentTimeMillis();
		if (harakiri_tries.containsKey(pn)&&((time-harakiri_tries.get(pn).time)<harakiri_try_delay)){
			harakiri_tries.get(pn).tries++;
		} else harakiri_tries.put(pn, new Try(time));

		return harakiri_tries.get(pn).tries;
	}

	@Override
	public void onDisable() {
		SaveDogtags();
		dtags.clear();
		dtimg.flush();



	}

	@Override
	public void onEnable() {

		LoadCfg();
		SaveCfg();

		u = new DTUtil (this, vcheck, savelng, language,"dotgags","Dogtags","dogtag",ChatColor.DARK_AQUA+"[DT] "+ ChatColor.WHITE);

		cmd = new DTCmd (this);
		getCommand("dogtag").setExecutor(cmd);
		des = getDescription();
		l = new DTListener (this);
		PluginManager pm = this.getServer().getPluginManager();
		pm.registerEvents(l, this);
		LoadDTImg();
		LoadDogtags();
		log.info("[Dogtags] Loaded "+Integer.toString(dtags.size())+ " dogtags...");

		try {
			MetricsLite metrics = new MetricsLite(this);
			metrics.start();
		} catch (IOException e) {
			log.info("[Dogtags] failed to submit stats to the Metrics (mcstats.org)");
		}

	}

	private void LoadDTImg(){
		try {
			dtimg = ImageIO.read(this.getClass().getResourceAsStream("dogtag.png"));
		} catch (IOException e) {
			e.printStackTrace();
			log.info("[DT] Error loading dogtag image");
		} 
	}

	public void updateMap(short id, String pname){
		MapView map = Bukkit.getServer().getMap(id);
		DTRenderer mr = new DTRenderer (this, pname);
		mr.initialize(map);
		for (MapRenderer r : map.getRenderers()) map.removeRenderer(r);
		map.addRenderer(mr);
	}

	public short createMap(Player p){
		MapView map = Bukkit.getServer().createMap(p.getWorld());
		map.setCenterX(Integer.MAX_VALUE);
		map.setCenterZ(Integer.MAX_VALUE);
		DTRenderer mr = new DTRenderer (this, p.getName());
		mr.initialize(map);
		map.getRenderers().clear();
		map.addRenderer(mr);
		p.sendMap(map);
		return map.getId();
	}


	public void LoadDogtags(){
		dtags.clear();
		try{
			File f = new File (getDataFolder()+File.separator+"dogtags.yml");
			if (f.exists()){
				YamlConfiguration dtl = new YamlConfiguration();
				dtl.load(f);
				for (String pn : dtl.getKeys(false)){
					dtags.put(pn, Short.parseShort(dtl.getString(pn)));
					updateMap(dtags.get(pn), pn);
				}
			}
		} catch (Exception e){
			e.printStackTrace();
		}

	}

	public void SaveDogtags(){
		if (dtags.size()>0){
			try{
				if (!getDataFolder().exists()) getDataFolder().mkdir();
				File f = new File (getDataFolder()+File.separator+"dogtags.yml");
				if (!f.exists()) f.createNewFile();
				YamlConfiguration dtl = new YamlConfiguration();
				for (String pn : dtags.keySet()){
					dtl.set(pn, Short.toString(dtags.get(pn)));
				}
				dtl.save(f);
			} catch (Exception e){
				e.printStackTrace();
			}
		}
	}

	public void LoadCfg(){
		language = getConfig().getString("general.language","english");
		vcheck = getConfig().getBoolean("general.check-updates",true);
		savelng = getConfig().getBoolean("general.language-save",false); // добавлять руками в конфиг
		backstab_crit_only=getConfig().getBoolean("backstabs.enable",true); 
		backstab_sneak_only=getConfig().getBoolean("backstabs.require-sneak",true);
		backstab_angle=getConfig().getInt("backstabs.angle",45);
		knife = getConfig().getString("knife.item","359");
		knife_chance = getConfig().getInt("knife.dogtag-chance", 90);
		knife_crit_chance = getConfig().getInt("knife.crit-chance", 50);
		knife_crit_dmg = getConfig().getInt("knife.crit-damage", 8);
		weapon = getConfig().getString("weapon.item","267,268,272,276");
		weapon_chance = getConfig().getInt("weapon.dogtag-chance", 5);
		weapon_crit_chance = getConfig().getInt("weapon.crit-chance", 5);
		weapon_crit_dmg = getConfig().getInt("weapon.crit-damage", 4);
		harakiri=getConfig().getBoolean("harakiri.enable",true);
		harakiri_dogtag_chance=getConfig().getInt("harakiri.dogtag-chance", 0);
		harakiri_weapon=getConfig().getString("harakiri.weapon","359");
		harakiri_cooldown=getConfig().getInt("harakiri.cooldown",600);
		harakiri_try_delay=getConfig().getInt("harakiri.click-delay",1000);
		harakiri_clicks=getConfig().getInt("harakiri.click-count",5);
	}

	public void SaveCfg(){
		getConfig().set("general.language",language);
		getConfig().set("general.check-updates",vcheck);
		getConfig().set("backstabs.enable",backstab_crit_only);
		getConfig().set("backstabs.require-sneak",backstab_sneak_only);
		getConfig().set("backstabs.angle",backstab_angle);
		getConfig().set("knife.item",knife);
		getConfig().set("knife.dogtag-chance", knife_chance);
		getConfig().set("knife.crit-chance", knife_crit_chance);
		getConfig().set("knife.crit-damage", knife_crit_dmg);
		getConfig().set("weapon.item",weapon);
		getConfig().set("weapon.dogtag-chance", weapon_chance);
		getConfig().set("weapon.crit-chance", weapon_crit_chance);
		getConfig().set("weapon.crit-damage", weapon_crit_dmg);
		getConfig().set("harakiri.enable", harakiri);
		getConfig().set("harakiri.dogtag-chance", harakiri_dogtag_chance);
		getConfig().set("harakiri.weapon",harakiri_weapon);
		getConfig().set("harakiri.cooldown",harakiri_cooldown);
		getConfig().set("harakiri.click-delay",harakiri_try_delay);
		getConfig().set("harakiri.click-count",harakiri_clicks);
		saveConfig();
	}

	/*
	public void sendMaps (final Player p){
		Bukkit.getScheduler().scheduleAsyncDelayedTask(this, new Runnable() {
			public void run() {
				for (String pn : dtags.keySet())			
					p.sendMap(Bukkit.getMap(dtags.get(pn)));
			}
		}, 30L);
	}*/

	public void AddDogtag (Player p){
		if (!dtags.containsKey(p.getName())) {
			dtags.put(p.getName(), createMap(p));
			SaveDogtags();
		}
	}

}
