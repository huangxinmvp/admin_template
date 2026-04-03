package com.hiking.treasure.modules.phase1.service;

import com.hiking.treasure.modules.phase1.entity.ProjectGovernanceState;

import java.util.Collection;
import java.util.Map;

public interface ProjectGovernanceLinkageService {

    ProjectGovernanceState recomputeProject(String projectId);

    ProjectGovernanceState getOrRecomputeProjectState(String projectId);

    Map<String, ProjectGovernanceState> loadOrRecomputeProjectStates(Collection<String> projectIds);

    void recomputeProjects(Collection<String> projectIds);

    void recomputeProjectsByWorkflowTemplate(String workflowTemplateId);

    void recomputeAllProjects();

    void removeProjectState(String projectId);
}
