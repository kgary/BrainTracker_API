package edu.asu.epilepsy.apiv30.helper;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * Gson Factory util for json conversion.
 * This factory is a thread safe json converter which converts json to object and vice versa.
 * It does not contain any custom serializers and deserializers yet, all these custom serializers and deserializers should
 * be register in this factory.
 */
public final class GsonFactory {
  private static GsonFactory instance = null;
  private static Gson gson = null;

  /**
   * disabled outer usage for our singleton pattern.
   */
  private GsonFactory(){}

  /**
   * Thread safe singleton getter. The only-way to access gson instance.
   * @return reusable gson instance
   */
  public static synchronized GsonFactory getInstance(){
    if(instance == null){
      GsonBuilder gsonBuilder = new GsonBuilder();
      gson = gsonBuilder.create();
      instance = new GsonFactory();
    }
    return instance;
  }

  /**
   * Get gson converter
   * @return gson
   */
  public Gson getGson(){
    return gson;
  }
}
