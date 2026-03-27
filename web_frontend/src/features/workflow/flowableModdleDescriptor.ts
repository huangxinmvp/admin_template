const flowableModdleDescriptor = {
  name: 'Flowable',
  prefix: 'flowable',
  uri: 'http://flowable.org/bpmn',
  xml: {
    tagAlias: 'lowerCase',
  },
  types: [
    {
      name: 'FlowableProcess',
      extends: ['bpmn:Process'],
      properties: [
        {
          name: 'candidateStarterUsers',
          isAttr: true,
          type: 'String',
        },
        {
          name: 'candidateStarterGroups',
          isAttr: true,
          type: 'String',
        },
      ],
    },
    {
      name: 'FlowableStartEvent',
      extends: ['bpmn:StartEvent'],
      properties: [
        {
          name: 'initiator',
          isAttr: true,
          type: 'String',
        },
        {
          name: 'formKey',
          isAttr: true,
          type: 'String',
        },
      ],
    },
    {
      name: 'FlowableActivity',
      extends: ['bpmn:Activity'],
      properties: [
        {
          name: 'async',
          isAttr: true,
          type: 'Boolean',
        },
        {
          name: 'exclusive',
          isAttr: true,
          type: 'Boolean',
        },
        {
          name: 'skipExpression',
          isAttr: true,
          type: 'String',
        },
      ],
    },
    {
      name: 'FlowableUserTask',
      extends: ['bpmn:UserTask'],
      properties: [
        {
          name: 'assignee',
          isAttr: true,
          type: 'String',
        },
        {
          name: 'candidateUsers',
          isAttr: true,
          type: 'String',
        },
        {
          name: 'candidateGroups',
          isAttr: true,
          type: 'String',
        },
        {
          name: 'formKey',
          isAttr: true,
          type: 'String',
        },
        {
          name: 'priority',
          isAttr: true,
          type: 'String',
        },
        {
          name: 'owner',
          isAttr: true,
          type: 'String',
        },
        {
          name: 'dueDate',
          isAttr: true,
          type: 'String',
        },
        {
          name: 'category',
          isAttr: true,
          type: 'String',
        },
      ],
    },
    {
      name: 'FlowableServiceTask',
      extends: ['bpmn:ServiceTask'],
      properties: [
        {
          name: 'class',
          isAttr: true,
          type: 'String',
        },
        {
          name: 'delegateExpression',
          isAttr: true,
          type: 'String',
        },
        {
          name: 'expression',
          isAttr: true,
          type: 'String',
        },
        {
          name: 'resultVariableName',
          isAttr: true,
          type: 'String',
        },
      ],
    },
    {
      name: 'ExecutionListener',
      superClass: ['Element'],
      properties: [
        {
          name: 'event',
          isAttr: true,
          type: 'String',
        },
        {
          name: 'class',
          isAttr: true,
          type: 'String',
        },
        {
          name: 'delegateExpression',
          isAttr: true,
          type: 'String',
        },
        {
          name: 'expression',
          isAttr: true,
          type: 'String',
        },
      ],
    },
    {
      name: 'TaskListener',
      superClass: ['Element'],
      properties: [
        {
          name: 'event',
          isAttr: true,
          type: 'String',
        },
        {
          name: 'class',
          isAttr: true,
          type: 'String',
        },
        {
          name: 'delegateExpression',
          isAttr: true,
          type: 'String',
        },
        {
          name: 'expression',
          isAttr: true,
          type: 'String',
        },
      ],
    },
    {
      name: 'FormProperty',
      superClass: ['Element'],
      properties: [
        {
          name: 'id',
          isAttr: true,
          type: 'String',
        },
        {
          name: 'name',
          isAttr: true,
          type: 'String',
        },
        {
          name: 'type',
          isAttr: true,
          type: 'String',
        },
        {
          name: 'variable',
          isAttr: true,
          type: 'String',
        },
        {
          name: 'expression',
          isAttr: true,
          type: 'String',
        },
        {
          name: 'default',
          isAttr: true,
          type: 'String',
        },
        {
          name: 'datePattern',
          isAttr: true,
          type: 'String',
        },
        {
          name: 'required',
          isAttr: true,
          type: 'Boolean',
        },
        {
          name: 'readable',
          isAttr: true,
          type: 'Boolean',
        },
        {
          name: 'writable',
          isAttr: true,
          type: 'Boolean',
        },
        {
          name: 'placeholder',
          isAttr: true,
          type: 'String',
        },
      ],
    },
    {
      name: 'Properties',
      superClass: ['Element'],
      properties: [
        {
          name: 'values',
          isMany: true,
          type: 'Property',
        },
      ],
    },
    {
      name: 'Property',
      superClass: ['Element'],
      properties: [
        {
          name: 'name',
          isAttr: true,
          type: 'String',
        },
        {
          name: 'value',
          isAttr: true,
          type: 'String',
        },
      ],
    },
  ],
};

export default flowableModdleDescriptor;
