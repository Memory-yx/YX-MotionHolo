package memory520.motionholo;

import com.gmail.filoghost.holographicdisplays.api.Hologram;
import com.gmail.filoghost.holographicdisplays.api.HologramsAPI;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.serverct.ersha.jd.*;
import org.serverct.ersha.jd.attribute.AttributeData;
import org.serverct.ersha.jd.event.AttrEntityCritEvent;
import org.serverct.ersha.jd.event.AttrEntityDamageEvent;

public final class MotionHolo extends JavaPlugin implements Listener {

    Plugin Holo = Bukkit.getServer().getPluginManager().getPlugin("HolographicDisplays");
    boolean Crit = false;
    int CritRate;
    int OldHealth;

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
    //当暴击时触发事件
    public void onCrit(AttrEntityCritEvent ae){
        Entity entity = (Entity) ae.getDamager();
        if(!(entity instanceof Player)) return;
        Player player = (Player) entity;

        Crit = true;

        AttributeData data = AttributeAPI.getAttrData(player);
        CritRate = (int)data.getAttributeValue("会心倍率");

        String Text = calculation(player,ae.getEntity(),true);

        //调用显示全息图方法
        holo(((LivingEntity)ae.getEntity()).getEyeLocation(),Text);
    }
    @EventHandler
    //造成伤害时触发事件(此处用的是AP的伤害事件)
    public void onDamage(AttrEntityDamageEvent ae) {
        Entity entity = (Entity) ae.getDamager();
        if(!(entity instanceof Player)) return;
        Player player = (Player) entity;

        String Text = calculation(player,ae.getEntity(),false);

        new BukkitRunnable() {
            @Override
            public void run() {
                if(Crit){
                    Crit = false;
                    cancel();
                } else{
                    //调用显示全息图方法
                    holo(((LivingEntity)ae.getEntity()).getEyeLocation(),Text);
                    cancel();
                }
            }
        }.runTaskLater(Holo,1);
    }

    @EventHandler
    //当玩家生命刷新时触发
    public void onRegainHealth(EntityRegainHealthEvent e){
        //读取配置文件
        FileConfiguration config = getConfig();

        if(!(e.getEntity() instanceof Player)) return;
        Player player = (Player) e.getEntity();

        //声明一个血量变量
        int HealthValue = OldHealth - (int)player.getHealth();
        String Text = "";
        //当血量小于0时显示生命恢复
        if(HealthValue < 0){
            Text = config.getString("RegainHealthText").replace("&", "§")+Math.abs(HealthValue);
        }
        //当血量大于0时显示失去生命
        else if(HealthValue > 0){
            Text = config.getString("LoseHealthText").replace("&", "§")+Math.abs(HealthValue);
        }
        OldHealth = (int)player.getHealth();

        holo(((LivingEntity)e.getEntity()).getEyeLocation(),Text);
    }

    //玩家伤害文本计算
    public String calculation(Player player, Entity e,Boolean Crit){
        //读取配置文件
        FileConfiguration config = getConfig();
        //实例化属性对象
        AttributeData data = AttributeAPI.getAttrData(player);

        //声明一个储存文本的变量
        String DamageText = "";
        //当暴击时修改文本并播放音效
        if(Crit){
            DamageText = config.getString("CritText").replace("&", "§");
            sound(player,config.getString("CritSound"));
        }

        //遍历config文件中的子级字符串
        for (String s:config.getConfigurationSection("DamagType").getKeys(false)) {
            //识别config文件中的属性名称,并读取该属性的值
            int DamageName = (int)data.getAttributeValue(config.getString("DamagType."+s+".DamageName"));
            //如果玩家身上存在该属性值,那么就给全息变量添加新的文本
            if(DamageName > 0){
                //读取config文件中的造成实际伤害的属性,并转化为该属性的值
                int DamageValue = (int)data.getAttributeValue(config.getString("DamagType."+s+".DamageValue"));
                //暴击时对显示伤害进行计算
                if(Crit){ DamageValue = (int)(DamageValue * (1.5+(CritRate/100))); }
                //在DamageText变量中,添加实际显示的全息文本
                DamageText = DamageText+config.getString("DamagType."+s+".DamageText").replace("&", "§")+DamageValue+" ";
            }
        }
        return DamageText;
    }

    //全息图显示
    public void holo(Location location,String text){
        //读取配置文件
        FileConfiguration config = getConfig();
        //创建一个全息图的位置属性
        Location HoloPosition = location;
        //创建一个全息图显示文本
        Hologram HoloText = HologramsAPI.createHologram(this.Holo, HoloPosition);
        HoloText.appendTextLine(text);
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
        }.runTaskTimerAsynchronously(Holo, 0, 1);
    }

    //播放音效
    public void sound(Player player,String sound){
        player.playSound(player.getLocation(), Sound.valueOf(sound), SoundCategory.PLAYERS, 1, 1);
    }

    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("reload")) {
            sender.hasPermission("MotionHolo.admin");
            saveDefaultConfig();
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
