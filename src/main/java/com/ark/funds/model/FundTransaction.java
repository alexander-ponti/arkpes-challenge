package com.ark.funds.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * An investor's transaction against a fund. The amount is always positive;
 * its effect on the fund (credit/debit) is derived from the type.
 * This table is also what links investors and funds (many-to-many).
 */
@Entity
@Table(name = "fund_transactions", indexes = {
        @Index(name = "ix_tx_fund_date", columnList = "fund_id, transactionDate"),
        @Index(name = "ix_tx_investor", columnList = "investor_id")})
public class FundTransaction {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "fund_id")
    private Fund fund;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "investor_id")
    private Investor investor;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 30)
    private TransactionType type;
    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal amount;
    @Column(nullable = false)
    private LocalDate transactionDate;
    @Column(length = 500)
    private String description;

    protected FundTransaction() {}
    public FundTransaction(Fund fund, Investor investor) { this.fund = fund; this.investor = investor; }

    public Long getId() { return id; }
    public Fund getFund() { return fund; }
    public void setFund(Fund fund) { this.fund = fund; }
    public Investor getInvestor() { return investor; }
    public void setInvestor(Investor investor) { this.investor = investor; }
    public TransactionType getType() { return type; }
    public void setType(TransactionType type) { this.type = type; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public LocalDate getTransactionDate() { return transactionDate; }
    public void setTransactionDate(LocalDate d) { this.transactionDate = d; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}
