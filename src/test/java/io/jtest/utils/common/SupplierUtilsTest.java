package io.jtest.utils.common;

import org.junit.Ignore;
import org.junit.Test;

@Ignore
public class SupplierUtilsTest {

    @Test(expected = RuntimeException.class)
    public void testActionRepeatWithRunnable() {
        SupplierUtils.pollIfThrows(() -> {
            System.out.println("Do smth");
            throw new IllegalStateException("lorem ipsum");
        }, IllegalStateException.class, 5, 200, 1.0);
    }

    @Test(expected = RuntimeException.class)
    public void testActionRepeatWithSupplier() {
        SupplierUtils.pollIfThrows(() -> {
            System.out.println("Do smth");
            return 9 / 0;
        }, ArithmeticException.class, 5, 200, 1.0);
    }

    @Test(expected = ArithmeticException.class)
    public void testActionRepeatWithSupplier_negative() {
        SupplierUtils.pollIfThrows(() -> {
            System.out.println("Do smth");
            return 9 / 0;
        }, IllegalStateException.class, 5, 200, 1.0);
    }
}
