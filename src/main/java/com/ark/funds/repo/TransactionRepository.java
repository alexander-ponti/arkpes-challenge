package com.ark.funds.repo;

import com.ark.funds.model.FundTransaction;
import com.ark.funds.model.TransactionType;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TransactionRepository extends JpaRepository<FundTransaction, Long>, JpaSpecificationExecutor<FundTransaction> {

    boolean existsByFundId(Long fundId);
    boolean existsByInvestorId(Long investorId);

    interface FundRow {
        Long getInvestorId();
        String getInvestorName();
        TransactionType getType();
        BigDecimal getTotal();
    }

    interface InvestorRow {
        Long getFundId();
        String getFundName();
        TransactionType getType();
        BigDecimal getTotal();
    }

    @Query("""
        select i.id as investorId, i.name as investorName, t.type as type, sum(t.amount) as total
        from FundTransaction t join t.investor i
        where t.fund.id = :fundId and t.transactionDate between :from and :to
        group by i.id, i.name, t.type
        """)
    List<FundRow> totalsForFund(@Param("fundId") Long fundId, @Param("from") LocalDate from, @Param("to") LocalDate to);

    @Query("""
        select f.id as fundId, f.name as fundName, t.type as type, sum(t.amount) as total
        from FundTransaction t join t.fund f
        where t.investor.id = :investorId and t.transactionDate between :from and :to
        group by f.id, f.name, t.type
        """)
    List<InvestorRow> totalsForInvestor(@Param("investorId") Long investorId, @Param("from") LocalDate from, @Param("to") LocalDate to);
}
