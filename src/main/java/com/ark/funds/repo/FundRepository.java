package com.ark.funds.repo;

import com.ark.funds.model.Fund;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface FundRepository extends JpaRepository<Fund, Long> {
    boolean existsByNameIgnoreCase(String name);
    boolean existsByNameIgnoreCaseAndIdNot(String name, Long id);

    @Query("select f from Fund f where exists (select t.id from FundTransaction t where t.investor.id = :investorId and t.fund = f)")
    Page<Fund> findByInvestorId(@Param("investorId") Long investorId, Pageable pageable);
}
