package entities;

import java.io.File;
import java.io.IOException;
import java.util.Map.Entry;
import java.util.Set;

import org.ini4j.Ini;
import org.ini4j.Profile.Section;

public class EntityIniLoader {
	public boolean load(Entity entity, String iniFileName){
		try{
			Ini ini = new Ini(new File(iniFileName));
			for(Entry<String, Section> e : ini.entrySet()){
				String sectionName = e.getKey();
				Section section = e.getValue();
				
				if(sectionName.equals("Entity")){
					processEntitySection(entity, section.entrySet());
				}
				else if(sectionName.equals("HoverCraft")){
					processHoverCraftSection(entity, section.entrySet());
				}
				else if(sectionName.equals("Weapon")){
					processWeaponSection(entity, section.entrySet());
				}
				
			}
			return true;
		}
		catch(IOException e){
			System.err.println("EntityIniLoader: Can't open file '"+iniFileName+"'!");
			return false;
		}
	}
	
	public void processEntitySection(Entity entity, Set<Entry<String, String>> values){
		for(Entry<String, String> ent : values){
			//System.out.println(ent.getKey() + " : " + ent.getValue());
			if(ent.getKey().equals("maxHealth")){
				int health = readIniInt(ent.getKey(), ent.getValue(), 100);
				entity.setMaxHealth(health);
				entity.setHealth(health);
			}
			else if(ent.getKey().equals("name")){
				String val = readIniString(ent.getKey(), ent.getValue(), entity.getClassName());
				entity.setDisplayName(val);
			}
		}
	}
	
	public void processHoverCraftSection(Entity entity, Set<Entry<String, String>> values){
		for(Entry<String, String> ent : values){
			//System.out.println(ent.getKey() + " : " + ent.getValue());
		}
	}
	public void processWeaponSection(Entity entity, Set<Entry<String, String>> values){
		//WeaponComponent component = new WeaponComponent(projectileModel, explosionModel, particleTexture, shotDelay, shotSpeed, lifetime);
		for(Entry<String, String> ent : values){
			//System.out.println(ent.getKey() + " : " + ent.getValue());
		}
	}
	
	private int readIniInt(String key, String value, int defaultValue){
		try{
			int i = Integer.valueOf(value);
			return i;
		}catch(NumberFormatException e){
			System.err.println("readIniInt(): failed to read value for '"+key+"'.");
			return defaultValue;
		}
	}
	
	private float readIniFloat(String key, String value, float defaultValue){
		try{
			float f = Float.valueOf(value);
			return f;
		}catch(NumberFormatException e){
			System.err.println("readIniFloat(): failed to read value for '"+key+"'.");
			return defaultValue;
		}
	}
	
	private String readIniString(String key, String value, String defaultValue){
		String[] vals=value.split("\"");
		if(vals.length == 3){
			return vals[1];
		}
		else{
			System.err.println("readIniString): failed to read value for '"+key+"'.");
			return "";
		}
	}
}
