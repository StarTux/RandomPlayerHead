package com.winthier.rph;

import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class RPHAPI {

    // Get a random Player head
    // returns null if plugin is not enabled
    public static ItemStack getRandomPlayerHead(){
        return getPlugin() == null ? null : getPlugin().randomHead().toItemStack();
    }

    // Get a list of heads that include a searchTerm
    // returns null if plugin is not enabled
    public static List<ItemStack> findHeads(String searchTerm){
        if(getPlugin() == null ) return null;
        List<ItemStack> heads = new ArrayList<>();
        for(Head head : getPlugin().findHeads(searchTerm)){
            heads.add(head.toItemStack());
        }
        return heads;
    }

    // Get a list of heads that match exactly a searchTerm
    // returns null if plugin is not enabled
    public static List<ItemStack> findHeadsExact(String searchTerm) {
        if(getPlugin() == null ) return null;
        List<ItemStack> heads = new ArrayList<>();
        for (Head head : getPlugin().findHeadsExact(searchTerm)) {
            heads.add(head.toItemStack());
        }
        return heads;
    }

    static RandomPlayerHeadPlugin getPlugin(){
        if(RandomPlayerHeadPlugin.getInstance() == null || !RandomPlayerHeadPlugin.getInstance().isEnabled()) return null;
        return RandomPlayerHeadPlugin.getInstance();
    }
}
