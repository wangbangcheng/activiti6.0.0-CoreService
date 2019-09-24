package com.wangbc.mapper;

import org.activiti.engine.task.Task;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
 * \* 描述：
 * \*         TODO
 * \*
 * \* @auther:    15201
 * \* @date:      2019/9/4 21:50
 * \
 */
public interface MyCustomMapper {

    @Select("select * from act_ru_task")
    public List<Map<String,Object>> findAll();

}
