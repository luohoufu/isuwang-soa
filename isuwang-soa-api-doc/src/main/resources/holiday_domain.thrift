namespace java com.isuwang.soa.holiday.domain

/**
* 日程表实体，描述节假日休假、调休安排
**/
struct DailySchedule {

        /**
         * id
         */
        1: i32 id,
    
        /**
         * 休/班原因
         * 比如：国庆节，国庆调休..etc
         */
        2: string name,
    
        /**
         * 是否上班
         * true: 上班
         * false: 休假
         */
        3: bool work,
    
    
        /**
        * @datatype(name="date")
        **/
        4: i64 startDate,
    
        /**
        * @datatype(name="date")
        **/
        5: i64 endDate,
    
        /**
         * 备注
         */
        6: optional string remark,
    
        7: i32 operatorId,

        /**
        * @datatype(name="date")
        **/
        8: i64 createdAt,
    
        /**
        * @datatype(name="date")
        **/
        9: i64 updatedAt;
        
}