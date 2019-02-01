package test.poulet;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertTrue;
import poulet.Main;

public class TestMain {
    @Test
    void test() {
        assertTrue(Main.test());
    }
}
