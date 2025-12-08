package com.midscene.core.utils;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;

class WaitingUtilsTest {

  @Test
  void testWaitUntilSuccess() {
    AtomicInteger counter = new AtomicInteger(0);
    WaitingUtils.waitUntil(2, 100, () -> {
      return counter.incrementAndGet() > 2;
    }, "testWaitUntilSuccess");
    assertTrue(counter.get() > 2);
  }

  @Test
  void testWaitUntilTimeout() {
    assertThrows(Exception.class, () -> {
      WaitingUtils.waitUntil(1, 100, () -> false, "testWaitUntilTimeout");
    });
  }

  @Test
  void testWaitUntilPredicateSuccess() {
    AtomicInteger counter = new AtomicInteger(0);
    int result = WaitingUtils.waitUntilPredicate(2, 100,
        counter::incrementAndGet,
        val -> val > 2,
        "testWaitUntilPredicateSuccess");
    assertTrue(result > 2);
  }

  @Test
  void testWaitUntilWithoutException() {
    // Should not throw exception
    WaitingUtils.waitUntilWithoutException(1, 100, () -> false, "testWaitUntilWithoutException");
  }
}
