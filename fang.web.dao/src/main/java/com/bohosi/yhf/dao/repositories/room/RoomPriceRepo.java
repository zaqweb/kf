package com.bohosi.yhf.dao.repositories.room;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.LockModeType;

import org.hibernate.LockMode;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import com.bohosi.yhf.dao.entity.rooms.RoomPriceCalendar;

public interface RoomPriceRepo extends CrudRepository<RoomPriceCalendar, Serializable>
{

	public Iterable<RoomPriceCalendar> findById_RoomIdAndId_RoomDateBetween(String roomId, Date start, Date end);

	public RoomPriceCalendar findById_RoomIdAndId_RoomDate(String roomId, Date start);

	@Lock(value = LockModeType.PESSIMISTIC_WRITE)
	public RoomPriceCalendar save(RoomPriceCalendar rpc);

	@Query("select rpc from RoomPriceCalendar rpc where rpc.id.roomId = ? and rpc.id.roomDate>=? and rpc.id.roomDate<? and rpc.status !=?")
	public Iterable<RoomPriceCalendar> findById_RoomIdAndId_RoomDateBetweenAndStatusNot(String roomId, Date start,
			Date end, int status);
}
