import {
  AimOutlined,
  CodeOutlined,
  DeleteOutlined,
  DownloadOutlined,
  EditOutlined,
  FormOutlined,
  PlusOutlined,
  RedoOutlined,
  SaveOutlined,
  UndoOutlined,
  UploadOutlined,
} from '@ant-design/icons';
import { PageContainer } from '@ant-design/pro-components';
import {
  Alert,
  App,
  Button,
  Card,
  Empty,
  Form,
  Input,
  List,
  Modal,
  Row,
  Col,
  Select,
  Space,
  Switch,
  Tabs,
  Tag,
  Typography,
} from 'antd';
import { useSearchParams } from '@umijs/max';
import React, { useEffect, useMemo, useRef, useState } from 'react';
import WorkflowBpmnDesigner, {
  type ListenerKind,
  type WorkflowBpmnDesignerHandle,
  type WorkflowDesignerProcessMeta,
  type WorkflowEditableField,
  type WorkflowExtensionPropertyDefinition,
  type WorkflowListenerDefinition,
  type WorkflowSelectedElementState,
} from '@/features/workflow/WorkflowBpmnDesigner';
import {
  buildAssociatedFormKey,
  parseAssociatedFormId,
  parseWorkflowDesignerForms,
  parseWorkflowProcessSettings,
  replaceAssociatedFormInXml,
  updateWorkflowDesignerFormsXml,
  updateWorkflowProcessSettingsXml,
  type WorkflowDesignerForm,
  type WorkflowDesignerFormField,
  type WorkflowProcessSettings,
} from '@/features/workflow/designerSchema';
import { createDefaultWorkflowDiagram } from '@/features/workflow/defaultWorkflowDiagram';
import {
  deployWorkflowDefinition,
  getWorkflowDefinitionXml,
  getWorkflowDefinitions,
  getWorkflowDraft,
  saveWorkflowDraft,
} from '@/services/backend/workflow';
import type {
  WorkflowDefinition,
  WorkflowDraft,
  WorkflowSaveDraftPayload,
} from '@/services/backend/types';

interface DeployFormValues {
  category?: string;
  name?: string;
}

interface DraftFormValues {
  category?: string;
  draftName?: string;
  remark?: string;
}

interface WorkflowFormMetaValues {
  description?: string;
  id: string;
  name: string;
}

interface WorkflowFormFieldValues {
  id: string;
  label: string;
  placeholder?: string;
  required?: boolean;
  type: string;
}

interface ListenerFormValues {
  event: string;
  implementation: string;
  implementationType: 'class' | 'delegateExpression' | 'expression';
}

interface ExtensionPropertyFormValues {
  name: string;
  value?: string;
}

interface WorkflowNodeEditorState extends WorkflowSelectedElementState {
  associatedFormId?: string;
  customFormKey?: string;
}

type DesignerTabKey = 'basic' | 'flow' | 'form';

const DEFAULT_CATEGORY = 'https://admin-template/workflow';

const FORM_FIELD_TYPE_OPTIONS = [
  { label: '单行输入', value: 'input' },
  { label: '多行文本', value: 'textarea' },
  { label: '数字', value: 'number' },
  { label: '下拉选择', value: 'select' },
  { label: '单选', value: 'radio' },
  { label: '日期', value: 'datePicker' },
  { label: '开关', value: 'switch' },
];

const EXECUTION_LISTENER_EVENT_OPTIONS = [
  { label: 'start', value: 'start' },
  { label: 'end', value: 'end' },
];

const TASK_LISTENER_EVENT_OPTIONS = [
  { label: 'create', value: 'create' },
  { label: 'assignment', value: 'assignment' },
  { label: 'complete', value: 'complete' },
  { label: 'delete', value: 'delete' },
];

const buildDefinitionFileName = (processId?: string) =>
  `${(processId || `workflow_${Date.now()}`).replace(/[^a-zA-Z0-9_-]+/g, '_')}.bpmn20.xml`;

const buildLocalKey = (prefix: string, seed?: string | number) =>
  `${prefix}-${seed || Date.now()}-${Date.now().toString(36)}`;

const createEmptyDesignerForm = (): WorkflowDesignerForm => {
  const id = `form_${Date.now()}`;
  return {
    description: '',
    fields: [],
    id,
    key: buildLocalKey('designer-form', id),
    name: '新建表单',
  };
};

const createEmptyDesignerFormField = (): WorkflowDesignerFormField => {
  const id = `field_${Date.now()}`;
  return {
    id,
    key: buildLocalKey('designer-form-field', id),
    label: '新字段',
    required: false,
    type: 'input',
  };
};

const downloadTextFile = (content: string, fileName: string, type: string) => {
  const blob = new Blob([content], { type });
  const url = URL.createObjectURL(blob);
  const link = document.createElement('a');
  link.href = url;
  link.download = fileName;
  link.click();
  URL.revokeObjectURL(url);
};

const toNodeEditorState = (
  source?: WorkflowSelectedElementState,
): WorkflowNodeEditorState | undefined => {
  if (!source) {
    return undefined;
  }
  const associatedFormId = parseAssociatedFormId(source.formKey);
  return {
    ...source,
    associatedFormId,
    customFormKey: associatedFormId ? undefined : source.formKey,
  };
};

const isFormAssociableNode = (elementType?: string) =>
  elementType === 'bpmn:StartEvent' || elementType === 'bpmn:UserTask';

const supportsExecutionListeners = (elementType?: string) =>
  Boolean(elementType) && elementType !== 'bpmn:SequenceFlow';

const supportsTaskListeners = (elementType?: string) => elementType === 'bpmn:UserTask';

const supportsActivityBehavior = (elementType?: string) =>
  elementType === 'bpmn:UserTask' ||
  elementType === 'bpmn:ServiceTask' ||
  elementType === 'bpmn:SubProcess' ||
  elementType === 'bpmn:CallActivity';

const renderPropertyList = <T extends { key: string }>(
  items: T[],
  renderItem: (item: T, index: number) => React.ReactNode,
  emptyText: string,
) => {
  if (!items.length) {
    return <Empty description={emptyText} image={Empty.PRESENTED_IMAGE_SIMPLE} />;
  }

  return <div className="saas-workflow-designer__list">{items.map(renderItem)}</div>;
};

const WorkflowDesignerPage: React.FC = () => {
  const { message } = App.useApp();
  const [deployForm] = Form.useForm<DeployFormValues>();
  const [draftForm] = Form.useForm<DraftFormValues>();
  const [processForm] = Form.useForm<WorkflowProcessSettings>();
  const [formMetaForm] = Form.useForm<WorkflowFormMetaValues>();
  const [fieldForm] = Form.useForm<WorkflowFormFieldValues>();
  const [listenerForm] = Form.useForm<ListenerFormValues>();
  const [extensionPropertyForm] = Form.useForm<ExtensionPropertyFormValues>();
  const [searchParams, setSearchParams] = useSearchParams();
  const designerRef = useRef<WorkflowBpmnDesignerHandle>(null);
  const importInputRef = useRef<HTMLInputElement>(null);
  const baselineXmlRef = useRef(createDefaultWorkflowDiagram());
  const normalizingOpenedDefinitionRef = useRef(false);
  const [activeTab, setActiveTab] = useState<DesignerTabKey>('basic');
  const [deployOpen, setDeployOpen] = useState(false);
  const [draftOpen, setDraftOpen] = useState(false);
  const [fieldEditor, setFieldEditor] = useState<{ index?: number }>();
  const [sourceEditorOpen, setSourceEditorOpen] = useState(false);
  const [deploying, setDeploying] = useState(false);
  const [draftSaving, setDraftSaving] = useState(false);
  const [sourceLoading, setSourceLoading] = useState(false);
  const [designerXml, setDesignerXml] = useState(baselineXmlRef.current);
  const [xmlDraft, setXmlDraft] = useState(baselineXmlRef.current);
  const [dirty, setDirty] = useState(false);
  const [currentDefinition, setCurrentDefinition] = useState<WorkflowDefinition>();
  const [currentDraft, setCurrentDraft] = useState<WorkflowDraft>();
  const [processMeta, setProcessMeta] = useState<WorkflowDesignerProcessMeta>(() => ({
    isExecutable: true,
    processName: '新建流程',
  }));
  const [designerForms, setDesignerForms] = useState<WorkflowDesignerForm[]>([]);
  const [activeFormId, setActiveFormId] = useState<string>();
  const [selectedNode, setSelectedNode] = useState<WorkflowSelectedElementState>();
  const [nodeSettingsOpen, setNodeSettingsOpen] = useState(false);
  const [nodeDraft, setNodeDraft] = useState<WorkflowNodeEditorState>();
  const [listenerEditor, setListenerEditor] = useState<{
    index?: number;
    kind: ListenerKind;
  }>();
  const [extensionPropertyEditor, setExtensionPropertyEditor] = useState<{
    index?: number;
  }>();

  const activeForm = useMemo(
    () => designerForms.find((item) => item.id === activeFormId),
    [activeFormId, designerForms],
  );

  const updateDesignerXml = (nextXml: string) => {
    setDesignerXml(nextXml);
    setXmlDraft(nextXml);
    setDirty(nextXml !== baselineXmlRef.current);
  };

  const applyCleanXml = (
    nextXml: string,
    options?: {
      definition?: WorkflowDefinition;
      draft?: WorkflowDraft;
      processMeta?: WorkflowDesignerProcessMeta;
    },
  ) => {
    baselineXmlRef.current = nextXml;
    setDirty(false);
    setCurrentDefinition(options?.definition);
    setCurrentDraft(options?.draft);
    setDesignerXml(nextXml);
    setXmlDraft(nextXml);
    if (options?.processMeta) {
      setProcessMeta(options.processMeta);
    }
  };

  const resetToNewProcess = (silent = false) => {
    normalizingOpenedDefinitionRef.current = false;
    applyCleanXml(createDefaultWorkflowDiagram(), {
      processMeta: {
        isExecutable: true,
        processName: '新建流程',
      },
    });
    setSearchParams({});
    setActiveTab('basic');
    if (!silent) {
      message.success('已创建新的流程');
    }
  };

  const openDefinitionById = async (definitionId: string, silent = false) => {
    setSourceLoading(true);
    try {
      const response = await getWorkflowDefinitions();
      const definition = (response.result || []).find((item) => item.id === definitionId);
      if (!definition) {
        throw new Error('流程定义不存在');
      }
      const xmlResponse = await getWorkflowDefinitionXml(definition.id);
      const definitionXml = xmlResponse.result || xmlResponse.message;
      normalizingOpenedDefinitionRef.current = true;
      applyCleanXml(definitionXml || createDefaultWorkflowDiagram(), {
        definition,
        processMeta: {
          isExecutable: true,
          processId: definition.key,
          processName: definition.name || definition.key,
        },
      });
      setSearchParams({ definitionId: definition.id });
      if (!silent) {
        message.success(`已打开流程定义：${definition.name || definition.key}`);
      }
    } catch (_error) {
      message.error('读取流程定义失败');
    } finally {
      setSourceLoading(false);
    }
  };

  const openDraftById = async (draftId: string, silent = false) => {
    setSourceLoading(true);
    try {
      const response = await getWorkflowDraft(draftId);
      const draft = response.result;
      normalizingOpenedDefinitionRef.current = true;
      applyCleanXml(draft?.bpmnXml || createDefaultWorkflowDiagram(), {
        draft,
        processMeta: {
          isExecutable: true,
          processId: draft?.processDefinitionKey || undefined,
          processName:
            draft?.processDefinitionName || draft?.draftName || '未命名流程草稿',
        },
      });
      setSearchParams({ draftId });
      if (!silent) {
        message.success(`已打开草稿：${draft?.draftName || draftId}`);
      }
    } catch (_error) {
      message.error('读取流程草稿失败');
    } finally {
      setSourceLoading(false);
    }
  };

  useEffect(() => {
    const draftId = searchParams.get('draftId');
    const definitionId = searchParams.get('definitionId');
    if (draftId && currentDraft?.id !== draftId) {
      void openDraftById(draftId, true);
      return;
    }
    if (!draftId && definitionId && currentDefinition?.id !== definitionId) {
      void openDefinitionById(definitionId, true);
    }
  }, [searchParams, currentDefinition?.id, currentDraft?.id]);

  useEffect(() => {
    const nextProcessSettings = parseWorkflowProcessSettings(designerXml);
    processForm.setFieldsValue(nextProcessSettings);
    const nextForms = parseWorkflowDesignerForms(designerXml);
    setDesignerForms(nextForms);
    setActiveFormId((currentId) => {
      if (currentId && nextForms.some((item) => item.id === currentId)) {
        return currentId;
      }
      return nextForms[0]?.id;
    });
  }, [designerXml, processForm]);

  useEffect(() => {
    if (activeTab !== 'form') {
      return;
    }
    if (!activeForm) {
      return;
    }

    formMetaForm.setFieldsValue({
      description: activeForm.description,
      id: activeForm.id,
      name: activeForm.name,
    });
  }, [activeForm, activeTab, formMetaForm]);

  useEffect(() => {
    setNodeDraft(toNodeEditorState(selectedNode));
  }, [selectedNode]);

  const handleImportLocalFile = async (event: React.ChangeEvent<HTMLInputElement>) => {
    const file = event.target.files?.[0];
    if (!file) {
      return;
    }

    try {
      const xml = await file.text();
      normalizingOpenedDefinitionRef.current = true;
      applyCleanXml(xml, {
        processMeta: {
          isExecutable: true,
          processName: file.name.replace(/(\.bpmn20\.xml|\.bpmn|\.xml)$/i, ''),
        },
      });
      setSearchParams({});
      message.success(`已导入本地流程文件：${file.name}`);
    } catch (_error) {
      message.error('读取本地流程文件失败');
    } finally {
      event.target.value = '';
    }
  };

  const handleOpenDraftModal = () => {
    draftForm.setFieldsValue({
      category: currentDraft?.category || DEFAULT_CATEGORY,
      draftName:
        currentDraft?.draftName ||
        processMeta.processName ||
        processMeta.processId ||
        '未命名流程草稿',
      remark: currentDraft?.remark || '',
    });
    setDraftOpen(true);
  };

  const handleSaveDraft = async () => {
    const values = await draftForm.validateFields();
    setDraftSaving(true);
    try {
      const payload: WorkflowSaveDraftPayload = {
        id: currentDraft?.id,
        bpmnXml: designerXml,
        category: values.category?.trim() || DEFAULT_CATEGORY,
        draftName:
          values.draftName?.trim() ||
          processMeta.processName ||
          processMeta.processId ||
          '未命名流程草稿',
        processDefinitionKey: processMeta.processId || undefined,
        processDefinitionName: processMeta.processName || undefined,
        remark: values.remark?.trim() || undefined,
        sourceDefinitionId:
          currentDraft?.sourceDefinitionId || currentDefinition?.id || undefined,
        sourceDefinitionKey:
          currentDraft?.sourceDefinitionKey || currentDefinition?.key || undefined,
      };
      const response = await saveWorkflowDraft(payload);
      const savedDraft = response.result;
      applyCleanXml(designerXml, {
        draft: savedDraft,
        processMeta: {
          isExecutable: processMeta.isExecutable,
          processId: processMeta.processId,
          processName: processMeta.processName,
        },
      });
      setDraftOpen(false);
      setSearchParams({ draftId: savedDraft.id });
      message.success('流程草稿已保存到后端');
    } catch (_error) {
      message.error('保存流程草稿失败');
    } finally {
      setDraftSaving(false);
    }
  };

  const handleDeploy = async () => {
    const values = await deployForm.validateFields();
    const file = new File([designerXml], buildDefinitionFileName(processMeta.processId), {
      type: 'text/xml;charset=utf-8',
    });

    setDeploying(true);
    try {
      await deployWorkflowDefinition({
        file,
        category: values.category?.trim() || DEFAULT_CATEGORY,
        name: values.name?.trim() || processMeta.processName || processMeta.processId,
      });
      baselineXmlRef.current = designerXml;
      setDirty(false);
      setDeployOpen(false);
      message.success('流程部署成功');
    } catch (_error) {
      message.error('流程部署失败');
    } finally {
      setDeploying(false);
    }
  };

  const handleApplyXmlDraft = () => {
    setDesignerXml(xmlDraft);
    setDirty(xmlDraft !== baselineXmlRef.current);
    setSourceEditorOpen(false);
    message.success('XML 源码已应用到设计器');
  };

  const handleExportSvg = async () => {
    try {
      if (!designerRef.current) {
        setActiveTab('flow');
        message.info('请先切到流程设计并等待画布加载完成后再导出 SVG');
        return;
      }
      const svg = await designerRef.current?.saveSvg();
      if (!svg) {
        throw new Error('未生成 SVG 内容');
      }
      downloadTextFile(
        svg,
        `${processMeta.processId || currentDraft?.draftName || 'workflow-diagram'}.svg`,
        'image/svg+xml;charset=utf-8',
      );
      message.success('SVG 已导出');
    } catch (_error) {
      message.error('导出 SVG 失败');
    }
  };

  const syncFormsToXml = (
    nextForms: WorkflowDesignerForm[],
    successText?: string,
    preferredFormId?: string,
  ) => {
    const nextXml = updateWorkflowDesignerFormsXml(designerXml, nextForms);
    setActiveFormId(
      preferredFormId && nextForms.some((item) => item.id === preferredFormId)
        ? preferredFormId
        : nextForms[0]?.id,
    );
    updateDesignerXml(nextXml);
    if (successText) {
      message.success(successText);
    }
  };

  const handleSaveProcessSettings = async () => {
    const values = await processForm.validateFields();
    const nextSettings: WorkflowProcessSettings = {
      candidateStarterGroups: values.candidateStarterGroups?.trim() || undefined,
      candidateStarterUsers: values.candidateStarterUsers?.trim() || undefined,
      documentation: values.documentation?.trim() || undefined,
      isExecutable: values.isExecutable !== false,
      processId: values.processId?.trim() || undefined,
      processName: values.processName?.trim() || undefined,
    };
    const nextXml = updateWorkflowProcessSettingsXml(designerXml, nextSettings);
    updateDesignerXml(nextXml);
    setProcessMeta({
      isExecutable: nextSettings.isExecutable,
      processId: nextSettings.processId,
      processName: nextSettings.processName,
    });
    message.success('基础设置已更新到流程 XML');
  };

  const handleCreateForm = () => {
    const nextForm = createEmptyDesignerForm();
    const nextForms = [...designerForms, nextForm];
    syncFormsToXml(nextForms, '已新增流程表单', nextForm.id);
  };

  const handleDeleteForm = (form: WorkflowDesignerForm) => {
    Modal.confirm({
      content: `删除后会同时清空节点上对表单 ${form.name} 的关联。`,
      okText: '删除',
      title: `确认删除表单“${form.name}”吗？`,
      onOk: () => {
        const nextForms = designerForms.filter((item) => item.id !== form.id);
        const nextXml = replaceAssociatedFormInXml(
          updateWorkflowDesignerFormsXml(designerXml, nextForms),
          form.id,
        );
        updateDesignerXml(nextXml);
        setActiveFormId(nextForms[0]?.id);
        message.success('流程表单已删除');
      },
    });
  };

  const handleSaveActiveFormMeta = async () => {
    if (!activeForm) {
      return;
    }
    const values = await formMetaForm.validateFields();
    const nextForms = designerForms.map((item) =>
      item.id === activeForm.id
        ? {
            ...item,
            description: values.description?.trim() || undefined,
            id: values.id.trim(),
            name: values.name.trim(),
          }
        : item,
    );
    let nextXml = updateWorkflowDesignerFormsXml(designerXml, nextForms);
    if (activeForm.id !== values.id.trim()) {
      nextXml = replaceAssociatedFormInXml(nextXml, activeForm.id, values.id.trim());
    }
    updateDesignerXml(nextXml);
    setActiveFormId(values.id.trim());
    message.success('表单信息已保存');
  };

  const openFieldEditor = (index?: number) => {
    const field = typeof index === 'number' ? activeForm?.fields[index] : undefined;
    fieldForm.setFieldsValue({
      id: field?.id || '',
      label: field?.label || '',
      placeholder: field?.placeholder,
      required: Boolean(field?.required),
      type: field?.type || 'input',
    });
    setFieldEditor({ index });
  };

  const handleSaveFieldEditor = async () => {
    if (!activeForm) {
      return;
    }
    const values = await fieldForm.validateFields();
    const nextField: WorkflowDesignerFormField = {
      id: values.id.trim(),
      key: buildLocalKey('designer-form-field', values.id.trim()),
      label: values.label.trim(),
      placeholder: values.placeholder?.trim() || undefined,
      required: Boolean(values.required),
      type: values.type,
    };
    const nextForms = designerForms.map((item) => {
      if (item.id !== activeForm.id) {
        return item;
      }
      const fields = [...item.fields];
      if (typeof fieldEditor?.index === 'number') {
        fields.splice(fieldEditor.index, 1, nextField);
      } else {
        fields.push(nextField);
      }
      return {
        ...item,
        fields,
      };
    });
    syncFormsToXml(nextForms, '表单字段已保存', activeForm.id);
    setFieldEditor(undefined);
  };

  const handleDeleteField = (index: number) => {
    if (!activeForm) {
      return;
    }
    const nextForms = designerForms.map((item) =>
      item.id === activeForm.id
        ? {
            ...item,
            fields: item.fields.filter((_, fieldIndex) => fieldIndex !== index),
          }
        : item,
    );
    syncFormsToXml(nextForms, '表单字段已删除', activeForm.id);
  };

  const openNodeSettings = () => {
    const currentSelected = designerRef.current?.getSelectedState() || selectedNode;
    if (!currentSelected) {
      message.info('请先在画布上选中一个流程节点');
      return;
    }
    setNodeDraft(toNodeEditorState(currentSelected));
    setNodeSettingsOpen(true);
  };

  const openListenerEditor = (kind: ListenerKind, index?: number) => {
    const source =
      kind === 'execution'
        ? nodeDraft?.executionListeners || []
        : nodeDraft?.taskListeners || [];
    const listener = typeof index === 'number' ? source[index] : undefined;
    listenerForm.setFieldsValue({
      event:
        listener?.event ||
        (kind === 'task'
          ? TASK_LISTENER_EVENT_OPTIONS[0]?.value
          : EXECUTION_LISTENER_EVENT_OPTIONS[0]?.value),
      implementation: listener?.implementation || '',
      implementationType: listener?.implementationType || 'class',
    });
    setListenerEditor({ kind, index });
  };

  const handleSaveListenerEditor = async () => {
    if (!nodeDraft || !listenerEditor) {
      return;
    }
    const values = await listenerForm.validateFields();
    const source =
      listenerEditor.kind === 'execution'
        ? [...nodeDraft.executionListeners]
        : [...nodeDraft.taskListeners];
    const nextListener: WorkflowListenerDefinition = {
      event: values.event,
      implementation: values.implementation.trim(),
      implementationType: values.implementationType,
      key: buildLocalKey(`${listenerEditor.kind}-listener`, values.event),
    };
    if (typeof listenerEditor.index === 'number') {
      source.splice(listenerEditor.index, 1, nextListener);
    } else {
      source.push(nextListener);
    }
    setNodeDraft((current) =>
      current
        ? {
            ...current,
            executionListeners:
              listenerEditor.kind === 'execution' ? source : current.executionListeners,
            taskListeners:
              listenerEditor.kind === 'task' ? source : current.taskListeners,
          }
        : current,
    );
    setListenerEditor(undefined);
  };

  const handleDeleteListener = (kind: ListenerKind, index: number) => {
    setNodeDraft((current) => {
      if (!current) {
        return current;
      }
      const source =
        kind === 'execution'
          ? [...current.executionListeners]
          : [...current.taskListeners];
      source.splice(index, 1);
      return {
        ...current,
        executionListeners: kind === 'execution' ? source : current.executionListeners,
        taskListeners: kind === 'task' ? source : current.taskListeners,
      };
    });
  };

  const openExtensionPropertyEditor = (index?: number) => {
    const property =
      typeof index === 'number' ? nodeDraft?.extensionProperties[index] : undefined;
    extensionPropertyForm.setFieldsValue({
      name: property?.name || '',
      value: property?.value,
    });
    setExtensionPropertyEditor({ index });
  };

  const handleSaveExtensionPropertyEditor = async () => {
    if (!nodeDraft) {
      return;
    }
    const values = await extensionPropertyForm.validateFields();
    const nextProperty: WorkflowExtensionPropertyDefinition = {
      key: buildLocalKey('extension-property', values.name.trim()),
      name: values.name.trim(),
      value: values.value?.trim() || undefined,
    };
    const nextProperties = [...nodeDraft.extensionProperties];
    if (typeof extensionPropertyEditor?.index === 'number') {
      nextProperties.splice(extensionPropertyEditor.index, 1, nextProperty);
    } else {
      nextProperties.push(nextProperty);
    }
    setNodeDraft((current) =>
      current
        ? {
            ...current,
            extensionProperties: nextProperties,
          }
        : current,
    );
    setExtensionPropertyEditor(undefined);
  };

  const handleDeleteExtensionProperty = (index: number) => {
    setNodeDraft((current) =>
      current
        ? {
            ...current,
            extensionProperties: current.extensionProperties.filter(
              (_, propertyIndex) => propertyIndex !== index,
            ),
          }
        : current,
    );
  };

  const handleSaveNodeSettings = () => {
    const handle = designerRef.current;
    if (!handle || !nodeDraft) {
      return;
    }
    if (nodeDraft.elementType !== 'bpmn:Process' && !nodeDraft.id.trim()) {
      message.warning('节点 ID 不能为空');
      return;
    }

    const applyField = (field: WorkflowEditableField, value: unknown) => {
      handle.applyFieldValue(field, value);
    };

    if (nodeDraft.elementType !== 'bpmn:Process') {
      applyField('id', nodeDraft.id);
      applyField('name', nodeDraft.name);
      applyField('documentation', nodeDraft.documentation);
    }

    if (supportsActivityBehavior(nodeDraft.elementType)) {
      applyField('asyncEnabled', nodeDraft.asyncEnabled);
      applyField('skipExpression', nodeDraft.skipExpression);
    }

    if (nodeDraft.elementType === 'bpmn:StartEvent') {
      applyField('initiator', nodeDraft.initiator);
    }

    if (nodeDraft.elementType === 'bpmn:UserTask') {
      applyField('assignee', nodeDraft.assignee);
      applyField('owner', nodeDraft.owner);
      applyField('candidateUsers', nodeDraft.candidateUsers);
      applyField('candidateGroups', nodeDraft.candidateGroups);
      applyField('priority', nodeDraft.priority);
      applyField('dueDate', nodeDraft.dueDate);
      applyField('category', nodeDraft.category);
    }

    if (nodeDraft.elementType === 'bpmn:ServiceTask') {
      applyField('delegateClass', nodeDraft.delegateClass);
      applyField('delegateExpression', nodeDraft.delegateExpression);
      applyField('expression', nodeDraft.expression);
      applyField('resultVariableName', nodeDraft.resultVariableName);
    }

    if (nodeDraft.elementType === 'bpmn:SequenceFlow') {
      applyField('conditionExpression', nodeDraft.conditionExpression);
    }

    if (isFormAssociableNode(nodeDraft.elementType)) {
      applyField(
        'formKey',
        nodeDraft.associatedFormId
          ? buildAssociatedFormKey(nodeDraft.associatedFormId)
          : nodeDraft.customFormKey,
      );
    }

    handle.replaceListeners('execution', nodeDraft.executionListeners);
    handle.replaceListeners(
      'task',
      supportsTaskListeners(nodeDraft.elementType) ? nodeDraft.taskListeners : [],
    );
    handle.replaceExtensionProperties(nodeDraft.extensionProperties);
    setNodeSettingsOpen(false);
    message.success('节点设置已应用到画布');
  };

  const modeLabel = currentDraft
    ? '草稿模式'
    : currentDefinition
      ? '定义衍生模式'
      : '新建模式';

  const sourceDescription = currentDraft
    ? `当前正在编辑草稿 ${currentDraft.draftName}${currentDraft.sourceDefinitionKey ? `，来源定义 ${currentDraft.sourceDefinitionKey}` : ''}`
    : currentDefinition
      ? `当前正在基于已部署定义 ${currentDefinition.key} · v${currentDefinition.version} 继续设计`
      : '当前是一个全新的本地流程画布，尚未保存到草稿箱或部署到 Flowable';

  return (
    <PageContainer
      className="saas-page-container saas-workflow-page"
      title="流程编辑器"
    >
      <Space direction="vertical" size={16} style={{ display: 'flex' }}>
        <div className="saas-workflow-designer-tabs">
          <Tabs
            activeKey={activeTab}
            className="saas-workflow-designer-nav"
            items={[
              { key: 'basic', label: '基础设置' },
              { key: 'flow', label: '流程设计' },
              { key: 'form', label: '表单设计' },
            ]}
            onChange={(key) => {
              setActiveTab(key as DesignerTabKey);
              if (key !== 'flow') {
                setNodeSettingsOpen(false);
              }
            }}
          />
        </div>

        <Card className="saas-workflow-toolbar-card" variant="borderless">
          <div className="saas-workflow-toolbar-card__inner">
            <div className="saas-workflow-toolbar-card__group">
              <Space wrap>
                <Tag color="blue">{modeLabel}</Tag>
                <Tag color={dirty ? 'warning' : 'success'}>
                  {dirty ? '存在未保存变更' : '当前内容已保存'}
                </Tag>
                <Tag color={processMeta.isExecutable === false ? 'default' : 'processing'}>
                  {processMeta.isExecutable === false ? '非可执行流程' : '可执行流程'}
                </Tag>
              </Space>
              <Typography.Text type="secondary">{sourceDescription}</Typography.Text>
            </div>

            <div className="saas-workflow-toolbar-card__group">
              <Typography.Text strong>设计动作</Typography.Text>
              <Space wrap>
                <Button icon={<PlusOutlined />} type="primary" onClick={() => resetToNewProcess()}>
                  新建流程
                </Button>
                <Button icon={<SaveOutlined />} onClick={handleOpenDraftModal}>
                  保存草稿
                </Button>
                <Button
                  icon={<UploadOutlined />}
                  onClick={() => {
                    importInputRef.current?.click();
                  }}
                >
                  导入 XML
                </Button>
                <Button
                  icon={<DownloadOutlined />}
                  onClick={() => {
                    downloadTextFile(
                      designerXml,
                      buildDefinitionFileName(processMeta.processId),
                      'text/xml;charset=utf-8',
                    );
                  }}
                >
                  导出 XML
                </Button>
                <Button icon={<DownloadOutlined />} onClick={() => void handleExportSvg()}>
                  导出 SVG
                </Button>
                <Button icon={<CodeOutlined />} onClick={() => setSourceEditorOpen(true)}>
                  源码编辑
                </Button>
                <Button
                  icon={<UploadOutlined />}
                  type="primary"
                  onClick={() => {
                    deployForm.setFieldsValue({
                      category: currentDraft?.category || DEFAULT_CATEGORY,
                      name:
                        processMeta.processName ||
                        currentDraft?.draftName ||
                        processMeta.processId,
                    });
                    setDeployOpen(true);
                  }}
                >
                  部署到 Flowable
                </Button>
              </Space>
            </div>

            {activeTab === 'flow' ? (
              <div className="saas-workflow-toolbar-card__group">
                <Typography.Text strong>画布动作</Typography.Text>
                <Space wrap>
                  <Button
                    icon={<AimOutlined />}
                    onClick={() => {
                      designerRef.current?.fitViewport();
                    }}
                  >
                    适配画布
                  </Button>
                  <Button
                    icon={<UndoOutlined />}
                    onClick={() => {
                      designerRef.current?.undo();
                    }}
                  >
                    撤销
                  </Button>
                  <Button
                    icon={<RedoOutlined />}
                    onClick={() => {
                      designerRef.current?.redo();
                    }}
                  >
                    重做
                  </Button>
                  <Button icon={<EditOutlined />} onClick={openNodeSettings}>
                    节点设置
                  </Button>
                </Space>
              </div>
            ) : null}

            <input
              accept=".bpmn,.bpmn20.xml,.xml"
              hidden
              ref={importInputRef}
              type="file"
              onChange={handleImportLocalFile}
            />
          </div>
        </Card>

        {activeTab === 'basic' ? (
          <Card className="saas-workflow-panel-card" variant="borderless">
            <Space direction="vertical" size={18} style={{ display: 'flex' }}>
              <Alert
                message="基础设置只维护整个流程的公共属性"
                showIcon
                type="info"
                description="这里配置流程名称、流程 Key、可执行状态、发起人范围和流程说明；节点级设置请切到“流程设计”。"
              />
              <Form<WorkflowProcessSettings>
                className="saas-inline-form saas-workflow-process-form"
                form={processForm}
                labelAlign="right"
                labelCol={{ flex: '120px' }}
                wrapperCol={{ flex: 'auto' }}
              >
                <Row gutter={[20, 16]}>
                  <Col md={12} sm={24} xs={24}>
                    <Form.Item
                      label="流程名称"
                      name="processName"
                      rules={[{ required: true, message: '请输入流程名称' }]}
                    >
                      <Input allowClear placeholder="请输入流程名称" />
                    </Form.Item>
                  </Col>
                  <Col md={12} sm={24} xs={24}>
                    <Form.Item
                      label="流程 Key"
                      name="processId"
                      rules={[{ required: true, message: '请输入流程 Key' }]}
                    >
                      <Input allowClear placeholder="例如 leaveApproval" />
                    </Form.Item>
                  </Col>
                  <Col md={12} sm={24} xs={24}>
                    <Form.Item label="可发起用户" name="candidateStarterUsers">
                      <Input allowClear placeholder="多个用户使用逗号分隔" />
                    </Form.Item>
                  </Col>
                  <Col md={12} sm={24} xs={24}>
                    <Form.Item label="可发起角色" name="candidateStarterGroups">
                      <Input allowClear placeholder="多个角色使用逗号分隔" />
                    </Form.Item>
                  </Col>
                  <Col md={12} sm={24} xs={24}>
                    <Form.Item
                      initialValue
                      label="可执行流程"
                      name="isExecutable"
                      valuePropName="checked"
                    >
                      <Switch checkedChildren="是" unCheckedChildren="否" />
                    </Form.Item>
                  </Col>
                  <Col span={24}>
                    <Form.Item label="流程说明" name="documentation">
                      <Input.TextArea
                        allowClear
                        placeholder="描述这个流程的使用场景、审批原则或注意事项"
                        rows={5}
                      />
                    </Form.Item>
                  </Col>
                </Row>
              </Form>
              <Space wrap>
                <Button type="primary" onClick={() => void handleSaveProcessSettings()}>
                  保存基础设置
                </Button>
                <Button
                  onClick={() => {
                    processForm.setFieldsValue(parseWorkflowProcessSettings(designerXml));
                  }}
                >
                  恢复当前 XML
                </Button>
              </Space>
            </Space>
          </Card>
        ) : null}

        {activeTab === 'flow' ? (
          <Card
            className="saas-workflow-panel-card saas-workflow-panel-card--designer"
            loading={sourceLoading}
            variant="borderless"
          >
            <Space direction="vertical" size={16} style={{ display: 'flex' }}>
              <div className="saas-workflow-canvas-header">
                <div>
                  <Typography.Title level={4} style={{ marginBottom: 4 }}>
                    流程画布
                  </Typography.Title>
                  <Typography.Text type="secondary">
                    这里专门负责拖拽和编排流程节点，节点的监听器、扩展属性和表单关联通过“节点设置”管理。
                  </Typography.Text>
                </div>
                <Space wrap>
                  <Tag color="blue">{selectedNode?.typeLabel || '未选中节点'}</Tag>
                  <Button icon={<EditOutlined />} type="primary" onClick={openNodeSettings}>
                    节点设置
                  </Button>
                </Space>
              </div>

              <div className="saas-workflow-node-summary">
                <div>
                  <span>当前节点</span>
                  <strong>{selectedNode?.name || selectedNode?.id || '未选中'}</strong>
                </div>
                <div>
                  <span>节点类型</span>
                  <strong>{selectedNode?.typeLabel || '-'}</strong>
                </div>
                <div>
                  <span>关联表单</span>
                  <strong>
                    {designerForms.find(
                      (item) => item.id === parseAssociatedFormId(selectedNode?.formKey),
                    )?.name || selectedNode?.formKey || '-'}
                  </strong>
                </div>
              </div>

              <WorkflowBpmnDesigner
                onImportError={(error) => {
                  message.error(error.message || '流程 XML 导入失败');
                  setSourceEditorOpen(true);
                }}
                onProcessMetaChange={(meta) => {
                  setProcessMeta(meta);
                }}
                onSelectionChange={(state) => {
                  setSelectedNode(state);
                }}
                onXmlChange={(nextXml) => {
                  if (normalizingOpenedDefinitionRef.current) {
                    normalizingOpenedDefinitionRef.current = false;
                    baselineXmlRef.current = nextXml;
                    setDesignerXml(nextXml);
                    setXmlDraft(nextXml);
                    setDirty(false);
                    return;
                  }
                  updateDesignerXml(nextXml);
                }}
                ref={designerRef}
                xml={designerXml}
              />
            </Space>
          </Card>
        ) : null}

        {activeTab === 'form' ? (
          <Row gutter={[16, 16]}>
            <Col lg={8} md={24} sm={24} xs={24}>
              <Card className="saas-workflow-panel-card" variant="borderless">
                <Space direction="vertical" size={16} style={{ display: 'flex' }}>
                  <div className="saas-workflow-side-header">
                    <div>
                      <Typography.Title level={4} style={{ marginBottom: 4 }}>
                        流程表单
                      </Typography.Title>
                      <Typography.Text type="secondary">
                        先定义表单，再到节点设置里做关联。
                      </Typography.Text>
                    </div>
                    <Button icon={<PlusOutlined />} type="primary" onClick={handleCreateForm}>
                      新建表单
                    </Button>
                  </div>
                  {designerForms.length ? (
                    <List
                      className="saas-workflow-form-list"
                      dataSource={designerForms}
                      renderItem={(item) => (
                        <List.Item
                          className={`saas-workflow-form-list__item${
                            item.id === activeFormId ? ' saas-workflow-form-list__item--active' : ''
                          }`}
                          key={item.key}
                          onClick={() => {
                            setActiveFormId(item.id);
                          }}
                        >
                          <div className="saas-workflow-form-list__meta">
                            <strong>{item.name}</strong>
                            <span>{item.id}</span>
                          </div>
                          <Tag>{item.fields.length} 个字段</Tag>
                        </List.Item>
                      )}
                    />
                  ) : (
                    <Empty
                      description="还没有流程表单"
                      image={Empty.PRESENTED_IMAGE_SIMPLE}
                    >
                      <Button icon={<FormOutlined />} type="primary" onClick={handleCreateForm}>
                        新建第一个表单
                      </Button>
                    </Empty>
                  )}
                </Space>
              </Card>
            </Col>
            <Col lg={16} md={24} sm={24} xs={24}>
              <Card className="saas-workflow-panel-card" variant="borderless">
                {!activeForm ? (
                  <Empty description="请选择左侧表单开始设计" image={Empty.PRESENTED_IMAGE_SIMPLE} />
                ) : (
                  <Space direction="vertical" size={20} style={{ display: 'flex' }}>
                    <div className="saas-workflow-side-header">
                      <div>
                        <Typography.Title level={4} style={{ marginBottom: 4 }}>
                          {activeForm.name}
                        </Typography.Title>
                        <Typography.Text type="secondary">
                          表单字段会作为流程设计里的可关联表单源。
                        </Typography.Text>
                      </div>
                      <Space wrap>
                        <Button danger icon={<DeleteOutlined />} onClick={() => handleDeleteForm(activeForm)}>
                          删除表单
                        </Button>
                        <Button type="primary" onClick={() => void handleSaveActiveFormMeta()}>
                          保存表单信息
                        </Button>
                      </Space>
                    </div>

                    <Form<WorkflowFormMetaValues>
                      className="saas-inline-form saas-workflow-process-form"
                      form={formMetaForm}
                      labelAlign="right"
                      labelCol={{ flex: '120px' }}
                      wrapperCol={{ flex: 'auto' }}
                    >
                      <Row gutter={[20, 16]}>
                        <Col md={12} sm={24} xs={24}>
                          <Form.Item
                            label="表单名称"
                            name="name"
                            rules={[{ required: true, message: '请输入表单名称' }]}
                          >
                            <Input allowClear placeholder="请输入表单名称" />
                          </Form.Item>
                        </Col>
                        <Col md={12} sm={24} xs={24}>
                          <Form.Item
                            label="表单 ID"
                            name="id"
                            rules={[{ required: true, message: '请输入表单 ID' }]}
                          >
                            <Input allowClear placeholder="例如 leaveApplyForm" />
                          </Form.Item>
                        </Col>
                        <Col span={24}>
                          <Form.Item label="表单说明" name="description">
                            <Input.TextArea
                              allowClear
                              placeholder="说明这个表单的用途和适用节点"
                              rows={4}
                            />
                          </Form.Item>
                        </Col>
                      </Row>
                    </Form>

                    <div className="saas-workflow-side-header">
                      <div>
                        <Typography.Title level={5} style={{ marginBottom: 4 }}>
                          字段设计
                        </Typography.Title>
                        <Typography.Text type="secondary">
                          当前字段用于审批节点展示和节点表单关联。
                        </Typography.Text>
                      </div>
                      <Button icon={<PlusOutlined />} type="primary" onClick={() => openFieldEditor()}>
                        新增字段
                      </Button>
                    </div>

                    {renderPropertyList(
                      activeForm.fields,
                      (field, index) => (
                        <div className="saas-workflow-designer__list-item" key={field.key}>
                          <div className="saas-workflow-designer__list-item-header">
                            <Space wrap>
                              <Tag color="blue">{field.label}</Tag>
                              <Tag>{field.type}</Tag>
                              {field.required ? <Tag color="red">必填</Tag> : null}
                            </Space>
                            <Space size={4}>
                              <Button
                                icon={<EditOutlined />}
                                size="small"
                                type="text"
                                onClick={() => openFieldEditor(index)}
                              />
                              <Button
                                danger
                                icon={<DeleteOutlined />}
                                size="small"
                                type="text"
                                onClick={() => handleDeleteField(index)}
                              />
                            </Space>
                          </div>
                          <Typography.Text strong>{field.id}</Typography.Text>
                          <Typography.Text type="secondary">
                            {field.placeholder || '未设置占位提示'}
                          </Typography.Text>
                        </div>
                      ),
                      '当前表单还没有字段',
                    )}
                  </Space>
                )}
              </Card>
            </Col>
          </Row>
        ) : null}
      </Space>

      <Modal
        destroyOnHidden
        confirmLoading={draftSaving}
        open={draftOpen}
        title={currentDraft?.id ? '更新流程草稿' : '保存流程草稿'}
        width={640}
        onCancel={() => {
          setDraftOpen(false);
        }}
        onOk={() => void handleSaveDraft()}
      >
        <Form<DraftFormValues> form={draftForm} layout="vertical">
          <Form.Item
            label="草稿名称"
            name="draftName"
            rules={[{ required: true, message: '请输入草稿名称' }]}
          >
            <Input allowClear maxLength={100} placeholder="请输入草稿名称" />
          </Form.Item>
          <Form.Item label="流程分类" name="category">
            <Input allowClear maxLength={100} placeholder={DEFAULT_CATEGORY} />
          </Form.Item>
          <Form.Item label="备注" name="remark">
            <Input.TextArea allowClear maxLength={500} rows={4} placeholder="可填写当前流程草稿的说明" />
          </Form.Item>
        </Form>
      </Modal>

      <Modal
        destroyOnHidden
        confirmLoading={deploying}
        open={deployOpen}
        title="部署 BPMN 流程"
        width={640}
        onCancel={() => {
          setDeployOpen(false);
        }}
        onOk={() => void handleDeploy()}
      >
        <Form<DeployFormValues> form={deployForm} layout="vertical">
          <Form.Item
            label="部署名称"
            name="name"
            rules={[{ required: true, message: '请输入部署名称' }]}
          >
            <Input allowClear maxLength={100} placeholder="默认使用当前流程名称" />
          </Form.Item>
          <Form.Item
            label="流程分类"
            name="category"
            rules={[{ required: true, message: '请输入流程分类' }]}
          >
            <Input allowClear maxLength={100} placeholder={DEFAULT_CATEGORY} />
          </Form.Item>
        </Form>
      </Modal>

      <Modal
        destroyOnHidden
        open={sourceEditorOpen}
        title="BPMN XML 源码编辑"
        width={980}
        onCancel={() => {
          setSourceEditorOpen(false);
        }}
        onOk={handleApplyXmlDraft}
      >
        <div className="saas-workflow-studio__xml">
          <div className="saas-workflow-studio__xml-actions">
            <Typography.Text type="secondary">
              这里可以直接编辑 BPMN XML，点击“应用源码”后会重新载入到设计器中。
            </Typography.Text>
            <Space wrap>
              <Button onClick={() => setXmlDraft(designerXml)}>恢复当前设计</Button>
            </Space>
          </div>
          <Input.TextArea
            className="saas-workflow-studio__xml-editor"
            spellCheck={false}
            value={xmlDraft}
            onChange={(event) => {
              setXmlDraft(event.target.value);
            }}
          />
        </div>
      </Modal>

      <Modal
        destroyOnHidden
        open={Boolean(fieldEditor)}
        title={fieldEditor?.index === undefined ? '新增表单字段' : '编辑表单字段'}
        width={640}
        onCancel={() => {
          setFieldEditor(undefined);
        }}
        onOk={() => void handleSaveFieldEditor()}
      >
        <Form<WorkflowFormFieldValues> form={fieldForm} layout="vertical">
          <Form.Item
            label="字段标题"
            name="label"
            rules={[{ required: true, message: '请输入字段标题' }]}
          >
            <Input allowClear placeholder="例如 请假天数" />
          </Form.Item>
          <Form.Item
            label="字段 ID"
            name="id"
            rules={[{ required: true, message: '请输入字段 ID' }]}
          >
            <Input allowClear placeholder="例如 leaveDays" />
          </Form.Item>
          <Form.Item
            label="字段类型"
            name="type"
            rules={[{ required: true, message: '请选择字段类型' }]}
          >
            <Select options={FORM_FIELD_TYPE_OPTIONS} placeholder="请选择字段类型" />
          </Form.Item>
          <Form.Item label="占位提示" name="placeholder">
            <Input allowClear placeholder="选填" />
          </Form.Item>
          <Form.Item initialValue={false} label="是否必填" name="required" valuePropName="checked">
            <Switch checkedChildren="是" unCheckedChildren="否" />
          </Form.Item>
        </Form>
      </Modal>

      <Modal
        destroyOnHidden
        open={nodeSettingsOpen}
        title={nodeDraft ? `节点设置 · ${nodeDraft.typeLabel}` : '节点设置'}
        width={880}
        onCancel={() => {
          setNodeSettingsOpen(false);
        }}
        onOk={handleSaveNodeSettings}
      >
        {!nodeDraft ? (
          <Empty description="请先在画布上选择一个节点" image={Empty.PRESENTED_IMAGE_SIMPLE} />
        ) : (
          <Space direction="vertical" size={18} style={{ display: 'flex' }}>
            <Alert
              message={
                nodeDraft.elementType === 'bpmn:Process'
                  ? '流程本身的公共属性已经移到“基础设置”'
                  : `当前正在配置 ${nodeDraft.typeLabel}`
              }
              showIcon
              type="info"
              description={
                nodeDraft.elementType === 'bpmn:Process'
                  ? '这里保留流程级执行监听器和扩展属性；流程名称、流程 Key、发起范围请回到“基础设置”。'
                  : '节点名称、运行行为、监听器、扩展属性和表单关联都集中在这里。'
              }
            />

            {nodeDraft.elementType !== 'bpmn:Process' ? (
              <Form layout="vertical">
                <Row gutter={[16, 16]}>
                  <Col md={12} sm={24} xs={24}>
                    <Form.Item label="节点 ID" required>
                      <Input
                        value={nodeDraft.id}
                        onChange={(event) => {
                          setNodeDraft((current) =>
                            current ? { ...current, id: event.target.value } : current,
                          );
                        }}
                      />
                    </Form.Item>
                  </Col>
                  <Col md={12} sm={24} xs={24}>
                    <Form.Item label="节点名称">
                      <Input
                        value={nodeDraft.name}
                        onChange={(event) => {
                          setNodeDraft((current) =>
                            current ? { ...current, name: event.target.value } : current,
                          );
                        }}
                      />
                    </Form.Item>
                  </Col>
                  <Col span={24}>
                    <Form.Item label="节点说明">
                      <Input.TextArea
                        rows={4}
                        value={nodeDraft.documentation}
                        onChange={(event) => {
                          setNodeDraft((current) =>
                            current
                              ? { ...current, documentation: event.target.value }
                              : current,
                          );
                        }}
                      />
                    </Form.Item>
                  </Col>
                </Row>
              </Form>
            ) : null}

            {supportsActivityBehavior(nodeDraft.elementType) ? (
              <Form layout="vertical">
                <Row gutter={[16, 16]}>
                  <Col md={12} sm={24} xs={24}>
                    <Form.Item label="异步执行">
                      <Switch
                        checked={nodeDraft.asyncEnabled}
                        checkedChildren="开"
                        unCheckedChildren="关"
                        onChange={(checked) => {
                          setNodeDraft((current) =>
                            current ? { ...current, asyncEnabled: checked } : current,
                          );
                        }}
                      />
                    </Form.Item>
                  </Col>
                  <Col md={12} sm={24} xs={24}>
                    <Form.Item label="跳过表达式">
                      <Input
                        value={nodeDraft.skipExpression}
                        onChange={(event) => {
                          setNodeDraft((current) =>
                            current
                              ? { ...current, skipExpression: event.target.value }
                              : current,
                          );
                        }}
                        placeholder="例如 ${skipCurrentNode}"
                      />
                    </Form.Item>
                  </Col>
                </Row>
              </Form>
            ) : null}

            {nodeDraft.elementType === 'bpmn:StartEvent' ? (
              <Form layout="vertical">
                <Row gutter={[16, 16]}>
                  <Col span={24}>
                    <Form.Item label="发起人变量">
                      <Input
                        value={nodeDraft.initiator}
                        onChange={(event) => {
                          setNodeDraft((current) =>
                            current ? { ...current, initiator: event.target.value } : current,
                          );
                        }}
                        placeholder="例如 initiatorId"
                      />
                    </Form.Item>
                  </Col>
                </Row>
              </Form>
            ) : null}

            {nodeDraft.elementType === 'bpmn:UserTask' ? (
              <Form layout="vertical">
                <Row gutter={[16, 16]}>
                  <Col md={12} sm={24} xs={24}>
                    <Form.Item label="办理人">
                      <Input
                        value={nodeDraft.assignee}
                        onChange={(event) => {
                          setNodeDraft((current) =>
                            current ? { ...current, assignee: event.target.value } : current,
                          );
                        }}
                        placeholder="例如 ${approver}"
                      />
                    </Form.Item>
                  </Col>
                  <Col md={12} sm={24} xs={24}>
                    <Form.Item label="任务拥有者">
                      <Input
                        value={nodeDraft.owner}
                        onChange={(event) => {
                          setNodeDraft((current) =>
                            current ? { ...current, owner: event.target.value } : current,
                          );
                        }}
                      />
                    </Form.Item>
                  </Col>
                  <Col md={12} sm={24} xs={24}>
                    <Form.Item label="候选用户">
                      <Input
                        value={nodeDraft.candidateUsers}
                        onChange={(event) => {
                          setNodeDraft((current) =>
                            current
                              ? { ...current, candidateUsers: event.target.value }
                              : current,
                          );
                        }}
                        placeholder="多个用户使用逗号分隔"
                      />
                    </Form.Item>
                  </Col>
                  <Col md={12} sm={24} xs={24}>
                    <Form.Item label="候选角色">
                      <Input
                        value={nodeDraft.candidateGroups}
                        onChange={(event) => {
                          setNodeDraft((current) =>
                            current
                              ? { ...current, candidateGroups: event.target.value }
                              : current,
                          );
                        }}
                        placeholder="多个角色使用逗号分隔"
                      />
                    </Form.Item>
                  </Col>
                  <Col md={12} sm={24} xs={24}>
                    <Form.Item label="优先级">
                      <Input
                        value={nodeDraft.priority}
                        onChange={(event) => {
                          setNodeDraft((current) =>
                            current ? { ...current, priority: event.target.value } : current,
                          );
                        }}
                      />
                    </Form.Item>
                  </Col>
                  <Col md={12} sm={24} xs={24}>
                    <Form.Item label="到期时间">
                      <Input
                        value={nodeDraft.dueDate}
                        onChange={(event) => {
                          setNodeDraft((current) =>
                            current ? { ...current, dueDate: event.target.value } : current,
                          );
                        }}
                        placeholder="例如 ${dueDate}"
                      />
                    </Form.Item>
                  </Col>
                  <Col span={24}>
                    <Form.Item label="任务分类">
                      <Input
                        value={nodeDraft.category}
                        onChange={(event) => {
                          setNodeDraft((current) =>
                            current ? { ...current, category: event.target.value } : current,
                          );
                        }}
                      />
                    </Form.Item>
                  </Col>
                </Row>
              </Form>
            ) : null}

            {nodeDraft.elementType === 'bpmn:ServiceTask' ? (
              <Form layout="vertical">
                <Row gutter={[16, 16]}>
                  <Col span={24}>
                    <Form.Item label="Java 类">
                      <Input
                        value={nodeDraft.delegateClass}
                        onChange={(event) => {
                          setNodeDraft((current) =>
                            current
                              ? { ...current, delegateClass: event.target.value }
                              : current,
                          );
                        }}
                        placeholder="例如 com.example.workflow.TaskDelegate"
                      />
                    </Form.Item>
                  </Col>
                  <Col span={24}>
                    <Form.Item label="委托表达式">
                      <Input
                        value={nodeDraft.delegateExpression}
                        onChange={(event) => {
                          setNodeDraft((current) =>
                            current
                              ? { ...current, delegateExpression: event.target.value }
                              : current,
                          );
                        }}
                        placeholder="例如 ${taskDelegate}"
                      />
                    </Form.Item>
                  </Col>
                  <Col span={24}>
                    <Form.Item label="执行表达式">
                      <Input
                        value={nodeDraft.expression}
                        onChange={(event) => {
                          setNodeDraft((current) =>
                            current ? { ...current, expression: event.target.value } : current,
                          );
                        }}
                        placeholder="例如 ${bean.execute(execution)}"
                      />
                    </Form.Item>
                  </Col>
                  <Col span={24}>
                    <Form.Item label="结果变量">
                      <Input
                        value={nodeDraft.resultVariableName}
                        onChange={(event) => {
                          setNodeDraft((current) =>
                            current
                              ? { ...current, resultVariableName: event.target.value }
                              : current,
                          );
                        }}
                      />
                    </Form.Item>
                  </Col>
                </Row>
              </Form>
            ) : null}

            {nodeDraft.elementType === 'bpmn:SequenceFlow' ? (
              <Form layout="vertical">
                <Form.Item label="条件表达式">
                  <Input.TextArea
                    rows={4}
                    value={nodeDraft.conditionExpression}
                    onChange={(event) => {
                      setNodeDraft((current) =>
                        current
                          ? { ...current, conditionExpression: event.target.value }
                          : current,
                      );
                    }}
                    placeholder="例如 ${approved == true}"
                  />
                </Form.Item>
              </Form>
            ) : null}

            {isFormAssociableNode(nodeDraft.elementType) ? (
              <Card className="saas-workflow-designer__section-card" size="small" title="节点关联表单">
                <Space direction="vertical" size={12} style={{ display: 'flex' }}>
                  <Select
                    allowClear
                    options={designerForms.map((item) => ({
                      label: `${item.name} (${item.id})`,
                      value: item.id,
                    }))}
                    placeholder="请选择已经设计好的流程表单"
                    value={nodeDraft.associatedFormId}
                    onChange={(value) => {
                      setNodeDraft((current) =>
                        current
                          ? {
                              ...current,
                              associatedFormId: value,
                              customFormKey: value ? undefined : current.customFormKey,
                            }
                          : current,
                      );
                    }}
                  />
                  <Input
                    allowClear
                    disabled={Boolean(nodeDraft.associatedFormId)}
                    placeholder="也可以直接填写外部 formKey"
                    value={nodeDraft.customFormKey}
                    onChange={(event) => {
                      setNodeDraft((current) =>
                        current
                          ? { ...current, customFormKey: event.target.value }
                          : current,
                      );
                    }}
                  />
                  <Typography.Text type="secondary">
                    选择内置表单后会把节点 `flowable:formKey` 写成 `form:表单ID`。
                  </Typography.Text>
                </Space>
              </Card>
            ) : null}

            {supportsExecutionListeners(nodeDraft.elementType) ? (
              <Card
                className="saas-workflow-designer__section-card"
                extra={
                  <Button icon={<PlusOutlined />} size="small" type="link" onClick={() => openListenerEditor('execution')}>
                    新增
                  </Button>
                }
                size="small"
                title="执行监听器"
              >
                {renderPropertyList(
                  nodeDraft.executionListeners,
                  (listener, index) => (
                    <div className="saas-workflow-designer__list-item" key={listener.key}>
                      <div className="saas-workflow-designer__list-item-header">
                        <Space wrap>
                          <Tag color="blue">{listener.event || '未设置事件'}</Tag>
                          <Tag>{listener.implementationType}</Tag>
                        </Space>
                        <Space size={4}>
                          <Button
                            icon={<EditOutlined />}
                            size="small"
                            type="text"
                            onClick={() => openListenerEditor('execution', index)}
                          />
                          <Button
                            danger
                            icon={<DeleteOutlined />}
                            size="small"
                            type="text"
                            onClick={() => handleDeleteListener('execution', index)}
                          />
                        </Space>
                      </div>
                      <Typography.Text ellipsis={{ tooltip: listener.implementation }}>
                        {listener.implementation || '-'}
                      </Typography.Text>
                    </div>
                  ),
                  '暂无执行监听器',
                )}
              </Card>
            ) : null}

            {supportsTaskListeners(nodeDraft.elementType) ? (
              <Card
                className="saas-workflow-designer__section-card"
                extra={
                  <Button icon={<PlusOutlined />} size="small" type="link" onClick={() => openListenerEditor('task')}>
                    新增
                  </Button>
                }
                size="small"
                title="任务监听器"
              >
                {renderPropertyList(
                  nodeDraft.taskListeners,
                  (listener, index) => (
                    <div className="saas-workflow-designer__list-item" key={listener.key}>
                      <div className="saas-workflow-designer__list-item-header">
                        <Space wrap>
                          <Tag color="purple">{listener.event || '未设置事件'}</Tag>
                          <Tag>{listener.implementationType}</Tag>
                        </Space>
                        <Space size={4}>
                          <Button
                            icon={<EditOutlined />}
                            size="small"
                            type="text"
                            onClick={() => openListenerEditor('task', index)}
                          />
                          <Button
                            danger
                            icon={<DeleteOutlined />}
                            size="small"
                            type="text"
                            onClick={() => handleDeleteListener('task', index)}
                          />
                        </Space>
                      </div>
                      <Typography.Text ellipsis={{ tooltip: listener.implementation }}>
                        {listener.implementation || '-'}
                      </Typography.Text>
                    </div>
                  ),
                  '暂无任务监听器',
                )}
              </Card>
            ) : null}

            <Card
              className="saas-workflow-designer__section-card"
              extra={
                <Button icon={<PlusOutlined />} size="small" type="link" onClick={() => openExtensionPropertyEditor()}>
                  新增
                </Button>
              }
              size="small"
              title="扩展属性"
            >
              {renderPropertyList(
                nodeDraft.extensionProperties,
                (property, index) => (
                  <div className="saas-workflow-designer__list-item" key={property.key}>
                    <div className="saas-workflow-designer__list-item-header">
                      <Typography.Text strong>{property.name}</Typography.Text>
                      <Space size={4}>
                        <Button
                          icon={<EditOutlined />}
                          size="small"
                          type="text"
                          onClick={() => openExtensionPropertyEditor(index)}
                        />
                        <Button
                          danger
                          icon={<DeleteOutlined />}
                          size="small"
                          type="text"
                          onClick={() => handleDeleteExtensionProperty(index)}
                        />
                      </Space>
                    </div>
                    <Typography.Text type="secondary">
                      {property.value || '未设置属性值'}
                    </Typography.Text>
                  </div>
                ),
                '暂无扩展属性',
              )}
            </Card>
          </Space>
        )}
      </Modal>

      <Modal
        destroyOnHidden
        open={Boolean(listenerEditor)}
        title={
          listenerEditor?.kind === 'task'
            ? listenerEditor?.index === undefined
              ? '新增任务监听器'
              : '编辑任务监听器'
            : listenerEditor?.index === undefined
              ? '新增执行监听器'
              : '编辑执行监听器'
        }
        onCancel={() => {
          setListenerEditor(undefined);
        }}
        onOk={() => void handleSaveListenerEditor()}
      >
        <Form<ListenerFormValues> form={listenerForm} layout="vertical">
          <Form.Item label="监听事件" name="event" rules={[{ required: true, message: '请选择监听事件' }]}>
            <Select
              options={
                listenerEditor?.kind === 'task'
                  ? TASK_LISTENER_EVENT_OPTIONS
                  : EXECUTION_LISTENER_EVENT_OPTIONS
              }
              placeholder="请选择监听事件"
            />
          </Form.Item>
          <Form.Item
            label="实现方式"
            name="implementationType"
            rules={[{ required: true, message: '请选择实现方式' }]}
          >
            <Select
              options={[
                { label: 'Java 类', value: 'class' },
                { label: '委托表达式', value: 'delegateExpression' },
                { label: '表达式', value: 'expression' },
              ]}
            />
          </Form.Item>
          <Form.Item
            label="实现内容"
            name="implementation"
            rules={[{ required: true, message: '请输入实现内容' }]}
          >
            <Input.TextArea rows={3} />
          </Form.Item>
        </Form>
      </Modal>

      <Modal
        destroyOnHidden
        open={Boolean(extensionPropertyEditor)}
        title={
          extensionPropertyEditor?.index === undefined ? '新增扩展属性' : '编辑扩展属性'
        }
        onCancel={() => {
          setExtensionPropertyEditor(undefined);
        }}
        onOk={() => void handleSaveExtensionPropertyEditor()}
      >
        <Form<ExtensionPropertyFormValues> form={extensionPropertyForm} layout="vertical">
          <Form.Item label="属性名" name="name" rules={[{ required: true, message: '请输入属性名' }]}>
            <Input allowClear placeholder="例如 uiColor" />
          </Form.Item>
          <Form.Item label="属性值" name="value">
            <Input.TextArea allowClear rows={3} placeholder="例如 success" />
          </Form.Item>
        </Form>
      </Modal>
    </PageContainer>
  );
};

export default WorkflowDesignerPage;
