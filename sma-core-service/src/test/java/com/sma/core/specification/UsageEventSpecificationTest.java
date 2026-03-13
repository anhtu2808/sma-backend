package com.sma.core.specification;

import com.sma.core.dto.model.QuotaOwnerContext;
import com.sma.core.entity.UsageEvent;
import com.sma.core.enums.Role;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Order;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@DisplayName("UsageEventSpecification Tests")
@ExtendWith(MockitoExtension.class)
class UsageEventSpecificationTest {

    @Mock
    private Root<UsageEvent> root;

    @Mock
    private CriteriaQuery<UsageEvent> query;

    @Mock
    private CriteriaBuilder criteriaBuilder;

    @Mock
    private Path<Object> subscriptionPath;

    @Mock
    private Path<Object> companyPath;

    @Mock
    private Path<Object> companyIdPath;

    @Mock
    private Path<Object> candidatePath;

    @Mock
    private Path<Object> candidateIdPath;

    @Mock
    private Path<Object> createdAtPath;

    @Mock
    private Predicate tenantPredicate;

    @Mock
    private Predicate combinedPredicate;

    @Mock
    private Order order;

    @Test
    @DisplayName("Should add company filter for recruiter history")
    void shouldAddCompanyFilterForRecruiterHistory() {
        QuotaOwnerContext ownerContext = QuotaOwnerContext.builder().role(Role.RECRUITER).companyId(99).build();
        configureOrdering();
        when(root.get("subscription")).thenReturn(subscriptionPath);
        when(subscriptionPath.get("company")).thenReturn(companyPath);
        when(companyPath.get("id")).thenReturn(companyIdPath);
        when(criteriaBuilder.equal(companyIdPath, 99)).thenReturn(tenantPredicate);
        when(criteriaBuilder.and(any(Predicate[].class))).thenReturn(combinedPredicate);

        UsageEventSpecification.filterBy(ownerContext, null, null, null, null, null)
                .toPredicate(root, query, criteriaBuilder);

        verify(criteriaBuilder).equal(companyIdPath, 99);
    }

    @Test
    @DisplayName("Should add candidate filter for candidate history")
    void shouldAddCandidateFilterForCandidateHistory() {
        QuotaOwnerContext ownerContext = QuotaOwnerContext.builder().role(Role.CANDIDATE).candidateId(55).build();
        configureOrdering();
        when(root.get("subscription")).thenReturn(subscriptionPath);
        when(subscriptionPath.get("candidate")).thenReturn(candidatePath);
        when(candidatePath.get("id")).thenReturn(candidateIdPath);
        when(criteriaBuilder.equal(candidateIdPath, 55)).thenReturn(tenantPredicate);
        when(criteriaBuilder.and(any(Predicate[].class))).thenReturn(combinedPredicate);

        UsageEventSpecification.filterBy(ownerContext, null, null, null, null, null)
                .toPredicate(root, query, criteriaBuilder);

        verify(criteriaBuilder).equal(candidateIdPath, 55);
    }

    @Test
    @DisplayName("Should not add tenant filter for admin history")
    void shouldNotAddTenantFilterForAdminHistory() {
        QuotaOwnerContext ownerContext = QuotaOwnerContext.builder().role(Role.ADMIN).build();
        configureOrdering();
        when(criteriaBuilder.and(any(Predicate[].class))).thenReturn(combinedPredicate);

        UsageEventSpecification.filterBy(ownerContext, null, null, null, null, null)
                .toPredicate(root, query, criteriaBuilder);

        verify(root, never()).get("subscription");
    }

    private void configureOrdering() {
        when(query.getResultType()).thenReturn((Class) UsageEvent.class);
        when(root.get("createdAt")).thenReturn(createdAtPath);
        when(criteriaBuilder.desc(createdAtPath)).thenReturn(order);
    }
}
