package com.wangbc.coreapi;

import org.activiti.engine.RepositoryService;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.DeploymentBuilder;
import org.activiti.engine.repository.DeploymentQuery;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.task.IdentityLink;
import org.activiti.engine.test.ActivitiRule;
import org.junit.Rule;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * \* 描述：
 * \*         TODO
 * \*
 * \* @auther:    15201
 * \* @date:      2019/8/5 19:09
 * \
 */
public class RepostoryServiceTest {
    private static final Logger LOGGER = LoggerFactory.getLogger(RepostoryServiceTest.class);

    @Rule
    public ActivitiRule activitiRule = new ActivitiRule();

    @Test       //流程部署测试
    public void test(){
        RepositoryService repositoryService = activitiRule.getRepositoryService();

        DeploymentBuilder deploymentBuilder = repositoryService.createDeployment();
        deploymentBuilder.name("测试部署资源")
                .addClasspathResource("MyProcess.bpmn20.xml")
                .addClasspathResource("my-process.bpmn20.xml");

        Deployment deploy = deploymentBuilder.deploy();

        LOGGER.info("deploy = {}",deploy);

        DeploymentBuilder deploymentBuilder1 = repositoryService.createDeployment();
        deploymentBuilder1.name("测试部署资源1")
                .addClasspathResource("MyProcess.bpmn20.xml")
                .addClasspathResource("my-process.bpmn20.xml");

        Deployment deploy1 = deploymentBuilder1.deploy();


        DeploymentQuery deploymentQuery = repositoryService.createDeploymentQuery();
        Deployment deployment = deploymentQuery.deploymentId(deploy.getId()).singleResult();

        List<ProcessDefinition> definitionList = repositoryService
                .createProcessDefinitionQuery()
                .deploymentId(deployment.getId())
                .listPage(0, 100);

        for (ProcessDefinition definition:definitionList){
            LOGGER.info("ProcessDefinition = {}, version = {} , key = {} , id = {}",
                    definition,
                    definition.getVersion(),
                    definition.getKey(),
                    definition.getId());
        }
        LOGGER.info("ProcessDefinitionList = {}",definitionList.size());


        Deployment deployment1 = deploymentQuery.deploymentId(deploy1.getId()).singleResult();
        List<ProcessDefinition> definitionList1 = repositoryService
                .createProcessDefinitionQuery()
                .orderByDeploymentId()
                .listPage(0, 100);

        for (ProcessDefinition definition:definitionList1){
            LOGGER.info("ProcessDefinition1 = {}, version1 = {} , key1 = {} , id1 = {}",
                    definition,
                    definition.getVersion(),
                    definition.getKey(),
                    definition.getId());
        }
        LOGGER.info("ProcessDefinitionList1 = {}",definitionList1.size());
    }


    @Test   //流程挂起和激活测试(suspend   activate)
    @org.activiti.engine.test.Deployment(resources = "my-process.bpmn20.xml")
    public void test2(){
        RepositoryService repositoryService = activitiRule.getRepositoryService();

        ProcessDefinition processDefinition = repositoryService
                .createProcessDefinitionQuery()
                .singleResult();

        LOGGER.info("processDefinition.id = {}",processDefinition.getId());

        repositoryService.suspendProcessDefinitionById(processDefinition.getId());


        try {
            LOGGER.info("启动流程");
            activitiRule.getRuntimeService().startProcessInstanceById(processDefinition.getId());
            LOGGER.info("启动成功");
        } catch (Exception e) {
            LOGGER.info("启动失败");
            LOGGER.info(e.getMessage(),e);

            repositoryService.activateProcessDefinitionById(processDefinition.getId());
            LOGGER.info("重新启动");
            activitiRule.getRuntimeService().startProcessInstanceById(processDefinition.getId());
            LOGGER.info("启动成功");
        }
    }


    @Test   //添加（删除）用户和组
    @org.activiti.engine.test.Deployment(resources = "my-process.bpmn20.xml")
    public void test3(){
        RepositoryService repositoryService = activitiRule.getRepositoryService();

        ProcessDefinition processDefinition = repositoryService
                .createProcessDefinitionQuery()
                .singleResult();

        LOGGER.info("processDefinition.id = {}",processDefinition.getId());

        repositoryService.addCandidateStarterUser(processDefinition.getId(),"小明");
        repositoryService.addCandidateStarterGroup(processDefinition.getId(),"用户组1");

        List<IdentityLink> identityLinkList = repositoryService
                .getIdentityLinksForProcessDefinition(processDefinition.getId());

        for (IdentityLink identityLink:identityLinkList){
            LOGGER.info("identityLink: group = {},user = {},processDefinitionID = {},type = {}",
                    identityLink.getGroupId(),
                    identityLink.getUserId(),
                    identityLink.getProcessDefinitionId(),
                    identityLink.getType());
        }
        LOGGER.info("identityLinkList.size = {}",identityLinkList.size());

        repositoryService.deleteCandidateStarterGroup(processDefinition.getId(),"用户组1");
        repositoryService.deleteCandidateStarterUser(processDefinition.getId(),"小明");
        repositoryService.deleteCandidateStarterUser(processDefinition.getId(),"小明");

        List<IdentityLink> identityLinkList2 = repositoryService
                .getIdentityLinksForProcessDefinition(processDefinition.getId());

        for (IdentityLink identityLink:identityLinkList2){
            LOGGER.info("identityLink: group = {},user = {},processDefinitionID = {},type = {}",
                    identityLink.getGroupId(),
                    identityLink.getUserId(),
                    identityLink.getProcessDefinitionId(),
                    identityLink.getType());
        }
        LOGGER.info("identityLinkList2.size = {}",identityLinkList2.size());

    }
}
