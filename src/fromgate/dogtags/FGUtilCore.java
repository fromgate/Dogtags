/*  
 *  FGUtilCore, Utilities class for Minecraft bukkit plugins
 *  
 *    (c)2012, fromgate, fromgate@gmail.com
 *  
 *      
 *  FGUtilCore is free software: you can redistribute it and/or modify
 *	it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  FGUtilCore is distributed in the hope that it will be useful,
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
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilderFactory;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public abstract class FGUtilCore {
	JavaPlugin plg;
	//конфигурация утилит
	public String px = "";
	private String permprefix="fgutilcore.";
	private boolean version_check = false; // включить после заливки на девбукит
	private String version_check_url = "";//"http://dev.bukkit.org/server-mods/skyfall/files.rss";
	private String version_name = ""; // идентификатор на девбукките (всегда должен быть такой!!!)
	private String version_info_perm = permprefix+"config"; // кого оповещать об обнволениях
	private String language="english";
	private String plgcmd = "<command>";
	// Сообщения+перевод
	YamlConfiguration lng;
	//String lngfile = this.language+".lng";
	protected HashMap<String,String> msg = new HashMap<String,String>(); //массив сообщений
	private char c1 = 'a'; //цвет 1 (по умолчанию для текста)
	private char c2 = '2'; //цвет 2 (по умолчанию для значений)
	protected String msglist ="";
	private boolean colorconsole = false;  // надо будет добавить методы для конфигурации "из вне"

	protected HashMap<String,Cmd> cmds = new HashMap<String,Cmd>();
	protected String cmdlist ="";

	PluginDescriptionFile des;
	private double version_current=0;
	private double version_new=0;
	private String version_new_str="unknown";
	private Logger log = Logger.getLogger("Minecraft");
	Random random = new Random ();
	BukkitTask chId;



	public FGUtilCore(JavaPlugin plg, boolean vcheck, boolean savelng, String lng, String devbukkitname, String version_name, String plgcmd, String px){
		this.plg = plg;
		this.des = plg.getDescription();
		this.version_current = Double.parseDouble(des.getVersion().replaceFirst("\\.", "").replace("/", ""));
		this.version_name = version_name;
		this.version_check=vcheck;
		this.language = lng;
		this.InitMsgFile();
		this.initStdMsg();
		// if (savelng) this.SaveMSG(); /// ммм... как бы это синхронизировать.... 

		if (devbukkitname.isEmpty()) this.version_check=false;
		else {
			this.version_check_url = "http://dev.bukkit.org/server-mods/"+devbukkitname+"/files.rss";
			this.permprefix = devbukkitname+".";
			UpdateMsg();
			startUpdateTick();
		}

		if (version_name.isEmpty()) this.version_name = des.getName();
		else this.version_name = version_name;
		this.px = px;
		this.plgcmd = plgcmd;
	}

	/* 
	 * Инициализация стандартных сообщений
	 */
	private void initStdMsg(){
		addMSG ("msg_outdated", "%1% is outdated!");
		addMSG ("msg_pleasedownload", "Please download new version (%1%) from ");
		addMSG ("hlp_help", "Help");
		addMSG ("hlp_thishelp", "%1% - this help");
		addMSG ("hlp_execcmd", "%1% - execute command");
		addMSG ("hlp_typecmd", "Type %1% - to get additional help");
		addMSG ("hlp_typecmdpage", "Type %1% - to see another page of this help");
		addMSG ("hlp_commands", "Command list:");
		addMSG ("hlp_cmdparam_command", "command");
		addMSG ("hlp_cmdparam_page", "page");
		addMSG ("hlp_cmdparam_parameter", "parameter");
		addMSG ("cmd_unknown", "Unknown command: %1%");
		addMSG ("cmd_cmdpermerr", "Something wrong (check command, permissions)");
		addMSG ("enabled", "enabled");
		msg.put("enabled", ChatColor.DARK_GREEN+msg.get("enabled"));
		addMSG ("disabled", "disabled");
		msg.put("disabled", ChatColor.RED+msg.get("disabled"));
		addMSG ("lst_title", "String list:");
		addMSG ("lst_footer", "Page: [%1% / %2%]");
		addMSG ("lst_listisempty", "List is empty");
	}


	public void setConsoleColored(boolean colorconsole){
		this.colorconsole = colorconsole;
	}

	public boolean isConsoleColored(){
		return this.colorconsole;
	}

	/* 
	 * Включение/выключение проверки версий. По идее не нужно ;)
	 */
	public void SetVersionCheck (boolean vc){
		this.version_check = vc;
	}


	/* Вывод сообщения о выходе новой версии, вызывать из
	 * обработчика события PlayerJoinEvent
	 */
	public void UpdateMsg (Player p){
		if ((version_check)&&(p.hasPermission(this.version_info_perm))&&(version_new>version_current)){
			printMSG(p, "msg_outdated",'e','6',"&6"+des.getName()+" v"+des.getVersion());
			printMSG(p,"msg_pleasedownload",'e','6',version_new_str);
			printMsg(p, "&3"+version_check_url.replace("files.rss", ""));
		}
	}

	/* Вызывается автоматом при старте плагина,
	 * пишет сообщение о выходе новой версии в лог-файл
	 */
	public void UpdateMsg (){
		plg.getServer().getScheduler().runTaskAsynchronously(plg, new Runnable(){
			public void run() {
				version_new = getNewVersion(version_current);
				if (version_new>version_current){
					log.info("["+des.getName()+"] "+des.getName()+" v"+des.getVersion()+" is outdated! Recommended version is v"+version_new_str);
					log.info("["+des.getName()+"] "+version_check_url.replace("files.rss", ""));
				}			
			}
		});
	}

	/* Проверяет вышла ли новая версия
	 * не рекомендуется вызывать из стандартных обработчиков событий (например, PlayerJoinEvent)
	 */

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

	/* Процесс проверяющий выход обновления каждые полчаса
	 * 
	 */
	private void startUpdateTick(){
		chId = plg.getServer().getScheduler().runTaskTimerAsynchronously(plg, new Runnable() {
			public void run() {
				version_new = getNewVersion (version_current);
			}
		}, (10+this.random.nextInt(50)) * 1200, 60 * 1200);
	}


	/*
	 * Процедуры для обработчика комманд
	 * 
	 */

	/* Добавляет новую команду в список
	 * 
	 */
	@Deprecated
	public void addCmd (String cmd, String perm, String desc){
		addCmd (cmd, perm,desc,false);
	}
	@Deprecated
	public void addCmd (String cmd, String perm, String desc, boolean console){
		cmds.put(cmd, new Cmd(this.permprefix+perm,desc,console));
		if (cmdlist.isEmpty()) cmdlist = cmd;
		else cmdlist = cmdlist+", "+cmd;
	}

	public void addCmd (String cmd, String perm, String desc_id, String desc_key){
		addCmd (cmd, perm,desc_id,desc_key,this.c1, this.c2,false);
	}

	public void addCmd (String cmd, String perm, String desc_id, String desc_key, char color){
		addCmd (cmd, perm,desc_id,desc_key,this.c1, color,false);
	}

	public void addCmd (String cmd, String perm, String desc_id, String desc_key, char color,boolean console){
		addCmd (cmd, perm,desc_id,desc_key,this.c1, color,console);
	}

	public void addCmd (String cmd, String perm, String desc_id, String desc_key, char color1, char color2){
		addCmd (cmd, perm,desc_id,desc_key,color1, color2,false);
	}

	public void addCmd (String cmd, String perm, String desc_id, String desc_key, char color1, char color2, boolean console){
		String desc = getMSG(desc_id,desc_key,color1, color2);
		cmds.put(cmd, new Cmd(this.permprefix+perm,desc,console));
		if (cmdlist.isEmpty()) cmdlist = cmd;
		else cmdlist = cmdlist+", "+cmd;
	}

	/* 
	 * Проверка пермишенов и наличия команды
	 */

	@Deprecated
	public boolean CheckCmdPerm (Player p, String cmd){
		return ((cmds.containsKey(cmd.toLowerCase()))&&
				(cmds.get(cmd.toLowerCase()).perm.isEmpty()||((!cmds.get(cmd.toLowerCase()).perm.isEmpty())&&
						p.hasPermission(cmds.get(cmd.toLowerCase()).perm))));
	}

	public boolean checkCmdPerm (CommandSender sender, String cmd){
		if (!cmds.containsKey(cmd.toLowerCase())) return false;
		Cmd cm = cmds.get(cmd.toLowerCase());
		if (sender instanceof Player) return (cm.perm.isEmpty()||sender.hasPermission(cm.perm));
		else return cm.console;
	}

	/* Класс, описывающий команду:
	 * perm - постфикс пермишена
	 * desc - описание команды
	 */
	public class Cmd {
		String perm;
		String desc;
		boolean console;
		public Cmd (String perm, String desc){
			this.perm = perm;
			this.desc = desc;
			this.console = false;
		}
		public Cmd (String perm, String desc, boolean console){
			this.perm = perm;
			this.desc = desc;
			this.console = console;
		}
	}

	public class PageList {
		private List<String> ln;
		private int lpp = 15;
		private String title_msgid="lst_title";
		private String footer_msgid="lst_footer";
		private boolean shownum=false;

		public void setLinePerPage (int lpp){
			this.lpp = lpp;
		}

		public PageList(List<String> ln, String title_msgid,String footer_msgid, boolean shownum){
			this.ln = ln;
			if (!title_msgid.isEmpty())	this.title_msgid =title_msgid; 
			if (!footer_msgid.isEmpty()) this.footer_msgid = footer_msgid;
			this.shownum = shownum;
		}

		public void addLine (String str){
			ln.add(str);
		}

		public boolean isEmpty(){
			return ln.isEmpty();
		}

		public void setTitle(String title_msgid){
			this.title_msgid = title_msgid;

		}

		public void setShowNum(boolean shownum){
		}

		public void setFooter(String footer_msgid){
			this.footer_msgid = footer_msgid;
		}

		public void printPage(Player p, int pnum){
			printPage (p, pnum,this.lpp);
		}

		public void printPage(CommandSender  p, int pnum, int linesperpage){
			if (ln.size()>0){

				int maxp = ln.size()/linesperpage;
				if ((ln.size()%linesperpage)>0) maxp++;
				if (pnum>maxp) pnum = maxp;
				int maxl = linesperpage;
				if (pnum == maxp) {
					maxl = ln.size()%linesperpage;
					if (maxp==1) maxl = ln.size();
				}
				if (maxl == 0) maxl = linesperpage;
				if (msg.containsKey(title_msgid)) printMsg(p, "&6&l"+getMSGnc(title_msgid));
				else printMsg(p,title_msgid);

				String numpx="";
				for (int i = 0; i<maxl; i++){
					if (shownum) numpx ="&3"+ Integer.toString(1+i+(pnum-1)*linesperpage)+". ";
					printMsg(p, numpx+"&a"+ln.get(i+(pnum-1)*linesperpage));
				}
				if (maxp>1)	printMSG(p, this.footer_msgid,'e','6', pnum,maxp);
			} else printMSG (p, "lst_listisempty",'c'); 
		} 

	}

	public void printPage (Player p, List<String> ln, int pnum, String title, String footer, boolean shownum){
		PageList pl = new PageList (ln, title, footer, shownum);
		pl.printPage(p, pnum);		
	}

	public void printPage (CommandSender  p, List<String> ln, int pnum, String title, String footer, boolean shownum, int lineperpage){
		PageList pl = new PageList (ln, title, footer, shownum);
		pl.printPage(p, pnum, lineperpage);
	}


	/*
	 * Разные полезные процедурки 
	 * 
	 */

	/* Функция проверяет входит ли число (int)
	 * в список чисел представленных в виде строки вида n1,n2,n3,...nN
	 */
	public boolean isIdInList (int id, String str){
		if (!str.isEmpty()){
			String [] ln = str.split(",");
			if (ln.length>0) 
				for (int i = 0; i<ln.length; i++)
					if ((!ln[i].isEmpty())&&ln[i].matches("[0-9]*")&&(Integer.parseInt(ln[i])==id)) return true;
		}
		return false;
	}

	/* 
	 * Функция проверяет входит ли слово (String) в список слов
	 * представленных в виде строки вида n1,n2,n3,...nN
	 */
	public boolean isWordInList (String word, String str){
		String [] ln = str.split(",");
		if (ln.length>0) 
			for (int i = 0; i<ln.length; i++)
				if (ln[i].equalsIgnoreCase(word)) return true;
		return false;
	}

	/* 
	 * Функция проверяет входит есть ли item (блок) с заданным id и data в списке,
	 * представленным в виде строки вида id1:data1,id2:data2,MATERIAL_NAME:data
	 * При этом если data может быть опущена
	 */
	public boolean isItemInList (int id, int data, String str){
		String [] ln = str.split(",");
		if (ln.length>0) 
			for (int i = 0; i<ln.length; i++)
				if (compareItemStr (id, data,ln[i])) return true;
		return false;
	}


	public boolean compareItemStr (ItemStack item, String itemstr){
		return compareItemStr (item.getTypeId(), item.getData().getData(), item.getAmount(), itemstr);
	}

	public boolean compareItemStr (int item_id, int item_data, String itemstr){
		return compareItemStr (item_id,item_data,1,itemstr);
	}

	// Надо использовать маску: id:data*amount, id:data, id*amount
	public boolean compareItemStr (int item_id, int item_data, int item_amount, String itemstr){
		if (!itemstr.isEmpty()){
			int id = -1;
			int amount =1;
			int data =-1;
			String [] si = itemstr.split("\\*");
			if (si.length>0){
				if ((si.length==2)&&si[1].matches("[1-9]+[0-9]*")) amount = Integer.parseInt(si[1]);
				String ti[] = si[0].split(":");
				if (ti.length>0){
					if (ti[0].matches("[0-9]*")) id=Integer.parseInt(ti[0]);
					else id=Material.getMaterial(ti[0]).getId();						
					if ((ti.length==2)&&(ti[1]).matches("[0-9]*")) data = Integer.parseInt(ti[1]);
					return ((item_id==id)&&((data<0)||(item_data==data))&&(item_amount>=amount));
				}
			}
		}									
		return false;
	}

	public boolean removeItemInHand(Player p, String itemstr){
		if (!itemstr.isEmpty()){
			int id = -1;
			int amount =1;
			int data =-1;
			String [] si = itemstr.split("\\*");
			if (si.length>0){
				if ((si.length==2)&&si[1].matches("[1-9]+[0-9]*")) amount = Integer.parseInt(si[1]);
				String ti[] = si[0].split(":");
				if (ti.length>0){
					if (ti[0].matches("[0-9]*")) id=Integer.parseInt(ti[0]);
					else id=Material.getMaterial(ti[0]).getId();						
					if ((ti.length==2)&&(ti[1]).matches("[0-9]*")) data = Integer.parseInt(ti[1]);
					return removeItemInHand (p, id,data,amount);
				}
			}
		}
		return false;
	}

	public boolean removeItemInHand(Player p, int item_id, int item_data, int item_amount){
		if ((p.getItemInHand() != null)&&
				(p.getItemInHand().getTypeId()==item_id)&&
				(p.getItemInHand().getAmount()>=item_amount)&&
				((item_data<0)||(item_data==p.getItemInHand().getData().getData()))){

			if (p.getItemInHand().getAmount()>item_amount) p.getItemInHand().setAmount(p.getItemInHand().getAmount()-item_amount);
			else p.setItemInHand(new ItemStack (Material.AIR));

			return true;
		}
		return false;
	}

	public void giveItemOrDrop (Player p, ItemStack item){
		HashMap<Integer,ItemStack> result = p.getInventory().addItem(item);
		if (result.size()>0)
			for (int i : result.keySet())
				p.getWorld().dropItemNaturally(p.getLocation(), result.get(i));
	}

	/*
	 * Вывод сообщения пользователю 
	 */
	public void printMsg(CommandSender p, String msg){
		String message =ChatColor.translateAlternateColorCodes('&', msg);
		if ((!(p instanceof Player))&&(!colorconsole)) message = ChatColor.stripColor(message);
		p.sendMessage(message);
	}
	@Deprecated
	public void PrintMsg(Player p, String msg){
		p.sendMessage(ChatColor.translateAlternateColorCodes('&', msg));
	}

	/*
	 *  Вывод сообещения пользователю (с префиксом)
	 */
	public void printPxMsg(CommandSender p, String msg){
		printMsg(p, px+msg);
	}

	@Deprecated
	public void PrintPxMsg(Player p, String msg){
		p.sendMessage(ChatColor.translateAlternateColorCodes('&', px+msg));
	}

	/*
	 * Бродкаст сообщения, использую при отладке 
	 */
	public void BC (String msg){
		plg.getServer().broadcastMessage(ChatColor.translateAlternateColorCodes('&', px+msg));
	}

	/*
	 * Запись сообщения в лог 
	 */
	public void log (String msg){
		log.info(ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&', px+msg)));
	}

	/*
	 * Отправка цветного сообщения в консоль 
	 */
	public void SC (String msg){
		plg.getServer().getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', px+msg));
	}



	/*
	 * Перевод
	 * 
	 */

	/*
	 *  Инициализация файла с сообщениями
	 */
	public void InitMsgFile(){
		try {
			lng = new YamlConfiguration();
			File f = new File (plg.getDataFolder()+File.separator+this.language+".lng");
			if (f.exists()) lng.load(f);
			else {
				InputStream is = plg.getClass().getResourceAsStream("/language/"+this.language+".lng");
				if (is!=null) lng.load(is);
			}
		} catch (Exception e){
			e.printStackTrace();
		}
	}

	/*
	 * Добавлене сообщения в список
	 * Убираются цвета.
	 * Параметры:
	 * key - ключ сообщения
	 * txt - текст сообщения
	 */
	public void addMSG(String key, String txt){
		msg.put(key, ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&', lng.getString(key,txt))));
		if (msglist.isEmpty()) msglist=key;
		else msglist=msglist+","+key;
	}


	/*
	 * Сохранение сообщений в файл 
	 */
	public void SaveMSG(){
		String [] keys = this.msglist.split(",");
		try {
			File f = new File (plg.getDataFolder()+File.separator+this.language+".lng");
			if (!f.exists()) f.createNewFile();
			YamlConfiguration cfg = new YamlConfiguration();
			for (int i = 0; i<keys.length;i++)
				cfg.set(keys[i], msg.get(keys[i]));
			cfg.save(f);
		} catch (Exception e){
			e.printStackTrace();
		}
	} 

	/* 
	 * Получение сообщения по ключу 
	 */
	@Deprecated
	public String MSG(String id){
		return MSG (id,"",this.c1, this.c2);
	}

	/*
	 *  Получение сообщения по ключу (с - цвет, одним символом)
	 */
	@Deprecated
	public String MSG(String id, char c){
		return MSG (id,"",c, this.c2);
	}

	/*  Получаем сообщение, при этом keys будет использовано
	 *  для подмены %1%, %2% и т.д.
	 */
	@Deprecated
	public String MSG(String id, String keys){
		return MSG (id,keys,this.c1, this.c2);
	}

	/*  Получаем сообщение (цвет с), при этом keys будет использовано
	 *  для подмены %1%, %2% и т.д.
	 */
	@Deprecated
	public String MSG(String id, String keys, char c){
		return MSG (id,keys,this.c1, c);
	}

	/*
	 *  MSG (String id, [char color1, char color2], Object param1, Object param2, Object param3... )
	 */
	public String getMSG (Object... s){
		String str = "&4Unknown message";
		String color1 = "&"+this.c1;
		String color2 = "&"+this.c2;
		if (s.length>0) {
			String id = s[0].toString();
			str = "&4Unknown message ("+id+")";
			if (msg.containsKey(id)){
				int px = 1;
				if ((s.length>1)&&(s[1] instanceof Character )){
					px = 2;
					color1 = "&"+(Character) s[1];
					if ((s.length>2)&&(s[2] instanceof Character )){
						px = 3;
						color2 = "&"+(Character) s[2];
					}
				}
				str = color1+msg.get(id);
				if (px<s.length)
					for (int i = px; i<s.length; i++)
						str = str.replace("%"+Integer.toString(i-px+1)+"%", color2+s[i].toString()+color1);
			}
		}
		return ChatColor.translateAlternateColorCodes('&', str);
	}

	public void printMSG (CommandSender p, Object... s){
		String message = getMSG (s); 
		if ((!(p instanceof Player))&&(!colorconsole)) message = ChatColor.stripColor(message);
		p.sendMessage(message);
	}



	/*  Получаем сообщение (с1 - цвет текст, c2 - цвет значения), при этом keys будет использовано
	 *  для подмены %1%, %2% и т.д.
	 */
	@Deprecated
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


	public String MSG(String id, char c1, char c2, Object... keys){
		String str = "&4Unknown message ("+id+")";
		if (msg.containsKey(id)){
			String clr1 = "";
			String clr2 = "";
			if (c1 != 'z') clr1 = "&"+c1;
			if (c2 != 'z') clr2 = "&"+c2;
			str = clr1 + msg.get(id);
			if (keys.length>0)
				for (int i =0; i<keys.length;i++)
					str.replace("%"+Integer.toString(i+1)+"%", clr2+keys[i].toString()+clr1);
		}
		return ChatColor.translateAlternateColorCodes('&', str);
	}


	/* 
	 * Печать сообщения
	 */
	@Deprecated
	public void PrintMSG (Player p, String msg_key, int key){
		p.sendMessage(MSG (msg_key, Integer.toString(key), this.c1, this.c2));
	}

	/* 
	 * Печать сообщения
	 */
	@Deprecated
	public void PrintMSG (Player p, String msg_key, String keys){
		p.sendMessage(MSG (msg_key, keys, this.c1, this.c2));
	}

	@Deprecated
	public void PrintPxMSG (Player p, String msg_key, String keys){
		PrintPxMsg(p,MSG (msg_key, keys, this.c1, this.c2));
	}



	/* 
	 * Печать сообщения
	 */
	@Deprecated
	public void PrintMSG (Player p, String msg_key, String keys, char c1, char c2){
		p.sendMessage(MSG (msg_key, keys, c1, c2));
	}

	/* 
	 * Печать сообщения
	 */
	@Deprecated
	public void PrintMSG (Player p, String msg_key, char c1){
		p.sendMessage(MSG (msg_key, c1));
	}

	@Deprecated
	public void PrintPxMSG (Player p, String msg_key, char c1){
		PrintPxMsg(p,MSG (msg_key, c1));
	}

	/* 
	 * Печать сообщения
	 */
	@Deprecated
	public void PrintMSG (Player p, String msg_key){
		p.sendMessage(MSG (msg_key));
	}

	@Deprecated
	public void PrintPxMSG (Player p, String msg_key){
		PrintPxMsg(p,MSG (msg_key));
	}


	/* 
	 * Печать справки
	 */
	@Deprecated
	public void PrintHLP (Player p){
		PrintMsg(p, "&6&l"+version_name+" v"+des.getVersion()+" &r&6| "+getMSG("hlp_help",'6'));
		printMSG(p, "hlp_thishelp","/"+plgcmd+" help");
		printMSG(p, "hlp_execcmd","/"+plgcmd+" <"+getMSG("hlp_cmdparam_command",'2')+"> ["+getMSG("hlp_cmdparam_parameter",'2')+"]");
		printMSG(p, "hlp_typecmd","/"+plgcmd+" help <"+getMSG("hlp_cmdparam_command",'2')+">");
		PrintMsg(p, getMSG("hlp_commands")+" &2"+cmdlist);
	}


	/* 
	 * Печать справки по команде
	 */
	@Deprecated
	public void PrintHLP (Player p, String cmd){
		if (cmds.containsKey(cmd)){
			printMsg(p, "&6&l"+version_name+" v"+des.getVersion()+" &r&6| "+getMSG("hlp_help",'6'));
			printMsg(p, cmds.get(cmd).desc);
		} else printMSG(p,"cmd_unknown",'c','e',cmd);
	}

	public void PrintHlpList (CommandSender p, int page, int lpp){
		String title = "&6&l"+version_name+" v"+des.getVersion()+" &r&6| "+getMSG("hlp_help",'6');
		List<String> hlp = new ArrayList<String>();
		hlp.add(getMSG("hlp_thishelp","/"+plgcmd+" help"));
		hlp.add(getMSG("hlp_execcmd","/"+plgcmd+" <"+getMSG("hlp_cmdparam_command",'2')+"> ["+getMSG("hlp_cmdparam_parameter",'2')+"]"));
		if (p instanceof Player) hlp.add(getMSG("hlp_typecmdpage","/"+plgcmd+" help <"+getMSG("hlp_cmdparam_page",'2')+">"));

		String [] ks = (cmdlist.replace(" ", "")).split(",");
		if (ks.length>0){
			for (String cmd : ks)
				hlp.add(cmds.get(cmd).desc);
		}
		printPage (p, hlp, page, title, "", false,lpp);
	}

	/* 
	 * Возврат логической переменной в виде текста выкл./вкл.
	 */
	public String EnDis (boolean b){
		return b ? getMSG ("enabled",'2') : getMSG ("disabled",'c'); 
	}

	public String EnDis (String str, boolean b){
		String str2 = ChatColor.stripColor(str);
		return b ? ChatColor.DARK_GREEN+str2 : ChatColor.RED+str2; 
	}

	/* 
	 * Печать значения логической переменной 
	 */
	public void printEnDis (CommandSender p, String msg_id, boolean b){
		p.sendMessage(getMSG (msg_id)+": "+EnDis(b));
	}

	@Deprecated
	public void PrintEnDis (Player p, String msg_id, boolean b){
		p.sendMessage(MSG (msg_id)+": "+EnDis(b));
	}

	/* 
	 * Печать значения логической переменной 
	 */
	@Deprecated
	public void PrintMSG (Player p, String msg_id, boolean b){
		PrintMSG (p,msg_id,EnDis(b));
	}


	/* 
	 * Дополнительные процедуры
	 */

	/*
	 * Переопределение префикса пермишенов 
	 */
	public void setPermPrefix(String ppfx){
		this.permprefix = ppfx+".";
		this.version_info_perm=this.permprefix+"config";
	}

	/*
	 * Проверка соответствия пермишена (указывать без префикса)
	 * заданной команде 
	 */
	public boolean equalCmdPerm(String cmd, String perm) {
		return (cmds.containsKey(cmd.toLowerCase())) && 
				((cmds.get(cmd.toLowerCase())).perm.equalsIgnoreCase(permprefix+perm));
	}


	/* 
	 * Преобразует строку вида <id>:<data> в ItemStack
	 * Возвращает null если строка кривая
	 */

	@Deprecated
	public ItemStack parseItem (String itemstr){
		if (!itemstr.isEmpty()){
			String[] ti = itemstr.split(":");
			if (ti.length>0){

				int id = -1;
				if (ti[0].matches("[1-9]+[0-9]*")) id=Integer.parseInt(ti[0]);
				else id = Material.getMaterial(ti[0]).getId();


				int count = 1;
				if ((ti.length>1)&&(ti[1].matches("[1-9]+[0-9]*")))
					count = Integer.parseInt(ti[1]);
				short data = 0;
				if ((ti.length==3)&&(ti[2].matches("[1-9]+[0-9]*")))
					data = Short.parseShort(ti[2]);				
				return new ItemStack (id, count, data);
			}
		}
		return null;
	}

	/* 
	 * Преобразует строку вида <id>:<data>[*<amount>] в ItemStack
	 * Возвращает null если строка кривая
	 */
	public ItemStack parseItemStack (String itemstr){
		if (!itemstr.isEmpty()){
			int id = -1;
			int amount =1;
			short data =0;			
			String [] si = itemstr.split("\\*");
			if (si.length>0){
				if ((si.length==2)&&si[1].matches("[1-9]+[0-9]*")) amount = Integer.parseInt(si[1]);
				String ti[] = si[0].split(":");
				if (ti.length>0){
					if (ti[0].matches("[0-9]*")) id=Integer.parseInt(ti[0]);
					else id=Material.getMaterial(ti[0]).getId();						
					if ((ti.length==2)&&(ti[1]).matches("[0-9]*")) data = Short.parseShort(ti[1]);
					return new ItemStack (id,amount,data);
				}
			}
		}
		return null;
	}


	/*
	 * Проверяет, есть ли игроки в пределах заданного радиуса
	 */
	public boolean isPlayerAround (Location loc, int radius){
		for (Player p : loc.getWorld().getPlayers()){
			if (p.getLocation().distance(loc)<=radius) return true;
		}
		return false;		
	}

	/*
	 *  Тоже, что и MSG, но обрезает цвет
	 */
	public String getMSGnc(Object... s){
		return ChatColor.stripColor(getMSG (s));
	}



	@Deprecated
	public String MSGnc(String id){
		return ChatColor.stripColor(MSG (id));
	}

	@Deprecated
	public String MSGnc(String msg_key, String key){
		return ChatColor.stripColor(MSG (msg_key, key));
	}

	/*
	 * Установка блока с проверкой на приват
	 */
	public boolean placeBlock(Location loc, Player p, Material newType, byte newData, boolean phys){
		return placeBlock (loc.getBlock(),p,newType,newData, phys);
	}

	/*
	 * Установка блока с проверкой на приват
	 */
	public boolean placeBlock(Block block, Player p, Material newType, byte newData, boolean phys){
		BlockState state = block.getState();
		block.setTypeIdAndData(newType.getId(), newData, phys);
		BlockPlaceEvent event = new BlockPlaceEvent(state.getBlock(), state, block, p.getItemInHand(), p, true);
		plg.getServer().getPluginManager().callEvent(event);
		if (event.isCancelled()) state.update(true);
		return event.isCancelled();
	}

	public boolean rollDiceChance (int chance){
		return (random.nextInt(100)<chance);
	}


	/*
	 * Проверка формата строкового представления целых чисел 
	 */
	public boolean isIntegerSigned (String str){
		return (str.matches("-?[0-9]+[0-9]*"));
	}

	public boolean isIntegerSigned (String... str){
		if (str.length==0) return false;
		for (String s : str)
			if (!s.matches("-?[0-9]+[0-9]*")) return false;
		return true;
	}

	public boolean isInteger (String str){
		return (str.matches("[0-9]+[0-9]*"));
	}

	public boolean isInteger (String... str){
		if (str.length==0) return false;
		for (String s : str)
			if (!s.matches("[0-9]+[0-9]*")) return false;
		return true;
	}


	public boolean isIntegerGZ (String str){
		return (str.matches("[1-9]+[0-9]*"));
	}

	public boolean isIntegerGZ (String... str){
		if (str.length==0) return false;
		for (String s : str)
			if (!s.matches("[1-9]+[0-9]*")) return false;
		return true;
	}


}


