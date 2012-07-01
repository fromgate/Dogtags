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


import java.io.File;
import java.net.URL;
import java.util.HashMap;
import java.util.Random;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilderFactory;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginDescriptionFile;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/* +    1. Проверка версий
 * +	2. Процедуры для обработчика комманда (перечень, печать хелпа -)
 */


public class FGUtil {
	Dogtags plg;
	
	
	//конфигурация утилит
	public String px = ChatColor.translateAlternateColorCodes('&', "&3[DT]&f ");
	String pxlog = "[DT] "; // префикс для лог файла
	
	private String permprefix="dogtags.";
	private boolean version_check = false; // включить после заливки на девбукит
	private String version_check_url = "http://dev.bukkit.org/server-mods/dogtags/files.rss";
	private String version_name = "Dogtags"; // идентификатор на девбукките (всегда должен быть такой!!!)
	private String version_info_perm = permprefix+"config"; // кого оповещать об обнволениях
	private String language="english";
	
	// Сообщения+перевод
	private HashMap<String,String> msg = new HashMap<String,String>(); //массив сообщений
	private char c1 = 'a'; //цвет 1 (по умолчанию для текста)
	private char c2 = '2'; //цвет 2 (по умолчанию для значений)
	private String msglist ="";
	
	private HashMap<String,Cmd> cmds = new HashMap<String,Cmd>();
	
	private String cmdlist ="";
	PluginDescriptionFile des;
	private double version_current=0;
	private double version_new=0;
	private String version_new_str="unknown";
	private Logger log = Logger.getLogger("Minecraft");
	Random random = new Random ();
	
	
	
	public FGUtil(Dogtags plg, boolean vcheck, boolean savelng, String language){
		this.plg = plg;
		this.des = plg.getDescription();
		this.version_current = Double.parseDouble(des.getVersion().replaceFirst("\\.", "").replace("/", ""));
		this.version_check=vcheck;
		this.language = language;
		this.LoadMSG();
		if (savelng) this.SaveMSG(); //для получения списка
		this.InitCmd();
	}

	
	/*
	 * Проверка версии 
	 */
	public void SetVersionCheck (boolean vc){
		this.version_check = vc;
	}
	
	
	// Вставить вызов в обработчик PlayerJoinEvent
	public void UpdateMsg (Player p){
		if (version_check){
			if (p.hasPermission(this.version_info_perm)){
				version_new = getNewVersion (version_current);
				if (version_new>version_current){
					PrintMSG(p, "msg_outdated","&6"+des.getName()+" v"+plg.des.getVersion(),'e','6');
					PrintMSG(p,"msg_pleasedownload",version_new_str,'e','6');
					PrintMsg(p, "&3"+version_check_url.replace("files.rss", ""));
				}
			}
		}
	}

	// Вставить в обработчик onEnable
	public void UpdateMsg (){
		if (version_check){
			version_new = getNewVersion (version_current);
			if (version_new>version_current){
				log.info(pxlog+des.getName()+" v"+des.getVersion()+" is outdated! Recommended version is v"+version_new_str);
				log.info(pxlog+version_check_url.replace("files.rss", ""));
			}			
		}
	}
	
	private double getNewVersion(double currentVersion){
		if (version_check){
			try {
				URL url = new URL(version_check_url);
				Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(url.openConnection().getInputStream());
				doc.getDocumentElement().normalize();
				NodeList nodes = doc.getElementsByTagName("item");
				Node firstNode = nodes.item(0);
				if (firstNode.getNodeType() == 1) {
					Element firstElement = (Element)firstNode;
					NodeList firstElementTagName = firstElement.getElementsByTagName("title");
					Element firstNameElement = (Element)firstElementTagName.item(0);
					NodeList firstNodes = firstNameElement.getChildNodes();
					version_new_str = firstNodes.item(0).getNodeValue().replace(version_name+" v", "").trim();
					return Double.parseDouble(version_new_str.replaceFirst("\\.", "").replace("/", ""));
				}
			}
			catch (Exception e) {
			}
		}
		return currentVersion;
	}
	
	
	public boolean isIdInList (int id, String str){
		String [] ln = str.split(",");
		if (ln.length>0) {
			for (int i = 0; i<ln.length; i++)
				if (Integer.parseInt(ln[i])==id) return true;
		}		
		return false;
	}

	
	
	/*
	 * Процедуры для обработчика комманд
	 * 
	 */
	
	public void AddCmd (String cmd, String perm, String desc){
		cmds.put(cmd, new Cmd(this.permprefix+perm,desc));
		if (cmdlist.isEmpty()) cmdlist = cmd;
		else cmdlist = cmdlist+", "+cmd;
	}
	
	public void InitCmd(){
		cmds.clear();
		cmdlist = "";

		AddCmd("help", "config",MSG("hlp_helpcmd","/dogtag help"));
		AddCmd("cfg", "config",MSG("hlp_cfg","/dogtag cfg"));
		AddCmd("reload", "config",MSG("hlp_reload","/dogtag reload"));
		
	}
	
	
	public boolean CheckCmdPerm (Player p, String cmd){
		return ((cmds.containsKey(cmd.toLowerCase()))&&
				(cmds.get(cmd.toLowerCase()).perm.isEmpty()||((!cmds.get(cmd.toLowerCase()).perm.isEmpty())&&p.hasPermission(cmds.get(cmd.toLowerCase()).perm))));
	}
	
	public String getCmdList(){
		return cmdlist;
	}
	
	public boolean equalCmdPerm (String cmd, String perm){
		return (cmds.containsKey(cmd.toLowerCase())&&cmds.get(cmd.toLowerCase()).perm.equalsIgnoreCase(perm));
	}
	
	public class Cmd {
		String perm;
		String desc;
		public Cmd (String perm, String desc){
			this.perm = perm;
			this.desc = desc;
		}
	} 
	
	
	/*
	 * Разные полезные процедурки 
	 * 
	 */
	public void PrintMsg(Player p, String msg){
		p.sendMessage(ChatColor.translateAlternateColorCodes('&', msg));
	}
	
	public void PrintMsgPX(Player p, String msg){
		p.sendMessage(ChatColor.translateAlternateColorCodes('&', px+msg));
	}


	public void BC (String msg){
		plg.getServer().broadcastMessage(ChatColor.translateAlternateColorCodes('&', px+msg));
	}
	
	
	/*
	 * Перевод
	 * 
	 */
	public void LoadMSG(){
		String lngfile = this.language+".lng";
		try {
			YamlConfiguration lng = new YamlConfiguration();
			File f = new File (plg.getDataFolder()+File.separator+lngfile);
			if (f.exists()) lng.load(f);
			
			FillMSG(lng);
		} catch (Exception e){
			e.printStackTrace();
		}
	}
	
	
	public void addMSG(YamlConfiguration cfg,String key, String txt){
		msg.put(key, ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&', cfg.getString(key,txt))));
		if (msglist.isEmpty()) msglist=key;
		else msglist=msglist+","+key;
	}

	
	///// процедура для формирования файла
	public void SaveMSG(){
		String lngfile = this.language+".lng";
		String [] keys = msglist.split(",");
		try {
			File f = new File (plg.getDataFolder()+File.separator+lngfile);
			if (!f.exists()) f.createNewFile();
			YamlConfiguration cfg = new YamlConfiguration();
			for (int i = 0; i<keys.length;i++)
				cfg.set(keys[i], msg.get(keys[i]));
			cfg.save(f);
		} catch (Exception e){
			e.printStackTrace();
		}
	} 
	
	public String MSG(String id){
		return MSG (id,"",this.c1, this.c2);
	}
	
	public String MSG(String id, char c){
		return MSG (id,"",c, this.c2);
	}

	public String MSG(String id, String keys){
		return MSG (id,keys,this.c1, this.c2);
	}

	public String MSG(String id, String keys, char c){
		return MSG (id,keys,this.c1, c);
	}

	public String MSG(String id, String keys, char c1, char c2){
		String str = "&4Unknown message ("+id+")";
		if (msg.containsKey(id)){
			str = "&"+c1+msg.get(id);
			String ln[] = keys.split(";");
			if (ln.length>0)
				for (int i = 0; i<ln.length;i++)
					str = str.replace("%"+Integer.toString(i+1)+"%", "&"+c2+ln[i]+"&"+c1);
		} 
		return ChatColor.translateAlternateColorCodes('&', str);
	}
	
	
	public void PrintMSG (Player p, String msg_key, int key){
		p.sendMessage(MSG (msg_key, Integer.toString(key), this.c1, this.c2));
	}
	
	public void PrintMSG (Player p, String msg_key, String keys){
		p.sendMessage(MSG (msg_key, keys, this.c1, this.c2));
	}

	public void PrintMSG (Player p, String msg_key, String keys, char c1, char c2){
		p.sendMessage(MSG (msg_key, keys, c1, c2));
	}

	public void PrintMSG (Player p, String msg_key, char c1){
		p.sendMessage(MSG (msg_key, c1));
	}

	public void PrintMSG (Player p, String msg_key){
		p.sendMessage(MSG (msg_key));
	}
	
	public void PrintHLP (Player p){
		PrintMsg(p, "&6&l"+version_name+" v"+des.getVersion()+" &r&6| "+MSG("hlp_help",'6'));
		PrintMSG(p, "cmd_dogtag","/dogtag ["+MSG("hlp_cmdparam_player",'2')+"]");
		PrintMSG(p, "hlp_helpcmd","/dogtag help");
		PrintMSG(p, "hlp_helpexec","/dogtag <"+MSG("hlp_cmdparam_command",'2')+"> ["+MSG("hlp_cmdparam_parameter",'2')+"]");
		PrintMSG(p, "hlp_helpcmdlist","/dogtag help <"+MSG("hlp_cmdparam_command",'2')+">");
		PrintMsg(p, MSG("hlp_commands")+" &2"+getCmdList());
	}
	
	
	public void PrintCfg (Player p){
		PrintMsg (p, "&6&l"+version_name+" v"+des.getVersion()+" &r&6| "+MSG("cfg_configuration",'6'));
		PrintMSG (p,"cfg_dogtags",Integer.toString(plg.dtags.size()));
		PrintMSG (p,"cfg_knifeweapon",'e');
		PrintMSG (p,"cfg_kitem",plg.knife);
		PrintMSG (p,"cfg_witem",plg.weapon);
		PrintMSG (p,"cfg_chance",plg.knife_chance+";"+plg.weapon_chance);
		PrintMSG (p,"cfg_crit",plg.knife_crit_chance+";"+plg.weapon_chance+";"+plg.knife_crit_dmg+";"+plg.weapon_crit_dmg);
		PrintEnDis (p,"cfg_bscfg",plg.backstab_crit_only);
		PrintMSG (p,"cfg_bssneakangle",EnDis(plg.backstab_crit_only)+";"+plg.backstab_angle);
		PrintEnDis (p,"cfg_hcfg",plg.harakiri);
		PrintMSG (p,"cfg_hchitem",plg.harakiri_dogtag_chance+";"+plg.harakiri_weapon);
		PrintMSG (p,"cfg_hcdclick",plg.harakiri_cooldown+";"+plg.harakiri_clicks+";"+plg.harakiri_try_delay);
		PrintMSG (p,"cfg_lngvers",plg.language+";"+plg.vcheck);
		p.sendMessage("");
		PrintMSG (p,"cfg_cmdmsg","/dogtag cfg <parameter> <value>",'7','8');
		PrintMSG (p,"cfg_parameters","kitem, kchance, kcritch, kcritdmg, witem, wchance, wcritch, wcritdmg, backstabs, bsneak, bangle, harakiri, hdtchance, hitem, hcooldown, hclicks, hlickdelay",'7','8');
	}
	
	
	public void PrintHLP (Player p, String cmd){
		if (cmds.containsKey(cmd)){
			PrintMsg(p, "&6&l"+version_name+" v"+plg.des.getVersion()+" &r&6| "+MSG("hlp_help",'6'));
			PrintMsg(p, cmds.get(cmd).desc);
		} else PrintMSG(p,"cmd_unknown",cmd);
	}
	
	public String EnDis (boolean b){
		if (b) return MSG ("enabled");
		else return MSG ("disabled");
	}

	public void PrintEnDis (Player p, String msg_id, boolean b){
		p.sendMessage(MSG (msg_id)+": "+EnDis(b));
	}
	
	
	public void FillMSG(YamlConfiguration cfg){
		msg.clear();
		msglist="";
		addMSG (cfg, "disabled","disabled");
		msg.put("disabled", "&c"+msg.get("disabled"));
		addMSG (cfg, "enabled","enabled");
		msg.put("enabled", "&2"+msg.get("enabled"));
		addMSG (cfg, "cmd_unknown", "Unknown command: %1%");
		addMSG (cfg, "cmd_dogtag", "%1% - display map number of player's dogtag");
		addMSG (cfg, "hlp_help", "Help");
		addMSG (cfg, "hlp_helpexec", "%1% - execute command ");
		addMSG (cfg, "hlp_helpcmdlist", "%1% - to get additional help");
		addMSG (cfg, "hlp_commands", "Commands:");
		addMSG (cfg, "hlp_cmdparam_command", "command");
		addMSG (cfg, "hlp_cmdparam_parameter", "parameter");
		addMSG (cfg, "hlp_cmdparam_player", "player");
		addMSG (cfg, "msg_id", "Your dogtag id is %1%");
		addMSG (cfg, "msg_id_player", "%1%'s dogtag id is %2%");
		addMSG (cfg, "msg_id_unknown", "No any dogtags records for player %1%");
		addMSG (cfg, "msg_no_permissions", "You have not enought permissions to execute this command");
		addMSG (cfg, "msg_death", "%1% killed %2% and claimed his dogtags");
		addMSG (cfg, "msg_crit_deal", "You deal a critical damage to %1%!");
		addMSG (cfg, "msg_crit_receive", "You received a critical damage from %1%!");
		addMSG (cfg, "msg_reloadcfg", "Configuration reloaded...");
		addMSG (cfg, "hlp_helpcmd", "%1% - show this help page");
		addMSG (cfg, "hlp_cfg", "%1% - display current configuration");
		addMSG (cfg, "hlp_reload", "%1% - reload configuration from file");
		addMSG (cfg, "cfg_configuration", "Configuration");
		addMSG (cfg, "cfg_dogtags", "Dogtags created: %1%");
		addMSG (cfg, "cfg_knifeweapon", "Knife/Regular weapon configuration");
		addMSG (cfg, "cfg_kitem", "Knife items: %1%");
		addMSG (cfg, "cfg_witem", "Regualar weapon items: %1%");
		addMSG (cfg, "cfg_chance", "Chance to claim dogtags (knife/weapon): %1%% / %2%%");
		addMSG (cfg, "cfg_crit", "Criticals. Chance: %1%% / %2%%; Damage: %3% / %4%");
		addMSG (cfg, "cfg_bscfg", "Backstabs: %1%");
		addMSG (cfg, "cfg_bssneakangle", "Require sneak: %1% Andgle: %2%");
		addMSG (cfg, "cfg_hcfg", "Harakiri: %1%");
		addMSG (cfg, "cfg_hchitem", "Drop dogtag chance: %1%% Weapon items: %2%");
		addMSG (cfg, "cfg_hcdclick", "Cooldown: %1% sec. Clicks required: %2% Click delay: %3% ms.");
		addMSG (cfg, "cfg_lngvers", "Language: %1% Check updates: %2%");
		addMSG (cfg, "cfg_cmdmsg", "Use %1% to configure plugin");
		addMSG (cfg, "cfg_parameters", "Parameter list: %1%");
		addMSG (cfg, "cfg_kchance", "Chance to claim dogtags with knife: %1%");
		addMSG (cfg, "cfg_kcritch", "Chance to deal critical damage with knife: %1%");
		addMSG (cfg, "cfg_kcritdmg", "Amount of additional damage for knife-criticals: %1%");
		addMSG (cfg, "cfg_wchance", "Chance to claim dogtags with regualar weapon: %1%");
		addMSG (cfg, "cfg_wcritch", "Chance to deal critical damage with regualar weapon: %1%");
		addMSG (cfg, "cfg_wcritdmg", "Amount of additional damage for regualar criticals: %1%");
		addMSG (cfg, "msg_bstab_deal", "You backstabbed %1%!");
		addMSG (cfg, "msg_bstab_receive", "%1% backstabbed you!");
		addMSG (cfg, "msg_harakiri", "%1% made a harakiri...");
		addMSG (cfg, "cfg_backstabs", "Backstabs (only backstabs will deal critical damage): %1%");
		addMSG (cfg, "cfg_bsneak", "Backstabs in sneak mode only: %1%");
		addMSG (cfg, "cfg_bangle", "Players direction angle for backstabs was set to: %1%");
		addMSG (cfg, "cfg_harakiri", "Harakiries: %1%");
		addMSG (cfg, "cfg_hdtchance", "Chance to drop dogtags after harakiri was set to: 4%1%");
		addMSG (cfg, "cfg_hitem", "Harakiri weapon items was set to: %1%");
		addMSG (cfg, "cfg_hcooldown", "Harakiri cooldown time was set to %1% seconds");
		addMSG (cfg, "cfg_hclicks", "Clicks required to made a harakiri: %1%");
		addMSG (cfg, "cfg_hclickdelay", "Delay between right clicks required to harakiri: %1%");
		//addMSG (cfg, "cfg_", "%1%");
	}

}


