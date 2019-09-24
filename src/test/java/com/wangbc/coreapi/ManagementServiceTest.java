package com.wangbc.coreapi;

import com.wangbc.mapper.MyCustomMapper;
import org.activiti.engine.ManagementService;
import org.activiti.engine.impl.cmd.AbstractCustomSqlExecution;
import org.activiti.engine.impl.persistence.entity.*;
import org.activiti.engine.management.TablePage;
import org.activiti.engine.management.TablePageQuery;
import org.activiti.engine.runtime.*;
import org.activiti.engine.test.ActivitiRule;
import org.activiti.engine.test.Deployment;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.junit.Rule;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * \* 描述：
 * \*         TODO
 * \*
 * \* @auther:    15201
 * \* @date:      2019/8/31 17:16
 * \
 */
public class ManagementServiceTest {
    private static final Logger LOGGER = LoggerFactory.getLogger(ManagementServiceTest.class);

    @Rule
    public ActivitiRule activitiRule = new ActivitiRule();


    @Test
    @Deployment(resources = {"my-process-job.bpmn20.xml"})
    public void testJobQuery(){
        ManagementService managementService = activitiRule.getManagementService();
        //定时job
        List<Job> timerJobs = managementService.createTimerJobQuery().listPage(0, 100);

        for(Job job:timerJobs){
            LOGGER.info("job = {}",job);
        }
        LOGGER.info("timerJobs.size = {}", timerJobs.size());

        JobQuery jobQuery = managementService.createJobQuery();

        //中断的，暂停的job
        SuspendedJobQuery suspendedJobQuery = managementService.createSuspendedJobQuery();
        List<Job> suspendedJobList = suspendedJobQuery.list();
        for (Job suspendedJob : suspendedJobList){
            LOGGER.info("suspendedJob = {}", suspendedJob);
        }
        LOGGER.info("suspendedJobList.size = {}" , suspendedJobList.size());


        //无法执行的job
        DeadLetterJobQuery deadLetterJobQuery = managementService.createDeadLetterJobQuery();
        List<Job> deadLetterJobList = deadLetterJobQuery.list();
        for (Job deadLetterJob : deadLetterJobList){
            LOGGER.info("deadLetterJob = {}", deadLetterJob);
        }
        LOGGER.info("deadLetterJobList.size = {}" , deadLetterJobList.size());
    }



    //获取表名称
    @Test
    @Deployment(resources = {"my-process-job.bpmn20.xml"})
    public void testTablePageQuery() {

        ProcessInstance processInstance =
                activitiRule.getRuntimeService().startProcessInstanceByKey("my-process", "110");

        ManagementService managementService = activitiRule.getManagementService();

        TablePage tablePage = managementService.createTablePageQuery()
                .tableName(managementService.getTableName(ExecutionEntity.class))
                .listPage(0, 100);
        LOGGER.info("tableName = {} ", tablePage.getTableName());
        List<Map<String, Object>> rows = tablePage.getRows();
        for (Map<String,Object> row:rows){
            LOGGER.info("row = {}", row);
        }
        LOGGER.info("rows.size = {}", rows.size());
    }


    //自定义sql
    @Test
    @Deployment(resources = {"my-process.bpmn20.xml"})
    public void testDIYPageQuery() {

        ProcessInstance processInstance =
                activitiRule.getRuntimeService().startProcessInstanceByKey("my-process");

        ManagementService managementService = activitiRule.getManagementService();

        List<Map<String, Object>> maps =managementService
                .executeCustomSql(new AbstractCustomSqlExecution<MyCustomMapper,
                    List<Map<String, Object>>>(MyCustomMapper.class) {

            public List<Map<String, Object>> execute(MyCustomMapper myCustomMapper) {
                return myCustomMapper.findAll();
            }
        });

        for (Map<String, Object> map:maps){
            LOGGER.info("map = {}", map);
        }

    }

}
