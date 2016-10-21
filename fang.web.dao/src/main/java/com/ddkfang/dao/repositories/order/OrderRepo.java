package com.ddkfang.dao.repositories.order;

import java.io.Serializable;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;

import com.ddkfang.dao.entity.order.Order;

public interface OrderRepo extends PagingAndSortingRepository<Order, Serializable>
{

	public Page<Order> findByBookerIdAndStatusOrderByCreateTimeDesc(String userId, int status, Pageable pageable);

	public Page<Order> findByBookerIdOrderByCreateTimeDesc(String userId, Pageable pageable);

	public List<Order> findByStatus(int status);

	public Order findById(String id);
	
	public Order findByOrderNumber(String orderNo);
}
