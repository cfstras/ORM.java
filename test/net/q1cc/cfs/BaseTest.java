package net.q1cc.cfs;

import org.junit.After;
import org.junit.AfterClass;
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
    public void setUp() {
    }
    
    @After
    public void tearDown() {
    }
    
    @Test
    public void dev() {        
        Potato p =  new Potato();
        p.setName("Peter");
        p.color = "slightly grey";
        p.age = 2;
        p.save();
        
        SweetPotato p2 =  new SweetPotato();
        p2.setName("Peter");
        p2.color = "slightly grey";
        p2.age = 2;
        p2.sweetness = 1.0f;
        p2.save();
    }
    
    static class Potato extends ORM.Model {
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