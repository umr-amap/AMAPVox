/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.amapvox.commons.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author calcul
 */
public class CommandLineParser {
    
    private final Map<String, String> named;
    private final List<String> unnamed;
    private final List<String> raw;

    public CommandLineParser(String[] args) {
        
        named = new HashMap<>();
        unnamed = new ArrayList<>();
        raw = new ArrayList<>(args.length);
        
        for(String s : args){
            
            if(s.contains("=")){
                String[] split = s.split("=");
                if(split.length == 2){
                    
                    if(split[0].length() > 2){
                        
                        String name = split[0].substring(2);
                        String value = split[1];
                        named.put(name, value);
                    }
                }
            }else{
                unnamed.add(s);
            }
            
            raw.add(s);
        }
    }

    public Map<String, String> getNamed() {
        return named;
    }

    public List<String> getUnnamed() {
        return unnamed;
    }

    public List<String> getRaw() {
        return raw;
    }
    
}
