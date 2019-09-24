package com.wangbc.coreapi;

import com.google.common.collect.Maps;
import org.activiti.engine.FormService;
import org.activiti.engine.HistoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.history.*;
import org.activiti.engine.runtime.Execution;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.runtime.ProcessInstanceBuilder;
import org.activiti.engine.task.Attachment;
import org.activiti.engine.task.IdentityLink;
import org.activiti.engine.task.Task;
import org.activiti.engine.test.ActivitiRule;
import org.activiti.engine.test.Deployment;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.junit.Rule;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Map;

/**
 * \* 描述：
 * \*         TODO
 * \*
 * \* @auther:    15201
 * \* @date:      2019/8/5 19:09
 * \
 */
public class HistoryServiceTest {
    private static final Logger LOGGER = LoggerFactory.getLogger(HistoryServiceTest.class);

    @Rule
    public ActivitiRule activitiRule = new ActivitiRule("activiti_history.cfg.xml");

    @Test
    @Deployment(resources = {"my-process.bpmn20.xml"})
    public void testHistory() {
        HistoryService historyService = activitiRule.getHistoryService();
        RuntimeService runtimeService = activitiRule.getRuntimeService();
        TaskService taskService = activitiRule.getTaskService();
        FormService formService = activitiRule.getFormService();

        ProcessInstanceBuilder processInstanceBuilder =
                runtimeService.createProcessInstanceBuilder();

        Map<String, Object> variables = Maps.newHashMap();
        variables.put("key0", "value0");
        variables.put("key1", "value1");
        variables.put("key2", "value2");

        Map<String, Object> transientVariables = Maps.newHashMap();
        transientVariables.put("tkey1", "tvalue1");
        ProcessInstance processInstance = processInstanceBuilder
                .processDefinitionKey("my-process")
                .variables(variables)
                .transientVariables(transientVariables)     //瞬时变量
                .start();

        runtimeService.setVariable(processInstance.getId(), "ru_key1", "ru_value1");

        Task task = taskService
                .createTaskQuery()
                .processInstanceId(processInstance.getProcessInstanceId())
                .singleResult();

        Map<String, String> properties = Maps.newHashMap();
        properties.put("f_key1", "f_value1");
        properties.put("key2", "f_value2_2");

        //提交表单数据
        formService.submitTaskFormData(task.getId(), properties);

        //历史流程实例
        List<HistoricProcessInstance> historicProcessInstances =
                historyService.createHistoricProcessInstanceQuery().listPage(0, 100);
        for (HistoricProcessInstance historicProcessInstance : historicProcessInstances) {
            LOGGER.info("historicProcessInstance = {}",
                    ToStringBuilder.reflectionToString(historicProcessInstance,
                            ToStringStyle.SHORT_PREFIX_STYLE));
        }
        LOGGER.info("historicProcessInstances.size = {}", historicProcessInstances.size());

        //历史工作流节点实例
        List<HistoricActivityInstance> historicActivityInstances =
                historyService.createHistoricActivityInstanceQuery().listPage(0, 100);
        for (HistoricActivityInstance historicActivityInstance : historicActivityInstances) {
            LOGGER.info("historicActivityInstance = {}", historicActivityInstance);
        }
        LOGGER.info("historicActivityInstances.size = {}", historicActivityInstances.size());

        //历史任务实例
        List<HistoricTaskInstance> historicTaskInstances =
                historyService.createHistoricTaskInstanceQuery().listPage(0, 100);
        for (HistoricTaskInstance historicTaskInstance : historicTaskInstances) {
            LOGGER.info("historicTaskInstance = {}",
                    ToStringBuilder.reflectionToString(historicTaskInstance,
                            ToStringStyle.SHORT_PREFIX_STYLE));
        }
        LOGGER.info("historicTaskInstances.size = {}", historicTaskInstances.size());

        //历史变量实例
        List<HistoricVariableInstance> historicVariableInstances =
                historyService.createHistoricVariableInstanceQuery().listPage(0, 100);
        for (HistoricVariableInstance historicVariableInstance : historicVariableInstances) {
            LOGGER.info("historicVariableInstance = {}", historicVariableInstance);
        }
        LOGGER.info("historicVariableInstances.size = {}", historicVariableInstances.size());
        //历史基本信息
        List<HistoricDetail> historicDetails =
                historyService.createHistoricDetailQuery().listPage(0, 100);

        for (HistoricDetail historicDetail : historicDetails) {
            LOGGER.info("historicDetail = {}", historicDetail);
        }
        LOGGER.info("historicDetails.size = {}", historicDetails.size());

        ProcessInstanceHistoryLog processInstanceHistoryLog =
                historyService.createProcessInstanceHistoryLogQuery(processInstance.getProcessInstanceId())
                        .includeActivities()
                        .includeComments()
                        .includeTasks()
                        .includeFormProperties()
                        .includeVariables()
                        .includeVariableUpdates().singleResult();

        List<HistoricData> historicDatas = processInstanceHistoryLog.getHistoricData();
        for (HistoricData historicData : historicDatas) {
            LOGGER.info("historicData = {}", historicData);
        }
        LOGGER.info("historicDatas.size = {}", historicDatas.size());





    }
}
