package net.md_5;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Queue;
import java.util.Random;
import java.util.logging.Level;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

public class DropParty extends JavaPlugin
{

    private final Random random = new Random();
    private final List<ItemStack> dropItems = new ArrayList<ItemStack>();

    @Override
    public void onEnable()
    {
        getConfig().addDefault( "start_message", "&7A drop party has begun!" );
        getConfig().addDefault( "give_message", "&9Wow you just got free stuff!" );
        getConfig().addDefault( "end_message", "&8Oh no, the party is over!" );
        getConfig().addDefault( "items", Arrays.asList( "DIAMOND_SWORD", "DIAMOND", "IRON_INGOT", "GOLD_INGOT" ) );
        getConfig().addDefault( "interval", 60 );
        getConfig().addDefault( "duration", 180 );

        getConfig().options().copyDefaults( true );
        saveConfig();

        for ( String s : getConfig().getStringList( "items" ) )
        {
            String[] split = s.split( ":", 2 );
            String name = split[0];
            int amount = split.length > 1 ? Integer.parseInt( split[1] ) : 1;

            Material material = Material.matchMaterial( name );
            if ( material == null )
            {
                getLogger().log( Level.WARNING, "Could not find material {0}", name );
            }
            dropItems.add( new ItemStack( material, amount ) );
        }

        int intervalTicks = getConfig().getInt( "interval" ) * 60 * 20;

        getServer().getScheduler().scheduleSyncRepeatingTask( this, new PartySchedule(), intervalTicks, intervalTicks );
    }

    private class PartySchedule implements Runnable
    {

        @Override
        public void run()
        {
            getServer().broadcastMessage( ChatColor.translateAlternateColorCodes( '&', getConfig().getString( "start_message" ) ) );
            new PartyTime();
        }
    }

    private class PartyTime implements Runnable
    {

        private final int taskId;
        private final Queue<Player> playersToGive;

        public PartyTime()
        {
            playersToGive = new ArrayDeque<Player>( Arrays.asList( getServer().getOnlinePlayers() ) );
            int approxInterval = getConfig().getInt( "duration" ) / playersToGive.size(); // In seconds
            taskId = getServer().getScheduler().scheduleSyncRepeatingTask( DropParty.this, this, 0, approxInterval * 20 );
        }

        @Override
        public void run()
        {
            Player player = playersToGive.poll();

            if ( player != null && player.isOnline() )
            {

                player.sendMessage( ChatColor.translateAlternateColorCodes( '&', getConfig().getString( "give_message" ) ) );
                player.getInventory().addItem( dropItems.get( random.nextInt( dropItems.size() ) ) );
            }

            if ( playersToGive.isEmpty() )
            {
                getServer().getScheduler().cancelTask( taskId );
                getServer().broadcastMessage( ChatColor.translateAlternateColorCodes( '&', getConfig().getString( "end_message" ) ) );
            }
        }
    }
}
