package entities;

import java.io.File;
import java.io.IOException;
import java.util.Map.Entry;
import java.util.Set;

import org.ini4j.Ini;
import org.ini4j.Profile.Section;
import org.lwjgl.util.vector.Vector3f;

import renderEngine.Loader;
import renderEngine.ModelCache;
import world.World;

public class EntityFactory {

	private World world;
	private Loader loader;
	private ModelCache modelCache;
	
	public EntityFactory(World world, Loader loader, ModelCache modelCache){
		this.world = world;
		this.loader = loader;
		this.modelCache = modelCache;
	}
	
	public Entity createEntity(String className, Vector3f position, float rotX, float rotY, float rotZ, float xSize, float ySize, float zSize, float scale){
		
		Entity e = new Entity(world, modelCache.loadModel(className), position, rotX,rotY,rotZ, xSize, ySize, zSize, 1.0f);
		e.setClassName(className);
		foo(e);
		return e;
	}
	
	
	
	public void foo(Entity entity){
	
		//System.out.println("Reading test.ini...");
		try{
			Ini ini = new Ini(new File("res/tank.ini"));
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
		}
		catch(IOException e){
			System.err.println("Can't open the ini file!");
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
				entity.setName(val);
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
