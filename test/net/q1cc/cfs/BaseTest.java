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

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 73 * hash + (this.name != null ? this.name.hashCode() : 0);
            hash = 73 * hash + (this.color != null ? this.color.hashCode() : 0);
            hash = 73 * hash + this.age;
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final Potato other = (Potato) obj;
            if ((this.name == null) ? (other.name != null) : !this.name.equals(other.name)) {
                return false;
            }
            if ((this.color == null) ? (other.color != null) : !this.color.equals(other.color)) {
                return false;
            }
            if (this.age != other.age) {
                return false;
            }
            return true;
        }

        public void setName(String name) {
            this.name = name;
        }
    }
    
    class SweetPotato extends Potato {
        public float sweetness;

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 37 * hash + Float.floatToIntBits(this.sweetness);
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final SweetPotato other = (SweetPotato) obj;
            if (Float.floatToIntBits(this.sweetness) != Float.floatToIntBits(other.sweetness)) {
                return false;
            }
            return true;
        }
    }
}