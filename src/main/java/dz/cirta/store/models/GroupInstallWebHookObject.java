package dz.cirta.store.models;

/**
 * @author Abdessamed Diab
 */
public class GroupInstallWebHookObject {
   public String object;
   public Entry[] entry;

   public GroupInstallWebHookObject() {
   }

   @Override
   public String toString() {
      return getClass().getSimpleName() +
            " : " +
            "object: " + object;
   }

   public static class GroupRef {
      public String groupId;
      public String updateTime;
      public String verb;
      public String actorId;

      public GroupRef() {
      }
   }

   public static class Entry {
      public String id;
      public long time;
      public Changes[] changes;

      public Entry() {
      }
   }

   public static class Changes {
      public String field;
      public GroupRef value;

      public Changes() {
      }
   }


}
