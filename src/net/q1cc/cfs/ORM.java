package net.q1cc.cfs;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;

/**
 *
 * @author cfstras
 */
public class ORM {
    
    private static HashMap<Class, ArrayList<Field>> classes = 
            new HashMap<Class, ArrayList<Field>>();
    
    private static Connection conn;
    
    /**
     * Do not instance me
     */
    private ORM(){}
    
    public static void connect(String driver, String uri,
            String username, String password) throws ORMException {
        //TODO check if already connected
        //TODO provide disconnect method
        try {
            Class.forName("org.postgresql.Driver");
            conn = DriverManager.getConnection(
                    "jdbc:postgresql://hostname:port/dbname", "username", "password");
            conn.close();
        } catch (ClassNotFoundException ex) {
            throw new ORMRuntimeException("JDBC Driver could not be loaded", ex);
        } catch (SQLException ex) {
            throw new ORMException("Could not connect to database", ex);
        }
    }
    
    /**
     * Base class to inherit objects you want to save.
     */
    public static class Model {
        
        private long id = -1;
        private ArrayList<Field> fields;
        
        public Model() {
            Class clazz = this.getClass();
            if(clazz == Model.class) {
                throw new ORMRuntimeException("Creating an instance of a Model is not allowed; "
                        + "create a subclass of Model which can then be used.");
            }
            fields = classes.get(clazz);
            // check if this class was analyzed yet
            if(fields == null) {
                fields = getFields(clazz);
                classes.put(clazz, fields);
                
                // also check whether we already have a table for this class
                createTable(clazz, fields);
            }
        }
        
        public long save() {
            //TODO
            return id;
        }
        
        public static Query find(long id) {
            return new Query().find(id);
        }
    }
    
    private static ArrayList<Field> getFields(Class<?> clazz) {
        ArrayList<Field> fields = new ArrayList<Field>();
        do {
            Field[] currentFields = clazz.getDeclaredFields();
            for(Field f : currentFields) {
                int mod = f.getModifiers();
                if (Modifier.isTransient(mod)) {
                    continue;
                }
                if(Modifier.isPrivate(mod)) {
                    f.setAccessible(true);
                }
                fields.add(f);
            }
            clazz = clazz.getSuperclass();
        } while (Model.class.isAssignableFrom(clazz) && Model.class != clazz);
        
        return fields;
    }
    
    private static void createTable(Class<? extends Model> clazz,
            ArrayList<Field> fields) {
        
    }
    
    public static class Query<T extends Model> {
        private Query() {
            //TODO
        }
        
        public Query find(long id) {
            //TODO
            return this;
        }
        
        public T first() {
            //TODO
            return null;
        }
        
        public ArrayList<T> array() {
            //TODO
            return null;
        }
    }
    
    /**
     * ORMException
     */
    public static class ORMException extends Exception {
        public ORMException() {
        }
        public ORMException(String message) {
            super(message);
        }
        public ORMException(String message, Throwable cause) {
            super(message, cause);
        }
        public ORMException(Throwable cause) {
            super(cause);
        }
    }
    
    /**
     * ORMRuntimeException
     */
    public static class ORMRuntimeException extends RuntimeException {
        public ORMRuntimeException() {
        }
        public ORMRuntimeException(String message) {
            super(message);
        }
        public ORMRuntimeException(String message, Throwable cause) {
            super(message, cause);
        }
        public ORMRuntimeException(Throwable cause) {
            super(cause);
        }
    }
}
