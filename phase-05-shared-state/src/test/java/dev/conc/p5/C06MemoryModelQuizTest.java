package dev.conc.p5;

import static org.junit.jupiter.api.Assertions.*;
import static dev.conc.p5.Answer.GUARANTEED;
import static dev.conc.p5.Answer.NOT_GUARANTEED;

import org.junit.jupiter.api.Test;

class C06MemoryModelQuizTest {

    private static final Answer[] EXPECTED = {
        NOT_GUARANTEED, GUARANTEED, GUARANTEED, NOT_GUARANTEED, GUARANTEED,
        NOT_GUARANTEED, GUARANTEED, GUARANTEED, NOT_GUARANTEED, GUARANTEED
    };

    @Test
    void answers() {
        for (int i = 1; i <= MemoryModelQuiz.SCENARIOS; i++) {
            assertEquals(EXPECTED[i - 1], MemoryModelQuiz.answer(i), "scenario " + i);
        }
    }

    @Test
    void everyAnswerHasARealReason() {
        for (int i = 1; i <= MemoryModelQuiz.SCENARIOS; i++) {
            String reason = MemoryModelQuiz.reason(i);
            assertNotNull(reason, "scenario " + i);
            assertTrue(reason.strip().length() >= 30, "scenario " + i + ": say which rule applies, in a sentence");
        }
    }

    @Test
    void outOfRangeScenariosAreRejected() {
        assertThrows(IllegalArgumentException.class, () -> MemoryModelQuiz.answer(0));
        assertThrows(IllegalArgumentException.class, () -> MemoryModelQuiz.answer(MemoryModelQuiz.SCENARIOS + 1));
        assertThrows(IllegalArgumentException.class, () -> MemoryModelQuiz.reason(0));
        assertThrows(IllegalArgumentException.class, () -> MemoryModelQuiz.reason(MemoryModelQuiz.SCENARIOS + 1));
    }
}
