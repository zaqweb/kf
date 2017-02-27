package com.bohosi.yhf.dao.repositories.base;

import static org.springframework.data.jpa.repository.query.QueryUtils.toOrders;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

public abstract class AbstractSearchableJpaRepository<T> extends AbstractDomainClassAwareRepository<T>
		implements
			IBaseRepo<T, Serializable>
{
	@PersistenceContext
	protected EntityManager entityManager;

	@Override
	public Page<T> search(SearchCriteria criteria, Pageable pageable)
	{
		CriteriaBuilder builder = this.entityManager.getCriteriaBuilder();

		CriteriaQuery<Long> countCriteria = builder.createQuery(Long.class);
		Root<T> countRoot = countCriteria.from(this.domainClass);
		long total = this.entityManager.createQuery(
				countCriteria.select(builder.count(countRoot)).where(toPredicates(criteria, countRoot, builder)))
				.getSingleResult();

		CriteriaQuery<T> pageCriteria = builder.createQuery(this.domainClass);
		Root<T> pageRoot = pageCriteria.from(this.domainClass);
		List<T> list = this.entityManager
				.createQuery(pageCriteria.select(pageRoot).where(toPredicates(criteria, pageRoot, builder))
						.orderBy(toOrders(pageable.getSort(), pageRoot, builder)))
				.setFirstResult(pageable.getOffset()).setMaxResults(pageable.getPageSize()).getResultList();

		return new PageImpl<T>(new ArrayList<T>(list), pageable, total);
	}
	
	
	public Map<String, Object> search(SearchCriteria criteria, Pageable pageable, String status)
	{
		CriteriaBuilder builder = this.entityManager.getCriteriaBuilder();

		CriteriaQuery<Long> countCriteria = builder.createQuery(Long.class);
		Root<T> countRoot = countCriteria.from(this.domainClass);
		long total = this.entityManager.createQuery(
				countCriteria.select(builder.count(countRoot)).where(toPredicates(criteria, countRoot, builder)))
				.getSingleResult();

		CriteriaQuery<T> pageCriteria = builder.createQuery(this.domainClass);
		Root<T> pageRoot = pageCriteria.from(this.domainClass);
		List<T> list = this.entityManager
				.createQuery(pageCriteria.select(pageRoot).where(toPredicates(criteria, pageRoot, builder))
						.orderBy(toOrders(pageable.getSort(), pageRoot, builder)))
				.setFirstResult(pageable.getOffset()).setMaxResults(pageable.getPageSize()).getResultList();

		Map<String, Object> resMap = new HashMap<String, Object>();
		resMap.put("resultList", new ArrayList<T>(list));
		resMap.put("pageable", pageable);
		resMap.put("total", total);
		return resMap;
	}

	private static Predicate[] toPredicates(SearchCriteria criteria, Root<?> root, CriteriaBuilder builder)
	{
		Predicate[] predicates = new Predicate[criteria.size()];
		int i = 0;
		for (Criterion c : criteria)
			predicates[i++] = c.getOperator().toPredicate(c, root, builder);
		return predicates;
	}
}
