import React from 'react';
import { WorkflowTaskTablePage } from '@/features/workflow/WorkflowTaskTablePage';
import { getMyWorkflowDoneTasks } from '@/services/backend/workflow';

const WorkflowDonePage: React.FC = () => (
  <WorkflowTaskTablePage
    description="对接 /api/workflow/tasks/done，展示当前账号已经办理完成的流程任务。"
    emptyDescription="当前没有已办任务"
    endpointLabel="/api/workflow/tasks/done"
    loadTasks={getMyWorkflowDoneTasks}
    title="我的已办"
  />
);

export default WorkflowDonePage;
