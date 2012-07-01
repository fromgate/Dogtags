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

import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;


public class DTListener implements Listener{
	Dogtags plg;
	FGUtil u;

	public DTListener (Dogtags plg){
		this.plg = plg;
		this.u = plg.u;
	}


	@EventHandler(priority=EventPriority.NORMAL)
	public void onPlayerJoin (PlayerJoinEvent event){
		Player p = event.getPlayer();
		plg.AddDogtag(p);
		plg.sendMaps(p);
		u.UpdateMsg(p);
	}
	
	
	@EventHandler(priority=EventPriority.NORMAL, ignoreCancelled = true)
	public void onHarakiri (PlayerInteractEvent event){
		Player p = event.getPlayer();
		if (p.getGameMode() == GameMode.CREATIVE) return;
		if (plg.harakiri&&
				((event.getAction() == Action.RIGHT_CLICK_BLOCK)||(event.getAction() == Action.RIGHT_CLICK_AIR))&&
				plg.allowHarakiri(p)&&(plg.tryHarakiri(p)>plg.harakiri_clicks)){
			plg.harakiri_time.put(p.getName(), System.currentTimeMillis());
			p.damage(p.getHealth(),p);
		}
	}
	

	@EventHandler(priority=EventPriority.NORMAL, ignoreCancelled = true)
	public void onCritDmg (EntityDamageEvent event){
		if ((event.getEntity() instanceof Player)&&(event.getCause() == DamageCause.ENTITY_ATTACK)){
			Player p = (Player) event.getEntity();
			EntityDamageByEntityEvent evdm = (EntityDamageByEntityEvent) event;

			if (evdm.getDamager() instanceof Player){
			
				Player dp = (Player) evdm.getDamager();
				if (p.equals(dp)) return; //харакири...
				
				if (plg.backstab_crit_only&&dp.hasPermission("dogtags.backstab")&&(!(plg.backstab_sneak_only&&(!dp.isSneaking())))){
					int paz = (int) (p.getLocation().getYaw() + 180 + 360) % 360;
					int dpaz = (int) (dp.getLocation().getYaw() + 180 + 360) % 360;
					int angle = Math.max(paz, dpaz)-Math.min(paz, dpaz);
					if (angle>180) angle = 360-angle;					
					if (angle>plg.backstab_angle) return;
				}

				
				if (dp.hasPermission("dogtags.claim")&&(dp.getItemInHand() != null)&&(dp.getItemInHand().getTypeId()>0)){
					int chance = 0;
					int dmg = 0;
					int id = dp.getItemInHand().getTypeId();
					
					if (u.isIdInList(id, plg.weapon)) {
						chance = plg.weapon_crit_chance;
						dmg = plg.weapon_crit_dmg;
					} else if (dp.hasPermission("dogtags.knife")&&(u.isIdInList(id, plg.knife))){
						chance = plg.knife_crit_chance;
						dmg = plg.knife_crit_dmg;
					}
					
					if (u.random.nextInt(100)<chance){
						event.setDamage(event.getDamage()+dmg);
						p.addPotionEffect(new PotionEffect(PotionEffectType.CONFUSION,150,2));
						
						if (plg.backstab_crit_only){
							u.PrintMSG(dp, "msg_bstab_deal", p.getName(),'e','6');
							u.PrintMSG(p, "msg_bstab_receive", dp.getName(),'c','4');
						} else {
							u.PrintMSG(dp, "msg_crit_deal", p.getName(),'e','6');
							u.PrintMSG(p, "msg_crit_receive", dp.getName(),'c','4');	
						}
					}
				}
			}
		}
	}
	
	

	//@EventHandler(priority=EventPriority.NORMAL, ignoreCancelled = true)
	@EventHandler(priority=EventPriority.NORMAL)
	public void onClaimDogtagAndHarakiri (PlayerDeathEvent event){
		
		if (event.getEntity().getKiller() ==null) return;
		Player p = event.getEntity();
		Player killer = event.getEntity().getKiller();
		if (!(killer.hasPermission("dogtags.harakiri")||killer.hasPermission("dogtags.claim"))) return;
		
		short map_id=-1;
		if (plg.dtags.containsKey(p.getName())) map_id=plg.dtags.get(p.getName());
		else {
			map_id = plg.createMap(p);
			plg.SaveDogtags();
		}
		
		
		if (killer.equals(p)&&p.hasPermission("dogtags.harakiri")) {
			if ((u.random.nextInt(100)<plg.harakiri_dogtag_chance)&&(map_id>=0)&&p.hasPermission("dogtags.claim"))
				p.getWorld().dropItemNaturally(p.getLocation().add(u.random.nextInt(2)-1, u.random.nextInt(2), u.random.nextInt(2)-1), new ItemStack (Material.MAP.getId(), 1, map_id));
			
			event.setDeathMessage(u.MSG("msg_harakiri",p.getName(),'6','4'));
			return;
		} else if ((killer.getItemInHand() != null)&&(killer.getItemInHand().getTypeId()>0)&&(killer.hasPermission("dogtags.claim"))){
				int chance = 0; 
				int id = killer.getItemInHand().getTypeId();
				if (u.isIdInList(id, plg.weapon)) chance = plg.weapon_chance;
				else if (killer.hasPermission("dogtags.knife")&&(u.isIdInList(id, plg.knife))) chance = plg.knife_chance;
				if ((chance>0)&&(u.random.nextInt(100)<chance)){
					if (map_id>=0){
						p.getWorld().dropItemNaturally(p.getLocation().add(u.random.nextInt(2)-1, u.random.nextInt(2), u.random.nextInt(2)-1), new ItemStack (Material.MAP.getId(), 1, map_id));
						event.setDeathMessage(u.MSG("msg_death",killer.getName()+";"+p.getName(),'e','6'));
					}
				}
			}
		}
	

}




