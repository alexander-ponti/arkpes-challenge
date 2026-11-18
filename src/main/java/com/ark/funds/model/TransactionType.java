package com.ark.funds.model;

public enum TransactionType {
    CONTRIBUTION(Direction.CREDIT),
    INTEREST_INCOME(Direction.CREDIT),
    DISTRIBUTION(Direction.DEBIT),
    GENERAL_EXPENSE(Direction.DEBIT),
    MANAGEMENT_FEE(Direction.DEBIT);

    private final Direction direction;

    TransactionType(Direction direction) { this.direction = direction; }

    public Direction getDirection() { return direction; }
    public boolean isCredit() { return direction == Direction.CREDIT; }
}
