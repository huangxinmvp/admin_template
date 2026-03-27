import React from 'react';
import { WorkflowTaskTablePage } from '@/features/workflow/WorkflowTaskTablePage';
import { getMyWorkflowTodoTasks } from '@/services/backend/workflow';

const WorkflowTodoPage: React.FC = () => (
  <WorkflowTaskTablePage
    allowComplete
    description="对接 /api/workflow/tasks/todo，查看当前账号待处理的流程任务，并可直接办理。"
    emptyDescription="当前没有待办任务"
    endpointLabel="/api/workflow/tasks/todo"
    loadTasks={getMyWorkflowTodoTasks}
    title="我的待办"
  />
);

export default WorkflowTodoPage;
