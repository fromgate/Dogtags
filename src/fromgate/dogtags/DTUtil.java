package fromgate.dogtags;

import org.bukkit.entity.Player;

public class DTUtil extends FGUtilCore {

	Dogtags plg;
	String version_name;
	String plgcmd;

	public DTUtil (Dogtags plg, boolean vcheck, boolean savelng, String language, String devbukkitname, String version_name, String plgcmd, String px){
		super (plg, vcheck, savelng, language, devbukkitname, version_name, plgcmd, px);
		this.version_name = version_name;
		this.plgcmd = plgcmd;
		this.plg = plg;
		FillMSG();
		InitCmd();
		if (savelng) SaveMSG();
	}


	public void InitCmd(){
		this.cmds.clear();
		this.cmdlist = "";
		addCmd("help", "config","hlp_helpcmd","/dogtag help");
		//addCmd("cfg", "config",MSG("hlp_cfg","/dogtag cfg"));
		addCmd("head", "head","hlp_headcmd","/dogtag head <player name>");
		addCmd("rg", "config","hlp_disreg","/dogtag rg [region name]");
		addCmd("reload", "config","hlp_reload","/dogtag reload");
	}

	public void FillMSG(){

		addMSG ("disabled","disabled");
		msg.put("disabled", "&c"+msg.get("disabled"));
		addMSG ("enabled","enabled");
		msg.put("enabled", "&2"+msg.get("enabled"));
		addMSG ("cmd_unknown", "Unknown command: %1%");
		addMSG ("cmd_dogtag", "%1% - display map number of player's dogtag");
		addMSG ("hlp_help", "Help");
		addMSG ("hlp_helpexec", "%1% - execute command ");
		addMSG ("hlp_helpcmdlist", "%1% - to get additional help");
		addMSG ("hlp_commands", "Commands:");
		addMSG ("hlp_cmdparam_command", "command");
		addMSG ("hlp_cmdparam_parameter", "parameter");
		addMSG ("hlp_cmdparam_player", "player");
		addMSG ("msg_id", "Your dogtag id is %1%");
		addMSG ("msg_id_player", "%1%'s dogtag id is %2%");
		addMSG ("msg_id_unknown", "No any dogtags records for player %1%");
		addMSG ("msg_no_permissions", "You have not enought permissions to execute this command");
		addMSG ("msg_death", "%1% killed %2% and claimed his dogtags");
		addMSG ("msg_crit_deal", "You deal a critical damage to %1%!");
		addMSG ("msg_crit_receive", "You received a critical damage from %1%!");
		addMSG ("msg_reloadcfg", "Configuration reloaded...");
		addMSG ("hlp_helpcmd", "%1% - show this help page");
		addMSG ("hlp_cfg", "%1% - display current configuration");
		addMSG ("hlp_reload", "%1% - reload configuration from file");
		addMSG ("hlp_disreg", "%1% -  shows list (if region name skipped) or toggles regions state (disable/enable Dogtags in region)");
		addMSG ("cfg_configuration", "Configuration");
		addMSG ("cfg_dogtags", "Dogtags created: %1%");
		addMSG ("cfg_knifeweapon", "Knife/Regular weapon configuration");
		addMSG ("cfg_kitem", "Knife items: %1%");
		addMSG ("cfg_witem", "Regualar weapon items: %1%");
		addMSG ("cfg_chance", "Chance to claim dogtags (knife/weapon): %1%% / %2%%");
		addMSG ("cfg_crit", "Criticals. Chance: %1%% / %2%%; Damage: %3% / %4%");
		addMSG ("cfg_bscfg", "Backstabs: %1%");
		addMSG ("cfg_bssneakangle", "Require sneak: %1% Andgle: %2%");
		addMSG ("cfg_hcfg", "Harakiri: %1%");
		addMSG ("cfg_hchitem", "Drop dogtag chance: %1%% Weapon items: %2%");
		addMSG ("cfg_hcdclick", "Cooldown: %1% sec. Clicks required: %2% Click delay: %3% ms.");
		addMSG ("cfg_lngvers", "Language: %1% Check updates: %2%");
		addMSG ("cfg_cmdmsg", "Use %1% to configure plugin");
		addMSG ("cfg_parameters", "Parameter list: %1%");
		addMSG ("cfg_kchance", "Chance to claim dogtags with knife: %1%");
		addMSG ("cfg_kcritch", "Chance to deal critical damage with knife: %1%");
		addMSG ("cfg_kcritdmg", "Amount of additional damage for knife-criticals: %1%");
		addMSG ("cfg_wchance", "Chance to claim dogtags with regualar weapon: %1%");
		addMSG ("cfg_wcritch", "Chance to deal critical damage with regualar weapon: %1%");
		addMSG ("cfg_wcritdmg", "Amount of additional damage for regualar criticals: %1%");
		addMSG ("msg_bstab_deal", "You backstabbed %1%!");
		addMSG ("msg_bstab_receive", "%1% backstabbed you!");
		addMSG ("msg_harakiri", "%1% made a harakiri...");
		addMSG ("cfg_backstabs", "Backstabs (only backstabs will deal critical damage): %1%");
		addMSG ("cfg_bsneak", "Backstabs in sneak mode only: %1%");
		addMSG ("cfg_bangle", "Players direction angle for backstabs was set to: %1%");
		addMSG ("cfg_harakiri", "Harakiries: %1%");
		addMSG ("cfg_hdtchance", "Chance to drop dogtags after harakiri was set to: 4%1%");
		addMSG ("cfg_hitem", "Harakiri weapon items was set to: %1%");
		addMSG ("cfg_hcooldown", "Harakiri cooldown time was set to %1% seconds");
		addMSG ("cfg_hclicks", "Clicks required to made a harakiri: %1%");
		addMSG ("cfg_hclickdelay", "Delay between right clicks required to harakiri: %1%");
		addMSG ("msg_behead", "%1% beheaded %2%");		
		addMSG ("hlp_plgcmd", "%1% - show dogtag number (map id)");
		addMSG ("hlp_headcmd", "%1% - create a head with skin of defined player and drop it near you");
		addMSG ("msg_headcreated", "Head of %1% created!");
		addMSG ("msg_headof", "Head of %1%");
		addMSG ("msg_rglist", "List of Dogtags-free regions: %1%");
		addMSG ("msg_rglistempty", "List of Dogtags-free regions is empty");
		addMSG ("msg_rgremoved", "Region %1% excluded from the Dogtags-free region list");
		addMSG ("msg_rgadded", "Region %1% added to the Dogtags-free region list");
		addMSG ("msg_rgcantadd", "Cannot add region %1%. You must create it using WorldGuard first.");
	}

	public void PrintCfg(Player p){
		p.sendMessage("");
		printMsg(p, "&6&l" + des.getName() + " v" + this.des.getVersion() + " &r&6| " + getMSG("cfg_configuration", '6'));

		//PrintEnDis(p, "cfg_showmsg", plg.showmsg);
		//PrintMSG(p, "cfg_fallradius", plg.fall_random_radius);
		//PrintMSG(p, "cfg_language", plg.language);
		//PrintEnDis(p, "cfg_vcheck", plg.vcheck);
	}


	public void PrintCfgOld (Player p){
		printMsg (p, "&6&l"+des.getName()+" v"+des.getVersion()+" &r&6| "+getMSG("cfg_configuration",'6'));
		printMSG (p,"cfg_dogtags",plg.dtags.size());
		printMSG (p,"cfg_knifeweapon",'e');
		printMSG (p,"cfg_kitem",plg.knife);
		printMSG (p,"cfg_witem",plg.weapon);
		printMSG (p,"cfg_chance",plg.knife_chance,plg.weapon_chance);
		printMSG (p,"cfg_crit",plg.knife_crit_chance,plg.weapon_chance,plg.knife_crit_dmg,plg.weapon_crit_dmg);
		printEnDis (p,"cfg_bscfg",plg.backstab_crit_only);
		printMSG (p,"cfg_bssneakangle",EnDis(plg.backstab_crit_only),plg.backstab_angle);
		printEnDis (p,"cfg_hcfg",plg.harakiri);
		printMSG (p,"cfg_hchitem",plg.harakiri_dogtag_chance,plg.harakiri_weapon);
		printMSG (p,"cfg_hcdclick",plg.harakiri_cooldown,plg.harakiri_clicks,plg.harakiri_try_delay);
		printMSG (p,"cfg_lngvers",plg.language,plg.vcheck);
		//p.sendMessage("");
		//printMSG (p,"cfg_cmdmsg","/dogtag cfg <parameter> <value>",'7','8');
		//printMSG (p,"cfg_parameters","kitem, kchance, kcritch, kcritdmg, witem, wchance, wcritch, wcritdmg, backstabs, bsneak, bangle, harakiri, hdtchance, hitem, hcooldown, hclicks, hlickdelay",'7','8');
	}
	
}
