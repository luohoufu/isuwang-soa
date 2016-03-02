include "holiday_domain.thrift"
namespace java com.isuwang.soa.holiday.service

/**
* Holiday Service
**/
service HolidayService {



   /**
    * 查询daily_schedules表，返回符合条件记录列表
    **/
    list<holiday_domain.DailySchedule> selectEntitiesByRange(1: i64 _date_1, 2: i64 _date_2);

}