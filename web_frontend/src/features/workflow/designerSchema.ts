export interface WorkflowProcessSettings {
  candidateStarterGroups?: string;
  candidateStarterUsers?: string;
  documentation?: string;
  isExecutable?: boolean;
  processId?: string;
  processName?: string;
}

export interface WorkflowDesignerFormField {
  id: string;
  key: string;
  label: string;
  placeholder?: string;
  required?: boolean;
  type: string;
}

export interface WorkflowDesignerForm {
  description?: string;
  fields: WorkflowDesignerFormField[];
  id: string;
  key: string;
  name: string;
}

const BPMN_NS = 'http://www.omg.org/spec/BPMN/20100524/MODEL';
const FLOWABLE_NS = 'http://flowable.org/bpmn';
export const WORKFLOW_FORM_STORAGE_PROPERTY_NAME = 'adminTemplateForms';

const getProcessNode = (documentNode: Document) =>
  documentNode.getElementsByTagName('bpmn:process')[0] ||
  documentNode.getElementsByTagName('process')[0];

const getElementChildrenByLocalName = (parent: Element | null, localName: string) =>
  Array.from(parent?.children || []).filter(
    (child) => child.localName === localName,
  );

const ensureExtensionElements = (documentNode: Document, processNode: Element) => {
  const existing = getElementChildrenByLocalName(processNode, 'extensionElements')[0];
  if (existing) {
    return existing;
  }

  const extensionElements = documentNode.createElementNS(
    BPMN_NS,
    'bpmn:extensionElements',
  );
  processNode.appendChild(extensionElements);
  return extensionElements;
};

const ensureFlowableProperties = (documentNode: Document, processNode: Element) => {
  const extensionElements = ensureExtensionElements(documentNode, processNode);
  const existing = getElementChildrenByLocalName(extensionElements, 'properties').find(
    (child) => child.namespaceURI === FLOWABLE_NS,
  );
  if (existing) {
    return existing;
  }

  const properties = documentNode.createElementNS(FLOWABLE_NS, 'flowable:properties');
  extensionElements.appendChild(properties);
  return properties;
};

const getDocumentationElement = (processNode: Element) =>
  getElementChildrenByLocalName(processNode, 'documentation')[0];

const upsertDocumentation = (
  documentNode: Document,
  processNode: Element,
  value?: string,
) => {
  const trimmed = value?.trim();
  const current = getDocumentationElement(processNode);

  if (!trimmed) {
    current?.remove();
    return;
  }

  if (current) {
    current.textContent = trimmed;
    return;
  }

  const documentation = documentNode.createElementNS(BPMN_NS, 'bpmn:documentation');
  documentation.textContent = trimmed;
  processNode.insertBefore(documentation, processNode.firstChild);
};

const findReservedFormProperty = (processNode: Element) => {
  const extensionElements = getElementChildrenByLocalName(processNode, 'extensionElements')[0];
  if (!extensionElements) {
    return undefined;
  }
  const propertiesNode = getElementChildrenByLocalName(extensionElements, 'properties').find(
    (child) => child.namespaceURI === FLOWABLE_NS,
  );
  if (!propertiesNode) {
    return undefined;
  }

  return getElementChildrenByLocalName(propertiesNode, 'property').find(
    (child) =>
      child.namespaceURI === FLOWABLE_NS &&
      child.getAttribute('name') === WORKFLOW_FORM_STORAGE_PROPERTY_NAME,
  );
};

export const parseWorkflowProcessSettings = (
  xml: string,
): WorkflowProcessSettings => {
  if (!xml) {
    return {};
  }

  const parser = new DOMParser();
  const documentNode = parser.parseFromString(xml, 'text/xml');
  const processNode = getProcessNode(documentNode);

  if (!processNode) {
    return {};
  }

  return {
    candidateStarterGroups:
      processNode.getAttribute('flowable:candidateStarterGroups') || undefined,
    candidateStarterUsers:
      processNode.getAttribute('flowable:candidateStarterUsers') || undefined,
    documentation: getDocumentationElement(processNode)?.textContent || undefined,
    isExecutable: processNode.getAttribute('isExecutable') !== 'false',
    processId: processNode.getAttribute('id') || undefined,
    processName: processNode.getAttribute('name') || undefined,
  };
};

export const updateWorkflowProcessSettingsXml = (
  xml: string,
  settings: WorkflowProcessSettings,
) => {
  const parser = new DOMParser();
  const documentNode = parser.parseFromString(xml, 'text/xml');
  const processNode = getProcessNode(documentNode);

  if (!processNode) {
    return xml;
  }

  const applyAttr = (qualifiedName: string, value?: string) => {
    const trimmed = value?.trim();
    if (trimmed) {
      processNode.setAttribute(qualifiedName, trimmed);
    } else {
      processNode.removeAttribute(qualifiedName);
    }
  };

  applyAttr('id', settings.processId);
  applyAttr('name', settings.processName);
  processNode.setAttribute(
    'isExecutable',
    settings.isExecutable === false ? 'false' : 'true',
  );
  applyAttr('flowable:candidateStarterUsers', settings.candidateStarterUsers);
  applyAttr('flowable:candidateStarterGroups', settings.candidateStarterGroups);
  upsertDocumentation(documentNode, processNode, settings.documentation);

  return new XMLSerializer().serializeToString(documentNode);
};

export const parseWorkflowDesignerForms = (xml: string): WorkflowDesignerForm[] => {
  if (!xml) {
    return [];
  }

  const parser = new DOMParser();
  const documentNode = parser.parseFromString(xml, 'text/xml');
  const processNode = getProcessNode(documentNode);
  if (!processNode) {
    return [];
  }

  const propertyNode = findReservedFormProperty(processNode);
  const rawValue = propertyNode?.getAttribute('value');
  if (!rawValue) {
    return [];
  }

  try {
    const parsed = JSON.parse(rawValue);
    if (!Array.isArray(parsed)) {
      return [];
    }
    return parsed
      .filter(
        (item: any) => item && typeof item.id === 'string' && typeof item.name === 'string',
      )
      .map((item: any, index: number) => ({
        description:
          typeof item.description === 'string' ? item.description : undefined,
        fields: Array.isArray(item.fields)
          ? item.fields
              .filter(
                (field: any) =>
                  field &&
                  typeof field.id === 'string' &&
                  typeof field.label === 'string' &&
                  typeof field.type === 'string',
              )
              .map((field: any, fieldIndex: number) => ({
                id: field.id,
                key: `${item.id}-field-${fieldIndex}-${field.id}`,
                label: field.label,
                placeholder:
                  typeof field.placeholder === 'string'
                    ? field.placeholder
                    : undefined,
                required: Boolean(field.required),
                type: field.type,
              }))
          : [],
        id: item.id,
        key: `designer-form-${index}-${item.id}`,
        name: item.name,
      }));
  } catch (_error) {
    return [];
  }
};

export const updateWorkflowDesignerFormsXml = (
  xml: string,
  forms: WorkflowDesignerForm[],
) => {
  const parser = new DOMParser();
  const documentNode = parser.parseFromString(xml, 'text/xml');
  const processNode = getProcessNode(documentNode);
  if (!processNode) {
    return xml;
  }

  const cleanForms = forms.map((form) => ({
    description: form.description?.trim() || undefined,
    fields: form.fields.map((field) => ({
      id: field.id.trim(),
      label: field.label.trim(),
      placeholder: field.placeholder?.trim() || undefined,
      required: Boolean(field.required),
      type: field.type.trim(),
    })),
    id: form.id.trim(),
    name: form.name.trim(),
  }));

  const currentProperty = findReservedFormProperty(processNode);
  if (!cleanForms.length) {
    currentProperty?.remove();
    return new XMLSerializer().serializeToString(documentNode);
  }

  const propertiesNode = ensureFlowableProperties(documentNode, processNode);
  const nextValue = JSON.stringify(cleanForms);

  if (currentProperty) {
    currentProperty.setAttribute('value', nextValue);
  } else {
    const propertyNode = documentNode.createElementNS(
      FLOWABLE_NS,
      'flowable:property',
    );
    propertyNode.setAttribute('name', WORKFLOW_FORM_STORAGE_PROPERTY_NAME);
    propertyNode.setAttribute('value', nextValue);
    propertiesNode.appendChild(propertyNode);
  }

  return new XMLSerializer().serializeToString(documentNode);
};

export const buildAssociatedFormKey = (formId?: string) =>
  formId ? `form:${formId}` : undefined;

export const parseAssociatedFormId = (formKey?: string) => {
  if (!formKey) {
    return undefined;
  }
  if (formKey.startsWith('form:')) {
    return formKey.slice(5);
  }
  return undefined;
};

export const replaceAssociatedFormInXml = (
  xml: string,
  sourceFormId: string,
  targetFormId?: string,
) => {
  if (!xml || !sourceFormId.trim()) {
    return xml;
  }

  const parser = new DOMParser();
  const documentNode = parser.parseFromString(xml, 'text/xml');
  const sourceKey = buildAssociatedFormKey(sourceFormId.trim());
  const targetKey = buildAssociatedFormKey(targetFormId?.trim());

  Array.from(documentNode.getElementsByTagName('*')).forEach((element) => {
    if (element.getAttribute('flowable:formKey') !== sourceKey) {
      return;
    }

    if (targetKey) {
      element.setAttribute('flowable:formKey', targetKey);
    } else {
      element.removeAttribute('flowable:formKey');
    }
  });

  return new XMLSerializer().serializeToString(documentNode);
};
