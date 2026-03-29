package br.com.mechanic.account.repository.account.jpa;

import br.com.mechanic.account.entity.topic.Topic;
import br.com.mechanic.account.enuns.AccountProfileTypeEnum;
import br.com.mechanic.account.enuns.TopicStatusEnum;
import jakarta.persistence.criteria.JoinType;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.Specification;

import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class TopicSpecifications {

    public static Specification<Topic> forAccountWithOptionalFilters(
            Long accountId,
            TopicStatusEnum statusFilter,
            AccountProfileTypeEnum profileTypeFilter
    ) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(
                    criteriaBuilder.equal(
                            root.join("account", JoinType.INNER).get("id"),
                            accountId
                    )
            );
            if (statusFilter != null) {
                predicates.add(criteriaBuilder.equal(root.get("status"), statusFilter));
            }
            if (profileTypeFilter != null) {
                predicates.add(criteriaBuilder.equal(root.get("profileType"), profileTypeFilter));
            }
            return criteriaBuilder.and(predicates.toArray(Predicate[]::new));
        };
    }
}
