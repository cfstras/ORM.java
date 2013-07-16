package net.q1cc.cfs;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 *
 * @author cfstras
 */
public class ORM {
    
    private static HashMap<Class, ArrayList<Field>> classes = 
            new HashMap<Class, ArrayList<Field>>();
    
    private static Connection conn;
    private static Map<String, Class<?>> typeMap;
    private static Map<Class<?>, String> typeRMap;

    /**
     * Do not instance me
     */
    private ORM(){}
    
    public static void connect(String driver, String uri,
            String username, String password) throws ORMException {
        //TODO check if already connected
        //TODO provide disconnect method
        try {
            Class.forName(driver);
            conn = DriverManager.getConnection(uri, username, password);
            initTypeMap();
        } catch (ClassNotFoundException ex) {
            throw new ORMRuntimeException("JDBC Driver could not be loaded", ex);
        } catch (SQLException ex) {
            throw new ORMException("Could not connect to database", ex);
        }
    }
    
    private static void initTypeMap() throws SQLException {
        typeMap = conn.getTypeMap();
        if(typeMap == null) {
            typeMap = new HashMap<String, Class<?>>();
            conn.setTypeMap(typeMap);
        }
        typeMap.put("text", String.class);
        
        typeMap.put("real", Float.class);
        typeMap.put("double precision", Double.class);
        typeMap.put("char", Character.class);
        typeMap.put("integer", Integer.class);
        typeMap.put("bigint", Long.class);
        typeMap.put("smallint", Short.class);
        typeMap.put("smallint", Byte.class);
        typeMap.put("real", Float.TYPE);
        typeMap.put("double precision", Double.TYPE);
        typeMap.put("char", Character.TYPE);
        typeMap.put("integer", Integer.TYPE);
        typeMap.put("bigint", Long.TYPE);
        typeMap.put("smallint", Short.TYPE);
        typeMap.put("smallint", Byte.TYPE);
        
        typeRMap = new HashMap<Class<?>, String>();
        for(Entry<String, Class<?>> e : typeMap.entrySet()) {
            typeRMap.put(e.getValue(), e.getKey());
        }
    }
    
    /**
     * Base class to inherit objects you want to save.
     */
    public static class Model<T extends Model> {
        
        private long id = -1;
        private final ArrayList<Field> fields;
        private final Class clazz;
        private final PreparedStatement[] queries;
        
        /**
         * If this is a raw instance of Model, without an inheriting Class to
         * save, don't allow save methods.
         */
        private final boolean allowSave;
        
        public Model() {
            clazz = getClass();
            allowSave = true;
            queries = new PreparedStatement[Queries.values().length];
            fields = init(getClass());
        }
        
        public Model(Class clazz) {
            this.clazz = clazz;
            allowSave = false;
            queries = new PreparedStatement[Queries.values().length];
            fields = init(clazz);
        }
        
        private ArrayList<Field> init(Class clazz) {
            if(clazz == Model.class) {
                throw new ORMRuntimeException("Creating an instance of a Model "
                        + "is not allowed; create a subclass of Model which can"
                        + " then be used.");
            }
            if(conn == null) {
                throw new ORMRuntimeException("Can't create a Model without an "
                        + "existing database connection");
            }
            ArrayList<Field> nfields = classes.get(clazz);
            // check if this class was analyzed yet
            if(nfields == null) {
                nfields = getFields(clazz);
                classes.put(clazz, nfields);
                try {
                    // also check whether we already have a table for this class
                    createTable(clazz, nfields);
                    //TODO also cache the statements
                    prepareStatements(clazz, nfields);
                } catch (SQLException ex) {
                    throw new ORMRuntimeException("Error while creating table",ex);
                }
            }
            return nfields;
        }
        
        public long save() {
            PreparedStatement st = null;
            ResultSet generatedKeys = null;
            try {
                if(!allowSave) {
                    throw new ORMRuntimeException("You can't save a base Model");
                }
                st = queries[Queries.INSERT.ordinal()];
                st.clearParameters();
                
                int i = 0;
                for(Field f : fields) {
                    st.setObject(++i, f.get(this));
                }
                int affectedRows = st.executeUpdate();
                if (affectedRows == 0) {
                    throw new ORMRuntimeException("Creating object failed, no rows affected.");
                }

                generatedKeys = st.getGeneratedKeys();
                if (generatedKeys.next()) {
                    id = generatedKeys.getLong(1);
                } else {
                    throw new ORMRuntimeException("Creating object failed, no generated key obtained.");
                }
                return id;
            } catch (SQLException ex) {
                throw new ORMRuntimeException("Error saving object", ex);
            } catch (IllegalArgumentException ex) {
                throw new ORMRuntimeException("Error saving object", ex);
            } catch (IllegalAccessException ex) {
                throw new ORMRuntimeException("Error saving object", ex);
            } finally {
                if (generatedKeys != null) try { generatedKeys.close(); } catch (SQLException ignore) {}
                if (st != null) try { st.close(); } catch (SQLException ignore) {}
            }
        }
        
        public void prepareStatements(Class<?> clazz, ArrayList<Field> fields)
                throws SQLException {
            // inserting a new object
            StringBuilder q = new StringBuilder();
            q.append("INSERT INTO \"").append(sqlFriendly(clazz.getSimpleName()))
                    .append("\" (");
            appendSQLFields(fields, q, false);
            q.append(") VALUES (");
            for(int i=fields.size(); i>0; i--) {
                q.append("?");
                if(i>1) q.append(", ");
            }
            q.append(");");
            System.out.println(q.toString());
            PreparedStatement stmt = conn.prepareStatement(q.toString(), Statement.RETURN_GENERATED_KEYS);
            queries[Queries.INSERT.ordinal()] = stmt;
            
            // finding an object
            q.setLength(0);
            q.append("SELECT * FROM \"").append(sqlFriendly(clazz.getSimpleName()))
                    .append("\" WHERE id = ?;");
            System.out.println(q.toString());
            stmt = conn.prepareStatement(q.toString(), Statement.RETURN_GENERATED_KEYS);
            queries[Queries.FIND.ordinal()] = stmt;
        }
        
        public T find(long id) {
            return new Query<T>(clazz).find(id);
        }
        public T first() {
            return new Query<T>(clazz).first();
        }
        public T last() {
            return new Query<T>(clazz).last();
        }
        public ArrayList<T> all() {
            return new Query<T>(clazz).all();
        }
        public long count() {
            return new Query<T>(clazz).count();
        }
    }
    
    private static ArrayList<Field> getFields(Class<?> clazz) {
        ArrayList<Field> fields = new ArrayList<Field>();
        do {
            Field[] currentFields = clazz.getDeclaredFields();
            for(Field f : currentFields) {
                int mod = f.getModifiers();
                if (Modifier.isTransient(mod) || f.getName().matches("this\\$\\d+")) {
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
            ArrayList<Field> fields) throws SQLException {
        Statement stmt = conn.createStatement();
        StringBuilder q = new StringBuilder();
        q.append("CREATE TABLE IF NOT EXISTS \"").append(sqlFriendly(
                clazz.getSimpleName())).append("\" (\n").append(
                "id SERIAL PRIMARY KEY,\n");
        appendSQLFields(fields, q, true);
        q.append(");");
        System.out.println("SQL: "+q.toString());
        stmt.execute(q.toString());
    }
    
    private static String sqlFriendly(String s) {
        return s.toLowerCase();
    }
    
    private static void appendSQLFields(ArrayList<Field> fields, StringBuilder q, boolean types) {
        boolean first = true;
        for(Field f : fields) {
            if(!first) {
                q.append(",\n");
            }
            first = false;
            q.append("\"").append(sqlFriendly(f.getName())).append("\" ");
            if(types) {
                String sqlType = ORM.typeRMap.get(f.getType());
                if(sqlType == null) {
                    throw new ORMRuntimeException("No SQL Type found for "+f.getType());
                }
                q.append(sqlType);
            }
        }
    }
    
    private static void printResult(ResultSet rs) throws SQLException {
        for(int i=0;i<rs.getMetaData().getColumnCount();i++) {
            System.out.print(rs.getMetaData().getColumnName(i)+"\t");
        }
        System.out.println();
        for(;rs.next();) {
            for(int i=0;i<rs.getMetaData().getColumnCount();i++) {
                System.out.print(rs.getObject(i)+"\t");
            }
            System.out.println();
        }
    }
    
    public static class Query<T extends Model> {
        
        private final Class clazz;
        
        public Query(Class clazz) {
            this.clazz = clazz;
        }
        
        public T find(long id) {
            //TODO
            return null;
        }
        
        public T first() {
            //TODO
            System.out.println(clazz.getSimpleName()+".first");
            return null;
        }
        
        public T last() {
            //TODO
            return null;
        }
        
        public long count() {
            //TODO
            return 0;
        }
        
        public ArrayList<T> all() {
            //TODO
            return null;
        }
    }
    
    private enum Queries {
        INSERT,
        UPSERT,
        DESTROY,
        FIND,
        FIRST,
        LAST
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
