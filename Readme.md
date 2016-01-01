ORM.java
========

is a lightweight and easy-to-use ORM library for all JDBC-compatible Databases.  
It is intended to be used for Programs with mostly simple queries for
readability and extendability. No more lists of hundreds of `PreparedStatement`s!  
Simply have your Classes `extend ORM.Model`, then `.save()` them and later `.find(id)` them!

Why?
----

Have you ever found yourself searching for a simple, but performant solution
to serialize your objects? *Search no more.*

Status
------

Implemented are basic save() and find() methods. This library is more to be seen as a concept of how one could implement an ORM using basic reflection in Java. I do not recommend it as a production library, but it can be a good learning experience.  
However, you are welcome to help by submitting issues or pull requests.

How?
----

Here is a basic usage example:

```java
import net.q1cc.cfs.ORM;

public class ORMTest {
    public static void main(String[] args) {
        /* Initialize ORM */
        ORM.connect("org.postgresql.Driver",
                    "jdbc:postgresql://localhost/ormtest",
                    "username", "password");

        /* Create a new Potato: */
        Potato p = new Potato("Agata", 0);
        /* Give her a name: */
        p.name = "Nadine";
        /* Save her. The returned long is the ID in the database. */
        long id = p.save();

        /* Retrieve Nadine from the database */
        PotatoModel potatoModel = new Model<Potato>(Potato.class);
        Potato p2 = potatoModel.find(id);
        System.out.println(p2.name); // "Nadine"
        System.out.println(p2.getVariety()); // "Agata"

        /* Subclasses work, too! */
        SweetPotato p = new SweetPotato("Bienville", 2);
        p.name = "Nadina";
        /* Save her. The returned long is the ID in the database. */
        long id = p.save();

        /* Retrieve Nadina from the database */
        SweetPotatoModel sweetPotatoModel = new Model<SweetPotato>(SweetPotato.class);
        SweetPotato p2 = sweetPotatoModel.find(id);
        System.out.println(p2.name); // "Nadina"
        System.out.println(p2.getVariety()); // "Bienville"
    }
}

/**
 * An example class to save
 */
public class Potato extends ORM.Model {
    /* You can use public attributes */
    public String name;

    /* As well as private ones */
    private String variety;

    /* Primitive Types work, too */
    private int age;

    /* Basic constructor and getters */
    public Potato(String variety, int age) {
        this.variety = variety;
        this.age = age;
    }

    public String getVariety() {
        return variety;
    }

    public int getAge() {
        return age;
    }
}

/**
 * Subclasses get a seperate Table
 */
public class SweetPotato extends Potato {

    /* This field will be saved along with all inherited fields */
    private float sweetness;

    /* transient fields will not be saved */
    private transient boolean hasBeenEaten;

    /* Basic constructor and getters */
    public Potato(String variety, int age, float sweetness) {
        super(variety, age);
        this.sweetness = sweetness;
    }

    public String getVariety() {
        return variety;
    }

    public int getAge() {
        return age;
    }
}
```

License
-------

ORM.java is released under the MIT License. For details please refer to
`License.md`.
