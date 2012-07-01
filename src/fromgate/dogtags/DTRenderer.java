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
	
	
	import org.bukkit.entity.Player;
	import org.bukkit.map.MapCanvas;
	import org.bukkit.map.MapRenderer;
	import org.bukkit.map.MapView;
	import org.bukkit.map.MinecraftFont;
	
	public class DTRenderer extends MapRenderer {
		
		Dogtags plg;
		String pname;
		
		public DTRenderer (Dogtags plg, String pname){
			super (true); 
			this.plg = plg;
			this.pname =  pname;
		}
		
	
		@Override
		public void render(MapView map, MapCanvas canvas, Player p) {
	        for (int j = 0; j < 128; j++) 
	            for (int i = 0; i < 128; i++)
	              canvas.setPixel(i, j, (byte) 0);
	        if (plg.dtimg != null) canvas.drawImage(0, 0, plg.dtimg);
	        canvas.drawText(30, 63-(MinecraftFont.Font.getHeight()/2), MinecraftFont.Font, pname);
	        canvas.drawText(2, 127-MinecraftFont.Font.getHeight(), MinecraftFont.Font, "§54;Dogtags by fromgate");
		}
	
	}
