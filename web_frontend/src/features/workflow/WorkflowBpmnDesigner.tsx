import BpmnModeler from 'bpmn-js/lib/Modeler';
import { layoutProcess } from 'bpmn-auto-layout';
import React, {
  forwardRef,
  useEffect,
  useImperativeHandle,
  useRef,
  useState,
} from 'react';
import { WORKFLOW_FORM_STORAGE_PROPERTY_NAME } from './designerSchema';
import { createDefaultWorkflowDiagram } from './defaultWorkflowDiagram';
import flowableModdleDescriptor from './flowableModdleDescriptor';

import 'bpmn-js/dist/assets/diagram-js.css';
import 'bpmn-js/dist/assets/bpmn-js.css';
import 'bpmn-js/dist/assets/bpmn-font/css/bpmn-embedded.css';

type DiagramElement = any;
type ModelerInstance = any;

export type ListenerKind = 'execution' | 'task';

export type WorkflowEditableField =
  | 'id'
  | 'name'
  | 'documentation'
  | 'isExecutable'
  | 'conditionExpression'
  | 'initiator'
  | 'candidateStarterUsers'
  | 'candidateStarterGroups'
  | 'assignee'
  | 'owner'
  | 'candidateUsers'
  | 'candidateGroups'
  | 'formKey'
  | 'priority'
  | 'dueDate'
  | 'category'
  | 'asyncEnabled'
  | 'skipExpression'
  | 'delegateClass'
  | 'delegateExpression'
  | 'expression'
  | 'resultVariableName';

export interface WorkflowDesignerProcessMeta {
  processId?: string;
  processName?: string;
  isExecutable?: boolean;
}

export interface WorkflowListenerDefinition {
  event?: string;
  implementation?: string;
  implementationType: 'class' | 'delegateExpression' | 'expression';
  key: string;
}

export interface WorkflowExtensionPropertyDefinition {
  key: string;
  name?: string;
  value?: string;
}

export interface WorkflowSelectedElementState {
  assignee?: string;
  asyncEnabled?: boolean;
  candidateGroups?: string;
  candidateStarterGroups?: string;
  candidateStarterUsers?: string;
  candidateUsers?: string;
  category?: string;
  conditionExpression?: string;
  delegateClass?: string;
  delegateExpression?: string;
  documentation?: string;
  dueDate?: string;
  elementType: string;
  executionListeners: WorkflowListenerDefinition[];
  extensionProperties: WorkflowExtensionPropertyDefinition[];
  expression?: string;
  formKey?: string;
  id: string;
  initiator?: string;
  isExecutable?: boolean;
  name?: string;
  owner?: string;
  priority?: string;
  resultVariableName?: string;
  skipExpression?: string;
  taskListeners: WorkflowListenerDefinition[];
  typeLabel: string;
}

export interface WorkflowBpmnDesignerHandle {
  applyFieldValue: (field: WorkflowEditableField, value: unknown) => void;
  fitViewport: () => void;
  getSelectedState: () => WorkflowSelectedElementState | undefined;
  redo: () => void;
  replaceExtensionProperties: (
    properties: WorkflowExtensionPropertyDefinition[],
  ) => void;
  replaceListeners: (
    kind: ListenerKind,
    listeners: WorkflowListenerDefinition[],
  ) => void;
  saveSvg: () => Promise<string>;
  undo: () => void;
  zoomIn: () => void;
  zoomOut: () => void;
}

interface WorkflowBpmnDesignerProps {
  onImportError?: (error: Error) => void;
  onProcessMetaChange?: (meta: WorkflowDesignerProcessMeta) => void;
  onSelectionChange?: (state?: WorkflowSelectedElementState) => void;
  onXmlChange?: (xml: string) => void;
  xml: string;
}

const DEFAULT_XML = createDefaultWorkflowDiagram();

const getBusinessObject = (element: DiagramElement) =>
  element?.businessObject || element;

const getDocumentationText = (businessObject: any) =>
  businessObject?.documentation?.[0]?.text || '';

const getConditionExpression = (businessObject: any) =>
  businessObject?.conditionExpression?.body || '';

const getExtensionAttribute = (businessObject: any, name: string) =>
  businessObject?.get?.(`flowable:${name}`) ??
  businessObject?.$attrs?.[`flowable:${name}`] ??
  '';

const getExtensionElements = (businessObject: any) =>
  businessObject?.extensionElements?.get?.('values') ||
  businessObject?.extensionElements?.values ||
  [];

const isType = (moddleElement: any, type: string) =>
  moddleElement?.$type === type || moddleElement?.$instanceOf?.(type);

const parseBooleanValue = (value: unknown, defaultValue = false) => {
  if (typeof value === 'boolean') {
    return value;
  }
  if (typeof value === 'string') {
    return value === 'true';
  }
  return defaultValue;
};

const getImplementationType = (listener: any) => {
  if (listener?.get?.('delegateExpression') || listener?.delegateExpression) {
    return 'delegateExpression' as const;
  }
  if (listener?.get?.('expression') || listener?.expression) {
    return 'expression' as const;
  }
  return 'class' as const;
};

const getImplementationValue = (listener: any) => {
  const implementationType = getImplementationType(listener);
  if (implementationType === 'delegateExpression') {
    return listener?.get?.('delegateExpression') || listener?.delegateExpression || '';
  }
  if (implementationType === 'expression') {
    return listener?.get?.('expression') || listener?.expression || '';
  }
  return listener?.get?.('class') || listener?.class || '';
};

const getFlowableListeners = (
  businessObject: any,
  listenerType: 'flowable:ExecutionListener' | 'flowable:TaskListener',
): WorkflowListenerDefinition[] =>
  getExtensionElements(businessObject)
    .filter((value: any) => isType(value, listenerType))
    .map((listener: any, index: number) => ({
      event: listener?.get?.('event') || listener?.event || undefined,
      implementation: getImplementationValue(listener),
      implementationType: getImplementationType(listener),
      key: `${listenerType}-${index}-${listener?.id || listener?.event || 'listener'}`,
    }));

const getFlowableExtensionProperties = (
  businessObject: any,
): WorkflowExtensionPropertyDefinition[] => {
  const propertyContainer = getExtensionElements(businessObject).find((value: any) =>
    isType(value, 'flowable:Properties'),
  );
  const propertyValues = propertyContainer?.get?.('values') || propertyContainer?.values || [];

  return propertyValues
    .filter(
      (property: any) =>
        (property?.get?.('name') || property?.name) !== WORKFLOW_FORM_STORAGE_PROPERTY_NAME,
    )
    .map((property: any, index: number) => ({
      key: `extension-property-${index}-${property?.name || 'property'}`,
      name: property?.get?.('name') || property?.name || undefined,
      value: property?.get?.('value') || property?.value || undefined,
    }));
};

const getTypeLabel = (type: string) => {
  switch (type) {
    case 'bpmn:Process':
      return '流程';
    case 'bpmn:StartEvent':
      return '开始事件';
    case 'bpmn:EndEvent':
      return '结束事件';
    case 'bpmn:UserTask':
      return '用户任务';
    case 'bpmn:ServiceTask':
      return '服务任务';
    case 'bpmn:SubProcess':
      return '子流程';
    case 'bpmn:CallActivity':
      return '调用活动';
    case 'bpmn:ExclusiveGateway':
      return '排他网关';
    case 'bpmn:ParallelGateway':
      return '并行网关';
    case 'bpmn:SequenceFlow':
      return '连接线';
    default:
      return type.replace('bpmn:', '') || '节点';
  }
};

const parseProcessMetaFromXml = (xml: string): WorkflowDesignerProcessMeta => {
  if (!xml) {
    return {};
  }

  const parser = new DOMParser();
  const documentNode = parser.parseFromString(xml, 'text/xml');
  const processNode =
    documentNode.getElementsByTagName('bpmn:process')[0] ||
    documentNode.getElementsByTagName('process')[0];

  if (!processNode) {
    return {};
  }

  return {
    processId: processNode.getAttribute('id') || undefined,
    processName: processNode.getAttribute('name') || undefined,
    isExecutable: processNode.getAttribute('isExecutable') !== 'false',
  };
};

const buildElementState = (element: DiagramElement): WorkflowSelectedElementState => {
  const businessObject = getBusinessObject(element);
  const elementType = businessObject?.$type || element?.type || 'bpmn:Process';

  return {
    assignee: getExtensionAttribute(businessObject, 'assignee'),
    asyncEnabled: parseBooleanValue(getExtensionAttribute(businessObject, 'async')),
    candidateGroups: getExtensionAttribute(businessObject, 'candidateGroups'),
    candidateStarterGroups: getExtensionAttribute(
      businessObject,
      'candidateStarterGroups',
    ),
    candidateStarterUsers: getExtensionAttribute(
      businessObject,
      'candidateStarterUsers',
    ),
    candidateUsers: getExtensionAttribute(businessObject, 'candidateUsers'),
    category: getExtensionAttribute(businessObject, 'category'),
    conditionExpression: getConditionExpression(businessObject),
    delegateClass: getExtensionAttribute(businessObject, 'class'),
    delegateExpression: getExtensionAttribute(businessObject, 'delegateExpression'),
    documentation: getDocumentationText(businessObject),
    dueDate: getExtensionAttribute(businessObject, 'dueDate'),
    elementType,
    executionListeners: getFlowableListeners(
      businessObject,
      'flowable:ExecutionListener',
    ),
    extensionProperties: getFlowableExtensionProperties(businessObject),
    expression: getExtensionAttribute(businessObject, 'expression'),
    formKey: getExtensionAttribute(businessObject, 'formKey'),
    id: businessObject?.id || '',
    initiator: getExtensionAttribute(businessObject, 'initiator'),
    isExecutable:
      elementType === 'bpmn:Process' ? businessObject?.isExecutable !== false : undefined,
    name: businessObject?.name || '',
    owner: getExtensionAttribute(businessObject, 'owner'),
    priority: getExtensionAttribute(businessObject, 'priority'),
    resultVariableName: getExtensionAttribute(businessObject, 'resultVariableName'),
    skipExpression: getExtensionAttribute(businessObject, 'skipExpression'),
    taskListeners: getFlowableListeners(businessObject, 'flowable:TaskListener'),
    typeLabel: getTypeLabel(elementType),
  };
};

const WorkflowBpmnDesigner = forwardRef<
  WorkflowBpmnDesignerHandle,
  WorkflowBpmnDesignerProps
>(({ onImportError, onProcessMetaChange, onSelectionChange, onXmlChange, xml }, ref) => {
  const canvasRef = useRef<HTMLDivElement>(null);
  const modelerRef = useRef<ModelerInstance | undefined>(undefined);
  const selectedElementRef = useRef<DiagramElement | undefined>(undefined);
  const importInProgressRef = useRef(false);
  const lastImportedXmlRef = useRef('');
  const lastEmittedXmlRef = useRef('');
  const saveTimerRef = useRef<number | undefined>(undefined);
  const [selectedState, setSelectedState] = useState<WorkflowSelectedElementState>();

  const emitSelectionState = (element?: DiagramElement) => {
    if (!element) {
      setSelectedState(undefined);
      onSelectionChange?.(undefined);
      return undefined;
    }

    const nextState = buildElementState(element);
    setSelectedState(nextState);
    onSelectionChange?.(nextState);
    return nextState;
  };

  const refreshSelectedState = () => emitSelectionState(selectedElementRef.current);

  const syncXml = async () => {
    const modeler = modelerRef.current;
    if (!modeler || importInProgressRef.current) {
      return;
    }

    const result = await modeler.saveXML({ format: true });
    const nextXml = result.xml || '';
    if (!nextXml || nextXml === lastEmittedXmlRef.current) {
      return;
    }

    lastEmittedXmlRef.current = nextXml;
    lastImportedXmlRef.current = nextXml;
    onXmlChange?.(nextXml);
    onProcessMetaChange?.(parseProcessMetaFromXml(nextXml));
  };

  const queueXmlSync = () => {
    if (typeof window === 'undefined') {
      return;
    }
    if (saveTimerRef.current) {
      window.clearTimeout(saveTimerRef.current);
    }
    saveTimerRef.current = window.setTimeout(() => {
      void syncXml();
    }, 120);
  };

  const importDiagram = async (nextXml: string) => {
    const modeler = modelerRef.current;
    if (!modeler) {
      return;
    }

    let resolvedXml = nextXml || DEFAULT_XML;
    importInProgressRef.current = true;
    try {
      try {
        await modeler.importXML(resolvedXml);
      } catch (error) {
        const resolvedError =
          error instanceof Error ? error : new Error('流程 XML 导入失败');
        if (!resolvedError.message.includes('no diagram to display')) {
          throw resolvedError;
        }
        resolvedXml = await layoutProcess(resolvedXml);
        await modeler.importXML(resolvedXml);
      }
      const canvas = modeler.get('canvas');
      const rootElement = canvas.getRootElement();
      canvas.zoom('fit-viewport');
      selectedElementRef.current = rootElement;
      emitSelectionState(rootElement);
      lastImportedXmlRef.current = resolvedXml;
      lastEmittedXmlRef.current = resolvedXml;
      onProcessMetaChange?.(parseProcessMetaFromXml(resolvedXml));
      if (resolvedXml !== nextXml) {
        onXmlChange?.(resolvedXml);
      }
    } catch (error) {
      const resolvedError =
        error instanceof Error ? error : new Error('流程 XML 导入失败');
      onImportError?.(resolvedError);
    } finally {
      importInProgressRef.current = false;
    }
  };

  const writeExtensionValues = (replaceTypes: string[], nextTypedValues: any[]) => {
    const modeler = modelerRef.current;
    const currentElement = selectedElementRef.current;
    if (!modeler || !currentElement) {
      return;
    }

    const businessObject = getBusinessObject(currentElement);
    const modeling = modeler.get('modeling');
    const bpmnFactory = modeler.get('bpmnFactory');
    const existingExtensionElements = businessObject?.get?.('extensionElements');
    const existingValues = existingExtensionElements?.get?.('values') || [];
    const preservedValues = existingValues.filter(
      (value: any) => !replaceTypes.some((type) => isType(value, type)),
    );
    const finalValues = [...preservedValues, ...nextTypedValues];

    if (!finalValues.length) {
      if (existingExtensionElements) {
        modeling.updateModdleProperties(currentElement, businessObject, {
          extensionElements: undefined,
        });
      }
      refreshSelectedState();
      return;
    }

    if (!existingExtensionElements) {
      const nextExtensionElements = bpmnFactory.create('bpmn:ExtensionElements', {
        values: finalValues,
      });
      nextExtensionElements.$parent = businessObject;
      finalValues.forEach((value: any) => {
        value.$parent = nextExtensionElements;
        (value.values || []).forEach((child: any) => {
          child.$parent = value;
        });
      });
      modeling.updateModdleProperties(currentElement, businessObject, {
        extensionElements: nextExtensionElements,
      });
      refreshSelectedState();
      return;
    }

    finalValues.forEach((value: any) => {
      value.$parent = existingExtensionElements;
      (value.values || []).forEach((child: any) => {
        child.$parent = value;
      });
    });
    modeling.updateModdleProperties(currentElement, existingExtensionElements, {
      values: finalValues,
    });
    refreshSelectedState();
  };

  const replaceListeners = (
    kind: ListenerKind,
    listeners: WorkflowListenerDefinition[],
  ) => {
    const modeler = modelerRef.current;
    if (!modeler) {
      return;
    }

    const bpmnFactory = modeler.get('bpmnFactory');
    const typeName =
      kind === 'execution' ? 'flowable:ExecutionListener' : 'flowable:TaskListener';
    const nextValues = listeners
      .filter((listener) => listener.event && listener.implementation)
      .map((listener) =>
        bpmnFactory.create(typeName, {
          event: listener.event,
          class:
            listener.implementationType === 'class'
              ? listener.implementation
              : undefined,
          delegateExpression:
            listener.implementationType === 'delegateExpression'
              ? listener.implementation
              : undefined,
          expression:
            listener.implementationType === 'expression'
              ? listener.implementation
              : undefined,
        }),
      );

    writeExtensionValues([typeName], nextValues);
  };

  const replaceExtensionProperties = (
    properties: WorkflowExtensionPropertyDefinition[],
  ) => {
    const modeler = modelerRef.current;
    const currentElement = selectedElementRef.current;
    if (!modeler || !currentElement) {
      return;
    }

    const businessObject = getBusinessObject(currentElement);
    const bpmnFactory = modeler.get('bpmnFactory');
    const propertyContainer = getExtensionElements(businessObject).find((value: any) =>
      isType(value, 'flowable:Properties'),
    );
    const propertyValues = propertyContainer?.get?.('values') || propertyContainer?.values || [];
    const reservedProperties = propertyValues.filter(
      (property: any) =>
        (property?.get?.('name') || property?.name) === WORKFLOW_FORM_STORAGE_PROPERTY_NAME,
    );
    const editableProperties = properties
      .filter((property) => property.name)
      .map((property) =>
        bpmnFactory.create('flowable:Property', {
          name: property.name,
          value: property.value || undefined,
        }),
      );
    const nextProperties = [...reservedProperties, ...editableProperties];
    const nextValues = nextProperties.length
      ? [
          bpmnFactory.create('flowable:Properties', {
            values: nextProperties,
          }),
        ]
      : [];

    writeExtensionValues(['flowable:Properties'], nextValues);
  };

  const applyFieldValue = (field: WorkflowEditableField, rawValue: unknown) => {
    const modeler = modelerRef.current;
    const currentElement = selectedElementRef.current;
    if (!modeler || !currentElement) {
      return;
    }

    const businessObject = getBusinessObject(currentElement);
    const modeling = modeler.get('modeling');
    const bpmnFactory = modeler.get('bpmnFactory');
    const value = typeof rawValue === 'string' ? rawValue.trim() : rawValue;

    if (field === 'id' && typeof value === 'string' && !value) {
      refreshSelectedState();
      return;
    }

    switch (field) {
      case 'id':
        modeling.updateProperties(currentElement, { id: value });
        break;
      case 'name':
        modeling.updateProperties(currentElement, { name: value || undefined });
        break;
      case 'documentation':
        modeling.updateModdleProperties(currentElement, businessObject, {
          documentation: value
            ? [bpmnFactory.create('bpmn:Documentation', { text: value })]
            : [],
        });
        break;
      case 'isExecutable':
        modeling.updateProperties(currentElement, {
          isExecutable: Boolean(rawValue),
        });
        break;
      case 'conditionExpression':
        modeling.updateProperties(currentElement, {
          conditionExpression: value
            ? bpmnFactory.create('bpmn:FormalExpression', { body: value })
            : undefined,
        });
        break;
      case 'initiator':
        modeling.updateProperties(currentElement, {
          'flowable:initiator': value || undefined,
        });
        break;
      case 'candidateStarterUsers':
        modeling.updateProperties(currentElement, {
          'flowable:candidateStarterUsers': value || undefined,
        });
        break;
      case 'candidateStarterGroups':
        modeling.updateProperties(currentElement, {
          'flowable:candidateStarterGroups': value || undefined,
        });
        break;
      case 'assignee':
        modeling.updateProperties(currentElement, {
          'flowable:assignee': value || undefined,
        });
        break;
      case 'owner':
        modeling.updateProperties(currentElement, {
          'flowable:owner': value || undefined,
        });
        break;
      case 'candidateUsers':
        modeling.updateProperties(currentElement, {
          'flowable:candidateUsers': value || undefined,
        });
        break;
      case 'candidateGroups':
        modeling.updateProperties(currentElement, {
          'flowable:candidateGroups': value || undefined,
        });
        break;
      case 'formKey':
        modeling.updateProperties(currentElement, {
          'flowable:formKey': value || undefined,
        });
        break;
      case 'priority':
        modeling.updateProperties(currentElement, {
          'flowable:priority': value || undefined,
        });
        break;
      case 'dueDate':
        modeling.updateProperties(currentElement, {
          'flowable:dueDate': value || undefined,
        });
        break;
      case 'category':
        modeling.updateProperties(currentElement, {
          'flowable:category': value || undefined,
        });
        break;
      case 'asyncEnabled':
        modeling.updateProperties(currentElement, {
          'flowable:async': Boolean(rawValue),
        });
        break;
      case 'skipExpression':
        modeling.updateProperties(currentElement, {
          'flowable:skipExpression': value || undefined,
        });
        break;
      case 'delegateClass':
        modeling.updateProperties(currentElement, {
          'flowable:class': value || undefined,
        });
        break;
      case 'delegateExpression':
        modeling.updateProperties(currentElement, {
          'flowable:delegateExpression': value || undefined,
        });
        break;
      case 'expression':
        modeling.updateProperties(currentElement, {
          'flowable:expression': value || undefined,
        });
        break;
      case 'resultVariableName':
        modeling.updateProperties(currentElement, {
          'flowable:resultVariableName': value || undefined,
        });
        break;
      default:
        break;
    }

    refreshSelectedState();
  };

  useImperativeHandle(ref, () => ({
    applyFieldValue,
    fitViewport: () => {
      const canvas = modelerRef.current?.get('canvas');
      canvas?.zoom('fit-viewport');
    },
    getSelectedState: () => selectedState,
    redo: () => {
      modelerRef.current?.get('commandStack')?.redo();
    },
    replaceExtensionProperties,
    replaceListeners,
    saveSvg: async () => {
      const result = await modelerRef.current?.saveSVG();
      return result?.svg || '';
    },
    undo: () => {
      modelerRef.current?.get('commandStack')?.undo();
    },
    zoomIn: () => {
      const canvas = modelerRef.current?.get('canvas');
      const currentZoom = canvas?.zoom();
      if (typeof currentZoom === 'number') {
        canvas.zoom(Math.min(4, currentZoom + 0.1));
      }
    },
    zoomOut: () => {
      const canvas = modelerRef.current?.get('canvas');
      const currentZoom = canvas?.zoom();
      if (typeof currentZoom === 'number') {
        canvas.zoom(Math.max(0.2, currentZoom - 0.1));
      }
    },
  }));

  useEffect(() => {
    if (!canvasRef.current) {
      return undefined;
    }

    const modeler = new BpmnModeler({
      container: canvasRef.current,
      moddleExtensions: {
        flowable: flowableModdleDescriptor,
      },
    });

    modelerRef.current = modeler;

    const eventBus = modeler.get('eventBus') as any;
    const canvas = modeler.get('canvas') as any;

    eventBus.on('selection.changed', (event: { newSelection: DiagramElement[] }) => {
      const nextSelection = event.newSelection?.[0] || canvas.getRootElement();
      selectedElementRef.current = nextSelection;
      emitSelectionState(nextSelection);
    });

    eventBus.on('commandStack.changed', () => {
      queueXmlSync();
    });

    void importDiagram(xml || DEFAULT_XML);

    return () => {
      if (typeof window !== 'undefined' && saveTimerRef.current) {
        window.clearTimeout(saveTimerRef.current);
      }
      modeler.destroy();
      modelerRef.current = undefined;
    };
  }, []);

  useEffect(() => {
    if (!modelerRef.current) {
      return;
    }
    if (xml && xml !== lastImportedXmlRef.current) {
      void importDiagram(xml);
    }
  }, [xml]);

  return (
    <div className="saas-workflow-designer saas-workflow-designer--canvas-only">
      <div className="saas-workflow-designer__canvas-shell">
        <div className="saas-workflow-designer__canvas" ref={canvasRef} />
      </div>
    </div>
  );
});

WorkflowBpmnDesigner.displayName = 'WorkflowBpmnDesigner';

export default WorkflowBpmnDesigner;
