package com.ddkfang.service.order.impl;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ddkfang.dao.entity.order.Order;
import com.ddkfang.dao.entity.rooms.Room;
import com.ddkfang.dao.entity.rooms.RoomPriceCalendar;
import com.ddkfang.dao.repositories.order.OrderRepo;
import com.ddkfang.dao.repositories.room.RoomBasicRepo;
import com.ddkfang.dao.repositories.room.RoomPriceRepo;
import com.ddkfang.exception.OrderDateConflictException;
import com.ddkfang.service.order.IOrdersService;
import com.ddkfang.service.rooms.IRoomPrice;
import com.ddkfang.util.priceCalendar.PriceCalendarUtil;
@Service
public class OrdersServiceImpl implements IOrdersService
{
	@Autowired
	OrderRepo orderRepo;

	@Autowired
	RoomBasicRepo roomRepo;

	@Autowired
	RoomPriceRepo roomPriceRepo;

	@Autowired
	IRoomPrice roomPrice;

	/**
	 * return new order Id
	 */
	@Transactional
	public Order createOrder(Map<String, Object> infoMap, Set<String> dayDetail) throws Exception
	{

		String id = infoMap.get("roomId").toString();
		String beginDate = infoMap.get("checkInDate").toString();
		String endDate = infoMap.get("checkOutDate").toString();

		//检查是否有日期不可用
		Iterable<RoomPriceCalendar> it = roomPriceRepo.findById_RoomIdAndId_RoomDateBetweenAndStatusNot(id,
				PriceCalendarUtil.stringToSimpleDate(beginDate), PriceCalendarUtil.stringToSimpleDate(endDate), 0);
		Room room = roomRepo.findOneByRoomId(id);
		List<RoomPriceCalendar> list = (List<RoomPriceCalendar>) it;
		if (list.size() > 0)
		{
			throw new OrderDateConflictException();
		}

		Order or = new Order();
		or.setRoomId(infoMap.get("roomId").toString());
		or.setDateStart(PriceCalendarUtil.stringToSimpleDate(infoMap.get("checkInDate").toString()));
		or.setDateEnd(PriceCalendarUtil.stringToSimpleDate(infoMap.get("checkOutDate").toString()));
		or.setBookerId(infoMap.get("bookerId").toString());
		or.setContactName(infoMap.get("contactName").toString());
		or.setContactPhone(infoMap.get("contactPhone").toString());
		or.setPrice((Integer) infoMap.get("price"));
		or.setTotalPrice((Integer) infoMap.get("basicPrice"));
		or.setCreateTime(PriceCalendarUtil.getCurrentTimestamp());
		or.setUpdateTime(PriceCalendarUtil.getCurrentTimestamp());
		//新建订单 默认为待付款
		or.setStatus(1);
		or.setUserDisplayStatus(1);
		or.setChannel((Integer) infoMap.get("channel"));
		//设置房间冗余信息
		or.setApartmentId(room.getRoomApartment().getApartmentId());
		or.setApartmentName(room.getRoomApartment().getApartmentName());
		or.setRoomBathroomCount(room.getRoomBathroomCount());
		or.setRoomBedroomCount(room.getRoomBedroomCount());
		String[] imgs = room.getRoomImages().split(",");
		or.setRoomImg(imgs != null ? imgs[0] : "");
		or.setRoomKitchenCount(room.getRoomKitchenCount());
		or.setRoomName(room.getRoomName());
		or.setRoomSittingCount(room.getRoomSittingCount());
		or.setLastPayTime(PriceCalendarUtil.getExactTimestamp(Calendar.MINUTE, 10));
		or.setOrderNumber(PriceCalendarUtil.getUniqueTimestampStr());
		orderRepo.save(or);

		//更新或插入价格日历
		if (or.getId() != null)
		{
			for (String s : dayDetail)
			{
				roomPrice.saveOrUpdatePriceCalendar(id, PriceCalendarUtil.stringToSimpleDate(s), 1,
						room.getRoomPrice());
			}
		}
		return or;
	}

	public Page<Order> getOrdersByBookerAndStatus(String userId, int status, Pageable pageable) throws Exception
	{
		Page<Order> po = orderRepo.findByBookerIdAndStatusOrderByCreateTimeDesc(userId, status, pageable);

		return po;
	}

	public Page<Order> getOrdersByBooker(String userId, Pageable pageable) throws Exception
	{
		Page<Order> po = orderRepo.findByBookerIdOrderByCreateTimeDesc(userId, pageable);
		return po;
	}

	public Order getOrdersById(String id) throws Exception
	{
		Order or = orderRepo.findById(id);
		return or;
	}

	public Order saveOrder(Order order) throws Exception
	{
		Order or = orderRepo.save(order);
		return or;
	}

	public void updatePriceCalendarForOvertimeOrder(Order or) throws Exception
	{
		Room room = roomRepo.findOneByRoomId(or.getRoomId());

		int days = PriceCalendarUtil.dateDiff(or.getDateEnd(), or.getDateStart());

		Set<String> dayDetail = PriceCalendarUtil
				.genCalendarWithStr(PriceCalendarUtil.simpleDateToString(or.getDateStart()), days);
		for (String s : dayDetail)
		{
			roomPrice.saveOrUpdatePriceCalendar(room.getRoomId(), PriceCalendarUtil.stringToSimpleDate(s), 0);
		}
	}

	@Async
	public void updateExpiredOrdersCalendar() throws Exception
	{
		// get add need pay order
		List<Order> orList = orderRepo.findByStatus(1);

		for (Order or : orList)
		{
			if (or.getLastPayTime().before(new Date()))
			{
				updatePriceCalendarForOvertimeOrder(or);
				or.setStatus(3);
				or.setUserDisplayStatus(4);
				orderRepo.save(or);
			}
		}
	}

	public boolean isOrdersExpired(String id) throws Exception
	{
		Order or = orderRepo.findById(id);
		if (or.getLastPayTime().after(new Date()))
		{
			return false;
		}
		return true;
	}

	public Order getOrdersByOrderNumber(String orderNo) throws Exception
	{
		Order or = orderRepo.findByOrderNumber(orderNo);
		
		return or;
	}

}
