package com.wangbc.coreapi;

import com.google.common.collect.Maps;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.runtime.ProcessInstance;
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
import org.springframework.core.style.ToStringStyler;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;

/**
 * \* 描述：
 * \*         TODO
 * \*
 * \* @auther:    15201
 * \* @date:      2019/8/5 19:09
 * \
 */
public class TaskServiceTest {
    private static final Logger LOGGER = LoggerFactory.getLogger(TaskServiceTest.class);

    @Rule
    public ActivitiRule activitiRule = new ActivitiRule();

    @Test
    @Deployment(resources = {"my-process-task.bpmn20.xml"})
    public void test(){
        Map<String, Object> variables = Maps.newHashMap();
        variables.put("message", "myTask test message!");
        RuntimeService runtimeService = activitiRule.getRuntimeService();
        ProcessInstance processInstance = runtimeService.
                startProcessInstanceByKey("my-process",variables);

        TaskService taskService = activitiRule.getTaskService();
        Task task = taskService.createTaskQuery().singleResult();

        LOGGER.info("task.description = {}", task.getDescription());
        LOGGER.info("task = {}",ToStringBuilder.reflectionToString(task, ToStringStyle.SHORT_PREFIX_STYLE));

        taskService.setVariable(task.getId(), "key1","value1"  );
        taskService.setVariableLocal(task.getId(), "keyLocal1", "valueLocal1");

        Map<String, Object> taskServiceVariables = taskService.getVariables(task.getId());
        Map<String, Object> taskServiceVariablesLocal = taskService.getVariablesLocal(task.getId());

        Map<String, Object> processVariables = activitiRule.getRuntimeService().getVariables(task.getExecutionId());
        LOGGER.info("taskServiceVariables = {}", taskServiceVariables);
        LOGGER.info("taskServiceVariablesLocal = {}", taskServiceVariablesLocal);
        LOGGER.info("processVariables = {}", processVariables);

        Map<String, Object> completeVar = Maps.newConcurrentMap();
        completeVar.put("cKey1", "cValue1");
        taskService.complete(task.getId(), completeVar);

        Task task1 = taskService.createTaskQuery().taskId(task.getId()).singleResult();
        LOGGER.info("task1 = {} ", task1);
    }

    @Test
    @Deployment(resources = {"my-process-task.bpmn20.xml"})
    public void testTaskServiceUser(){
        Map<String, Object> variables = Maps.newHashMap();
        variables.put("message", "myTask test message!");
        RuntimeService runtimeService = activitiRule.getRuntimeService();
        ProcessInstance processInstance = runtimeService.
                startProcessInstanceByKey("my-process",variables);

        TaskService taskService = activitiRule.getTaskService();
        Task task = taskService
                .createTaskQuery()
                .processInstanceId(processInstance.getProcessInstanceId())
                .singleResult();

        LOGGER.info("task.description = {}", task.getDescription());
        LOGGER.info("task = {}",ToStringBuilder.reflectionToString(task, ToStringStyle.SHORT_PREFIX_STYLE));

       taskService.setOwner(task.getId(), "user1");
//       taskService.setAssignee(task.getId(), "xiaoming");     //不建议使用

        List<Task> taskList = taskService
                .createTaskQuery()
                .taskCandidateUser("xiaoming")
                .taskUnassigned()
                .listPage(0, 100);


        for (Task task1:taskList){
            try {
                taskService.claim(task1.getId(), "xiaoming");
            } catch (Exception e) {
                LOGGER.error(e.getMessage(),e);
            }
        }

        List<IdentityLink> identityLinksForTask = taskService.getIdentityLinksForTask(task.getId());
        for (IdentityLink identityLink:identityLinksForTask){
            LOGGER.info("identitLink = {}", identityLink);
        }
        LOGGER.info("identityLinksForTask.size = {}", identityLinksForTask.size());

        List<Task> xiaomings = taskService
                .createTaskQuery()
                .taskAssignee("xiaoming")
                .listPage(0, 100);

        for (Task xiaoming:xiaomings){
            Map<String,Object> vars = Maps.newHashMap();
            vars.put("cKey1", "cValue1");
            taskService.complete(xiaoming.getId(),vars);
        }

        xiaomings = taskService.createTaskQuery().taskAssignee("xiaoming").listPage(0, 100);
        LOGGER.info("是否存在 {}", CollectionUtils.isEmpty(xiaomings));
    }

    @Test
    @Deployment(resources = {"my-process-task.bpmn20.xml"})
    public void testTaskAttachment() {
        Map<String, Object> variables = Maps.newHashMap();
        variables.put("message", "myTask test message!");
        RuntimeService runtimeService = activitiRule.getRuntimeService();
        ProcessInstance processInstance = runtimeService.
                startProcessInstanceByKey("my-process", variables);

        TaskService taskService = activitiRule.getTaskService();

        Task task = taskService.createTaskQuery().singleResult();

        taskService.claim(task.getId(), "小明");
        taskService.createAttachment("url"
                , task.getId(), processInstance.getProcessInstanceId()
                ,"name", "desc"
                , "url/test.png");
        List<Attachment> taskAttachments = taskService.getTaskAttachments(task.getId());
        for (Attachment taskAttachment:taskAttachments){
            LOGGER.info("taskAttachment = {}", ToStringBuilder.reflectionToString(taskAttachment, ToStringStyle.SHORT_PREFIX_STYLE));
        }
        LOGGER.info("taskAttachments.size = {}", taskAttachments.size());


    }
}
