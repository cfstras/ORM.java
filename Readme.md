ORM.java
========

is a lightweight and easy-to-use ORM library for all JDBC-compatible Databases.
It is intended to be used for Programs with mostly simple queries for
readability and extendability. No more lists of hundreds of `PreparedStatement`!

Why?
----

Have you ever found yourself searching for a simple, but performant solution
to serialize your objects? *Search no more.*

Status
------

As of now, ORM.java is **not yet** implemented. Stay tuned for updates.  
Implementation is going on in my private time, so don't rush me.
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
        Potato p2 = Model.find(id, Potato.class).first();
        System.out.println(p2.name); // "Nadine"
        System.out.println(p2.getVariety()); // "Agata"

        /* Subclasses work, too! */
        SweetPotato p = new SweetPotato("Bienville", 2);
        p.name = "Nadina";
        /* Save her. The returned long is the ID in the database. */
        long id = p.save();

        /* Retrieve Nadina from the database */
        SweetPotato p2 = Model.find(id, SweetPotato.class).first();
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
