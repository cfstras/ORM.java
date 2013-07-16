package net.q1cc.cfs;

import net.q1cc.cfs.ORM.Model;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author cfstras
 */
public class BaseTest {
    
    public BaseTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() throws ORM.ORMException {
        ORM.connect("org.postgresql.Driver",
                "jdbc:postgresql://localhost/ORMTest",
                "ORMTest", "ORMTest");
    }
    
    @After
    public void tearDown() {
    }
    
    @Test
    public void dev() {
        Potato p = new Potato();
        p.setName("Peter");
        p.color = "slightly grey";
        p.age = 2;
        long id = p.save();
        
        SweetPotato p2 = new SweetPotato();
        p2.setName("Peter");
        p2.color = "slightly grey";
        p2.age = 2;
        p2.sweetness = 1.0f;
        p2.save();
        
        Model<Potato> potatoModel = new Model<Potato>(Potato.class);
        assertEquals("Basic test of save+first", p, potatoModel.find(id));
    }
    
    static class Potato extends Model {
        private String name;
        public String color;
        public int age;
        
        public void setName(String name) {
            this.name = name;
        }
    }
    
    class SweetPotato extends Potato {
        public float sweetness;
    }
}