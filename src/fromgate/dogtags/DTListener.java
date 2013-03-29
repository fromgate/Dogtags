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

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;


public class DTListener implements Listener{
	Dogtags plg;
	DTUtil u;

	public DTListener (Dogtags plg){
		this.plg = plg;
		this.u = plg.u;
	}

	@EventHandler(priority=EventPriority.NORMAL, ignoreCancelled = true)
	public void onPlayerPickupItemEvent (PlayerPickupItemEvent event){
		Item item = event.getItem();
		if (item != null){
			ItemStack itemStack = item.getItemStack();
			if ((itemStack != null)&&(itemStack.getType() == Material.MAP)){
				Player p = event.getPlayer();
				Short mapid = itemStack.getDurability();
				if (plg.dtags.containsValue(mapid))
					p.sendMap(Bukkit.getMap(mapid));				
			}
		}
	}

	@EventHandler(priority=EventPriority.NORMAL, ignoreCancelled = true)
	public void onPlayerItemHeld (PlayerItemHeldEvent event){
		Player p = event.getPlayer();
		int itemSlot = event.getNewSlot();
		if (itemSlot>=0){
			ItemStack item = p.getInventory().getItem(itemSlot);
			if ((item != null)&&(item.getType()==Material.MAP)){
				Short mapid = p.getInventory().getItem(itemSlot).getDurability();
				if (plg.dtags.containsValue(mapid))
					p.sendMap(Bukkit.getMap(mapid));
			}
		}
	}

	@EventHandler(priority=EventPriority.NORMAL)
	public void onPlayerJoin (PlayerJoinEvent event){
		Player p = event.getPlayer();
		plg.rh.clearHistory(p);
		plg.AddDogtag(p);
		u.UpdateMsg(p);
		if ((p.getItemInHand()!=null)&&
				(p.getItemInHand().getType()==Material.MAP)&&
				(plg.dtags.containsKey(p.getItemInHand().getDurability())))
			p.sendMap(Bukkit.getMap(p.getItemInHand().getDurability()));
	}


	@EventHandler(priority=EventPriority.NORMAL, ignoreCancelled = true)
	public void onHarakiri (PlayerInteractEvent event){
		Player p = event.getPlayer();
		if (p.getGameMode() == GameMode.CREATIVE) return;
		if (plg.harakiri&&
				((event.getAction() == Action.RIGHT_CLICK_BLOCK)||(event.getAction() == Action.RIGHT_CLICK_AIR))&&
				plg.allowHarakiri(p)&&(plg.tryHarakiri(p)>plg.harakiri_clicks)){
			plg.harakiri_time.put(p.getName(), System.currentTimeMillis());
			p.damage(1000,p);
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
				if (!isBackstab(dp, p)) return;
				int chance = 0;
				int dmg = 0;
				if (canCutDogtag(dp)){
					int id = dp.getItemInHand().getTypeId();
					if (u.isIdInList(id, plg.weapon)) {
						chance = plg.weapon_crit_chance;
						dmg = plg.weapon_crit_dmg;
					} else if (u.isIdInList(id, plg.knife)){
						chance = plg.knife_crit_chance;
						dmg = plg.knife_crit_dmg;
					}
				} else if (canCutHead(dp)){
					int id = dp.getItemInHand().getTypeId();
					if (u.isIdInList(id, plg.weapon)) {
						chance = plg.weapon_crit_chance;
						dmg = plg.weapon_crit_dmg;
					} else if (u.isIdInList(id, plg.axe)){
						chance = plg.axe_crit_chance;
						dmg = plg.axe_crit_dmg;
					}
				}

				if ((chance>0)&&u.rollDiceChance(chance)){
					event.setDamage(event.getDamage()+dmg);
					p.addPotionEffect(new PotionEffect(PotionEffectType.CONFUSION,150,2));
					if (plg.backstab_crit_only){
						u.printMSG(dp, "msg_bstab_deal",'e','6', p.getName());
						u.printMSG(p, "msg_bstab_receive",'c','4', dp.getName());
					} else {
						u.printMSG(dp, "msg_crit_deal",'e','6', p.getName());
						u.printMSG(p, "msg_crit_receive",'c','4', dp.getName());	
					}
				}
			}
		}
	}



	@EventHandler(priority=EventPriority.HIGH, ignoreCancelled = true)
	public void onClaimDogtagAndHarakiri (PlayerDeathEvent event){
		if (event.getEntity().getKiller() == null) return;
		Player p = event.getEntity();
		Player killer = event.getEntity().getKiller();
		if (!(killer.hasPermission("dogtags.harakiri")||
				killer.hasPermission("dogtags.claim")||
				killer.hasPermission("dogtags.beheader"))) return;

		short map_id=-1;
		if (plg.dtags.containsKey(p.getName())) map_id=plg.dtags.get(p.getName());
		else {
			map_id = plg.createMap(p);
			plg.SaveDogtags();
		}

		if (killer.equals(p)&&p.hasPermission("dogtags.harakiri")) {
			if ((u.random.nextInt(100)<plg.harakiri_dogtag_chance)&&(map_id>=0)&&p.hasPermission("dogtags.claim"))
				p.getWorld().dropItemNaturally(p.getLocation().add(u.random.nextInt(2)-1, u.random.nextInt(2), u.random.nextInt(2)-1), new ItemStack (Material.MAP.getId(), 1, map_id));
			event.setDeathMessage(u.getMSG("msg_harakiri",'6','4',p.getName()));
			return;
		} else if (canCutDogtag(killer)) {
			int chance = plg.weapon_chance;
			int id = killer.getItemInHand().getTypeId();
			if (u.isIdInList(id, plg.knife)) chance = plg.knife_chance;
			if ((chance>0)&&(u.rollDiceChance(chance))){
				if (map_id>=0){
					p.getWorld().dropItemNaturally(p.getLocation().add(u.random.nextInt(2)-1, u.random.nextInt(2), u.random.nextInt(2)-1), new ItemStack (Material.MAP.getId(), 1, map_id));
					event.setDeathMessage(u.getMSG("msg_death",'e','6',killer.getName(),p.getName()));
				}
			}
		} else if (canCutHead (killer)){
			int chance = plg.weapon_chance;
			int id = killer.getItemInHand().getTypeId();
			if (u.isIdInList(id, plg.axe)) chance = plg.axe_chance;
			if (u.rollDiceChance(chance)){
				plg.dropHead(p.getLocation().add(0, 1, 0), p.getName());
				event.setDeathMessage(u.getMSG("msg_behead",'e','6',killer.getName(),p.getName()));
			}
		}
	}

	@EventHandler(priority=EventPriority.NORMAL, ignoreCancelled = true)
	public void onItemCraft (CraftItemEvent event){
		if ((event.getCurrentItem().getType()==Material.MAP)&&
				(plg.isMapIsDogtag(event.getCurrentItem().getDurability())))
			event.setCancelled(true);
	}


	/* 
	 * params:
	 * @dp - тот кто бьет
	 * @p - пострадавший
	 * 
	 */
	public boolean isBackstab(Player dp, Player p){
		if (!plg.backstab_crit_only) return true;
		if (!dp.hasPermission("dogtags.backstab")) return false;
		if (!(plg.backstab_sneak_only&&(!dp.isSneaking()))){
			int paz = (int) (p.getLocation().getYaw() + 180 + 360) % 360;
			int dpaz = (int) (dp.getLocation().getYaw() + 180 + 360) % 360;
			int angle = Math.max(paz, dpaz)-Math.min(paz, dpaz);
			if (angle>180) angle = 360-angle;					
			if (angle>plg.backstab_angle) return false;
		}
		return true;
	}


	public boolean canCutDogtag(Player p){
		return p.hasPermission("dogtags.claim")&&
				((p.getItemInHand() != null)&&(p.getItemInHand().getTypeId()>0)&&
						((u.isIdInList(p.getItemInHand().getTypeId(), plg.knife))||
								(plg.weapon_dogtags&&u.isIdInList(p.getItemInHand().getTypeId(), plg.weapon)))&&
								(!DTRgDisable.isDogatgsDisabledRegion(p.getLocation())));
	}

	public boolean canCutHead(Player p){
		return p.hasPermission("dogtags.beheader")&&
				((p.getItemInHand() != null)&&(p.getItemInHand().getTypeId()>0)&&
						((u.isIdInList(p.getItemInHand().getTypeId(), plg.axe))||
								(plg.weapon_heads&&u.isIdInList(p.getItemInHand().getTypeId(), plg.weapon)))&&
								(!DTRgDisable.isDogatgsDisabledRegion(p.getLocation())));
	}
	

}




