package com.ark.funds.repo;

import com.ark.funds.model.Investor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface InvestorRepository extends JpaRepository<Investor, Long> {
    boolean existsByEmailIgnoreCase(String email);
    boolean existsByEmailIgnoreCaseAndIdNot(String email, Long id);

    @Query("select i from Investor i where exists (select t.id from FundTransaction t where t.fund.id = :fundId and t.investor = i)")
    Page<Investor> findByFundId(@Param("fundId") Long fundId, Pageable pageable);
}
