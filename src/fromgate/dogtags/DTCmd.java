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

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;


public class DTCmd implements CommandExecutor{
	Dogtags plg;

	FGUtil u;

	public DTCmd (Dogtags plg){
		this.plg = plg;
		this.u = this.plg.u;
	}


	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String cmdLabel, String[] args) {
		if (sender instanceof Player){
			Player p = (Player) sender;

			if ((args.length>0)&&(u.CheckCmdPerm(p, args[0]))){
				if (args.length==1) return ExecuteCmd (p, args[0]);
				else if (args.length==2) return ExecuteCmd (p, args[0],args[1]);
				else if ((args.length==3)&&(args[0].equalsIgnoreCase("cfg"))) {
					plg.SaveCfg();
					return ExecuteCmd (p, args[1],args[2]);
				}
				

			} else if (args.length==1){
				if (p.hasPermission("dogtags.id.players")){
					if (plg.dtags.containsKey(args[0])) u.PrintMSG(p, "msg_id_player",args[0]+";"+Integer.toString(plg.dtags.get(args[0])));
					else u.PrintMSG(p, "msg_id_unknown",args[0]);
				} else u.PrintMSG(p, "msg_no_permissions",'c');
				return true;
			} else if (args.length==0){
				if (p.hasPermission("dogtags.id")) {
					if (plg.dtags.containsKey(p.getName())) u.PrintMSG(p, "msg_id",plg.dtags.get(p.getName()));
					else u.PrintMSG(p, "msg_id_unknown",p.getName());
				} else u.PrintMSG(p, "msg_no_permissions",'c');
				return true;
			}
		} else sender.sendMessage("[Dogtag] Only players can execute this command. Sorry."); 
		return false;
	}


	public boolean ExecuteCmd (Player p, String cmd){
		//String pname = p.getName();
		
		if (cmd.equalsIgnoreCase("help")){
			u.PrintHLP(p);
			return true;
		} else if (cmd.equalsIgnoreCase("cfg")){
			u.PrintCfg(p);
			return true;
		} else if (cmd.equalsIgnoreCase("reload")){
			plg.LoadCfg();
			u.PrintMSG(p, "msg_reloadcfg");
			return true;
		}
		return false;
	}
	
	//команда + параметры
	public boolean ExecuteCmd (Player p, String cmd, String arg){

		if (cmd.equalsIgnoreCase("kitem")){
			plg.knife = arg;
			u.PrintMSG (p,"cfg_kitem",plg.knife);							
			return true;
		} else if (cmd.equalsIgnoreCase("kchance")){
			if (arg.matches("[1-9]+[0-9]*")) {
				plg.knife_chance = Integer.parseInt(arg);
			}
			u.PrintMSG (p,"cfg_kchance",plg.knife_chance);
			return true;
		} else if (cmd.equalsIgnoreCase("kcritch")){
			if (arg.matches("[1-9]+[0-9]*")) {
				plg.knife_crit_chance = Integer.parseInt(arg);
			}
			u.PrintMSG (p,"cfg_kcritch",plg.knife_crit_chance);
			return true;
		} else if (cmd.equalsIgnoreCase("kcritdmg")){
			if (arg.matches("[1-9]+[0-9]*")) {
				plg.knife_crit_dmg = Integer.parseInt(arg);
			}
			u.PrintMSG (p,"cfg_kcritdmg",plg.knife_crit_dmg);
			return true;
		} else if (cmd.equalsIgnoreCase("witem")){
			plg.weapon = arg;
			u.PrintMSG (p,"cfg_witem",plg.weapon);							
			return true;
		} else if (cmd.equalsIgnoreCase("wchance")){
			if (arg.matches("[1-9]+[0-9]*")) {
				plg.weapon_chance = Integer.parseInt(arg);
			}
			u.PrintMSG (p,"cfg_wchance",plg.weapon_chance);
			return true;
		} else if (cmd.equalsIgnoreCase("wcritch")){
			if (arg.matches("[1-9]+[0-9]*")) {
				plg.weapon_crit_chance = Integer.parseInt(arg);
			}
			u.PrintMSG (p,"cfg_wcritch",plg.weapon_crit_chance);
			return true;
		} else if (cmd.equalsIgnoreCase("wcritdmg")){
			if (arg.matches("[1-9]+[0-9]*")) {
				plg.weapon_crit_dmg = Integer.parseInt(arg);
			}
			u.PrintMSG (p,"cfg_wcritdmg",plg.weapon_crit_dmg);
			return true;
			
		} else if (cmd.equalsIgnoreCase("backstabs")){
			plg.backstab_crit_only=(arg.equalsIgnoreCase("on")||arg.equalsIgnoreCase("true"));
			u.PrintEnDis (p,"cfg_backstabs",plg.backstab_crit_only);
			return true;
		} else if (cmd.equalsIgnoreCase("bsneak")){
			plg.backstab_sneak_only=(arg.equalsIgnoreCase("on")||arg.equalsIgnoreCase("true"));
			u.PrintEnDis (p,"cfg_bsneak",plg.backstab_sneak_only);
			return true;
		} else if (cmd.equalsIgnoreCase("bangle")){
			if (arg.matches("[1-9]+[0-9]*")) plg.backstab_angle = Integer.parseInt(arg);
			u.PrintMSG (p,"cfg_bangle",plg.backstab_angle);
			return true;
		} else if (cmd.equalsIgnoreCase("harakiri")){
			plg.harakiri=(arg.equalsIgnoreCase("on")||arg.equalsIgnoreCase("true"));
			u.PrintEnDis (p,"cfg_harakiri",plg.harakiri);
			return true;
		} else if (cmd.equalsIgnoreCase("hdtchance")){
			if (arg.matches("[1-9]+[0-9]*")) plg.harakiri_dogtag_chance = Integer.parseInt(arg);
			u.PrintMSG (p,"cfg_hdtchance",plg.harakiri_dogtag_chance);
			return true;
		} else if (cmd.equalsIgnoreCase("hitem")){
			plg.harakiri_weapon = arg;
			u.PrintMSG (p,"cfg_hitem",plg.harakiri_weapon);							
			return true;
		} else if (cmd.equalsIgnoreCase("hcooldown")){
			if (arg.matches("[1-9]+[0-9]*")) plg.harakiri_cooldown = Integer.parseInt(arg);
			u.PrintMSG (p,"cfg_hcooldown",plg.harakiri_cooldown);
			return true;
		} else if (cmd.equalsIgnoreCase("hclicks")){
			if (arg.matches("[1-9]+[0-9]*")) plg.harakiri_clicks = Integer.parseInt(arg);
			u.PrintMSG (p,"cfg_hclicks",plg.harakiri_clicks);
			return true;
		} else if (cmd.equalsIgnoreCase("hclickdelay")){
			if (arg.matches("[1-9]+[0-9]*")) plg.harakiri_try_delay = Integer.parseInt(arg);
			u.PrintMSG (p,"cfg_hclickdelay",plg.harakiri_try_delay);
			return true;
		} else if (cmd.equalsIgnoreCase("help")){
			u.PrintHLP(p, arg);
			return true;
		}

		return false;
	}
	
}
