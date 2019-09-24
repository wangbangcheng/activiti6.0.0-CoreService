package com.wangbc.coreapi;

import com.google.common.collect.Maps;
import org.activiti.engine.HistoryService;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.history.HistoricTaskInstance;
import org.activiti.engine.repository.DeploymentBuilder;
import org.activiti.engine.repository.DeploymentQuery;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.runtime.Execution;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.runtime.ProcessInstanceBuilder;
import org.activiti.engine.task.IdentityLink;
import org.activiti.engine.task.Task;
import org.activiti.engine.test.ActivitiRule;
import org.activiti.engine.test.Deployment;
import org.junit.Rule;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

/**
 * \* 描述：
 * \*         runtimeService
 * \*
 * \* @auther:    15201
 * \* @date:      2019/8/5 19:09
 * \
 */
public class RuntimeServiceTest {
    private static final Logger LOGGER = LoggerFactory.getLogger(RuntimeServiceTest.class);

    @Rule
    public ActivitiRule activitiRule = new ActivitiRule();

    @Test
    @Deployment(resources = {"my-process.bpmn20.xml"})
    public void testStartProcess(){
        RuntimeService runtimeService = activitiRule.getRuntimeService();
        Map<String,Object> variables = Maps.newHashMap();
        variables.put("key1","value1");
        variables.put("key2","value2");
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("my-process",variables);
        LOGGER.info("processInstance = {}",processInstance.getId());
    }
    @Test
    @Deployment(resources = {"my-process.bpmn20.xml"})
    public void testStartProcessByProcessDefinition(){
        RuntimeService runtimeService = activitiRule.getRuntimeService();
        ProcessDefinition processDefinition = activitiRule
                .getRepositoryService()
                .createProcessDefinitionQuery()
                .singleResult();
        Map<String,Object> variables = Maps.newHashMap();
        variables.put("key1","value1");
        variables.put("key2","value2");
        ProcessInstance processInstance = runtimeService.startProcessInstanceById(processDefinition.getId(),variables);
        LOGGER.info("processInstance = {} processInstance.processVariables = {}"
                ,processInstance
                ,processInstance.getProcessVariables().containsKey("key1"));
    }

    @Test
    @Deployment(resources = {"my-process.bpmn20.xml"})
    public void testStartProcessBuilder(){
        RuntimeService runtimeService = activitiRule.getRuntimeService();
        Map<String,Object> variables = Maps.newHashMap();
        variables.put("key1","value1");
        variables.put("key2","value2");
        ProcessInstanceBuilder processInstanceBuilder = runtimeService.createProcessInstanceBuilder();
        //设置builder
        ProcessInstance processInstance = processInstanceBuilder
                .businessKey("businessKey001")
                .processDefinitionKey("my-process")
                .variables(variables)
                .start();

        LOGGER.info("processInstance = {}",processInstance);

    }
    @Test
    @Deployment(resources = {"my-process.bpmn20.xml"})
    public void testVariables(){
        RuntimeService runtimeService = activitiRule.getRuntimeService();
        Map<String,Object> variables = Maps.newHashMap();
        variables.put("key1","value1");
        variables.put("key2","value2");
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("my-process",variables);
        LOGGER.info("processInstance = {}",processInstance);

        runtimeService.setVariable(processInstance.getId(), "key2", "value22");
        runtimeService.setVariable(processInstance.getId(), "key3", "value3");
        Map<String, Object> variables1 = runtimeService.getVariables(processInstance.getId());
        LOGGER.info("variables1 = {}", variables1);

    }
    @Test
    @Deployment(resources = {"my-process.bpmn20.xml"})
    public void testProcessInstanceQuery(){
        RuntimeService runtimeService = activitiRule.getRuntimeService();
        Map<String,Object> variables = Maps.newHashMap();
        variables.put("key1","value1");
        variables.put("key2","value2");
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("my-process",variables);
        LOGGER.info("processInstance = {}",processInstance);

        ProcessInstance processInstance1 = runtimeService
                .createProcessInstanceQuery()
                .processInstanceId(processInstance.getId())
                .singleResult();
        LOGGER.info("processInstance = {}",processInstance);
    }

    @Test
    @Deployment(resources = {"my-process.bpmn20.xml"})
    public void testExecutionQuery(){
        RuntimeService runtimeService = activitiRule.getRuntimeService();
        Map<String,Object> variables = Maps.newHashMap();
        variables.put("key1","value1");
        variables.put("key2","value2");
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("my-process",variables);
        LOGGER.info("processInstance = {}",processInstance);
        ProcessInstance processInstance2 = runtimeService.startProcessInstanceByKey("my-process",variables);
        List<Execution> executions = runtimeService.createExecutionQuery().listPage(0, 100);
        for(Execution execution:executions){
            LOGGER.info("execution = {}, execution.processInstance = {}", execution,execution.getProcessInstanceId());
        }
        LOGGER.info("execution.size = {},",executions.size());
    }

    @Test
    @Deployment(resources = {"my-process-trigger.bpmn20.xml"})
    public void testTrigger(){
        RuntimeService runtimeService = activitiRule.getRuntimeService();
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("my-process");

        Execution execution = runtimeService
                .createExecutionQuery()
                .activityId("someTask")
                .singleResult();
        LOGGER.info("execution = {}", execution );

        runtimeService.trigger(execution.getId());
        execution = runtimeService
                .createExecutionQuery()
                .activityId("someTask")
                .singleResult();
        LOGGER.info("execution = {}", execution);

    }


    @Test
    @Deployment(resources = {"my-process-signal-received.bpmn20.xml"})
    public void testSignalEventReceived(){
        RuntimeService runtimeService = activitiRule.getRuntimeService();
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("my-process");

        Execution execution = runtimeService
                .createExecutionQuery()
                .signalEventSubscriptionName("my-signal")
                .singleResult();

        LOGGER.info("execution = {}", execution);

        runtimeService.signalEventReceived("my-signal");

        execution = runtimeService
                .createExecutionQuery()
                .signalEventSubscriptionName("my-signal")
                .singleResult();

        LOGGER.info("execution = {}", execution);
    }

    @Test
    @Deployment(resources = {"my-process-message-received.bpmn20.xml"})
    public void testMessageReceived(){
        RuntimeService runtimeService = activitiRule.getRuntimeService();
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("my-process");

        Execution execution = runtimeService
                .createExecutionQuery()
                .messageEventSubscriptionName("my-message")
                .singleResult();

        LOGGER.info("execution = {}", execution);

        runtimeService.messageEventReceived("my-message", execution.getId());

        execution = runtimeService
                .createExecutionQuery()
                .messageEventSubscriptionName("my-message")
                .singleResult();

        LOGGER.info("execution = {}", execution);
    }


    @Test   //不建议
    @Deployment(resources = {"my-process-message-received.bpmn20.xml"})
    public void testMessageStart(){
        RuntimeService runtimeService = activitiRule.getRuntimeService();
        ProcessInstance processInstance = runtimeService.startProcessInstanceByMessage("my-message");

        LOGGER.info("processInstance = {}", processInstance);
    }



    @Test
    @Deployment(resources = {"my-process.bpmn20.xml"})
    public void testTaskService(){
        RuntimeService runtimeService = activitiRule.getRuntimeService();
        Map<String,Object> variables = Maps.newHashMap();
        variables.put("key1","value1");
        variables.put("key2","value2");
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("my-process",variables);
        ProcessInstance processInstance2 = runtimeService.startProcessInstanceByKey("my-process",variables);
        LOGGER.info("processInstance = {}",processInstance.getId());
        LOGGER.info("processInstance2 = {}",processInstance2.getId());

        TaskService taskService = activitiRule.getTaskService();
        Task task = taskService.createTaskQuery().processInstanceId(processInstance.getProcessInstanceId()).singleResult();
        LOGGER.info("task = {}", task);
        taskService.complete(task.getId());
        Task task1 = taskService.createTaskQuery().singleResult();
        LOGGER.info("After complete task1 = {}", task1);

        HistoryService historyService = activitiRule.getHistoryService();
        List<HistoricTaskInstance> historicTaskInstances = historyService.createHistoricTaskInstanceQuery().listPage(0, 100);
        for (HistoricTaskInstance hisTaskInstance:historicTaskInstances){
            LOGGER.info("hisTaskInstance = {}",hisTaskInstance.getName());
        }
        LOGGER.info("historicTaskInstances.size = {}",historicTaskInstances.size());


    }


}
