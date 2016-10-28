package com.bohosi.yhf.service.rooms;

import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.bohosi.yhf.dao.entity.rooms.Room;
import com.bohosi.yhf.dao.entity.rooms.RoomPriceCalendar;
import com.bohosi.yhf.dao.repositories.base.SearchCriteria;

public interface IRoomBasic
{

	public List<Room> getActivityBanner();

	public Page<Room> getAllAvaliableRooms(SearchCriteria searchCriteria, Pageable pageable);

	public Room getRoomDetailById(String roomId);

	public Map<String, RoomPriceCalendar> getRoomPriceCalendar(String roomId, String start, String end);

	public void saveRoom(Room room);
}