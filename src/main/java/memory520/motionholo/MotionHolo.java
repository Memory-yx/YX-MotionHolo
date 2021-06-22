package memory520.motionholo;

import com.gmail.filoghost.holographicdisplays.api.Hologram;
import com.gmail.filoghost.holographicdisplays.api.HologramsAPI;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.serverct.ersha.jd.*;
import org.serverct.ersha.jd.attribute.AttributeData;
import org.serverct.ersha.jd.event.AttrEntityDamageEvent;

public final class MotionHolo extends JavaPlugin implements Listener {

    Plugin Holo = Bukkit.getServer().getPluginManager().getPlugin("HolographicDisplays");

    @Override
    public void onEnable() {
        System.out.println("# +----------------------------+");
        System.out.println("# |");
        System.out.println("# |   §bYX-MotionHolo");
        System.out.println("# |   #载入成功");
        System.out.println("# |");
        System.out.println("# |   载入前置插件");
        if(Bukkit.getPluginManager().isPluginEnabled("AttributePlus")){
            System.out.println("# |   | "+Bukkit.getPluginManager().getPlugin("AttributePlus")+" §6完成");
        }else{
            System.out.println("# |   | AttributePlus §c失败");
        }
        if(Bukkit.getPluginManager().isPluginEnabled("HolographicDisplays")){
            System.out.println("# |   | "+Bukkit.getPluginManager().getPlugin("HolographicDisplays")+" §6完成");
        }else{
            System.out.println("# |   | HolographicDisplays §c失败");
        }
        System.out.println("# |");
        System.out.println("# |   | §eby.Memory520");
        System.out.println("# |   | §eQQ:3332397782");
        System.out.println("# |");
        System.out.println("# +-------");
        System.out.println("");

        Bukkit.getServer().getPluginManager().registerEvents(this, this);
        this.Holo = Bukkit.getServer().getPluginManager().getPlugin("HolographicDisplays");
        // 保存config.yml至插件文件夹
        saveDefaultConfig();
        super.onEnable();
    }

    @EventHandler
    //造成伤害时触发事件(此处用的是AP的伤害事件)
    public void onDamage(AttrEntityDamageEvent ae) {
        Entity entity = (Entity) ae.getDamager();
        if(!(entity instanceof Player)) return;
        Player player = (Player) entity;

        calculation(player,ae);
    }

    public void calculation(Player player,AttrEntityDamageEvent ae){
        //读取配置文件
        FileConfiguration config = getConfig();
        //实例化属性对象
        AttributeData data = AttributeAPI.getAttrData(player);

        //int Damage_Normal = (int)data.getAttributeValue("基准伤害");
        //String Damage_Normal_String = "";
        //if(Damage_Normal>0){
        //    Damage_Normal_String = "§7基准 "+data.getAttributeValue("基准伤害全息值")+" ";
        //}

        //声明一个储存文本的变量
        String DamageText = "";
        for (int i=1;i<=Integer.parseInt(config.getString("DamagType.DamageList"));i++){
            //识别config文件中的属性名称,并读取该属性的值
            int DamageName = (int)data.getAttributeValue(config.getString("DamagType.Damage"+i+".DamageName"));
            //如果玩家身上存在该属性值,那么就给全息变量添加新的文本
            if(DamageName > 0){
                //读取config文件中的造成实际伤害的属性,并转化为该属性的值
                int DamageValue = (int)data.getAttributeValue(config.getString("DamagType.Damage"+i+".DamageValue"));
                //在DamageText变量中,添加实际显示的全息文本
                DamageText = DamageText+config.getString("DamagType.Damage"+i+".DamageText").replace("&", "§")+DamageValue+" ";
            }
        }

        //在聊天栏显示造成伤害的属性
        //player.sendRawMessage("读取属性: "+Damage_Normal_String+Damage_Fire_String+Damage_Water_String+Damage_Wind_String+Damage_Poison_String+Damage_Electric_String);

        //创建一个全息图的位置属性
        Location HoloPosition = ((LivingEntity)ae.getEntity()).getEyeLocation();
        //创建一个全息图显示文本
        Hologram HoloText = HologramsAPI.createHologram(this.Holo, HoloPosition);
        HoloText.appendTextLine(DamageText);
        //声明最大与最小偏移值变量,并计算XZ轴随机偏移值
        double RandMax = Double.parseDouble(config.getString("Rand.RandMax"));
        double RandMin = Double.parseDouble(config.getString("Rand.RandMin"));
        double RandX = (double) (Math.random()*(RandMax-RandMin)+RandMin)/5;
        double RandZ = (double) (Math.random()*(RandMax-RandMin)+RandMin)/5;
        //增加全息文本的偏移位置,实现随机方向的上浮
        new BukkitRunnable() {
            int RunTick;
            @Override
            public void run() {
                RunTick++;
                if(RunTick <= Integer.parseInt(config.getString("RunTick"))){
                    HoloText.teleport(HoloText.getLocation().add(RandX,Double.parseDouble(config.getString("Rand.TextRise")),RandZ));
                }else if(RunTick > Integer.parseInt(config.getString("RunTick"))){
                    HoloText.delete();
                    cancel();
                }
            }
        }.runTaskTimerAsynchronously(Holo, 0, 1);;

    }

    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("reload")) {
            sender.hasPermission("MotionHolo.admin");
            reloadConfig();
            sender.sendMessage("§aYX-MotionHolo 重载完成!");
        }
        return false;
    }

    @Override
    public void onDisable() {
        Bukkit.getScheduler().cancelTasks(Holo);
        // Plugin shutdown logic
        super.onDisable();
    }
}
