package com.ark.funds.dto;

import com.ark.funds.model.*;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public final class Dtos {
    private Dtos() {}

    // ----- Funds -----
    public record FundRequest(@NotBlank @Size(max = 200) String name, @Size(max = 2000) String description) {}
    public record FundResponse(Long id, String name, String description, Instant createdAt) {
        public static FundResponse from(Fund f) { return new FundResponse(f.getId(), f.getName(), f.getDescription(), f.getCreatedAt()); }
    }

    // ----- Investors -----
    public record InvestorRequest(@NotBlank @Size(max = 200) String name, @NotBlank @Email @Size(max = 320) String email) {}
    public record InvestorResponse(Long id, String name, String email, Instant createdAt) {
        public static InvestorResponse from(Investor i) { return new InvestorResponse(i.getId(), i.getName(), i.getEmail(), i.getCreatedAt()); }
    }

    // ----- Transactions -----
    public record TransactionRequest(
            @NotNull Long fundId,
            @NotNull Long investorId,
            @NotNull TransactionType type,
            @NotNull @Positive @Digits(integer = 15, fraction = 4) BigDecimal amount,
            @NotNull LocalDate transactionDate,
            @Size(max = 500) String description) {}

    public record TransactionResponse(Long id, Long fundId, Long investorId, TransactionType type, Direction direction,
                                      BigDecimal amount, LocalDate transactionDate, String description) {
        public static TransactionResponse from(FundTransaction t) {
            return new TransactionResponse(t.getId(), t.getFund().getId(), t.getInvestor().getId(), t.getType(),
                    t.getType().getDirection(), t.getAmount(), t.getTransactionDate(), t.getDescription());
        }
    }

    // ----- Reports -----
    public record InvestorPosition(Long investorId, String investorName, BigDecimal totalCredits, BigDecimal totalDebits, BigDecimal netBalance) {}

    public record FundReport(Long fundId, String fundName, LocalDate from, LocalDate to,
                             BigDecimal totalCredits, BigDecimal totalDebits, BigDecimal netBalance,
                             int investorCount, Map<TransactionType, BigDecimal> totalsByType,
                             List<InvestorPosition> investors) {}

    public record FundPosition(Long fundId, String fundName, BigDecimal totalCredits, BigDecimal totalDebits,
                               BigDecimal netBalance, Map<TransactionType, BigDecimal> totalsByType) {}

    public record InvestorReport(Long investorId, String investorName, LocalDate from, LocalDate to,
                                 BigDecimal totalCredits, BigDecimal totalDebits, BigDecimal netBalance,
                                 int fundCount, List<FundPosition> funds) {}
}
