package com.ark.funds;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.ark.funds.model.Direction;
import com.ark.funds.model.TransactionType;
import org.junit.jupiter.api.Test;

class TransactionTypeTest {
    @Test
    void directionsMatchBusinessRules() {
        assertEquals(Direction.CREDIT, TransactionType.CONTRIBUTION.getDirection());
        assertEquals(Direction.CREDIT, TransactionType.INTEREST_INCOME.getDirection());
        assertEquals(Direction.DEBIT, TransactionType.DISTRIBUTION.getDirection());
        assertEquals(Direction.DEBIT, TransactionType.GENERAL_EXPENSE.getDirection());
        assertEquals(Direction.DEBIT, TransactionType.MANAGEMENT_FEE.getDirection());
    }
}
